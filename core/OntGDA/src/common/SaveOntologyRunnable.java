/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package common;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import gda.GlobalInfo;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import ontology.JenaInterface;

/**
 *
 * @author Dustin Dannenhauer
 * @email  dtd212@lehigh.edu
 * @date   Nov 10, 2013
 */
public class SaveOntologyRunnable implements Runnable {
    private OntModel origModel;
    public SaveOntologyRunnable(OntModel om) {
        origModel = om;
    }
    public void run() {
        long beforeTime = Calendar.getInstance().getTimeInMillis();
        long cpuTime = -1;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        String newFileName = Useful.getPropValue("ONTOLOGIES_DIR") + "ontology_" + timeStamp + ".owl";
        // make a copy of the curr model, so that i can save this without affecting another thread
        JOptionPane.showMessageDialog(null, "About to copy model, next step is to write to file");
        OntModel copyOfCurrModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, origModel);
        JOptionPane.showMessageDialog(null, "Finished copying model, next step is to write to file");
        try {
            copyOfCurrModel.write(new FileOutputStream(newFileName));
            cpuTime = (Calendar.getInstance().getTimeInMillis() - beforeTime) / 1000; // converting milliseconds to seconds
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(null, "Exception thrown in writing to file" + ex.getMessage());
            Logger.getLogger(JenaInterface.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Exception thrown in writing to file" + e.getMessage());
        }
        JOptionPane.showMessageDialog(null, "Model saved after " + cpuTime + "s, saved to \n\n\n" + newFileName);
    }
}
