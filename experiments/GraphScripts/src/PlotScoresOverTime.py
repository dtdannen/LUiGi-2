'''
Created on Apr 7, 2014

@author: dustin
'''

import os
import json
import matplotlib.pyplot as plt

def main():
    print "Let's graph some data!"
    
    # just enter this manually every new day experiments are run
    todays_date_str = 'Mon-May-19'
    
    # get today's directory
    player_h_filepath = 'C:\\Users\\Dustin\\Documents\\GitHub\\hierarchical-gda\\data\\' + todays_date_str + '\\'
    player_non_h_filepath = 'C:\\Users\\Dustin\\workspaceTemp\\hierarchical-gda\\data\\' + todays_date_str + '\\'
    
    # some hardcoded values relating to the files that are generated
    h_player_key = 'player_64321_'
    non_h_player_key = 'player_64322_'
    
    # get the most recent data file for each player
    # h_player
    files = sorted([f for f in os.listdir(player_h_filepath)])
    player_h_data_file = player_h_filepath + files[-1]
    
    files = sorted([f for f in os.listdir(player_non_h_filepath)])
    player_non_h_data_file = player_non_h_filepath + files[-1]
    
    
    # print "Current Directory is " + os.getcwd()
    print "Most recent player_h_data_file is " + str(player_h_data_file)
    print "Most recent player_non_h_data_file is " + str(player_non_h_data_file)
    
    
    f = open(player_h_data_file)
    p1_lines = f.readlines()
    f.close
    f2 = open(player_non_h_data_file)
    p2_lines = f2.readlines()
    f2.close
    
    
    print "Read all data from data files"
    
    secondsPassed = []
    h_player_spent_minerals = []
    h_player_spent_gas = []
    h_player_supply_total = []
    h_player_kill_score = []
    h_player_unit_score = []
    h_player_razing_score = []
    h_player_total_score = []
    
    non_h_player_spent_minerals = []
    non_h_player_kill_score = []
    non_h_player_unit_score = []
    non_h_player_razing_score = []
    non_h_player_spent_gas = []
    non_h_player_total_score = []
    
    small_size = len(p2_lines)
    if (len(p1_lines) < len(p2_lines)):
        small_size = len(p1_lines)
    
    counter = 0    
    for line in p1_lines:
        if (counter < small_size and counter < 1400):
            data = json.loads(line)
            secondsPassed.append(data['secondsPassed'])
            h_player_unit_score.append(data[h_player_key+'unit_score'])
            h_player_kill_score.append(data[h_player_key+'kill_score'])
            h_player_razing_score.append(data[h_player_key+'razing_score'])
            h_player_spent_minerals.append(data[h_player_key+'spent_minerals'])
            h_player_spent_gas.append(data[h_player_key+'spent_gas'])
            h_player_total_score.append(data[h_player_key+'kill_score']+data[h_player_key+'razing_score'])
            
        counter+=1
    counter = 0        
    for line in p2_lines:
        if (counter < small_size and counter < 1400):
            data = json.loads(line)
            non_h_player_unit_score.append(data[non_h_player_key+'unit_score'])
            non_h_player_kill_score.append(data[non_h_player_key+'kill_score'])
            non_h_player_razing_score.append(data[non_h_player_key+'razing_score'])
            non_h_player_spent_minerals.append(data[non_h_player_key+'spent_minerals'])
            non_h_player_spent_gas.append(data[non_h_player_key+'spent_gas'])
            non_h_player_total_score.append(data[non_h_player_key+'kill_score']+data[non_h_player_key+'razing_score'])
        counter+=1
        
    diff_unit_score = []
    diff_kill_score = []
    diff_razing_score = []
    diff_spent_minerals_score = []
    diff_spent_gas_score = []
    diff_total_score = []
    for i in range(len(secondsPassed)):
        diff_unit_score.append(h_player_unit_score[i] - non_h_player_unit_score[i])
        diff_kill_score.append(h_player_kill_score[i] - non_h_player_kill_score[i])
        diff_razing_score.append(h_player_razing_score[i] - non_h_player_razing_score[i])
        diff_spent_minerals_score.append(h_player_spent_minerals[i] - non_h_player_spent_minerals[i])
        diff_spent_gas_score.append(h_player_spent_gas[i] - non_h_player_spent_gas[i])
        diff_total_score.append(h_player_total_score[i] - non_h_player_total_score[i])
        
    print "Processed all data from data files"
    
    plt.plot(secondsPassed, diff_kill_score, 'r--')
    plt.plot(secondsPassed, diff_unit_score, 'b-.')
    plt.plot(secondsPassed, diff_razing_score, 'g:')
    plt.plot(secondsPassed, diff_total_score, 'c')
    #plt.plot(secondsPassed, diff_spent_minerals_score, 'c')
    #plt.plot(secondsPassed, h_player_unit_score, 'orange')
    #plt.plot(secondsPassed, non_h_player_unit_score, 'yellow')
    #plt.plot(secondsPassed, non_h_player_unit_score, 'c')
    plt.axhline(0, 0, len(secondsPassed), color='black')
    plt.xlabel("Seconds")
    plt.ylabel("Score")
    plt.title("Cumulative Score (Kill, Unit, Razing) vs. Time")
    #plt.plot(secondsPassed, h_player_supply_total, 'c')
    #plt.plot(secondsPassed, h_player_spent_minerals, 'g')
    plt.show()
    
    print "--- Done"    

def main2():
    print "Let's graph some data!"
    
    # just enter this manually every new day experiments are run
    todays_date_str = 'Sat-May-17'
    
    # get today's directory
    player_h_filepath = 'C:\\Users\\dustin\\Documents\\GitHub\\hierarchical-gda\\data\\' + todays_date_str + '\\'
    #player_non_h_filepath = 'C:\\Users\\dustin\\workspaceTemp\\hierarchical-gda\\data\\' + todays_date_str + '\\'
    
    # some hardcoded values relating to the files that are generated
    h_player_key = 'player_64321_'
    #non_h_player_key = 'player_64322_'
    
    # get the most recent data file for each player
    # h_player
    files = sorted([f for f in os.listdir(player_h_filepath)])
    player_h_data_file = player_h_filepath + files[-1]
    
    #files = sorted([f for f in os.listdir(player_non_h_filepath)])
    #player_non_h_data_file = player_non_h_filepath + files[-1]
    
    
    # print "Current Directory is " + os.getcwd()
    print "Most recent player_h_data_file is " + str(player_h_data_file)
    #print "Most recent player_non_h_data_file is " + str(player_non_h_data_file)
    
    
    f = open(player_h_data_file)
    p1_lines = f.readlines()
    f.close
    #f2 = open(player_non_h_data_file)
    #p2_lines = f2.readlines()
    #f2.close
    
    
    print "Read all data from data files"
    
    secondsPassed = []
    h_player_spent_minerals = []
    h_player_spent_gas = []
    h_player_supply_total = []
    h_player_kill_score = []
    h_player_unit_score = []
    h_player_razing_score = []
    
    #non_h_player_spent_minerals = []
    #non_h_player_kill_score = []
    #non_h_player_unit_score = []
    #non_h_player_razing_score = []
    #non_h_player_spent_gas = []
    
    #small_size = len(p2_lines)
    #if (len(p1_lines) < len(p2_lines)):
    #    small_size = len(p1_lines)
    
    counter = 0    
    for line in p1_lines:
        if (True):#counter < small_size):
            data = json.loads(line)
            secondsPassed.append(data['secondsPassed'])
            h_player_unit_score.append(data[h_player_key+'unit_score'])
            h_player_kill_score.append(data[h_player_key+'kill_score'])
            h_player_razing_score.append(data[h_player_key+'razing_score'])
            h_player_spent_minerals.append(data[h_player_key+'spent_minerals'])
            h_player_spent_gas.append(data[h_player_key+'spent_gas'])
            
        counter+=1
    #counter = 0        
    #for line in p2_lines:
    #    if (counter < small_size):
    #        data = json.loads(line)
    #        non_h_player_unit_score.append(data[non_h_player_key+'unit_score'])
    #        non_h_player_kill_score.append(data[non_h_player_key+'kill_score'])
    #        non_h_player_razing_score.append(data[non_h_player_key+'razing_score'])
    #        non_h_player_spent_minerals.append(data[non_h_player_key+'spent_minerals'])
    #        non_h_player_spent_gas.append(data[non_h_player_key+'spent_gas'])
    #    counter+=1
        
    diff_unit_score = []
    diff_kill_score = []
    diff_razing_score = []
    diff_spent_minerals_score = []
    diff_spent_gas_score = []
    #for i in range(len(secondsPassed)):
    #    diff_unit_score.append(h_player_unit_score[i] - non_h_player_unit_score[i])
    #    diff_kill_score.append(h_player_kill_score[i] - non_h_player_kill_score[i])
    #    diff_razing_score.append(h_player_razing_score[i] - non_h_player_razing_score[i])
    #    diff_spent_minerals_score.append(h_player_spent_minerals[i] - non_h_player_spent_minerals[i])
    #    diff_spent_gas_score.append(h_player_spent_minerals[i] - non_h_player_spent_gas[i])
        
    print "Processed all data from data files"
    
    plt.plot(secondsPassed, h_player_unit_score, 'r')
    plt.plot(secondsPassed, h_player_kill_score, 'b')
    plt.plot(secondsPassed, h_player_razing_score, 'g')
    plt.plot(secondsPassed, h_player_spent_minerals, 'c')
    plt.plot(secondsPassed, h_player_spent_minerals, 'orange')
    #plt.plot(secondsPassed, non_h_player_unit_score, 'c')
    plt.axhline(0, 0, len(secondsPassed), color='black')
    plt.xlabel("Seconds")
    plt.ylabel("Score")
    plt.title("Kill Score Diff (red), Unit Score Diff (blue), Razing Score (green) vs. Game Time")
    #plt.plot(secondsPassed, h_player_supply_total, 'c')
    #plt.plot(secondsPassed, h_player_spent_minerals, 'g')
    plt.show()
    
    print "--- Done"    


if __name__ == '__main__':
    main()