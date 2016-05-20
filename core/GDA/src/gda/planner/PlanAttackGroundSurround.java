/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gda.planner;

import common.Pair;
import gda.GlobalInfo;
import gda.GoalType;
import gda.components.Expectation;
import java.util.ArrayList;
import ontology.OntologyInfo;

/**
 *
 * @author Dustin Dannenhauer
 * @email  dtd212@lehigh.edu
 * @date   Nov 20, 2013
 */
public class PlanAttackGroundSurround implements Plan {

    private ArrayList<PlanStep> planSteps = new ArrayList<>();
    private GoalType goalType;
    private ArrayList<Pair<String, Integer>> unitTypeCounts;
    private int currentPlanStepIndex = -1;
    private static int ID = 0;
    
    public PlanAttackGroundSurround() {
        this.goalType = GoalType.ATTACK_GROUND_SURROUND;
        PlanStep step = null;
        this.ID++;
        unitTypeCounts = new ArrayList<Pair<String, Integer>>();
        
        // Step 0. Produce units
        ////// Step 1 - Acquire Units ////// 
        ArrayList<Expectation> step1PreExpectations = new ArrayList<Expectation>();
        ArrayList<Expectation> step1PostExpectations = new ArrayList<Expectation>();

        // pre expectations
        step1PreExpectations.add(new Expectation(Expectation.ExpectationName.CONTROL_REGION).setRegionId(GlobalInfo.getStartingRegionID()));
        unitTypeCounts.add(new Pair<String, Integer>("Terran_Marine", 10));
            // post expectations
        for (Pair<String, Integer> unitTypeCount : unitTypeCounts) {
            step1PostExpectations.add(new Expectation(Expectation.ExpectationName.HAVE_UNITS).addUnitCountPair(unitTypeCount));
        }
        
        step = new PlanStep(PlanStepType.PRODUCE_UNITS, goalType, step1PreExpectations, step1PostExpectations, this);
        for (Pair<String, Integer> unitTypeCount : unitTypeCounts) {
            step.addUnitTypeCount(unitTypeCount); // only needed for acquire unit plan step
        }
        this.planSteps.add(step);
        
        ////// Step 2 - Move Units //////
        ArrayList<Expectation> step2PreExpectations = new ArrayList<Expectation>();
        ArrayList<Expectation> step2PostExpectations = new ArrayList<Expectation>();

        // pre expectations
        step2PreExpectations.add(new Expectation(Expectation.ExpectationName.CONTROL_REGION).setRegionId(GlobalInfo.getStartingRegionID()));
        for (Pair<String, Integer> unitTypeCount : unitTypeCounts) {
            step2PreExpectations.add(new Expectation(Expectation.ExpectationName.HAVE_UNITS).addUnitCountPair(unitTypeCount));
        }
        // post expectations:
        step2PostExpectations.add(new Expectation(Expectation.ExpectationName.UNITS_IN_REGION).setRegionId(GlobalInfo.getEnemyStartingRegionID()));
        step = new PlanStep(PlanStepType.ATTACK_MOVE_UNITS_SURROUND, goalType, step2PreExpectations, step2PostExpectations, this);
        step.setDestRegionId(GlobalInfo.getEnemyStartingRegionID());
        step.setDestination(OntologyInfo.getRegionCenterPosition(GlobalInfo.getEnemyStartingRegionID()));
        this.planSteps.add(step);

        ////// Step 3 - Attack //////
        ArrayList<Expectation> step3PreExpectations = new ArrayList<Expectation>();
        ArrayList<Expectation> step3PostExpectations = new ArrayList<Expectation>();

        step3PostExpectations.add(new Expectation(Expectation.ExpectationName.CONTROL_REGION).setRegionId(GlobalInfo.getEnemyStartingRegionID()));
        step = new PlanStep(PlanStepType.ATTACK, goalType, step3PreExpectations, step3PostExpectations, this);
        step.setDestination(OntologyInfo.getRegionCenterPosition(GlobalInfo.getEnemyStartingRegionID()));
        this.planSteps.add(step);
        
    }
    
    public boolean moveToNextPlanStep(ArrayList<Integer> unitIDs) {
        if(this.isPlanOnLastStep()) return false;
        this.currentPlanStepIndex++;
        // update unit ids for the expectations
        // pre expectations
        for (Expectation e : this.planSteps.get(currentPlanStepIndex).getPreExpectations()) {
            e.setUnitIDs(unitIDs);
        }
        // post expectations
        for (Expectation e : this.planSteps.get(currentPlanStepIndex).getPostExpectations()) {
            e.setUnitIDs(unitIDs);
        }
        return true;
    }

    public PlanStep getCurrentPlanStep() {
        return this.planSteps.get(currentPlanStepIndex);
    }
    
    public boolean isPlanOnLastStep() {
        return this.currentPlanStepIndex >= this.planSteps.size()-1;
    }

    public int getID() {
        return this.ID;
    }

    @Override
    public void reset() {
        this.currentPlanStepIndex = -1;
    }
    
    public String toString() {
        return "Attack Ground Surround";
    }
}
