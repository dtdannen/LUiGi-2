package common;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import gda.GlobalInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import ontology.JenaInterface;
import ontology.OntologyInfo;

/**
 * Just some bonus useful functions - run this file to get useful output.
 *
 * @author Dustin Dannenhauer
 * @email dtd212@lehigh.edu
 * @date Aug 4, 2013
 */
public class Useful {

    public static void main(String[] args) {
        printCurrDirectory();
    }

    // prints the current directory of this project
    public static void printCurrDirectory() {
        String current = null;
        try {
            current = new java.io.File(".").getCanonicalPath();
        } catch (IOException ex) {
            Logger.getLogger(OntologyInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Current dir:" + current);
        String currentDir = System.getProperty("user.dir");
        System.out.println("Current dir using System:" + currentDir);
    }

    /**
     * Returns the value of this key in the global settings file
     * (settings.properties) and does so in a way that the file is closed after
     * reading - this is to ensure there is no lock between the c++ and java
     * program which both use the settings file.
     *
     * @param key
     * @return
     */
    public static String getPropValue(String key) {
        String result = "";
        // load the settings file to get where the dump files are
        Properties configFile = new Properties();
        String configFileName = ProjectSettings.SETTINGS_FILE_NAME;

        try {
            InputStream inputStream = new FileInputStream(configFileName);
            configFile.load(inputStream);
            result = configFile.getProperty(key);
            inputStream.close();
        } catch (IOException ex) {
            Logger.getLogger(OntologyInfo.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    public static void deleteGameStateOutputFiles() {
        //String command = "del "+ getPropValue("GAMESTATE_DIR").replace("/", "\\") + "*";
        File gameStateDir = new File(getPropValue("GAMESTATE_DIR"));
        for (File fileEntry : gameStateDir.listFiles()) {
        	// don't delete any version control files (specifically
        	// there is a .gitignore file in this directory
        	// that must not be deleted
        	if (fileEntry.getName().equals(".gitignore" )) {
        		//JOptionPane.showMessageDialog(null, "(2) Not deleting file: "+fileEntry.getName());
        		// do nothing
        	}else {
        		fileEntry.delete();
        	}
        }
//        
//        try {
//            Runtime.getRuntime().exec(command);
//        } catch (IOException ex) {
//            // if exception was caused then no worries, just means files were not there
//            
//            JOptionPane.showMessageDialog(null, "Exception caused from running command:\n"+command);
//            Logger.getLogger(Useful.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    /**
     * Pass in the model to save, this will make a copy of it, start the saving process in
     * a separate thread, and return
     */
    public static void saveOntModelToFile(OntModel origModel) {
        SaveOntologyRunnable saveOntRunnable = new SaveOntologyRunnable(origModel);
        Thread t = new Thread(saveOntRunnable);
        t.start();
    }
}