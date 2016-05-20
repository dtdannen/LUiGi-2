package gda.planner;

import gda.GDAMain;
import gda.Goal;
import gda.GoalType;
import gui.TextAreaHandler;
import gui.TimerActionListener;
import gda.GlobalInfo;

import java.awt.Color;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

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
public class GDABotConnection implements Runnable {

	private static GDABotConnection instance;

	private GDABotConnection() {
	}

	public static synchronized GDABotConnection getInstance() {
		if (instance == null) {
			instance = new GDABotConnection();
		}
		return instance;
	}

	// server related variables
	private static ServerSocket serverSocket;
	private static Socket clientSocket;
	private static BufferedReader in; // listens to client
	private static PrintWriter out; // sends to client
	private static String inputLine;
	public static final int port = 64321;
	private static boolean isConnected = false;
	// thread related variables
	private Thread serverThread = null;
	private boolean clientIsConnected = false;
	private boolean listenForNextPlanSteps = false;
	// GDA related variables
	// for the gui
	private static JLabel serverStatusLabel;
	public volatile boolean newPlanFromGDA = false;
	private static JTextField lastBotMsgTextFieldLocal;
	private static JTextArea planTraceTextArea = null;

	public void setServerStatusLabel(JLabel serverStatusLabel) {
		this.serverStatusLabel = serverStatusLabel;
	}

	public static void setPlanTraceTextArea(JTextArea p) {
		planTraceTextArea = p;
	}

	public enum ServerStatus {

		OFF, WAITING, CONNECTED
	};

	private static ServerStatus serverStatus = ServerStatus.OFF;

	public GDABotConnection(JTextArea serverTextArea,
			JTextField lastBotMsgTextField) {
		serverStatus = ServerStatus.OFF;
		lastBotMsgTextFieldLocal = lastBotMsgTextField;

		// logging
		Logger.getLogger(GDABotConnection.class.getName()).addHandler(
				new TextAreaHandler(serverTextArea));
		SimpleFormatter fmt = new SimpleFormatter();
		StreamHandler sh = new StreamHandler(System.out, fmt);
		Logger.getLogger(GDABotConnection.class.getName()).addHandler(sh);
		Logger.getLogger(GDABotConnection.class.getName()).setLevel(Level.ALL);

		Logger root = Logger.getLogger("");
		root.setLevel(Level.FINE);
		for (Handler handler : root.getHandlers()) {
			if (handler instanceof TextAreaHandler) {
				// java.util.logging.ConsoleHandler.level = ALL
				handler.setLevel(Level.FINE);
			}
		}
	}

	/**
	 * ************ PlannerConnection Server Implementation *************
	 */
	public static void initializeConnection() {
		Logger.getLogger(GDABotConnection.class.getName()).log(Level.INFO,
				"Starting up the server");
		// Create the server socket
		try {
			Logger.getLogger(GDABotConnection.class.getName()).log(
					Level.FINE,
					"Trying to establish server socket on port " + port
							+ " ...");

			// System.out.println("Trying to establish server socket on port " +
			// port + " ...");
			serverSocket = new ServerSocket(port);
			Logger.getLogger(GDABotConnection.class.getName()).log(
					Level.FINE,
					"Successfully established server socket on port " + port
							+ " ...");
			// System.out.println("Successfully established server socket on port "
			// + port + " ...");
		} catch (IOException e) {
			Logger.getLogger(GDABotConnection.class.getName()).log(
					Level.SEVERE, "ERROR: Could not listen on port: " + port);
			System.out.println("ERROR: Could not listen on port: " + port);
			System.exit(-1);
		}

		// Accept connection from client socket
		try {
			Logger.getLogger(GDABotConnection.class.getName()).log(Level.INFO,
					"Waiting to accept client via port " + port + " ...");
			System.out.println("Waiting to accept client via port " + port
					+ " ...");

			serverStatus = ServerStatus.WAITING;
			serverStatusLabel.setText(serverStatus.name());
			serverStatusLabel.setOpaque(true);
			serverStatusLabel.setBackground(Color.gray);
			serverStatusLabel.setForeground(Color.orange);

			clientSocket = serverSocket.accept();
			Logger.getLogger(GDABotConnection.class.getName()).log(
					Level.FINE,
					"Successfully accepted client via port " + port + " @ "
							+ new Date().toString());
			System.out.println("Successfully accepted client via port " + port
					+ " @ " + new Date().toString());

			serverStatus = ServerStatus.CONNECTED;
			serverStatusLabel.setText(serverStatus.name());
			serverStatusLabel.setOpaque(true);
			serverStatusLabel.setBackground(Color.gray);
			serverStatusLabel.setForeground(Color.green);

		} catch (IOException e) {
			Logger.getLogger(GDABotConnection.class.getName()).log(
					Level.SEVERE, "ERROR: Accept failed: " + port);
			System.out.println("ERROR: Accept failed: " + port);
			System.exit(-1);
		}

		// now create reader 'in' for listening to client messages
		try {
			in = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));
		} catch (IOException ex) {
			Logger.getLogger(GDABotConnection.class.getName())
					.log(Level.SEVERE,
							"ERROR: Could not create buffered reader from client socket");
			System.out
					.println("ERROR: Could not create buffered reader from client socket");
		}

		// now create printwriter 'out' to send msgs to client
		try {
			out = new PrintWriter(clientSocket.getOutputStream(), true);
		} catch (IOException ex) {
			Logger.getLogger(GDABotConnection.class.getName()).log(
					Level.SEVERE,
					"ERROR: Could not create printwriter from client socket");
			System.out
					.println("ERROR: Could not create printwriter from client socket");
		}

		isConnected = true;
	}

	public static ServerStatus getServerStatus() {
		return serverStatus;
	}

	@Override
	public void run() {
		if (!isConnected) {
			initializeConnection();
		}

		// System.out.println("Connection created, now listening on port " +
		// port);
		int count = 0;
		int planId = 0;
		while (isConnected) {
			// listen for requests for next plan steps
			// System.out.println("listening...");
			try {
				while ((inputLine = in.readLine()) != null) {
					planTraceTextArea.append("\n[Bot -> GDA] " + inputLine);

					ArrayList<Integer> unitIds = new ArrayList<>();

					System.out.println("[CLIENT] " + inputLine);
					if (lastBotMsgTextFieldLocal != null) {
						lastBotMsgTextFieldLocal.setText(inputLine);

					}

					boolean firstRequest = inputLine.contains("msg0");

					if (firstRequest) {
						this.startGDACycle();
						// JOptionPane.showMessageDialog(null, "in msg0");
						// first request of the game

						// CREATE THE STARTING PLANS, AND FOR EACH PLAN
						// SEND A MESSAGE TO THE BOT WITH THE NEXT STEP (WHICH
						// IS THE FIRST STEP)
						Thread.sleep(1000);
						for (GoalType gt : GlobalInfo.getStartingGoals()) {
							Planner.getInstance().addPlan(
									new Goal(gt, GlobalInfo
											.getEnemyStartingRegionID()).getPlan());
						}
						

						// Planner.getInstance().addPlan(
						// new PlanAttackGroundDirect());
						// Planner.getInstance()
						// .addPlan(new PlanAttackAirDirect());
						// ArrayList<Plan> plans = Planner.getInstance()
						// .getPlans();
						Planner.getInstance().updateExpTextArea();
						try {
							// for (Plan p : plans) {
							// sendMessageToBot(Planner.getInstance()
							// .getNextStep(unitIds, p.getID())
							// .toString());
							// }
						} catch (Exception e) {
							JOptionPane.showMessageDialog(null, e.toString()
									+ "\n" + e.getStackTrace().toString());

						}
					} else if (inputLine.equals("Game Over")) {
						this.closeConnection();
						break; // important because closeConnection makes
								// in=null
					} else if (inputLine.contains("UNITS")
							&& inputLine.contains("PLAN-ID")) {
						// The message will always be like: PLAN-ID 7 Request
						// next plan step UNITS

						// parse plan id from bot
						planId = Integer.parseInt(inputLine.split(" ")[1]);

						// JOptionPane.showMessageDialog(null,
						// "Asked for new plan step for plan id " + planId);

						// parse unit ids from bot
						String unitIDStr = inputLine
								.substring(inputLine.indexOf("UNITS"))
								.replace("UNITS", "").trim();
						String[] unitIDs = unitIDStr.split(" ");
						unitIds = new ArrayList<>();
						for (String uID : unitIDs) {
							if (uID.length() > 0) {
								unitIds.add(Integer.parseInt(uID));
							}
						}
					} else {
						// its just a request for a new plan
					}

					if (!firstRequest) {
						while (!OntologyInfo.getInstance().isReady()) {
							Logger.getLogger(GDABotConnection.class.getName())
									.log(Level.FINE,
											"Waiting for game state file dumps");
							// OntologyInfo.getInstance().update();
							Thread.sleep(100);
						}

						// OntologyInfo.getInstance().update();

						// if (planner.isPlanFinished()) {
						// planner.reset();
						// }

						// MAJOR ASSUMPTION:
						// The GDA bot will only ever be given primitive plan
						// steps
						// and therefore will only ever send back primitive plan
						// steps
						// acknowledging that they have finished

						// Mark the plan id of the plan step that just finished
						// as ready to move forward to next plan step
						// JOptionPane.showMessageDialog(null,
						// "About to setReadyToMoveToNextPlanStep() for plan id "
						// + planId);
						
						Plan currentPlan = Planner.getInstance().getPlanById(planId);
						if (currentPlan != null) {
							currentPlan.setReadyToMoveToNextPlanStep(unitIds);
							askPlannerToCheckPlans(planId);
						}
								

						 // NOTE: nextPlanSteps
															// may be empty, if
															// during
															// getNextSteps a
															// post discrepancy
															// occurs

						// if the plan id is of a plan that has a parent plan
						// (aka part of a hierarchical plan)
						// first check the hierarchical plan to see if its ready
						// for all subplans to progress
						// if (Planner.getInstance().getPlanById(planId)
						// .getParentPlan() == null) {
						//
						// PlanStep nextPlanStep = Planner.getInstance()
						// .getNextStep(unitIds, planId);
						// if (nextPlanStep == null) {
						// // just ignore, a plan has finished, but a new
						// // one was added
						// } else {
						//
						// // planner.sendPlanStepToDiscrepancyDetector();
						// sendMessageToBot(nextPlanStep.toString());
						// }
						// } else {
						// // simply mark this plan as being ready to move to
						// // next plan step
						// // and check the top most plan to see if all plan
						// // steps are ready to move forward
						// Planner.getInstance().getPlanById(planId)
						// .setReadyToMoveToNextPlanStep(unitIds);
						// Plan topParentPlan = Planner.getInstance()
						// .getPlanById(planId).getParentPlan();
						// while (topParentPlan.getParentPlan() != null) {
						// topParentPlan = topParentPlan.getParentPlan();
						// }
						//
						// if (topParentPlan.isReadyToMoveToNextPlanStep()) {
						// // now move every primitive plan of that parent
						// // plan to next plan step
						// // TODO - move this code into the plan class?
						// for (Plan primPlan : topParentPlan
						// .getPrimitivePlans()) {
						// PlanStep nextPlanStep = Planner
						// .getInstance()
						// .getNextStep(
						// primPlan.getNextStepUnitIds(),
						// primPlan.getID());
						// if (nextPlanStep == null) {
						// // just ignore, a plan has finished, but
						// // a new
						// // one was added
						// } else {
						//
						// // planner.sendPlanStepToDiscrepancyDetector();
						// sendMessageToBot(nextPlanStep
						// .toString());
						// }
						// }
						// }
						// }
					}
				}

			} catch (SocketException ex) {
				Logger.getLogger(GDABotConnection.class.getName()).log(
						Level.FINE,
						"Client disconnected" + " @ " + new Date().toString());
				System.out.println("Client disconnected" + " @ "
						+ new Date().toString());
				this.closeConnection();
			} catch (IOException ex) {
				Logger.getLogger(GDABotConnection.class.getName()).log(
						Level.SEVERE, ex.toString());
				System.out.println(ex.toString());
			} catch (InterruptedException ex) {
				Logger.getLogger(GDABotConnection.class.getName()).log(
						Level.SEVERE, ex.toString());
			}
			// // if connection has crashed or game has ended, run again
			// if (!isConnected) {
			// planner.reset(); // very important if planning to accept
			// // new connections
			// run();
			// }
		}
	}

	/**
	 * If given a valid plan Id will only check that plan for updates
	 * otherwise if plan id is less than 0, it will check all current plans
	 * TODO overloading planId which is a terrible temporary hack - should create a separate method
	 * for update all plans
	 * @param planId
	 */
	public void askPlannerToCheckPlans(int planId) {
		ArrayList<PlanStep> nextPlanSteps = new ArrayList<PlanStep>();
		nextPlanSteps = Planner.getInstance().getNextSteps(planId);
		sendMessagesToBot(nextPlanSteps);	
	}
	
	private void startGDACycle() {

		gui.GDA_Server_GUI.isStopped = false;
		// set up timer that records time since last reasoning process finished
		TimerActionListener reasonerTimerLabelUpdater = new TimerActionListener();
		reasonerTimerLabelUpdater
				.setLabel(gui.GDA_Server_GUI.reasonerTimerLabel);
		gui.GDA_Server_GUI.reasonerTimer = new Timer(1000,
				reasonerTimerLabelUpdater); // 1000 delay is 1 second

		// set up timer that records time since last gamestate file was read
		TimerActionListener dumpFileTimerLabelUpdater = new TimerActionListener();
		dumpFileTimerLabelUpdater
				.setLabel(gui.GDA_Server_GUI.dumpFileTimerLabel);
		gui.GDA_Server_GUI.dumpFileTimer = new Timer(1000,
				dumpFileTimerLabelUpdater);

		GDAMain.beginGDACycle(gui.GDA_Server_GUI.gdaStatusLabel,
				gui.GDA_Server_GUI.reasonerTimer,
				gui.GDA_Server_GUI.reasonerTimerLabel,
				gui.GDA_Server_GUI.dumpFileTimer,
				gui.GDA_Server_GUI.dumpFileTimerLabel);

	}

	/**
	 * Given an array list of plan steps, calls toString() on each plan step and
	 * sends each to the bot in a separate message
	 * 
	 * @param msg
	 */
	public static void sendMessagesToBot(ArrayList<PlanStep> steps) {
		if (isConnected && out != null) {
			for (PlanStep p : steps) {
				if (!p.queryHasBeenSentToBot() && !p.getPlan().isFinished()) {
					String msg = "[NEXT PLAN STEP] " + p.toString();
					msg += "*";
					msg = msg.replace('*', '\0'); // hack for c++
					out.print(msg);
					out.flush();
					planTraceTextArea.append("\n[GDA -> Bot] " + msg);
					p.notifySentToBot();
				}
			}
		}
	}

	/**
	 * Given the id of the plan to abort, send a message to the bot of with an
	 * ABORT-PLAN message
	 * 
	 * @param planId
	 */
	public static void sendAbortMessageToBot(int planId) {
		if (isConnected && out != null && 
			Planner.getInstance().getPlanById(planId) != null && 
			!Planner.getInstance().getPlanById(planId).isFinished()) {
			
			String msg = "[NEXT PLAN STEP] ABORT-PLAN " + planId;
			msg += "*";
			msg = msg.replace('*', '\0'); // hack for c++
			out.print(msg);
			out.flush();
			planTraceTextArea.append("\n[GDA -> Bot] " + msg);
		}
	}

	/**
	 * Closes the sockets, ends the connection.
	 */
	public void closeConnection() {
		isConnected = false;
		// refresh sockets, start over and listen again
		try {
			serverSocket.close();
			clientSocket.close();
			serverSocket = null;
			clientSocket = null;
			in.close();
			out.close();
			in = null;
			out = null;
		} catch (IOException ex1) {
			System.out.println(ex1.getMessage());
		}
	}

	public void beginListeningForNewPlanSteps() {
		this.listenForNextPlanSteps = true;
	}

	public void stopListeningForNewPlanSteps() {
		this.listenForNextPlanSteps = false;
	}
	/**
	 * ************ Planning steps and goals Implementation *************
	 */
}

/*
 * public class ServerTest { public static void main(String[] args) { // Wait
 * for client to connect on 63400 try { serverSocket = new ServerSocket(63400);
 * clientSocket = serverSocket.accept(); // Create a reader bufferedReader = new
 * BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // Get
 * the client message while((inputLine = bufferedReader.readLine()) != null)
 * System.out.println(inputLine); } catch(IOException e) {
 * System.out.println(e); } } }
 */