/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gda.planner;

import gda.GlobalInfo;
import gda.Goal;
import gda.GoalType;
import gda.components.DiscrepancyDetector;
import gda.components.Explanation;
import gda.components.Explanation.ExplanationType;
import gda.components.ExplanationCaseBase;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * This class represents the planner (in the future will probably be swapped out
 * for a hardcore planner (like an HTN planner))
 * 
 * This is a singleton class
 * 
 * @author Dustin Dannenhauer
 * @email dtd212@lehigh.edu
 * @date Jul 13, 2013
 */
public class Planner {

	private static Planner instance;

	private Planner() {
		// unitIDs = new ArrayList<Integer>();
	}

	public static synchronized Planner getInstance() {
		if (instance == null) {
			instance = new Planner();
		}
		return instance;

	}

	// private Plan plan = null;
	private ArrayList<Plan> plans = new ArrayList<>();
	private ArrayList<Integer> unitIDs;
	private static JTextField currPlanTextField;
	private static JTextArea planTraceArea;
	private static JTextArea plansTextArea;
	private static JTextArea expectationsTextArea;
	private static int nextPlanID = 1;

	public static void setPlanStepTextField(
			JTextField currPlanTextFieldFromGui, JTextArea planTraceAreaParam) {
		currPlanTextField = currPlanTextFieldFromGui;
		planTraceArea = planTraceAreaParam;
	}

	public static void setPlansTextArea(JTextArea tArea) {
		plansTextArea = tArea;
	}

	public static void setExpectationsTextArea(JTextArea expTextArea) {
		expectationsTextArea = expTextArea;
	}

	/**
	 * Start the planner back to the first step of the plan
	 */
	// public void reset() {
	// // create new plan
	// this.plan = new PlanAttackGroundDirect();
	// // give next plan step to discrepancy detector
	// if (currPlanTextField != null) {
	// currPlanTextField.setText(this.plan.toString());
	// planTraceArea.append("\n[Planner.reset()] Planner reset, now pursuing plan "
	// + this.plan.toString());
	// }
	// }

	/**
	 * Returns the next plan step, should only be called on primitive plans!
	 * 
	 * @param unitIds
	 * @param planId
	 * @return
	 */
	public ArrayList<PlanStep> getNextSteps(int planId) {
		ArrayList<PlanStep> nextSteps = new ArrayList<PlanStep>();

		// Grab plan and topmost parent plan
		Plan matchingPlan = this.getPlanById(planId);

		// SAFETY CHECK
		if (matchingPlan == null) {
			JOptionPane.showMessageDialog(null,
					"ERROR: Possible missing plan of id " + planId
							+ "in Planner.getNextStep()");
			return nextSteps;
		}

		// Grab top most parent plan
		Plan topParentPlan = matchingPlan.getParentPlan();
		while (topParentPlan != null && topParentPlan.getParentPlan() != null) {
			topParentPlan = topParentPlan.getParentPlan();
		}

		// if top parent isn't ready, just return
		if (topParentPlan != null) {
			if (!topParentPlan.isReadyToMoveToNextPlanStep()) {
				// JOptionPane.showMessageDialog(null,
				// "topParentPlan isn't ready");
				return nextSteps;
			}

			// we have a parent plan and its not null
			matchingPlan = topParentPlan;

		} else
		// this is the case where this is no parent, and this is a primitive
		// plan (kind of redundant)
		// NOTE: It will only be empty if it is a hierarchical plan and it
		// is waiting on other primitive plan steps to finish
		if (!matchingPlan.isReadyToMoveToNextPlanStep()) {
			return nextSteps;
		}

		updatePlansTextArea();
		updateExpTextArea();

		// if no current plans, get a new default plan
		if (this.plans.isEmpty()) {
			
			for (GoalType gt : GlobalInfo.getStartingGoals()) {
				Plan defaultPlan = new Goal(gt,
						GlobalInfo.getEnemyStartingRegionID()).getPlan();
				this.plans.add(defaultPlan);
				defaultPlan.moveToNextPlanSteps();
				nextSteps.addAll(defaultPlan.getCurrentPlanSteps());
			}
			
		} else {
			// this is the case where we have a previous plan! and need to get
			// the next step(s)

			// very first thing to do is to check that the previous plan step's
			// post expectations are valid
			// if not, discrepancy detector will choose new plan for us

			// returns true if the expectations were met
			// if they were not met, then a new goal will be chosen
			if (DiscrepancyDetector.getInstance()
					.checkPrevPlanStepPostExpectations(
							matchingPlan.getCurrentPlanSteps())) {

				// LOGIC:
				// For now, check if the move to next plan step of the matching
				// plan results in a null plan step
				// IF this is the case, check if there are any other plans. If
				// so, just drop this plan, otherwise
				// add the default plan back into the plan set

				// IF matching plan is not null, then simply return the next
				// plan step

				if (matchingPlan.moveToNextPlanSteps()) {
					nextSteps = matchingPlan.getCurrentPlanSteps();

				} else {
					// Here we know matching plan is finished, so refer to
					// explanation case base
					ExplanationCaseBase.chooseNextGoal(matchingPlan, null,
							null, new Explanation(ExplanationType.SUCCESSFUL));
					this.plans.remove(matchingPlan);

				}
			}

		}

		// Now if any current plans have finished, remove them
		// Have to use an iterator for concurrent modification
		Iterator<Plan> it = this.plans.iterator();
		while (it.hasNext()) {
			if (it.next().isFinished()) {
				it.remove();
			}
		}

		updatePlansTextArea();
		updateExpTextArea();

		return nextSteps;

	}

	// }
	//
	//
	// // PlanStep oldPlanStep = null;
	//
	// // check to see if the current plans match the plan id
	//
	// // if there was a previous plan step for this plan step id
	// // run the discrepancyDetector to check the previous plan's post
	// // expectations
	// if (matchingPlan != null
	// && DiscrepancyDetector.getInstance()
	// .checkPrevPlanStepPostExpectations(oldPlanStep)) {
	//
	// // if we've made it here we know we did NOT find
	// // a discrepancy
	//
	// if (matchingPlan != null) {
	// // old plan step
	// oldPlanStep = matchingPlan.getCurrentPlanStep();
	//
	// matchingPlan.moveToNextPlanStep(unitIds);
	// return matchingPlan.getCurrentPlanStep();
	// }
	// }
	// }
	//
	// // if this returns false, it will choose a new plan, so do not choose
	// // new step below
	// if (DiscrepancyDetector.getInstance()
	// .checkPrevPlanStepPostExpectations()) {
	//
	// PlanStep pStep = null;
	// if (plan.moveToNextPlanStep(unitIds)) {
	// pStep = plan.getCurrentPlanStep();
	// }
	// if (pStep == null) {
	// this.plan = this.plan = new Goal(GlobalInfo.STARTING_GOAL,
	// GlobalInfo.getEnemyStartingRegionID()).getPlan();
	// if (plan.moveToNextPlanStep(unitIds)) {
	// pStep = plan.getCurrentPlanStep();
	// }
	// }
	// sendPlanStepToDiscrepancyDetector();
	// if (currPlanTextField != null) {
	// currPlanTextField.setText(this.plan.toString());
	// planTraceArea
	// .append("\n[Planner.getNextStep()] Now pursuing plan "
	// + this.plan.toString());
	// }
	// return pStep;
	// } else {
	// // a new plan has already been chosen and executed, so just use it
	// this.plan.moveToNextPlanStep(unitIds);
	// // update discrepancy detector with this new step
	// sendPlanStepToDiscrepancyDetector();
	// // return this step
	// return this.plan.getCurrentPlanStep();
	// }
	//

	/**
	 * Send the new plan to the planner connection class
	 * 
	 * @param plan
	 */
	public void addPlan(Plan plan) {

		// double check to make sure this plan doesn't already exist
		if (!(plan.getID() <= 0) && getPlanById(plan.getID()) != null) {
			JOptionPane.showMessageDialog(
					null,
					"Just tried to add already existing plan of id "
							+ plan.getID());
		}

		plan.setID(nextPlanID);
		nextPlanID++;
		this.plans.add(plan);

		plan.moveToNextPlanSteps();

		// if a primitive plan, add it directly
		if (plan.isPrimitive()) {

			if (currPlanTextField != null) {
				if (plan.getParentPlan() != null) {
					currPlanTextField.setText(plan.toString() + " PARENT: "
							+ plan.getParentPlan().toString());
					planTraceArea
							.append("\n[Planner.setPlan()] Now pursuing plan "
									+ plan.toString() + " PARENT: "
									+ plan.getParentPlan().toString());
				} else {
					currPlanTextField.setText(plan.toString());
					planTraceArea
							.append("\n[Planner.setPlan()] Now pursuing plan "
									+ plan.toString());
				}
			}
			updatePlansTextArea();
			// should we just send the plan to the bot once we add it? Yes

			GDABotConnection.sendMessagesToBot(plan.getCurrentPlanSteps());
		} else {
			// NOT PRIMITIVE
			// add all primitive plans from plan
			for (Plan p : plan.getPrimitivePlans()) {
				addPlan(p);
			}
		}
	}

	// public void setPlanAndNotifyBot(Plan plan) {
	// this.plan = plan;
	//
	// if (currPlanTextField != null) {
	// currPlanTextField.setText(this.plan.toString());
	// planTraceArea
	// .append("\n[Planner.setPlanAndNotifyBot()] Now pursuing plan "
	// + this.plan.toString());
	// }
	//
	// GDABotConnection.sendMessageToBot(this.getNextStep(unitIDs, 0)
	// .toString());
	//
	// }

	/**
	 * Given a planId, the planner will take care of aborting the plan, via
	 * sending a message to the bot, and the removing the plan from its current
	 * plans
	 * 
	 * @param planId
	 */
	public void abortPlan(int planId) {
		// check to make sure that this plan id is
		Plan foundPlan = null;
		for (Plan p : this.plans) {
			if (p.getID() == planId) {
				foundPlan = p;
			}
		}

		if (foundPlan != null) {
			// if foundPlan is not primitive, remove all its children too
			// and call abort on them
			if (!foundPlan.isPrimitive()) {
				// go through every plan and check to see if its current parent
				// is this parent
				for (Plan p : Planner.getInstance().getPlans()) {
					if (p.getParentPlan() != null
							&& p.getParentPlan().getID() == foundPlan.getID()) {
						abortPlan(p.getID());
					}
				}
				// and now remove this from plans (do not call abort as bot
				// doesnt know about hierarchical plans)
				removePlan(foundPlan);
			} else {
				// JOptionPane.showMessageDialog(null,
				// "About to abort plan "+foundPlan.getID());
				GDABotConnection.sendAbortMessageToBot(planId);
				removePlan(foundPlan);
				// now remove it from its parent
				
				if (foundPlan.getParentPlan() != null) {
					foundPlan.getParentPlan().removeAbortedSubPlan(foundPlan);
					// don't forget to check to see if we should update the
					// hierarchical plan
					GDABotConnection.getInstance().askPlannerToCheckPlans(
							foundPlan.getParentPlan().getID());
				}
			}
		}
		updatePlansTextArea();
	}

	/**
	 * Aborts every subplan of the hierarchy and removes them from the planner
	 * 
	 * @param planId
	 */
	public void abortPlanHierarchy(int planId) {
		// get the top most parent plan
		// Grab top most parent plan

		if (Planner.getInstance().getPlanById(planId) != null) {

			Plan topParentPlan = Planner.getInstance().getPlanById(planId)
					.getParentPlan();
			while (topParentPlan != null
					&& topParentPlan.getParentPlan() != null) {
				topParentPlan = topParentPlan.getParentPlan();
			}

			if (topParentPlan != null) {
				abortPlan(topParentPlan.getID()); // this will abort all
													// children
				abortPlan(planId);
			} else {
				abortPlan(planId); // only abort this plan
			}
		}
	}

	/**
	 * Used to refresh the plansTextArea of the GUI
	 */
	private void updatePlansTextArea() {
		if (plansTextArea != null) {
			String result = "";
			result += "Aborted Plan IDs: ";
			for (Integer id : ExplanationCaseBase.pastAbortedParentPlans) {
				result += id + " ";
			}
			result += "\n";
			for (Plan p : this.plans) {
				result += "Plan ID: " + p.getID() + " " + p.toString() + " ParentID: " +
						(p.getParentPlan() == null ? "N/a" : p.getParentPlan().getID())+"\n";
			}
			plansTextArea.setText(result);
		}
	}

	/**
	 * Used to refresh the expectationTextArea of the GUI
	 */
	public void updateExpTextArea() {
		// JOptionPane.showMessageDialog(null, "In update expectations!");
		if (expectationsTextArea != null) {
			// JOptionPane.showMessageDialog(null,
			// "In update expectations and text area is not null!");
			String result = "";
			for (Plan p : this.plans) {
				for (PlanStep ps : p.getCurrentPlanSteps()) {
					if (ps.getPlan().isPrimitive()) {
						// JOptionPane.showMessageDialog(null,
						// "About to add expectations");
						result += "----------------------------------------\nPlan ID "
								+ p.getID()
								+ "\n----------------------------------------\n"
								+ "PRE Expectations:\n"
								+ ps.getPreExpectations()
								+ "\n"
								+ "POST Expectations:\n"
								+ ps.getPostExpectations() + "\n";
					}
				}
			}
			expectationsTextArea.setText(result);
		}

	}

	public ArrayList<Plan> getPlans() {
		return this.plans;
	}

	public Plan getPlanById(int planId) {
		for (Plan p : this.plans) {
			if (p.getID() == planId) {
				return p;
			}
		}
		//JOptionPane.showMessageDialog(null, "Did not find plan with planId "
		//		+ planId);
		return null;
	}

	public void removePlan(Plan plan) {
		// keep all plans that do not match the given plan id
		ArrayList<Plan> newPlans = new ArrayList<Plan>();
		for (Plan p : this.plans) {
			if (p.getID() != plan.getID()) {
				newPlans.add(p);
			}
		}
		this.plans = newPlans;
	}

}
