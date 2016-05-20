package gda.components;

import gda.GoalType;

/**
 * Case based reasoning system that recommends new goals to choose
 * 
 * @author Dustin Dannenhauer
 * @email  dtd212@lehigh.edu
 */
public class GoalFormulator {

    private GoalManager goalManager;
    
    public GoalFormulator(GoalManager goalManager) {
        this.goalManager = goalManager;
    }
    
    public void goForAirGoal() {
        this.goalManager.recieveGoalFromGoalFormulator(GoalType.ATTACK_AIR_DIRECT);
    }
    
    public void goForRushDefendGoal(int regionId) {
        this.goalManager.recieveGoalFromGoalFormulator(GoalType.RUSH_DEFEND, regionId);
    }

    void goForDirectGroundGoal() {
        this.goalManager.recieveGoalFromGoalFormulator(GoalType.ATTACK_GROUND_DIRECT);
    }
    
    
}
