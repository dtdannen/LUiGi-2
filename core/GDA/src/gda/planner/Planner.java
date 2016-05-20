/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gda.planner;

import gda.GlobalInfo;
import gda.Goal;
import gda.components.DiscrepancyDetector;
import java.util.ArrayList;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * This class represents the planner (in the future will probably be swapped out
 * for a hardcore planner (like an HTN planner))
 *
 * @author Dustin Dannenhauer
 * @email dtd212@lehigh.edu
 * @date Jul 13, 2013
 */
public class Planner {

    private Plan plan = null;
    private DiscrepancyDetector discrepancyDetector;
    private ArrayList<Integer> unitIDs;
    private PlannerConnection plannerConnection;
    private static JTextField currPlanTextField;
    private static JTextArea planTraceArea;

    public Planner(DiscrepancyDetector dd) {
        this.discrepancyDetector = dd;
        unitIDs = new ArrayList<Integer>();
    }

    public static void setPlanStepTextField(JTextField currPlanTextFieldFromGui, JTextArea planTraceAreaParam) {
        currPlanTextField = currPlanTextFieldFromGui;
        planTraceArea = planTraceAreaParam;
    }

    /**
     * Start the planner back to the first step of the plan
     */
//    public void reset() {
//        // create new plan
//        this.plan = new PlanAttackGroundDirect();
//        // give next plan step to discrepancy detector
//        if (currPlanTextField != null) {
//            currPlanTextField.setText(this.plan.toString());
//            planTraceArea.append("\n[Planner.reset()] Planner reset, now pursuing plan " + this.plan.toString());
//        }
//    }

    public PlanStep getNextStep(ArrayList<Integer> unitIds) {
        if (this.plan == null) {
            //this.plan.reset();
            //this.plan = new PlanAttackAirDirect();
            this.plan = new Goal(GlobalInfo.STARTING_GOAL,GlobalInfo.getEnemyStartingRegionID()).getPlan();
        }

        // if this returns false, it will choose a new plan, so do not choose
        // new step below
        if (this.discrepancyDetector.checkPrevPlanStepPostExpectations()) {

            PlanStep pStep = null;
            if (plan.moveToNextPlanStep(unitIds)) {
                pStep = plan.getCurrentPlanStep();
            }
            if (pStep == null) {
                this.plan = this.plan = new Goal(GlobalInfo.STARTING_GOAL,GlobalInfo.getEnemyStartingRegionID()).getPlan();
                if (plan.moveToNextPlanStep(unitIds)) {
                    pStep = plan.getCurrentPlanStep();
                }
            }
            sendPlanStepToDiscrepancyDetector();
            if (currPlanTextField != null) {
                currPlanTextField.setText(this.plan.toString());
                planTraceArea.append("\n[Planner.getNextStep()] Now pursuing plan " + this.plan.toString());
            }
            return pStep;
        }else{
            // a new plan has already been chosen and executed, so just use it
            this.plan.moveToNextPlanStep(unitIds);
            // update discrepancy detector with this new step
            sendPlanStepToDiscrepancyDetector();
            // return this step
            return this.plan.getCurrentPlanStep();
        }
        
    }

    public void setUnitIDs(ArrayList<Integer> uIds) {
        this.unitIDs = uIds;
    }

    /**
     * Sends the current plan step to the discrepancy detector
     */
    public void sendPlanStepToDiscrepancyDetector() {
        this.discrepancyDetector.setPlanStep(this.plan.getCurrentPlanStep());

    }

    public boolean isPlanFinished() {
        if (this.plan == null) {
            return false;
        } else {
            return this.plan.isPlanOnLastStep();
        }
    }

    /**
     * Send the new plan to the planner connection class
     *
     * @param plan
     */
    public void setPlan(Plan plan) {
        this.plan = plan;

        if (currPlanTextField != null) {
            currPlanTextField.setText(this.plan.toString());
            planTraceArea.append("\n[Planner.setPlan()] Now pursuing plan " + this.plan.toString());
        }
    }

    public void setPlanAndNotifyBot(Plan plan) {
        this.plan = plan;

        if (currPlanTextField != null) {
            currPlanTextField.setText(this.plan.toString());
            planTraceArea.append("\n[Planner.setPlanAndNotifyBot()] Now pursuing plan " + this.plan.toString());
        }

        PlannerConnection.sendMessageToBot(this.getNextStep(unitIDs).toString());

    }

    public void setPlannerConnection(PlannerConnection plannerConnection) {
        this.plannerConnection = plannerConnection;
    }
}
