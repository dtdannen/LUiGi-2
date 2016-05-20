// Author: Dustin Dannenhauer
// This class represents the component of the bot that manages units, executes individual plan
// steps, and notifies the discrepancy detector.
#ifndef PLAN_EXECUTOR
#define PLAN_EXECUTOR

#pragma once
#define NOMINMAX
#include "Arbitrator.h"
#include "BWAPI.h"
#include "PlannerClient.hpp"
//#include "CombatCommander.h"
#include "PlanStep.h" // must come after combat commander
#include "UnitProductionManager.h"
#include "BaseManager.h"

#define SSTR( x ) dynamic_cast< std::ostringstream & >( \
	( std::ostringstream() << std::dec << x ) ).str()

class PlannerClient;

class PlanExecutor : Arbitrator::Controller<BWAPI::Unit*,double> {

public:

	enum REQUEST_STATUS {CURRENTLY_EXECUTING_STEP, WAITING_FOR_REQUEST};

	PlanExecutor();
	PlanExecutor(Arbitrator::Arbitrator<BWAPI::Unit*,double>* arbitrator, UnitProductionManager* unitProductionManager, ExpandManager * expandManager, BaseManager * baseManager);
	
	virtual void update();
	virtual std::string getName() const;
	virtual std::string getShortName() const;
	virtual void onOffer(std::set<BWAPI::Unit*> units);
	virtual void onRevoke(BWAPI::Unit* unit, double bid);
	void onRemoveUnit(BWAPI::Unit* unit);

	// this is a method that starts its own thread, communicates through a server protocol with the
	// planner, and recieves new plan steps from the planner. When it recieves a new step, it 
	// updates the recievedNewPlanStep flag
	
	void beginAskingForPlanSteps();
	PlanStep* getCurrPlanStep();

	// UAlbertaBot's CombatCommander - responsible for executing attack and defend commands
	//CombatCommander combatCommander;

	// close connection of the plannerClientObject
	void closePlannerServerConnection();
	void createNewPlanStep(std::string);
	void displayPlanSteps();

private:
	Arbitrator::Arbitrator<BWAPI::Unit*,double>* arbitrator;
	std::set<BWAPI::Unit*> unassignedAvailableUnits; // have not been given to a plan step
	std::set<BWAPI::Unit*> assignedAvailableUnits; // HAVE been given to a plan step

	// when the planner gives us a new plan step, make this variable true
	bool recievedNewPlanStep;

	PlannerClient* plannerClient;
	bool listeningForNewPlanSteps;


	ExpandManager  * expandManager;
	UnitProductionManager* unitProductionManager;
	BaseManager* baseManager;
	//InformationManager* informationManager;
	// our current plan step object, to be created as soon as we get the new plan step
	PlanStep* currPlanStep;

	std::vector<PlanStep*> currPlanSteps;
	// as soon as we recieve a new plan step, the previous currPlanStep will become this 
	// prevPlanStep
	//PlanStep prevPlanStep; 
	bool requestSent;
};

#endif /*PLAN_EXECUTOR*/