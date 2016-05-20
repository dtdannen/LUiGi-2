package gda.components;

import com.hp.hpl.jena.sparql.function.library.e;
import common.Pair;
import common.ProjectSettings;
import gda.GlobalInfo;
import gda.components.Explanation.ExplanationType;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import ontology.OntologyInfo;

/**
 * This class represents an expectation that the discrepancy detector will
 * attempt to use to reason over.
 *
 * @author Dustin Dannenhauer
 * @email dtd212@lehigh.edu
 * @date Aug 7, 2013
 */
public class Expectation {

    private ArrayList<Pair<String, Integer>> unitTypeCounts;
    private ArrayList<Integer> unitIDs; // id's of all units that are covered
    // by this expectation
    private ExpectationName expectationName;
    private int regionID = -1;
    private Explanation explanation = new Explanation(ExplanationType.NO_EXPLANATION_YET);
    private int id = 0;
    private static int baseId = 0;

    public Expectation setUnitIDs(ArrayList<Integer> unitIDs) {
        if (unitIDs == null) { throw new NullPointerException(); }
        this.unitIDs = unitIDs;
        this.id = baseId;
        baseId++;
        return this;
    }

    int getId() {
        return this.id;
    }
    
    int getRegionId() {
        return this.regionID;
    }

    public ExpectationName getExpectationName() {
        return this.expectationName;
    }

//    public enum ExpectationLevel {
//
//        PRIMITIVE, AXIOM
//    };
    public enum ExpectationName {

        // primitive
        HAVE_UNITS,
        HAVE_UNITS_WITH_IDS,
        UNITS_IN_REGION,
        // axiomatic
        PRESENCE_IN_REGION,
        CONTROL_REGION,
        CONTESTED_REGION
    };

    
    //private ExpectationLevel expectationType;

    /**
     * ** variable specific to whatever expectation this is ***
     */
    public Expectation(ExpectationName eN) {
        //this.expectationType = eT;
        this.expectationName = eN;
        this.unitTypeCounts = new ArrayList<>();
        this.unitIDs = new ArrayList<>();
    }

    public Expectation addUnitCountPair(Pair<String, Integer> unitTypeCount) {
        unitTypeCounts.add(unitTypeCount);
        return this;
    }

    public Expectation setRegionId(int startingRegionID) {
        this.regionID = startingRegionID;
        return this;
    }

    /**
     * Based on what type of expectation this is, will return true if the
     * ontology satisfies it. Will actually call methods on the model
     *
     * @return
     */
    public boolean isExpectationMet() {
        boolean expectationMet = true; // true unless proven false

        switch (this.expectationName) {
            case HAVE_UNITS:
                /**
                 * loop through and check that all units are accounted for *
                 */
                // check to make sure we have all the unit type counts
                for (Pair<String, Integer> unitTypeCount : unitTypeCounts) {
                    int expectedCount = unitTypeCount.getSecond();
                    int realCount = OntologyInfo.countUnitsOfTypeOfPlayer(unitTypeCount.getFirst(), ProjectSettings.myPlayerID);
                    if (expectedCount > realCount) {
                        expectationMet = false;
                    }
                }

                if (expectationMet == false) {
                    this.explanation = new Explanation(ExplanationType.MISSING_UNITS);
                }

                break;
            case HAVE_UNITS_WITH_IDS:
                // right now there are no expectations for this case
                break;
            case CONTROL_REGION:
                expectationMet = OntologyInfo.doesPlayerControlRegionSPARQL(ProjectSettings.myPlayerID, regionID);
                //expectationMet = OntologyInfo.doesPlayerControlRegionPELLET(ProjectSettings.myPlayerID, regionID);

                if (expectationMet == false) {
                    // check to see if we have any units in the region
                    if (OntologyInfo.isRegionContested(regionID)) {
                        //JOptionPane.showMessageDialog(null, "Region "+regionID+" is contested!!");
                        this.explanation = new Explanation(ExplanationType.NOT_ALL_FRIENDLY_UNITS_IN_REGION);
                        this.explanation.setUnitIDs(OntologyInfo.getFightingUnitIDsInRegion(GlobalInfo.getEnemyPlayerID(), regionID));
                        if (this.explanation.getUnitIDs().size() > 0) {
                            //JOptionPane.showMessageDialog(null, "There is more than 1 unit!!");
                        }
                    } else if (OntologyInfo.getFightingUnitIDsInRegion(ProjectSettings.myPlayerID,regionID).isEmpty())  {
                        this.explanation = new Explanation(ExplanationType.NO_UNITS_IN_REGION);
                    }else {
                        this.explanation = new Explanation(ExplanationType.UNKNOWN);
                    }
                }

                break;
            case UNITS_IN_REGION:
                // return true if at least one of our units are in the region
                ArrayList<Integer> unitIdsInRegion = OntologyInfo.getUnitIDsInRegion(regionID);
                int count = 0;
                for (Integer uID : this.unitIDs) {
                    if (unitIdsInRegion.contains(uID)) {
                        count++;
                    }
                }

                if (count < 1) {
                    expectationMet = false;
                }

                if (expectationMet == false) {
                    this.explanation = new Explanation(ExplanationType.NO_UNITS_IN_REGION);
                }

                break;
        }
        return expectationMet;
    }

    /**
     * Returns null if no explanation. Assumes that isExpectationMet() was
     * called before calling this method.
     *
     * @return
     */
    public Explanation getExplanation() {
        return this.explanation;
    }

    public String toString() {
        String str = id + ". " + this.expectationName.name();

        if (this.unitTypeCounts != null) {
            str += "\n UnitTypeCounts " + unitTypeCounts.toString();
        }
        
        if (this.regionID != -1) {
            str += "\n RegionID " + this.regionID;
        }

        if (this.unitIDs != null && !this.unitIDs.isEmpty()) {
            str += "\nUnit IDs: " + this.unitIDs.toString();
        }
        
        str += "\nExplanation: "+this.explanation;

        return str;
    }
}
