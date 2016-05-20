package gda;

/*
 * GDA Main is the main class that runs each GDA component. The current architecture uses singleton
 * classes for the GDA componenets. The following components are singletons
 * 
 * DiscrepancyDetector
 * ExplanationGenerator
 * GoalFormulator
 * GoalManager
 * Planner 
 * PlannerConnection
 * 
 */
//import common.Useful;
//import components.DiscrepancyDetector;
//import components.GoalFormulator;
//import components.GoalManager;
//import gda.planner.Planner;
//import gda.planner.PlannerConnection;
//import gda.planner.PlannerConnection.ServerStatus;
import gda.components.DiscrepancyDetector;
import gda.components.GoalFormulator;
import gda.components.GoalManager;
import gda.planner.Planner;
import gda.planner.GDABotConnection;
import gda.planner.GDABotConnection.ServerStatus;

import java.awt.Color;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;

import ontology.OntologyInfo;
import common.DataDumper;
import common.Useful;

/**
 * This class is the main GDA thread class.
 * 
 * @author dustin
 * 
 */
public class GDAMain {

	private static GDAMain instance;

	private GDAMain() {
		
		
	}
	
	public static synchronized GDAMain getInstace() {
		if (instance == null) {
			instance = new GDAMain();
		}
		return instance;
		
	}
	
	private static Thread plannerThread = null;

	public enum GDAStatus {

		OFF, DiscrepancyDetection, ExplanationGeneration, GoalFormulation, GoalManagement
	};

	private static GDAStatus gdaStatus = GDAStatus.OFF;
	private static JLabel statusLabel = null;

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		init(new JTextArea(), new JTextArea(), new JTextArea(),
				new JTextArea(), new JTextArea(), new JTextArea(),
				new JTextField(), new JTextField(), new JTextArea());
		startServer(new JLabel()); // doesn't matter
		// beginGDACycle(new JLabel());
		// System.exit(0);
		//DataDumper.getInstance();
	}

	/**
	 * Initializes all the system components
	 */
	public static void init(JTextArea serverTextArea,
			JTextArea ontologyTextArea, JTextArea controlledRegionsTextArea,
			JTextArea unitDataTextArea, JTextArea expectationsTextArea,
			JTextArea discrepanciesTextArea, JTextField planStepTextField,
			JTextField lastBotMsgTextField, JTextArea planTraceTextArea) {
		// clear old gamestate files
		Useful.deleteGameStateOutputFiles();


		// initiliaze the discrepancy detector
		DiscrepancyDetector.getInstance().initialize(ontologyTextArea,
				controlledRegionsTextArea, unitDataTextArea,
				expectationsTextArea, discrepanciesTextArea,
				planStepTextField, planTraceTextArea);

	}

	public static void testNewPlanFromGDAPlannerConnection() {
		GDABotConnection.getInstance().newPlanFromGDA = true;
	}

	/**
	 * Continously run through this cycle:
	 * 
	 * 1. Discrepancy Detection 2. Explanation Generation 3. Goal Formulation 4.
	 * Goal Management (selection)
	 * 
	 * If no discrepancies are ever found, the cycle will stay in the first
	 * step, discrepancy detection
	 */
	public static void beginGDACycle(JLabel statusLabel, Timer reasonerTimer,
			JLabel reasonerTimerLabel, Timer dumpFileTimer, JLabel dumpFileLabel) {
		File file = new File(Useful.getPropValue("GAMESTATE_LOG_FILE"));
		if (file.exists()) {

			
			
			statusLabel.setOpaque(true);
			statusLabel.setBackground(Color.gray);
			statusLabel.setForeground(Color.green);
			statusLabel.setText(gdaStatus.name());
			DiscrepancyDetector.getInstance().setReasonerTimer(reasonerTimer);
			DiscrepancyDetector.getInstance().setReasonerTimerLabel(reasonerTimerLabel);
			DiscrepancyDetector.getInstance().setDumpFileTimer(dumpFileTimer);
			DiscrepancyDetector.getInstance().setDumpFileTimerLabel(dumpFileLabel);
			DiscrepancyDetector.getInstance().beginDiscrepancyDetection();

			OntologyInfo.getInstance().update();
			
			gdaStatus = GDAStatus.DiscrepancyDetection;
			statusLabel.setText(gdaStatus.name());
			// try {
			// Thread.sleep(4000);
			// } catch (InterruptedException ex) {
			// Logger.getLogger(GDAMain.class.getName()).log(Level.SEVERE, null,
			// ex);
			// }
			// discrepancyDetector.stopDiscrepancyDetection();
			// System.out.println("Just called stop discrepancy detection");
		}
		DataDumper.getInstance().beginDataCollection();
	}

	/**
	 * Ends all GDA threads that are running
	 */
	public static void stopGDACycle(JLabel statusLabel) {
		DiscrepancyDetector.getInstance().stopDiscrepancyDetection();

		gdaStatus = GDAStatus.OFF;
		statusLabel.setOpaque(true);
		statusLabel.setBackground(Color.gray);
		statusLabel.setForeground(Color.orange);
		statusLabel.setText(gdaStatus.name());
	}

	/**
	 * Start the planner server
	 * 
	 * @param serverStatusLabel
	 *            GUI label for displaying current status
	 */
	public static void startServer(JLabel serverStatusLabel) {
		if (plannerThread == null || !plannerThread.isAlive()) {
			// set the label so it can update once the planner connection
			// is initialized
			GDABotConnection.getInstance().setServerStatusLabel(serverStatusLabel);
			// start up the server
			plannerThread = new Thread(GDABotConnection.getInstance());
			plannerThread.start();

		} else if (plannerThread.isInterrupted()) {
			plannerThread.start();
		}
	}

	public static void stopServer() {
		// if (plannerThread.isAlive()) {
		plannerThread.interrupt();
		// }
	}

	public static ServerStatus getServerStatus() {
		return GDABotConnection.getInstance().getServerStatus();
	}
}
