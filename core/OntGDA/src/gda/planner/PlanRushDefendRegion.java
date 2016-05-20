package gda.planner;

import common.Pair;
import gda.GlobalInfo;
import gda.GoalType;
import gda.components.Expectation;

import java.util.ArrayList;

import javax.swing.JOptionPane;

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
    private Plan parentPlan;
    private ArrayList<PlanStep> planSteps = new ArrayList<>();
    private GoalType goalType = GoalType.RUSH_DEFEND;
    //private ArrayList<Pair<String, Integer>> unitTypeCounts;
    private int currentPlanStepIndex = -1;
    private int ID;

    private ArrayList<Integer> nextStepUnitIds = new ArrayList<Integer>();
    private boolean isReadyToMoveToNextPlanStep = false;
    private boolean finished = false;
    
    
    public PlanRushDefendRegion(int regionIdToDefend) {

        // Don't acquire units, just use the units from the last plan
        this.goalType = GoalType.RUSH_DEFEND;
        PlanStep step = null;
        

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
        step.setDestination(OntologyInfo.getInstance().getRegionCenterPosition(regionIdToDefend));
        //step.setDestRegionId(regionIdToDefend);
        this.planSteps.add(step);

        ////// Step 2 - Defend //////
        ArrayList<Expectation> step2PreExpectations = new ArrayList<Expectation>();
        ArrayList<Expectation> step2PostExpectations = new ArrayList<Expectation>();

        step2PostExpectations.add(new Expectation(Expectation.ExpectationName.CONTROL_REGION).setRegionId(regionIdToDefend));
        step = new PlanStep(PlanStepType.ATTACK, goalType, step2PreExpectations, step2PostExpectations, this);
        step.setDestination(OntologyInfo.getInstance().getRegionCenterPosition(regionIdToDefend));
        this.planSteps.add(step);
    }

    @Override
    public boolean moveToNextPlanSteps() {
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
            e.setUnitIDs(this.nextStepUnitIds);
        }
        // post expectations
        for (Expectation e : this.planSteps.get(currentPlanStepIndex).getPostExpectations()) {
            e.setUnitIDs(this.nextStepUnitIds);
        }
        this.isReadyToMoveToNextPlanStep = false;
        return true;
    }

    @Override
    public ArrayList<PlanStep> getCurrentPlanSteps() {
    	ArrayList<PlanStep> steps = new ArrayList<PlanStep>();
    	steps.add(this.planSteps.get(currentPlanStepIndex));
        return steps;
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
    
    public void setID(int ID) {
    	this.ID = ID;
    }
    
    @Override
	public void setReadyToMoveToNextPlanStep(ArrayList<Integer> unitIds) {
    	this.nextStepUnitIds = unitIds;
    	this.isReadyToMoveToNextPlanStep = true;
	}

	@Override
	public boolean isReadyToMoveToNextPlanStep() {
		return this.isReadyToMoveToNextPlanStep;
	}
	
	@Override
	public Plan getParentPlan() {
		return this.parentPlan;
	}

	@Override
	public void setParentPlan(Plan p) {
		this.parentPlan = p;
	}
	
	@Override
	public boolean isPrimitive() {
		return true;
	}
	
	@Override
	public ArrayList<Plan> getPrimitivePlans() {
		JOptionPane.showMessageDialog(null, "Calling getPrimitivePlans() on a non-primitive plan, with plan id "+plan.getID());
		return null;
	}

	@Override
	public ArrayList<Integer> getNextStepUnitIds() {
		return this.nextStepUnitIds;
	}

	@Override
	public void setUnitIds(ArrayList<Integer> unitIds) {
		this.nextStepUnitIds = unitIds;
		
	}
	
	@Override
	public boolean isFinished() {
		return this.finished;
	}

	@Override
	public void setFinished() {
		// TODO Auto-generated method stub
		this.finished = true;
	}

	@Override
	public GoalType getGoalType() {
		return this.goalType;
	}

	@Override
	public void removeAbortedSubPlan(Plan failedPlan) {
		// TODO Auto-generated method stub
		
	}
	
	
}
