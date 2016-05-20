package gda.planner;

import common.Pair;
import gda.GoalType;
import java.util.ArrayList;

/**
 *
 * @author Dustin Dannenhauer
 * @email  dtd212@lehigh.edu
 * @date   Dec 2, 2013
 */
public class PlanAttackGroundDirectFirebat implements Plan {

    private Plan plan;
    public PlanAttackGroundDirectFirebat() {
        ArrayList<Pair<String, Integer>> unitTypeCounts = new ArrayList<Pair<String, Integer>>();
        unitTypeCounts.add(new Pair<String, Integer>("Terran_Firebat", 10));
        //unitTypeCounts.add(new Pair<String, Integer>("Terran_Siege_Tank_Tank_Mode", 5));
        //unitTypeCounts.add(new Pair<String, Integer>("Terran_Firebat", 6));
        //unitTypeCounts.add(new Pair<String, Integer>("Terran_Medic", 3));
        this.plan = new PlanAttackDirect(GoalType.ATTACK_GROUND_DIRECT, unitTypeCounts);
    }

    @Override
    public boolean moveToNextPlanStep(ArrayList<Integer> unitIds) {
        return this.plan.moveToNextPlanStep(unitIds);
    }

    @Override
    public PlanStep getCurrentPlanStep() {
        return this.plan.getCurrentPlanStep();
    }

    @Override
    public boolean isPlanOnLastStep() {
        return this.plan.isPlanOnLastStep();
    }
    
    public String toString() {
        return "Attack Ground Direct Firebat";
    }

    public int getID() {
        return this.plan.getID();
    }
    
    @Override
    public void reset() {
        this.plan.reset();
    }
}
