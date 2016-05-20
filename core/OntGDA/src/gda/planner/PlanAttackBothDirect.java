package gda.planner;

import common.Pair;
import gda.GoalType;

import java.util.ArrayList;

import javax.swing.JOptionPane;

/**
 *
 * @author Dustin Dannenhauer
 * @email  dtd212@lehigh.edu
 * @date   Nov 20, 2013
 */
public class PlanAttackBothDirect implements Plan {

    private Plan plan;
    private Plan parentPlan;
    
    public PlanAttackBothDirect() {
        ArrayList<Pair<String, Integer>> unitTypeCounts = new ArrayList<Pair<String, Integer>>();
        unitTypeCounts.add(new Pair<String, Integer>("Terran_Wraith", 2));
        unitTypeCounts.add(new Pair<String, Integer>("Terran_Marine", 4));
        this.plan = new PlanAttackDirect(GoalType.ATTACK_BOTH_DIRECT, unitTypeCounts);
    }

    @Override
    public boolean moveToNextPlanSteps() {
        return this.plan.moveToNextPlanSteps();
    }

    @Override
    public ArrayList<PlanStep> getCurrentPlanSteps() {
        return this.plan.getCurrentPlanSteps();
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
    
    public void setID(int ID) {
    	this.plan.setID(ID);
    }
    
    @Override
	public void setReadyToMoveToNextPlanStep(ArrayList<Integer> unitIds) {
		this.plan.setReadyToMoveToNextPlanStep(unitIds);
		
	}

	@Override
	public boolean isReadyToMoveToNextPlanStep() {
		return this.plan.isReadyToMoveToNextPlanStep();
	}
	
	@Override
	public Plan getParentPlan() {
		return this.plan.getParentPlan();
	}

	@Override
	public void setParentPlan(Plan p) {
		this.plan.setParentPlan(p);
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
		return this.plan.getNextStepUnitIds();
	}

	@Override
	public void setUnitIds(ArrayList<Integer> unitIds) {
		this.plan.setUnitIds(unitIds);
		
	}
	
	@Override
	public boolean isFinished() {
		return this.plan.isFinished();
	}

	@Override
	public void setFinished() {
		this.plan.setFinished();
	}

	@Override
	public GoalType getGoalType() {
		return this.plan.getGoalType();
	}

	@Override
	public void removeAbortedSubPlan(Plan failedPlan) {
		// TODO Auto-generated method stub
		
	}
}
