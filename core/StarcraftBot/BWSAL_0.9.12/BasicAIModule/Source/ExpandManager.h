#pragma once
#include <BWAPI.h>
#include <BuildOrderManager.h>
#include <BaseManager.h>
#include "DebugPrinter.h"
#include <string>
#include <boost\foreach.hpp>
#include <BWTA.h>
//#include <iostream>

#include <sstream> // needed for SSTR definition below
#define SSTR( x ) dynamic_cast< std::ostringstream & >( \
	( std::ostringstream() << std::dec << x ) ).str()

class ExpandManager
{
public:
	ExpandManager(BuildOrderManager * buildOrderManager,BaseManager* baseManager);
	~ExpandManager(void);
	bool ExpandManager::newGoal(BWAPI::Position position);
	bool newGoal(BWTA::Region* region);
	void newGoal();//expand nearest base location
	void update();



private:
	BuildOrderManager * buildOrderManager;
	BaseManager * baseManager;
	//TechManager * techManager;



	//std::map<BWAPI::UnitType, int> composition;

	// this keeps track of how many units we are expecting so that we know when we need to refresh
	// this needs to be stored in the plan step not the productin manager because
	// there is moe than one plan step

    std::map<BWTA::BaseLocation*,Base*> location2base;
    std::set<Base*> allBases;

    int BaseCount;
};

