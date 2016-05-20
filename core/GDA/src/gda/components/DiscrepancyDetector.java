package gda.components;

import gda.planner.PlanStep;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;
import ontology.OntologyInfo;

/**
 *
 * @author Dustin Dannenhauer
 * @email dtd212@lehigh.edu
 */
public class DiscrepancyDetector {

    private Logger myLogger = Logger.getLogger(DiscrepancyDetector.class.getName());
    private String currentExpectations = null;
    private Thread thisThread = null;
    //private Thread discrepancyDetectorThread = null;
    private JTextArea ontologyTextArea = null;
    private JTextArea regionsTextArea = null;
    private JTextArea unitDataTextArea = null;
    private JTextArea discrepanciesTextArea = null;
    private boolean running;
    private Timer reasonerTimer = null;
    private JLabel reasonerTimerLabel = null;
    private Timer dumpFileTimer = null;
    private JLabel dumpFileLabel = null;
    private PlanStep planStep;
    private DiscrepancyDetectorThread inThreadDiscrepancyDetector = null;
    private JTextArea expectationsTextArea = null;
    private GoalFormulator goalFormulator = null;
    private JTextField planStepTextField = null;
    private JTextArea planTraceTextArea = null;

    // non-threaded constructor
    public DiscrepancyDetector(JTextArea ontologyTextArea, JTextArea controlledRegionsTextArea, JTextArea unitDataTextArea, JTextArea expectationsTextArea, JTextArea discrepanciesTextArea, GoalFormulator goalFormulator, JTextField planStepTextField, JTextArea planTraceTextArea) {
        //myLogger.addHandler(new TextAreaHandler(ontologyTextArea));
        //myLogger.log(Level.INFO, "Discrepancy Detector has been constructed!");
        this.ontologyTextArea = ontologyTextArea;
        this.regionsTextArea = controlledRegionsTextArea;
        this.unitDataTextArea = unitDataTextArea;
        this.expectationsTextArea = expectationsTextArea;
        this.discrepanciesTextArea = discrepanciesTextArea;
        this.planStepTextField = planStepTextField;
        this.goalFormulator = goalFormulator;
        this.planTraceTextArea = planTraceTextArea;
        this.running = false;
    }

    public void testSPARQLQueries() {
    }

    /**
     * Sets the timer which allows the discrepancy detector to reset it when the
     * reasoner restarts
     *
     * @param timer
     */
    public void setReasonerTimer(Timer timer) {
        this.reasonerTimer = timer;
    }

    public void setReasonerTimerLabel(JLabel reasonerTimerLabel) {
        this.reasonerTimerLabel = reasonerTimerLabel;
    }

    public void setDumpFileTimer(Timer timer) {
        this.dumpFileTimer = timer;
    }

    public void setDumpFileTimerLabel(JLabel dumpFileTimerLabel) {
        this.dumpFileLabel = dumpFileTimerLabel;
    }

    /**
     * Starts a new thread that continously checks new gamestate dump files to
     * see if the expectations have been violated
     */
    public void beginDiscrepancyDetection() {
        if (!this.running) {
            inThreadDiscrepancyDetector = new DiscrepancyDetectorThread(this.goalFormulator, this.ontologyTextArea, this.regionsTextArea, this.unitDataTextArea, this.discrepanciesTextArea, this.reasonerTimer, this.reasonerTimerLabel, this.dumpFileTimer, this.dumpFileLabel);
            thisThread = new Thread(inThreadDiscrepancyDetector);
            thisThread.setName("DiscrepancyDetectorThread");
            thisThread.start();
            this.running = true;
        }
    }

    public void stopDiscrepancyDetection() {
        if (this.running) {
            thisThread.interrupt();
            this.running = false;
            this.planStep = null;
        }
    }

    public void setCurrentExpectations(String e) {
        this.currentExpectations = e;
    }

    /**
     * Sets the current plan step to be the given one (erases old one)
     *
     * @param pStep
     */
    public void setPlanStep(PlanStep pStep) {
        
        // then set the plan step of the thread plan step        
        this.inThreadDiscrepancyDetector.planStep = pStep;

        // and update this one
        this.planStep = pStep;

        // and update the gui text area
        String expectationsStr = "Pre Expectations:\n";
        expectationsStr += "-----------------\n";
        for (Expectation e : this.planStep.getPreExpectations()) {
            expectationsStr += e.toString() + "\n";
        }

        expectationsStr += "\nPost Expectations:\n";
        expectationsStr += "-----------------\n";
        for (Expectation e : this.planStep.getPostExpectations()) {
            expectationsStr += e.toString() + "\n";
        }

        this.expectationsTextArea.setText(expectationsStr);

        this.planStepTextField.setText(this.planStep.toString());
        this.planTraceTextArea.append("\n[DiscrepancyDetector.setPlanStep()] New step is "+this.planStep.toString());
    }
    
    public boolean checkPrevPlanStepPostExpectations() {
        boolean expectationsMet = true;
    // first, check the POST expectations of the old step
        if (this.planStep != null && !this.planStep.hasDiscrepancyBeenDetected()) {
            // first clear the discpencancies text area ACTUALLY don't want to do this because want to see discrepancies
            //discrepanciesTextArea.setText("");
            
            for (Expectation e : this.planStep.getPostExpectations()) {
                while (!OntologyInfo.update()) {
                    /* wait until ontology has been refreshed */
                    try {
                        Thread.sleep(10);
                        System.out.println("Waiting for ontology to update...");
                    } catch (InterruptedException ex) {
                        Logger.getLogger(DiscrepancyDetector.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (!e.isExpectationMet()) {
                    // if chooseNextGoal returns false, then ignore this discrepancy, it means its not worth addressing
                    if (ExplanationCaseBase.chooseNextGoal(goalFormulator, planStep.getPlan(), planStep, e, e.getExplanation())) {

                        //JOptionPane.showMessageDialog(null, "Sir, there is a failed POST expectation:\n " + e.toString());
                        discrepanciesTextArea.append("[POST] " + e.toString() + "\n------------------------------------------------------\n");
                        discrepanciesTextArea.setCaretPosition(discrepanciesTextArea.getDocument().getLength());
                        //this.goalFormulator.goForAirGoal();
                        expectationsMet = false;
                        break;
                    }
                }
            }
        }
        return expectationsMet;
    }
}
