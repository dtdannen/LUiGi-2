/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gda;

import gda.planner.Plan;
import gda.planner.PlanAttackAirDirect;
import gda.planner.PlanAttackBothDirect;
import gda.planner.PlanAttackGroundDirect;
import gda.planner.PlanAttackGroundSurround;
import gda.planner.PlanRushDefendRegion;

/**
 * Because goals are tightly coupled with plans, a goal is the same thing as
 * a plan, and this goal object exists only to fill in for a future goal object.
 * So for now a goal object is used to get the plan that is associated with it.
 * 
 * Right now this class may seem useless, because a goal is literally a plan
 * but in the future goals may be different than plans, like once we use a real
 * planner.
 * 
 * @author Dustin Dannenhauer
 * @email  dtd212@lehigh.edu
 */
public class Goal {

    private Plan plan;
    private GoalType goalType;
    
    
    public Goal(GoalType goalType) {
        createGoal(goalType, -1);
    }
    
    public Goal(GoalType goalType, int regionID) {
        createGoal(goalType, regionID);
    }
    
    
    private void createGoal(GoalType goalType, int regionId) {
        this.goalType = goalType;
        switch (goalType) {
            case ATTACK_GROUND_DIRECT:
                this.plan = new PlanAttackGroundDirect();
                break;
            case ATTACK_AIR_DIRECT:
                this.plan = new PlanAttackAirDirect();
                break;
            case ATTACK_BOTH_DIRECT:
                this.plan = new PlanAttackBothDirect();
                break;
            case ATTACK_GROUND_SURROUND:
                this.plan = new PlanAttackGroundSurround();
                break;
            case RUSH_DEFEND:
                this.plan = new PlanRushDefendRegion(regionId);
                break;
        }
    }
    
    public Plan getPlan() {
        return this.plan;
    }
}
