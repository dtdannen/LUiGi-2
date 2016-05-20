package gda.components;

import gda.planner.PlanStep;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
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

	private static DiscrepancyDetector instance;

	private DiscrepancyDetector() {
	}

	public static synchronized DiscrepancyDetector getInstance() {
		if (instance == null) {
			instance = new DiscrepancyDetector();
		}
		return instance;
	}

	private Logger myLogger = Logger.getLogger(DiscrepancyDetector.class
			.getName());
	private String currentExpectations = null;
	private Thread thisThread = null;
	// private Thread discrepancyDetectorThread = null;
	private JTextArea ontologyTextArea = null;
	private JTextArea regionsTextArea = null;
	private JTextArea unitDataTextArea = null;
	private JTextArea discrepanciesTextArea = null;
	private boolean running;
	private Timer reasonerTimer = null;
	private JLabel reasonerTimerLabel = null;
	private Timer dumpFileTimer = null;
	private JLabel dumpFileLabel = null;
	private ArrayList<PlanStep> planSteps;
	private DiscrepancyDetectorThread inThreadDiscrepancyDetector = null;
	private JTextArea expectationsTextArea = null;
	private JTextField planStepTextField = null;
	private JTextArea planTraceTextArea = null;

	// non-threaded constructor
	public void initialize(JTextArea ontologyTextArea,
			JTextArea controlledRegionsTextArea, JTextArea unitDataTextArea,
			JTextArea expectationsTextArea, JTextArea discrepanciesTextArea,
			JTextField planStepTextField, JTextArea planTraceTextArea) {
		// myLogger.addHandler(new TextAreaHandler(ontologyTextArea));
		// myLogger.log(Level.INFO,
		// "Discrepancy Detector has been constructed!");
		this.ontologyTextArea = ontologyTextArea;
		this.regionsTextArea = controlledRegionsTextArea;
		this.unitDataTextArea = unitDataTextArea;
		this.expectationsTextArea = expectationsTextArea;
		this.discrepanciesTextArea = discrepanciesTextArea;
		this.planStepTextField = planStepTextField;
		this.planTraceTextArea = planTraceTextArea;
		this.planSteps = new ArrayList<>();
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
		if (inThreadDiscrepancyDetector == null) {
			inThreadDiscrepancyDetector = new DiscrepancyDetectorThread(
					this.ontologyTextArea, this.regionsTextArea,
					this.unitDataTextArea, this.discrepanciesTextArea,
					this.reasonerTimer, this.reasonerTimerLabel,
					this.dumpFileTimer, this.dumpFileLabel);
			thisThread = new Thread(inThreadDiscrepancyDetector);
			thisThread.setName("DiscrepancyDetectorThread");
			this.running = true;
			thisThread.start();
		} else if (this.running == false) {
			thisThread.start();
		}
	}

	public void stopDiscrepancyDetection() {
		if (this.running) {
			thisThread.interrupt();
			this.running = false;
			this.planSteps = null;
		}
	}

	public void setCurrentExpectations(String e) {
		this.currentExpectations = e;
	}

	/**
	 * Adds a new plan step to the current steps of this discrepancy detector,
	 * and if there was a previous plan step with the same plan id, that old
	 * plan step is removed.
	 * 
	 * @param pStep
	 */
	public void addPlanStep(PlanStep pStep) {

		// check to see if a current plan step has the same plan step id
		// and if so remove it
		for (PlanStep p : this.planSteps) {
			if (p.getPlan().getID() == pStep.getPlan().getID()) {
				this.planSteps.remove(p);
			}
		}

		// add this step
		this.planSteps.add(pStep);

		// then set the plan steps of the thread plan steps
		this.inThreadDiscrepancyDetector.planSteps = this.planSteps;

		// and update the gui text area
		String expectationsStr = "Pre Expectations:\n";
		expectationsStr += "-----------------\n";
		for (PlanStep p : this.planSteps) {
			expectationsStr += "\nPlan ID " + p.getPlan().getID() + "\n";
			expectationsStr += "-----------\n";
			for (Expectation e : p.getPreExpectations()) {
				expectationsStr += e.toString() + "\n";
			}
		}

		expectationsStr += "\nPost Expectations:\n";
		expectationsStr += "-----------------\n";
		for (PlanStep p : this.planSteps) {
			expectationsStr += "\nPlan ID " + p.getPlan().getID() + "\n";
			expectationsStr += "-----------\n";
			for (Expectation e : p.getPostExpectations()) {
				expectationsStr += e.toString() + "\n";
			}
		}

		this.expectationsTextArea.setText(expectationsStr);
		// JOptionPane.showMessageDialog(null,
		// "Just added expectation string of:\n"+expectationsStr);

		String planStepsStr = "";
		for (PlanStep p : this.planSteps) {
			planStepsStr += p.toString();
		}

		this.planStepTextField.setText(planStepsStr);
		this.planTraceTextArea
				.append("\n[DiscrepancyDetector.setPlanStep()] New step is "
						+ pStep.toString());
	}

	public boolean checkPrevPlanStepPostExpectations(
			ArrayList<PlanStep> planSteps) {
		// JOptionPane.showMessageDialog(null,
		// "in checkPrevPlanStep, plan step is " + planStep.toString() +
		// "\n\n With expectations:\n"+
		// planStep.getPostExpectations().toString());
		boolean expectationsMet = true;
		// first, check the POST expectations of the old step
		for (PlanStep planStep : planSteps) {
			if (planStep != null && !planStep.hasDiscrepancyBeenDetected()) {
				// first clear the discpencancies text area ACTUALLY don't want
				// to
				// do this because want to see discrepancies
				// discrepanciesTextArea.setText("");

				for (Expectation e : planStep.getPostExpectations()) {
					while (!OntologyInfo.getInstance().update()) {
						/* wait until ontology has been refreshed */
						try {
							Thread.sleep(10);
							System.out
									.println("Waiting for ontology to update...");
						} catch (InterruptedException ex) {
							Logger.getLogger(
									DiscrepancyDetector.class.getName()).log(
									Level.SEVERE, null, ex);
						}
					}
					if (!e.isExpectationMet()) {
						// if chooseNextGoal returns false, then ignore this
						// discrepancy, it means its not worth addressing

						if (ExplanationCaseBase.chooseNextGoal(
								planStep.getPlan(), planStep, e,
								e.getExplanation())) {

							// JOptionPane.showMessageDialog(null,
							// "Sir, there is a failed POST expectation:\n " +
							// e.toString());
							discrepanciesTextArea
									.append("[POST] "
											+ e.toString()
											+ "\n------------------------------------------------------\n");
							discrepanciesTextArea
									.setCaretPosition(discrepanciesTextArea
											.getDocument().getLength());
							// this.goalFormulator.goForAirGoal();
							expectationsMet = false;
							break;
						}

					}
				}
			}
		}
		return expectationsMet;
	}
}
