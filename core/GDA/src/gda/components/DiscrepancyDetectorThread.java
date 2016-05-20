/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gda.components;

import gda.planner.PlanStep;
import gui.TextAreaHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import ontology.OntologyInfo;

/**
 *
 * @author Dustin Dannenhauer
 * @email dtd212@lehigh.edu
 * @date Aug 10, 2013
 */
public class DiscrepancyDetectorThread implements Runnable {

    private Logger myLogger = Logger.getLogger(DiscrepancyDetector.class.getName());
    private JTextArea ontologyTextArea = null;
    private JTextArea regionsTextArea = null;
    private JTextArea unitDataTextArea = null;
    private JTextArea discrepanciesTextArea = null;
    private boolean running;
    private Timer reasonerTimer = null;
    private JLabel reasonerTimerLabel = null;
    private Timer dumpFileTimer = null;
    private JLabel dumpFileLabel = null;
    private GoalFormulator goalFormulator = null;
    public volatile PlanStep planStep;

    // threaded constructor (this should only be called within the discrepancy detector
    public DiscrepancyDetectorThread(GoalFormulator goalFormulator, JTextArea ontologyTextArea, JTextArea regionsTextArea, JTextArea unitDataTextArea, JTextArea discrepanciesTextArea, Timer reasonerTimer, JLabel reasonerTimerLabel, Timer dumpFileTimer, JLabel dumpFileLabel) {
        myLogger.addHandler(new TextAreaHandler(ontologyTextArea));
        this.myLogger.log(Level.INFO, "Discrepancy Detector has been constructed!");
        this.ontologyTextArea = ontologyTextArea;
        this.regionsTextArea = regionsTextArea;
        this.unitDataTextArea = unitDataTextArea;
        this.discrepanciesTextArea = discrepanciesTextArea;
        this.goalFormulator = goalFormulator;
        this.running = false;

        // only for threaded version
        this.reasonerTimer = reasonerTimer;
        this.reasonerTimerLabel = reasonerTimerLabel;
        this.dumpFileTimer = dumpFileTimer;
        this.dumpFileLabel = dumpFileLabel;
        OntologyInfo.setLastDumpFileLabel(dumpFileLabel);
        OntologyInfo.setLastDumpFileTimer(dumpFileTimer);

        OntologyInfo.setLogger(myLogger);
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            // get the latest gamestate dump and load into ontology
            OntologyInfo.update();
            // now query ontology to see if we expectations exist or have been violated
            if (this.planStep != null && !this.planStep.hasDiscrepancyBeenDetected()) {
                for (Expectation e : this.planStep.getPreExpectations()) {
                    if (!e.isExpectationMet()) {
                        // if chooseNextGoal returns false, then ignore this discrepancy, it means its not worth addressing
                        if (ExplanationCaseBase.chooseNextGoal(goalFormulator, planStep.getPlan(), planStep, e, e.getExplanation())) {

                            //JOptionPane.showMessageDialog(null, "Sir, there is a failed PRE expectation:\n " + e.toString());
                            //if () {
                            //this.discrepanciesTextArea.append("[PRE] " + e.toString() + "RESPONSE: " + ExplanationCaseBase.chooseNextGoal(goalFormulator, planStep.getPlan(), planStep, e, e.getExplanation()) + "\n------------------------------------------------------\n");
                            this.discrepanciesTextArea.append("[PRE] " + e.toString() + "\n------------------------------------------------------\n");
                            discrepanciesTextArea.setCaretPosition(discrepanciesTextArea.getDocument().getLength());
                            //JOptionPane.showMessageDialog(null, "Sir, there is a failed PRE expectation:\nChoosing new goal" + e.toString());

                            // mark the current plan step as being terminated
                            this.planStep.discrepancyHasBeenDetected();

                            break;
                            //}
                        }
                    }
                }
            }

            /**
             * ******* TESTING SPARQL QUERIES ********
             */
            //jenaOntologyModel.queryPlayerPresenceRegions(0);
            //jenaOntologyModel.queryPlayerPresenceRegions(1);
            // reset the timer - we just finished reasoning
            if (this.reasonerTimer != null && this.reasonerTimerLabel != null) {
                this.reasonerTimer.restart();
                this.reasonerTimerLabel.setText("0");
            }

//            String regionDataString = "Controlled Regions:\n"
//                    + OntologyInfo.getControlledRegions().toString().replace(">, <",">\n<") // little hack to make output slightly more readable
//                    + "\n\nContested Regions:\n"
//                    + OntologyInfo.getContestedRegions().toString().replace(">, <",">\n<");
            //this.regionsTextArea.setText(regionDataString);
            this.regionsTextArea.setText(OntologyInfo.getRegionData());
            this.unitDataTextArea.setText(OntologyInfo.getUnitData());
            this.myLogger.log(Level.INFO, "Just finished reasoning");
            //System.out.println(jenaOntologyModel.getControlledRegions());
            //sbreak;
        }
    }
}
