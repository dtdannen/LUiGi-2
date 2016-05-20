#pragma once
#include <BWAPI.h>
#include <BuildOrderManager.h>
#include <BaseManager.h>
#include "DebugPrinter.h"
#include <string>
#include <boost\foreach.hpp>
//#include <iostream>
#include <BWTA.h>
#include <sstream> // needed for SSTR definition below
#define SSTR( x ) dynamic_cast< std::ostringstream & >( \
	( std::ostringstream() << std::dec << x ) ).str()

class UnitProductionManager
{
public:
	UnitProductionManager(BuildOrderManager * buildOrderManager);
	~UnitProductionManager(void);

	void newGoal(std::map<BWAPI::UnitType, int> composition);
	void update();
	void refresh(); 
	bool readyToRefresh();
	void onUnitComplete(BWAPI::Unit* unit);


private:
	BuildOrderManager * buildOrderManager;
	std::map<BWAPI::UnitType, int> composition;

	// this keeps track of how many units we are expecting so that we know when we need to refresh
	// this needs to be stored in the plan step not the productin manager because
	// there is moe than one plan step
	std::map<BWAPI::UnitType, int> expectedCounts;

	std::map<BWAPI::UnitType, int> unitsCompleted;
};

