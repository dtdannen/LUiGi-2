package gda;

import java.util.ArrayList;
import ontology.OntologyInfo;

/**
 * This class represents global information that the GDA system knows or learns 
 * over time.
 * 
 * @author Dustin Dannenhauer
 * @email  dtd212@lehigh.edu
 * @date   Aug 7, 2013
 */
public class GlobalInfo {

    private static int startingRegionID = -1;
    private static int enemyStartingRegionID = -1;
    private static boolean isReady = false;
    
    public static boolean saveNextOntologyToFile = false;
    public static String lastOntologySavedToFileName = "../Ontologies/";
    
    public static final GoalType STARTING_GOAL = GoalType.ATTACK_GROUND_SURROUND;
    
    public static void setStartingRegionID(int r) {
        startingRegionID = r;
        // if our starting region is 2, enemy is 4 and vice versa
        enemyStartingRegionID = r == 2 ? 4 : 2;
        isReady = true;
    }
    
    public static int getStartingRegionID() {
        return startingRegionID;
    }
    
    public static void setEnemyStartingRegionID(int r) {
        enemyStartingRegionID = r;
    }
    
    public static int getEnemyStartingRegionID() {
        return enemyStartingRegionID;
    }

    public static boolean isReady() {
        return isReady;
    }
    
    /**
     * Returns a list of the unit id's of all my fighting units. It gets this
     * information from the ontology.
     * @return 
     */
    public static ArrayList<Integer> getAllFightingUnits() {
        return OntologyInfo.getAllFightingUnitIds();
    }

    public static int getEnemyPlayerID() {
        return 1;
    }
    
    public static int getOurPlayerID() {
        return 0;
    }
    
    
}
