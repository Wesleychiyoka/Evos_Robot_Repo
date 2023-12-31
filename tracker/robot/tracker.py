import common
from datetime import datetime
import math
import json
import numpy as np
import cv2
import sys
sys.path.append(__file__ + '/../..')

# lowwer HSL, upper HSL, rectangle BGR color1, [rectangle BGR color2 - small - optional]
tracking_uppers_lowers = {}
# ([114,70,65],[125,158,125], (255, 0, 0)) #[19,100,0],[23,236,255],
# tracking_uppers_lowers['left'] = ([98, 14, 144], [107, 31, 154], (0, 255, 0))
# ([64,35,51],[82,105,111], (0, 255, 0))green #([0, 190, 157], [35, 255, 216], (0, 255, 0)) #yellow
# tracking_uppers_lowers['right'] = ([152, 45, 147], [172, 76, 174], (0, 255, 255))

# tracking_uppers_lowers['left'] = ([20, 100, 100], [30, 255, 255], (0, 165, 255))
# tracking_uppers_lowers['right'] = ([90, 50, 70], [128, 255, 255], (255, 0, 0))

tracking_uppers_lowers['left'] = ([20, 100, 100], [30, 255, 255], (0, 165, 255))
tracking_uppers_lowers['right'] = ([100, 50, 50], [130, 255, 255], (255, 0, 0))

# blue [85,74,117],[108,164,168], (255, 0, 0)
# yellow

# FOR RED-YELLOW
# tracking_uppers_lowers[common.ANTITARGET_STRING] = ([85,74,117],[108,164,168], common.ANTITARGET_COLOR)
# FOR SHAPES
# tracking_uppers_lowers[(common.TARGET_STRING, common.ANTITARGET_STRING)] = ([?, ?, ?], [?, ?, ?], (common.TARGET_COLOR, common.ANTITARGET_COLOR))

for name in tracking_uppers_lowers:
    tracking_uppers_lowers[name] = (np.array(tracking_uppers_lowers[name][0], np.uint8),
                                    np.array(
                                        tracking_uppers_lowers[name][1], np.uint8),
                                    tracking_uppers_lowers[name][2])


with open('../region.json', 'r') as fp:
    region = json.load(fp)


StartX = region['StartX']
StartY = region['StartY']
EndX = region['EndX']
EndY = region['EndY']
pixel_width = EndX - StartX  # maxwidth = 1, maxheight = ?
pixel_height = EndY - StartY


def image_to_cord(pixel_x, pixel_y):
    return np.array([image_to_cord_x(pixel_x),
                     image_to_cord_y(pixel_y)])


def image_to_cord_x(pixel_x):
    return ((pixel_x - StartX) / (pixel_width / 2)) - 1


def image_to_cord_y(pixel_y):
    return -1 * (((pixel_y - StartY) / (pixel_width / 2)) - 1)


def cord_to_image(cord):
    cord_x, cord_y = cord
    pixel_x = ((cord_x + 1) * (pixel_width / 2)) + StartX
    pixel_y = (((-1 * cord_y) + 1) * (pixel_width / 2)) + StartY
    return np.array([pixel_x, pixel_y])


bearing = 0
center = np.array([0, 0])
lost_center = True

trackers = {}

data_gen_point_list = {}
frame = None
bearing_history = []
record = False


def tracking_loop(detailed_tracking):
    global bearing, center, trackers, frame, lost_center
    cap = cv2.VideoCapture(0)
    while True:
        _, img = cap.read()
        frame = img
        if record:
            file = datetime.utcnow().strftime(
                '%Y%m%d_%HH%MM%SS.%f')[:-3] + '.png'
            dir = '../../output/robot/tracker'
            common.mkdir(dir)
            cv2.imwrite(dir + '/' + file, frame)

        hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)
        kernal = np.ones((5, 5), "uint8")

        internal_trackers = {}
        for name in tracking_uppers_lowers.keys():
            lowwer, upper, rect_colors = tracking_uppers_lowers[name]
            two_objects = len(rect_colors) == 2
            if two_objects:
                color1 = rect_colors[0]
                color2 = rect_colors[1]
            else:
                color1 = rect_colors
                color2 = None

            tracking = cv2.inRange(hsv, lowwer, upper)
            tracking = cv2.dilate(tracking, kernal)

            contours, hierarchy = cv2.findContours(
                tracking, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)

            best_area = 0
            best_pair = None
            second_best_area = 0
            second_best_pair = None
            for pic, contour in enumerate(contours):
                area = cv2.contourArea(contour)
                # x, y, w, h = rect
                # area = 1000 - (w * h)
                if area > best_area:
                    if two_objects:
                        second_best_area = best_area
                        second_best_pair = best_pair
                    best_pair = cv2.boundingRect(contour)
                    best_area = area
                elif two_objects and area > second_best_area:
                    second_best_pair = cv2.boundingRect(contour)
                    second_best_area = area

            if two_objects and best_pair is not None and second_best_pair is not None:
                _, y1, _, _ = best_pair
                _, y2, _, _ = second_best_pair

                if y2 < y1:
                    temp = best_pair
                    best_pair = second_best_pair
                    second_best_pair = temp

            if best_pair is not None:
                x, y, w, h = best_pair
                if detailed_tracking:
                    img = cv2.rectangle(img, (x, y), (x + w, y + h), color1, 1)
                temp_name = name[0] if two_objects else name
                internal_trackers[temp_name] = image_to_cord(
                    x + (w / 2), y + (h / 2))

            if second_best_pair is not None:
                x, y, w, h = second_best_pair
                if detailed_tracking:
                    img = cv2.rectangle(img, (x, y), (x + w, y + h), color2, 1)
                internal_trackers[name[1]] = image_to_cord(
                    x + (w / 2), y + (h / 2))

        for name in internal_trackers:
            if name != 'right' and name != 'left':
                # trackers are avoided during navigation
                trackers[name] = internal_trackers[name]

        for color in data_gen_point_list:
            pnts = data_gen_point_list[color]
            for p in pnts:
                cv2.circle(img, tuple(cord_to_image(p).astype(
                    np.int32)), 2, color, -1)  # target

        if 'right' in internal_trackers and 'left' in internal_trackers:
            right_coord = internal_trackers['right']
            left_coord = internal_trackers['left']

            lr = right_coord - left_coord
            # if np.linalg.norm(lr) <= 0.15:  # make sure parts are close together
            bearing = (-math.atan2(lr[1], lr[0])) % (math.pi * 2)
            bearing = (bearing / math.pi) * 180
            center = (right_coord + left_coord) / 2
            back_length = 0.01
            center = center + np.array([back_length * math.sin(bearing / 180 * math.pi),
                                        back_length * math.cos(bearing / 180 * math.pi)])

            bearing_center = (-math.atan2(center[0], center[1])) % (math.pi * 2)
            bearing_center = (bearing_center / math.pi) * 180
            
            bearing2 = (bearing + bearing_center + 180) % 360

            # print(bearing2)
            
            lost_center = False

            center_pixel = cord_to_image(center)
            if detailed_tracking:
                cv2.line(img, tuple(cord_to_image(right_coord).astype(np.int32)),
                         tuple(cord_to_image(left_coord).astype(np.int32)), (0, 255, 255), 1)
                ARROW_LENGTH = 0.2
                cv2.line(img, tuple(center_pixel.astype(np.int32)),
                         tuple(cord_to_image(center + np.array([ARROW_LENGTH * math.sin(bearing / 180 * math.pi),
                                                                ARROW_LENGTH * math.cos(
                                                                    bearing / 180 * math.pi)])).astype(np.int32)),
                         (0, 255, 255), 1)

                cv2.rectangle(img, tuple(cord_to_image([-1, 1]).astype(np.int32)),
                              tuple(cord_to_image([1, 1]).astype(np.int32)), (255, 255, 255), 1)
                cv2.line(img, tuple(cord_to_image(np.array([-1, 0])).astype(np.int32)),
                         tuple(cord_to_image(np.array([1, 0])).astype(np.int32)), (255, 255, 255), 1)
                cv2.line(img, tuple(cord_to_image(np.array([0, -1])).astype(np.int32)),
                         tuple(cord_to_image(np.array([0, 1])).astype(np.int32)), (255, 255, 255), 1)

                bearing_history.append(bearing)
                if len(bearing_history) > 10:
                    del bearing_history[0]

                ar = np.array(bearing_history)
                difference = np.max(ar) - np.min(ar)
                cv2.putText(img, '%f +/- %f' % (bearing, difference),
                            (10, 15), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255))
            else:
                lost_center = True
        # print(bearing)
        cv2.imshow("Color Tracking", img)
        if cv2.waitKey(10) & 0xFF == ord('q'):
            cap.release()
            cv2.destroyAllWindows()
            exit(0)


if __name__ == "__main__":
    # tracking_loop(True)
    import threading

    p2 = threading.Thread(target=tracking_loop, args=(True,))
    p2.start()

    # while True:
    #     if frame is not None:
    #         cv2.imshow('Onboard Camera', frame)

    #         # print('FPS: %f' % fps)
    #         if cv2.waitKey(100) == 0x1b:
    #             print('ESC pressed. Exiting ...')
    #             break
