package gda.components;

import java.util.ArrayList;
import java.util.Random;

import gda.GlobalInfo;
import gda.Goal;
import gda.GoalType;
import gda.components.Explanation.ExplanationType;
import gda.planner.Plan;
import gda.planner.PlanStep;
import gda.planner.PlanStepType;
import gda.planner.Planner;

import javax.swing.JOptionPane;

import org.omg.PortableInterceptor.SUCCESSFUL;

/**
 * For now this class will just maintain what to do given a 1. plan step 2.
 * discrepancy 3. explanation
 * 
 * TODO - actually use case based reasoning
 * 
 * @author Dustin Dannenhauer
 * @email dtd212@lehigh.edu
 * @date Aug 22, 2013
 */
public class ExplanationCaseBase {
	static boolean justwentforairgoal = false;

	// keeps track of all plans that have been aborted so that
	// if a parent plan has been aborted, we don't try multiple aborts of the same parent
	// from multiple children and thus issuing many new plan requests
	public static ArrayList<Integer> pastAbortedParentPlans = new ArrayList<Integer>();

	/**
	 * Returns true if a new goal should be chosen, false if ignored
	 * 
	 * IMPORTANT: by returning false, you are stating that the discrepancy didnt matter
	 * and will not show up on the discrepancy gui panel
	 * 
	 * @param gf
	 * @param plan
	 * @param planStep
	 * @param ex
	 * @param explanation
	 * @return
	 */
	public static boolean chooseNextGoal(Plan plan, PlanStep planStep,
			Expectation ex, Explanation explanation) {

		System.out.println("chooseNextGoal("+plan+", "+planStep+", "+ex+", "+explanation+") ThreadID: "+Thread.currentThread().getId());
		
		if (pastAbortedParentPlans.contains(plan.getID()) ||
			(plan.getParentPlan() != null && 
			 pastAbortedParentPlans.contains(plan.getParentPlan().getID()))) {
			return false; // may need to change this, may need to call abort here
		}
		
		
		
		if (plan.isFinished()) {
			System.out.println("plan finished");
			Planner.getInstance().removePlan(plan);
			pastAbortedParentPlans.add(plan.getID());
			if (plan.getParentPlan() != null) {
				pastAbortedParentPlans.add(plan.getParentPlan().getID());
			}
			GoalFormulator.getInstance().redoGoal(plan.getGoalType());
			return true;
		}
		
		
		if (GlobalInfo.RANDOM_CASE_BASE) {
			// this logic should be encoded in the ontology, but for now is here
			// so that the random case base is not penalized for it
			if (ex.getExpectationName() == Expectation.ExpectationName.CONTROL_REGION
					&& explanation.getType() == ExplanationType.NOT_ALL_FRIENDLY_UNITS_IN_REGION
					&& ex.getRegionId() == GlobalInfo.getStartingRegionID()) {
				// if we were invaded while we were attacking
				if (explanation.getUnitIDs().size() < 2) {
					return false;
				}
			}
			
			// pick a random goal and return true
			
			// list of available goals to choose at random
			ArrayList<GoalType> possibleGoals = new ArrayList<GoalType>();
			possibleGoals.add(GoalType.ATTACK_AIR_DIRECT);
			possibleGoals.add(GoalType.ATTACK_AIR_SNEAK);
			possibleGoals.add(GoalType.ATTACK_AIR_SURROUND);
			possibleGoals.add(GoalType.ATTACK_BOTH_DIRECT);
			possibleGoals.add(GoalType.ATTACK_GROUND_DIRECT);
			possibleGoals.add(GoalType.ATTACK_GROUND_SURROUND);
			possibleGoals.add(GoalType.ATTACK_SURROUND_DISTRACT);
			possibleGoals.add(GoalType.RUSH_DEFEND);
			
			Random generator = new Random();
			int i = generator.nextInt(possibleGoals.size());
			GoalType randomlyChosenGoalType = possibleGoals.get(i);
			
			// abort properly
			pastAbortedParentPlans.add(plan.getID());
			if (plan.getParentPlan() != null) {
				pastAbortedParentPlans.add(plan.getParentPlan().getID());
			}
			Planner.getInstance().abortPlanHierarchy(plan.getID());
			
			// this is the only one that needs to be populated with the region id of our starting region
			if (randomlyChosenGoalType == GoalType.RUSH_DEFEND) {
				GoalFormulator.getInstance().goForRushDefendGoal(ex.getRegionId());
			}else{
				GoalFormulator.getInstance().redoGoal(randomlyChosenGoalType);
			}
			return true;
		}
		
		// NOT RANDOM
		
		// check to see if we were invaded while executing our plan
		if (ex.getExpectationName() == Expectation.ExpectationName.CONTROL_REGION
				&& explanation.getType() == ExplanationType.NOT_ALL_FRIENDLY_UNITS_IN_REGION
				&& ex.getRegionId() == GlobalInfo.getStartingRegionID()) {
			// if we were invaded while we were attacking
			if (explanation.getUnitIDs().size() < 2) {
				// just ignore, TODO need to make sure they are protoss probes,
				// not just any unit
				// JOptionPane.showMessageDialog(null,
				// "less than two units, not going to choose new goal");
				return false;
			}
			System.out.println("chooseNextGoal() executing: "+"goForRushDefendGoal(ex.getRegionId())");
			
			pastAbortedParentPlans.add(plan.getID());
			if (plan.getParentPlan() != null) {
				pastAbortedParentPlans.add(plan.getParentPlan().getID());
			}
			
			// abort all plans here, and just go for rush defend goal
			Planner.getInstance().abortPlanHierarchy(plan.getID());
			for (Plan p : Planner.getInstance().getPlans()) {
				Planner.getInstance().abortPlanHierarchy(p.getID());
			}
			
			GoalFormulator.getInstance().goForRushDefendGoal(ex.getRegionId());
			return true;
		}else if (explanation.getType() != ExplanationType.SUCCESSFUL
			&& (planStep.getPlanStepType() == PlanStepType.ATTACK ||
				planStep.getPlanStepType() == PlanStepType.ATTACK_WORKERS)) {
			// if plan finished when it was on an attacking step, repeat it
			System.out.println("chooseNextGoal() executing: "+"redoGoal(plan.getGoalType())");
			
			
			pastAbortedParentPlans.add(plan.getID());
			if (plan.getParentPlan() != null) {
				pastAbortedParentPlans.add(plan.getParentPlan().getID());
			}
			Planner.getInstance().abortPlanHierarchy(plan.getID());
			
			if (plan.getParentPlan() != null) {
				GoalFormulator.getInstance().redoGoal(plan.getParentPlan().getGoalType());
			}else{
				GoalFormulator.getInstance().redoGoal(plan.getGoalType());
			}
			
			return true;
			
		}else if (explanation.getType() == ExplanationType.NO_UNITS_IN_REGION &&
				  plan.getParentPlan() != null &&
				  !plan.getParentPlan().isPrimitive()) {
			
			// check to see if we have another of the same plan being executed
			// helpful value, true if there is another plan of the same type currently being executed by the planner
			boolean anotherSamePlan = false;
			for (Plan p : Planner.getInstance().getPlans()) {
				if (p.getGoalType() == plan.getGoalType()) {
					anotherSamePlan = true;
				}
			}
			
			// if so, then don't abort whole hierarchy, just this local plan, if not, abort whole hierarchy of plan
			if (anotherSamePlan) {
				// just ignore - this means one of our plans failed but we have another doing the same objective
				System.out.println("chooseNextGoal() executing: "+"aborting only this sub plan");
				
				pastAbortedParentPlans.add(plan.getID());
				Planner.getInstance().abortPlan(plan.getID());
				return true;
			}else{
				// there are no other plans doing the same thing so abort whole hierarchy
				System.out.println("chooseNextGoal() executing: "+"aborting whole hierarchy as no other same plan and choosing to redo goal");
				
				pastAbortedParentPlans.add(plan.getID());
				if (plan.getParentPlan() != null) {
					pastAbortedParentPlans.add(plan.getParentPlan().getID());
				}
				Planner.getInstance().abortPlanHierarchy(plan.getID());
				GoalFormulator.getInstance().redoGoal(GlobalInfo.STARTING_GOAL);
				return true;
			}
			
		}else if (explanation.getType() == ExplanationType.MISSING_UNITS) {
			// just ignore and keep going
			System.out.println("chooseNextGoal() executing: "+"NOTHING - just ignoring");
			
			pastAbortedParentPlans.add(plan.getID());
			Planner.getInstance().abortPlanHierarchy(plan.getID());
			return true;
		}else{
			// add another one of the default plans
			System.out.println("chooseNextGoal() executing: "+"starting goal");
			
			pastAbortedParentPlans.add(plan.getID());
			if (plan.getParentPlan() != null) {
				pastAbortedParentPlans.add(plan.getParentPlan().getID());
			}
			Planner.getInstance().abortPlanHierarchy(plan.getID());
			GoalFormulator.getInstance().redoGoal(GlobalInfo.STARTING_GOAL);
			return true;
		} 
	}

}
