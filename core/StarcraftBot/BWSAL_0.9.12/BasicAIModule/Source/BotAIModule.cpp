#include "BotAIModule.h"
#include "../Addons/Util.h"
#include <iostream>
#include <fstream>
#include <sstream>
//#define _WINSOCKAPI_
//#include <Windows.h>
#include <ctime>
#include "GamestateDumper.h"
//#include "PlanExecutor.h"
#define DEBUG_DRAW
//using namespace BWAPI;

void drawTerrain();
void drawRegionNumbers(std::map<BWTA::Region*, int>);

BotAIModule::BotAIModule()  {
	assignedWorkerToAttack = false;
	//nPort = 64321;
	//gameStateDumperSettingsFileName = "C:\\Users\\dustin\\Documents\\GitHub\\hierarchical-gda\\core\\settings.properties";
	//nPort = 64322;
	//gameStateDumperSettingsFileName =  "C:\\Users\\dustin\\workspaceTemp\\hierarchical-gda\\core\\settings.properties";

	//log(" ==== New Game Starting @ %s ==== \n", currentDateTime());

}

void BotAIModule::onStart()
{
	bool assignedWorkerToAttack = false;
	//MessageBoxA(NULL,"Is this working?","Test",0);
	//activateLogger();
	log(" ======================================================================= \n\n");
	log("        New Game Starting @ %s \n", currentDateTime().c_str());
	log("\n ======================================================================= ");

	
	currFrame = 0;
	pullFrequency = 20;
	// THIS DISPLAYS THE CURRENT DIRECTORY
	/*TCHAR pwd[MAX_PATH];
	GetCurrentDirectory(MAX_PATH,pwd);
	MessageBox(NULL,pwd,pwd,0);*/

	if (BWAPI::Broodwar->isReplay()) return;
	// Enable some cheat flags
	BWAPI::Broodwar->enableFlag(BWAPI::Flag::UserInput);
	//Broodwar->enableFlag(Flag::CompleteMapInformation);

	BWAPI::Broodwar->setLocalSpeed(20);

	// Run BWTA
	BWTA::readMap();
	BWTA::analyze();

	this->analyzed=true;

	// get information about our bot
	BWAPI::Race race = BWAPI::Broodwar->self()->getRace();
	BWAPI::Race enemyRace = BWAPI::Broodwar->enemy()->getRace();
	BWAPI::UnitType workerType=race.getWorker();
	double minDist;
	BWTA::BaseLocation* natural=NULL;
	BWTA::BaseLocation* home=BWTA::getStartLocation(BWAPI::Broodwar->self());
	for(std::set<BWTA::BaseLocation*>::const_iterator 
		b=BWTA::getBaseLocations().begin();b!=BWTA::getBaseLocations().end();b++)
	{
		if (*b==home) continue;
		double dist=home->getGroundDistance(*b);
		if (dist>0)
		{
			if (natural==NULL || dist<minDist)
			{
				minDist=dist;
				natural=*b;
			}
		}
	}

	// Create managers
	this->buildManager       = new BuildManager(&this->arbitrator);
	this->techManager        = new TechManager(&this->arbitrator);
	this->upgradeManager     = new UpgradeManager(&this->arbitrator);
	this->workerManager      = new WorkerManager(&this->arbitrator);
	this->supplyManager      = new SupplyManager();
	this->baseManager        = new BaseManager();
	this->buildOrderManager  = new BuildOrderManager(this->buildManager,this->techManager,
		this->upgradeManager,this->workerManager,this->supplyManager);

	this->scoutManager       = new ScoutManager(&this->arbitrator);

	this->informationManager = InformationManager::create();
	this->borderManager      = BorderManager::create();
	this->unitGroupManager   = UnitGroupManager::create();


	this->supplyManager->setBuildManager(this->buildManager);
	this->supplyManager->setBuildOrderManager(this->buildOrderManager);
	this->techManager->setBuildingPlacer(this->buildManager->getBuildingPlacer());
	this->upgradeManager->setBuildingPlacer(this->buildManager->getBuildingPlacer());

	this->workerManager->setBaseManager(this->baseManager);
	this->workerManager->setBuildOrderManager(this->buildOrderManager);
	//MessageBoxA(NULL,"Is this working?","Test",0);
	this->baseManager->setBuildOrderManager(this->buildOrderManager);
	this->baseManager->setBorderManager(this->borderManager);
	
	this->unitProductionManager		   = new UnitProductionManager(this->buildOrderManager);
	this->expandManager = new ExpandManager(this->buildOrderManager,this->baseManager);
	this->buildOrderManager->enableDependencyResolver();
	if (race == BWAPI::Races::Terran)
	{
		this->buildOrderManager->build(5,workerType,90);
		this->buildOrderManager->build(2,BWAPI::UnitTypes::Terran_Barracks, 70);
		this->buildOrderManager->build(1,BWAPI::UnitTypes::Terran_Factory,38);
		this->buildOrderManager->build(1,BWAPI::UnitTypes::Terran_Factory,35);

	}
	else // we are not Terran, alert user
	{
		std::string wrongRace = "Undefined behavior when bot does not play as Terran, bye!";
		BWAPI::Broodwar->sendText("%s",wrongRace.c_str());
	}

	this->workerManager->enableAutoBuild();
	this->workerManager->setAutoBuildPriority(80);
	this->baseManager->setRefineryBuildPriority(30);


	// set up the game state dumper
	gsDumper.setup(this->analyzed);
	// connect to GDA server in Java

	this->planExecutor = new PlanExecutor(&this->arbitrator, this->unitProductionManager,this->expandManager, this->baseManager);

	//PlanExecutor planExecutor = PlanExecutor(&this->arbitrator);
	log("\nJust gave PlanExecutor arbitrator of %s from BotAIModule.cpp", this->arbitrator);

	// begin building marines continously
	//std::map<BWAPI::UnitType, int> continousBuildGoal;
	//continousBuildGoal[BWAPI::UnitTypes::Terran_Marine] = 4;
	//this->UnitProductionManager->newGoal(continousBuildGoal);
	//	log("Exiting onStart()\n");

	// Micro from UAlbertaBot
	//if (Options::Modules::USING_MICRO_SEARCH)
	//{
	//	Search::StarcraftData::init();
	//	MicroSearch::Hash::initHash();
	//	//micro.onStart();
	//}
	BWAPI::Broodwar->sendText("LUIGi FTW!!!!!");
}

BotAIModule::~BotAIModule() {}

void BotAIModule::onEnd(bool isWinner)
{
	std::string didWeWin;
	if (isWinner == NULL) {
		didWeWin = "We Lost";
	}else{
		didWeWin = isWinner ? "We Won" : "We Lost";
	}

	log("\nGame over (%s)\n",didWeWin);
	gsDumper.finish();
	closeLogger();
	// close connection to GDA server in java
	this->planExecutor->closePlannerServerConnection();
	//MessageBoxA(NULL,"Game ended, just closed socket connection","Test",0);
}
void BotAIModule::onFrame()
{
	// if there is an enemy worker in our base, attack it with one of our
	// workers who is currently mining minerals
	///	BOOST_FOREACH (BWAPI::Unit* enUn, Broodwar->getPlayer(1))

	// for all my workers, if there is an enemy worker in a 1000 radius, attack it
	
	//BOOST_FOREACH (BWAPI::Unit* myUnit, BWAPI::Broodwar->self()->getUnits()) {
	//	if (myUnit->getType().isWorker() && myUnit->isUnderAttack()) {
	//		BOOST_FOREACH (BWAPI::Unit* enemyUnit, BWAPI::Broodwar->getPlayer(1)->getUnits()) {
	//			if (enemyUnit->getRegion() == myUnit->getRegion()) {
	//				myUnit->attack(enemyUnit);
	//				BWAPI::Broodwar->sendText("Just issued worker attack");
	//				//this->arbitrator->setBid(this,myUnit, 150);
	//				assignedWorkerToAttack = true;
	//			}
	//		}
	//	}
	//}

	//drawBases();
	drawTerrain();	
	drawRegionNumbers(this->gsDumper.getRegionIDs());
	// show current mouse position (ONLY IN THE FRAME)
	//std::string mpos = "SCREEN: "+SSTR(Broodwar->getScreenPosition().x()) + "," + SSTR(Broodwar->getScreenPosition().y())+ " MOUSE IS: "+SSTR(Broodwar->getMousePosition().x())+","+SSTR(Broodwar->getMousePosition().y());
	//Broodwar->sendText(mpos.c_str());


	//	log("entering onFrame()\n");
	if (BWAPI::Broodwar->isReplay()) return;
	if (!this->analyzed) return;
	currFrame++;

	if (currFrame == 200) {
		this->planExecutor->beginAskingForPlanSteps();
		//this->planExecutor.beginAskingForPlanSteps();
	}

	if (currFrame % pullFrequency == 0) {
		gsDumper.dumpGameState();
		//this->planExecutor.update(); // for now this calls request next plan step
	}

	if (BWAPI::Broodwar->getFrameCount()>24*50)
		scoutManager->setScoutCount(1);

	this->buildManager->update();
	this->buildOrderManager->update();
	this->baseManager->update();
	this->workerManager->update();
	this->techManager->update();
	this->upgradeManager->update();
	this->supplyManager->update();
	this->borderManager->update();
	this->arbitrator.update();
	this->unitProductionManager->update();
	this->planExecutor->update();
	this->scoutManager->update();
	//this->planExecutor.update();
	//	log("exiting onFrame()\n");

	this->planExecutor->displayPlanSteps();

	/*if (this->planExecutor->getCurrPlanStep() != NULL) {
		Broodwar->drawTextScreen(90,17,"\x07 CurrPlanStep: \x1B%s",this->planExecutor->getCurrPlanStep()->getDisplayString().c_str());
	}*/



	//if (Options::Modules::USING_MICRO_SEARCH)
	//{
	//	//micro.update();
	//}
}

void BotAIModule::onUnitDestroy(BWAPI::Unit* unit)
{
	if (BWAPI::Broodwar->isReplay()) return;
	//Broodwar->sendText("Unit %d was destroyed",unit->getID());
	this->arbitrator.onRemoveObject(unit);
	this->buildManager->onRemoveUnit(unit);
	this->techManager->onRemoveUnit(unit);
	this->upgradeManager->onRemoveUnit(unit);
	this->workerManager->onRemoveUnit(unit);
	this->scoutManager->onRemoveUnit(unit);
	//this->defenseManager->onRemoveUnit(unit);
	this->informationManager->onUnitDestroy(unit);
	this->baseManager->onRemoveUnit(unit);
}

void BotAIModule::onUnitDiscover(BWAPI::Unit* unit)
{
	if (BWAPI::Broodwar->isReplay()) return;
	//Broodwar->sendText("Unit %d was discovered",unit->getID());
	this->informationManager->onUnitDiscover(unit);
	this->unitGroupManager->onUnitDiscover(unit);
}

void BotAIModule::onUnitComplete(BWAPI::Unit* unit) {
	if (!unit->getType().isWorker() && !unit->getType().isBuilding()) {
		// if the completed unit is not a worker or building, tell goal manager
		this->unitProductionManager->onUnitComplete(unit);
	}
}

void BotAIModule::onUnitEvade(BWAPI::Unit* unit)
{
	if (BWAPI::Broodwar->isReplay()) return;
	//Broodwar->sendText("Unit %d evaded",unit->getID());
	this->informationManager->onUnitEvade(unit);
	this->unitGroupManager->onUnitEvade(unit);
}

void BotAIModule::onUnitMorph(BWAPI::Unit* unit)
{
	if (BWAPI::Broodwar->isReplay()) return;
	//Broodwar->sendText("Unit %d morphed",unit->getID());
}
void BotAIModule::onUnitRenegade(BWAPI::Unit* unit)
{
	if (BWAPI::Broodwar->isReplay()) return;
	//Broodwar->sendText("Unit %d is now a renegade",unit->getID());
	this->unitGroupManager->onUnitRenegade(unit);
}

void BotAIModule::onSendText(std::string text)
{
	BWAPI::Broodwar->sendText("%s",text.c_str());
	return;
}

/** Functions for drawing the terrain **/
//void drawBases()
//{
//	//we will iterate through all the base locations, and draw their outlines.
//	// Our bases
//	for(std::map<BWTA::BaseLocation*, BWAPI::TilePosition>::const_iterator i=informationManager->_ourBases.begin();i!=informationManager->_ourBases.end();++i) {
//		TilePosition p = i->first->getTilePosition();
//		Broodwar->drawBox(CoordinateType::Map,p.x()*TILE_SIZE,p.y()*TILE_SIZE,p.x()*TILE_SIZE+4*TILE_SIZE,p.y()*TILE_SIZE+3*TILE_SIZE,Colors::Green,false);
//	}
//	// Mineral patch
//	workerManager->_mineralsExploitation;
//	for(ResourceToWorkerMap::const_iterator j = workerManager->_mineralsExploitation.begin(); j != workerManager->_mineralsExploitation.end(); ++j) {
//		Position q=j->first->getPosition();
//		Broodwar->drawCircleMap(q.x(),q.y(),30,Colors::Cyan,false);
//		Broodwar->drawTextMap(q.x(),q.y()-5,"%d", j->second);
//	}
//
//	// Empty bases
//	for(std::set<BWTA::BaseLocation*>::const_iterator i=informationManager->_emptyBases.begin();i!=informationManager->_emptyBases.end();++i) {
//		TilePosition p=(*i)->getTilePosition();
//		Broodwar->drawBox(CoordinateType::Map,p.x()*TILE_SIZE,p.y()*TILE_SIZE,p.x()*TILE_SIZE+4*TILE_SIZE,p.y()*TILE_SIZE+3*TILE_SIZE,Colors::Yellow,false);
//	}
//
//	// Ignore bases
//	for(std::set<TilePosition>::const_iterator i=informationManager->_ignoreBases.begin();i!=informationManager->_ignoreBases.end();++i) {
//		TilePosition p=(*i);
//		Broodwar->drawBox(CoordinateType::Map,p.x()*TILE_SIZE,p.y()*TILE_SIZE,p.x()*TILE_SIZE+4*TILE_SIZE,p.y()*TILE_SIZE+3*TILE_SIZE,Colors::Orange,false);
//	}
//
//	// Enemy bases
//	for(std::set<BWTA::BaseLocation*>::const_iterator i=informationManager->_enemyBases.begin();i!=informationManager->_enemyBases.end();++i) {
//		TilePosition p=(*i)->getTilePosition();
//		Broodwar->drawBox(CoordinateType::Map,p.x()*TILE_SIZE,p.y()*TILE_SIZE,p.x()*TILE_SIZE+4*TILE_SIZE,p.y()*TILE_SIZE+3*TILE_SIZE,Colors::Red,false);
//	}
//}

void drawTerrain() 
{
	//we will iterate through all the regions and ...
	const std::set<BWTA::Region*> &regions = BWTA::getRegions();
	for(std::set<BWTA::Region*>::const_iterator r = regions.begin(); r != regions.end(); ++r)
	{
		// Draw the polygon outline of it in green
		const BWTA::Polygon &p=(*r)->getPolygon();
		for(int j=0;j<(int)p.size();j++)
		{
			BWAPI::Position point1=p[j];
			BWAPI::Position point2=p[(j+1) % p.size()];
			BWAPI::Broodwar->drawLine(BWAPI::CoordinateType::Map,point1.x(),point1.y(),point2.x(),point2.y(),BWAPI::Colors::Green);
		}

		// Draw the chokepoints with yellow lines
		const std::set<BWTA::Chokepoint*> &chokepoints = (*r)->getChokepoints();
		for(std::set<BWTA::Chokepoint*>::const_iterator c = chokepoints.begin(); c != chokepoints.end(); ++c)
		{
			const BWAPI::Position &point1=(*c)->getSides().first;
			const BWAPI::Position &point2=(*c)->getSides().second;
			BWAPI::Broodwar->drawLine(BWAPI::CoordinateType::Map,point1.x(),point1.y(),point2.x(),point2.y(),BWAPI::Colors::Yellow);
		}
	}
}

void drawRegionNumbers(std::map<BWTA::Region*, int> regionIDMap) {

	//we will iterate through all the regions and 
	for (std::map<BWTA::Region*,int>::iterator r = regionIDMap.begin(); r != regionIDMap.end(); r++) {
		BWAPI::Broodwar->drawText(BWAPI::CoordinateType::Map,(*r).first->getCenter().x(), (*r).first->getCenter().y(), std::string("Region_").append(SSTR((*r).second)).c_str());
	}
}