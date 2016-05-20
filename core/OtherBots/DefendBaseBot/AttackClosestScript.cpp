#include "AttackClosestScript.h"
#include <Windows.h>
using namespace BWAPI;
using namespace boost;

AttackClosestScript::AttackClosestScript() {
	// do nothing
}

AttackClosestScript::AttackClosestScript(std::set<BWAPI::Unit*> unitsForCombat, BWAPI::Position pos) {
	this->unitsForCombat = unitsForCombat;
	this->destination = pos;
}

void AttackClosestScript::update() {
	// find nearby enemy units
	BWAPI::Player* enemy = BWAPI::Broodwar->self()->isEnemy(BWAPI::Broodwar->getPlayer(0)) ?
		BWAPI::Broodwar->getPlayer(0) : BWAPI::Broodwar->getPlayer(1); // get the enemy
	// see if the enemy has any visible units
	std::set<BWAPI::Unit*> nearbyEnemyUnits;
	BOOST_FOREACH (BWAPI::Unit* e, enemy->getUnits()) {
		// if the enemy unit is within 500 distance of this plan's units,
		// include them in battle simulation
		bool alreadyAddedCurrEnemy = false;
		BOOST_FOREACH (BWAPI::Unit* a, this->unitsForCombat) { 
			if (e->getPosition().getApproxDistance(a->getPosition()) < 500 // nearby, or
				|| BWTA::getRegion(a->getPosition().x(),a->getPosition().y()) == BWTA::getRegion(e->getPosition().x(),e->getPosition().y())) { // in same region
					if (!alreadyAddedCurrEnemy) { // prevent duplicate insertions
						nearbyEnemyUnits.insert(e);
						alreadyAddedCurrEnemy = true;
					}
			}
		}
	}

	if (!nearbyEnemyUnits.empty()) {

		// 1. Loop through all of my units
		BOOST_FOREACH (BWAPI::Unit* myUnit, this->unitsForCombat) {

			// if it is a healing unit, nevermind 
			if (myUnit->getType() != BWAPI::UnitTypes::Terran_Medic) {

				// check to see if my unit has a target
				std::map<int,int>::iterator currentTargetIT = attackAssignments.find(myUnit->getID());
				std::pair<int,int> currentTarget;
				BWAPI::Unit* newTarget = NULL;

				if (currentTargetIT != attackAssignments.end()) {
					// my unit DOES have a target
					currentTarget = *currentTargetIT;
					BWAPI::Unit* currEnemyUnit = Broodwar->getUnit(currentTarget.second);

					// if my unit is not alive, remove it from the assignments list
					if (myUnit->getHitPoints() <= 0) {
						attackAssignments.erase(myUnit->getID());
					}else if (currEnemyUnit->getHitPoints() <= 0 || !currEnemyUnit->isVisible() || !currEnemyUnit->exists()){
						// my unit is alive but enemy unit is dead, or invisible, or does not exist
						// so give my unit a new target
						newTarget = findClosestEnemyUnitNOK(myUnit, nearbyEnemyUnits, attackAssignments);

					}else {
						// my unit has a target so do nothing
					}

				}else{
					// My unit does not have a target, find him the nearest enemy
					newTarget = findClosestEnemyUnitNOK(myUnit, nearbyEnemyUnits, attackAssignments);
				}

				if (newTarget != NULL) {
					// order the attack
					myUnit->attack(newTarget);
					// update assignment
					attackAssignments[myUnit->getID()] = newTarget->getID();
					// DEBUG Info
					//Broodwar->sendText("Give my unit %d new attack", myUnit->getID());
				}
			}
		}

		// debug information: draw lines showing who is attacking who
		std::map<int,int>::iterator mapping = attackAssignments.begin();
		while (mapping != attackAssignments.end()) {
			if (Broodwar->getUnit(mapping->first)->getHitPoints() > 0) {
				int x1 = Broodwar->getUnit(mapping->first)->getPosition().x();
				int y1 = Broodwar->getUnit(mapping->first)->getPosition().y();
				int x2 = Broodwar->getUnit(mapping->second)->getPosition().x();
				int y2 = Broodwar->getUnit(mapping->second)->getPosition().y();
				Broodwar->drawLine(BWAPI::CoordinateType::Map,x1,y1,x2,y2,BWAPI::Colors::Red);
			}
			mapping++;

		}
	}else{
		// order each unit to attack move to destination
		BOOST_FOREACH (BWAPI::Unit* myUnit, this->unitsForCombat) {
			if (!myUnit->isMoving() || !myUnit->isAttacking()) {
				//MessageBoxA(NULL, ("Just ordered unit to attack to attack "+SSTR(this->destination.x()) + "," + SSTR(this->destination.y())).c_str(),"TEST MESSAGE",0);
				myUnit->attack(this->destination);
			}
		}
	}
	this->nearbyEnemyUnits = nearbyEnemyUnits;
}

std::set<BWAPI::Unit*> AttackClosestScript::getAvailableUnits() {
	return this->unitsForCombat;
}

// if all our units are dead OR
// if we are in center of region and there are no more enemy units visible
bool AttackClosestScript::finished() {
	bool anyUnitAlive = false;
	BOOST_FOREACH(BWAPI::Unit* myUnit, this->unitsForCombat) {
		if (myUnit->getHitPoints() > 0) {
			anyUnitAlive = true;
		}
	}

	if (!anyUnitAlive) return true; // DONE - we have all died


	bool areWeStillMoving = false;
	// first check to see if we are still moving to center
	BOOST_FOREACH(BWAPI::Unit* myUnit, this->unitsForCombat) {
		if (myUnit->getPosition().getApproxDistance(this->destination) >= 125) {
			areWeStillMoving = true;
		}
	}

	if (areWeStillMoving) return false; // NOT DONE - still moving

	// we have stopped moving, and are still alive, are there still enemies?
	bool anyVisibleEnemyUnit = false;
	BOOST_FOREACH(BWAPI::Unit* enemyUnit, this->nearbyEnemyUnits) {
		if (enemyUnit->getHitPoints() > 0) {
			anyVisibleEnemyUnit = true;
		}
	}

	return !anyVisibleEnemyUnit; // If any visible unit, keep going
}

// returns the nearest enemy unit my unit should now attack
BWAPI::Unit* AttackClosestScript::findClosestEnemyUnitNOK(BWAPI::Unit* myUnit, std::set<BWAPI::Unit*> enemyUnits, std::map<int,int> currentAssignments) {
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

BWAPI::Unit* AttackClosestScript::findClosestEnemyUnit(BWAPI::Unit* myUnit, std::set<BWAPI::Unit*> enemyUnits) {
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

void AttackClosestScript::setUnits( std::set<BWAPI::Unit*> availableUnits )
{
	this->unitsForCombat = availableUnits;
}
