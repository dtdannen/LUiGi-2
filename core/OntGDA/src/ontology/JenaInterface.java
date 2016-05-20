package ontology;

/**
 * Author: William West Filename: JenaInterface.java Class: CSE428 - Semantic
 * Web Assignment: Final Project Description:	An object that allows for
 * interfacing with Jena and the model used to represent a GameState object.
 * Contains methods that are used to directly manipulate the model and query its
 * contents.
 */
import gda.GlobalInfo;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.query.ResultSet;
import common.Useful;

public class JenaInterface {

    private static final String BASE = "http://insyte.cse.lehigh.edu/starcraft_ontology";
    private static final String NS = "http://insyte.cse.lehigh.edu/starcraft_ontology#";
    private OntModel currentModel;
    private OntModel priorCurrentModel = null;

    public JenaInterface(String s) throws Exception {
        importOntology(s);
    }

    /**
     * Given a path to a base ontology file in RDF XML format, imports the file
     * and adds the model to currentModel
     */
    private void importOntology(String path) throws Exception {
        //System.out.println("Importing Ontology.");
        OntModel model = ModelFactory.createOntologyModel();
        InputStream in = FileManager.get().open(path);
        model.read(in, BASE);
        in.close();
        currentModel = model;
    }

    /**
     * @return BASE	the base URI for the ontology in currentModel
     */
    public String getBase() {
        return BASE;
    }

    /**
     * @return NS	the namespace for the ontology in currentModel
     */
    public String getNS() {
        return NS;
    }

    /**
     * @return currentModel	the ontology model represented by this JenaInterface
     * object. NOTE: this is the model that has already been reasoned over with
     *
     */
    public OntModel getModel() {
        return currentModel;
    }

    /**
     * Return the model without inferences.
     *
     * @return
     */
    public OntModel getUnReasonedOverModel() {
        return priorCurrentModel;
    }

    /**
     * Loads a GameState object into currentModel, including all players,
     * regions, and checkpoints.
     *
     * @param gameState	a GameState object to be loaded into the model
     */
    
    public void loadGameState(GameState gameState) {
    	loadMapName(gameState.getMapName());
    	loadMapWidthAndHeight(gameState.getMapWidth(), gameState.getMapHeight());
    	loadRegionList(gameState.getRegions());
        loadPlayersList(gameState.getPlayers());
        loadChokepointsList(gameState.getChokepoints());
    }

    /**
     * Loads an array of Region objects into currentModel.
     *
     * @param regions
     */
    private void loadRegionList(ArrayList<Region> regions) {
        for (Region r : regions) {
            loadRegion(r);
        }
    }

    /**
     * Loads an array of Player objects into currentModel.
     *
     * @param players
     */
    private void loadPlayersList(ArrayList<Player> players) {
        for (Player p : players) {
            loadPlayer(p);
        }
    }

    /**
     * Loads an array of Chokepoints into currentModel.
     *
     * @param chokepoints
     */
    private void loadChokepointsList(ArrayList<ChokePoint> chokepoints) {
        for (ChokePoint c : chokepoints) {
            loadChokepoint(c);
        }
    }

    /**
     * Loads an arary of Unit objects into currentModel
     *
     * @param units	an ArrayList<Unit> of units to be added to the model
     */
    private void loadUnitsList(ArrayList<Unit> units) {
        for (Unit u : units) {
            loadUnit(u);
        }
    }

    /**
     * Loads a Player object and all associated attributes into currentModel
     *
     * @param player	a Player object to be added to the model
     */
    private void loadPlayer(Player player) {
        int playerId = player.getPlayerId();
        int killScore = player.getKillScore();
        int unitScore = player.getUnitScore();
        int spentMinerals = player.getSpentMinerals();
        int spentGas = player.getSpentGas();
        int supplyTotal = player.getSupplyTotal();
        int razingScore = player.getRazingScore();
        

        if (player.getMyUnits() != null) {
            loadUnitsList(player.getMyUnits());
        }
        Resource playerClass = r("Player");
        Resource playerIndividual = r("player" + playerId);
        Property playerIdProperty = p("hasPlayerId");
        Property hasUnitProperty = p("hasUnit");
        Property hasEnemyUnitProperty = p("hasEnemyUnit");
        Literal playerIdValue = l(playerId);
        Property hasKillScoreProperty = p("hasKillScore");
        Literal killScoreValue = l(killScore);
        Property hasUnitScoreProperty = p("hasUnitScore");
        Literal unitScoreValue = l(unitScore);
        Property hasSpentMineralsProperty = p("hasSpentMinerals");
        Literal spentMineralsValue = l(spentMinerals);
        Property hasSpentGasProperty = p("hasSpentGas");
        Literal spentGasValue = l(spentGas);
        Property hasSupplyTotalProperty = p("hasSupplyTotal");
        Literal supplyTotalValue = l(supplyTotal);
        Property hasRazingScoreProperty = p("hasRazingScore");
        Literal razingScoreValue = l(razingScore);

        currentModel.createIndividual(NS + "player" + playerId, playerClass);
        currentModel.addLiteral(playerIndividual, playerIdProperty, playerIdValue);
        currentModel.addLiteral(playerIndividual, hasKillScoreProperty, killScoreValue);
        currentModel.addLiteral(playerIndividual, hasUnitScoreProperty, unitScoreValue);
        currentModel.addLiteral(playerIndividual, hasSpentMineralsProperty, spentMineralsValue);
        currentModel.addLiteral(playerIndividual, hasSpentGasProperty, spentGasValue);
        currentModel.addLiteral(playerIndividual, hasSupplyTotalProperty, supplyTotalValue);
        currentModel.addLiteral(playerIndividual, hasRazingScoreProperty, razingScoreValue);
        

        if (player.getMyUnits() != null) {
            for (Unit u : player.getMyUnits()) {
                Resource unitIndividual = r("unit" + u.getUnitId());
                currentModel.add(playerIndividual, hasUnitProperty, unitIndividual);
            }
        }
    }

    /**
     * Loads a Region object and all associated attributes into currentModel
     *
     * @param region	a Region object to be added to the model
     */
    private void loadRegion(Region region) {
        int regionId = region.getRegionId();
        int regionCenterX = region.getRegionCenterX();
        int regionCenterY = region.getRegionCenterY();

        Resource regionClass = r("Region");
        Resource regionIndividual = r("region" + regionId);
        Property regionIdProperty = p("hasRegionId");
        Property hasCenterXProperty = p("hasCenterX");
        Property hasCenterYProperty = p("hasCenterY");
        Literal regionIdValue = l(regionId);
        Literal centerXValue = l(regionCenterX);
        Literal centerYValue = l(regionCenterY);

        currentModel.createIndividual(NS + "region" + regionId, regionClass);
        currentModel.addLiteral(regionIndividual, regionIdProperty, regionIdValue);
        currentModel.addLiteral(regionIndividual, hasCenterXProperty, centerXValue);
        currentModel.addLiteral(regionIndividual, hasCenterYProperty, centerYValue);
    }
    
    /**
     * Adds the mapName into the ontology
     * @param mapName
     */
    private void loadMapName(String mapName) {
    	Resource matchClass = r("Match");
    	Resource matchIndividual = r("match0"); // create a match0
    	Property matchMapName = p("hasMapName");
    	Literal mapNameValue = l(mapName);
    	
    	currentModel.createIndividual(NS + "match0", matchClass);
    	currentModel.addLiteral(matchIndividual, matchMapName, mapNameValue);
    }

    /**
     * Adds the mapName into the ontology
     * @param mapName
     */
    private void loadMapWidthAndHeight(int width, int height) {
    	Resource matchClass = r("Match");
    	Resource matchIndividual = r("match0"); // create a match0
    	Property matchMapWidth = p("hasMapWidth");
    	Literal mapWidthValue = l(width);
    	Property matchMapHeight = p("hasMapHeight");
    	Literal mapHeightValue = l(height);
    	
    	currentModel.createIndividual(NS + "match0", matchClass);
    	currentModel.addLiteral(matchIndividual, matchMapWidth, mapWidthValue);
    	currentModel.addLiteral(matchIndividual, matchMapHeight, mapHeightValue);
    }
    
    /**
     * Loads a ChokePoint object and all associated attributes into currentModel
     *
     * @param chokepoint	a ChokePoint object to be added to the model
     */
    private void loadChokepoint(ChokePoint chokepoint) {
        int chokepointId = chokepoint.getChokepointId();
        int chokepointCenterX = chokepoint.getChokepointCenterX();
        int chokepointCenterY = chokepoint.getChokepointCenterY();
        int connectedToRegionOne = chokepoint.getConnectedToRegionOne();
        int connectedToRegionTwo = chokepoint.getConnectedToRegionTwo();

        Resource chokepointClass = r("Chokepoint");
        Resource chokepointIndividual = r("chokepoint" + chokepointId);
        Property chokepointIdProperty = p("hasChokepointId");
        Property chokepointCenterXProperty = p("hasChokepointCenterX");
        Property chokepointCenterYProperty = p("hasChokepointCenterY");
        Property connectedToRegionOneProperty = p("isConnectedToRegionOne");
        Property connectedToRegionTwoProperty = p("isConnectedToRegionTwo");

        Resource connectedToRegionOneValue = r("region" + connectedToRegionOne);
        Resource connectedToRegionTwoValue = r("region" + connectedToRegionTwo);
        Literal chokepointIdValue = l(chokepointId);
        Literal chokepointCenterXValue = l(chokepointCenterX);
        Literal chokepointCenterYValue = l(chokepointCenterY);

        currentModel.createIndividual(NS + "chokepoint" + chokepointId, chokepointClass);
        currentModel.addLiteral(chokepointIndividual, chokepointIdProperty, chokepointIdValue);
        currentModel.addLiteral(chokepointIndividual, chokepointCenterXProperty, chokepointCenterXValue);
        currentModel.addLiteral(chokepointIndividual, chokepointCenterYProperty, chokepointCenterYValue);
        currentModel.add(chokepointIndividual, connectedToRegionOneProperty, connectedToRegionOneValue);
        currentModel.add(chokepointIndividual, connectedToRegionTwoProperty, connectedToRegionTwoValue);
    }

    /**
     * Loads a Unit object and all associated attributes into currentModel
     *
     * @param unit	a Unit object to be added to the model
     */
    private void loadUnit(Unit unit) {
        int unitId = unit.getUnitId();
        String unitType = unit.getUnitType();
        int currentHitPoints = unit.getCurrentHitPoints();
        int maxHitPoints = unit.getMaxHitPoints();
        boolean isBeingAttacked = unit.getIsBeingAttacked();
        int xCoord = unit.getXCoord();
        int yCoord = unit.getYCoord();
        int regionId = unit.getRegionId();
        int armor = unit.getArmor();
        int mineralCost = unit.getMineralCost();
        int gasCost = unit.getGasCost();

        unitType = processUnitTypeString(unitType);
        //System.out.println(unitType);
        
        Resource unitSubClass = r(unitType);

        Resource isInRegionValue = r("region" + regionId);
        Literal unitIdValue = l(unitId);
        Literal currentHitPointsValue = l(currentHitPoints);
        Literal maxHitPointsValue = l(maxHitPoints);
        Literal isBeingAttackedValue = l(isBeingAttacked);
        Literal xCoordValue = l(xCoord);
        Literal yCoordValue = l(yCoord);
        Literal regionIdValue = l(regionId);
        Literal armorValue = l(armor);
        Literal mineralCostValue = l(mineralCost);
        Literal gasCostValue = l(gasCost);

        Resource unitClass = r("Unit");
        Resource unitIndividual = r("unit" + unitId);

        Property unitIdProperty = p("hasUnitId");
        Property currentHitPointsProperty = p("hasCurrentHitPoints");
        Property maxHitPointsProperty = p("hasMaxHitPoints");
        Property isBeingAttackedProperty = p("isBeingAttacked");
        Property xCoordProperty = p("hasXCoord");
        Property yCoordProperty = p("hasYCoord");
        Property isInRegionProperty = p("isInRegion");
        Property armorProperty = p("hasArmor");
        Property mineralCostProperty = p("hasMineralCost");
        Property gasCostProperty = p("hasGasCost");

        currentModel.createIndividual(NS + "unit" + unitId, unitSubClass);

        currentModel.addLiteral(unitIndividual, unitIdProperty, unitIdValue);
        currentModel.addLiteral(unitIndividual, currentHitPointsProperty, currentHitPointsValue);
        currentModel.addLiteral(unitIndividual, maxHitPointsProperty, maxHitPointsValue);
        currentModel.addLiteral(unitIndividual, isBeingAttackedProperty, isBeingAttackedValue);
        currentModel.addLiteral(unitIndividual, xCoordProperty, xCoordValue);
        currentModel.addLiteral(unitIndividual, yCoordProperty, yCoordValue);
        currentModel.add(unitIndividual, isInRegionProperty, isInRegionValue);
        currentModel.addLiteral(unitIndividual, armorProperty, armorValue);
        currentModel.addLiteral(unitIndividual, mineralCostProperty, mineralCostValue);
        currentModel.addLiteral(unitIndividual, gasCostProperty, gasCostValue);
    }

    /**
     * Allows for SPARQL queries over the model by external objects
     *
     * @param queryString	a query to be submitted to the model
     * @return resuls	a ResultSet obtained by querying the model
     */
    public ResultSet queryModel(String queryString) {
        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(query, currentModel);
        com.hp.hpl.jena.query.ResultSet results = qe.execSelect();
        return results;
    }

    /**
     * Uses a Pellet reasonser to obtain inferences over currentModel. The
     * reasoner used was obtained from: http://clarkparsia.com/pellet/
     *
     * Some code was used directly and indirectly from the following tutorial:
     * http://allthingssemantic.blogspot.com/2012/04/configuring-pellet-reasoner-and-jena.html
     */
    public void reasonOverModel() {
        Reasoner reasoner = ReasonerRegistry.getOWLReasoner();

        //bind the reasoner to the ontology model
        reasoner = reasoner.bindSchema(currentModel);

        //Bind the reasoner to the data model into a new Inferred model
        //System.out.println("Reasoning using OWL Reasoner.");
        InfModel infModel = ModelFactory.createInfModel(reasoner, currentModel);

        //Create pellet reasoner and bind to inferred model
        //System.out.println("Reasoning using Pellet Reasoner.");
        Reasoner pelletReasoner = PelletReasonerFactory.theInstance().create();
        InfModel pelletInfModel = ModelFactory.createInfModel(pelletReasoner, currentModel);

        OntModel finalModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, pelletInfModel);
        priorCurrentModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, currentModel);
        currentModel = finalModel;

        // if the user wants to save the current ontology, save it to a file
        if (GlobalInfo.saveNextOntologyToFile) {
            Useful.saveOntModelToFile(currentModel); // does it in another thread
            GlobalInfo.saveNextOntologyToFile = false;
        }
//            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
//            String newFileName = Useful.getPropValue("ONTOLOGIES_DIR") + "ontology_"+timeStamp + ".owl";
//            // make a copy of the curr model, so that i can save this without affecting another thread
//            JOptionPane.showMessageDialog(null, "About to copy model, next step is to write to file");
//            OntModel copyOfCurrModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, currentModel);
//            JOptionPane.showMessageDialog(null, "Finished copying model, next step is to write to file");
//            try {
//                copyOfCurrModel.write(new FileOutputStream(newFileName));
//            } catch (FileNotFoundException ex) {
//                JOptionPane.showMessageDialog(null, "Exception thrown in writing to file"+ex.getMessage());
//                Logger.getLogger(JenaInterface.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (Exception e) {
//                JOptionPane.showMessageDialog(null, "Exception thrown in writing to file"+e.getMessage());
//            }
//            JOptionPane.showMessageDialog(null, "Just finished saving model, saved to \n\n\n"+newFileName);
//            
//        }
    }

    /**
     * Utility used to clean unit type data obtained from the BWAPI.
     *
     * @param rawString	an unprocessed string obtained by the BWAPI
     * @return rawString	the processed string to be added to the model
     */
    private String processUnitTypeString(String rawString) {
        rawString = rawString.replaceAll("\\s", "");
        rawString = rawString.replace("Terran", "");
        rawString = rawString.replace("SiegeMode", "");
        rawString = rawString.replace("TankMode", "");
        rawString = rawString.replace("SpiderMine", "");
        return rawString;
    }

    /**
     * Writes currentModel to a file in RDF/XML format. Note that some OWL
     * axioms are not supported by Jena and will not be printed properly. Refer
     * to reported errors for any issues
     */
    public void outputToFile() throws Exception {
        FileOutputStream fout = new FileOutputStream("output_model.owl");
        currentModel.write(fout);
    }

    /**
     * Utility to take a resource string and return a Resource object. Obtained
     * from:
     * https://github.com/castagna/jena-examples/blob/master/src/main/java/org/apache/jena/examples/ExampleDataTypes_01.java
     *
     * @param localname	the string representation of a Resource in currentModel
     * @return Resource	a resource object
     */
    private static Resource r(String localname) {
        return ResourceFactory.createResource(NS + localname);
    }

    /**
     * Utility to take a property string and return a Property object. Obtained
     * from:
     * https://github.com/castagna/jena-examples/blob/master/src/main/java/org/apache/jena/examples/ExampleDataTypes_01.java
     *
     * @param localname	the string representation of a Property in currentModel
     * @return Property	a property object
     */
    private static Property p(String localname) {
        return ResourceFactory.createProperty(NS, localname);
    }

    /**
     * Utility to take a literal value and return a Literal object. Obtained
     * from:
     * https://github.com/castagna/jena-examples/blob/master/src/main/java/org/apache/jena/examples/ExampleDataTypes_01.java
     *
     * @param value	the value of a Literal to be added to currentModel
     * @return Literal	a literal object
     */
    private static Literal l(Object value) {
        return ResourceFactory.createTypedLiteral(value);
    }

    /**
     * Utility to take a literal string and return a Literal object. Obtained
     * from:
     * https://github.com/castagna/jena-examples/blob/master/src/main/java/org/apache/jena/examples/ExampleDataTypes_01.java
     *
     * @param lexicalform	the string representation of a Literal in currentModel
     * @param datatype	the datatype of the String
     * @return Resource	a literal object
     */
    private static Literal l(String lexicalform, RDFDatatype datatype) {
        return ResourceFactory.createTypedLiteral(lexicalform, datatype);
    }
}
