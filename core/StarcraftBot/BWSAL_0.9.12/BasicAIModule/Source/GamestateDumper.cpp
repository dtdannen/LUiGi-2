// This class encapsulates the functionalities needed to capture game state data in the form
// of an ontology and output the data in JSON format

#include "GamestateDumper.h"
//#include "../Addons/Util.h"
//#include <iostream>
//#include <fstream>
//#include <Windows.h>
#include <ctime>
using namespace BWAPI;
using namespace boost;

// must have absolute path because the bot sits in the starcraft directory, not the project dir
//std::string settingsFileName = "C:\\Users\\Dustin\\Documents\\GitHub\\hierarchical-gda\\core\\settings.properties";
std::string settingsFileName = "C:\\Users\\Gary\\Desktop\\AIOntology\\settings.properties"; // non hierarchical


std::map<BWTA::Region*,int> GamestateDumper::getRegionIDs() {
	return this->regionIDs;
}

// - called during the onStart() method in an agent
// - IMPORTANT - this can only be run after BWTA finishes analzing the map
void GamestateDumper::setup(bool doneAnalyzing) {

	

	// logging setup
	errorLogFilename = "error-log.txt";
	errorLogOutputFile.open(errorLogFilename.c_str(), std::fstream::ate);
	errorLogOutputFile << "[NEW GAME] Start time is "+ currentDateTime() + "\n";
	errorLogOutputFile << "[setup()] Beginning of method\n";
	errorLogOutputFile.flush();

	// leave off file extension because curr frame will be added
	//dataFilename = "C:\\Users\\dustin\\Desktop\\Current Projects\\StarcraftObserver\\GameStateOutput\\gameStateData-JSON-"; 
	//dataFilenamePrefix = "C:\\Users\\dustin\\Documents\\GitHub\\hierarchical-gda\\aiide-ws-2013\\botNo3rdParty\\GameStateOutput\\gameStateData-JSON-"; 
	//logFilename = "C:\\Users\\dustin\\Documents\\GitHub\\hierarchical-gda\\aiide-ws-2013\\botNo3rdParty\\GameStateOutput\\log.txt"; 
	//Broodwar->setLocalSpeed(200);
	dataFilenamePrefix = this->getPropValue("GAMESTATE_DIR_ABS");
	//boost::replace_all(dataFilenamePrefix, "\\","\\\\");
	dataFilenamePrefix += "\\gameStateData-JSON-";

	logFilename = this->getPropValue("GAMESTATE_LOG_FILE_ABS");
	boost::replace_all(dataFilenamePrefix, "\\","\\\\");

	// get information on regions once BWTA is finished analyzing
	if (doneAnalyzing) {
		int currRegionID = 0;
		for (std::set<BWTA::Region*>::const_iterator r = BWTA::getRegions().begin();
			r != BWTA::getRegions().end(); r++) {
				regionIDs.insert(std::make_pair((*r), currRegionID));
				currRegionID++;
		}
	}

	// get information on chokepoints
	if (doneAnalyzing) {
		int currChokeID = 0;
		for (std::set<BWTA::Chokepoint*>::const_iterator c = BWTA::getChokepoints().begin();
			c != BWTA::getChokepoints().end(); c++) {
				chokePointIDs.insert(std::make_pair((*c), currChokeID));
				currChokeID++;
		}
	}

	//Broodwar->sendText("Map Height is %d",Broodwar->mapHeight()); // 96 * 32
	//Broodwar->sendText("Map Width is %d",Broodwar->mapWidth()); // 128 * 32
	Broodwar->sendText("Good Luck!!"); // 128 * 32
	// create a stream for error logging - to help with debugging
	// then update the log file

	errorLogOutputFile << "[setup()] end of method\n";
	errorLogOutputFile.flush();
}

// - called anytime the player wishes to dump gamestate data
void GamestateDumper::dumpGameState() {
	
	//Broodwar->sendText(("datafileprefix: "+dataFilenamePrefix).c_str());
	errorLogOutputFile << "[pullData()] beginning of method\n";
	errorLogOutputFile.flush();

	std::ofstream outputFile;
	std::string dataFilename = dataFilenamePrefix + currentDateTime() + ".txt";
	outputFile.open(dataFilename.c_str());
	
	std::string output = "";

	// Game data
	output += "{\"gameID\":\"0\",\n";
	output += " \"mapName\":\"" + Broodwar->mapFileName() + "\",\n";
	output += " \"mapWidth\":" + SSTR(Broodwar->mapWidth()*TILE_SIZE) + ",\n";
	output += " \"mapHeight\":" + SSTR(Broodwar->mapHeight()*TILE_SIZE) + ",\n";
	output += " \"elapsedTime\":\"" + SSTR(Broodwar->elapsedTime()) + "\",\n";

	errorLogOutputFile << "[pullData()] pulled gamestate data\n";
	errorLogOutputFile.flush();

	// Player data
	output += " \"players\":[\n";

	// remove the last comma (-2 for \n and the ,) then add the newline back
	// output = output.substr(0, output.length()-2) + "\n";
	// output += "\n               ],\n"; // ends the myUnits array
	// Units data for the units that our player sees but doesn't control
	// output += "     \"enemyUnits\":[\n";

	BWAPI::Player* me = Broodwar->self();

	// BWAPI::Player* ePlayer;

	bool currPlayerHasUnit = false;

	// loop over all the players and find the one that is an enemy and not neutral
	int count = 0; // this is hacky...ewww - but I don't know how to turn an iterator into player 
	// object
	int numPlayers = Broodwar->getPlayers().size();
	for (int i = 0; i < numPlayers; i++) {
		BWAPI::Player* currPlayer = Broodwar->getPlayer(i);
		if (!currPlayer->isNeutral()) {

			if (currPlayer == Broodwar->self()) {
				// I am always player 0
				output += "    {\"playerId\":\"0\",\n";
				// player score
				output += "       \"killScore\":"+ SSTR(Broodwar->self()->getKillScore()) + ",\n";
				output += "       \"unitScore\":"+ SSTR(Broodwar->self()->getUnitScore()) + ",\n";
				output += "       \"spentMinerals\":"+ SSTR(Broodwar->self()->spentMinerals()) + ",\n";
				output += "       \"spentGas\":"+ SSTR(Broodwar->self()->spentGas()) + ",\n";
				output += "       \"supplyTotal\":"+ SSTR(Broodwar->self()->supplyTotal()) + ",\n";
				output += "       \"razingScore\":"+ SSTR(Broodwar->self()->getRazingScore()) + ",\n";

			}else{
				// Enemy is always player 1
				output += "    {\"playerId\":\"1\",\n";
			}

			

			if (currPlayer->getUnits().size() == 0) {
				output = output.substr(0, output.length()-2) + "},\n";
			}else{

				// Units data for the units our player controls
				output += "     \"myUnits\":[\n";

				std::set<BWAPI::Unit*>::const_iterator u = currPlayer->getUnits().begin();
				while (u != currPlayer->getUnits().end()) {

					currPlayerHasUnit = true;


					output += "         {\"unitID\":\"" + SSTR((*u)->getID());
					// output += "\",\"unitTypeID\":\"" + SSTR((*u)->getType().getID());
					output += "\",\"unitType\":\"" + (*u)->getType().getName();
					output += "\",\"currentHitPoints\":\"" + SSTR((*u)->getHitPoints());
					output += "\",\"maxHitPoints\":\"" + SSTR((*u)->getType().maxHitPoints());
					output += "\",\"isBeingAttacked\":\"" + SSTR((*u)->isUnderAttack());
					output += "\",\"x\":\"" + SSTR((*u)->getPosition().x());
					output += "\",\"y\":\"" + SSTR((*u)->getPosition().y());
					int rID = getClosestRegionId((*u)->getPosition());
					output += "\",\"regionID\":\"" + SSTR(rID);
					output += "\",\"armor\":\"" + SSTR((*u)->getType().armor()); // un-upgraded armor
					output += "\",\"mineralCost\":\"" + SSTR((*u)->getType().mineralPrice());
					output += "\",\"gasCost\":\"" + SSTR((*u)->getType().gasPrice());
					output += "\"},\n";	

					u++;
				}
				// remove the last comma (-2 for \n and the ,) then add the newline back
				if (currPlayerHasUnit) {
					output = output.substr(0, output.length()-2) + "\n";
				}
				output += "\n               ]},\n"; // ends the myUnits array
			}
		}

	} // ends the player loop


	output = output.substr(0, output.length()-2) + "\n"; // trim the last comma

	// old version of getting the enemy player
	// BWAPI::Player* ePlayer = Broodwar->getPlayer(1);


	output += "            ],\n"; // ends the players array

	// write to file, flush, and reset the string
	outputFile << output;
	outputFile.flush();
	output = "";

	errorLogOutputFile << "[pullData()] pulled player (including units) data\n";
	errorLogOutputFile.flush();


	// Region data
	output += "\n \"regions\":[\n";
	std::map<BWTA::Region*, int>::const_iterator r = regionIDs.begin();
	while (r != regionIDs.end()) {
		output += "     {\"regionID\":\"" + SSTR((*r).second);
		output += "\",\"regionCenterX\":\"" + SSTR((*r).first->getCenter().x());
		output += "\",\"regionCenterY\":\"" + SSTR((*r).first->getCenter().y());
		output += "\"},\n";
		r++;
	}
	// remove the last comma (-2 for \n and the ,) then add the newline back
	output = output.substr(0, output.length()-2) + "\n";
	output += "\n           ],\n"; // ends the regions array

	errorLogOutputFile << "[pullData()] pulled region data\n";
	errorLogOutputFile.flush();

	// Chokepoint data
	output += "\n \"chokepoints\":[\n";
	//Broodwar->sendText("Dumping game state data, there are %d chokepoints",currFrame);
	std::map<const BWTA::Chokepoint*, int>::const_iterator c = chokePointIDs.begin();
	while (c != chokePointIDs.end()) {
		output += "     {\"chokepointID\":\"" + SSTR((*c).second);
		output += "\",\"chokepointCenterX\":\"" + SSTR((*c).first->getCenter().x());
		output += "\",\"chokepointCenterY\":\"" + SSTR((*c).first->getCenter().y());
		int rID1 = regionIDs[(*c).first->getRegions().first];
		int rID2 = regionIDs[(*c).first->getRegions().second];
		output += "\",\"connectedToRegionOneID\":\"" + SSTR(rID1);
		output += "\",\"connectedToRegionTwoID\":\"" + SSTR(rID2);
		output += "\"},\n";
		c++;
	}

	// remove the last comma (-2 for \n and the ,) then add the newline back
	output = output.substr(0, output.length()-2) + "\n";
	output += "\n               ]\n"; // end choke points array

	errorLogOutputFile << "[pullData()] pulled chokepoint data\n";
	errorLogOutputFile.flush();

	output += "}\n"; // end game object

	errorLogOutputFile << "[pullData()] end of method\n";
	errorLogOutputFile.flush();

	outputFile << output;
	outputFile.close();

	// now update log file with this file name
	// then update the log file
	std::ofstream logOutputFile;
	logOutputFile.open(logFilename.c_str(), std::fstream::ate);
	logOutputFile << "\n" + dataFilename;
	logOutputFile.close();
}

// basically to close all the file ports
void GamestateDumper::finish() {
	errorLogOutputFile.close();
}

// useful for getting date and time
const std::string GamestateDumper::currentDateTime() {
	// code borrowed from:
	// http://social.msdn.microsoft.com/Forums/vstudio/en-US/d621b718-fda9-4b8b-8458-4e303ece1520/help-with-localtimes-and-strftime
	char str[70];
	time_t rawtime;
	struct tm timeinfo;
	time(&rawtime);
	localtime_s(&timeinfo,&rawtime);
	strftime(str, sizeof(str), "%Y-%m-%d---%H-%M-%S", &timeinfo);

	return str;

}

std::string GamestateDumper::getPropValue(std::string key) {
	// borrowed from (and slightly modified):
	// http://www.linuxquestions.org/questions/programming-9/c-equivalent-for-java-properties-class-256340/
	std::string result = "";
	typedef std::map<const std::string,std::string> CfgMap;
	///Broodwar->sendText(("About to open Settings file: "+settingsFileName).c_str());
	std::ifstream settingsFileStream(settingsFileName.c_str());
	//Broodwar->sendText(("Done open Settings file: "+settingsFileName).c_str());
	CfgMap config;
	std::string s;
	while(settingsFileStream.good()) {
		getline(settingsFileStream,s);
		config[s.substr(0,s.find('='))] = s.substr(s.find('=')+1);
	}

	if(config[key].empty()) result = "Setting " + key + " missing\n";
	else result = config[key];

	/*std::cout << "All settings:\n";
	for(CfgMap::iterator i=config.begin();i!=config.end();i++) {
		cout << "Item \"" << i->first << "\" has value \"" << i->second << '\"' << endl ;
	}*/

	// important: close stream
	settingsFileStream.close();

	return result;
}

// Need to rewrite this because there are areas of the map
// which don't belong to any region and if flying units are in those areas, they
// won't belong in any region, soo
// THE FIX: just put them in the nearest region
int GamestateDumper::getClosestRegionId(BWAPI::Position pos) {
	// get the region
	BWTA::Region* reg = BWTA::getRegion(pos);

	// If the pos is not within any region, find the nearest region center and put in that region
	if (reg == NULL) {
		// NOTE: this could cause a very obscure bug where a flying unit may be closer to a region
		// center of a region that is actually farther away

		// unit is not in a region, so loop over all regions to find the nearest region center
		
		int minDist = 10000;
		int currDist = -1;
		BOOST_FOREACH (BWTA::Region* r, BWTA::getRegions()) {
			currDist = r->getCenter().getApproxDistance(pos);
			if (minDist > currDist) {
				minDist = currDist;
				reg = r;
			}
		}
	}

	return regionIDs[reg];
	



}