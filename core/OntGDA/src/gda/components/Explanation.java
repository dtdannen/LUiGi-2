package gda.components;

import java.util.ArrayList;

/**
 * This class represents an explanation for an violated expectation.
 *
 * @author Dustin Dannenhauer
 * @email dtd212@lehigh.edu
 * @date Sep 2, 2013
 */
public class Explanation {

    public enum ExplanationType {

        NO_UNITS_IN_REGION,
        NOT_ALL_FRIENDLY_UNITS_IN_REGION,
        MISSING_UNITS,
        UNKNOWN,
        NO_EXPLANATION_YET,
        SUCCESSFUL};
    private ExplanationType explanationType;
    private ArrayList<Integer> unitIDs;

    public Explanation(ExplanationType t) {
        this.explanationType = t;
    }

    public Explanation(ExplanationType t, ArrayList<Integer> unitIDs) {
        this.explanationType = t;
        this.unitIDs = unitIDs;
    }

    public void setUnitIDs(ArrayList<Integer> unitIDs) {
        this.unitIDs = unitIDs;
    }

    public ExplanationType getType() {
        return this.explanationType;
    }
    
    public ArrayList<Integer> getUnitIDs() {
        return this.unitIDs;
    }
    
    public String toString() {
        return this.explanationType.name();
    }
}
