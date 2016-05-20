package gda;

import java.awt.Point;
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
    
    public static final GoalType STARTING_GOAL = GoalType.ATTACK_SURROUND_DISTRACT;
    public static final boolean RANDOM_CASE_BASE = false;
    
    
    private static int mapWidth = -3;
	private static int mapHeight = -3;
    private static final int MAP_EDGE_MARGIN = 32*3; // so that units don't try to move off the map, 32 is tile size
	
	
    public static ArrayList<GoalType> getStartingGoals() {
    	ArrayList<GoalType> STARTING_GOALS = new ArrayList<GoalType>();
    	
    	//STARTING_GOALS.add(GoalType.ATTACK_GROUND_SURROUND);
    	//STARTING_GOALS.add(GoalType.ATTACK_SURROUND_DISTRACT);
    	//STARTING_GOALS.add(GoalType.ATTACK_GROUND_DIRECT);
    	STARTING_GOALS.add(GoalType.ATTACK_GROUND_SURROUND);
    	STARTING_GOALS.add(GoalType.ATTACK_SURROUND_DISTRACT);
    	return STARTING_GOALS;
    }
    
    public static void setStartingRegionID(int r, String mapName) {
        startingRegionID = r;
        
        /*******************
         * 
         * Hardcoded region knowledge for different maps
         * 
         *******************/
        
        if (mapName.contains("Benzene")) {
        	// starting regions are 3 and 6
        	enemyStartingRegionID = r == 3 ? 6 : 3;
        }else if (mapName.contains("Destination")) {
        	// starting regions are 0 and 2
        	enemyStartingRegionID = r == 0 ? 2 : 0;
        }else if (mapName.contains("HeartbreakRidge")) {
        	// starting regions are 0 and 2
        	enemyStartingRegionID = r == 8 ? 0 : 8;
        }else if (mapName.contains("BottleneckLimited")){
        	// default testing map
        	enemyStartingRegionID = r == 2 ? 4 : 2;
        }else if (mapName.contains("Aztec")){
        	// default testing map
        	enemyStartingRegionID = r == 18 ? 2 : 18;
        }else if (mapName.contains("Challenger")){
        	// default testing map
        	enemyStartingRegionID = r == 11 ? 15 : 11;
        }else if (mapName.contains("Road")){
        	// default testing map
        	enemyStartingRegionID = r == 2 ? 25 : 2;
        }else if (mapName.contains("Boxer")){
        	// default testing map
        	enemyStartingRegionID = r == 2 ? 4 : 2;
        }else if (mapName.contains("River") && mapName.contains("Crossing")){
        	// default testing map
        	enemyStartingRegionID = r == 0 ? 10 : 0;
        }else if (mapName.contains("Volcanis")){
        	// default testing map
        	enemyStartingRegionID = r == 1 ? 3 : 1;
        }
        
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

	
    
    public static void setMapWidth(int mapWidth) {
    	GlobalInfo.mapWidth = mapWidth;
    }
    
    public static void setMapHeight(int mapHeight) {
    	GlobalInfo.mapHeight = mapHeight;
    }
    
    
    public static Point getNearestMapCorner(Point p) {
    	// 4 corners are
    	Point corner1 = new Point(0+MAP_EDGE_MARGIN,0+MAP_EDGE_MARGIN);
    	Point corner2 = new Point(mapWidth-MAP_EDGE_MARGIN,0+MAP_EDGE_MARGIN);
    	Point corner3 = new Point(0+MAP_EDGE_MARGIN,mapHeight-MAP_EDGE_MARGIN);
    	Point corner4 = new Point(mapWidth-MAP_EDGE_MARGIN,mapHeight-MAP_EDGE_MARGIN);
    	
    	ArrayList<Point> corners = new ArrayList<Point>();
    	corners.add(corner1);
    	corners.add(corner2);
    	corners.add(corner3);
    	corners.add(corner4);
    	
    	Double currShortestDist = Math.abs(corner1.distance(p));
    	Point nearestCorner = corner1;
    	for (Point currCorner : corners) {
    		Double currDist = Math.abs(currCorner.distance(p)); 
    		if (currDist < currShortestDist) {
    			nearestCorner = currCorner;
    			currShortestDist = currDist;
    		}
    	}
    	
    	//System.out.println("Center of enemy region: "+p.toString()+", nearest corner chosen: "+nearestCorner.toString()+", mapWidth: "+mapWidth+", mapHeight: "+mapHeight);    
    	return nearestCorner;
    }
    
    /**
     * Returns a list of the unit id's of all my fighting units. It gets this
     * information from the ontology.
     * @return 
     */
    public static ArrayList<Integer> getAllFightingUnits() {
        return OntologyInfo.getInstance().getInstance().getAllFightingUnitIds();
    }

    public static int getEnemyPlayerID() {
        return 1;
    }
    
    public static int getOurPlayerID() {
        return 0;
    }
    
    
}
