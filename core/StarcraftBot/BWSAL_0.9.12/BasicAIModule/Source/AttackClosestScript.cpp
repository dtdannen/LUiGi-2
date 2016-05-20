#include "AttackClosestScript.h"
#include <Windows.h>
using namespace BWAPI;
using namespace boost;

AttackClosestScript::AttackClosestScript() {
	// do nothing
	targetPriorityWorkerUnit = false;
}

AttackClosestScript::AttackClosestScript(std::set<BWAPI::Unit*> unitsForCombat, BWAPI::Position pos) {
	this->unitsForCombat = unitsForCombat;
	this->destination = pos;
	targetPriorityWorkerUnit = false;
}

void AttackClosestScript::update() {
	bool haveWorkerEnemy = false;
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
				|| BWTA::getRegion(a->getPosition()) == BWTA::getRegion(e->getPosition())) { // in same region
					if (!alreadyAddedCurrEnemy) { // prevent duplicate insertions
						nearbyEnemyUnits.insert(e);
						alreadyAddedCurrEnemy = true;

						if (e->getType().isWorker()) {
							haveWorkerEnemy = true;
						}
					}
			}
		}
	}

	std::set<BWAPI::Unit*> nearbyWorkerEnemyUnits;

	if (!nearbyEnemyUnits.empty()) {

		// if our priority is to kill worker units, then if any worker units exist
		// ignore all other units
		
		if (this->targetPriorityWorkerUnit && haveWorkerEnemy) {
			// remove any workers from the set of enemies
			BOOST_FOREACH (BWAPI::Unit* currEnemyUnit, nearbyEnemyUnits) {
				if (currEnemyUnit->getType().isWorker()) {
					nearbyWorkerEnemyUnits.insert(currEnemyUnit);		
				}
			}
		}

		//nearbyEnemyUnits = nearbyWorkerEnemyUnits; // cant do this because nearby enemy units is used elsewhere

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
						if (this->targetPriorityWorkerUnit && haveWorkerEnemy) {
							newTarget = findClosestEnemyUnitNOK(myUnit, nearbyWorkerEnemyUnits, attackAssignments);
						}else{
							newTarget = findClosestEnemyUnitNOK(myUnit, nearbyEnemyUnits, attackAssignments);
						}


					}else {
						// my unit has a target so do nothing
					}

				}else{
					// My unit does not have a target, find him the nearest enemy
					if (this->targetPriorityWorkerUnit && haveWorkerEnemy) {
						newTarget = findClosestEnemyUnitNOK(myUnit, nearbyWorkerEnemyUnits, attackAssignments);
					}else{
						newTarget = findClosestEnemyUnitNOK(myUnit, nearbyEnemyUnits, attackAssignments);
					}
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
				if (this->targetPriorityWorkerUnit) {
					myUnit->move(this->destination); // because we do not want to attack anything until we get to our destination because we want to make sure we kill workers first
				}else{
					myUnit->attack(this->destination);
				}
			}
		}
	}
	this->nearbyEnemyUnits = nearbyEnemyUnits;
	this->nearbyWorkerEnemyUnits = nearbyWorkerEnemyUnits;
}

void AttackClosestScript::killWorkerUnits() {
	this->targetPriorityWorkerUnit = true;
}



// if all our units are dead OR
// if we are in center of region and there are no more enemy units visible
bool AttackClosestScript::finished() {
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
void AttackClosestScript::finishedProduce(std::map<BWAPI::UnitType , int> * How_Many_Died , int * Total_Died, int * Enemy_Scale , int * Our_Scale , int * StillMoving){
	bool anyUnitAlive = false;
	int numb_units_alive = 0;
	int numb_units_died = 0;
	int count=0 ;

	typedef std::pair<const BWAPI::UnitType, int> uti_t;
	std::map<BWAPI::UnitType , int > temp ;
	BOOST_FOREACH(BWAPI::Unit* myUnit, this->unitsForCombat) {
		std::pair<BWAPI::UnitType , int >  a = std::pair<BWAPI::UnitType , int >(myUnit->getType() , 0);
		(temp).insert(a);
		(*How_Many_Died).insert(a);
	}
	BOOST_FOREACH(BWAPI::Unit* myUnit, this->unitsForCombat) {
		if (myUnit->getHitPoints() > 0) {
			anyUnitAlive = true;
			numb_units_alive++;
		}else{
			numb_units_died++;
			BOOST_FOREACH(uti_t uti, (temp)) {
			if (std::strcmp(myUnit->getType().getName().c_str(), uti.first.getName().c_str()) == 0) {
				// if they match, increment count
				uti.second ++;
				
			} 

			//Broodwar->sendText(("Unit "+SSTR(myUnit->getID())+" has died").c_str());
		}
	}
	}
    
	BOOST_FOREACH(uti_t uti1, (temp)) {

		BOOST_FOREACH(uti_t uti2, (*How_Many_Died)) {
			
			if (std::strcmp(uti1.first.getName().c_str(), uti2.first.getName().c_str()) == 0) {
				// if they match, if temp > Original , do increment for origial
				if(uti1.second > uti2.second){

					uti2.second= uti1.second;
				}
				
			}
		}
	}


	if(*Total_Died   < numb_units_died)
	*Total_Died = numb_units_died ;
	
	*Our_Scale  = numb_units_alive ;

	


	bool areWeStillMoving = false;
	// first check to see if we are still moving to center
	BOOST_FOREACH(BWAPI::Unit* myUnit, this->unitsForCombat) {
		if (myUnit->getHitPoints() > 0 && myUnit->getPosition().getApproxDistance(this->destination) >= 125) {
			areWeStillMoving = true;
		}
	}

	if(areWeStillMoving){
	    *StillMoving = 1;
	}
	else{
		*StillMoving = 0;
	}

	// we have stopped moving, and are still alive, are there still enemies?
	bool anyVisibleEnemyUnit = false;
	int numb_enemy_units_alive = 0;
	if (this->targetPriorityWorkerUnit) {
		BOOST_FOREACH(BWAPI::Unit* enemyUnit, this->nearbyWorkerEnemyUnits) {
			if (enemyUnit->getHitPoints() > 0) {
				anyVisibleEnemyUnit = true;
				numb_enemy_units_alive ++;
			}
		}
	}else{
		BOOST_FOREACH(BWAPI::Unit* enemyUnit, this->nearbyEnemyUnits) {
			if (enemyUnit->getHitPoints() > 0) {
				anyVisibleEnemyUnit = true;
				numb_enemy_units_alive++;
			}
		}
	}
	* Enemy_Scale = numb_enemy_units_alive;

	



	
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
