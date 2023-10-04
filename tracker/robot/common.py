import os




MOVABLE_BOARD_RANGE_X = [-0.85,0.85]
MOVABLE_BOARD_RANGE_Y = [-0.85,0.85]





REAL_BOARD_DIM_X = [-0.92,0.92]
REAL_BOARD_DIM_Y = [-0.92,0.92]


ROBOT_STEP_TIME = 0.5
CLOSE_ENOUGH_TO_STOP = 0.25
MAX_TIME_STEPS = 50


TARGET_STRING = 'red'
ANTITARGET_STRING = 'yellow'
TARGET_COLOR = (0, 0, 255)
ANTITARGET_COLOR = (0, 255, 0)
GAUNTLET_SCENARIOS = 120
EVAL_MAX_TIME_STEPS = 70



def mkdir(directory):
    if not os.path.exists(directory):
        os.makedirs(directory)

def mkdir_forfile(file):
    mkdir(os.path.dirname(file))