package gda.components;

import gda.GlobalInfo;
import gda.components.Explanation.ExplanationType;
import gda.planner.Plan;
import gda.planner.PlanStep;
import javax.swing.JOptionPane;

/**
 * For now this class will just maintain what to do given a 
 * 1. plan step
 * 2. discrepancy
 * 3. explanation
 * 
 * TODO - actually use case based reasoning
 * 
 * @author Dustin Dannenhauer
 * @email  dtd212@lehigh.edu
 * @date   Aug 22, 2013
 */
public class ExplanationCaseBase {
    static boolean justwentforairgoal = false;
    /**
     * Returns true if a new goal should be chosen, false if ignored
     * 
     * @param gf
     * @param plan
     * @param planStep
     * @param ex
     * @param explanation
     * @return 
     */
    public static boolean chooseNextGoal(GoalFormulator gf, Plan plan, PlanStep planStep, Expectation ex, Explanation explanation) {
        
        // this most likely happens at the beginning of the game when an enemy probe is in our base
        if (ex.getExpectationName() == Expectation.ExpectationName.CONTROL_REGION && explanation.getType() == ExplanationType.NOT_ALL_FRIENDLY_UNITS_IN_REGION && ex.getRegionId() == GlobalInfo.getStartingRegionID()) {
            if (explanation.getUnitIDs().size() < 2) {
                // just ignore, TODO need to make sure they are protoss probes, not just any unit
                //JOptionPane.showMessageDialog(null, "less than two units, not going to choose new goal");
                return false;
            }
            gf.goForRushDefendGoal(ex.getRegionId());
            return true;
        }else /*if (ex.getExpectationName() == Expectation.ExpectationName.UNITS_IN_REGION && explanation.getType() == ExplanationType.NOT_ALL_FRIENDLY_UNITS_IN_REGION)*/{
            if (justwentforairgoal) {
            gf.goForDirectGroundGoal();
            justwentforairgoal = false;
            }else{
            gf.goForAirGoal();
            justwentforairgoal = true;
            }
            
            return true;
        }
        
    }
    
}
