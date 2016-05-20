package gda.planner;

import common.Pair;
import gda.GlobalInfo;
import gda.GoalType;
import gda.components.Expectation;

import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import ontology.OntologyInfo;

/**
 *
 * @author Dustin Dannenhauer
 * @email dtd212@lehigh.edu
 * @date Aug 12, 2013
 */
public class PlanAttackDirect implements Plan {

    private ArrayList<PlanStep> planSteps = new ArrayList<>();
    private GoalType goalType;
    private ArrayList<Pair<String, Integer>> unitTypeCounts;
    private int currentPlanStepIndex = -1;
    private int ID;
    
    private int myStartingRegion = GlobalInfo.getStartingRegionID();
    
    private boolean isReadyToMoveToNextPlanStep = false;
    
    private Plan parentPlan;
    
    private ArrayList<Integer> nextStepUnitIds = new ArrayList<Integer>();
    
    private boolean finished = false;
    
 // note this may be changed to be a different region, like an adjacent region
    private int myTargetRegion = GlobalInfo.getEnemyStartingRegionID();  

    /**
     * This constructs a plan with a default target region of the enemy's starting location
     * @param goalType
     * @param unitTypeCounts
     */
    public PlanAttackDirect(GoalType goalType, ArrayList<Pair<String, Integer>> unitTypeCounts) {
        this.goalType = goalType;
        PlanStep step = null;
        
        

        ////// Step 1 - Acquire Units ////// 
        ArrayList<Expectation> step1PreExpectations = new ArrayList<Expectation>();
        ArrayList<Expectation> step1PostExpectations = new ArrayList<Expectation>();

        // pre expectations
        step1PreExpectations.add(new Expectation(Expectation.ExpectationName.CONTROL_REGION).setRegionId(myStartingRegion));
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
        step2PreExpectations.add(new Expectation(Expectation.ExpectationName.CONTROL_REGION).setRegionId(myStartingRegion));
        for (Pair<String, Integer> unitTypeCount : unitTypeCounts) {
            step2PreExpectations.add(new Expectation(Expectation.ExpectationName.HAVE_UNITS).addUnitCountPair(unitTypeCount));
        }
        // post expectations:
        step2PostExpectations.add(new Expectation(Expectation.ExpectationName.UNITS_IN_REGION).setRegionId(myTargetRegion));
        step = new PlanStep(PlanStepType.ATTACK_MOVE_UNITS, goalType, step2PreExpectations, step2PostExpectations, this);
        step.setDestination(OntologyInfo.getInstance().getRegionCenterPosition(GlobalInfo.getEnemyStartingRegionID()));
        step.setDestRegionId(GlobalInfo.getEnemyStartingRegionID());
        this.planSteps.add(step);

        ////// Step 3 - Attack //////
        ArrayList<Expectation> step3PreExpectations = new ArrayList<Expectation>();
        ArrayList<Expectation> step3PostExpectations = new ArrayList<Expectation>();

        step3PostExpectations.add(new Expectation(Expectation.ExpectationName.CONTROL_REGION).setRegionId(myTargetRegion));
        step = new PlanStep(PlanStepType.ATTACK, goalType, step3PreExpectations, step3PostExpectations, this);
        step.setDestination(OntologyInfo.getInstance().getRegionCenterPosition(myTargetRegion));
        this.planSteps.add(step);
    }
    
    /**
     * This constructs a plan where the target region is given
     * @param goalType
     * @param unitTypeCounts
     * @param targetRegion
     */
    public PlanAttackDirect(GoalType goalType, ArrayList<Pair<String, Integer>> unitTypeCounts, int targetRegion) {
        this.goalType = goalType;
        PlanStep step = null;
        //this.ID++;
        
        myTargetRegion = targetRegion; 
        

        ////// Step 1 - Acquire Units ////// 
        ArrayList<Expectation> step1PreExpectations = new ArrayList<Expectation>();
        ArrayList<Expectation> step1PostExpectations = new ArrayList<Expectation>();

        // pre expectations
        step1PreExpectations.add(new Expectation(Expectation.ExpectationName.CONTROL_REGION).setRegionId(myStartingRegion));
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
        step2PreExpectations.add(new Expectation(Expectation.ExpectationName.CONTROL_REGION).setRegionId(myStartingRegion));
        for (Pair<String, Integer> unitTypeCount : unitTypeCounts) {
            step2PreExpectations.add(new Expectation(Expectation.ExpectationName.HAVE_UNITS).addUnitCountPair(unitTypeCount));
        }
        // post expectations:
        step2PostExpectations.add(new Expectation(Expectation.ExpectationName.UNITS_IN_REGION).setRegionId(myTargetRegion));
        step = new PlanStep(PlanStepType.ATTACK_MOVE_UNITS, goalType, step2PreExpectations, step2PostExpectations, this);
        step.setDestination(OntologyInfo.getInstance().getRegionCenterPosition(myTargetRegion));
        step.setDestRegionId(myTargetRegion);
        this.planSteps.add(step);

        ////// Step 3 - Attack //////
        ArrayList<Expectation> step3PreExpectations = new ArrayList<Expectation>();
        ArrayList<Expectation> step3PostExpectations = new ArrayList<Expectation>();

        step3PreExpectations.add(new Expectation(Expectation.ExpectationName.CONTROL_REGION).setRegionId(myStartingRegion));
        step3PostExpectations.add(new Expectation(Expectation.ExpectationName.CONTROL_REGION).setRegionId(myTargetRegion));
        step = new PlanStep(PlanStepType.ATTACK, goalType, step3PreExpectations, step3PostExpectations, this);
        step.setDestination(OntologyInfo.getInstance().getRegionCenterPosition(myTargetRegion));
        this.planSteps.add(step);
    }
    
    /**
     * Like a 'finisher' which takes unitIds and uses them to attack the destined region
     * the boolean param is just to make this signature different than the first one
     * @param goalType
     * @param unitIds
     */
    public PlanAttackDirect(GoalType goalType, ArrayList<Integer> unitIds, boolean makeSignatureUnique) {
    	this.goalType = goalType;
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
        
        
    	//// Step 2 - Move Units //////
        ArrayList<Expectation> step2PreExpectations = new ArrayList<Expectation>();
        ArrayList<Expectation> step2PostExpectations = new ArrayList<Expectation>();

        // pre expectations
        step2PreExpectations.add(new Expectation(Expectation.ExpectationName.CONTROL_REGION).setRegionId(myStartingRegion));
//        for (Pair<String, Integer> unitTypeCount : unitTypeCounts) {
//            step2PreExpectations.add(new Expectation(Expectation.ExpectationName.HAVE_UNITS).addUnitCountPair(unitTypeCount));
//        }
        
        // post expectations:
        step2PostExpectations.add(new Expectation(Expectation.ExpectationName.UNITS_IN_REGION).setRegionId(myTargetRegion));
        step = new PlanStep(PlanStepType.ATTACK_MOVE_UNITS, goalType, step2PreExpectations, step2PostExpectations, this);
        step.setDestination(OntologyInfo.getInstance().getRegionCenterPosition(GlobalInfo.getEnemyStartingRegionID()));
        step.setDestRegionId(GlobalInfo.getEnemyStartingRegionID());
        step.setUnitIDs(unitIds); // BIGGEST DIFFERENCE FROM OTHER CONSTRUCTORS
        this.planSteps.add(step);

        ////// Step 3 - Attack //////
        ArrayList<Expectation> step3PreExpectations = new ArrayList<Expectation>();
        ArrayList<Expectation> step3PostExpectations = new ArrayList<Expectation>();

        step3PostExpectations.add(new Expectation(Expectation.ExpectationName.CONTROL_REGION).setRegionId(myTargetRegion));
        step = new PlanStep(PlanStepType.ATTACK, goalType, step3PreExpectations, step3PostExpectations, this);
        step.setDestination(OntologyInfo.getInstance().getRegionCenterPosition(GlobalInfo.getEnemyStartingRegionID()));
        this.planSteps.add(step);

    	
    }

    /**
     * Like a 'finisher' which takes unitIds and uses them to attack the destined region
     * the boolean params are just to make this signature different than the first one
     * @param goalType
     * @param unitIds
     */
    public PlanAttackDirect(GoalType goalType, ArrayList<Integer> unitIds, boolean makeSignatureUnique, boolean makeSignatureUnique2) {
    	this.goalType = goalType;
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
        step = new PlanStep(PlanStepType.PRODUCE_UNITS, goalType, step1PreExpectations, step1PostExpectations, this);
        step.setUnitIDs(unitIds);
//        for (Pair<String, Integer> unitTypeCount : unitTypeCounts) {
//            step.addUnitTypeCount(unitTypeCount); // only needed for acquire unit plan step
//        }
        this.planSteps.add(step);
        
        
    	//// Step 2 - Move Units //////
        ArrayList<Expectation> step2PreExpectations = new ArrayList<Expectation>();
        ArrayList<Expectation> step2PostExpectations = new ArrayList<Expectation>();

        // pre expectations
        step2PreExpectations.add(new Expectation(Expectation.ExpectationName.CONTROL_REGION).setRegionId(myStartingRegion));
//        for (Pair<String, Integer> unitTypeCount : unitTypeCounts) {
//            step2PreExpectations.add(new Expectation(Expectation.ExpectationName.HAVE_UNITS).addUnitCountPair(unitTypeCount));
//        }
        
        // post expectations:
        step2PostExpectations.add(new Expectation(Expectation.ExpectationName.UNITS_IN_REGION).setRegionId(myTargetRegion));
        step = new PlanStep(PlanStepType.ATTACK_MOVE_UNITS, goalType, step2PreExpectations, step2PostExpectations, this);
        step.setDestination(OntologyInfo.getInstance().getRegionCenterPosition(GlobalInfo.getEnemyStartingRegionID()));
        step.setDestRegionId(GlobalInfo.getEnemyStartingRegionID());
        step.setUnitIDs(unitIds); // BIGGEST DIFFERENCE FROM OTHER CONSTRUCTORS
        this.planSteps.add(step);

        ////// Step 3 - Attack //////
        ArrayList<Expectation> step3PreExpectations = new ArrayList<Expectation>();
        ArrayList<Expectation> step3PostExpectations = new ArrayList<Expectation>();

        step3PostExpectations.add(new Expectation(Expectation.ExpectationName.CONTROL_REGION).setRegionId(myTargetRegion));
        step = new PlanStep(PlanStepType.ATTACK_WORKERS, goalType, step3PreExpectations, step3PostExpectations, this);
        step.setDestination(OntologyInfo.getInstance().getRegionCenterPosition(GlobalInfo.getEnemyStartingRegionID()));
        this.planSteps.add(step);

    	
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
	public GoalType getGoalType() {
		return this.goalType;
	}

	@Override
	public void removeAbortedSubPlan(Plan failedPlan) {
		// TODO Auto-generated method stub
		
	}
	
	
}

