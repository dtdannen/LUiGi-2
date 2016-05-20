package gda.components;

import gda.GoalType;
import gda.planner.Plan;

/**
 * Case based reasoning system that recommends new goals to choose
 * 
 * @author Dustin Dannenhauer
 * @email  dtd212@lehigh.edu
 */
public class GoalFormulator {

	private static GoalFormulator instance;
	
	// constructor is private so no one else can create a GoalFormulator object
	private GoalFormulator() { }
	
	// returns the current instance of the GoalFormulator
	public static synchronized GoalFormulator getInstance() {
		if (instance == null) {
			instance = new GoalFormulator();
		}
		return instance;
	}

	
	
	
    public void goForAirGoal() {
        GoalManager.getInstance().recieveGoalFromGoalFormulator(GoalType.ATTACK_AIR_DIRECT);
    }
    
    public void goForRushDefendGoal(int regionId) {
    	GoalManager.getInstance().recieveGoalFromGoalFormulator(GoalType.RUSH_DEFEND, regionId);
    }

    void goForDirectGroundGoal() {
    	GoalManager.getInstance().recieveGoalFromGoalFormulator(GoalType.ATTACK_GROUND_DIRECT);
    }

	public void goForSurroundGroundGoal() {
		GoalManager.getInstance().recieveGoalFromGoalFormulator(GoalType.ATTACK_GROUND_SURROUND);		
	}
    
	public void goForSurroundAirGoal() {
		GoalManager.getInstance().recieveGoalFromGoalFormulator(GoalType.ATTACK_AIR_SURROUND);		
	}

	public void redoGoal(GoalType gt) {
		GoalManager.getInstance().recieveGoalFromGoalFormulator(gt);
		
	}
    
}
