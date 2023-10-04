import control
import tracker
import threading
import math
import time
import sys

data = open("data.csv", "w")

def log_data():
    while True:
        world_bearing = tracker.bearing
        pos = 83 * tracker.center
        dist = math.sqrt(pos[0] ** 2 + pos[1] ** 2)

        light_bearing = (-math.atan2(pos[0], pos[1])) % (math.pi * 2)
        light_bearing = (light_bearing / math.pi) * 180

        # Corrected angle
        bearing = (world_bearing + light_bearing + 180) % 360

        sensor_reading = control.get_sensor_reading()

        # Write csv: bearing,distance,position_x,position_y,[sensor_readings]
        data.write(
            ','.join(map(str, [bearing, dist, pos[0], pos[1], ';'.join(map(str, sensor_reading))])) + '\n')

        print("Bearing: " + str(bearing))
        print("Distance: " + str(dist))
        print("Position: " + str(pos))
        print(*sensor_reading, sep=';')
        print()
        time.sleep(0.5)


if __name__ == "__main__":
    # p1 = threading.Thread(target=control.connect_and_control)
    # p1 = threading.Thread(target=control.connect_and_move)
    p2 = threading.Thread(target=tracker.tracking_loop, args=(True,))
    p3 = threading.Thread(target=log_data)
    p1 = threading.Thread(target=control.move, args=(p3,))

    p2.daemon = True
    p3.daemon = True

    p1.start()
    p2.start()

    p1.join()
    data.close()
    sys.exit()
