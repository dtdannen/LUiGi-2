/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gda.planner;

import common.Pair;
import gda.GoalType;
import java.util.ArrayList;

/**
 *
 * @author Dustin Dannenhauer
 * @email dtd212@lehigh.edu
 * @date Aug 12, 2013
 */
public class PlanAttackAirDirect implements Plan{

    private Plan plan;
    
    public PlanAttackAirDirect() {
        ArrayList<Pair<String, Integer>> unitTypeCounts = new ArrayList<Pair<String, Integer>>();
        unitTypeCounts.add(new Pair<String, Integer>("Terran_Wraith", 5));
        this.plan = new PlanAttackDirect(GoalType.ATTACK_AIR_DIRECT, unitTypeCounts);
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
        return "Attack Air Direct";
    }
    
    public int getID() {
        return this.plan.getID();
    }
    
    @Override
    public void reset() {
        this.plan.reset();
    }
}
