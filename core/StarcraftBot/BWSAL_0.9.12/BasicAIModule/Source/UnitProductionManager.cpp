#include "UnitProductionManager.h"
#include <string>

/* Authors: Dustin Dannenhauer
Pete Biencourt

Description: This class is ensures that we are constantly building the proper ratios of the
units we are building.
*/


UnitProductionManager::UnitProductionManager(BuildOrderManager * buildOrderManager)
{
	this->buildOrderManager = buildOrderManager;
	bool finishedLastBuild = false;
}


UnitProductionManager::~UnitProductionManager(void)
{
}

// Given of map specifying how many of each type of unit to produce,
// issues build orders to the build order manager. This should only be called
// to give a new goal (to change the ratio) not to increase or refresh
void UnitProductionManager::newGoal(std::map<BWAPI::UnitType, int> composition)
{
	/*MessageBoxA(NULL,
			this->getDisplayString().c_str(),
			"Just got new",
			MB_ICONINFORMATION|MB_OK);*/

	this->composition = composition;
	//this->buildOrderManager->clear();
	//this->unitsCompleted.clear();
	//int count = 0;
	int bunker_priority=60;
	for (std::map<BWAPI::UnitType, int>::const_iterator i = composition.begin(); 
		i != composition.end(); i++)
	{
		DebugPrinter::printDebug("Adding units to BOM");
		BWAPI::Broodwar->sendText("added %d %s to build order", i->second, i->first.getName().c_str());
		if(i->first ==BWAPI::UnitTypes::Terran_Bunker)
		{
			//BWTA::getNearestChokepoint();
			BWAPI::TilePosition sl = (BWAPI::Broodwar->self()->getStartLocation());
			//find the all Baelocations
			/*std::set<BWTA::BaseLocation*> bl = (BWTA::getBaseLocations());
			BOOST_FOREACH (BWTA::BaseLocation* u, bl) {
				if(u->getRegion()->getCenter().x()
			}*/
			//BWAPI::TilePosition a= BWAPI::TilePosition(1000,1000);
			BWAPI::Position a = BWAPI::Position(sl);
			
			int ax = a.x();
			int ay = a.y();
			if(ax>1850){
				ay+=75;
			}
			else{
				ay-=80;
				ax+=50;
			}
			
			BWTA::Chokepoint* nearestChokePoint = BWTA::getNearestChokepoint(a);
			BWAPI::TilePosition f= BWAPI::TilePosition((*nearestChokePoint).getCenter());
			int xx = f.x();
			int yy =0;
			if(xx>50){
				 yy=f.y()-11;
				 xx-=10;
			}
			else{
				xx+=10;
				yy = f.y()+11;
			}

			BWAPI::Broodwar->sendText("The x is %d and y before is %d   after is %d ", xx, f.y(), yy );
			BWAPI::TilePosition f1= BWAPI::TilePosition(xx,yy);
			this->buildOrderManager->buildAdditional(i->second, i->first, bunker_priority,f1);
			bunker_priority -=6;
		}
		else{
		this->buildOrderManager->buildAdditional(i->second, i->first, 50);
		}
		//this->expectedCounts[i->first] += i->second;
		//this->unitsCompleted[i->first] = 0;
		//count++;
		//BWAPI::Broodwar->sendText("Just decremented count");
	}
}

// DEPRECATED
bool UnitProductionManager::readyToRefresh() {
	for (std::map<BWAPI::UnitType, int>::const_iterator i = composition.begin(); 
		i != composition.end(); i++)
	{
		//BWAPI::Broodwar->sendText("BOM gPC at %d while ePC at %d", this->buildOrderManager->getPlannedCount(i->first),this->expectedCounts[i->first]);
		if (this->unitsCompleted[i->first] != this->expectedCounts[i->first]) {
			// if we are still waiting on some units, return false
			return false;
		}
	}

	// as long as composition has at least 1 entry, we are ready to refresh
	return composition.size() != 0;
	//return true;
}

void UnitProductionManager::onUnitComplete(BWAPI::Unit* unit) {
	this->unitsCompleted[unit->getType()] += 1;
}

// DEPRECATED
// refresh goal is just like new goal except it doesn't clear the buildOrderManager,
// this is important because other build orders (i.e. for buildings or workers) will be
// deleted. This is just to build more of the same ratio
void UnitProductionManager::refresh() {
	for (std::map<BWAPI::UnitType, int>::const_iterator i = composition.begin(); 
		i != composition.end(); i++)
	{
		DebugPrinter::printDebug("Adding units to BOM");
		this->buildOrderManager->buildAdditional(i->second, i->first, 50);
		this->expectedCounts[i->first] = i->second;
		this->unitsCompleted[i->first] = 0;
	}
}


void UnitProductionManager::update() {

	// count number of barracks
	int barracksCount = 0;
	BOOST_FOREACH (BWAPI::Unit* u, BWAPI::Broodwar->self()->getUnits()) {
		if (u->getType() == BWAPI::UnitTypes::Terran_Barracks) {
			barracksCount += 1;
		}
	}
	int barracksQueued = this->buildOrderManager->getPlannedCount(BWAPI::UnitTypes::Terran_Barracks);
	if (BWAPI::Broodwar->self()->minerals() >= 300) {
		//std::string msg = "barracks count = "+ SSTR(barracksCount) + " and barracks queued = " + SSTR(barracksQueued);
		//BWAPI::Broodwar->sendText(msg.c_str());
		if (barracksCount == 1 && barracksQueued == 1) {
			this->buildOrderManager->buildAdditional(1,BWAPI::UnitTypes::Terran_Barracks, 100);
		}
	}		

	// only call this once every 25 frames to ensure we don't spam order marines
	/*if (BWAPI::Broodwar->self()->minerals() >= 1200 && BWAPI::Broodwar->getFrameCount() % 25 == 0) {
		this->buildOrderManager->buildAdditional(1,BWAPI::UnitTypes::Terran_Marine, 60);
	}*/


	// if we have plenty of resources, build another barracks

	/*if (BWAPI::Broodwar->self()->minerals() > (barracksCount + barracksQueued) * 600) {
	this->buildOrderManager->buildAdditional(1,BWAPI::UnitTypes::Terran_Barracks, 60);
	}*/

	//// check to refresh
	//if (readyToRefresh()) {
	//	BWAPI::Broodwar->sendText("Ready to refresh");
	//	//refresh();
	//}
}
