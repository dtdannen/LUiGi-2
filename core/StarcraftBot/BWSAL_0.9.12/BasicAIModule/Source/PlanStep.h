// Author: Dustin Dannenhauer
// This class represents a single plan step, and maintains all the units involved in executing the
// step.
//
// Notes:
// 1. Every plan step will have some set of units it is working with, either producing them
//    or moving the, attacking with them, etc. They need to be passed on to the next plan step
#pragma once
#define NOMINMAX
#include "BWAPI.h"
#include "BWTA.h"
#include <map>
//#include <boost\foreach.hpp>
//#include <boost\tokenizer.hpp>
//#include <boost\lexical_cast.hpp>
//#include <boost\format.hpp>
#include "UnitProductionManager.h"
#include "AttackClosestScript.h"
#include "ExpandManager.h"
#include "ExpandAndMine.h"
//#include "CombatCommander.h"

// macro for converting ints to strings
#define SSTR( x ) dynamic_cast< std::ostringstream & >( \
	( std::ostringstream() << std::dec << x ) ).str()

class PlanStep {

public:
	// PRODUCE_UNITS:  Goal is to create (order building of) a group of units for later plan
	//                 steps and assemble them in one place
	// ACQUIRE_UNITS:  Goal is to obtain the units by id, from list of id's from server
	// MOVE_UNITS:     Goal is to move the units to a target region - actual movement depends on 
	//                 StepUnitMovement value
	// ENGAGE:         Goal is to attack or defend the region - will make use of UAlbertaBot micro
	//                 management for battle
	// ENGAGE_WORKERS: Goal is to attack worker units until they are all killed, at the location- will make use of UAlbertaBot micro
	//                 management for battle


	enum StepGoal { DEFEND_BASE_BUNKER,PRODUCE_UNITS, ACQUIRE_UNITS, MOVE_UNITS, ATTACK_MOVE_UNITS, ATTACK_MOVE_UNITS_SURROUND, ENGAGE, ENGAGE_WORKERS, CORNER_MOVE_UNITS,EXPAND_ClOSEST,EXPAND_SPECIFIC,EXPAND_CLOSEST,DEFEND_MOVE_UNITS };

	// ATTACK:         The style of battle will be to attack the enemy to remove them from the region
	// DEFEND:         The style of battle will be to defend the region without pursuing the enemy
	enum StepFightStyle { ATTACK, DEFEND,EXPAND };

	// GROUND:         Our units will only be ground units (i.e. Marines)
	//                 Note: this does not exclude dropships for certain steps
	// AIR:            Our units will only be air units (i.e. Wraiths)
	// GROUND_AND_AIR: Our units will be both ground and air units (i.e. Marines and Wraiths)
	enum StepUnitType { GROUND, AIR, GROUND_AND_AIR };

	// DIRECT:         Units will directly move to the enemy (if air, this means flying in a
	//                 straight line, if ground, it means traveling over land followigng the 
	//                 path to the destination)
	// SURROUND:       Units will surround the target region
	// STEALTH:        Units will attempt to travel to target region without being noticed by enemy
	//                 units in other regions (i.e. flying near the edges of the map)
	enum StepUnitMovement { DIRECT, SURROUND, STEALTH };

	PlanStep(); // necessary so we know if we have created a plan step or not
	// Constructs a new plan step from the message received by GDA server
	PlanStep(std::string);
	PlanStep(std::set<BWAPI::Unit*>, BWTA::Region* targetRegion, StepGoal, StepFightStyle, StepUnitType, StepUnitMovement);
	
	// begins executing the step
	void beginExecution();
	bool hasFinishedExecution();
	void setUnitProductionManager(UnitProductionManager*);
	void setExpandManager(ExpandManager *);
	void setBaseManager(BaseManager*);

	void updateStep();

	void setAvailableUnits(std::set<BWAPI::Unit*>);
	void getResourceAtRegion(BWTA::Region* a);
	void reProduce();
	StepGoal getStepGoal();
	StepFightStyle getStepFightStyle();

	// force the plan step to end - needs to be called when switching plans (i.e. if a discrepancy
	// occurs)
	// RETURNS the remaining alive units for this step
	std::set<BWAPI::Unit*> endStep();

	// returns true if this plan step has begun execution
	bool hasStarted();

	std::string getDisplayString();

	std::set<BWAPI::Unit*> getUnits();

	//void setCombatCommander(CombatCommander);
	bool planHasBeenAborted();

	int getPlanID();

	bool stillNeedUnitType(BWAPI::UnitType givenType);

	// gives this plan step a unit to use
	void assignUnit(BWAPI::Unit* u);

	std::set<BWAPI::Unit*> getAvailableUnits();

	void requestSentForNextPlanStep();
	bool hasSentForNextPlanStep();
	void setTotalBaseCount(int a);



	int  Enemy_Scale , Our_Scale , StillMoving;
	std::map<BWAPI::UnitType , int> How_Many_Died;
	int Total_Died;
	int Total_Died_Previous;


	int Enemy_Scale_Previous;
	bool IsProducing();

private:
	bool hasBegunExecution;
	bool hasFinishedExecutionFlag;
	bool abortedPlan;
	bool sentForNextPlanStep;

	int allLoadedMarines;
	StepGoal stepGoal;
	StepFightStyle stepFightStyle;
	StepUnitType stepUnitType;
	StepUnitMovement stepUnitMovement;

	BWTA::Region* targetRegion;

	// units for this plan step
	std::set<BWAPI::Unit*> units;

	// release units (give them back to PlanExecutor.availableUnits)
	std::set<BWAPI::Unit*> releaseUnits();

	std::string displayString;

	// endExecution is only called within the step, and this puts the step into a
	// state of finished - so that the planner can ask for the next step
	void endExecution();

	///////////////////////////////////////
	// PRODUCE_UNITS related

	// a map of unit types to counts, which is used in Acquire_Units plan steps
	// represents how many of each type of unit we desire
	std::map<BWAPI::UnitType, int> desiredUnitCounts;
	std::map<BWAPI::UpgradeType, int> desiredUpgradeCounts;
	UnitProductionManager* unitProductionManager;
	ExpandManager* expandManager ;
	BaseManager * baseManager;
	std::set<BWAPI::Unit*> availableUnits;
	std::set<BWAPI::Unit*> Minerals;
	// boolean flag to let us know if we have started producing units yet
	bool alreadyIssuedUnitProductionGoal;

	// returns true if all the units in the desiredUnitCounts exist in our
	// availableUnits set
	bool haveProducedUnits();
	double haveProducedUnits2();
	bool haveConstructedBase();
	////////////////////////////////////////
	// ACQUIRE_UNITS related

	std::set<int> unitIdsToAcquire;

	///////////////////////////////////////
	// MOVE_UNITS related

	BWAPI::Position destination;
	BWAPI::Position ClosestBaseLocation;
	int closestBaseCount;
	int totalBaseCount;
	int BaseIssued;
	bool haveIssuedMoveOrder;
	bool unitsAtDestination;
	BWAPI::Position firstEntryIntoRegion; // so that move units won't clump near entry
	void moveUnits(bool attack, bool stealthFlight);
	void moveUnits(bool attack, BWAPI::Position* dest, std::set<BWAPI::Unit*> unitGrp, bool stealthFlight); 
	void moveUnitsIntoBunker(BWAPI::Position dest,int desired_bunker_number);
	int getBunkerUnitNumber(BWAPI::Unit* Bunker);
	///////////////////////////////////////
	// MOVE_UNITS_SURROUND related
	
	// NOTE: surroundDestinations should correspond to unitMoveGroups, in the sense that the first
	// set of units in unitMoveGroups should have the first destination in surroundDestinations
	std::vector<BWAPI::Position*> surroundDestinations;
	std::vector<std::set<BWAPI::Unit*>> unitMoveGroups; 
	bool alreadyComputedUnitGroups;
	///////////////////////////////////////
	// ENGAGE (attack or defend) related

	//CombatCommander combatCommander;
	bool alreadyIssuedAttackOrder;
	//SquadData squadData;
	bool alreadyCreatedSquad;
	bool alreadyIssuedBase;
	bool alreadyConstructedBase;
	// AttackClosestScript
	AttackClosestScript attackManager;
	ExpandAndMine expandAndMineManager;
	int destRegionId;

	// The plan that this step is part of
	int planID;
	int redo;
	int redo_bunker;
	BWAPI::Position destinationOfBunker;
	std::clock_t start;

};