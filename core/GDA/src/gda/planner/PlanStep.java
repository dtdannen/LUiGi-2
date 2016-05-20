package gda.planner;

import common.Pair;
import gda.GoalType;
import gda.GDAMain;
import gda.GlobalInfo;
import gda.components.Expectation;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import ontology.OntologyInfo;

/**
 * Represents an individual step of a plan with methods for returning a string
 * to send to the C++ Bot as well as corresponding discrepancies.
 *
 * @author Dustin Dannenhauer
 * @email dtd212@lehigh.edu
 */
public class PlanStep {

    private Plan plan;
    private PlanStepType planStepType;
    private GoalType goalType;
    private Point destination; // used in SEND_TROOP steps
    private ArrayList<Point> routeToDestination; // used in SEND_TROOP steps
    // for INDIRECT goals
    // specifies the number of each type of troop to acquire
    private HashMap<String, Integer> unitTypeCounts; // used for ACQUIRE_TROOPS
    private ArrayList<Expectation> preExpectations;
    private ArrayList<Expectation> postExpectations;
    
    private ArrayList<Integer> unitIDs;
    private boolean discrepancyHasBeenDetected;

    private int destRegionId = -1; // different representation of destination
    
    public PlanStep(PlanStepType pst, GoalType gt, ArrayList<Expectation> preExpectations, ArrayList<Expectation> postExpectations, Plan plan) {
        this.plan = plan;
        this.planStepType = pst;
        this.goalType = gt;

        this.preExpectations = preExpectations;
        this.postExpectations = postExpectations;

        // initiliaze values depending on what type of step this is
        switch (pst) {
            case PRODUCE_UNITS:
                this.unitTypeCounts = new HashMap<String, Integer>();
                this.unitIDs = new ArrayList<>();
                break;
            case MOVE_UNITS:
                // TODO - perhaps better initialization code
                this.destination = null;
                this.routeToDestination = null;
                this.unitIDs = new ArrayList<>();
                break;
            case ATTACK:
            case DEFEND:
                this.unitIDs = new ArrayList<>();
                break;
        }   
    }

    public Plan getPlan() {
        return this.plan;
    }
    
    public PlanStepType getPlanStepType() {
        return this.planStepType;
    }
    
    public void setUnitIDs(ArrayList<Integer> unitIDs) {
        this.unitIDs = unitIDs;
    }
    
    public ArrayList<Integer> getUnitIDs() {
        return this.unitIDs;
    }

    /**
     * Set the destination of this plan step - only makes sense to do this for
     * steps that have a planStepType of MOVE_UNITS
     *
     * @param dest
     */
    public void setDestination(Point dest) {
        this.destination = dest;
        if (this.planStepType != PlanStepType.MOVE_UNITS) {
            Logger.getLogger(GDAMain.class.getName()).log(Level.FINE,
                    "Setting destination on plan step that is NOT "
                    + "of planStepType SendTroops");
        }
    }

    /**
     * Sets the route of this plan step - only makes sense for plan steps that
     * have a planStepType of MOVE_UNITS and a goalType of SURROUND
     *
     * @param route
     */
    public void setRoute(ArrayList<Point> route) {
        this.routeToDestination = route;
        if (this.goalType != GoalType.ATTACK_AIR_SURROUND
                || this.goalType != GoalType.ATTACK_BOTH_SURROUND
                || this.goalType != GoalType.ATTACK_GROUND_SURROUND) {
            Logger.getLogger(GDAMain.class.getName()).log(Level.FINE,
                    "Setting route on plan step that is NOT "
                    + "of goalType SURROUND");
        }
    }

    public GoalType getGoalType() {
        return this.goalType;
    }
    
    /**
     * Adds a count for the unit type for ACQURIE_UNIT plan steps. If the unit
     * type already exists, this method updates it.
     *
     * @param unitType of the desired starcraft unit to acquire
     * @param count number of units to acquire
     */
    public void addUnitTypeCount(String unitType, int count) {
        if (this.planStepType == PlanStepType.PRODUCE_UNITS) {
            this.unitTypeCounts.put(unitType, count);
        }
    }

    public void addUnitTypeCount(Pair<String, Integer> pair) {
        if (this.planStepType == PlanStepType.PRODUCE_UNITS) {
            this.unitTypeCounts.put(pair.getFirst(), pair.getSecond());
        }
    }

    public ArrayList<Expectation> getPreExpectations() {
        return this.preExpectations;
    }
    
    public ArrayList<Expectation> getPostExpectations() {
        return this.postExpectations;
    }
    
    /**
     * Returns a specifically formatted string that will be sent to the C++ bot,
     * which will then execute the step.
     *
     * @return
     */
    public String toString() {
        String result = this.planStepType.name() + " ";

        // add extra information depending on which plan step this is
        switch (this.planStepType) {
            case PRODUCE_UNITS:
                for (String unitType : unitTypeCounts.keySet()) {
                    result += unitType + " " + unitTypeCounts.get(unitType) + " ";
                }
                break;
            case ACQUIRE_UNITS:
                for (Integer uID : this.unitIDs) {
                    result += uID + " ";
                }
                break;
            case MOVE_UNITS:
            case ATTACK_MOVE_UNITS:
                // check destination
                if (this.destRegionId != -1) {
                    result += "DEST-REGION-ID " + this.destRegionId;
                }else if (this.destination == null) {
                    System.err.println("dest is null");
                } else {
                    result += " DEST X " + this.destination.x
                            + " Y " + this.destination.y; // send destination
                }

                // check route
                if (this.routeToDestination == null) {
                    System.err.println("route is null");
                } else {
                    result += "ROUTE ";
                    for (Point p : routeToDestination) { // send all points in route
                        result += p.toString() + " ";
                    }
                }
                break;
            case MOVE_UNITS_SURROUND:
            case ATTACK_MOVE_UNITS_SURROUND:
                // check destination
                if (this.destination == null) {
                    System.err.println("dest is null");
                } else {
                    // find all surrounding regions
                    ArrayList<String> p = OntologyInfo.getAdjacentRegions(destRegionId);
                    Point tmpDest;
                    for (String s : p) {
                        // s will be of the form "region 3", so we do a little parsing to get just "3" and turn that into an integer
                        tmpDest = OntologyInfo.getRegionCenterPosition(Integer.parseInt(s.replaceAll("region", "").trim()));
                        result += " DEST X " + tmpDest.x
                            + " Y " + tmpDest.y; 
                    }
                    
                    //JOptionPane.showMessageDialog(null, x);
                    
                }
                break;
            case ATTACK:
                // check destination
                if (this.destination == null) {
                    System.err.println("dest is null");
                } else {
                    result += " DEST X " + this.destination.x
                            + " Y " + this.destination.y; // send destination
                }
            case DEFEND:
//                if (this.destination == null) {
//                    System.err.println("dest is null");
//                }
//                result += " DEST " + this.destination;
                break;
        }
        return result;
    }

    public void discrepancyHasBeenDetected() {
        this.discrepancyHasBeenDetected = true;
    }
    
    public boolean hasDiscrepancyBeenDetected() {
        return this.discrepancyHasBeenDetected;
    }

    void setDestRegionId(int regionIdToDefend) {
        this.destRegionId = regionIdToDefend;
    }
}
