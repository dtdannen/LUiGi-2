package gda.planner;

import gui.TextAreaHandler;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import ontology.OntologyInfo;

/**
 *
 * @author Dustin Dannenhauer
 * @email dtd212@lehigh.edu
 */
public class PlannerConnection implements Runnable {

    // server related variables
    private static ServerSocket serverSocket;
    private static Socket clientSocket;
    private static BufferedReader in; // listens to client
    private static PrintWriter out; // sends to client
    private static String inputLine;
    private static final int port = 64321;
    private static boolean isConnected = false;
    // thread related variables
    private Thread serverThread = null;
    private boolean clientIsConnected = false;
    private boolean listenForNextPlanSteps = false;
    // GDA related variables
    private static Planner planner;
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

    public PlannerConnection(Planner planner, JTextArea serverTextArea, JTextField lastBotMsgTextField) {
        this.planner = planner;
        serverStatus = ServerStatus.OFF;
        lastBotMsgTextFieldLocal = lastBotMsgTextField;

        // logging
        Logger.getLogger(PlannerConnection.class.getName()).addHandler(new TextAreaHandler(serverTextArea));
        SimpleFormatter fmt = new SimpleFormatter();
        StreamHandler sh = new StreamHandler(System.out, fmt);
        Logger.getLogger(PlannerConnection.class.getName()).addHandler(sh);
        Logger.getLogger(PlannerConnection.class.getName()).setLevel(Level.ALL);

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
        Logger.getLogger(PlannerConnection.class.getName()).log(Level.INFO, "Starting up the server");
        // Create the server socket
        try {
            Logger.getLogger(PlannerConnection.class.getName()).log(Level.FINE, "Trying to establish server socket on port " + port + " ...");

            //System.out.println("Trying to establish server socket on port " + port + " ...");
            serverSocket = new ServerSocket(port);
            Logger.getLogger(PlannerConnection.class.getName()).log(Level.FINE, "Successfully established server socket on port " + port + " ...");
            //System.out.println("Successfully established server socket on port " + port + " ...");
        } catch (IOException e) {
            Logger.getLogger(PlannerConnection.class.getName()).log(Level.SEVERE, "ERROR: Could not listen on port: " + port);
            System.out.println("ERROR: Could not listen on port: " + port);
            System.exit(-1);
        }

        // Accept connection from client socket
        try {
            Logger.getLogger(PlannerConnection.class.getName()).log(Level.INFO, "Waiting to accept client via port " + port + " ...");
            System.out.println("Waiting to accept client via port " + port + " ...");

            serverStatus = ServerStatus.WAITING;
            serverStatusLabel.setText(serverStatus.name());
            serverStatusLabel.setOpaque(true);
            serverStatusLabel.setBackground(Color.gray);
            serverStatusLabel.setForeground(Color.orange);

            clientSocket = serverSocket.accept();
            Logger.getLogger(PlannerConnection.class.getName()).log(Level.FINE, "Successfully accepted client via port " + port + " @ " + new Date().toString());
            System.out.println("Successfully accepted client via port " + port + " @ " + new Date().toString());

            serverStatus = ServerStatus.CONNECTED;
            serverStatusLabel.setText(serverStatus.name());
            serverStatusLabel.setOpaque(true);
            serverStatusLabel.setBackground(Color.gray);
            serverStatusLabel.setForeground(Color.green);

        } catch (IOException e) {
            Logger.getLogger(PlannerConnection.class.getName()).log(Level.SEVERE, "ERROR: Accept failed: " + port);
            System.out.println("ERROR: Accept failed: " + port);
            System.exit(-1);
        }

        // now create reader 'in' for listening to client messages
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException ex) {
            Logger.getLogger(PlannerConnection.class.getName()).log(Level.SEVERE, "ERROR: Could not create buffered reader from client socket");
            System.out.println("ERROR: Could not create buffered reader from client socket");
        }

        // now create printwriter 'out' to send msgs to client
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException ex) {
            Logger.getLogger(PlannerConnection.class.getName()).log(Level.SEVERE, "ERROR: Could not create printwriter from client socket");
            System.out.println("ERROR: Could not create printwriter from client socket");
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

        //System.out.println("Connection created, now listening on port " + port);
        int count = 0;
        while (isConnected) {
            // listen for requests for next plan steps
            //System.out.println("listening...");
            try {
                while ((inputLine = in.readLine()) != null) {
                    planTraceTextArea.append("\n[Bot -> GDA] "+inputLine);
                    
                    
                    System.out.println("[CLIENT] " + inputLine);
                    if (lastBotMsgTextFieldLocal != null) {
                        lastBotMsgTextFieldLocal.setText(inputLine);
                        
                    }
                    ArrayList<Integer> unitIds = new ArrayList<>();
                    if (inputLine.equals("Game Over")) {
                        this.closeConnection();
                        break; // important because closeConnection makes in=null
                    } else if (inputLine.contains("UNITS")) {
                        // parse unit ids from bot
                        String unitIDStr = inputLine.substring(inputLine.indexOf("UNITS")).replace("UNITS", "").trim();
                        String[] unitIDs = unitIDStr.split(" ");
                        unitIds = new ArrayList<>();
                        for (String uID : unitIDs) {
                            unitIds.add(Integer.parseInt(uID));
                        }
                    }

                    while (!OntologyInfo.isReady()) {
                        Logger.getLogger(PlannerConnection.class.getName()).log(Level.FINE, "Waiting for game state file dumps");
                        //OntologyInfo.update();
                        Thread.sleep(100);
                    }

                    //OntologyInfo.update();

//                    if (planner.isPlanFinished()) {
//                        planner.reset();
//                    }

                    String nextStepString = planner.getNextStep(unitIds).toString();
                    //planner.sendPlanStepToDiscrepancyDetector();
                    sendMessageToBot(nextStepString);
                }

            } catch (SocketException ex) {
                Logger.getLogger(PlannerConnection.class.getName()).log(Level.FINE, "Client disconnected" + " @ " + new Date().toString());
                System.out.println("Client disconnected" + " @ " + new Date().toString());
                this.closeConnection();
            } catch (IOException ex) {
                Logger.getLogger(PlannerConnection.class.getName()).log(Level.SEVERE, ex.toString());
                System.out.println(ex.toString());
            } catch (InterruptedException ex) {
                Logger.getLogger(PlannerConnection.class.getName()).log(Level.SEVERE, ex.toString());
            }
//            // if connection has crashed or game has ended, run again
//            if (!isConnected) {
//                planner.reset(); // very important if planning to accept 
//                // new connections
//                run();
//            }
        }
    }

    public static void sendMessageToBot(String msg) {
        if (isConnected && out != null) {
            msg = "[NEXT PLAN STEP] " + msg;
            msg += "*";
            msg = msg.replace('*', '\0'); // hack for c++
            out.print(msg);
            out.flush();
            planTraceTextArea.append("\n[GDA -> Bot] "+msg);
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
public class ServerTest
{
	public static void main(String[] args)
	{
		// Wait for client to connect on 63400
		try
		{
			serverSocket = new ServerSocket(63400);
			clientSocket = serverSocket.accept();
			// Create a reader
			bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			// Get the client message
			while((inputLine = bufferedReader.readLine()) != null)
			System.out.println(inputLine);
		}
		catch(IOException e)
		{
			System.out.println(e);
		}
	}
}

*/