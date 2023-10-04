import time
import serial
import threading
import os
from tkinter import *

MAX_MOTOR_SPEED = 17500.0  # (POSITIVE OR NEGATIVE)
MIN_MOTOR_SPEED = 8000.0  # (POSITIVE OR NEGATIVE)
MAX_MOTOR_TIME = 3100
MIN_MOTOR_TIME = 300
MOTOR_TIME = 400

comi = 0
ser = None

watchdog_running = False
watchdog_heartbeat = 0
watchdog_resets = 0

root = None

sensor_reading = None

def millis():
    return time.time() * 1000.0


def connect(comi):
    global ser
    try:
        port_path = 'COM%d' % comi
        ser = serial.Serial(
            port=port_path,
            baudrate=9600,
            parity=serial.PARITY_NONE,
            stopbits=serial.STOPBITS_ONE,
            bytesize=serial.EIGHTBITS,
            write_timeout=0
        )
        ser.isOpen()
        print('Connected on: %s' % port_path)
        return True
    except serial.serialutil.SerialException:
        return False


# def watchdog_loop():
#     global watchdog_heartbeat, ser, comi, watchdog_resets
#     while True:
#         if watchdog_heartbeat != 0 and watchdog_heartbeat < millis():
#             # times up
#             print('Watch dog alert! Restarting bluetooth service...')
#             ser = None
#             comi += 1
#
#             print('bluetooth service restart...')
#             os.popen('sudo -S ' + 'service bluetooth restart', 'w').write('cirl\n')
#             time.sleep(5)
#             print('bluetooth pair...')
#             p = os.popen('bt-device -c 00:07:80:96:99:F6', 'w')
#             time.sleep(2)
#             p.write('0000\n')
#             time.sleep(2)
#
#             def connect_com():
#                 print('bluetooth start comm connection...')
#                 os.popen('sudo -S rfcomm connect rfcomm%d 00:07:80:96:99:F6' % comi, 'w').write('cirl\n')
#                 time.sleep(3)
#
#             connect_com()
#             while not connect(comi):
#                 connect_com()
#             watchdog_heartbeat = 0
#             watchdog_resets += 1
#
#         time.sleep(0.05)


def open_port():
    global ser, watchdog_running, comi
    # configure the serial connections (the parameters differs on the device you are connecting to)
    # if not watchdog_running:
    #     watchdog_running = True
    #     p1 = threading.Thread(target=watchdog_loop, args=())
    #     p1.start()

    comi = 2
    while comi < 10:
        if connect(comi):
            break
        else:
            comi += 1
    # connect(comi)
    if ser is None:
        print('ERROR: Failed to connect')
        # exit(-1)
    else:
        print('Serial port opened.')


def set_speed(left, right):
    command = 'D,l' + str(left) + ',l' + str(right) + '\n'
    ser.write(command.encode())
    ser.flush()


def get_sensor_reading():
    message = 'O' + '\n'
    ser.write(message.encode())
    ser.flush()

    line = ser.readline()
    while not line.decode().strip().startswith('o'):
        line = ser.readline()

    return list(map(int, line.decode().strip().split(',')[1:-1]))


def set_scaled_speed(left, right):
    cLeft = left * 10000
    if cLeft < 0:
        cLeft -= 5000
    elif cLeft > 0:
        cLeft += 5000

    cRight = right * 10000
    if cRight < 0:
        cRight -= 5000
    elif cRight > 0:
        cRight += 5000

    cRight = int(cRight)
    cLeft = int(cLeft)

    set_speed(cLeft, cRight)


def set_scaled_speed_classic(left, right):
    cLeft = left * 5000
    if cLeft < 0:
        cLeft -= 8000
    elif cLeft > 0:
        cLeft += 8000

    cRight = right * 5000
    if cRight < 0:
        cRight -= 8000
    elif cRight > 0:
        cRight += 8000

    cRight = int(cRight)
    cLeft = int(cLeft)

    set_speed(cLeft, cRight)


def close_port():
    if ser is not None:
        set_speed(0, 0)
        ser.write('M\n'.encode())
        ser.flush()
        ser.close()
        print('Serial port closed. Remember to turn off the robot.')


keyPressed = {'w': False, 's': False, 'a': False, 'd': False}
flag = False

# def keyup(e):
#     if not (e.char in keyPressed.keys()):
#         return
#
#     keyPressed[e.char] = False
#     if not any(keyPressed.values()):
#         time.sleep(0.5)
#         print(0, 0)
#         set_speed(0, 0)

def keyup(e):
    global keyPressed
    keyPressed[e.char] = False

    if e.char == 'w' and not any(keyPressed.values()):
        time.sleep(0.5)
        print(0, 0)
        set_speed(0, 0)

    if e.char == 'd' and not any(keyPressed.values()):
        time.sleep(0.5)
        print(0, 0)
        set_speed(0, 0)

    if e.char == 'a' and not any(keyPressed.values()):
        time.sleep(0.5)
        print(0, 0)
        set_speed(0, 0)

    if e.char == 's' and not any(keyPressed.values()):
        time.sleep(0.5)
        print(0, 0)
        set_speed(0, 0)


def keydown(e):
    if not (e.char in keyPressed.keys()) or not keyPressed[e.char]:

        if e.char == 'w':
            keyPressed[e.char] = True
            print(8000, 8000)
            set_speed(8000, 8000)

        if e.char == 'd':
            keyPressed[e.char] = True
            print(8000, -8000)
            set_speed(8000, -8000)

        if e.char == 'a':
            keyPressed[e.char] = True
            print(-8000, 8000)
            set_speed(-8000, 8000)

        if e.char == 's':
            keyPressed[e.char] = True
            print(-8000, -8000)
            set_speed(-8000, -8000)


def log_data():
    print("Sensor readings: " + str(get_sensor_reading()))
    print()
    root.after(1000, log_data)


def connect_and_control():
    global root

    open_port()

    root = Tk()
    frame = Frame(root, width=200, height=200)
    frame.bind("<KeyPress>", keydown)
    frame.bind("<KeyRelease>", keyup)
    frame.pack()
    frame.focus_set()
    root.mainloop()

    close_port()


def connect_and_move():
    global root

    open_port()

    root = Tk()
    frame = Frame(root, width=200, height=200)
    frame.bind("<KeyPress>", lambda e: move2() if e.char == 's' else None)
    frame.pack()
    frame.focus_set()
    root.mainloop()

    close_port()


def move(logThread):
    # open_port()
    input("Start: ")
    logThread.start()
    print(8000, 8000)
    # set_speed(8000, 8000)
    time.sleep(2)
    print(-8000, 8000)
    # set_speed(-8000, 8000)
    time.sleep(4.96)
    print(0, 0)
    # set_speed(0, 0)
    time.sleep(0.5)
    # close_port()

def move2(logThread):
    open_port()

    logThread.start()

    for _ in range(6):
        move()

    set_speed(-8000, 8000)
    time.sleep(1.86)

    for _ in range(6):
        move()
    
    set_speed(-8000, 8000)
    time.sleep(1.24)

    for _ in range(4):
        move()

    set_speed(8000, -8000)
    time.sleep(1.24)
    set_speed(8000, 8000)
    time.sleep(2)
    set_speed(8000, -8000)
    time.sleep(0.62)

    for _ in range(6):
        move()

    set_speed(8000, -8000)
    time.sleep(1.86)

    for _ in range(6):
        move()

    close_port()

def test(child_conn):
    child_conn.send("Test!")
    child_conn.close()


if __name__ == '__main__':
    # open_port()
    # try:
    #     AMOUNT = 13000
    #     for i in range(5):
    #         print(get_sensor_reading())
    #         time.sleep(0.5)
    #         set_speed(AMOUNT, -AMOUNT)
    #         print('Turning one way.')
    #         time.sleep(0.5)
    #         set_speed(0, 0)
    #         time.sleep(0.5)
    #         set_speed(-AMOUNT, AMOUNT)
    #         print('Turning other way.')
    #         time.sleep(0.5)
    # except KeyboardInterrupt:
    #     pass

    # Robot input stuff
    # root = Tk()
    # frame = Frame(root, width=200, height=200)
    # frame.bind("<KeyPress>", keydown)
    # frame.bind("<KeyRelease>", keyup)
    # frame.pack()
    # frame.focus_set()
    # root.after(1000, log_data)
    # root.mainloop()

    # close_port()

    p1 = threading.Thread(target=move)
    p1.start()
    p1.join()
