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
    boolean moveToNextPlanSteps(); // returns false if plan is finished
    ArrayList<PlanStep> getCurrentPlanSteps(); // returns the next plan steps
    boolean isPlanOnLastStep();
        
    void reset(); // used when a plan ends successfully, simply repeat for now
    
    void setID(int ID); // The planner is responsible for giving every plan a unique ID
    int getID(); // returns the ID
    
    // only used in hierarchical plans
    void setReadyToMoveToNextPlanStep(ArrayList<Integer> unitIds); // needs the unitIds from the time when the plan is ready to move forward
    boolean isReadyToMoveToNextPlanStep();
    Plan getParentPlan();
    void setParentPlan(Plan p);
    ArrayList<Plan> getPrimitivePlans(); // returns all the primitive plans for this plan (all leaf nodes/plans)
    ArrayList<Integer> getNextStepUnitIds();
    
    
    // used to determine if a plan has any subplans (aka is hierarchical)
    boolean isPrimitive();
    
    
    void setUnitIds(ArrayList<Integer> unitIds); // only sets id's, does not signfi the plan is to move to the next step
                                                    // this was originally implemented for when a hierarchical plan wants to transfer
                                                     // unit id's from one or more finished sub plans to 1 more next-in-line subplans
    
    boolean isFinished();
    void setFinished();
    
    GoalType getGoalType();
	void removeAbortedSubPlan(Plan failedPlan);
}
