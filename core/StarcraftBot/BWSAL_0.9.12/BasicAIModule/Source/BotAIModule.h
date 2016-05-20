#ifndef BOT_AI_MODULE
#define BOT_AI_MODULE

#pragma once
#define NOMINMAX
#include <BWAPI.h>
#include <BWTA.h>
#include <sstream>
#include <iostream>
#include <fstream>
#include <Arbitrator.h>
#include <WorkerManager.h>
#include <SupplyManager.h>
#include <BuildManager.h>
#include <BuildOrderManager.h>
#include <TechManager.h>
#include <ScoutManager.h>
#include <UpgradeManager.h>
#include <BorderManager.h>
#include <InformationManager.h>
#include <UnitGroupManager.h>
//#include "MicroSearchManager.h"
#include "UnitProductionManager.h"
#include "GamestateDumper.h"
#include "PlanExecutor.h"
#include "ExpandManager.h"


class BotAIModule : public BWAPI::AIModule
{
public:
	BotAIModule();
	virtual void onStart();
	virtual void onEnd(bool isWinner);
	virtual void onFrame();
	virtual void onUnitDiscover(BWAPI::Unit* unit);
	virtual void onUnitEvade(BWAPI::Unit* unit);
	virtual void onUnitMorph(BWAPI::Unit* unit);
	virtual void onUnitRenegade(BWAPI::Unit* unit);
	virtual void onUnitDestroy(BWAPI::Unit* unit);
	virtual void onSendText(std::string text);
	virtual void onUnitComplete(BWAPI::Unit* unit);
	~BotAIModule(); //not part of BWAPI::AIModule
	void showStats(); //not part of BWAPI::AIModule
	void showPlayers();
	void showForces();
	bool analyzed;
	std::map<BWAPI::Unit*,BWAPI::UnitType> buildings;

	// returns a JSON formatted string that can be turned into RDF triples easily
	void pullDataAndWriteToFile(std::string, int);
	void writeToFile(std::string, int frame, std::string);

	Arbitrator::Arbitrator<BWAPI::Unit*,double> arbitrator;
	WorkerManager* workerManager;
	SupplyManager* supplyManager;
	BuildManager* buildManager;
	TechManager* techManager;
	UpgradeManager* upgradeManager;
	BaseManager* baseManager;
	ScoutManager* scoutManager;
	BuildOrderManager* buildOrderManager;
	//DefenseManager* defenseManager;
	InformationManager* informationManager;
	BorderManager* borderManager;
	UnitGroupManager* unitGroupManager;
	//EnhancedUI* enhancedUI;
	UnitProductionManager * unitProductionManager;
	//SlowPushManager * slowPushManager;
	//ChokePointAdvisor * chokePointAdvisor;
	ExpandManager * expandManager;
	//MicroSearchManager		micro;
	
	static int numberOfServerRequests;
	
	static int nPort;
	static std::string gameStateDumperSettingsFileName;

private:

	// represents the current frame, updated every time onFrame() is called
	int currFrame;
	int pullFrequency;
	GamestateDumper gsDumper;

	// our units
	std::vector<BWAPI::Unit*> fightingUnits;

	// Used for executing plan steps
	PlanExecutor* planExecutor;
	//PlanExecutor planExecutor;

	bool assignedWorkerToAttack;

};
#endif BOT_AI_MODULE