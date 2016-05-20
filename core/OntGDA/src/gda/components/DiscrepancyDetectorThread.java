/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gda.components;

import gda.planner.PlanStep;
import gui.TextAreaHandler;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.Timer;

import ontology.OntologyInfo;
import ontology.OntologyInfoDisplayable;

/**
 * 
 * @author Dustin Dannenhauer
 * @email dtd212@lehigh.edu
 * @date Aug 10, 2013
 */
public class DiscrepancyDetectorThread implements Runnable {

	private Logger myLogger = Logger.getLogger(DiscrepancyDetector.class
			.getName());
	private JTextArea ontologyTextArea = null;
	private JTextArea regionsTextArea = null;
	private JTextArea unitDataTextArea = null;
	private JTextArea discrepanciesTextArea = null;
	private boolean running;
	private Timer reasonerTimer = null;
	private JLabel reasonerTimerLabel = null;
	private Timer dumpFileTimer = null;
	private JLabel dumpFileLabel = null;
	// public volatile PlanStep planStep;
	public volatile ArrayList<PlanStep> planSteps;

	// threaded constructor (this should only be called within the discrepancy
	// detector
	public DiscrepancyDetectorThread(JTextArea ontologyTextArea,
			JTextArea regionsTextArea, JTextArea unitDataTextArea,
			JTextArea discrepanciesTextArea, Timer reasonerTimer,
			JLabel reasonerTimerLabel, Timer dumpFileTimer, JLabel dumpFileLabel) {
		myLogger.addHandler(new TextAreaHandler(ontologyTextArea));
		this.myLogger.log(Level.INFO,
				"Discrepancy Detector has been constructed!");
		this.ontologyTextArea = ontologyTextArea;
		this.regionsTextArea = regionsTextArea;
		this.unitDataTextArea = unitDataTextArea;
		this.discrepanciesTextArea = discrepanciesTextArea;
		this.running = false;

		// only for threaded version
		this.reasonerTimer = reasonerTimer;
		this.reasonerTimerLabel = reasonerTimerLabel;
		this.dumpFileTimer = dumpFileTimer;
		this.dumpFileLabel = dumpFileLabel;
		OntologyInfoDisplayable.setLastDumpFileLabel(dumpFileLabel);
		OntologyInfoDisplayable.setLastDumpFileTimer(dumpFileTimer);

		OntologyInfoDisplayable.setLogger(myLogger);

		planSteps = new ArrayList<>();
	}

	@Override
	public void run() {
		while (!Thread.interrupted()) {
			
			// THIS IS CHECKING PRE EXPECTATIONS
			
			// get the latest gamestate dump and load into ontology
			OntologyInfo.getInstance().update();
			// now query ontology to see if we expectations exist or have been
			// violated
			for (PlanStep p : planSteps) {
				if (p != null && !p.hasDiscrepancyBeenDetected()) {
					for (Expectation e : p.getPreExpectations()) {
						if (!e.isExpectationMet()) {
							// if chooseNextGoal returns false, then ignore this
							// discrepancy, it means its not worth addressing
							if (ExplanationCaseBase.chooseNextGoal(p.getPlan(),
									p, e, e.getExplanation())) {

								// JOptionPane.showMessageDialog(null,
								// "Sir, there is a failed PRE expectation:\n "
								// + e.toString());
								// if () {
								// this.discrepanciesTextArea.append("[PRE] " +
								// e.toString() + "RESPONSE: " +
								// ExplanationCaseBase.chooseNextGoal(goalFormulator,
								// planStep.getPlan(), planStep, e,
								// e.getExplanation()) +
								// "\n------------------------------------------------------\n");
								this.discrepanciesTextArea
										.append("[PRE] "
												+ e.toString()
												+ "\n------------------------------------------------------\n");
								discrepanciesTextArea
										.setCaretPosition(discrepanciesTextArea
												.getDocument().getLength());
								// JOptionPane.showMessageDialog(null,
								// "Sir, there is a failed PRE expectation:\nChoosing new goal"
								// + e.toString());

								// mark the current plan step as being
								// terminated
								p.discrepancyHasBeenDetected();

								break;
								// }
							}
						}
					}
				}
			}

			/**
			 * ******* TESTING SPARQL QUERIES ********
			 */
			// jenaOntologyModel.queryPlayerPresenceRegions(0);
			// jenaOntologyModel.queryPlayerPresenceRegions(1);
			// reset the timer - we just finished reasoning
			if (this.reasonerTimer != null && this.reasonerTimerLabel != null) {
				this.reasonerTimer.restart();
				this.reasonerTimerLabel.setText("0");
			}

			// String regionDataString = "Controlled Regions:\n"
			// +
			// OntologyInfo.getInstance().getControlledRegions().toString().replace(">, <",">\n<")
			// // little hack to make output slightly more readable
			// + "\n\nContested Regions:\n"
			// +
			// OntologyInfo.getInstance().getContestedRegions().toString().replace(">, <",">\n<");
			// this.regionsTextArea.setText(regionDataString);
			this.regionsTextArea.setText(OntologyInfo.getInstance()
					.getRegionData());
			this.unitDataTextArea.setText(OntologyInfo.getInstance()
					.getUnitData());
			//this.myLogger.log(Level.INFO, "Just finished reasoning");
			// System.out.println(jenaOntologyModel.getControlledRegions());
			// sbreak;
		}
	}
}
