"""
MarsWorld Domain methods for Pyhop 1.1.
Author: Dustin Dannenhauer <dtd212@lehigh.edu>, November 5, 2014
* Inspired by MudWorld in Molineaux and Aha ACS-2014
"""
import pyhop

""" World coordinates are bounded by [0,state.dim-1] for both x and y
"""

""" move down """
def movesouth(state, agent):
    if state.agents[agent] not in state.mud.values():
        xy_str = state.agents[agent]
        x = int(xy_str.split(',')[0])
        y = int(xy_str.split(',')[1])
        new_y = y
        if y == 0:
            new_y = state.dim['dim']-1
        else:
            new_y = y - 1

        new_xy_str = str(x)+','+str(new_y)
        state.agents[agent] = new_xy_str
        return state
    else:
        return False

""" move up """
def movenorth(state, agent):
    if state.agents[agent] not in state.mud.values():
        xy_str = state.agents[agent]
        x = int(xy_str.split(',')[0])
        y = int(xy_str.split(',')[1])
        new_y = y
        if y == state.dim['dim']-1:
            new_y = 0
        else:
            new_y = y + 1

        new_xy_str = str(x)+','+str(new_y)
        state.agents[agent] = new_xy_str
        return state
    else:
        return False

""" move left """
def movewest(state, agent):
    if state.agents[agent] not in state.mud.values():
        xy_str = state.agents[agent]
        x = int(xy_str.split(',')[0])
        y = int(xy_str.split(',')[1])
        new_x = x
        if x == 0:
            new_x = state.dim['dim']-1
        else:
            new_x = x - 1

        new_xy_str = str(new_x)+','+str(y)
        state.agents[agent] = new_xy_str
        return state
    else:
        return False

""" move right """
def moveeast(state, agent):
    if state.agents[agent] not in state.mud.values():
        xy_str = state.agents[agent]
        x = int(xy_str.split(',')[0])
        y = int(xy_str.split(',')[1])
        new_x = x
        if x == state.dim['dim']-1:
            new_x = 0
        else:
            new_x = x + 1

        new_xy_str = str(new_x)+','+str(y)
        state.agents[agent] = new_xy_str
        return state
    else:
        return False

def dropbeacon(state, agent):
    if state.agents[agent]:
        xy_str = state.agents[agent]
        x = int(xy_str.split(',')[0])
        y = int(xy_str.split(',')[1])
        new_x = x
        if xy_str in state.beacons.values():
            return False # there is already a beacon here
        else:
            beaconStr = 'b'+str(x)+','+str(y)
            state.beacons[beaconStr] = xy_str
            return state
    else:
        return False    

pyhop.declare_operators(movenorth, movesouth, moveeast, movewest, dropbeacon)



# def loadtruck(state, obj, truck):
# 	if state.trucks[truck] == state.pkgs[obj]:
# 		state.pkgs[obj] = truck
# 		return state
# 	else: return False

# def unloadtruck(state, obj, truck):
# 	if state.pkgs[obj] == truck:
# 		state.pkgs[obj] = state.trucks[truck]
# 		return state
# 	else: return False	

# def loadairplane(state, obj, apln):
# 	if state.pkgs[obj] == state.aplns[apln]:
# 		state.pkgs[obj] = apln
# 		return state
# 	else: return False

# def unloadairplane(state, obj, apln):
# 	if state.pkgs[obj] == apln:
# 		state.pkgs[obj] = state.aplns[apln]
# 		return state
# 	else: return False	

# def drivetruck(state, truck, locfrom, locto):
# 	if (state.trucks[truck] == locfrom and
#             state.city[state.trucks[truck]] == state.city[locto]): # trucks can only travel within a city
# 		state.trucks[truck] = locto
# 		return state
# 	else: return False

# def flyairplane(state, apln, locfrom, locto):
# 	if state.aplns[apln] == locfrom:
# 		state.aplns[apln] = locto
# 		return state
# 	else: return False

# pyhop.declare_operators(loadtruck, unloadtruck, loadairplane, unloadairplane, drivetruck, flyairplane)

