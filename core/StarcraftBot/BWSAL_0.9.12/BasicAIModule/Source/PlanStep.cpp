#define NOMINMAX
#include "PlanStep.h"
#include <Windows.h>
#include <string>
#include <sstream>
//#include "..\Sparcraft\source\SparCraft.h"
using namespace BWAPI;
using namespace boost;



PlanStep::PlanStep() {
	this->displayString = " No Plan Step Yet";
	this->hasBegunExecution = false;
	this->hasFinishedExecutionFlag = false;
	this->haveIssuedMoveOrder = false;
	this->unitsAtDestination = false;
	this->alreadyIssuedAttackOrder = false;
	this->alreadyCreatedSquad = false;
	this->abortedPlan = false;
	this->destRegionId = -1;
	alreadyComputedUnitGroups = false;
	this->planID = -1;
	this->sentForNextPlanStep = false;
	this->closestBaseCount=0;
	this->alreadyConstructedBase=false;
	/*
	std::set<Base*> aBases= this->baseManager->getActiveBases();
				int count=0;
				for(std::set<Base*>::const_iterator b = aBases.begin(); b != aBases.end(); b++)
				{
					count++;
				}
				*/
	this->BaseIssued=0;
	this->Total_Died= 0 ;
	this->Total_Died_Previous=0;
	this->Enemy_Scale = 0;
	this->Our_Scale = 0;
	this->StillMoving = 0;
	this->redo = 1;
	this->redo_bunker=1;
	this->start = std::clock();
}

PlanStep::PlanStep(std::string stepStr) {
	//MessageBoxA(NULL, "IN Constructor of PlanStep","Error",0); 
	this->hasBegunExecution = false;
	this->hasFinishedExecutionFlag = false;
	this->haveIssuedMoveOrder = false;
	this->unitsAtDestination = false;
	this->alreadyIssuedAttackOrder = false;
	this->alreadyCreatedSquad = false;
	this->abortedPlan = false;
	this->destRegionId = -1;
	alreadyComputedUnitGroups = false;
	this->sentForNextPlanStep = false;
	this->firstEntryIntoRegion = BWAPI::Position(-1,-1);
	// whole lot of parsing
	this->displayString = stepStr;
	this->planID = -1;
	Broodwar->sendText(stepStr.c_str());
	
	this->closestBaseCount=0;
	this->alreadyIssuedBase=false;
	this->alreadyConstructedBase=false;
   
	this->Enemy_Scale = 0;
	this->Our_Scale = 0;
	this->StillMoving = 0;
	this->redo = 1;
	this->redo_bunker=1;
	this->Total_Died= 0 ;
	this->Total_Died_Previous = 0;
	this->start = std::clock();
	this->Enemy_Scale_Previous= 0;
	this->allLoadedMarines=0;
		//std::set<Base*> aBases= this->baseManager->getActiveBases();
	//int count1= this->baseManager->getrefieneryBuildPriority();
	//MessageBoxA(NULL, SSTR(count1)
	//				.c_str(),"Refinery",0); 

	//int count=0;
			//for(std::set<Base*>::const_iterator b = aBases.begin(); b != aBases.end(); b++)
			//	{
			//		count++;
			//	}
	
	this->BaseIssued=0;
	this->totalBaseCount=0;
	// tokenize the input string
	char_separator<char> sep(" ");
	tokenizer< char_separator<char> > tokens(stepStr, sep);
	tokenizer< char_separator<char> >::iterator word = tokens.begin();

	// always first is the plan id (UNLESS IT IS AN ABORT)
	if (word->compare("ABORT-PLAN") == 0) {
		word++;
		this->planID = lexical_cast<int>(*word);
		// mark this as aborted
		this->abortedPlan = true;
	}else{
		this->planID = lexical_cast<int>(*word);
		word++;
	}

	// initiliaze this object depending on the input string
	if (word->compare("PRODUCE_UNITS") == 0) {
		this->stepGoal = PRODUCE_UNITS;
		this->alreadyIssuedUnitProductionGoal = false;

		// begin parsing unit types and counts describing the units to acquire
		word++; // increment to next word
		while (!word.at_end()) {
			UnitType type = UnitTypes::getUnitType(*word);
			word++; // move to next word
			// safety check
			if (word.at_end()) {
				// Error, malformed plan step string
				MessageBoxA(NULL, stepStr.append(" [ERROR] malformed plan step string")
					.c_str(),"Error",0); 
			}
			std::string typeCount = (*word);
			// store in map and convert typeCount from string to int
			this->desiredUnitCounts[type] = lexical_cast<int>(typeCount);
			word++; // move to next word

			// TODO


			// Helpful debugging code - eventually to be deleted
			//std::string s = str(format("Just added %s with count %s") % type.getName().c_str() % typeCount.c_str());
			//MessageBoxA(NULL, s.c_str(), "TEST",0);
		}

	}else if (word->compare("ACQUIRE_UNITS") == 0) {
		this->stepGoal = ACQUIRE_UNITS;
		this->alreadyIssuedUnitProductionGoal = false;

		// begin parsing unit ids
		word++; // increment to next word
		std::string unitIdsStr = "UNIT IDs To Acquire:";
		while (!word.at_end()) {
			int unitID = lexical_cast<int>(*word);
			this->unitIdsToAcquire.insert(unitID);
			unitIdsStr.append(" "+(*word));
			word++; // move to next word
		}
		unitIdsStr.append("\n");

		Broodwar->sendText(unitIdsStr.c_str());

	}else if (word->compare("MOVE_UNITS") == 0) {
		//MessageBoxA(NULL, stepStr.append(" [Testing]").c_str(),"Error",0); 
		this->stepGoal = MOVE_UNITS;

		// begin parsing destination to move to
		word++; // parse DEST
		if (word.at_end() || word->compare("DEST") != 0) {
			// Error, malformed plan step string
			MessageBoxA(NULL, stepStr.append(" [ERROR] malformed plan step string")
				.c_str(),"Error",0); 
		}else{
			// next word is the destination
			// Format is: MOVE_UNITS X <n> Y <n>
			word++; // parse X
			word++; // now word is x value
			int destX = lexical_cast<int>(*word);
			word++; // parse Y
			word++; // now word is y value
			int destY = lexical_cast<int>(*word);
			this->destination = BWAPI::Position(destX, destY);
		}

	}else if (word->compare("CORNER_MOVE_UNITS") == 0) {
		//MessageBoxA(NULL, stepStr.append(" [Testing]").c_str(),"Error",0); 
		this->stepGoal = CORNER_MOVE_UNITS;

		// begin parsing destination to move to
		word++; // parse DEST
		if (word.at_end() || word->compare("DEST") != 0) {
			// Error, malformed plan step string
			MessageBoxA(NULL, stepStr.append(" [ERROR] malformed plan step string")
				.c_str(),"Error",0); 
		}else{
			// next word is the destination
			// Format is: MOVE_UNITS X <n> Y <n>
			word++; // parse X
			word++; // now word is x value
			int destX = lexical_cast<int>(*word);
			word++; // parse Y
			word++; // now word is y value
			int destY = lexical_cast<int>(*word);
			this->destination = BWAPI::Position(destX, destY);
		}

	}else if (word->compare("ATTACK_MOVE_UNITS_SURROUND") == 0) {
		this->stepGoal = ATTACK_MOVE_UNITS_SURROUND;

		//MessageBoxA(NULL, " Received step ATTACK_MOVE_UNITS_SURROUND","Error",0); 
		// begin parsing destination to move to
		word++; // parse DEST
		do {

			if (word.at_end() || word->compare("DEST") != 0) {
				// Error, malformed plan step string
				MessageBoxA(NULL, stepStr.append(" [ERROR] malformed plan step string")
					.c_str(),"Error",0); 
			}else{
				// next word is the destination
				// Format is: ATTACK_MOVE_UNITS DEST X <n> Y <n> DEST X <n> Y <n> ...
				word++; // parse X
				word++; // now word is x value
				std::string xStr = *word;
				int destX = lexical_cast<int>(*word);
				word++; // parse Y
				word++; // now word is y value
				std::string yStr = *word;
				int destY = lexical_cast<int>(*word);
				this->surroundDestinations.push_back(new BWAPI::Position(destX, destY));
				// for each destination, add a new unitMoveGroup
				unitMoveGroups.push_back(std::set<BWAPI::Unit*>());

				//MessageBoxA(NULL, ("Parsed " + xStr + "," + yStr + " successfully").c_str(),"Test",0);
				word++;
			}

		} while (!word.at_end());
		//MessageBoxA(NULL, "END OF step ATTACK_MOVE_UNITS_SURROUND","Error",0); 

	}else if (word->compare("ATTACK_MOVE_UNITS") == 0) {
		this->stepGoal = ATTACK_MOVE_UNITS;

		// begin parsing destination to move to
		word++; // parse DEST
		if (word.at_end() || word->compare("DEST") != 0) {
			// Error, malformed plan step string
			MessageBoxA(NULL, stepStr.append(" [ERROR] malformed plan step string")
				.c_str(),"Error",0); 
		}else{
			// next word is the destination
			// Format is: ATTACK_MOVE_UNITS X <n> Y <n>
			word++; // parse X
			word++; // now word is x value
			int destX = lexical_cast<int>(*word);
			word++; // parse Y
			word++; // now word is y value
			int destY = lexical_cast<int>(*word);
			this->destination = BWAPI::Position(destX, destY);
		}

	}else if (word->compare("ATTACK") == 0) {
		this->stepGoal = ENGAGE;
		this->stepFightStyle = ATTACK;

		// for now the destination is the center of the region of the enemy base

		// begin parsing destination to move to
		word++; // parse DEST
		if (word.at_end() || (word->compare("DEST") != 0 && word->compare("DEST-REGION-ID") != 0)) {
			// Error, malformed plan step string
			MessageBoxA(NULL, stepStr.append(" [ERROR] malformed plan step string")
				.c_str(),"Error",0); 
		}else{
			if (word->compare("DEST") == 0) {

				// next word is the destination
				// Format is: MOVE_UNITS X <n> Y <n>
				word++; // parse X
				word++; // now word is x value
				int destX = lexical_cast<int>(*word);
				word++; // parse Y
				word++; // now word is y value
				int destY = lexical_cast<int>(*word);
				this->destination = BWAPI::Position(destX, destY);
			}else if (word->compare("DEST-REGION-ID") == 0) {
				// next word is the int id of the region
				word++;
				this->destRegionId = lexical_cast<int>(*word);
			}
		}
	}else if (word->compare("ATTACK_WORKERS") == 0) {
		this->stepGoal = ENGAGE_WORKERS;
		this->stepFightStyle = ATTACK;

		// for now the destination is the center of the region of the enemy base

		// begin parsing destination to move to
		word++; // parse DEST
		if (word.at_end() || (word->compare("DEST") != 0 && word->compare("DEST-REGION-ID") != 0)) {
			// Error, malformed plan step string
			MessageBoxA(NULL, stepStr.append(" [ERROR] malformed plan step string")
				.c_str(),"Error",0); 
		}else{
			if (word->compare("DEST") == 0) {

				// next word is the destination
				// Format is: MOVE_UNITS X <n> Y <n>
				word++; // parse X
				word++; // now word is x value
				int destX = lexical_cast<int>(*word);
				word++; // parse Y
				word++; // now word is y value
				int destY = lexical_cast<int>(*word);
				this->destination = BWAPI::Position(destX, destY);
			}else if (word->compare("DEST-REGION-ID") == 0) {
				// next word is the int id of the region
				word++;
				this->destRegionId = lexical_cast<int>(*word);
			}
		}
	}
	else if(word->compare("EXPAND_SPECIFIC") == 0){
		this->stepGoal =  EXPAND_SPECIFIC;
	    this->stepFightStyle = EXPAND;
		word++; // parse DEST
		if (word.at_end() ) {
			// Error, malformed plan step string
			MessageBoxA(NULL, stepStr.append(" [ERROR] malformed plan step string")
				.c_str(),"Error",0); 
		}else{
			// next word is the BaseLocation
			// Format is: MOVE_UNITS X <n> Y <n>
			word++; // parse X
			word++; // now word is x value
			int destX = lexical_cast<int>(*word);
			word++; // parse Y
			word++; // now word is y value
			int destY = lexical_cast<int>(*word);
			this->ClosestBaseLocation = BWAPI::Position(destX, destY);
			this->BaseIssued+=1;
		     
		}
	}
	else if(word->compare("EXPAND_CLOSEST")==0){
		Broodwar->sendText("***************************************************");
		this->stepGoal = EXPAND_CLOSEST;
		this->stepFightStyle=EXPAND;
		word++;
		if(word.at_end()){
			MessageBoxA(NULL, stepStr.append(" [ERROR] malformed plan step string 1111")
				.c_str(),"Error",0); 
		}else{
			Broodwar->sendText("#########################################");
	
			this->closestBaseCount+=1;
			this->BaseIssued+=1;
			///std::string msg= SSTR(totalBaseCount);
			///MessageBoxA(NULL,msg.c_str(), "Total Base Count",0); 
		}
		Broodwar->sendText("Received expand step");
	}
	else if (word->compare("DEFEND") == 0) {
		this->stepGoal = ENGAGE;
		this->stepFightStyle = DEFEND;

	}
	else if(word->compare("DEFEND_MOVE_UNITS")==0){
		Broodwar->sendText("DDDDDDDDDDDDDDDDDDDDMMMMMMMMMMMMMMMMMMMM");
	    word++;
		if(word.at_end()){
			MessageBoxA(NULL, stepStr.append(" [ERROR] malformed plan step string @@@@@@@@@@@@")
				.c_str(),"Error",0); 
		}
		else{
			word++; //skip DEST
			word++; //skip X

				std::string xStr = *word;
				int destX = lexical_cast<int>(*word);
				word++; // parse Y
				word++; // now word is y value
				std::string yStr = *word;
				int destY = lexical_cast<int>(*word);
				this->destination = BWAPI::Position(destX, destY);

				//MessageBoxA(NULL, ("Parsed " + xStr + "," + yStr + " successfully").c_str(),"Test",0);
				
				

		this->stepGoal = DEFEND_MOVE_UNITS;
	    this->stepFightStyle = DEFEND;
		}
	}
	else if(word->compare("DEFEND_BASE_BUNKER")==0){
		Broodwar->sendText("DEFEND_BASE_BUNKER");

		this->stepGoal = DEFEND_BASE_BUNKER;
		this->stepFightStyle=DEFEND;
		word++;
		if(word.at_end()){
			MessageBoxA(NULL, stepStr.append("ERROR IN DEFEND BASE BUNKER")
				.c_str(),"Error",0); 
		}else{
			Broodwar->sendText("#########################################");
			this->destinationOfBunker=BWAPI::Position(1,1);//
			///std::string msg= SSTR(totalBaseCount);
			///MessageBoxA(NULL,msg.c_str(), "Total Base Count",0); 
		}



	}

	// THIS GETS HANDLED FIRST, not last anymore
	//else if (word->compare("ABORT-PLAN") == 0) { 
	//}
}

bool PlanStep::planHasBeenAborted() {
	return this->abortedPlan;
}

PlanStep::PlanStep(std::set<BWAPI::Unit*> units, BWTA::Region* targetRegion, StepGoal stepGoal, 
				   StepFightStyle stepFightStyle, StepUnitType stepUnitType,
				   StepUnitMovement stepUnitMovement) {

					   this->units = units;
					   this->targetRegion = targetRegion;
					   this->stepGoal = stepGoal;
					   this->stepFightStyle = stepFightStyle;
					   this->stepUnitType = stepUnitType;
					   this->stepUnitMovement = stepUnitMovement;
					   this->hasBegunExecution = false;
					   this->displayString = "";
}

void PlanStep::setUnitProductionManager(UnitProductionManager* mngr) {
	this->unitProductionManager = mngr;
}
void PlanStep::setExpandManager(ExpandManager * expandManager){

	this->expandManager = expandManager;
}

void PlanStep::setBaseManager(BaseManager* mngr) {
	this->baseManager = mngr;
}


void PlanStep::setAvailableUnits(std::set<BWAPI::Unit*> aUnits) {
	this->availableUnits = aUnits;
}

void PlanStep::beginExecution() {
	this->hasBegunExecution = true;
}

bool PlanStep::hasFinishedExecution() {
	return this->hasFinishedExecutionFlag;
}

void PlanStep::endExecution() {
	if (this->hasBegunExecution) {
		this->hasFinishedExecutionFlag = true;
	}else{
		// ERROR!!
		std::string errMsg = "[ERROR] ending step that had never begunExecution()";
		MessageBoxA(NULL, errMsg.c_str(),"Error in PlanStep.endExecution()",0);
	}
}

std::string PlanStep::getDisplayString() {
	return this->displayString;
}

PlanStep::StepFightStyle PlanStep::getStepFightStyle() {
	return this->stepFightStyle;
}

// if attack is true, order units to attack move, otherwise just move
// only moves the units in the unitGrp to the given destination
// stealthFlight means the units are flying and are part of a sneak attack, so therefore 
// dont worry what region they are in, just send them to the destination
void PlanStep::moveUnits(bool attack, BWAPI::Position* dest, std::set<BWAPI::Unit*> unitGrp, bool stealthFlight) {
	if (!this->haveIssuedMoveOrder) {
		if (dest != NULL && !unitGrp.empty()) {
			BOOST_FOREACH (BWAPI::Unit* u, unitGrp) {
				if (attack) {
					// if we are defending, then don't attack move our flying units
					// because they can fly over enemies
					if (this->getStepFightStyle() == DEFEND && u->getType() == BWAPI::UnitTypes::Terran_Wraith) {
						u->move(*dest);
					}else{
						u->attack(*dest);
					}
				}else{
					u->move(*dest);
				}
			}
		}
		this->haveIssuedMoveOrder = true;
	}else{

		// units should be moving, now check when they have arrived
		// TODO - Ensure that killed units do not cause move to fail when the
		//        rest of the units reach the destination
		bool everyUnitReached = true;
		bool someUnitAlive = false;
		int maxDistToDest = 125;
		//Broodwar->printf("Available Units Are:");
		BOOST_FOREACH (BWAPI::Unit* u, unitGrp) {


			//Broodwar->printf(u->getType().getName().c_str());
			//Broodwar->printf(SSTR(u->getID()).c_str());


			if (u->getHitPoints() != 0) {
				someUnitAlive = true;



				if (stealthFlight) {
					// if any unit is less than 15 away from the target position, and hold position
					// so you wait until they all arrive
					if (u->getPosition().getApproxDistance(*dest) < 15) {
								if (!u->isHoldingPosition()) {
									// important to only order once, otherwise will prevent
									// unit from being able to attack
									u->holdPosition();
								}
					}else{
						// some unit is farther than 15 from dest
						everyUnitReached = false;
					}
				}else if (BWTA::getRegion(u->getPosition()) != BWTA::getRegion(*dest)) {
					// instead check to see if every unit is in that region, if not, wait until they all are
					everyUnitReached = false;
					//}

					// if any unit is more than maxDistToDest away, keep them moving
					//if (u->getPosition().getApproxDistance(dest) > maxDistToDest) {
					//std::string dist = "u-pos " + SSTR(u->getPosition().x()) + "," + SSTR(u->getPosition().y()) + " dest " + SSTR(dest.x()) + ","+ SSTR(dest.y()) + " approx dist " + SSTR(dest.getApproxDistance(u->getPosition()));
					//Broodwar->sendText(dist.c_str());
					//everyUnitReached = false;
				}else{
					// if the unit has reached the destination, tell it to hold

					// but wait until it is at least 15 spaces from region boundary (to prevent clumping)
					if (!this->firstEntryIntoRegion.isValid()) {
						this->firstEntryIntoRegion = u->getPosition();
					}else{

						if (u->getPosition().getApproxDistance(this->firstEntryIntoRegion) > 65 
							&&  u->getType() == BWAPI::UnitTypes::Terran_Marine) {
								if (!u->isHoldingPosition()) {
									// important to only order once, otherwise will prevent
									// unit from being able to attack
									u->holdPosition();
								}
						}
					}
				}
			}
		}

		if (!someUnitAlive) {
			//MessageBoxA(NULL, "All units died before MOVE_UNITS has finished","TEST MESSAGE",0);
			this->endExecution();
			//Broodwar->sendText("Finished move or attack_move step b/c all units died");
			this->firstEntryIntoRegion = BWAPI::Position(-1,-1);
		}else if (everyUnitReached) {


			//Broodwar->setLocalSpeed(50);
			BWAPI::Unit* someUnit = *(unitGrp.begin());
			BWAPI::Position newScreenPosition(someUnit->getPosition().x()-250, someUnit->getPosition().y()-250);
			//Broodwar->setScreenPosition(newScreenPosition);

			//MessageBoxA(NULL, std::string("MOVE_UNITS has finished - they are now in region ").append(SSTR(BWTA::getRegion(someUnit->getPosition())->getCenter())).c_str(),"TEST MESSAGE",0);
			//MessageBoxA(NULL, std::string("MOVE_UNITS has finished").c_str(),"TEST MESSAGE",0);
			this->endExecution();
			//Broodwar->sendText("Finished move or attack_move step successfully");
			this->firstEntryIntoRegion = BWAPI::Position(-1,-1);
		}
	}
}


// if attack is true, order units to attack move, otherwise just move
void PlanStep::moveUnits(bool attack, bool stealthFlight) {

	if (!this->haveIssuedMoveOrder) {
		if (this->destination != NULL && !this->availableUnits.empty()) {
			BOOST_FOREACH (BWAPI::Unit* u, this->availableUnits) {
				if (attack) {
					// if we are defending, then don't attack move our flying units
					// because they can fly over enemies
					if (this->getStepFightStyle() == DEFEND && u->getType() == BWAPI::UnitTypes::Terran_Wraith) {
						u->move(this->destination);
					}else{
						u->attack(this->destination);
					}
				}else{
					u->move(this->destination);
				}

			}
		}
		this->haveIssuedMoveOrder = true;
	}else{

		// units should be moving, now check when they have arrived
		// TODO - Ensure that killed units do not cause move to fail when the
		//        rest of the units reach the destination
		bool everyUnitReached = true;
		bool someUnitAlive = false;
		int maxDistToDest = 125;
		//Broodwar->printf("Available Units Are:");
		BOOST_FOREACH (BWAPI::Unit* u, this->availableUnits) {


			//Broodwar->printf(u->getType().getName().c_str());
			//Broodwar->printf(SSTR(u->getID()).c_str());


			if (u->getHitPoints() != 0) {
				someUnitAlive = true;


				
				if (stealthFlight) {
					// if any unit is less than 15 away from the target position, and hold position
					// so you wait until they all arrive
					if (u->getPosition().getApproxDistance(this->destination) < TILE_SIZE*2) {
								if (!u->isHoldingPosition()) {
									// important to only order once, otherwise will prevent
									// unit from being able to attack
									u->holdPosition();
									//MessageBoxA(NULL, ("Unit "+SSTR(u->getID())+" is now holding").c_str(),"Debug message",0);
								}
					}else{
						// some unit is farther than 15 from dest
						everyUnitReached = false;
					}
				}//here for different units the standard of getApproxDistance is different
				
				else if (BWTA::getRegion(u->getPosition()) != BWTA::getRegion(this->destination) && u->getPosition().getApproxDistance(this->destination) > 150 ) {
					// instead check to see if every unit is in that region, if not, wait until they all are
					
					everyUnitReached = false;
					//}

					// if any unit is more than maxDistToDest away, keep them moving
					//if (u->getPosition().getApproxDistance(this->destination) > maxDistToDest) {
					//std::string dist = "u-pos " + SSTR(u->getPosition().x()) + "," + SSTR(u->getPosition().y()) + " dest " + SSTR(this->destination.x()) + ","+ SSTR(this->destination.y()) + " approx dist " + SSTR(this->destination.getApproxDistance(u->getPosition()));
					//Broodwar->sendText(dist.c_str());
					//everyUnitReached = false;
				}else{
					// if the unit has reached the destination, tell it to hold

					// but wait until it is at least 15 spaces from region boundary (to prevent clumping)
					if (!this->firstEntryIntoRegion.isValid()) {
						this->firstEntryIntoRegion = u->getPosition();
					}else{
						//here for tank the move should be more tricky
						if (u->getPosition().getApproxDistance(this->firstEntryIntoRegion) > 120 
							&&  u->getType() == BWAPI::UnitTypes::Terran_Marine) {
								if (!u->isHoldingPosition()) {
									// important to only order once, otherwise will prevent
									// unit from being able to attack
									u->holdPosition();
								}
								/*
								int x_coordinate =  this->destination.x()+10; 
								int y_coordinate = this->destination.y()+10;
								BWAPI::Position a = BWAPI::Position(x_coordinate,y_coordinate);
								u->patrol(a);
								*/
						}
					}
				}
			}
		}

		if (!someUnitAlive) {
			//MessageBoxA(NULL, "All units died before MOVE_UNITS has finished","TEST MESSAGE",0);
			this->endExecution();
			//Broodwar->sendText("Finished move or attack_move step b/c all units died");
			this->firstEntryIntoRegion = BWAPI::Position(-1,-1);
		}else if (everyUnitReached) {


			//Broodwar->setLocalSpeed(50);
			BWAPI::Unit* someUnit = *(this->availableUnits.begin());
			BWAPI::Position newScreenPosition(someUnit->getPosition().x()-250, someUnit->getPosition().y()-250);
			//Broodwar->setScreenPosition(newScreenPosition);

			//MessageBoxA(NULL, std::string("MOVE_UNITS has finished - they are now in region ").append(SSTR(BWTA::getRegion(someUnit->getPosition())->getCenter())).c_str(),"TEST MESSAGE",0);
			//MessageBoxA(NULL, std::string("MOVE_UNITS has finished").c_str(),"TEST MESSAGE",0);
			this->endExecution();
			//Broodwar->sendText("Finished move or attack_move step successfully");
			this->firstEntryIntoRegion = BWAPI::Position(-1,-1);
		}
	}
}


void PlanStep::reProduce(){
	typedef std::pair<const BWAPI::UnitType, int> uti_t;
	if(this->redo>0) {
		int redo_number = 0;
	if(this->Enemy_Scale  > 5){
		redo_number=4;
	this->unitProductionManager->newGoal(this->How_Many_Died);
	
	BOOST_FOREACH(uti_t uti1, (this->desiredUnitCounts)) {

		BOOST_FOREACH(uti_t uti2, this->How_Many_Died) {
			
			if (std::strcmp(uti1.first.getName().c_str(), uti2.first.getName().c_str()) == 0) {
				// if they match, if temp > Original , do increment for origial
				if(uti1.second > uti2.second){      // in case Dead Units > Dedired
					int a = uti2.second+redo_number;   //why 5?
					uti1.second+= a;
				}
				
			}
		}
	}
	this->redo--;
	}
	else{//enemy is weak
		/*
		std::map<BWAPI::UnitType , int> temp_weak_enemy_situation ; 
		BOOST_FOREACH(uti_t uti1, this->How_Many_Died) {
			temp_weak_enemy_situation.insert(uti1);

		}*/
        this->unitProductionManager->newGoal(this->How_Many_Died);
		this->redo--;
	}
	//*****************************************HERE WE COULD BUILD MORE COMPETITIVE & SPECIFIC UNITS AGAINST THE ATTACK, NOT JUST AS SAME AS THE ORIGINAL ONES*********************** 
	}	
	else{//redo  = 1; 
		//we have already added more units, if
	}
}


bool PlanStep::IsProducing(){
	typedef std::pair<const BWAPI::UnitType, int> uti_t;
	std::set<BWAPI::Unit*> ALL_UNITS = Broodwar->getAllUnits();
	std::set<BWAPI::UnitType> ALL_BUILDING_TYPES ;
	
	BOOST_FOREACH(uti_t Unit1, this->desiredUnitCounts) {
		if(Unit1.first == BWAPI::UnitTypes::Terran_Marine ||Unit1.first == BWAPI::UnitTypes::Terran_Medic|| Unit1.first == BWAPI::UnitTypes::Terran_Firebat){

			ALL_BUILDING_TYPES.insert(BWAPI::UnitTypes::Terran_Barracks);
			continue;
		}
		else if(Unit1.first == BWAPI::UnitTypes::Terran_Siege_Tank_Tank_Mode ||Unit1.first == BWAPI::UnitTypes::Terran_Siege_Tank_Siege_Mode ||Unit1.first == BWAPI::UnitTypes::Terran_Goliath  ){
			ALL_BUILDING_TYPES.insert(BWAPI::UnitTypes::Terran_Factory);
			continue;
		}
		else if(Unit1.first == BWAPI::UnitTypes::Terran_Dropship || Unit1.first == BWAPI::UnitTypes::Terran_Science_Vessel ||Unit1.first == BWAPI::UnitTypes::Terran_Valkyrie || Unit1.first == BWAPI::UnitTypes::Terran_Wraith ||Unit1.first == BWAPI::UnitTypes::Terran_Battlecruiser )
		{
			ALL_BUILDING_TYPES.insert(BWAPI::UnitTypes::Terran_Starport);
			continue;
		}
		else if(Unit1.first ==BWAPI::UnitTypes::Terran_Bunker )
		{
			ALL_BUILDING_TYPES.insert(BWAPI::UnitTypes::Terran_Bunker);
		}
		else if(Unit1.first ==BWAPI::UnitTypes::Terran_Missile_Turret)
		{
			ALL_BUILDING_TYPES.insert(BWAPI::UnitTypes::Terran_Missile_Turret);

		}
			
	}
	int count_All_Production_building=0;
	int count_Defense_building = 0;
	BOOST_FOREACH(BWAPI::Unit* Unit1, ALL_UNITS) {
		if(Broodwar->self()->getID()== Unit1->getPlayer()->getID() ) // our units
		{
			BOOST_FOREACH(BWAPI::UnitType type, ALL_BUILDING_TYPES) {
				
				if(Unit1->getType() == type  ){
					if(Unit1->isBeingConstructed() && ( Unit1->getType() == BWAPI::UnitTypes::Terran_Bunker ||Unit1->getType() == BWAPI::UnitTypes::Terran_Missile_Turret ||Unit1->getType() == BWAPI::UnitTypes::Terran_Command_Center) ){
					
					count_Defense_building++;
					}
					else if(Unit1->isBeingConstructed() &&  Unit1->getType() != BWAPI::UnitTypes::Terran_Bunker && Unit1->getType() != BWAPI::UnitTypes::Terran_Missile_Turret )
					{
					count_All_Production_building++;
					}

					else if(Unit1->isTraining()){
						return true;
					}
					

					//else{continue;}


				}
			    if((Unit1->getType() == BWAPI::UnitTypes::Terran_Machine_Shop || Unit1->getType() == BWAPI::UnitTypes::Terran_Control_Tower || Unit1->getType() == BWAPI::UnitTypes::Terran_Physics_Lab ) && (Unit1->getRemainingBuildTime()>0 || Unit1->isBeingConstructed()) )
					{
						return true;
					}

			}
		}

	}
	//here we need to ensure no bug in contruct building.
	if(Broodwar->self()->minerals() <400){
		return true;
	}
	else if(count_All_Production_building>0 ){
		return true;
	}
	else if(count_Defense_building >0){
		return true;
	}
	else 
	{
		return false;
	}
	
}


void PlanStep::updateStep() {

	/*if (!(this->hasBegunExecution)) {
	MessageBoxA(NULL,
	this->getDisplayString().c_str(),
	"0. THIS STEP HAS NOT BEGUN",
	MB_ICONINFORMATION|MB_OK);
	}else{
	MessageBoxA(NULL,
	this->getDisplayString().c_str(),
	"0. THIS STEP *HAS* BEGUN",
	MB_ICONINFORMATION|MB_OK);
	}*/

	bool redo_or_not = false;
	double duration = (clock() -start)/(double) CLOCKS_PER_SEC;
	if(duration>5.0){
	this->start = clock();
	redo_or_not = true;
	}


	if (this->hasStarted() && !this->hasFinishedExecution()) {
		// all the important stuff

		switch (this->stepGoal) {
		case PRODUCE_UNITS:
			/*MessageBoxA(NULL,
			this->getDisplayString().c_str(),
			"ALMOST 1. order all the units to be built",
			MB_ICONINFORMATION|MB_OK);*/
			if (!alreadyIssuedUnitProductionGoal) {
				// first issue order to produce units
				this->unitProductionManager->newGoal(this->desiredUnitCounts);
				this->alreadyIssuedUnitProductionGoal = true;
				attackManager = AttackClosestScript(this->availableUnits, BWAPI::Position(Broodwar->self()->getStartLocation()));
				/*MessageBoxA(NULL,
				this->getDisplayString().c_str(),
				"1. order all the units to be built",
				MB_ICONINFORMATION|MB_OK);*/
			}else{

				// have all the units we own defend the base they are in

				attackManager.setUnits(this->availableUnits);


				/*
				if (!attackManager.finished()) {
					attackManager.update();
					Broodwar->sendText("update AttackManager in produce unit");

				}
				*/
				attackManager.finishedProduce(   &( this->How_Many_Died)  , &(this->Total_Died), &(this->Enemy_Scale) ,&(this->Our_Scale) , &(this->StillMoving ));
				if(this->Enemy_Scale > 0 || this->StillMoving==1){
					attackManager.update();
					//Broodwar->sendText("update AttackManager in produce unit");
				}
//*************************************************************************************************************************************************
// this Part of code, I wish we could check it every five seconds.



				// check if units are produced, if they are, end step
				double accomplishment_Units = haveProducedUnits2();
				if (accomplishment_Units==1) {
				   /* MessageBoxA(NULL,
					this->getDisplayString().c_str(),
					"Finished Producing Units!!",
					MB_ICONINFORMATION|MB_OK);
					*/
					this->endExecution();
				}else{
					if(redo_or_not){
					//First Check redo tag, if re-done end execution, else add new goal
						if(this->Total_Died > this->Total_Died_Previous ){// we are been attacked by the enemy and need to produce more. 

							//re produce some ....based on enemy's quantity
							this->reProduce();
							
						}
						else{
							if((Enemy_Scale-Enemy_Scale_Previous)>2)
							{
								this->redo ++;// we find that enemy sended more units here, add redo times.
								this->reProduce(); //how to reproduce and should we mannual add transfer it to other plan steps.
								
							}
							else{
							// HERE CHECK  if total number of units is the same based on the longest production time

								//TODO  ...... <<<<<<<<<<<<<<<<<<<<<<<<<<<<
							// here write a function to check if any barrack or terran factory are producing based on the unitType
							    //TODO  ......>>>>>>>>>>>>>>>>>>>>>>>>>>
								if(IsProducing()){
								//next loop
								}
								else{
									//MessageBoxA(NULL, "End END END ","Error in PlanStep.endExecution()",0);
									this->endExecution();
								}

							// this->endExecution();

							}
						}

					this->Total_Died_Previous = this->Total_Died;
					this->Enemy_Scale_Previous = this->Enemy_Scale;
					}
					else{
					//Actually, here is for doing other things, like detection and handle other steps.
					}
				}
			}
			break;

		case ACQUIRE_UNITS:

			this->availableUnits.clear();
			for (std::set<int>::iterator id = unitIdsToAcquire.begin(); id != unitIdsToAcquire.end(); id++) {
				BWAPI::Unit* u = Broodwar->getUnit((*id));
				if (u != NULL && u->exists() && u->getHitPoints() > 0) {
					this->availableUnits.insert(u);
				}
			}
			this->endExecution();
			//Broodwar->sendText("Finished executing ACQUIRE_UNITS");
			break;

		case MOVE_UNITS:

			this->moveUnits(false, false);
			break;

		case ATTACK_MOVE_UNITS:
			/*MessageBoxA(NULL,
				this->getDisplayString().c_str(),
				("Plan Step Info"),
				MB_ICONINFORMATION|MB_OK);*/
			this->moveUnits(true, false);
			break;

		case CORNER_MOVE_UNITS:
			this->moveUnits(false,true);
			break;
	
		case ATTACK_MOVE_UNITS_SURROUND:
			if (!alreadyComputedUnitGroups) {
				int unitGrpIndex = 0;
				BOOST_FOREACH(BWAPI::Unit* u, availableUnits) {
					unitMoveGroups.at(unitGrpIndex % unitMoveGroups.size()).insert(u);
					unitGrpIndex++;
				}
				alreadyComputedUnitGroups = true;
			}else{
				for (int i = 0; i < (this->surroundDestinations.size()); i++) {
					this->moveUnits(true,surroundDestinations.at(i), unitMoveGroups.at(i), false);
				}
			}


			break;

		case ENGAGE_WORKERS:
			if (this->stepFightStyle == ATTACK) {
				if (!alreadyCreatedSquad) {
					//UnitVector combatUnits(this->availableUnits.begin(), this->availableUnits.end());
					//this->squadData.addSquad(Squad(combatUnits, SquadOrder(SquadOrder::Attack, dest, 1000, "Attack Enemy")));
					alreadyCreatedSquad = true;
					attackManager = AttackClosestScript(this->availableUnits, this->destination);
					attackManager.killWorkerUnits();
					//MessageBoxA(NULL, ("Just created attack manager with dest "+SSTR(dest.x()) + "," + SSTR(dest.y())).c_str(),"TEST MESSAGE",0);
				}else{
					attackManager.update();
					if (attackManager.finished()) {
						Broodwar->sendText("Finished attack workers step");
						this->endExecution();
					}
				}
			}
			break;
		case ENGAGE:
			if (this->stepFightStyle == ATTACK) {
				if (!alreadyCreatedSquad) {
					//UnitVector combatUnits(this->availableUnits.begin(), this->availableUnits.end());
					//this->squadData.addSquad(Squad(combatUnits, SquadOrder(SquadOrder::Attack, dest, 1000, "Attack Enemy")));
					alreadyCreatedSquad = true;
					attackManager = AttackClosestScript(this->availableUnits, this->destination);
					//MessageBoxA(NULL, ("Just created attack manager with dest "+SSTR(dest.x()) + "," + SSTR(dest.y())).c_str(),"TEST MESSAGE",0);
				}else{
					attackManager.update();
					if (attackManager.finished()) {
						Broodwar->sendText("Finished attacking step");
						this->endExecution();
					}
				}
			}else if (this->stepFightStyle == DEFEND) {
			}
			break;

		case EXPAND_CLOSEST:
			if(!alreadyIssuedBase){
				/*
				MessageBoxA(NULL,
					"Start Adding expand step to executor!!",
					"Prompt",
					MB_ICONINFORMATION|MB_OK);
					*/
				this->expandManager->newGoal();
				//here check the total number of bases consists of (new + old )
				std::set<Base*> aBases= this->baseManager->getAllBases();
				int count=0;
				for(std::set<Base*>::const_iterator b = aBases.begin(); b != aBases.end(); b++)
				{
					count++;
				}

				totalBaseCount= count;
	

			
				alreadyIssuedBase=true;
				this->closestBaseCount-=1;//just for debuggin
				expandAndMineManager = ExpandAndMine();

					
			}else{

				std::set<Base*> aBases= this->baseManager->getActiveBases();
				int count=0;
				for(std::set<Base*>::const_iterator b = aBases.begin(); b != aBases.end(); b++)
				{
					count++;
				}
				/*
				std::string msg= "Base Count   ";
				msg.append(SSTR(count));
				msg.append("       Total = ");
				msg.append(SSTR(this->totalBaseCount));
				msg.append("       BaseIssued = ");
				msg.append(SSTR(this->BaseIssued));
				
				Broodwar->sendText(msg.c_str());
		*/

			if (count<=totalBaseCount ) {
					
					expandAndMineManager.update();

				}


			if (haveConstructedBase()) {
					MessageBoxA(NULL,
					"Finished Building Base!!",
					"Prompt",
					MB_ICONINFORMATION|MB_OK);
					this->endExecution();
					
				}else{
					// do nothing and wait :)
				}
			}

			break;
		
		case DEFEND_BASE_BUNKER:
			
			this->moveUnitsIntoBunker(this->destinationOfBunker,4);
			break;

		case EXPAND_SPECIFIC:
			if(!alreadyIssuedBase){
				Broodwar->sendText("Start Constructing Base step");
				this->expandManager->newGoal(ClosestBaseLocation);
				alreadyConstructedBase=true;

			}else{
			if (haveConstructedBase()) {
					/*MessageBoxA(NULL,
					this->getDisplayString().c_str(),
					"Finished Producing Units!!",
					MB_ICONINFORMATION|MB_OK);*/
					this->endExecution();
				}else{
					// do nothing and wait :)
				}
			}
			break;
		}

	}
}
void PlanStep::moveUnitsIntoBunker(BWAPI::Position dest,int desired_bunker_number){
	int count_Bunker=0;
	bool finished=true;
//TODO next, check the amount of destroyed bunker.
	std::set<BWAPI::Unit*> allUnits= Broodwar->self()->getUnits();
	if (!this->haveIssuedMoveOrder) {
		
		BOOST_FOREACH (BWAPI::Unit* u, allUnits){
		if(u->getType()==BWAPI::UnitTypes::Terran_Bunker ){
			int a =getBunkerUnitNumber(u);
			count_Bunker++;
			int count=0;
			if(a<4){
				BOOST_FOREACH (BWAPI::Unit* u_Marine, allUnits) {
					if(u_Marine->getType()==BWAPI::UnitTypes::Terran_Marine){
						if(u_Marine->isIdle()){
							u_Marine->rightClick(u);
						    allUnits.erase(u_Marine);
							count++;
							this->allLoadedMarines++;
						}
						if(count==(4-a))
						{
							break;
						}
					}
				}
		
			}
		}
	}
	
	
	if(count_Bunker==desired_bunker_number && this->allLoadedMarines> 4* desired_bunker_number)		
	{	this->haveIssuedMoveOrder=true;}

	else if(count_Bunker< desired_bunker_number && Broodwar->self()->minerals()>400)
	{
		//here we need build more bunker because we may probably be attacked.
		if(redo_bunker>0){
		std::pair<BWAPI::UnitType , int >  Bunker = std::pair<BWAPI::UnitType , int >(BWAPI::UnitTypes::Terran_Bunker , (desired_bunker_number-count_Bunker));
		std::pair<BWAPI::UnitType , int >  Medic = std::pair<BWAPI::UnitType , int >(BWAPI::UnitTypes::Terran_Medic , (3));
		std::pair<BWAPI::UnitType , int >  Marine = std::pair<BWAPI::UnitType , int >(BWAPI::UnitTypes::Terran_Marine , (3));
		std::map<BWAPI::UnitType, int> produce;
		produce.insert(Bunker);
		produce.insert(Medic);
		produce.insert(Marine);
		this->unitProductionManager->newGoal(produce);
		redo_bunker--;
	}
	}
	else if(this->allLoadedMarines < 4*count_Bunker ){
		if(this->redo>0){
		std::pair<BWAPI::UnitType , int >  Medic = std::pair<BWAPI::UnitType , int >(BWAPI::UnitTypes::Terran_Medic , (3));
		std::pair<BWAPI::UnitType , int >  Marine = std::pair<BWAPI::UnitType , int >(BWAPI::UnitTypes::Terran_Marine , (4*desired_bunker_number-allLoadedMarines+2));
		std::map<BWAPI::UnitType, int> produce;
		produce.insert(Medic);

		produce.insert(Marine);
		this->unitProductionManager->newGoal(produce);
		this->redo--;
		}
	}
	if(this->redo==0 &&  this->redo_bunker==0 )
		{	this->haveIssuedMoveOrder=true;}
	}
	else{
		int count_injured_Bunker=0;
		BOOST_FOREACH (BWAPI::Unit* u, allUnits) {
			if(u->getType()==BWAPI::UnitTypes::Terran_Bunker)
			{
				int a=getBunkerUnitNumber(u);
				if(a<=3){
					finished=false;
					int count=0;
					BOOST_FOREACH (BWAPI::Unit* u_Marine, allUnits) {
					if(u_Marine->getType()==BWAPI::UnitTypes::Terran_Marine){
						if(u_Marine->isIdle()){
							u_Marine->rightClick(u);
						    allUnits.erase(u_Marine);
							count++;
							this->allLoadedMarines++;
						}
						if(count==(4-a))
						{
							break;
						}
					}
					
					}
				}
				if(u->getHitPoints()<340 ){
					BOOST_FOREACH (BWAPI::Unit* u1, allUnits){
						if(u1->getType()==BWAPI::UnitTypes::Terran_SCV){
							u1->rightClick(u);
						}
					}

				}
			
			}
		}
		if(finished || Broodwar->self()->minerals()>1000)
		{
		   this->endExecution();	
		}
	}
	
}
int PlanStep::getBunkerUnitNumber(BWAPI::Unit* Bunker){
	std::set<BWAPI::Unit*> units = Bunker->getLoadedUnits();
	int count=0;
	BOOST_FOREACH(BWAPI::Unit* u , units){
		if(u->getType()==BWAPI::UnitTypes::Terran_Marine){
					count++;	
		}
	}
	return count;
}
PlanStep::StepGoal PlanStep::getStepGoal() {
	return this->stepGoal;
}
//Here, better checking the base units one by one
bool PlanStep::haveConstructedBase(){
	/*
    std::set<BWAPI::Unit*> unitSet =BWAPI::Broodwar->getAllUnits();
	
	int count =0;
	  for ( std::set<BWAPI::Unit*>::const_iterator unit= unitSet.begin();unit!=unitSet.end();unit++){

		  if((*unit)->getType()==BWAPI::UnitTypes::Terran_Command_Center && (*unit)->getPlayer()==Broodwar->getPlayer(0))
		 {
			
			count++;
			if(count>= this->totalBaseCount ){
				return true;
			}
		 }
	  }
	  return false;
	  */

	std::set<Base*> aBases= this->baseManager->getActiveBases();
				int count=0;
				for(std::set<Base*>::const_iterator b = aBases.begin(); b != aBases.end(); b++)
				{
					count++;
				}
	if(count>=totalBaseCount){
		return true;
	}
	else return false;

}

bool PlanStep::haveProducedUnits() {
	typedef std::pair<const BWAPI::UnitType, int> uti_t;
	int currUnitCount = 0;
	int numUnitTypesSatisfied = 0; // every time one of the unittypes that we desire
	// has its requirement met, increment this number
	// then we compare this to the length of desired
	// unit types, and if they are equal, we are done
	BOOST_FOREACH(uti_t& uti, this->desiredUnitCounts) {
		currUnitCount = 0;
		BOOST_FOREACH(BWAPI::Unit* u, this->availableUnits) {

			// use string compare to compare BWAPI::UnitTypes
			if (std::strcmp(u->getType().getName().c_str(), uti.first.getName().c_str()) == 0) {
				// if they match, increment count
				currUnitCount++;
			} 
		}

		if (currUnitCount >= uti.second) {
			numUnitTypesSatisfied++;
		}
	}

	// did we meet all requirements for all desired units?
	return numUnitTypesSatisfied == this->desiredUnitCounts.size();
}

double PlanStep::haveProducedUnits2( ) {
	typedef std::pair<const BWAPI::UnitType, int> uti_t;
	int currUnitCount = 0;
	int numUnitTypesSatisfied = 0; // every time one of the unittypes that we desire
	// has its requirement met, increment this number
	// then we compare this to the length of desired
	// unit types, and if they are equal, we are done
	BOOST_FOREACH(uti_t& uti, this->desiredUnitCounts) {
		currUnitCount = 0;
		BOOST_FOREACH(BWAPI::Unit* u, this->availableUnits) {

			// use string compare to compare BWAPI::UnitTypes
			if (std::strcmp(u->getType().getName().c_str(), uti.first.getName().c_str()) == 0) {
				// if they match, increment count
				currUnitCount++;
			} 
		}

		if (currUnitCount >= uti.second) {
			numUnitTypesSatisfied++;
		}
	}

	// did we meet all requirements for all desired units?
	return (numUnitTypesSatisfied*1.0)/(this->desiredUnitCounts.size());
}

//void PlanStep::setCombatCommander(CombatCommander cc) {
//	this->combatCommander = cc;
//}

bool PlanStep::hasStarted() {
	return this->hasBegunExecution;
}

std::set<BWAPI::Unit*> PlanStep::getUnits() {
	return this->availableUnits;
}

std::set<BWAPI::Unit*> PlanStep::endStep() {
	this->hasBegunExecution = false;
	return releaseUnits();
}

std::set<BWAPI::Unit*> PlanStep::releaseUnits() {
	// cycle through all units, removing any that are dead. Any that are still alive, return them
	for (std::set<BWAPI::Unit*>::iterator u = units.begin(); u != units.end(); u++) {
		// TODO (DTD): may want to also ignore units that are stuck, froze, stasis, etc
		if ((*u)->getHitPoints() <= 0) {
			units.erase((*u));
		}
	}
	return units;
}

int PlanStep::getPlanID() {
	return this->planID;
}

//Returns true if this plan step still needs a unit of this type
// NOTE: plan steps can only be given units if they are of the PRODUCE_UNITS
// or ACQUIRE_UNITS step goal type
bool PlanStep::stillNeedUnitType(BWAPI::UnitType givenType) {
	if (!(this->stepGoal == PRODUCE_UNITS || this->stepGoal == ACQUIRE_UNITS)) {
		return false;
	}

	typedef std::pair<const BWAPI::UnitType, int> uti_t;
	int currUnitCount = 0;

	BOOST_FOREACH(uti_t& uti, desiredUnitCounts) {
		currUnitCount = 0;

		if (uti.first == givenType) { // we only care about the given type, ignore others
			BOOST_FOREACH(BWAPI::Unit* u, availableUnits) {

				// use string compare to compare BWAPI::UnitTypes
				if (std::strcmp(u->getType().getName().c_str(), uti.first.getName().c_str()) == 0) {
					// if they match, increment count
					currUnitCount++;
				} 
			}

			// if the givenType is one we're looking for, just return whether or not we need anymore
			return currUnitCount < uti.second;
		}
	}

	// if the given type didn't match any we need, return false
	return false;
}

void PlanStep::assignUnit(BWAPI::Unit* u) {
	/*MessageBoxA(NULL,
	("1. assignUnit, now this step has "+SSTR(availableUnits.size())+"").c_str(),
	this->getDisplayString().c_str()
	,
	MB_ICONINFORMATION|MB_OK);*/
	this->availableUnits.insert(u);
}

std::set<BWAPI::Unit*> PlanStep::getAvailableUnits() {
	return this->availableUnits;
}

void PlanStep::requestSentForNextPlanStep() {
	this->sentForNextPlanStep = true;
}

bool PlanStep::hasSentForNextPlanStep() {
	return this->sentForNextPlanStep;
}