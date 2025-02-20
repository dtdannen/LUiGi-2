"""
MarsWorld Domain methods for Pyhop 1.1.
Author: Dustin Dannenhauer <dtd212@lehigh.edu>, November 5, 2014
* Inspired by MudWorld in Molineaux and Aha ACS-2014
"""
import pyhop
from random import randint

"""
Assumptions of Domain:
1. The world is circular (no boundaries: going south when y=0 goes to y=6, etc)
2. The world is a square, there is a single dimension value, and tiles are 0 to dim-1
"""

def move(state, agent, dest):
    if state.agents[agent]:
        xy_str = state.agents[agent]
        if xy_str == dest:
            return [] # done
        x = int(xy_str.split(',')[0])
        y = int(xy_str.split(',')[1])
        x_dest = int(dest.split(',')[0])
        y_dest = int(dest.split(',')[1])
        # choose direction to go
        # (ask hector about this)
        if abs(x - x_dest) > abs(y - y_dest):
#            print("x="+str(x)+",x_dest="+str(x_dest)+"y="+str(y)+",y_dest="+str(y_dest))
            if x - x_dest > 0:
                return [('movewest', agent),('navigate', agent, dest)]
            else:
                return [('moveeast', agent),('navigate', agent, dest)]
        else:
            if y - y_dest > 0:
                return [('movesouth', agent),('navigate', agent, dest)]
            else:
                return [('movenorth', agent),('navigate', agent, dest)]
        
        # if we get here that means we are exactly equidistant in all
        # directions from the destination, so pick a random direction
        # rando_num = randint(1,4)
        # if rando_num == 1:
        #     return [('movesouth', agent),('move', agent, dest)]      
        # elif rando_num == 2:
        #     return [('movenorth', agent),('move', agent, dest)]
        # elif rando_num == 3:
        #     return [('moveeast', agent),('move', agent, dest)]
        # elif rando_num == 4:
        #     return [('movewest', agent),('move', agent, dest)]
        
        return False


def perimeterize(state, agent, beacon_locs): 
    # if there are no more beacon locs we are done
    #print('beacon_locs are '+str(beacon_locs))
    if beacon_locs: 
        xy_str = state.agents[agent]
        # get the first beacon loc
        return [('navigate',agent,beacon_locs[0]),
                ('dropbeacon', agent),
                ('make_perimeter', agent, beacon_locs[1:])]
    elif beacon_locs == []:
        return [] # done
    else: 
        return False


pyhop.declare_methods('navigate',move)
pyhop.declare_methods('make_perimeter',perimeterize)
#pyhop.declare_methods('movenorth',movenorth)
#pyhop.declare_methods('moveeast',moveeast)
#pyhop.declare_methods('movewest',movewest)
#pyhop.declare_methods('movesouth',movesouth)



######################################################################
## This model is how we could do it in a pure vanilla way but I when I
## do it this way, it runs in an infinite loop because the planner
## does depth first search instead of breadth first search

# def movenorth(state, agent, dest):
#     if state.agents[agent] == dest:
#         return [] # done
#     else:
#         return [('movenorth', agent),('navigate', agent, dest)]

# def moveeast(state, agent, dest):
#     if state.agents[agent] == dest:
#         return [] # done
#     else:
#         return [('moveeast', agent),('navigate', agent, dest)]

# def movewest(state, agent, dest):
#     if state.agents[agent] == dest:
#         return [] # done
#     else:
#         return [('movewest', agent),('navigate', agent, dest)]

# def movesouth(state, agent, dest):
#     if state.agents[agent] == dest:
#         return [] # done
#     else:
#         return [('movesouth', agent),('navigate', agent, dest)]

# pyhop.declare_methods('navigate', movenorth,moveeast,movewest,movesouth)

## End vanilla, pure planning model
######################################################################




######################################################################
# Old Content
######################################################################



# """ This allows other methods to end with subtask 'deliver' and not
#  recur infinitely if the package is already at destination """
# def is_done_check(state, obj, truck, ap, fromloc, toloc):
#         if (state.pkgs[obj] == toloc):
#                 return [] # done
#         else:
#                 return False
                
# """ Transport package in same city with truck at start loc """
# def incity_atstart(state, obj, truck, ap, fromloc, toloc):
# 	if (state.city[fromloc] == state.city[toloc] and 
#             state.pkgs[obj] == fromloc and
#             state.trucks[truck] == fromloc):
# 		return [('loadtruck', obj, truck),
# 		        ('drivetruck', truck, fromloc, toloc),
# 		        ('unloadtruck', obj, truck),
#                         ('deliver', obj, truck, ap, fromloc, toloc)]                        
#         else:
#                 return False

# """ Transport package in same city with no truck at start loc """
# def incity_notatstart(state, obj, truck, ap, fromloc, toloc):
# 	if (state.city[fromloc] == state.city[toloc] and 
#             state.pkgs[obj] == fromloc and
#             state.trucks[truck] != fromloc):
#                 origTruckPos = state.trucks[truck]
# 		return [('drivetruck', truck, origTruckPos, fromloc),
#                         ('deliver', obj, truck, ap, fromloc, toloc)]
#         else:
#                 return False

# """ Transport package different city, pkg at airport, airplane at
#     airport """
# def diffcity_obj_ap_with_ap(state, obj, truck, ap, fromloc, toloc):
#         if (state.city[fromloc] != state.city[toloc] and
#             fromloc in state.aprts.keys() and # safety check for airport lookup
#             state.aprts[fromloc] and # do lookup
#             toloc in state.aprts.keys() and # another safety check
#             state.aprts[toloc] and  # do lookup
#             state.pkgs[obj] == fromloc and # is this redundant? I think so
#             state.aplns[ap] == fromloc):
                
#                 return [('loadairplane',obj,ap),
#                         ('flyairplane', ap, fromloc, toloc),
#                         ('unloadairplane',obj,ap),
#                         ('deliver', obj, truck, ap, fromloc, toloc)]             

#         else:
#                 return False


# """ Transport package different city, pkg at airport, airplane not at
#     airport """
# def diffcity_obj_ap_no_ap(state, obj, truck, ap, fromloc, toloc):
#         if (state.city[fromloc] != state.city[toloc] and
#             fromloc in state.aprts.keys() and # safety check for airport lookup
#             state.aprts[fromloc] and # do lookup
#             toloc in state.aprts.keys() and # another safety check
#             state.aprts[toloc] and  # do lookup
#             state.aplns[ap] != fromloc):
#                 origAplnPos = state.aplns[ap]                
#                 return [('flyairplane', ap, origAplnPos, fromloc),
#                         ('deliver',obj, truck, ap, fromloc, toloc)]
#         else:
#                 return False

# """ Transport package different city, pkg not at airport, airplane at
#     airport """
# def diffcity_obj_not_ap_no_ap(state, obj, truck, ap, fromloc, toloc):
#         # different city
#         if (state.city[fromloc] != state.city[toloc] and
#             # pkg loc is not an airport
#             (not (state.pkgs[obj] in state.aprts.keys())) and
#             # dest is an airport 
#             toloc in state.aprts.keys()):

#                 start_city_airport = None
#                 for airport in state.aprts:
#                         if state.city[airport] == state.city[fromloc]:
#                                 start_city_airport = airport
#                 if start_city_airport:
                        
#                         return [('deliver', obj, truck, ap, fromloc, start_city_airport),
#                                 ('deliver', obj, truck, ap, start_city_airport, toloc)]
        
#         return False

# """ Transport package different cities and recur appropriately """
# def diffcity_general(state, obj, truck, ap, fromloc, toloc):
#         if state.city[fromloc] != state.city[toloc]:
#                 # find an airport in this city and dest city
#                 start_city_airport = None
#                 dest_city_airport = None
#                 for airport in state.aprts:
#                         if state.city[airport] == state.city[fromloc]:
#                                 start_city_airport = airport
#                         elif state.city[airport] == state.city[toloc]:
#                                 dest_city_airport = airport
                                
#                 # find a truck in the start city
#                 start_city_truck = None
#                 for tr_key in state.trucks.keys():
#                         if state.city[state.trucks[tr_key]] == state.city[fromloc]:
#                                 start_city_truck = tr_key

#                 # find a truck in the dest city
#                 dest_city_truck = None
#                 for tr_key in state.trucks.keys():
#                         if state.city[state.trucks[tr_key]] == state.city[toloc]:
#                                 dest_city_truck = tr_key

#                 # find some airplane
#                 some_apln = None
#                 if len(state.aplns) > 0:
#                         some_apln = state.aplns.keys()[0]
                                
#                 # did we find airports? if so, return plan
#                 if (start_city_airport and dest_city_airport and
#                     start_city_truck and dest_city_truck and some_apln):                        
#                         return [('deliver', obj, start_city_truck, some_apln, fromloc, start_city_airport),
#                                 ('deliver', obj, truck, some_apln, start_city_airport, dest_city_airport),
#                                 ('deliver', obj, dest_city_truck, some_apln, dest_city_airport, toloc)]

#         return False
        

# pyhop.declare_methods('deliver', is_done_check, incity_atstart, 
#                       incity_notatstart, diffcity_obj_ap_with_ap,
#                       diffcity_obj_ap_no_ap, diffcity_obj_not_ap_no_ap, diffcity_general)

