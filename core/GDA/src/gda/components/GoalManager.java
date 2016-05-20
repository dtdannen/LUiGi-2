package gda.components;

import gda.Goal;
import gda.GoalType;
import gda.planner.Planner;
import gda.planner.PlannerConnection;
import java.util.ArrayList;

/**
 * This class send the goal that the planner should pursue
 *
 * @author Dustin Dannenhauer
 * @email dtd212@lehigh.edu
 * @date Aug 12, 2013
 */
public class GoalManager {

    private Planner planner;
    //private ArrayList<Goal> currentGoals;
    private Goal currentGoal;
    private int regionIdForGoal = -1;
    
    /**
     * Very important, you must call set planner after you make the goal manager
     */
    public GoalManager() {
        planner = null;
    }
    
    public void setPlanner(Planner planner) {
        this.planner = planner;
    }
    
    public void setRegionIdForGoal(int r) {
        this.regionIdForGoal = r;
    }
    
    public void recieveGoalFromGoalFormulator(GoalType goalType) {
        this.currentGoal = new Goal(goalType);
        sendNewGoalToPlanner();
    }
    
    public void recieveGoalFromGoalFormulator(GoalType goalType, int regionId) {
        if (goalType == GoalType.RUSH_DEFEND) {
            this.currentGoal = new Goal(goalType, regionId);
        }
        sendNewGoalToPlanner();
    }
    
    private void sendNewGoalToPlanner() {
        if (this.currentGoal != null) {
            this.planner.setPlan(this.currentGoal.getPlan());
            PlannerConnection.sendMessageToBot("ABORT-PLAN");
        }
    }
    
}
