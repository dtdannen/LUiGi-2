#include "ExpandManager.h"
#include <string>

/* Authors: Xiang Gao

*/


ExpandManager::ExpandManager(BuildOrderManager * buildOrderManager, BaseManager * baseManager)
{
	this->buildOrderManager = buildOrderManager;
	this->baseManager = baseManager;
	bool finishedLastBuild = false;
	this->allBases=baseManager->getAllBases();
	this->location2base=baseManager->getL2B();
}


ExpandManager::~ExpandManager(void)
{
}

//Expand a specific region. For now one BWTA region only contains one baseLocation.
//if contains more, we should decide whether we can build there
bool ExpandManager::newGoal(BWTA::Region* region)
{
	/*MessageBoxA(NULL,
			this->getDisplayString().c_str(),
			"Just got new",
			MB_ICONINFORMATION|MB_OK);*/

	
	    int count=0;
	    std::set<BWTA::BaseLocation*> b1= (region)->getBaseLocations();
		DebugPrinter::printDebug("Adding Command_Center to BaseManager");
		BWAPI::Broodwar->sendText("Building New Command_Center in region: ( %f  ,  %f )",region->getCenter().x(), region->getCenter().y());
		for (std::set<BWTA::BaseLocation*>::const_iterator b =b1.begin();
			b != b1.end(); b++){
				
		this->baseManager->expand(*b,60);
		count++;
		if(count>1) break;
		}
//Do we need to wait?
		this->allBases=baseManager->getAllBases();
		this->location2base=baseManager->getL2B();
		

	
	
}
bool ExpandManager::newGoal(BWAPI::Position position){

	DebugPrinter::printDebug("Adding Command_Center to BaseManager");
		BWAPI::Broodwar->sendText("Building New Command_Center");
		this->baseManager->expand();
	this->allBases=baseManager->getAllBases();
	this->location2base=baseManager->getL2B();
	return true;
}
//new goal for destination address
void ExpandManager::newGoal(){

	DebugPrinter::printDebug("Adding Command_Center to BaseManager");
		BWAPI::Broodwar->sendText("Building New Command_Center by location x,y");
		this->baseManager->expand(20);
	this->allBases=baseManager->getAllBases();
	this->location2base=baseManager->getL2B();
}
/*
void UnitProductionManager::newExpanSionGoal(BWAPI::TilePosition position)
{
	

	BWAPI::Broodwar->sendText("Expand! Build Base at ( %d , %d )",position.x(),position.y());
	this->buildOrderManager->buildAdditional(1, BWAPI::UnitTypes::Terran_Command_Center, 70, position);
		
		
	
}
*/








void ExpandManager::update() {

	
}
