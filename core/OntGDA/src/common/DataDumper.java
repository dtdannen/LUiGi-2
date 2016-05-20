package common;

import gda.planner.GDABotConnection;
import gda.planner.Plan;
import gda.planner.Planner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;

import javax.swing.JOptionPane;

import ontology.OntologyInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


/**
 * This class is a singleton that is used to dump data as the system plays the game
 * 
 * @author dustin
 *
 */
public class DataDumper {

	private static DataDumper instance;
	private static SimpleDateFormat folderNameFormat;
	private static Date todayDate;
	private static String folderName = "";
	private static String dataDumpFilename;
	private static String dataDirString = "";
	private Timer timer;
	public boolean stopDataCollectionFlag = false;
	
	private DataDumper() {
		
		try {
			folderNameFormat =  new SimpleDateFormat("EEE-MMM-dd"); //"EEE-MMM-dd"
			folderName = folderNameFormat.format(new Date()).toString();
			
			// get curr path of data directory
			File dataDir = new File(getCurrPath()).getParentFile().getParentFile().getParentFile();
			dataDirString = dataDir.toString()+"\\data";
			//JOptionPane.showMessageDialog(null, "Data directory is :\n"+dataDirString);
			
			folderName = dataDirString + "\\" + folderName;
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(null, e.toString());
		}
		
		// check to see if there is a data folder for the day the experiments ran
		File folder = new File(folderName); 
		if (!folder.exists()) {
			//FileUtils.forceMkdir(folder)
			if (!folder.mkdir()) {
				//JOptionPane.showMessageDialog(null, "Failed to make new folder: "+ folderName);
			}else{
				//JOptionPane.showMessageDialog(null, "Just created folder:\n"+folder.getAbsolutePath());
			}
		}else{
			//JOptionPane.showMessageDialog(null, "Folder already exists:\n"+folder.getAbsolutePath());
		}
	}

	public static synchronized DataDumper getInstance() {
		if (instance == null) {
			instance = new DataDumper();
		}
		return instance;
	}
	
	private static String getCurrPath() {
		URL location = DataDumper.class.getProtectionDomain().getCodeSource().getLocation();
        return location.getFile().toString();
	}
	
	/*
	 * This will get the latest information from every component of the system and write it to a file.
	 */
	private void writeCurrentData(int secondsPassed, PrintWriter writer) {
		String jsonDumpStr = "{\"secondsPassed\": " + secondsPassed + ", "
		+ "\"player_" + GDABotConnection.port + "_kill_score\":" + OntologyInfo.getInstance().getKillScore() + ", "
		+ "\"player_" + GDABotConnection.port + "_razing_score\":" + OntologyInfo.getInstance().getRazingScore() + ", "
		+ "\"player_" + GDABotConnection.port + "_unit_score\":" + OntologyInfo.getInstance().getUnitScore()  + ", "
		+ "\"player_" + GDABotConnection.port + "_spent_minerals\":" + OntologyInfo.getInstance().getSpentMinerals()  + ", "
		+ "\"player_" + GDABotConnection.port + "_spent_gas\":" + OntologyInfo.getInstance().getSpentGas()  + ", "
		+ "\"player_" + GDABotConnection.port + "_supply_total\":" + OntologyInfo.getInstance().getSupplyTotal()  + ", "
		+ "\"player_" + GDABotConnection.port + "_plans\": [";
		
		// now add plans for this player
		
		ArrayList<Plan> currPlans = Planner.getInstance().getPlans(); 
		for (Plan p : currPlans) {
			jsonDumpStr += "\"" + p.toString() + "_" + p.getID() + "\", ";
		}
		
		if (!currPlans.isEmpty()) {
			jsonDumpStr = jsonDumpStr.substring(0, jsonDumpStr.length()-2); // trim the last comma
		}
		
		jsonDumpStr += "]}";
		//jsonDumpStr += "\"score\": " + OntologyInfo.getInstance().getScore();
		//return jsonDumpStr;
		//JOptionPane.showMessageDialog(null, "About to write:\n\n"+jsonDumpStr);
		writer.println(jsonDumpStr);
		writer.flush();
	}
	
	
	/**
	 * This will start a new thread that will continuously dump data every second
	 */
	public void beginDataCollection() {
		// 1. create new file that will contain the data in a csv format
		SimpleDateFormat fileNameFormat =  new SimpleDateFormat("kk-mm-ss"); //"EEE-MMM-dd"
		String fileName = folderName + "\\" + fileNameFormat.format(new Date()).toString() + ".dat";
		//JOptionPane.showMessageDialog(null, fileName);
		try {
			final PrintWriter writer = new PrintWriter(fileName);
			// 2. Start a time that runs every second and calls getCurrentData each second
			 timer = new Timer();
			
			// local class for fixed rate execution
			class DumpData extends TimerTask {
				private int secondsPassedCollectingData = 0;
				public void run() {
					DataDumper.getInstance().writeCurrentData(secondsPassedCollectingData, writer);
					secondsPassedCollectingData++;
					//JOptionPane.showMessageDialog(null, "run() of DataDumper TimerTask");
					if (DataDumper.getInstance().stopDataCollectionFlag) {
						timer.cancel();
						writer.close();
					}
				}
			}
			
			timer.scheduleAtFixedRate(new DumpData(), new Date(), 1000);
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
	public void stopDataCollection() {
		this.stopDataCollectionFlag = true;
	}
	
	
}
