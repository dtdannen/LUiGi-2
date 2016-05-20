package gda.planner;

import common.Pair;
import gda.GoalType;
import java.util.ArrayList;

/**
 *
 * @author Dustin Dannenhauer
 * @email  dtd212@lehigh.edu
 * @date   Nov 20, 2013
 */
public class PlanAttackBothDirect implements Plan {

    private Plan plan;
    
    public PlanAttackBothDirect() {
        ArrayList<Pair<String, Integer>> unitTypeCounts = new ArrayList<Pair<String, Integer>>();
        unitTypeCounts.add(new Pair<String, Integer>("Terran_Wraith", 5));
        unitTypeCounts.add(new Pair<String, Integer>("Terran_Marine", 5));
        this.plan = new PlanAttackDirect(GoalType.ATTACK_BOTH_DIRECT, unitTypeCounts);
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
        return "Attack Both Direct";
    }
    
    public int getID() {
        return this.plan.getID();
    }

    @Override
    public void reset() {
        this.plan.reset();
    }
    
    
}
