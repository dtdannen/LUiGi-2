from __future__ import print_function
from pyhop import *

import marsworld_operators
print('')
print_operators()

import marsworld_methods
print('')
print_methods()

AGENT1 = 1 # effects only
AGENT2 = 2 # complete state
AGENT3 = 3 # cumulative
DIM = 10


# Lets do some simulations!
# 1. generate states
# 2. run the simulation on each state and for each agent, recording the following:
#    1. Plan success/failure
#    2. P
data_file = open_data_file_for_experiment()
data_file_name = data_file.name
#print("data file name is "+data_file_name)
data_file.write("SimID,AgentID,PlanResult,FailureResult\n")

log_file = open_data_file_for_experiment()

number_of_runs = 1000 # used for both nav and perimeter goals

""" navigation goals """

temp_start_states = []
temp_end_states = []
min_dist = 5
for i in range(0,number_of_runs):
	x_start = random.randint(1,9)
	y_start = random.randint(1,9)
	x_end = random.randint(1,9)
	y_end = random.randint(1,9)
	# loop forever until you get valid examples
	while (abs(x_start - x_end) + abs(y_start - y_end) < min_dist):
		x_start = random.randint(1,9)
		y_start = random.randint(1,9)
		x_end = random.randint(1,9)
		y_end = random.randint(1,9)

	temp_start_states.append(str(x_start)+','+str(y_start))
	temp_end_states.append(str(x_end)+','+str(y_end))

""" run nav goals """

sim_id = 1
for i in range(0,number_of_runs):

	state1 = State('state1')
	state1.agents={'curiosity':temp_start_states[i]} # put beacons here too? and mud tiles?
	state1.dim={'dim':DIM}
	state1.mud={}
	state1.beacons={}

	plan_state1_agent1,agent1_state1_exp = pyhop_est_effects_only(state1,[('navigate','curiosity',temp_end_states[i])],verbose=0)
	plan_state1_agent2,agent2_state1_exp = pyhop_est(state1,[('navigate','curiosity',temp_end_states[i])],verbose=0)
	plan_state1_agent3,agent3_state1_exp = pyhop_est_cumulative_effects(state1,[('navigate','curiosity',temp_end_states[i])],verbose=0)

	# keep the state the same for all agents during this round
	state1 = initialize_mud(state1)
	state1 = update_rad_clouds(state1,verbose=0)

	sim_id,a_id,presult,fresult = pyhop_sim(sim_id,state1,plan_state1_agent1,agent1_state1_exp.array_format_no_str(),AGENT1,log_file,4)
	data_file.write("{0},{1},{2},{3}\n".format(str(sim_id),str(a_id),str(presult),fresult))
	sim_id +=1
	sim_id,a_id,presult,fresult = pyhop_sim(sim_id,state1,plan_state1_agent2,agent2_state1_exp.array_format_no_str(),AGENT2,log_file,4)
	data_file.write("{0},{1},{2},{3}\n".format(str(sim_id),str(a_id),str(presult),fresult))
	sim_id +=1
	sim_id,a_id,presult,fresult = pyhop_sim(sim_id,state1,plan_state1_agent3,agent3_state1_exp.array_format_no_str(),AGENT3,log_file,4)
	data_file.write("{0},{1},{2},{3}\n".format(str(sim_id),str(a_id),str(presult),fresult))
	sim_id +=1


""" perimeter goals:
      - each goal will require dropping 4 beacons at random points
      - starting location will always be 1,1
      - every beacon must be at least 2 spaces away from the previous beacon
"""

beacon_goal_coords = [] # each element in this array is an array of coords
for i in range(0,number_of_runs):
	# first beacon cords
	beacon1_x = random.randint(1,9)
	beacon1_y = random.randint(1,9)
	b1xy = str(beacon1_x) + "," + str(beacon1_y)
	# second beacon cords
	beacon2_x = random.randint(1,9)
	beacon2_y = random.randint(1,9)
	while (abs(beacon2_x-beacon1_x) + abs(beacon2_y-beacon1_y) < 2):
		# make sure this beacon is at least two away, if not, loop until you find one
		beacon2_x = random.randint(1,9)
		beacon2_y = random.randint(1,9)
	b2xy = str(beacon2_x) + "," + str(beacon2_y)

	# third beacon
	beacon3_x = random.randint(1,9)
	beacon3_y = random.randint(1,9)
	while (abs(beacon3_x-beacon2_x) + abs(beacon3_y-beacon2_y) < 2 or
		   abs(beacon3_x-beacon1_x) + abs(beacon3_y-beacon1_y) < 2):
		# make sure this beacon is at least two away from the other beacons,
		# if not, loop until you find one
		beacon3_x = random.randint(1,9)
		beacon3_y = random.randint(1,9)
	b3xy = str(beacon3_x) + "," + str(beacon3_y)

	beacon_goal_coords.append([b1xy,b2xy,b3xy])

""" run perimeter goals """

for run_id in range(0,number_of_runs):

	state1 = State('state1')
	state1.agents={'curiosity':'1,1'} # put beacons here too? and mud tiles?
	state1.dim={'dim':DIM}
	state1.mud={}
	state1.beacons={}
	state1.rad_clouds={}
	print('beacons goal coords are '+str(beacon_goal_coords[run_id]))
	plan_state1_agent1,agent1_state1_exp = pyhop_est_effects_only(state1,[('make_perimeter','curiosity',beacon_goal_coords[run_id])],verbose=0)
	plan_state1_agent2,agent2_state1_exp = pyhop_est(state1,[('make_perimeter','curiosity',beacon_goal_coords[run_id])],verbose=0)
	plan_state1_agent3,agent3_state1_exp = pyhop_est_cumulative_effects(state1,[('make_perimeter','curiosity',beacon_goal_coords[run_id])],verbose=0)

	# keep the state the same for all agents during this round
	state1 = initialize_mud(state1)

	# this random number generator state is used to make sure the same clouds are created
	# and update for each agent - ensuring the states are all the same
	random.seed(run_id)
	#rgen_for_clouds = random.getstate()

	sim_id,a_id,presult,fresult = pyhop_sim(sim_id,state1,plan_state1_agent1,agent1_state1_exp.array_format_no_str(),AGENT1,log_file,4)
	data_file.write("{0},{1},{2},{3}\n".format(str(sim_id),str(a_id),str(presult),fresult))
	sim_id +=1

	random.seed(run_id)

	sim_id,a_id,presult,fresult = pyhop_sim(sim_id,state1,plan_state1_agent2,agent2_state1_exp.array_format_no_str(),AGENT2,log_file,4)
	data_file.write("{0},{1},{2},{3}\n".format(str(sim_id),str(a_id),str(presult),fresult))
	sim_id +=1

	random.seed(run_id)

	sim_id,a_id,presult,fresult = pyhop_sim(sim_id,state1,plan_state1_agent3,agent3_state1_exp.array_format_no_str(),AGENT3,log_file,4)
	data_file.write("{0},{1},{2},{3}\n".format(str(sim_id),str(a_id),str(presult),fresult))
	sim_id +=1


#temp_start_states = ['1,1','1,3','3,1','1,5','5,1']
#temp_end_states   = ['4,4','2,5','1,4','2,3','2,3']

	
data_file.close()
log_file.close()	

""" 5 states with perimeter goals """


""" Process the data in a useful manner """
result_counts = {1:[0,0,0,0,0,0],2:[0,0,0,0,0,0],3:[0,0,0,0,0,0]} # result counts per agent 
data_file = open(data_file_name,'r')
for line in data_file.readlines()[1:]:
	line_t = line.split(",")
	agent_id = int(line_t[1])
	plan_result = int(line_t[2])
	# update result_counts
	curr_counts = result_counts[agent_id]
	curr_counts[plan_result-1] = curr_counts[plan_result-1]+1 
	result_counts[agent_id] = curr_counts
	#print("just added 1 for agent "+str(agent_id)+"for plan result "+str(plan_result))
data_file.close()

print("result_counts: "+str(result_counts))

""" Show some useful graphs """
import matplotlib.pyplot as plt 
labels = ["plan success", "pre exp success", "post exp success", "false pos exp", "detection failed","plan failure"]
colors = ["blue","darkgreen","lightgreen","orange","red","brown"]
agent1_sizes = result_counts[1]
agent2_sizes = result_counts[2]
agent3_sizes = result_counts[3]

fig = plt.figure(figsize=(15,2))
a1_p = fig.add_subplot(1,5,1)

a1_labels = map(lambda x:x+'1',labels)
a2_labels = map(lambda x:x+'2',labels)
a3_labels = map(lambda x:x+'3',labels)

## Trims any labels that are 0, makes graphs prettier

agent1_sizes_r = []
a1_labels_r = []
a1_colors = []
for i in range(0,len(agent1_sizes)):
	if agent1_sizes[i] > 0:
		agent1_sizes_r.append(agent1_sizes[i])
		a1_labels_r.append(a1_labels[i])
		a1_colors.append(colors[i])
print("agent1_sizes is " + str(agent1_sizes))
print("agent1_sizes_r is " + str(agent1_sizes_r))

agent2_sizes_r = []
a2_labels_r = []
a2_colors = []
for i in range(0,len(agent2_sizes)):
	if agent2_sizes[i] > 0:
		agent2_sizes_r.append(agent2_sizes[i])
		a2_labels_r.append(a2_labels[i])
		a2_colors.append(colors[i])
print("agent2_sizes is " + str(agent2_sizes))
print("agent2_sizes_r is " + str(agent2_sizes_r))

agent3_sizes_r = []
a3_labels_r = []
a3_colors = []
for i in range(0,len(agent3_sizes)):
	if agent3_sizes[i] > 0:
		agent3_sizes_r.append(agent3_sizes[i])
		a3_labels_r.append(a3_labels[i])
		a3_colors.append(colors[i])
print("agent3_sizes is " + str(agent3_sizes))
print("agent3_sizes_r is " + str(agent3_sizes_r))

## Create Graphs

a1_p.pie(agent1_sizes_r, labels=a1_labels_r, colors=a1_colors)
#fig.title('Agent 1', bbox={'facecolor':'0.8', 'pad':5})

a2_p = fig.add_subplot(1,5,3)
a2_p.pie(agent2_sizes_r, labels=a2_labels_r, colors=a2_colors)
#a2_p.title('Agent 2', bbox={'facecolor':'0.8', 'pad':5})

a3_p = fig.add_subplot(1,5,5)
a3_p.pie(agent3_sizes_r, labels=a3_labels_r, colors=a3_colors)

#a3_fig = plt.figure()
#a3_fig.pie(agent3_sizes, labels=labels, colors=colors)
#a3_fig.title('Agent 3', bbox={'facecolor':'0.8', 'pad':5})
plt.show()




#import texttable

# print("-------------------------------------------------------------")
# print("Test 1 - Navigation from 1,1 to 5,4")
# state1 = State('state1')
# state1.agents={'curiosity':'1,1'} # put beacons here too? and mud tiles?
# state1.dim={'dim':6}
# state1.mud={}
# print_state(state1)
# pyhop_result = pyhop(state1,[('navigate','curiosity','5,4')], verbose=1)
# print("-------------------------------------------------------------")

# print("-------------------------------------------------------------")
# print("Test 2 - Navigation from 5,4 to 1,1")
# state1 = State('state1')
# state1.agents={'curiosity':'5,4'} # put beacons here too? and mud tiles?
# state1.dim={'dim':6}
# state1.mud={}
# print_state(state1)
# pyhop_result = pyhop(state1,[('navigate','curiosity','1,1')], verbose=1)
# print("-------------------------------------------------------------")

# print("-------------------------------------------------------------")
# print("Test 3 - Navigation from 1,1 to 1,5")
# state1 = State('state1')
# state1.agents={'curiosity':'1,1'} # put beacons here too? and mud tiles?
# state1.dim={'dim':6}
# state1.mud={}
# print_state(state1)
# pyhop_result = pyhop(state1,[('navigate','curiosity','1,5')], verbose=1)
# print("-------------------------------------------------------------")

# print("-------------------------------------------------------------")
# print("Test 4 - Navigation from 5,5 to 1,5")
# state1 = State('state1')
# state1.agents={'curiosity':'5,5'} # put beacons here too? and mud tiles?
# state1.dim={'dim':6}
# state1.mud={}
# print_state(state1)
# pyhop_result = pyhop(state1,[('navigate','curiosity','1,5')], verbose=1)
# print("-------------------------------------------------------------")

# print("-------------------------------------------------------------")
# print("Test 5 - Navigation from 3,3 to 1,1")
# state1 = State('state1')
# state1.agents={'curiosity':'3,3'} # put beacons here too? and mud tiles?
# state1.dim={'dim':6}
# state1.mud={}
# print_state(state1)
# pyhop_result = pyhop(state1,[('navigate','curiosity','1,1')], verbose=4)
# print("-------------------------------------------------------------")

# print("-------------------------------------------------------------")
# print("Test 5 - Navigation from 3,3 to 3,3 (edge case)")
# state1 = State('state1')
# state1.agents={'curiosity':'3,3'} # put beacons here too? and mud tiles?
# state1.dim={'dim':6}
# state1.mud={}
# print_state(state1)
# pyhop_result = pyhop(state1,[('navigate','curiosity','1,1')], verbose=4)
# print("-------------------------------------------------------------")

# print("-------------------------------------------------------------")
# print("Test 6 - Place beacons at 1,1 then 1,5 then 5,5 then 5,1")
# state1 = State('state1')
# state1.agents={'curiosity':'3,3'} # put beacons here too? and mud tiles?
# state1.dim={'dim':6}
# state1.beacons={} # already placed beacons
# state1.mud={}
# print_state(state1)
# pyhop_result = pyhop(state1,[('make_perimeter','curiosity',['1,1','1,5','5,5','5,1'])], verbose=4)
# print("-------------------------------------------------------------")

# print("-------------------------------------------------------------")
# print("Test 7 - Place beacons at 1,1 then 1,5 then 5,5 then 5,1 (edge case)")
# state1 = State('state1')
# state1.agents={'curiosity':'1,1'} # put beacons here too? and mud tiles?
# state1.dim={'dim':6}
# state1.beacons={} # already placed beacons
# state1.mud={}
# print_state(state1)
# pyhop_result = pyhop(state1,[('make_perimeter','curiosity',['1,1','1,5','5,5','5,1'])], verbose=4)
# print("-------------------------------------------------------------")

# print("-------------------------------------------------------------")
# print("Test 8 - Place beacons at 1,1 then 1,5 then 5,5 then 5,1")
# state1 = State('state1')
# state1.agents={'curiosity':'1,1'} # put beacons here too? and mud tiles?
# state1.dim={'dim':6}
# state1.beacons={} # already placed beacons
# state1.mud={}
# print_state(state1)
# pyhop_result = pyhop(state1,[('make_perimeter','curiosity',['1,1','1,5','5,5','5,1'])], verbose=4)
# print("-------------------------------------------------------------")

# print("-------------------------------------------------------------")
# print("-----------------------Simulator Tests-----------------------")
# print("-------------------------------------------------------------")
# print("Sim Test 1 - Place beacons at 1,1 then 1,5 then 5,5 then 5,1")
# state1 = State('state1')
# state1.agents={'curiosity':'1,1'} # put beacons here too? and mud tiles?
# state1.dim={'dim':6}
# state1.beacons={} # already placed beacons
# state1.mud={}
# #print_state(state1)
# pyhop_plan = pyhop(state1,[('make_perimeter','curiosity',['1,1','1,5','5,5','5,1'])], verbose=0)
# pyhop_sim(state1, pyhop_plan, verbose=4)
# print("-------------------------------------------------------------")

# print("Sim Test 2 - Place beacons at every location")
# state1 = State('state1')
# state1.agents={'curiosity':'0,0'} # put beacons here too? and mud tiles?
# state1.dim={'dim':6}
# state1.beacons={} # already placed beacons
# state1.mud={}
# #print_state(state1)
# beacon_drop_points = []
# for x in range(0,state1.dim['dim']):
#     for y in range(0,state1.dim['dim']):
#         drop_point_id = str(x)+','+str(y)
#         beacon_drop_points.append(drop_point_id)
# pyhop_plan = pyhop(state1,[('make_perimeter','curiosity',beacon_drop_points)], verbose=0)
# pyhop_sim(state1, pyhop_plan, verbose=4)
# print("-------------------------------------------------------------")


# print("-------------------------------------------------------------")
# print("-----------------------Expectation Tests-----------------------")
# print("-------------------------------------------------------------")
# print("-------------------------------------------------------------")
# print("EST Test 1 - Navigation from 1,1 to 5,4")
# state1 = State('state1')
# state1.agents={'curiosity':'1,1'} # put beacons here too? and mud tiles?
# state1.dim={'dim':6}
# state1.mud={}
# print_state(state1)
# plan,exp = pyhop_est(state1,[('navigate','curiosity','5,4')], verbose=0)
# plan,exp_effects_only = pyhop_est_effects_only(state1, [('navigate','curiosity','5,4')], verbose=0)
# print('len(exp) is '+str(len(exp.array_format())))
# table = texttable.Texttable()
# table.header(['Plan Step','Entire State','Effects Only'])
# for plan_step,expectation,exp_effects in zip(plan,exp.array_format(),exp_effects_only.array_format()):
#     table.add_row([str(plan_step),str(expectation),str(exp_effects)])
# print(table.draw())

# print("-------------------------------------------------------------")
# print("EST Test 2 - Place beacons at every location")
# state1 = State('state1')
# state1.agents={'curiosity':'0,0'} 
# state1.dim={'dim':6}
# state1.beacons={} # already placed beacons
# state1.mud={}
# #print_state(state1)
# beacon_drop_points = []
# for x in range(0,state1.dim['dim']):
#     for y in range(0,state1.dim['dim']):
#         drop_point_id = str(x)+','+str(y)
#         beacon_drop_points.append(drop_point_id)

# plan,exp = pyhop_est(state1,[('make_perimeter','curiosity',beacon_drop_points)], verbose=0)
# plan,exp_effects_only = pyhop_est_effects_only(state1,[('make_perimeter','curiosity',beacon_drop_points)], verbose=0)
# plan,cumulative_effects_raw = pyhop_est_cumulative_effects(state1,[('make_perimeter','curiosity',beacon_drop_points)], verbose=0)

# table = texttable.Texttable()
# table.header(['Plan Step','Entire State','Effects Only'])
# for plan_step,expectation,exp_effects in zip(plan,exp.array_format(),exp_effects_only.array_format()):
#     table.add_row([str(plan_step),str(expectation),str(exp_effects)])
# print(table.draw())
# i = 0
# for plan_step,expectation,exp_effects,cumulative_effects in zip(plan,exp.array_format(),exp_effects_only.array_format(), cumulative_effects_raw.array_format()):
#     print("--- Plan Step                #"+str(i)+"#: ---")
#     print(str(plan_step))
#     print("--- Entire State Expectation #"+str(i)+"#: ---")
#     print(str(expectation))
#     print("--- Cumulative Effects Expectation #"+str(i)+"#: ---")
#     print(str(cumulative_effects))
#     print("--- Only Effects Expectation #"+str(i)+"#: ---")
#     print(str(exp_effects))
#     i = i+1

#[for later] #pyhop_sim(state1, pyhop_plan, verbose=4)


print("-------------------------------------------------------------")


# # Test for in city, truck at start
# print("Test 1")
# state1 = State('state1')
# state1.pkgs={'p1':'loc1'}
# state1.trucks={'t1':'loc1'}
# state1.aplns={'apln1':'aprt1'}
# state1.city={'loc1':'city1','loc2':'city1'}

# pyhop_result = pyhop(state1,[('deliver','p1','t1', 'apln1', 'loc1','loc2')], verbose=1)

# est_gs = pyhop_est(state1,[('deliver','p1','t1', 'apln1', 'loc1','loc2')], verbose=1)
# est_gs.write_dot_graph("trans-test-1.dot")

# # Test for in city, no truck at start, instead truck at dest
# print("Test 2")
# state1 = State('state1')
# state1.pkgs={'p1':'loc1'}
# state1.trucks={'t1':'loc2'}
# state1.aplns={'apln1':'aprt1'}
# state1.city={'loc1':'city1','loc2':'city1'}

# pyhop_result = pyhop(state1,[('deliver','p1','t1', 'apln1', 'loc1','loc2')], verbose=1)

# est_gs = pyhop_est(state1,[('deliver','p1','t1', 'apln1', 'loc1','loc2')], verbose=1)
# est_gs.write_dot_graph("trans-test-2.dot")

# # Test for in city, truck at 3rd location, not at start
# print("Test 3")
# state1 = State('state1')
# state1.pkgs={'p1':'loc1'}
# state1.trucks={'t1':'loc3'}
# state1.aplns={'apln1':'aprt1'}
# state1.city={'loc1':'city1','loc2':'city1','loc3':'city1'}

# pyhop_result = pyhop(state1,[('deliver','p1','t1', 'apln1','loc1','loc2')], verbose=1)

# est_gs = pyhop_est(state1,[('deliver','p1','t1', 'apln1', 'loc1','loc2')], verbose=1)
# est_gs.write_dot_graph("trans-test-3.dot")

# # Test for different city, pkg at airport and dest is airport, plane at airport
# print("Test 4")
# state1 = State('state1')
# state1.pkgs={'p1':'aprt1'}
# state1.trucks={'t1':'loc3'}
# state1.city={'loc1':'city1','loc2':'city1',
#              'loc3':'city1','aprt1':'city1',
#              'aprt2':'city2'}
# state1.aplns={'apln1':'aprt1', 'apln2':'aprt2'}
# state1.aprts={'aprt1':True, 'aprt2':True}

# pyhop_result = pyhop(state1,[('deliver','p1', 't1', 'apln1','aprt1','aprt2')], verbose=1)

# est_gs = pyhop_est(state1,[('deliver','p1','t1', 'apln1', 'aprt1','aprt2')], verbose=1)
# est_gs.write_dot_graph("trans-test-4.dot")

# # Test for different city, pkg at airport and dest is airport, but no plane at airport
# print("Test 5")
# state1 = State('state1')
# state1.pkgs={'p1':'aprt1'}
# state1.trucks={'t1':'loc3'}
# state1.city={'loc1':'city1','loc2':'city1',
#              'loc3':'city1','aprt1':'city1',
#              'aprt2':'city2'}
# state1.aplns={'apln1':'aprt2', 'apln2':'aprt2'}
# state1.aprts={'aprt1':True, 'aprt2':True}

# pyhop_result = pyhop(state1,[('deliver','p1', 't1', 'apln1','aprt1','aprt2')], verbose=1)

# est_gs = pyhop_est(state1,[('deliver','p1','t1', 'apln1', 'aprt1','aprt2')], verbose=1)
# est_gs.write_dot_graph("trans-test-5.dot")

# # Test for diffcity, pkg not at airport, dest is airport, with plane at airport
# print("Test 6")
# state1 = State('state1')
# state1.pkgs={'p1':'loc1'}
# state1.trucks={'t1':'loc3'}
# state1.city={'loc1':'city1','loc2':'city1',
#              'loc3':'city1','aprt1':'city1',
#              'aprt2':'city2'}
# state1.aplns={'apln1':'aprt1', 'apln2':'aprt2'}
# state1.aprts={'aprt1':True, 'aprt2':True}

# pyhop_result = pyhop(state1,[('deliver','p1', 't1', 'apln1','loc1','aprt2')], verbose=1)

# est_gs = pyhop_est(state1,[('deliver','p1', 't1', 'apln1','loc1','aprt2')], verbose=1)

# est_gs.write_dot_graph("trans-test-6.dot")

# # Test for diffcity, pkg not at airport, dest is airport, plane not at airport
# print("Test 7")
# state1 = State('state1')
# state1.pkgs={'p1':'loc1'}
# state1.trucks={'t1':'loc3'}
# state1.city={'loc1':'city1','loc2':'city1',
#              'loc3':'city1','aprt1':'city1',
#              'aprt2':'city2'}
# state1.aplns={'apln1':'aprt2', 'apln2':'aprt2'}
# state1.aprts={'aprt1':True, 'aprt2':True}

# pyhop_result = pyhop(state1,[('deliver','p1', 't1', 'apln1','loc1','aprt2')], verbose=1)

# est_gs = pyhop_est(state1,[('deliver','p1', 't1', 'apln1','loc1','aprt2')], verbose=1)

# est_gs.write_dot_graph("trans-test-7.dot")



# # Test for diffcity, pkg not at airport, dest is not airport, plane not at airport
# print("Test 8")
# state1 = State('state1')
# state1.pkgs={'p1':'loc1'}
# state1.trucks={'t1':'loc3','t2':'aprt2'}
# state1.city={'loc1':'city1','loc2':'city1',
#              'loc3':'city1','loc4':'city2', 
#              'aprt1':'city1','aprt2':'city2'}
# state1.aplns={'apln1':'aprt2', 'apln2':'aprt2'}
# state1.aprts={'aprt1':True, 'aprt2':True}

# pyhop_result = pyhop(state1,[('deliver','p1', 't1', 'apln1','loc1','loc4')], verbose=1)

# #est_gs = pyhop_est(state1,[('deliver','p1', 't1', 'apln1','loc1','loc4')], verbose=1)

# #est_gs.write_dot_graph("trans-test-8.dot")
