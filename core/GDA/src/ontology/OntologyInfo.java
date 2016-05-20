package ontology;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.MalformedJsonException;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.reasoner.Reasoner;
import common.Pair;
import common.ProjectSettings;
import common.Useful;
import gda.GlobalInfo;
import java.awt.Color;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import org.mindswap.pellet.jena.PelletReasonerFactory;

/**
 * NOT FINISHED - need to come back to after experiments are done
 *
 * This class maintains the current ontology the system is using. If anywhere
 * else in the GDA system, questions need to be asked about the ontology, they
 * should be done using this class.
 *
 * @author Dustin Dannenhauer
 * @email dtd212@lehigh.edu
 * @date Aug 8, 2013
 */
public class OntologyInfo {

    static JenaInterface jena;
    static String NS = "";
    static boolean initialized = false;
    static String INPUT_PATH = Useful.getPropValue("GAMESTATE_LOG_FILE");
    static Logger logger = Logger.getLogger(OntologyInfo.class.getName());
    static String prevDumpFileName = "";
    // Timers (for gui)
    static Timer lastDumpFileTimer = null;
    static JLabel lastDumpFileLabel = null;
    // Used to collect information on the first game state dump
    static boolean firstDump = true;
    static JTextArea globalInfoTextArea = null;
    static boolean haveWeReadGameStateFile = false;
    static JLabel ontologyStatusLabel = null;
    static volatile boolean ontologyLockedInUpdate = false;

    /**
     * Sets up the variables for working with the ontology. This method should
     * be called before any other method of this class is called.
     */
    public synchronized static void init(JenaInterface jena) {
        update(); // just make sure you call update at least once
        // before trying to query the ontology
        initialized = true;
    }

    public synchronized static void setLogger(Logger logggger) {
        logger = logggger;
    }

    public synchronized static void setLastDumpFileTimer(Timer t) {
        lastDumpFileTimer = t;
    }

    public synchronized static void setLastDumpFileLabel(JLabel label) {
        lastDumpFileLabel = label;
    }

    public synchronized static void setGlobalInfoTextArea(JTextArea textArea) {
        globalInfoTextArea = textArea;
    }

    public static void setOntologyStatusLable(JLabel statusLabel) {
        ontologyStatusLabel = statusLabel;
        ontologyStatusLabel.setOpaque(true);
        ontologyStatusLabel.setBackground(Color.gray);
    }

    /**
     * Runs the reasoner over the last data that has been dumped from the
     * gamestate of the c++ bot.
     */
    public synchronized static boolean update() {




        File logFile = new File(Useful.getPropValue("GAMESTATE_LOG_FILE"));
        boolean logFileExists = logFile.exists();

        String dumpFileName = getLastDumpFile();
        GameState gameState = null;

        // if log file doesnt exist, fail
        // if dumpFileName is the same as the prev One, return false also
        if (!logFileExists || dumpFileName.equals(prevDumpFileName)) {
            return false;
        }

        try {

            if (!dumpFileName.equals(prevDumpFileName)) {
                ontologyLockedInUpdate = true;
                // only load new ontology if dump file has changed
                InputStream in = new FileInputStream(dumpFileName);
                ontologyStatusLabel.setForeground(Color.green);
                ontologyStatusLabel.setText("Reading last dump");
                gameState = readJsonStream(in);

                // reset timer once we have read new gamestate dump
                if (lastDumpFileTimer != null && lastDumpFileLabel != null) {
                    lastDumpFileTimer.restart();
                    lastDumpFileLabel.setText("0");
                }

                ontologyStatusLabel.setForeground(Color.CYAN);
                ontologyStatusLabel.setText("Loading base ontology file into jena");
                jena = new JenaInterface(Useful.getPropValue("ONTOLOGY_FILE"));
                ontologyStatusLabel.setForeground(Color.blue);
                ontologyStatusLabel.setText("Loading gamestate into jena");
                jena.loadGameState(gameState);
                ontologyStatusLabel.setForeground(Color.pink);
                ontologyStatusLabel.setText("Reasoning over model");
                jena.reasonOverModel();
                haveWeReadGameStateFile = true;
                ontologyLockedInUpdate = false;
            }

        } catch (FileNotFoundException e) {
            logger.log(Level.WARNING, "Error: " + e.toString());
            ontologyLockedInUpdate = false;
        } catch (MalformedJsonException e) {
            logger.log(Level.WARNING, "Error: " + e.toString());
            JOptionPane.showMessageDialog(null, e.getMessage());
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error: " + e.toString());
            ontologyLockedInUpdate = false;

        } catch (Exception e) {
            logger.log(Level.WARNING, "Error: " + e.toString());
            ontologyLockedInUpdate = false;
        }
        collectGlobalInfo();
        return true;
    }

    /**
     * For now only run this the first time we get a gamestate dump
     */
    private synchronized static void collectGlobalInfo() {
        // just in case I start using this in more than one place later
        if (firstDump) {
            firstDump = false;
            // 1. get my starting region (should be the only region I control)
            int countNumberOfMyControlledRegions = 0;
            int startingRegionID = -1;
            for (Pair<String, String> p : getControlledRegions()) {
                if (p.getFirst().equals("" + ProjectSettings.myPlayerID)) {
                    startingRegionID = Integer.parseInt(p.getSecond());
                    countNumberOfMyControlledRegions++;
                }
            }

            if (countNumberOfMyControlledRegions == 1) {
                GlobalInfo.setStartingRegionID(startingRegionID);
            }

        }

        if (globalInfoTextArea != null) {
            String str = "My Starting region = " + GlobalInfo.getStartingRegionID();
            str += "\nEnemy Starting region = " + GlobalInfo.getEnemyStartingRegionID();

            globalInfoTextArea.setText(str);
        }

    }

    /**
     * The output of the StarCraft API provides a log file with a list of
     * gamestate output file paths. This function returns a String representing
     * the latest gamestate output file.
     *
     * @return	String representing the latest gamestate output file.
     */
    public synchronized static String getLastDumpFile() {
        String s = "";
        String lastLine = "";
        try {
            BufferedReader b = new BufferedReader(new FileReader(INPUT_PATH));

            while ((s = b.readLine()) != null) {
                lastLine = s;
            }
            System.out.println(lastLine);




        } catch (IOException ex) {
            Logger.getLogger(OntologyInfo.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return lastLine;
    }

    /**
     * ***************************************************
     * ************************************************* *
     *
     * BEGIN
     *
     * JSON PARSING CODE
     *
     * ************************************************* *
     ****************************************************
     */
    /**
     * This function takes as input an InputStream of a JSON gamestate output
     * file and uses a JsonReader parser to extract the gamestate object (the
     * entire document)
     *
     * @param in	the InputStream of the gamestate output file
     * @return	the GameState object associated with this gamestate document
     */
    public synchronized static GameState readJsonStream(InputStream in) throws IOException {
        GameState g = new GameState();
        JsonReader reader = new JsonReader(new InputStreamReader(in));
        try {
            g = readGameState(reader);
        } catch (Exception e) {
            System.out.println("Error: " + e);
        } finally {
            reader.close();
        }
        return g;
    }

    /**
     * Extracts the lists of Player, Region, and ChokePoint objects and adds
     * them to the GameState object associated with this JSON file.
     *
     * @param reader	the JsonReader object containing the Gamestate object JSON.
     * @return gameState	the GameState object containing the Players, Regions,
     * and Checkpoints of the game.
     */
    public synchronized static GameState readGameState(JsonReader reader) throws IOException {
        GameState gameState = new GameState();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("players")) {
                gameState.setPlayers(readPlayersArray(reader));
            } else if (name.equals("regions")) {
                gameState.setRegions(readRegionsArray(reader));
            } else if (name.equals("chokepoints")) {
                gameState.setChokepoints(readChokepointsArray(reader));
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return gameState;
    }

    /**
     * Reads a Players array in JSON format. The array can contain any number of
     * players, but at its current state it assumes that there are two, one
     * friendly and one enemy.
     *
     * @param reader	a JsonReader object currently pointed to the Player array.
     * @return players	an ArrayList<Player> containing all players in the JSON
     * array.
     */
    public synchronized static ArrayList<Player> readPlayersArray(JsonReader reader) throws IOException {
        ArrayList<Player> players = new ArrayList<Player>();
        reader.beginArray();
        while (reader.hasNext()) {
            players.add(readPlayerObject(reader));
        }
        reader.endArray();
        return players;
    }

    /**
     * Reads a Player object in a JSON format
     *
     * @param reader	a JsonReader object pointed to a Player object
     * @return player	a Player object.
     */
    public synchronized static Player readPlayerObject(JsonReader reader) throws IOException {
        Player player = new Player();
        Player enemyPlayer = new Player();
        reader.beginObject();

        ArrayList<Unit> units = null;
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("playerId")) {
                int playerId = reader.nextInt();
                player.setPlayerId(playerId);
                enemyPlayer.setPlayerId(playerId + 1);
            } else if (name.equals("myUnits")) {
                ArrayList<Unit> unitsArray = readUnitsArray(reader);
                player.setMyUnits(unitsArray);
            } else if (name.equals("enemyUnits")) {
                ArrayList<Unit> enemyUnitsArray = readUnitsArray(reader);
                enemyPlayer.setMyUnits(enemyUnitsArray);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return player;
    }

    /**
     * Reads an array of Units in JSON format
     *
     * @param	reader	a JsonReader object pointed to an array of Units
     * @return units	an ArrayList<Unit> of Unit objects contained in the array
     */
    public synchronized static ArrayList<Unit> readUnitsArray(JsonReader reader) throws IOException {
        ArrayList<Unit> units = new ArrayList<Unit>();

        reader.beginArray();
        while (reader.hasNext()) {
            units.add(readUnit(reader));
        }
        reader.endArray();
        return units;
    }

    /**
     * Reads a Unit object, capturing all attributes in the object.
     *
     * @param reader	a JsonReader object pointed to an array of Units
     * @return units	a Unit object
     */
    public synchronized static Unit readUnit(JsonReader reader) throws IOException {
        Unit unit = new Unit();

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("unitID")) {
                unit.setUnitId(reader.nextInt());
            } else if (name.equals("unitType")) {
                unit.setUnitType(reader.nextString());
            } else if (name.equals("currentHitPoints")) {
                unit.setCurrentHitPoints(reader.nextInt());
            } else if (name.equals("maxHitPoints")) {
                unit.setMaxHitPoints(reader.nextInt());
            } else if (name.equals("isBeingAttacked")) {
                int b = reader.nextInt();
                boolean isBeingAttacked = false;
                if (b == 1) {
                    isBeingAttacked = true;
                }
                unit.setIsBeingAttacked(isBeingAttacked);
            } else if (name.equals("x")) {
                unit.setXCoord(reader.nextInt());
            } else if (name.equals("y")) {
                unit.setYCoord(reader.nextInt());
            } else if (name.equals("regionID")) {
                unit.setRegionId(reader.nextInt());
            } else if (name.equals("armor")) {
                unit.setArmor(reader.nextInt());
            } else if (name.equals("mineralCost")) {
                unit.setMineralCost(reader.nextInt());
            } else if (name.equals("gasCost")) {
                unit.setGasCost(reader.nextInt());
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return unit;
    }

    /**
     * Reads a Region array in JSON format. The array can contain any number of
     * regions.
     *
     * @param reader	a JsonReader pointed to an array of Regions.
     * @return regions	returns an ArrayList<Region> of Region objects.
     */
    public synchronized static ArrayList<Region> readRegionsArray(JsonReader reader) throws IOException {
        ArrayList<Region> regions = new ArrayList<Region>();
        reader.beginArray();
        while (reader.hasNext()) {
            regions.add(readRegionObject(reader));
        }
        reader.endArray();
        return regions;
    }

    /**
     * Reads a ChokePoint array in JSON format. The array can contain any number
     * of chokepoints.
     *
     * @param reader	a JsonReader pointed to an array of ChokePoint objects.
     * @return chokepoints	an ArrayList<Chokepoint> of chokepoint objects.
     */
    public synchronized static ArrayList<ChokePoint> readChokepointsArray(JsonReader reader) throws IOException {
        ArrayList<ChokePoint> chokepoints = new ArrayList<ChokePoint>();
        reader.beginArray();
        while (reader.hasNext()) {
            chokepoints.add(readChokepointObject(reader));
        }
        reader.endArray();
        return chokepoints;
    }

    /**
     * Reads a Region object in JSON format.
     *
     * @param reader	a JsonReader pointed to a Region object
     * @return region	a Region object
     */
    public synchronized static Region readRegionObject(JsonReader reader) throws IOException {
        Region region = new Region();
        reader.beginObject();

        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("regionCenterX")) {
                region.setRegionCenterX(reader.nextInt());
            } else if (name.equals("regionCenterY")) {
                region.setRegionCenterY(reader.nextInt());
            } else if (name.equals("regionID")) {
                region.setRegionId(reader.nextInt());
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return region;
    }

    /**
     * Reads a Chokepiont object in JSON format.
     *
     * @param reader	a JsonReader pointed to a ChokePoint object
     * @return chokepoint	a ChokePoint object
     */
    public synchronized static ChokePoint readChokepointObject(JsonReader reader) throws IOException {
        ChokePoint chokepoint = new ChokePoint();
        reader.beginObject();

        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("chokepointID")) {
                chokepoint.setChokepointId(reader.nextInt());
            } else if (name.equals("chokepointCenterX")) {
                chokepoint.setChokepointCenterX(reader.nextInt());
            } else if (name.equals("chokepointCenterY")) {
                chokepoint.setChokepointCenterY(reader.nextInt());
            } else if (name.equals("connectedToRegionOneID")) {
                chokepoint.setConnectedToRegionOne(reader.nextInt());
            } else if (name.equals("connectedToRegionTwoID")) {
                chokepoint.setConnectedToRegionTwo(reader.nextInt());
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return chokepoint;
    }

    /**
     * ***************************************************
     * ************************************************* *
     *
     * END
     *
     * JSON PARSING CODE
     *
     * ************************************************* *
     ****************************************************
     */
    /**
     * ***************************************************
     * ************************************************* *
     *
     * BEGIN
     *
     * ONTOLOGY QUERY'ING CODE
     *
     * ************************************************* *
     ****************************************************
     */
    public synchronized static ResultSet queryEnemyAbilities() {
        waitUntilOntologyUnlocked();

        // player 1 refers to the enemy

        String specificPlayerQueryString = "PREFIX sc:<" + jena.getNS() + ">"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "SELECT ?s ?p ?o "
                + "WHERE {"
                + //"?player0 rdf:type sc:Player ."+
                "?s sc:hasPlayerId 1 ."
                + "?s ?p ?o ."
                + "}";

        // run the query
        ResultSet results = jena.queryModel(specificPlayerQueryString);

        // make query object
        Query query = QueryFactory.create(specificPlayerQueryString);

        // format results and send to standard out
        //ResultSetFormatter.out(System.out, results, query);

        return results;
    }

    /**
     * Returns the regions of which the given player has some presence in
     *
     * @param playerId 0 for me, 1 for enemy
     * @return
     */
    public synchronized static ResultSet queryPlayerPresenceRegions(int playerId) {
        ResultSet results = null;

        waitUntilOntologyUnlocked();
        String queryString = "PREFIX sc:<" + jena.getNS() + ">"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "SELECT ?r "
                + "WHERE {"
                //+ "?player0 rdf:type sc:Player ."+
                //+ "?s rdf:type sc:Player ."
                + "?r rdf:type sc:Region ."
                + "sc:player" + playerId + " sc:hasPresenceIn ?r ."
                //+ "?s sc:hasCenterY ?centerY ."
                + "}";
        results = jena.queryModel(queryString);

        // make query object
        //Query query = QueryFactory.create(queryString);




        // format results and send to standard out
        //ResultSetFormatter.out(System.out, results, query);

        return results;

    }

    private synchronized static ResultSet queryRegions() {
        waitUntilOntologyUnlocked();
        String queryString = "PREFIX sc:<" + jena.getNS() + ">"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "SELECT ?r ?id "
                + "WHERE {"
                + "?r rdf:type sc:Region ."
                + "?r sc:hasRegionId ?id"
                //+ "?s sc:hasCenterY ?centerY ."
                + "}";
        ResultSet results = jena.queryModel(queryString);

        // make query object
        Query query = QueryFactory.create(queryString);

        // format results and send to standard out
        //ResultSetFormatter.out(System.out, results, query);

        return results;
    }

    public synchronized static ArrayList<String> getAdjacentRegions(int regionID) {
        String queryString = "PREFIX sc:<" + jena.getNS() + ">"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
                + "SELECT ?rA "
                + "WHERE {"
                + "?r rdf:type sc:Region ."
                + "?r sc:hasRegionId \"" + regionID + "\"^^xsd:int ."
                + "?c rdf:type sc:Chokepoint ."
                + "{"
                + "?c sc:isConnectedToRegionOne ?r ."
                + "?c sc:isConnectedToRegionTwo ?rA ."
                + "} UNION {"
                + "?c sc:isConnectedToRegionOne ?rA ."
                + "?c sc:isConnectedToRegionTwo ?r ."
                + "}"
                //+ "?s sc:hasCenterY ?centerY ."
                + "}";
        
        //System.out.println(queryString);
        //JOptionPane.showMessageDialog(null, queryString);
        ResultSet results = jena.queryModel(queryString);

        // make query object
        //Query query = QueryFactory.create(queryString);

        // format results and send to standard out
        //ResultSetFormatter.out(System.out, results, query);

        ArrayList<String> adjacentRegions = new ArrayList<String>();

        while (results.hasNext()) {
            QuerySolution soln = results.nextSolution();
            //System.out.println("Region is "+soln.getResource("r").getLocalName());
            adjacentRegions.add(soln.getResource("rA").getLocalName());
        }

        return adjacentRegions;
    }

    /**
     * Returns the mapping of regions to the player that controls that region
     *
     * @return
     */
    public synchronized static ArrayList<Pair<String, String>> getControlledRegions() {
        waitUntilOntologyUnlocked();
        boolean atLeastOneRegionControlled = false;
        ArrayList<Pair<String, String>> controlledRegions = null;

        do {
            // collect all the regions that player0 has presence in
            ArrayList<String> player0Regions = new ArrayList<>();
            ResultSet player0Results = queryPlayerPresenceRegions(0);

            while (player0Results.hasNext()) {
                QuerySolution soln = player0Results.nextSolution();
                //System.out.println("Region is "+soln.getResource("r").getLocalName());
                player0Regions.add(soln.getResource("r").getLocalName());
            }

            // warning - we do not control any regions, which means we should be dead
            if (player0Regions.isEmpty()) {
                System.out.println("We don't control any regions");
            }

            // collect all regions that player 1 has a presence in
            ArrayList<String> player1Regions = new ArrayList<>();
            ResultSet player1Results = queryPlayerPresenceRegions(1);

            while (player1Results.hasNext()) {
                QuerySolution soln = player1Results.nextSolution();
                //System.out.println("Region is "+soln.getResource("r").getLocalName());
                player1Regions.add(soln.getResource("r").getLocalName());
            }

            controlledRegions = new ArrayList<>();

            // build up the hashmap of controlled regions
            ArrayList<String> player0RegionsClone = (ArrayList<String>) player0Regions.clone();
            for (String player0Region : player0RegionsClone) {
                // check if exists in player 1's regions
                if (!player1Regions.contains(player0Region)) {
                    controlledRegions.add(new Pair("0", player0Region.replaceAll("region", "")));
                } else {
                    // remove both regions from each players array list
                    player0Regions.remove(player0Region);
                    player1Regions.remove(player0Region);
                }
            }
            // any remaining regions in player1Regions list get added to hashmap
            for (String player1Region : player1Regions) {
                controlledRegions.add(new Pair("1", player1Region.replaceAll("region", "")));
            }

            // detects bug in ontology, means no players control any regions
            if (!controlledRegions.isEmpty()) {
                atLeastOneRegionControlled = true;
            } else {
                update(); // refresh ontology
            }

        } while (!atLeastOneRegionControlled);

        return controlledRegions;
        //if (soln.getResource("r").toString().contains(matchTrain.subSequence(0, matchTrain.length()))) {
    }

    /**
     * Returns a query over all the regions that are contested
     *
     * @return
     */
    public synchronized static ResultSet queryContestedRegions() {
        waitUntilOntologyUnlocked();

        String queryString = "PREFIX sc:<" + jena.getNS() + ">"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "SELECT ?r ?centerX ?centerY "
                + "WHERE {"
                + //"?player0 rdf:type sc:Player ."+
                "?r rdf:type sc:ContestedRegion ."
                //+ "?p sc:hasPresenceIn ?r ."
                + "?r sc:hasCenterX ?centerX ."
                + "?r sc:hasCenterY ?centerY ."
                + "}";

        // run the query
        ResultSet results = jena.queryModel(queryString);

        // make query object
        Query query = QueryFactory.create(queryString);

        // format results and send to standard out
        //ResultSetFormatter.out(System.out, results, query);

        return results;
    }

    public synchronized static ResultSet queryBattleRegions() {
        waitUntilOntologyUnlocked();
        String queryString = "PREFIX sc:<" + jena.getNS() + ">"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "SELECT ?s ?centerX ?centerY "
                + "WHERE {"
                + //"?player0 rdf:type sc:Player ."+
                "?s rdf:type sc:BattleRegion ."
                + "?s sc:hasCenterX ?centerX ."
                + "?s sc:hasCenterY ?centerY ."
                + "}";

        // run the query
        ResultSet results = jena.queryModel(queryString);

        // make query object
        Query query = QueryFactory.create(queryString);

        // format results and send to standard out
        //ResultSetFormatter.out(System.out, results, query);

        return results;
    }

    public synchronized static ResultSet queryMyUnits() {
        waitUntilOntologyUnlocked();
        String queryString = "PREFIX sc:<" + jena.getNS() + ">"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "SELECT ?u ?t ?x ?y "
                + "WHERE {"
                + "?s sc:hasPlayerId 0 ." // human is always 0
                + "?s sc:hasUnit ?u ."
                + "?u rdf:type sc:Unit ."
                + "?u sc:hasXCoord ?x ."
                + "?u sc:hasYCoord ?y ."
                + "?u rdf:type ?t ."
                + "}";

        // run the query
        ResultSet results = jena.queryModel(queryString);

        // make query object
        Query query = QueryFactory.create(queryString);

        // format results and send to standard out
        //ResultSetFormatter.out(System.out, results, query);

        return results;
    }

    public synchronized static ResultSet queryMySCVs() {
        waitUntilOntologyUnlocked();
        String queryString = "PREFIX sc:<" + jena.getNS() + ">"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "SELECT REDUCED ?u ?x ?y "
                + "WHERE {"
                + "?s sc:hasPlayerId 0 ." // human is always 0
                + "?s sc:hasUnit ?u ."
                + "?u rdf:type sc:SCV ."
                + "?u sc:hasXCoord ?x ."
                + "?u sc:hasYCoord ?y ."
                //+ "?u rdf:type ?t ."
                + "}"
                + "ORDER BY ?u";

        // run the query
        ResultSet results = jena.queryModel(queryString);

        // make query object
        Query query = QueryFactory.create(queryString);

        // format results and send to standard out
        //ResultSetFormatter.out(System.out, results, query);

        return results;
    }

    public synchronized static ResultSet queryMyMarines() {
        waitUntilOntologyUnlocked();
        String queryString = "PREFIX sc:<" + jena.getNS() + ">"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "SELECT REDUCED ?u ?x ?y "
                + "WHERE {"
                + "?s sc:hasPlayerId 0 ." // human is always 0
                + "?s sc:hasUnit ?u ."
                + "?u rdf:type sc:Marine ."
                + "?u sc:hasXCoord ?x ."
                + "?u sc:hasYCoord ?y ."
                //+ "?u rdf:type ?t ."
                + "}"
                + "ORDER BY ?u";

        // run the query
        ResultSet results = jena.queryModel(queryString);

        // make query object
        Query query = QueryFactory.create(queryString);

        // format results and send to standard out
        //ResultSetFormatter.out(System.out, results, query);

        return results;
    }

    public synchronized static Integer countUnitsOfTypeOfPlayer(String type, int playerId) {
        waitUntilOntologyUnlocked();
        // just a quick hack until all the unit classes in our ontology file
        // have the same names that BWAPI uses (i.e. Terran_Marine etc)
        if (type.equals("Terran_Marine")) {
            type = "Marine";
        } else if (type.equals("Terran_Wraith")) {
            type = "Wraith";
        }

        String queryString = "PREFIX sc:<" + jena.getNS() + ">"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "SELECT ?u ?x ?y "
                + "WHERE {"
                + "?s sc:hasPlayerId " + playerId + " ." // human is always 0
                + "?s sc:hasUnit ?u ."
                + "?u rdf:type sc:" + type + " ."
                //+ "?u rdf:type sc:Unit ."
                //+ "?u sc:hasXCoord ?x ."
                //+ "?u sc:hasYCoord ?y ."
                + "}";

        ResultSet results = jena.queryModel(queryString);

        // make query object
        Query query = QueryFactory.create(queryString);
        //ResultSet afterQueryResultSet;
        //logger.log(Level.INFO, ResultSetFormatter.asText(results, query));

        int count = 0;
        while (results.hasNext()) {
            QuerySolution soln = results.nextSolution();
            //System.out.println("Region is "+soln.getResource("r").getLocalName());
            //if (soln.getResource("u").getLocalName().equals(type)) {
            count++;
            //}
        }

        return count; // change this 

    }

    /**
     * Returns the chokepoint point (x,y) of the chokepoints connected to the
     * enemy region
     *
     * @param regionId
     * @return An array list of all the points
     */
    public synchronized static ArrayList<Point> queryChokepointsOfRegionId(int regionId) {
        waitUntilOntologyUnlocked();

        ArrayList<Point> chokePointCenters = new ArrayList<Point>();
        String queryString = "PREFIX sc:<" + jena.getNS() + ">"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "SELECT ?x ?y "
                + "WHERE {"
                + "?r rdf:type sc:Region ."
                + "?c sc:isConnectedToRegionOne ?r1 ."
                + "?c sc:isConnectedToRegionTwo ?r2 ."
                + "?c sc:hasChokePointCenterX ?x ."
                + "?c sc:hasChokePointCenterX ?y ."
                + "?r1 sc:hasRegionId ?r1ID ."
                + "?r2 sc:hasRegionId ?r2ID ."
                + "FILTER (?r1ID = " + regionId + " || ?r2ID = " + regionId + ")"
                //+ "?s sc:hasCenterY ?centerY ."
                + "}";
        ResultSet results = jena.queryModel(queryString);

        // make query object
        Query query = QueryFactory.create(queryString);

        // format results and send to standard out
        //ResultSetFormatter.out(System.out, results, query);

        return chokePointCenters;

    }

    public synchronized static ResultSet queryEnemyUnits() {
        waitUntilOntologyUnlocked();
        String queryString = "PREFIX sc:<" + jena.getNS() + ">"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "SELECT ?u ?x ?y "
                + "WHERE {"
                + "?s sc:hasPlayerId 1 ." // human is always 0
                + "?s sc:hasUnit ?u ."
                + "?u rdf:type sc:Unit ."
                + "?u sc:hasXCoord ?x ."
                + "?u sc:hasYCoord ?y ."
                + "}";

        // run the query
        ResultSet results = jena.queryModel(queryString);

        // make query object
        //Query query = QueryFactory.create(queryString);

        // format results and send to standard out
        //ResultSetFormatter.out(System.out, results, query);

        return results;
    }

    public synchronized static ResultSet queryArmyHealth() {
        waitUntilOntologyUnlocked();
        String queryString = "PREFIX sc:<" + jena.getNS() + ">"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "SELECT ?s ?curr ?max "
                + "WHERE {"
                + //"?player0 rdf:type sc:Player ."+
                "?s rdf:type sc:Unit ."
                + "?s sc:hasCurrentHitPoints ?curr ."
                + "?s sc:hasMaxHitPoints ?max ."
                + "?s sc:isOwnedBy sc:player0 ."
                + "}";

        // run the query
        ResultSet results = jena.queryModel(queryString);

        // make query object
        Query query = QueryFactory.create(queryString);

        return results;
    }

    public synchronized static ArrayList<String> getContestedRegions() {
        waitUntilOntologyUnlocked();
        ArrayList<String> contestedRegions = new ArrayList<>();
        ResultSet player0Results = queryContestedRegions();

        while (player0Results.hasNext()) {
            QuerySolution soln = player0Results.nextSolution();
            //System.out.println("Region is "+soln.getResource("r").getLocalName());
            contestedRegions.add(soln.getResource("r").getLocalName());
        }

        return contestedRegions;
    }

    public synchronized static boolean isRegionContested(int regionID) {
        waitUntilOntologyUnlocked();
        ArrayList<String> contestedRegions = getContestedRegions();
        for (String r : contestedRegions) {
            if (r.replace("region", "").equals("" + regionID)) {
                return true;
            }
        }
        return false;
    }

    public synchronized static ArrayList<Integer> getFightingUnitIDsInRegion(int playerID, int regionID) {
        waitUntilOntologyUnlocked();
        ArrayList<Integer> ids = new ArrayList<>();

        String queryString = "PREFIX sc:<" + jena.getNS() + ">"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "SELECT ?id "
                + "WHERE {"
                + "?s sc:hasPlayerId " + playerID + " ." // human is always 0
                + "?s sc:hasUnit ?u ."
                + "?u rdf:type sc:FightingUnit ."
                + "?u sc:hasUnitId ?id ."
                //+ "?u rdf:type sc:Unit ."
                //+ "?u sc:hasXCoord ?x ."
                //+ "?u sc:hasYCoord ?y ."
                + "}";

        ResultSet results = jena.queryModel(queryString);

        QuerySolution soln;
        while (results.hasNext()) {
            soln = results.next();
            ids.add(soln.getLiteral("id").getInt());
        }
        return ids;
    }

    public synchronized static boolean doesPlayerControlRegionSPARQL(int playerID, int region) {
        waitUntilOntologyUnlocked();
        ArrayList<Pair<String, String>> playersAndRegions = getControlledRegions();
        boolean result = false;
        for (Pair<String, String> player : playersAndRegions) { // regions
            if (player.getFirst().equals("" + playerID)) {
                if (player.getSecond().equals("" + region)) {
                    result = true;
                }
            }
        }
        if (!result) {
            System.out.println("AHHHHH");
        }
        return result;
    }

    /**
     * Returns true if the ontology says the player controls the region
     *
     * This is different than the SPARQL version because it actually adds the
     * statement - player controls region - then runs the reasoner, and checks
     * to see if the ontology is consistent.
     *
     * @param playerID
     * @param regionId
     * @return
     */
    public synchronized static boolean doesPlayerControlRegionPELLET(int playerID, int regionID) {
        waitUntilOntologyUnlocked();
        boolean result = false;

        // new statement (fact) to be added to the model
        Resource sub = ResourceFactory.createResource(jena.getNS() + "player" + playerID);
        System.out.println(jena.getNS() + "player" + playerID);
        Property pred = ResourceFactory.createProperty(jena.getNS() + "controls");
        Resource obj = ResourceFactory.createResource(jena.getNS() + "region" + regionID);
        Statement newFactPlayerControlsRegion = ResourceFactory.createStatement(sub, pred, obj);

        // add fact to model
        //Model modelWithExpectedFact = jena.getModel().add(newFactPlayerControlsRegion);
        Model modelWithExpectedFact = jena.getUnReasonedOverModel().add(newFactPlayerControlsRegion);

        // Pellet Reasoner
        Reasoner pelletReasoner = PelletReasonerFactory.theInstance().create();

        // get a copy of the model
        InfModel modelWithExpectedFacts = ModelFactory.createInfModel(pelletReasoner, modelWithExpectedFact);

        try {
            OntModel finalModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, modelWithExpectedFacts);
            finalModel.write(new FileOutputStream(new File("../Ontologies/testModelNoPlayerControlsRegion.owl")), null);
            //finalModel.write(System.out);
            //throw new FileNotFoundException();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(OntologyInfo.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    public synchronized static ArrayList<Integer> getUnitIDsInRegion(int regionID) {
        waitUntilOntologyUnlocked();
        ArrayList<Integer> resultUnitIDs = new ArrayList<>();

        String queryString = "PREFIX sc:<" + jena.getNS() + ">"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "SELECT DISTINCT ?id "
                + "WHERE {"
                + //"?player0 rdf:type sc:Player ."+
                "?r rdf:type sc:Region ."
                + "?r sc:hasRegionId " + regionID + " ."
                + "?r sc:contains ?u ."
                + "?u sc:hasUnitId ?id ."
                + "}";

        ResultSet results = jena.queryModel(queryString);
        while (results.hasNext()) {
            QuerySolution soln = results.nextSolution();
            resultUnitIDs.add(soln.getLiteral("id").getInt());
        }
        return resultUnitIDs;
    }

    public synchronized static Point getRegionCenterPosition(int regionID) {
        boolean success = false;
        QuerySolution soln = null;
        while (!success) {
            waitUntilOntologyUnlocked();

            String queryString = "PREFIX sc:<" + jena.getNS() + ">"
                    + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                    + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                    + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
                    + "SELECT ?r ?rID ?centerX ?centerY "
                    + "WHERE {"
                    + //"?player0 rdf:type sc:Player ."+
                    "?r rdf:type sc:Region ."
                    + "?r sc:hasRegionId \"" + regionID + "\"^^xsd:int ."
                    //+ "?r sc:hasRegionId ?rID ."
                    + "?r sc:hasCenterX ?centerX ."
                    + "?r sc:hasCenterY ?centerY ."
                    + "}";
            try {
                ResultSet regionResults = jena.queryModel(queryString);
                soln = regionResults.nextSolution();
                success = true;
            } catch (NoSuchElementException e) {
                // wierd bug in ontology, just try again
                success = false;
            }

        }

        return new Point(soln.getLiteral("centerX").getInt(),
                soln.getLiteral("centerY").getInt());
    }

    /**
     * ***************************************************
     * ************************************************* *
     *
     * END
     *
     * ONTOLOGY QUERY'ING CODE
     *
     * ************************************************* *
     ****************************************************
     */
    /**
     * ***************************************************
     * ************************************************* *
     *
     * BEGIN
     *
     * ONTOLOGY QUERY'ING CODE RETURNING NICE HUMAN READABLE FORMAT
     *
     * ************************************************* *
     ****************************************************
     */
    /**
     * Returns a string of nicely formatted region data
     *
     * @return
     */
    public static String getRegionData() {
        waitUntilOntologyUnlocked();

        String resultStr = "";
        ResultSet results;
        QuerySolution soln;

//        resultStr += "All Regions:\n";
//        resultStr += "-----------------------------\n";
//        results = queryRegions();
//        while (results.hasNext()) {
//            soln = results.next();
//            resultStr += soln.getResource("r").getLocalName() + " " + soln.getLiteral("id").getInt() + "\n";
//        }

        resultStr += "\nPlayer 0 Presence in Regions:\n";
        resultStr += "-----------------------------\n";
        results = queryPlayerPresenceRegions(0);
        while (results.hasNext()) {
            soln = results.next();
            resultStr += soln.getResource("r").getLocalName() + "\n";
        }

        resultStr += "\nPlayer 1 Presence in Regions:\n";
        resultStr += "-----------------------------\n";
        results = queryPlayerPresenceRegions(1);
        while (results.hasNext()) {
            soln = results.next();
            resultStr += soln.getResource("r").getLocalName() + "\n";
        }

        resultStr += "\nControlled Regions:\n";
        resultStr += "-----------------------------\n";
        ArrayList<Pair<String, String>> controlledRegions = getControlledRegions();
        for (Pair<String, String> r : controlledRegions) {
            resultStr += "player " + r.getFirst() + " controls region " + r.getSecond() + "\n";
        }

        resultStr += "\nContested Regions:\n";
        resultStr += "-----------------------------\n";
        results = queryContestedRegions();
        while (results.hasNext()) {
            soln = results.next();
            resultStr += soln.getResource("r").getLocalName() + "\n";
        }

        resultStr += "\nAll Regions:\n";
        resultStr += "-----------------------------\n";
        results = queryRegions();
        while (results.hasNext()) {
            soln = results.next();
            resultStr += soln.getResource("r").getLocalName() + " " + soln.getLiteral("id").getInt() + "\n";
        }

        return resultStr;
    }

    /**
     * Returns a string of nicely formatted region data
     *
     * @return
     */
    public static String getUnitData() {
        waitUntilOntologyUnlocked();

        String resultStr = "";
        ResultSet results;
        QuerySolution soln;

        resultStr += "Marines:\n";
        resultStr += "-----------------------------\n";
        results = queryMyMarines();
        while (results.hasNext()) {
            soln = results.next();
            resultStr += "Marine " + soln.getResource("u").getLocalName().replace("unit", "") + " at "
                    + soln.getLiteral("x").getInt() + "," + soln.getLiteral("y").getInt() + "\n";
        }

        resultStr += "\nSCVs:\n";
        resultStr += "-----------------------------\n";
        results = queryMySCVs();
        while (results.hasNext()) {
            soln = results.next();
            resultStr += "SCV " + soln.getResource("u").getLocalName().replace("unit", "") + " at "
                    + soln.getLiteral("x").getInt() + "," + soln.getLiteral("y").getInt() + "\n";
        }

        return resultStr;
    }

    /**
     * Returns true if the bot has written game state data files and the
     * ontology can read them
     *
     * @return
     */
    public synchronized static boolean isReady() {
        return haveWeReadGameStateFile;
    }

    public static void waitUntilOntologyUnlocked() {
        while (ontologyLockedInUpdate) {
            // twirl thumbs
            System.out.println("Ontology Locked");
        }
    }

    public static ArrayList<Integer> getAllFightingUnitIds() {
        waitUntilOntologyUnlocked();

        ArrayList<Integer> fightingUnitIds = new ArrayList<Integer>();

        /**
         * Get all flying vehicle ids *
         */
        String queryString = "PREFIX sc:<" + jena.getNS() + ">"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "SELECT DISTINCT ?id "
                + "WHERE {"
                + //"?player0 rdf:type sc:Player ."+
                "?u rdf:type sc:FlyingVehicle ."
                + "?u sc:isOwnedBy sc:player0 ."
                + "?u sc:hasUnitId ?id ."
                + "}";

        ResultSet results = jena.queryModel(queryString);
        while (results.hasNext()) {
            QuerySolution soln = results.nextSolution();
            fightingUnitIds.add(soln.getLiteral("id").getInt());
        }

        /**
         * Get all ground vehicle ids *
         */
        queryString = "PREFIX sc:<" + jena.getNS() + ">"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "SELECT DISTINCT ?id "
                + "WHERE {"
                + //"?player0 rdf:type sc:Player ."+
                "?u rdf:type sc:GroundVehicle ."
                + "?u sc:isOwnedBy sc:player0 ."
                + "?u sc:hasUnitId ?id ."
                + "}";

        results = jena.queryModel(queryString);
        while (results.hasNext()) {
            QuerySolution soln = results.nextSolution();
            fightingUnitIds.add(soln.getLiteral("id").getInt());
        }

        /**
         * Get all soldiers ids *
         */
        queryString = "PREFIX sc:<" + jena.getNS() + ">"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "SELECT DISTINCT ?id "
                + "WHERE {"
                + //"?player0 rdf:type sc:Player ."+
                "?u rdf:type sc:Soldier ."
                + "?u sc:isOwnedBy sc:player0 ."
                + "?u sc:hasUnitId ?id ."
                + "}";

        results = jena.queryModel(queryString);
        while (results.hasNext()) {
            QuerySolution soln = results.nextSolution();
            fightingUnitIds.add(soln.getLiteral("id").getInt());
        }

        return fightingUnitIds;

    }
}
