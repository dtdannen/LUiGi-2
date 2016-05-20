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

import javax.swing.JOptionPane;

import ontology.OntologyInfo;

/**
 *
 * @author Dustin Dannenhauer
 * @email dtd212@lehigh.edu
 * @date Aug 12, 2013
 */
public class PlanFinishAttackAirSneak implements Plan{

   // private Plan plan;
    private Plan parentPlan;
    private int tarRegion = -1;
    
    private ArrayList<PlanStep> planSteps = new ArrayList<>();
    private GoalType goalType = GoalType.ATTACK_FINISH_AIR_SNEAK;
    private ArrayList<Pair<String, Integer>> unitTypeCounts;
    private int currentPlanStepIndex = -1;
    private int ID;
    
    private int myStartingRegion = GlobalInfo.getStartingRegionID();
    
    private boolean isReadyToMoveToNextPlanStep = false;
    
    private ArrayList<Integer> nextStepUnitIds = new ArrayList<Integer>();
    
    private boolean finished = false;
    
    public PlanFinishAttackAirSneak() {
        PlanStep step = null;
        ////// Step 3 - Attack //////
        ArrayList<Expectation> step3PreExpectations = new ArrayList<Expectation>();
        ArrayList<Expectation> step3PostExpectations = new ArrayList<Expectation>();

        step3PostExpectations.add(new Expectation(Expectation.ExpectationName.CONTROL_REGION).setRegionId(GlobalInfo.getEnemyStartingRegionID()));
        step = new PlanStep(PlanStepType.ATTACK_WORKERS, goalType, step3PreExpectations, step3PostExpectations, this);
        step.setDestination(OntologyInfo.getInstance().getRegionCenterPosition(GlobalInfo.getEnemyStartingRegionID()));
        this.planSteps.add(step);
    }
    
    /**
     * Assumed target region is enemy starting region, but does not produce units, instead uses given units
     */
    public PlanFinishAttackAirSneak(ArrayList<Integer> unitIds) {
    	PlanStep step = null;
    	
    	 ////// Step 1 - Acquire Units ////// 
        ArrayList<Expectation> step1PreExpectations = new ArrayList<Expectation>();
        ArrayList<Expectation> step1PostExpectations = new ArrayList<Expectation>();

        // pre expectations
        step1PreExpectations.add(new Expectation(Expectation.ExpectationName.CONTROL_REGION).setRegionId(myStartingRegion));
        // post expectations TODO we should have some post expectation of having these specific units
//        for (Pair<String, Integer> unitTypeCount : unitTypeCounts) {
//            step1PostExpectations.add(new Expectation(Expectation.ExpectationName.HAVE_UNITS).addUnitCountPair(unitTypeCount));
//        }
        step = new PlanStep(PlanStepType.ACQUIRE_UNITS, goalType, step1PreExpectations, step1PostExpectations, this);
        step.setUnitIDs(unitIds);
//        for (Pair<String, Integer> unitTypeCount : unitTypeCounts) {
//            step.addUnitTypeCount(unitTypeCount); // only needed for acquire unit plan step
//        }
        this.planSteps.add(step);
        
        ////// STEP 2 - Attack Workers
        ArrayList<Expectation> step2PreExpectations = new ArrayList<Expectation>();
        ArrayList<Expectation> step2PostExpectations = new ArrayList<Expectation>();

        //step2PostExpectations.add(new Expectation(Expectation.ExpectationName.CONTROL_REGION).setRegionId(GlobalInfo.getEnemyStartingRegionID()));
        step = new PlanStep(PlanStepType.MOVE_UNITS, goalType, step2PreExpectations, step2PostExpectations, this);
        step.setDestination(OntologyInfo.getInstance().getRegionCenterPosition(GlobalInfo.getEnemyStartingRegionID()));
        this.planSteps.add(step);
        
        ////// STEP 2 - Attack Workers
        ArrayList<Expectation> step3PreExpectations = new ArrayList<Expectation>();
        ArrayList<Expectation> step3PostExpectations = new ArrayList<Expectation>();

        step3PostExpectations.add(new Expectation(Expectation.ExpectationName.CONTROL_REGION).setRegionId(GlobalInfo.getEnemyStartingRegionID()));
        step = new PlanStep(PlanStepType.ATTACK_WORKERS, goalType, step3PreExpectations, step3PostExpectations, this);
        step.setDestination(OntologyInfo.getInstance().getRegionCenterPosition(GlobalInfo.getEnemyStartingRegionID()));
        this.planSteps.add(step);
        
    }
    
    /**
     * Constructs a plan with the given target region
     * @param targetRegion
     */
    public PlanFinishAttackAirSneak(int targetRegion) {
        ArrayList<Pair<String, Integer>> unitTypeCounts = new ArrayList<Pair<String, Integer>>();
        unitTypeCounts.add(new Pair<String, Integer>("Terran_Wraith", 2));
        //unitTypeCounts.add(new Pair<String, Integer>("Terran_Siege_Tank_Tank_Mode", 5));
        //unitTypeCounts.add(new Pair<String, Integer>("Terran_Firebat", 6));
        //unitTypeCounts.add(new Pair<String, Integer>("Terran_Medic", 3));
        //this.plan = new PlanAttackDirect(GoalType.ATTACK_AIR_DIRECT, unitTypeCounts, targetRegion);
        tarRegion = targetRegion;
    }
    
    public boolean moveToNextPlanSteps() {
        if(this.isPlanOnLastStep()) return false;
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
        
        // reset isReadyToMoveToNextPlanStep
        this.isReadyToMoveToNextPlanStep = false;
        
        return true;
    }

    public ArrayList<PlanStep> getCurrentPlanSteps() {
    	ArrayList<PlanStep> steps = new ArrayList<PlanStep>();
    	steps.add(this.planSteps.get(currentPlanStepIndex));
        return steps;
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

	@Override
	public void setID(int ID) {
		this.ID = ID;
	}
	
	@Override
	public void setReadyToMoveToNextPlanStep(ArrayList<Integer> unitIds) {
		this.isReadyToMoveToNextPlanStep = true;
		this.nextStepUnitIds = unitIds;
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
		JOptionPane.showMessageDialog(null, "Calling getPrimitivePlans() on a non-primitive plan, with plan id <may be null>");
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
	public String toString() {
		// TODO Auto-generated method stub
		return "Finish Attack Air Sneak";
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
