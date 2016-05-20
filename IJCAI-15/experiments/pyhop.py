"""
MODIFIED BY DUSTIN DANNENHAUER in order to produce expectations


Pyhop, version 1.2.2 -- a simple SHOP-like planner written in Python.
Author: Dana S. Nau, 2013.05.31

Copyright 2013 Dana S. Nau - http://www.cs.umd.edu/~nau

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
Pyhop should work correctly in both Python 2.7 and Python 3.2.
For examples of how to use it, see the example files that come with Pyhop.

Pyhop provides the following classes and functions:

- foo = State('foo') tells Pyhop to create an empty state object named 'foo'.
  To put variables and values into it, you should do assignments such as
  foo.var1 = val1

- bar = Goal('bar') tells Pyhop to create an empty goal object named 'bar'.
  To put variables and values into it, you should do assignments such as
  bar.var1 = val1

- print_state(foo) will print the variables and values in the state foo.

- print_goal(foo) will print the variables and values in the goal foo.

- declare_operators(o1, o2, ..., ok) tells Pyhop that o1, o2, ..., ok
  are all of the planning operators; this supersedes any previous call
  to declare_operators.

- print_operators() will print out the list of available operators.

- declare_methods('foo', m1, m2, ..., mk) tells Pyhop that m1, m2, ..., mk
  are all of the methods for tasks having 'foo' as their taskname; this
  supersedes any previous call to declare_methods('foo', ...).

- print_methods() will print out a list of all declared methods.

- pyhop(state1,tasklist) tells Pyhop to find a plan for accomplishing tasklist
  (a list of tasks), starting from an initial state state1, using whatever
  methods and operators you declared previously.

- In the above call to pyhop, you can add an optional 3rd argument called
  'verbose' that tells pyhop how much debugging printout it should provide:
- if verbose = 0 (the default), pyhop returns the solution but prints nothing;
- if verbose = 1, it prints the initial parameters and the answer;
- if verbose = 2, it also prints a message on each recursive call;
- if verbose = 3, it also prints info about what it's computing.
"""

# Pyhop's planning algorithm is very similar to the one in SHOP and JSHOP
# (see http://www.cs.umd.edu/projects/shop). Like SHOP and JSHOP, Pyhop uses
# HTN methods to decompose tasks into smaller and smaller subtasks, until it
# finds tasks that correspond directly to actions. But Pyhop differs from 
# SHOP and JSHOP in several ways that should make it easier to use Pyhop
# as part of other programs:
# 
# (1) In Pyhop, one writes methods and operators as ordinary Python functions
#     (rather than using a special-purpose language, as in SHOP and JSHOP).
# 
# (2) Instead of representing states as collections of logical assertions,
#     Pyhop uses state-variable representation: a state is a Python object
#     that contains variable bindings. For example, to define a state in
#     which box b is located in room r1, you might write something like this:
#     s = State()
#     s.loc['b'] = 'r1'
# 
# (3) You also can define goals as Python objects. For example, to specify
#     that a goal of having box b in room r2, you might write this:
#     g = Goal()
#     g.loc['b'] = 'r2'
#     Like most HTN planners, Pyhop will ignore g unless you explicitly
#     tell it what to do with g. You can do that by referring to g in
#     your methods and operators, and passing g to them as an argument.
#     In the same fashion, you could tell Pyhop to achieve any one of
#     several different goals, or to achieve them in some desired sequence.
# 
# (4) Unlike SHOP and JSHOP, Pyhop doesn't include a Horn-clause inference
#     engine for evaluating preconditions of operators and methods. So far,
#     I've seen no need for it; I've found it easier to write precondition
#     evaluations directly in Python. But I could consider adding such a
#     feature if someone convinces me that it's really necessary.
# 
# Accompanying this file are several files that give examples of how to use
# Pyhop. To run them, launch python and type "import blocks_world_examples"
# or "import simple_travel_example".


from __future__ import print_function
import copy,sys, pprint
import random # for generating mud tiles

import os

############################################################
# States and goals

class State():
    """A state is just a collection of variable bindings."""
    def __init__(self,name):
        self.__name__ = name

class Goal():
    """A goal is just a collection of variable bindings."""
    def __init__(self,name):
        self.__name__ = name

### print_state and print_goal are identical except for the name

def print_state(state,indent=4):
    """Print each variable in state, indented by indent spaces."""
    if state != False:
        for (name,val) in vars(state).items():
            if name != '__name__':
                for x in range(indent): sys.stdout.write(' ')
                sys.stdout.write(state.__name__ + '.' + name)
                print(' =', val)
    else: print('False')

# same thing as print_state, just returns string instead of printing it
def str_state(state,indent=4):
    """Print each variable in state, indented by indent spaces."""

    result = ""
    if state != False:
        for (name,val) in vars(state).items():
            if name != '__name__':
                for x in range(indent): result = result + ' ' #sys.stdout.write(' ')
                #sys.stdout.write(state.__name__ + '.' + name)
                result = result + state.__name__ + '.' + name
                result = result + " = " + str(val) + "\n" 

    else: result = "False"
    return result

# same thing as str_state, but as small a string as possible
def str_state_compressed(state,indent=4):
    """Print each variable in state, indented by indent spaces."""

    result = ""
    if state != False:
        for (name,val) in vars(state).items():
            if name != '__name__':
                #sys.stdout.write(state.__name__ + '.' + name)
                result = result + name[0] + ":" + str(val)+","

    else: result = "False"
    return result

def str_state_explicit_newline(state,indent=1):
    """Print each variable in state, indented by indent spaces."""
    result = ""
    if state != False:
        for (name,val) in vars(state).items():
            if name != '__name__':
                for x in range(indent): result = result + ' ' #sys.stdout.write(' ')
                #sys.stdout.write(state.__name__ + '.' + name)
                result = result + state.__name__ + '.' + name
                result = result + " = " + str(val) + "\\n " 

    else: result = "False"
    return result

def print_goal(goal,indent=4):
    """Print each variable in goal, indented by indent spaces."""
    if goal != False:
        for (name,val) in vars(goal).items():
            if name != '__name__':
                for x in range(indent): sys.stdout.write(' ')
                sys.stdout.write(goal.__name__ + '.' + name)
                print(' =', val)
    else: print('False')

def str_goal(goal, indent=4):
    """Print each variable in goal, indented by indent spaces."""
    result = ""
    if goal != False:
        for (name,val) in vars(goal).items():
            if name != '__name__':
                for x in range(indent): result = result + " "
                result = result + goal.__name__ + "." + name
                result = result + "= " + val

    else: result = result + "False"
    return result

# returns a nice string representation of the task
def str_task(task):
    #print("Task is: " + str(task))
    return str(task)

KEYNOTFOUND = '<KEYNOTFOUND>'       # KeyNotFound for dictDiff

def pure_dict_diff(first, second):
    """ Return a dict of keys that differ with another config object.  If a value is
        not found in one fo the configs, it will be represented by KEYNOTFOUND.
        @param first:   Fist dictionary to diff.
        @param second:  Second dicationary to diff.
        @return diff:   Dict of Key => (first.val, second.val)
    """
    diff = {}
    # Check all keys in first dict
    for key in first.keys():
        if (not second.has_key(key)):
            diff[key] = (first[key], KEYNOTFOUND)
        elif (first[key] != second[key]):
            diff[key] = (first[key], second[key])
    # Check all keys in second dict to find missing
    for key in second.keys():
        if (not first.has_key(key)):
            diff[key] = (KEYNOTFOUND, second[key])
    return diff

##### This code borrowed from: http://code.activestate.com/recipes/576644-diff-two-dictionaries/
#### The original author is NOT myself (Dustin Dannenhauer)
#### Modifications by Dustin Dannenhauer in order to return values from only the second dictionary, when they differ
KEYNOTFOUND = '<KEYNOTFOUND>'       # KeyNotFound for dictDiff

def dict_diff(first, second):
    """ Return a dict of keys that differ with another config object.  If a value is
        not found in one fo the configs, it will be represented by KEYNOTFOUND.
        @param first:   Fist dictionary to diff.
        @param second:  Second dicationary to diff.
        @return diff:   Dict of Key => (first.val, second.val)
    """
    diff = {}
    # Check all keys in first dict
    for key in first.keys():
        if (not second.has_key(key)):
            diff[key] = (first[key], KEYNOTFOUND)
        elif (first[key] != second[key]):
            
            # diff[key] = (first[key], second[key]) ## Original line
            diff[key] = second[key] ## Modification by Dustin Dannenhauer

    # Check all keys in second dict to find missing
    for key in second.keys():
        if (not first.has_key(key)):
            diff[key] = second[key]
    return diff

def pure_state_diff(state1, state2):
    # returns only what is different in these two states
    # and returns the same states
    exists_any_difference = False
    new_state = State('diff_state')
    if state1 != False and state2 != False:
        for (name,state1_dict) in vars(state1).items():
            if isinstance(state1_dict, dict):
                state2_final_dict = {}
                for (name2,state2_curr_dict) in vars(state2).items():
                    if name == name2:
                        state2_final_dict = state2_curr_dict
                if isinstance(state2_final_dict, dict):
                    curr_dict_diff = pure_dict_diff(state1_dict, state2_final_dict)
                    if curr_dict_diff:
                        setattr(new_state,name,curr_dict_diff)
                        exists_any_difference = True
    if not exists_any_difference:
        return False
    return new_state

def state_diff(state1, state2):
    # returns only what is different in these two states
    # and returns the same states
    new_state = State('diff_state')
    if state1 != False and state2 != False:
        for (name,state1_dict) in vars(state1).items():
            if isinstance(state1_dict, dict):
                state2_final_dict = {}
                for (name2,state2_curr_dict) in vars(state2).items():
                    if name == name2:
                        state2_final_dict = state2_curr_dict
                if isinstance(state2_final_dict, dict):
                    curr_dict_diff = dict_diff(state1_dict, state2_final_dict)
                    if curr_dict_diff:
                        setattr(new_state,name,state2_final_dict)
    return new_state
# returns true if state1 is a subset of state2, false otherwise
def state_subset_of(state1, state2):
    if state1 != False and state2 != False:        
        for (name,state1_dict) in vars(state1).items():
            if isinstance(state1_dict, dict) and len(state1_dict) > 0:
                matching_dict_exists = False
                for (name2,state2_curr_dict) in vars(state2).items():
                    if name == name2:   
                        matching_dict_exists = True
                        valid_subset = all(item in state2_curr_dict.items() for item in state1_dict.items())
                        if not valid_subset:
                            return False
                if not matching_dict_exists: 
                    return False
    return True

# special function by Dustin Dannenhauer
# updates the first state to contain any new values the seond state has
# as long as they don't override the first the state, if they do, use the second states values
def update_state(state1, state2):
    resulting_diff_state = state_diff(state1, state2)


    # returns only what is different in these two states
    # and returns the same states
    new_state = copy.deepcopy(state1)
    if state1 != False and state2 != False:
        for (name,state1_dict) in vars(state1).items():
            if isinstance(state1_dict, dict):
                state2_final_dict = {}
                for (name2,state2_curr_dict) in vars(state2).items():
                    if name == name2:
                        state2_final_dict = state2_curr_dict

                if isinstance(state2_final_dict, dict):
                    new_state1_dict = copy.deepcopy(state1_dict)
                    new_state1_dict.update(state2_final_dict)
                    setattr(new_state,name,new_state1_dict)

    # if anything exists in state2 that wasn't in state1, add it
        for (name,state2_dict) in vars(state2).items():
            if isinstance(state2_dict, dict):
                state2_final_dict = {}
                exists_in_state1 = False
                #print("vars(state1).keys() is "+str(vars(state1).keys()))
                for name_1 in vars(state1).keys():
                    if name == name_1:
                        state2_final_dict = state2_curr_dict
                        exists_in_state1 = True        
                if not exists_in_state1:
                    setattr(new_state,name,state2_dict)                    

    #print("--------------------------------------")
    #print("---- state1 ----")
    #print_state(state1)
    # print("---- state2 ----")
    # print_state(state2)
    # print("---- udpated state ---")
    # print_state(new_state)
    # print("--------------------------------------")
    return new_state

                    
            # for key,val in state1_dict:
            #     if name != '__name__':
            #         if state2.name[key] != val:
            #             sys.stdout.write(state.__name__ + '.' + name)
            #             print(' =', state1_dict)

############################################################
# Helper functions that may be useful in domain models

def forall(seq,cond):
    """True if cond(x) holds for all x in seq, otherwise False."""
    for x in seq:
        if not cond(x): return False
    return True

def find_if(cond,seq):
    """
    Return the first x in seq such that cond(x) holds, if there is one.
    Otherwise return None.
    """
    for x in seq:
        if cond(x): return x
    return None

############################################################
# Commands to tell Pyhop what the operators and methods are

operators = {}
methods = {}

def declare_operators(*op_list):
    """
    Call this after defining the operators, to tell Pyhop what they are. 
    op_list must be a list of functions, not strings.
    """
    operators.update({op.__name__:op for op in op_list})
    return operators

def declare_methods(task_name,*method_list):
    """
    Call this once for each task, to tell Pyhop what the methods are.
    task_name must be a string.
    method_list must be a list of functions, not strings.
    """
    methods.update({task_name:list(method_list)})
    return methods[task_name]

############################################################
# Commands to find out what the operators and methods are

def print_operators(olist=operators):
    """Print out the names of the operators"""
    print('OPERATORS:', ', '.join(olist))

def print_methods(mlist=methods):
    """Print out a table of what the methods are for each task"""
    print('{:<14}{}'.format('TASK:','METHODS:'))
    for task in mlist:
        print('{:<14}'.format(task) + ', '.join([f.__name__ for f in mlist[task]]))

############################################################
# The actual planner

def pyhop(state,tasks,verbose=0):
    """
    Try to find a plan that accomplishes tasks in state. 
    If successful, return the plan. Otherwise return False.
    """
    if verbose>0: print('** pyhop, verbose={}: **\n   state = {}\n   tasks = {}'.format(verbose, state.__name__, tasks))
    result = seek_plan(state,tasks,[],0,verbose)
    if verbose>0: print('** result =',result,'\n')
    return result

def seek_plan(state,tasks,plan,depth,verbose=0):
    """
    Workhorse for pyhop. state and tasks are as in pyhop.
    - plan is the current partial plan.
    - depth is the recursion depth, for use in debugging
    - verbose is whether to print debugging messages
    """
    if verbose>1: print('depth {} tasks {}'.format(depth,tasks))
    if tasks == []:
        if verbose>2: print('depth {} returns plan {}'.format(depth,plan))
        return plan
    task1 = tasks[0]
    if task1[0] in operators:
        if verbose>2: print('depth {} action {}'.format(depth,task1))
        operator = operators[task1[0]]
        newstate = operator(copy.deepcopy(state),*task1[1:])
        if verbose>2:
            print('depth {} new state:'.format(depth))
            #print_state(newstate)
        if newstate:
            solution = seek_plan(newstate,tasks[1:],plan+[task1],depth+1,verbose)
            if solution != False:
                return solution
    if task1[0] in methods:
        if verbose>2: print('depth {} method instance {}'.format(depth,task1))
        relevant = methods[task1[0]]
        for method in relevant:
            subtasks = method(state,*task1[1:])
            # Can't just say "if subtasks:", because that's wrong if subtasks == []
            if verbose>2:
                print('depth {} new tasks: {}'.format(depth,subtasks))
            if subtasks != False:
                solution = seek_plan(state,subtasks+tasks[1:],plan,depth+1,verbose)
                if solution != False:
                    return solution
    if verbose>2: print('depth {} returns failure'.format(depth))
    return False

################################################
## Breadth-First-Search instead of DFS
## Modification by Dustin Dannenhauer
## NOT WORKING 
################################################

# def seek_plan(state,tasks,plan,depth,verbose=0):
#     """
#     Workhorse for pyhop. state and tasks are as in pyhop.
#     - plan is the current partial plan.
#     - depth is the recursion depth, for use in debugging
#     - verbose is whether to print debugging messages
#     """
#     if verbose>1: print('depth {} tasks {}'.format(depth,tasks))
#     if tasks == []:
#         if verbose>2: print('depth {} returns plan {}'.format(depth,plan))
#         return plan
    
#     task1 = tasks[0]
#     if task1[0] in operators:
#         if verbose>2: print('depth {} action {}'.format(depth,task1))
#         operator = operators[task1[0]]
#         newstate = operator(copy.deepcopy(state),*task1[1:])
#         if verbose>2:
#             print('depth {} new state:'.format(depth))
#             print_state(newstate)
#         if newstate:
#             print('finished exploring task '+str(task1))
#             solution = seek_plan(newstate,tasks[1:],plan+[task1],depth+1,verbose)
#             if solution != False:
#                 return solution
#     if task1[0] in methods:
#         if verbose>2: print('depth {} method instance {}'.format(depth,task1))
#         relevant = methods[task1[0]]
#         for method in relevant:
#             subtasks = method(state,*task1[1:])
#             # Can't just say "if subtasks:", because that's wrong if subtasks == []
#             if verbose>2:
#                 print('depth {} new tasks: {}'.format(depth,subtasks))
#             if subtasks != False:
#                 solution = seek_plan(state,subtasks+tasks[1:],plan,depth+1,verbose)
#                 if solution != False:
#                     return solution
#     if verbose>2: print('depth {} returns failure'.format(depth))
#     return False


############################################################
# Simulator (Work by Dustin Dannenhauer)

# returns a state with mud occuring at some % of locations randomly
# NOTE: this function gauruntees there is never mud at the agent's start state
def initialize_mud(state):
    state.mud = {}
    for x in range(0,state.dim['dim']):
        for y in range(0,state.dim['dim']):
            if random.random() < 0.10:
                state.mud['mud'+str(x)+','+str(y)] = str(x)+','+str(y)

    # if mud was generated at the agent's start state, remove it
    for mudkey,mudcoord in state.mud.items():
        if state.agents.values()[0] == mudcoord:
            del state.mud[mudkey]

    return state
    
# input: the true state
# output: the view of the state from the agent            
def update_agent_perceived_state(state):
    agent_state = copy.deepcopy(state)
    # Remove all mud beyond agent's perception distance of 1 tile
    agent_xy_str = state.agents.values()[0] # get the first agent's x,y string
    agent_x = int(agent_xy_str.split(',')[0])
    agent_y = int(agent_xy_str.split(',')[1])
    for mud_xy_str in state.mud.values():
        mud_x = int(mud_xy_str.split(',')[0])
        mud_y = int(mud_xy_str.split(',')[1])
        if not abs(mud_x - agent_x) + abs(mud_y - agent_y) <= 1:
            # here distance is 1 away, or less
            del agent_state.mud['mud'+mud_xy_str]

    # Remove any beacons that are at the same location as a rad cloud
    for beacon_xy in agent_state.beacons.values():
        if beacon_xy in agent_state.rad_clouds.keys():
            del agent_state.beacons['b'+beacon_xy]
    
    # ensure the agent cannot see rad clouds
    agent_state.rad_clouds = {}

    return agent_state

# to be called before each action is executed in the state 
# this will do two things:
# 1. it will manage the clouds (creating them, deleting them, etc)
# 2. it will remove any beacons from the state if
## rand_state is the current state of the random number generator to be used
def update_rad_clouds(state,verbose=4):
    #print('vars(state) returns '+str(vars(state).keys))
    state = copy.deepcopy(state)
    if 'rad_clouds' not in vars(state).keys():
        if verbose == 4: print('making a new rad_clouds in state')
        state.rad_clouds = {}

    # chance per tile a cloud will appear
    likelihood = 0.10
    duration = 3 # number of ticks in the environment the cloud lasts
    for x in range(0,state.dim['dim']):
        for y in range(0,state.dim['dim']):
            cloud_id = (str(x)+','+str(y))
            #if (str(x)+','+str(y)) in state.rad_clouds.keys():
            #for curr_cloud_str in state.rad_clouds.values():
            #    print('comparing curr_cloud_str '+str(curr_cloud_str) + ' to '+cloud_id)
            if cloud_id in state.rad_clouds.keys():
                if verbose == 4: print('cloud '+cloud_id+' already exists so decrementing')                
                state.rad_clouds[cloud_id] = state.rad_clouds[cloud_id] - 1 # decrement if exists
                if state.rad_clouds[cloud_id] == 0:
                    if verbose == 4: print('cloud '+cloud_id+' has finished its duration, about to delete')                
                    del state.rad_clouds[cloud_id] # delete if this was last tick                                    
            elif random.random() < likelihood:
                state.rad_clouds[str(x)+','+str(y)] = duration # make a new cloud
    if verbose == 4: print('Created new rad clouds: '+str(state.rad_clouds.keys()))
       
    return state

import time

# return a file handle to write to, dont forget to close it
def open_data_file_for_experiment():
    dir_str = "data/" + time.strftime("%a-%b-%d/")
    if not os.path.exists(dir_str):
        os.makedirs(dir_str)
    file_str = "data/" + time.strftime("%a-%b-%d/%H-%M-%S.csv")
    output_file_handle = open(file_str,'w')
    return output_file_handle

def open_log_file_for_experiment():
    file_str = "data/" + time.strftime("%a-%b-%d/%H-%M-%S.log")
    output_file_handle = open(file_str,'w')
    # agent is either effects, state, cumulative
    # plan-result is either 0 for failure, 1 for success
    # exp-result is either 0 for missed, 1 for false positive, 2 for successful catch
    return output_file_handle

""" Simulator for MarsWorld. The output is a triple containing the plan
    result, agent-id, and the failure-result if any

    Output Codes: <run-id,plan-result,agent-id,failure-result>

    Run-Id is passed in and every call to a simulation should have its own unique call id

    Plan Result Output Codes:

        1 - plan successfully executed
        2 - agent successfully triggered GDA cycle using pre expectations and discrepancy detection
        3 - agent successfully triggered GDA cycle using post expectations and discrepancy detection
        4 - agent incorrectly triggered GDA cycle using expectations (false positive)
        5 - agent missed detecting an expectation
        6 - plan failure

    Agent Id Output Codes:

        1 - Effects only
        2 - Complete State
        3 - Cumulative Effects only

    Failure Result Codes:

        'mud'   - agent failed to detect mud
        'cloud' - agent failed to detect a beacon interrupted by a cloud

    Example return triples:

    4,1,mud = plan failed and agent 1 missed detecting mud
    2,2,cloud = agent 2 successfully detected a beacon missing

"""
AGENT1 = 1 # immediate effects only
AGENT2 = 2 # molineaux
AGENT3 = 3 # cumulative
def pyhop_sim(id, start_state, plan, exp, agent, log_file,verbose=4):
    PLAN_RESULT_UNKNOWN = -1
    plan_result = PLAN_RESULT_UNKNOWN
    agent_id = agent
    failure_result = 'none'

    print("#########################################")
    print("#  Simulation Run "+str(id)+":")
    print("#  agent = "+str(agent))
    print("#########################################")

    num_tiles = start_state.dim['dim'] * start_state.dim['dim']
    true_start_state = start_state
    # obstacle #1: populate state with mud tiles
    # true_start_state = initialize_mud(start_state)

    # obstacle #2: radiation clouds
    # true_start_state = update_rad_clouds(true_start_state,verbose)

    # remove mud, beacons, and rad clouds that are beyond agent's perception 
    agent_start_state = update_agent_perceived_state(true_start_state)

    if verbose == 4:
        print('actual start state:')
        print_state(true_start_state)
        print('agent start state:')
        print_state(agent_start_state)
        #print('plan:')
        print('plan:')
        for action in plan:
            #print('  '+str(action))
            print('  '+str(action))
    if verbose >= 1:
        #print('plan has '+str(len(plan))+' steps')
        print('plan has '+str(len(plan))+' steps')
        #print('there are '+str(len(start_state.mud.values()))+' out of '+str(num_tiles)+' mud tiles')
        print('there are '+str(len(start_state.mud.values()))+' out of '+str(num_tiles)+' mud tiles')

    ####################
    # begin simulation #
    ####################

    plan_execution_history = [start_state] # history of states
    last_true_state = copy.deepcopy(true_start_state) 
    last_agent_state = copy.deepcopy(agent_start_state)
    step_count = 1
    for action,expectation in zip(plan,exp):
        print('----- sim step '+str(step_count)+' ------')
        #print('  agent: '+last_true_state.agents.values()[0]+", clouds: "+str(last_true_state.rad_clouds.keys())+", beacons: "+str(last_true_state.beacons.values()))
        print_state(last_true_state)
        step_count = step_count+1
        if action[0] not in operators:
            print('action is not in operators: '+str(action[0]))

        # get the operator function
        operator = operators[action[0]]

        # check pre-expectations
        # only agents 1 and 3 will use pre expectations (of course could also do it with molineaux)
        # NOTE: this is only a simulation - I don't actually compute these pre-expectations
        # but it is obviously trivial with a vanilla htn system that we are referring to theoretically
        #if agent != AGENT2 and operator.__name__.startswith('move'):
        if operator.__name__.startswith('move'):
            if verbose >= 2: print("## Checking Pre Expectations: ##")
            if last_agent_state.agents.values()[0] in last_agent_state.mud.values():
                if verbose >= 2: print("##### Failed Pre Expectations: Agent in Mud")
                plan_result = 2
                failure_result = 'mud'
                break
            else:
                if verbose >= 2: print("## Pre Expectations Satisified ##")

        if verbose >= 2: print('applying action '+str(action[0]))

        # apply agent's next action to true state
        new_true_state = operator(copy.deepcopy(last_true_state),*action[1:])

        # ensure action was valid on that true state
        # Note: in the first state, the agent's action should always be valid
        if not new_true_state:
            print("*** BEGIN FAILURE: "+operator.__name__+" with args "+str(action[1:])+ " failed on state:")
            print_state(last_true_state)
            print("*** END FAILURE")
            plan_result = 6 # plan failed
            # see what caused result
            if operator.__name__.startswith('move'):
                failure_result = 'mud'
            else:
                failure_result = 'cloud'
            break


        new_agent_state = update_agent_perceived_state(new_true_state)

        ### check for expectations
        if verbose >= 2: print("## Checking Post Expectations: ##")
        if verbose >= 4: print("new agent percieved state is ")
        if verbose >= 4: print_state(new_agent_state)
        if verbose >= 4: print("expectation is ")
        if verbose >= 4: print_state(expectation)

        ### check to see if there is an actual discrepancy in the state
        # A false positive occurs when there is no obstruction to the agent
        # but discrepany detection (using expectations) triggers a discrepancy
        ## In our environment, with obstacles mud and rad clouds, if there is no mud
        ## in the agent's location AND no rad cloud is covering a beacon, then there
        ## is no real discrepancy
        if verbose >= 4: print("new true state is ")
        if verbose >= 4: print_state(new_true_state)                   # last_true_state for a reason!   
        #exists_real_mud_obstacle = last_true_state.agents.values()[0] in last_true_state.mud.values()
        exists_real_mud_obstacle = False
        exists_real_cloud_obstacle = False
        for beacon_coords in new_true_state.beacons.values():
            if beacon_coords in new_true_state.rad_clouds.keys():
                exists_real_cloud_obstacle = True
        
        if verbose >= 4: print("exists_real_mud_obstacle="+str(exists_real_mud_obstacle))
        if verbose >= 4: print("exists_real_cloud_obstacle="+str(exists_real_cloud_obstacle))


        if agent == AGENT2: # using pure state matching for molineaux
            mismatch = pure_state_diff(expectation, new_agent_state)
            if mismatch: # yes found discrepancy
                if exists_real_mud_obstacle or exists_real_cloud_obstacle: # yes real obstacle
                    plan_result = 3 # correctly identified a discrepancy
                    if verbose >= 2: print("## Post Anomaly Successfully Detected ##")
                    break
                else: # no, no real obstacle
                    plan_result = 4 # incorrectly identified a discrepancy (false pos)
                    if verbose >= 2: print("## Found Discrepancy But No Real Obstacle ##")
                    if verbose >= 4: print("## Expectated State: ")
                    if verbose >= 4: print_state(expectation)
                    if verbose >= 4: print("## Actual Perceived State: ")
                    if verbose >= 4: print_state(new_agent_state)                    
                    break
                # if verbose >= 2: print("## Expectated State: ")
                # if verbose >= 2: print_state(expectation)
                # if verbose >= 2: print("## Actual State: ")
                # if verbose >= 2: print_state(new_agent_state)    
            else: # no did not find discrepancy
                if exists_real_mud_obstacle or exists_real_cloud_obstacle: # yes real obstacle
                    # failed to detect a discrepancy
                    plan_result = 5
                    if verbose >= 2: print("## Post Expectations Failed to Detect Obstacle ##")
                    if verbose >= 4: print("## Expectated State: ")
                    if verbose >= 4: print_state(expectation)
                    if verbose >= 4: print("## Actual Perceived State: ")
                    if verbose >= 4: print_state(new_agent_state)                    
                    break
                else: # no, no real obstacle
                    if verbose >= 2: print("## Post Expectations Successful in Not Detecting an Anomaly ##")
                    # plan_result = 3
                    

        else: # do subset matching for expectations that aren't complete states   
            expectations_met = state_subset_of(expectation, new_agent_state)
            if expectations_met: # no did not find discrepancy
                if exists_real_mud_obstacle or exists_real_cloud_obstacle: # yes real obstacle
                    # failed to detect a discrepancy
                    plan_result = 5
                    if verbose >= 2: print("## Post Expectations Failed to Detect Obstacle ##")
                    if verbose >= 4: print("## Expectated State: ")
                    if verbose >= 4: print_state(expectation)
                    if verbose >= 4: print("## Actual Perceived State: ")
                    if verbose >= 4: print_state(new_agent_state)                        
                    break
                else: # no, no real obstacle
                    if verbose >= 2: print("## Post Expectations Successful in Not Detecting an Anomaly ##")
                    # plan_result = 3
                                        
            else: # yes found discrepancy
                if exists_real_mud_obstacle or exists_real_cloud_obstacle: # yes real obstacle
                    plan_result = 3 # correctly identified a discrepancy
                    if verbose >= 2: print("## Post Anomaly Successfully Detected ##")
                    break
                else: # no, no real obstacle
                    plan_result = 4 # incorrectly identified a discrepancy (false pos)
                    if verbose >= 2: print("## Found Discrepancy But No Real Obstacle ##")
                    if verbose >= 4: print("## Expectated State: ")
                    if verbose >= 4: print_state(expectation)
                    if verbose >= 4: print("## Actual Perceived State: ")
                    if verbose >= 4: print_state(new_agent_state)    
                    break
                



            ########################################
            # Here we need to determine between a false positive
            # and a real expectation

            # TODO - implement false positive detection

            # Correctly Identified Anomaly post action:
            

        plan_execution_history.append(new_agent_state)
        last_agent_state = new_agent_state
        last_true_state = new_true_state

        # don't forget to update rad clouds
        last_true_state = update_rad_clouds(last_true_state,verbose=0)

    # if the loop didn't exit, plan executed correctly
    if plan_result == PLAN_RESULT_UNKNOWN:
        plan_result = 1

    if verbose >= 2: print("Plan Result is "+str(plan_result))

    return id,agent_id,plan_result,failure_result
    

############################################################
# Expectations (Work by Dustin Dannenhauer)


# ExpectedStateTree: data structure that holds the tree

class EST_Node(object):
    """ A tree of all possible states from any plan """
    state = State("NIL")
    children = []
    op_from_parent = None # this is the method or operator used to
                          # arrive at the state in the child node from
                          # the state in the parent node
    def __init__(self, state, children):
        self.state = state
        self.children = children
        
    def add_child(self, child):
        self.children.append(child)
        return self

    # remember this gets the state of the current node (NOT its children)
    def get_state(self):
        return self.state

    def set_state(self, state_arg):
        self.state = state_arg

    # returns the expectations which is OR applied to all of of the
    # leaves
    def get_expectations(self, curr_exp=[]):
        if len(self.children) == 0: # leaf node
            curr_exp.append(self.state)
        else:
            for child in self.children:
                child.get_expectations(curr_exp)
        return curr_exp

    def compute_cumulative_effects(self):
        curr_node = self
        # for now expectations are only linear
        while len(curr_node.children) == 1: 
            child_state = curr_node.children[0].get_state()
            parent_state = curr_node.get_state()
            curr_node.children[0].set_state(update_state(parent_state,child_state))
            curr_node = curr_node.children[0]
        return self


    def __str__(self):
        return self.simplestr()
        #return self.mystr()

    def mystr(self,prefix="--"):
        if not self.children:
            return prefix + " <leaf> " + str_state(self.state)
        else:            
            result = prefix + " <node> " + str_state(self.state) + "\n"
            for chi in self.children:
                #print("about to recur in est_node.__str__()")
                #print("CHI is " + str(chi))
                result = result + prefix + "--"  + " <child> \n"  + chi.mystr(prefix+"----") + "\n"
        return result
        
    def array_format(self):
        expectations_array = []
        
        curr_node = self
        # for now expectations are only linear
        while len(curr_node.children) == 1: 
            expectations_array.append(str_state_compressed(curr_node.state))
            curr_node = curr_node.children[0]
            
        return expectations_array

    def array_format_no_str(self):
        expectations_array = []
        
        curr_node = self
        # for now expectations are only linear
        while len(curr_node.children) == 1: 
            expectations_array.append(curr_node.state)
            curr_node = curr_node.children[0]
            
        return expectations_array



    def simplestr(self,depth=0,prefix="-"):
        if not self.children:
            return prefix + " <leaf> "# + str_state(self.state)
        else:            
            result = prefix + " <node (" + str(len(self.children)) + " children, d="+str(depth)+")> \n "   
            for chi in self.children:
                #print("about to recur in est_node.__str__()")
                #print("CHI is " + str(chi))
                result = result + prefix + "-"  + " <child> \n"  + chi.simplestr(depth+1,prefix+"-") + "\n"
        return result
    
    # will write a graph file of the EST to the given filename
    def write_dot_graph(self, filename):
        f = open(filename, 'w')

        # function to traverse EST 
        global count
        count = 0
        def traverse(node,parentStr=None, parentLabel=None):
            global count
            if len(node.children) != 0: # leaf node
                if (parentStr == None and parentLabel == None):
                    nodeStr = "node" + str(count)

                    nodeLabel = "node"+str(count)+" [ label =\"NO"+str_state_explicit_newline(node.get_state())+"\"];"
                    count = count+1
                    f.write(nodeLabel+"\n")
                    #f.write(nodeStr+" has "+str(len(node.children))+" children\n")
                else:
                    nodeStr = parentStr
                    nodeLabel = parentLabel
                
                for child in node.children:
                    childStr = "node" + str(count)
                    childLabel = "node"+str(count)+" [ label =\"CH"+str_state_explicit_newline(child.get_state())+"\"];"
                    count = count+1
                    f.write(childLabel+"\n")
                    #f.write(childStr+" has "+str(len(child.children))+" children\n")
                    if child.op_from_parent != None:
                        f.write(nodeStr + " -> " + childStr + " [label=\""+child.op_from_parent+"\"];\n")                        
                    else:
                        f.write(nodeStr + " -> " + childStr + ";\n")    

                # doing this in a separate loop makes it breadth first
                # search instead of depth first search
                for child in node.children:
                    traverse(child, childStr, childLabel)

        f.write("digraph G{\n") # boilerplate
        
        # write all the vertices here
        traverse(self)

        f.write("}") # boilerplate
        f.close()

# input: the result from calling pyhop_est
#def get_est_arr(est):
    

def pyhop_est(state,tasks,verbose=0):
    """
    Find all possible expected states
    """
    if verbose>0: print('** pyhop, verbose={}: **\n   state = {}\n   tasks = {}'.format(verbose, state.__name__, tasks))
    plan,est = seek_est(state,tasks,[],0,verbose)
    #print("result is "+str(result))
    #if result:
        #if verbose>0: print('** est is =',result,'\n')
        #if verbose>0: print('** est.children is =',result.children,'\n')
        #print(result)
    
        # for exp_item in result.get_expectations():
        #     print("Expectations Item: ")
        #     print_state(exp_item)
        #print_state(result.get_expectations()[-1]
    return plan,est
        
# create data structure
def seek_est(state,tasks,plan,depth,verbose=0):
    """
    Workhorse for expectationsp. state and tasks are as in pyhop.
    - plan is the current partial plan.
    - depth is the recursion depth, for use in debugging
    - verbose is whether to print debugging messages
    """
    if verbose>1: print('depth {} tasks {}'.format(depth,tasks))
    if tasks == []:
        if verbose>2: print('depth {} returns plan {}'.format(depth,plan))
        
        #print("About to return state: " + str(state))
        #print("returning a leaf node")
        #print("leaf node solution is:*")
        #print_state(state)
        #print("*")
        return plan,EST_Node(state,[])

    #est = EST_Node(state,[])
    task1 = tasks[0]
    #print("task1 is " + str(task1))
    if task1[0] in operators:
        if verbose>2: print('depth {} action {}'.format(depth,task1))
        operator = operators[task1[0]]
        newstate = operator(copy.deepcopy(state),*task1[1:])
        if verbose>2:
            print('depth {} new state:'.format(depth))
            #print_state(newstate)
        if newstate:
            #print("recurring...")
            plan,est = seek_est(newstate,tasks[1:],plan+[task1],depth+1,verbose)
            if est != False:
                #print("About to return state: " + str(state))
                #print("returning a leaf node")
                #print("leaf node solution is:***")
                #print_state(est.get_state())
                #print("***")
                
                #est.add_child(solution)
                est_parent = EST_Node(newstate,[])
                est.op_from_parent = str_task(task1)
                est_parent.add_child(est)
                return plan,est_parent
#                return EST_Node(newstate,[])
        #else:
        #    return EST_Node(state,[])
    if task1[0] in methods:
        if verbose>2: print('depth {} method instance {}'.format(depth,task1))
        relevant = methods[task1[0]]
        est_parent = EST_Node(state,[])
        for method in relevant:
            subtasks = method(state,*task1[1:])
            # Can't just say "if subtasks:", because that's wrong if subtasks == []
            if verbose>2:
                print('depth {} new tasks: {}'.format(depth,subtasks))
            if subtasks != False:
                #solution = seek_plan(state,subtasks+tasks[1:],plan,depth+1,verbose)
                #if solution != False:
                #    return solution
                
                plan,est = seek_est(state,subtasks+tasks[1:],plan,depth+1,verbose)
                #print("About to add child " + str(est_result))
                #print("is child instance of EST_Node? "+ str(isinstance(est_result, EST_Node)))
                #print("About to add child")
                est.op_from_parent = str_task(task1)+ " + |M| " +str(method)                
                est_parent.add_child(est)
                
        #print("About to return est")
        return plan,est
    if verbose>2: print('depth {} returns failure'.format(depth))
    print("*** returning a NIL node, input was state: ")
    print_state(state)
    print("*** returning a NIL node, input was tasks: ")
    print(str(tasks))
    print("*** returning a NIL node, input was plan: ")
    print(str(plan))
    return EST_Node(State("NIL"),[])

## only return the effects of actions:

def pyhop_est_effects_only(state,tasks,verbose=0):
    """
    Find all possible expected states
    """
    if verbose>0: print('** pyhop, verbose={}: **\n   state = {}\n   tasks = {}'.format(verbose, state.__name__, tasks))
    plan,est = seek_est_effects_only(state,tasks,[],0,verbose)
    #print("result is "+str(result))
    #if result:
        #if verbose>0: print('** est is =',result,'\n')
        #if verbose>0: print('** est.children is =',result.children,'\n')
        #print(result)
    
        # for exp_item in result.get_expectations():
        #     print("Expectations Item: ")
        #     print_state(exp_item)
        #print_state(result.get_expectations()[-1]
    return plan,est
        
# create data structure
def seek_est_effects_only(state,tasks,plan,depth,verbose=0):
    """
    Workhorse for expectationsp. state and tasks are as in pyhop.
    - plan is the current partial plan.
    - depth is the recursion depth, for use in debugging
    - verbose is whether to print debugging messages
    """
    if verbose>1: print('depth {} tasks {}'.format(depth,tasks))
    if tasks == []:
        if verbose>2: print('depth {} returns plan {}'.format(depth,plan))
        
        #print("About to return state: " + str(state))
        #print("returning a leaf node")
        #print("leaf node solution is:*")
        #print_state(state)
        #print("*")
        return plan,EST_Node(State('NIL'),[])

    #est = EST_Node(state,[])
    task1 = tasks[0]
    #print("task1 is " + str(task1))
    if task1[0] in operators:
        if verbose>2: print('depth {} action {}'.format(depth,task1))
        operator = operators[task1[0]]
        newstate = operator(copy.deepcopy(state),*task1[1:])
        if verbose>2:
            print('depth {} new state:'.format(depth))
            #print_state(newstate)
        if newstate:
            #print("recurring...")
            plan,est = seek_est_effects_only(newstate,tasks[1:],plan+[task1],depth+1,verbose)
            if est != False:
                #print("About to return state: " + str(state))
                #print("returning a leaf node")
                #print("leaf node solution is:***")
                #print_state(est.get_state())
                #print("***")
                
                #est.add_child(solution)

                #### Only get the effects
                effects_only_state = state_diff(state, newstate)

                est_parent = EST_Node(effects_only_state,[])
                est.op_from_parent = str_task(task1)
                est_parent.add_child(est)
                return plan,est_parent
#                return EST_Node(newstate,[])
        #else:
        #    return EST_Node(state,[])
    if task1[0] in methods:
        if verbose>2: print('depth {} method instance {}'.format(depth,task1))
        relevant = methods[task1[0]]
        est_parent = EST_Node(state,[])
        for method in relevant:
            subtasks = method(state,*task1[1:])
            # Can't just say "if subtasks:", because that's wrong if subtasks == []
            if verbose>2:
                print('depth {} new tasks: {}'.format(depth,subtasks))
            if subtasks != False:
                #solution = seek_plan(state,subtasks+tasks[1:],plan,depth+1,verbose)
                #if solution != False:
                #    return solution
                
                plan,est = seek_est_effects_only(state,subtasks+tasks[1:],plan,depth+1,verbose)
                #print("About to add child " + str(est_result))
                #print("is child instance of EST_Node? "+ str(isinstance(est_result, EST_Node)))
                #print("About to add child")
                est.op_from_parent = str_task(task1)+ " + |M| " +str(method)                
                est_parent.add_child(est)
                
        #print("About to return est")
        return plan,est
    if verbose>2: print('depth {} returns failure'.format(depth))
    print("*** returning a NIL node in effects only, input was state: ")
    print_state(state)
    print("*** returning a NIL node in effects only, input was tasks: ")
    print(str(tasks))
    print("*** returning a NIL node in effects only, input was plan: ")
    print(str(plan))
    print("task1 is "+str(task1))
    return EST_Node(State("NIL"),[])

def pyhop_est_cumulative_effects(state,tasks,verbose=0):
    """
    Find all possible expected states
    """
    if verbose>0: print('** pyhop, verbose={}: **\n   state = {}\n   tasks = {}'.format(verbose, state.__name__, tasks))
    plan,est = seek_est_effects_only(state,tasks,[],0,verbose)
    # run post processing step on the est:
    est = est.compute_cumulative_effects()
    #print(str(est.array_format()))


    return plan,est
