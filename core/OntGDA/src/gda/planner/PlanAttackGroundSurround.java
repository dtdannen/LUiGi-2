/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gda.planner;

import common.Pair;
import gda.GlobalInfo;
import gda.GoalType;
import gda.components.Expectation;

import java.awt.Point;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import ontology.OntologyInfo;

/**
 * 
 * @author Dustin Dannenhauer
 * @email dtd212@lehigh.edu
 * @date Nov 20, 2013
 */
public class PlanAttackGroundSurround implements Plan {

	private ArrayList<PlanStep> planSteps = new ArrayList<>();
	private GoalType goalType = GoalType.ATTACK_GROUND_SURROUND;
	private ArrayList<Pair<String, Integer>> unitTypeCounts;
	private int currentPlanStepIndex = -1;
	private int ID = 0;

	private ArrayList<ArrayList<Plan>> subplans = new ArrayList<>();
	private int subPlanIndex = 0;

	private Plan parentPlan;

	private boolean finished = false;
	
	public PlanAttackGroundSurround() {
		this.goalType = GoalType.ATTACK_GROUND_SURROUND;

		// create subplans for each adjacent region
		// find all surrounding regions
		ArrayList<String> p = OntologyInfo.getInstance().getAdjacentRegions(
				GlobalInfo.getEnemyStartingRegionID());
		int currRegionID = -1;
		this.subplans.add(new ArrayList<Plan>()); // first set of sub plans
		for (String s : p) {
			// s will be of the form "region 3", so we do a little parsing to
			// get just "3" and turn that into an integer
			currRegionID = Integer.parseInt(s.replaceAll("region", "").trim());
			this.subplans.get(0).add(new PlanAttackGroundDirect(currRegionID));
		}

		// create an ending plan that will take all the units from previous set
		// of plans
		// and use them to attack the enemy
		// TODO
		
		subplans.add(new ArrayList<Plan>()); // this prevents it from being on the last plan step

		// for all subplans, make this plan the parent
		for (ArrayList<Plan> subPlanSet : subplans) {
			for (Plan subPlan : subPlanSet) {
				subPlan.setParentPlan(this);
			}
		}
		//
		// PlanStep step = null;
		// this.ID++;
		// unitTypeCounts = new ArrayList<Pair<String, Integer>>();
		//
		// // Step 0. Produce units
		// // //// Step 1 - Acquire Units //////
		// ArrayList<Expectation> step1PreExpectations = new
		// ArrayList<Expectation>();
		// ArrayList<Expectation> step1PostExpectations = new
		// ArrayList<Expectation>();
		//
		// // pre expectations
		// step1PreExpectations.add(new Expectation(
		// Expectation.ExpectationName.CONTROL_REGION)
		// .setRegionId(GlobalInfo.getStartingRegionID()));
		// unitTypeCounts.add(new Pair<String, Integer>("Terran_Marine", 10));
		// // post expectations
		// for (Pair<String, Integer> unitTypeCount : unitTypeCounts) {
		// step1PostExpectations.add(new Expectation(
		// Expectation.ExpectationName.HAVE_UNITS)
		// .addUnitCountPair(unitTypeCount));
		// }
		//
		// step = new PlanStep(PlanStepType.PRODUCE_UNITS, goalType,
		// step1PreExpectations, step1PostExpectations, this);
		// for (Pair<String, Integer> unitTypeCount : unitTypeCounts) {
		// step.addUnitTypeCount(unitTypeCount); // only needed for acquire
		// // unit plan step
		// }
		// this.planSteps.add(step);
		//
		// // //// Step 2 - Move Units //////
		// ArrayList<Expectation> step2PreExpectations = new
		// ArrayList<Expectation>();
		// ArrayList<Expectation> step2PostExpectations = new
		// ArrayList<Expectation>();
		//
		// // pre expectations
		// step2PreExpectations.add(new Expectation(
		// Expectation.ExpectationName.CONTROL_REGION)
		// .setRegionId(GlobalInfo.getStartingRegionID()));
		// for (Pair<String, Integer> unitTypeCount : unitTypeCounts) {
		// step2PreExpectations.add(new Expectation(
		// Expectation.ExpectationName.HAVE_UNITS)
		// .addUnitCountPair(unitTypeCount));
		// }
		// // post expectations:
		// step2PostExpectations.add(new Expectation(
		// Expectation.ExpectationName.UNITS_IN_REGION)
		// .setRegionId(GlobalInfo.getEnemyStartingRegionID()));
		// step = new PlanStep(PlanStepType.ATTACK_MOVE_UNITS_SURROUND,
		// goalType,
		// step2PreExpectations, step2PostExpectations, this);
		// step.setDestRegionId(GlobalInfo.getEnemyStartingRegionID());
		// step.setDestination(OntologyInfo.getInstance().getRegionCenterPosition(GlobalInfo
		// .getEnemyStartingRegionID()));
		// this.planSteps.add(step);
		//
		// // //// Step 3 - Attack //////
		// ArrayList<Expectation> step3PreExpectations = new
		// ArrayList<Expectation>();
		// ArrayList<Expectation> step3PostExpectations = new
		// ArrayList<Expectation>();
		//
		// step3PostExpectations.add(new Expectation(
		// Expectation.ExpectationName.CONTROL_REGION)
		// .setRegionId(GlobalInfo.getEnemyStartingRegionID()));
		// step = new PlanStep(PlanStepType.ATTACK, goalType,
		// step3PreExpectations, step3PostExpectations, this);
		// step.setDestination(OntologyInfo.getInstance().getRegionCenterPosition(GlobalInfo
		// .getEnemyStartingRegionID()));
		// this.planSteps.add(step);
		//
	}

	public boolean moveToNextPlanSteps() {

		// for all subplans in the current bracket, check they are ready to move
		// forward
		boolean allready = true;
		for (Plan p : subplans.get(subPlanIndex)) {
			if (!p.isReadyToMoveToNextPlanStep()) {
				allready = false;
			}
		}

		if (!allready) {
			return false;
		} 

		// check to see if all plans are on last step, if so
		// then no need to call move to next step,
		// remove them and move to the nex bracket of plans
		boolean allfinished = true;
		for (Plan p : subplans.get(subPlanIndex)) {
			if (!p.isPlanOnLastStep()) {
				allfinished = false;
			}
		}

		if (subPlanIndex == 1) {
			//JOptionPane.showMessageDialog(null, "On second bracket of plans in AttackGroundSurround");
			
		}
		
		if (allfinished) {
			//JOptionPane.showMessageDialog(null, "All finished");
			
			
			// *I THINK THE PLANNER WILL REMOVE PLANS AUTOMATICALLY? IF NOT MAY
			// NEED TO DO IT HERE
			//JOptionPane.showMessageDialog(null, "here2");
			if (subPlanIndex == 0) {
				subPlanIndex++;

				ArrayList<Integer> prevUnitIds = new ArrayList<Integer>();
				for (Plan prevPlan : subplans.get(0)) {
					prevUnitIds.addAll(prevPlan.getNextStepUnitIds());
				}
//				JOptionPane.showMessageDialog(
//						null,
//						"Giving Plan ID " + p.getID()
//								+ " the following units:\n "
//								+ prevUnitIds.toString());

				// DYNAMICALLY add the new plan, and give it prev unit ids
				
				Plan newPlan = new PlanAttackGroundDirect(prevUnitIds);
				newPlan.setParentPlan(this);
				subplans.get(1).add(newPlan);
				Planner.getInstance().addPlan(newPlan);
			}
			
			// mark all the previous plans as finished
			for (Plan p : subplans.get(subPlanIndex-1)) {
				p.setFinished();
			}

		} else {
			// need to move each plan forward in this group of plans
			//JOptionPane.showMessageDialog(null, "here");
			for (Plan p : subplans.get(subPlanIndex)) {
				if (!p.isPlanOnLastStep()) {
					p.moveToNextPlanSteps();
				}
			}

		}

		// update unit ids for the expectations
		// pre expectations
		for (Plan p : this.subplans.get(subPlanIndex)) {
			for (PlanStep ps : p.getCurrentPlanSteps()) {
				// pre expectation
				for (Expectation e : ps.getPreExpectations()) {
					e.setUnitIDs(p.getNextStepUnitIds());
				}
				// post expectations
				for (Expectation e : ps.getPostExpectations()) {
					e.setUnitIDs(p.getNextStepUnitIds());
				}
			}
		}

		return true;
	}

	public boolean isPlanOnLastStep() {
		return this.subPlanIndex >= this.subplans.size() - 1;
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

	public void setID(int ID) {
		this.ID = ID;
	}

	@Override
	/*
	 * Returns true if all subplans are ready, otherwise false
	 * 
	 * (non-Javadoc)
	 * 
	 * @see gda.planner.Plan#isReadyToMoveToNextPlanStep()
	 */
	public boolean isReadyToMoveToNextPlanStep() {
		boolean someSubPlanNotReady = false;
		for (Plan p : this.subplans.get(subPlanIndex)) {

			if (!p.isReadyToMoveToNextPlanStep()) {
				someSubPlanNotReady = true;
				break;
			}
		}

		return !someSubPlanNotReady;
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
		return false;
	}

	@Override
	// recursively return all primitive plans
	public ArrayList<Plan> getPrimitivePlans() {
		ArrayList<Plan> primPlans = new ArrayList<Plan>();
		for (Plan p : this.subplans.get(subPlanIndex)) {
			if (p.isPrimitive()) {
				primPlans.add(p);
			} else {
				primPlans.addAll(p.getPrimitivePlans());
			}
		}
		return primPlans;
	}

	@Override
	public void setReadyToMoveToNextPlanStep(ArrayList<Integer> unitIds) {
		// this should not be used because it is not a primitive plan

	}

	@Override
	public ArrayList<Integer> getNextStepUnitIds() {
		// should not be used because it is a primitive plan
		return null;
	}

	@Override
	public ArrayList<PlanStep> getCurrentPlanSteps() {
		ArrayList<PlanStep> currSteps = new ArrayList<PlanStep>();
		for (Plan p : subplans.get(subPlanIndex)) {
			currSteps.addAll(p.getCurrentPlanSteps());
		}
		return currSteps;
	}

	@Override
	public void setUnitIds(ArrayList<Integer> unitIds) {
		// TODO honestly i'm not sure how this would be used?

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
