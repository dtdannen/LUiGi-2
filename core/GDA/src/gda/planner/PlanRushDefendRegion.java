package gda.planner;

import common.Pair;
import gda.GlobalInfo;
import gda.GoalType;
import gda.components.Expectation;
import java.util.ArrayList;
import ontology.OntologyInfo;

/**
 * Rush defend region represents the plan to defend our region as fast as
 * possible because we are probably getting attack. This is different than
 * defending a region before the enemy has a presence in the region.
 *
 * @author Dustin Dannenhauer
 * @email dtd212@lehigh.edu
 * @date Aug 23, 2013
 */
public class PlanRushDefendRegion implements Plan {

    private Plan plan;
    private ArrayList<PlanStep> planSteps = new ArrayList<>();
    private GoalType goalType;
    //private ArrayList<Pair<String, Integer>> unitTypeCounts;
    private int currentPlanStepIndex = -1;
    private static int ID = 0;

    public PlanRushDefendRegion(int regionIdToDefend) {

        // Don't acquire units, just use the units from the last plan
        this.goalType = GoalType.RUSH_DEFEND;
        PlanStep step = null;
        this.ID++;

        ////// Step 0 - ACQUIRE_UNITS  - get all fighting units, no expectations for now //////
        ArrayList<Expectation> step0PreExpectations = new ArrayList<Expectation>();
        ArrayList<Expectation> step0PostExpectations = new ArrayList<Expectation>();

        //step0PostExpectations.add(new Expectation(Expectation.ExpectationName.HAVE_UNITS_WITH_IDS).setRegionId(regionIdToDefend));
        step = new PlanStep(PlanStepType.ACQUIRE_UNITS, goalType, step0PreExpectations, step0PostExpectations, this);
        step.setUnitIDs(GlobalInfo.getAllFightingUnits());
        this.planSteps.add(step);

        ////// Step 1 - Attack Move Units - to destination //////
        ArrayList<Expectation> step1PreExpectations = new ArrayList<Expectation>();
        ArrayList<Expectation> step1PostExpectations = new ArrayList<Expectation>();

        // post expectations:
        step1PostExpectations.add(new Expectation(Expectation.ExpectationName.UNITS_IN_REGION).setRegionId(regionIdToDefend));
        step = new PlanStep(PlanStepType.ATTACK_MOVE_UNITS, goalType, step1PreExpectations, step1PostExpectations, this);
        step.setDestination(OntologyInfo.getRegionCenterPosition(regionIdToDefend));
        //step.setDestRegionId(regionIdToDefend);
        this.planSteps.add(step);

        ////// Step 2 - Defend //////
        ArrayList<Expectation> step2PreExpectations = new ArrayList<Expectation>();
        ArrayList<Expectation> step2PostExpectations = new ArrayList<Expectation>();

        step2PostExpectations.add(new Expectation(Expectation.ExpectationName.CONTROL_REGION).setRegionId(regionIdToDefend));
        step = new PlanStep(PlanStepType.ATTACK, goalType, step2PreExpectations, step2PostExpectations, this);
        step.setDestination(OntologyInfo.getRegionCenterPosition(regionIdToDefend));
        this.planSteps.add(step);
    }

    @Override
    public boolean moveToNextPlanStep(ArrayList<Integer> unitIDs) {
        if (this.currentPlanStepIndex == 1) {
            System.out.println(currentPlanStepIndex);
        }
        if (this.isPlanOnLastStep()) {
            return false;
        }
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

    @Override
    public PlanStep getCurrentPlanStep() {
        return this.planSteps.get(currentPlanStepIndex);
    }

    @Override
    public boolean isPlanOnLastStep() {
        return this.currentPlanStepIndex == this.planSteps.size() - 1;
    }
    
    public String toString() {
        return "Rush Defend Region";
    }

    public int getID() {
        return ID;
    }
    
    @Override
    public void reset() {
        this.currentPlanStepIndex = -1;
    }
}
