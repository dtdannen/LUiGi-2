package ontology;

import java.awt.Color;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;

/**
 * This class holds all the static objects that are used to display information
 * about the OntologyInfo object.
 * 
 * @author dustin
 * 
 */
public class OntologyInfoDisplayable {

	public static Logger logger = Logger
			.getLogger(OntologyInfo.class.getName());
	public static Timer lastDumpFileTimer = null;
	public static JLabel lastDumpFileLabel = null;
	public static JTextArea globalInfoTextArea = null;
	public static JLabel ontologyStatusLabel = null;
	public static JTextField lastOntMethodCalledTextField = null;
	public static JTextField mapNameTextField = null;
	
	public static void setLogger(Logger logggger) {
		logger = logggger;
	}

	public static void setLastDumpFileTimer(Timer t) {
		lastDumpFileTimer = t;
	}

	public static void setLastDumpFileLabel(JLabel label) {
		lastDumpFileLabel = label;
	}

	public static synchronized void setGlobalInfoTextArea(JTextArea textArea) {
		globalInfoTextArea = textArea;
	}

	public static void setOntologyStatusLable(JLabel statusLabel) {
		ontologyStatusLabel = statusLabel;
		ontologyStatusLabel.setOpaque(true);
		ontologyStatusLabel.setBackground(Color.gray);
	}

	public static void setLastOntologyMethodCalledTextField(
			JTextField lastOntMethodCalledTxtF) {
		lastOntMethodCalledTextField = lastOntMethodCalledTxtF;
	}
	
	public static void setMapNameTextField(JTextField mapNameTextFieldArg) {
		mapNameTextField = mapNameTextFieldArg;
	}
	
	
}
