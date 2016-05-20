#include "ExpandAndMine.h"
#include <Windows.h>
using namespace BWAPI;
using namespace boost;

ExpandAndMine::ExpandAndMine() {

}

ExpandAndMine::ExpandAndMine(std::set<BWAPI::Unit*> unitsForCombat, BWAPI::Position pos) {

}

void ExpandAndMine::update() {

	//Sleep(1);
}

void ExpandAndMine::killWorkerUnits() {
	this->targetPriorityWorkerUnit = true;
}



//to check whether new base has already been constructed and worker is gathering minerals
bool ExpandAndMine::finished(int count) {



	bool baseFinished= false;


	bool anyUnitAlive = false;
	int numb_units_alive = 0;
	BOOST_FOREACH(BWAPI::Unit* myUnit, this->unitsForCombat) {
		if (myUnit->getHitPoints() > 0) {
			anyUnitAlive = true;
			numb_units_alive++;
		}else{
			//Broodwar->sendText(("Unit "+SSTR(myUnit->getID())+" has died").c_str());
		}
	}



	if (!anyUnitAlive) return true; // DONE - we have all died


	bool areWeStillMoving = false;
	// first check to see if we are still moving to center
	BOOST_FOREACH(BWAPI::Unit* myUnit, this->unitsForCombat) {
		if (myUnit->getHitPoints() > 0 && myUnit->getPosition().getApproxDistance(this->destination) >= 125) {
			areWeStillMoving = true;
		}
	}

	if (areWeStillMoving) return false; // NOT DONE - still moving

	// we have stopped moving, and are still alive, are there still enemies?
	bool anyVisibleEnemyUnit = false;
	if (this->targetPriorityWorkerUnit) {
		BOOST_FOREACH(BWAPI::Unit* enemyUnit, this->nearbyWorkerEnemyUnits) {
			if (enemyUnit->getHitPoints() > 0) {
				anyVisibleEnemyUnit = true;
			}
		}
	}else{
		BOOST_FOREACH(BWAPI::Unit* enemyUnit, this->nearbyEnemyUnits) {
			if (enemyUnit->getHitPoints() > 0) {
				anyVisibleEnemyUnit = true;
			}
		}
	}


	return !anyVisibleEnemyUnit; // If any visible unit, keep going
}

// returns the nearest enemy unit my unit should now attack
BWAPI::Unit* ExpandAndMine::findClosestEnemyUnitNOK(BWAPI::Unit* myUnit, std::set<BWAPI::Unit*> enemyUnits, std::map<int,int> currentAssignments) {
	int maxNumAttackers = 3;
	// duplicate enemy units set
	std::set<BWAPI::Unit*> enemyUnitsCopy(enemyUnits);
	// remove all enemies that have 3 or more attackers
	bool removeUnitFromCurrentEnemies = false;
	BOOST_FOREACH(BWAPI::Unit* eUC, enemyUnits) {
		int count = 0;
		std::map<int,int>::iterator currAssIT = currentAssignments.begin();
		while (currAssIT != currentAssignments.end()) {
			if (currAssIT->second == eUC->getID()) {
				count++;
				if (count >= maxNumAttackers) {
					//removeUnitFromCurrentEnemies = true;
					if (!enemyUnitsCopy.empty() && enemyUnitsCopy.find(eUC) != enemyUnitsCopy.end()) {
						enemyUnitsCopy.erase(eUC);
					}
					break;
				}
			}
			currAssIT++;
		}

		//if (removeUnitFromCurrentEnemies) {

		//}
	}

	// now find the closest enemy 
	return findClosestEnemyUnit(myUnit, enemyUnitsCopy);
}

BWAPI::Unit* ExpandAndMine::findClosestEnemyUnit(BWAPI::Unit* myUnit, std::set<BWAPI::Unit*> enemyUnits) {
	if (enemyUnits.empty()) {
		return NULL;
	}

	// start off with the first enemy unit in the set as the closest one
	BWAPI::Unit* currEnemyUnit = *(enemyUnits.begin());
	int currDist = currEnemyUnit->getPosition().getApproxDistance(myUnit->getPosition());

	// are there non-building enemies?
	bool areThereNonBuildingEnemies = false;

	BOOST_FOREACH (BWAPI::Unit* u, enemyUnits) {
		if (!u->getType().isBuilding()) {
			areThereNonBuildingEnemies = true;
		}
	}

	// find closest enemy
	BOOST_FOREACH (BWAPI::Unit* e, enemyUnits) {
		if (e->getPosition().getApproxDistance(myUnit->getPosition()) < currDist && e->getHitPoints() > 0) {
			// if there are units that aren't buildings, attack those first
			if (areThereNonBuildingEnemies) {
				// check this unit is not a building, only then assign
				if (!e->getType().isBuilding()) { 
					currEnemyUnit = e;
					currDist = e->getPosition().getApproxDistance(myUnit->getPosition());
				}
			}else{
				// just do the regular thing, consider all units
				currEnemyUnit = e;
				currDist = e->getPosition().getApproxDistance(myUnit->getPosition());
			}
		}
	}

	return currEnemyUnit;
}

void ExpandAndMine::setUnits( std::set<BWAPI::Unit*> availableUnits )
{
	this->unitsForCombat = availableUnits;
}
