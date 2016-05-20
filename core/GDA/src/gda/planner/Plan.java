package gda.planner;

import common.Pair;
import gda.GoalType;
import gda.GDAMain;
import gda.GlobalInfo;
import gda.components.Expectation;
import java.awt.Point;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import ontology.OntologyInfo;

/**
 * A plan aims to achieve a possible goal, and maintains an ordered list of
 * steps that must be taken to achieve the goal. Each step also has its own
 * discrepancies.
 *
 * @author Dustin Dannenhauer
 * @email dtd212@lehigh.edu
 */
public interface Plan {
    boolean moveToNextPlanStep(ArrayList<Integer> unitIds);
    PlanStep getCurrentPlanStep();
    boolean isPlanOnLastStep();
    int getID(); // ID's are only unique for plans of the same type
                 // (AttackGroundDirect will never have the same ID as a
                 // different AttackGroundDirect plan but MAY have the 
                 // same ID as an AttackAirDirect plan)
    
    void reset(); // used when a plan ends successfully, simply repeat for now
}
