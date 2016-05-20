#pragma once
#include "PlanExecutor.h"
#include "Arbitrator.h"
#include "../Addons/Util.h"
//#include "PlannerClient.h"

// Only wrote this function so that I could compile the DLL.cpp
PlanExecutor::PlanExecutor() {
	log("\nCalling constructor of PlanExecutor()");
	//this->arbitrator = NULL;
	this->currPlanStep = NULL;
	this->currPlanSteps = std::vector<PlanStep*>();
}

PlanExecutor::PlanExecutor(Arbitrator::Arbitrator<BWAPI::Unit*,double>* arbitrator, UnitProductionManager* unitProductionManager,ExpandManager* expandManager, BaseManager * baseManager) {
	log("\nCalling constructor of PlanExecutor(arbitrator)");
	this->arbitrator = arbitrator;
	this->unitProductionManager = unitProductionManager;
	this->expandManager = expandManager;
	this->baseManager = baseManager;
	log("\n just set arbitrator in constructor, arbitraor is now %s",this->arbitrator);
	//this->plannerClient = PlannerClient();
	this->plannerClient = new PlannerClient();
	this->plannerClient->setPlanExecutor(this);
	this->plannerClient->start();
	this->listeningForNewPlanSteps = true;
	this->currPlanStep = NULL;
	this->currPlanSteps = std::vector<PlanStep*>();
	this->requestSent = false;
}

void PlanExecutor::beginAskingForPlanSteps(){

	//construct a new plan step from the string received from the server
	this->requestSent = true;
	this->plannerClient->requestNextPlanStep(this->currPlanStep);

	//this->currPlanStep = new PlanStep(this->plannerClient->getCurrentPlanStepStr());

	// allows the plan step to issue orders to produce units 
	//this->currPlanStep->setUnitProductionManager(this->unitProductionManager);

	// start executing step actions
	//this->currPlanStep->beginExecution();
}

void PlanExecutor::createNewPlanStep(std::string newStepStr) {
	this->requestSent = false; // turn this off once we get the step we requested (here)

	// create a new plan step
	this->currPlanStep = new PlanStep(newStepStr);

	// check to see if any current (old) plan steps have the same plan id,
	// and if so, give this planstep its prev plan steps units, and remove the old plan step
	BOOST_FOREACH (PlanStep* p, this->currPlanSteps) {
		/*MessageBoxA(NULL,
		("In CreateNewPlanStep loop, currpID = "+SSTR(p->getPlanID())+" newPID = "+SSTR(currPlanStep->getPlanID())).c_str(),
		"title",
		MB_ICONINFORMATION|MB_OK);*/
		if (p->getPlanID() == this->currPlanStep->getPlanID()) {
			this->currPlanStep->setAvailableUnits(p->getAvailableUnits()); // copy over prev units
			//this->currPlanStep->setTotalBaseCount();

			//std::remove(this->currPlanSteps.begin(), this->currPlanSteps.end(), p); // delete old step
			/*MessageBoxA(NULL,
			("Just deleted old plan step, currPlanSteps size is "+SSTR(currPlanSteps.size())).c_str(),
			"title",
			MB_ICONINFORMATION|MB_OK);*/
		}	
	}

	// BEGIN HACKY WAY TO DELETE
	// hacky way to delete currplan step but 'better' way was not working before
	int i = 0;
	bool foundOldMatchingPlanStep = false;
	while (i < currPlanSteps.size()) {
		if (currPlanSteps.at(i)->getPlanID() == this->currPlanStep->getPlanID()) {
			foundOldMatchingPlanStep = true;
			break;
		}else{
			i++;
		}
	}

	// delete old step
	if (foundOldMatchingPlanStep) {
		this->currPlanSteps.erase(this->currPlanSteps.begin()+i);
	}
	// END HACKY WAY TO DELETE

	// give unit production manager
	this->currPlanStep->setUnitProductionManager(this->unitProductionManager);
	this->currPlanStep->setExpandManager(this->expandManager);
	this->currPlanStep->setBaseManager(this->baseManager);
	// START this plan step immediately
	this->currPlanStep->beginExecution();

	// add plan step to the current vector of plan steps
	this->currPlanSteps.push_back(currPlanStep);

}

void PlanExecutor::update() {
	// Bid on all completed military units

	// SINCE WE KNOW THAT WE WILL GET EVERY COMPLETED UNIT, KEEP TRACK OF NEW UNITS FOR ALLOCATION
	std::vector<BWAPI::Unit*> newUnits;

	std::set<BWAPI::Unit*> myPlayerUnits=BWAPI::Broodwar->self()->getUnits();
	for (std::set<BWAPI::Unit*>::iterator u = myPlayerUnits.begin(); u != myPlayerUnits.end(); u++)
	{
		if (this->unassignedAvailableUnits.find((*u)) == this->unassignedAvailableUnits.end() &&
			this->assignedAvailableUnits.find((*u)) == this->assignedAvailableUnits.end() &&
			// don't bid if we alreayd have
			(*u)->isCompleted() && 
			!(*u)->getType().isWorker() && 
			!(*u)->getType().isBuilding() &&
			(*u)->getType() != BWAPI::UnitTypes::Zerg_Egg &&
			(*u)->getType() != BWAPI::UnitTypes::Zerg_Larva)
		{
			log("\nPlanExecutor about to bid for unit id = %d, type = %s", (*u)->getID(), (*u)->getType().getName().c_str());
			log("\narbitrator is %s", this->arbitrator);
			this->arbitrator->setBid(this, *u, 90);
			newUnits.push_back(*u);
			log("\nPlanExecutor just sent bid for unit id = %d", (*u)->getID());
			//BWAPI::Broodwar->sendText("SlowPushManager bid on another unit!");
		}
	}

	/*if (BWAPI::Broodwar->getFrameCount() % 150 == 0) {
	MessageBoxA(NULL,
	"title",
	("There are "+SSTR(this->unassignedAvailableUnits.size()) +" unassignedAvailableUnits").c_str(),
	MB_ICONINFORMATION|MB_OK);
	}*/


	// if any plan step needs any unassigned units, and we have those units, give it to that plan step




	BOOST_FOREACH (BWAPI::Unit* newUnit, newUnits) {
		BOOST_FOREACH (PlanStep* p, this->currPlanSteps) {
			if (p->stillNeedUnitType(newUnit->getType())) {
				/*MessageBoxA(NULL,
				("Plan ID "+SSTR(p->getPlanID())+" about to be assigned "+newUnit->getType().getName().c_str()+" "+SSTR(newUnit->getID())).c_str(),
				"title",
				MB_ICONINFORMATION|MB_OK);*/
				p->assignUnit(newUnit);


				this->assignedAvailableUnits.insert(newUnit);

				// now delete
				this->unassignedAvailableUnits.erase(newUnit);
				break;// Only make one assignment 

				//this->unassignedAvailableUnits.erase(u);
				/*MessageBoxA(NULL,
				"Finished asignment",
				"title",
				MB_ICONINFORMATION|MB_OK);*/
				//break;
				//
				/*MessageBoxA(NULL,
				"Finished assignedAvailableUnits.insert(u)",
				"title",
				MB_ICONINFORMATION|MB_OK);*/
			}
		} 
	}

	// update all plan steps and request new steps
	BOOST_FOREACH (PlanStep *p, currPlanSteps) {
		p->updateStep();
		//switch (p->getStepGoal()) {
	//	case p->EXPAND_CLOSEST:

		//break;
	//	}

		if (p->hasFinishedExecution() && !p->hasSentForNextPlanStep()) {
			this->plannerClient->requestNextPlanStep(p);
			p->requestSentForNextPlanStep(); // so that you only send once per plan step
			/*MessageBoxA(NULL,
			"PlanStep has finished execution, request sent for next plan step",
			"title",
			MB_ICONINFORMATION|MB_OK);*/
		}
	}


	// SUPER HACKY WAY TO REMOVE PLAN STEPS THAT HAVE BEEN ABORTED

	bool doneAborting = false;
	// true if we have any plans to abort
	bool havePlanToAbort = false;
	std::vector<PlanStep*>::iterator planStepToAbort;

	while (!doneAborting) {

		// update all plan steps
		BOOST_FOREACH (PlanStep *p, currPlanSteps) {

			// if plan has been aborted, just remove it
			if (p->planHasBeenAborted()) {

				// BEGIN HACKY WAY TO DELETE
				// this is so hacky but UGH std::remove doens't work!!!!
				// hacky way to delete currplan step but 'better' way was not working before
				int i = 0;
				bool foundOldMatchingPlanStep = false;
				while (i < currPlanSteps.size()) {
					if (currPlanSteps.at(i)->getPlanID() == p->getPlanID()) {
						foundOldMatchingPlanStep = true;
						break;
					}else{
						i++;
					}
				}

				// delete old step
				if (foundOldMatchingPlanStep) {
					havePlanToAbort = true;
					planStepToAbort = this->currPlanSteps.begin()+i;
					//this->currPlanSteps.erase();
					//haveWeAborted = true;
				}
				// END HACKY WAY TO DELETE
			}


			/*if (haveWeAborted) {
			MessageBoxA(NULL,
			"Out of plan step update loop",
			"title",
			MB_ICONINFORMATION|MB_OK);
			}*/

			// if plan has finished or needs to be aborted, handle it here
			// TO DO

		}

		if (havePlanToAbort) {
			this->currPlanSteps.erase(planStepToAbort);
			havePlanToAbort = false; // reset
		}else{
			doneAborting = true;
		}


	}

	// OLD CODE FOR HANDLING GETTING A NEW PLAN STEP IF IT WAS ABORTED OR FINISHED ``````V

	//// if plan was aborted, request new one
	//if (this->currPlanStep != NULL && this->currPlanStep->planHasBeenAborted()) {
	//	this->requestSent = true; 
	//	this->plannerClient->requestNextPlanStep(this->currPlanStep);

	//	// update current plan step

	//	// get next plan step if curr plan step has finished
	//}else if (this->currPlanStep != NULL && this->currPlanStep->hasFinishedExecution() 
	//	&& !this->requestSent) { // to prevent multiple requests 

	//		this->requestSent = true; 
	//		this->plannerClient->requestNextPlanStep(this->currPlanStep);
	//		// otherwise update
	//}else if (this->currPlanStep != NULL) {


	//}

	/*MessageBoxA(NULL,
	("There are currently " + SSTR(currPlanSteps.size()) + " being updated").c_str(),
	"1. calling begin Execution()",
	MB_ICONINFORMATION|MB_OK);*/
}


PlanStep* PlanExecutor::getCurrPlanStep() {
	return this->currPlanStep;
}

std::string PlanExecutor::getName() const {
	return "Bot Executor";
}

std::string PlanExecutor::getShortName() const {
	return "BotExctr";
}



// to do - fill this in
void PlanExecutor::onOffer(std::set<BWAPI::Unit*> units) {
	for(std::set<BWAPI::Unit*>::iterator u = units.begin(); u != units.end(); u++)
	{
		//log("\nonOffer called with unit id=%d",(*u)->getID());
		if (unassignedAvailableUnits.find(*u) == unassignedAvailableUnits.end() && 
			assignedAvailableUnits.find(*u) == assignedAvailableUnits.end())
		{
			log("\nPlanExecutor1 about to accept unit id = %d, type = %s", (*u)->getID()), (*u)->getType().getName();
			this->arbitrator->accept(this, *u);
			log("\nPlanExecutor just accepted unit id = %d",(*u)->getID());
			unassignedAvailableUnits.insert(*u);
		}
	}

	// everytime a unit is added to our available units, give that to the current plan step
	// ONLY if the curr plan step is to acquire units, and it has not finished
	// TODO - perhaps there is a better way to do this
	/*if (this->currPlanStep->getStepGoal() == PlanStep::PRODUCE_UNITS && !this->currPlanStep->hasFinishedExecution()) {
	this->currPlanStep->setAvailableUnits(availableUnits);
	}*/

}

void PlanExecutor::onRevoke(BWAPI::Unit* unit, double bid) {

}


void PlanExecutor::onRemoveUnit(BWAPI::Unit* unit) {


}

void PlanExecutor::closePlannerServerConnection() {
	this->plannerClient->close();
}

// Write the plan steps on the screen
void PlanExecutor::displayPlanSteps() {
	int yVal = 17;
	// always show current plan step on screen (if debug mode)
	BOOST_FOREACH (PlanStep* p, this->currPlanSteps) {
		BWAPI::Broodwar->drawTextScreen(90,yVal,"\x07 CurrPlanStep: \x1B%s",p->getDisplayString().c_str());
		yVal += 10;
	}
	BWAPI::Broodwar->drawTextScreen(90,yVal,("\x07 CurrPlanSteps Size: \x1B "+SSTR(this->currPlanSteps.size())).c_str());
}
