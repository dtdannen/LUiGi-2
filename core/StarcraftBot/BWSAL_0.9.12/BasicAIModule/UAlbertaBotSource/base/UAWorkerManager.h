#pragma once

#include "../Common.h"
#include "BuildingManager.h"
#include "WorkerData.h"

class Building;

class UAWorkerManager {

	//static UAWorkerManager *		instance;

	WorkerData					workerData;

	int							workersPerRefinery;

	void						setMineralWorker(BWAPI::Unit * unit);

	UAWorkerManager();

public:

	void						update();
	void						onUnitDestroy(BWAPI::Unit * unit);
	void						onUnitMorph(BWAPI::Unit * unit);
	void						onUnitShow(BWAPI::Unit * unit);
	void						onUnitRenegade(BWAPI::Unit * unit);
	void						finishedWithWorker(BWAPI::Unit * unit);

	void						handleIdleWorkers();
	void						handleGasWorkers();
	void						handleMoveWorkers();
	void						handleCombatWorkers();
	void						finishedWithCombatWorkers();

	void						drawResourceDebugInfo();
	void						updateWorkerStatus();
	void						drawWorkerInformation(int x, int y);

	int							getNumMineralWorkers();
	int							getNumGasWorkers();
	int							getNumIdleWorkers();
	void						setScoutWorker(BWAPI::Unit * worker);

	bool						isWorkerScout(BWAPI::Unit * worker);
	bool						isFree(BWAPI::Unit * worker);
	bool						isBuilder(BWAPI::Unit * worker);

	BWAPI::Unit *				getBuilder(Building & b, bool setJobAsBuilder = true);
	BWAPI::Unit *				getMoveWorker(BWAPI::Position p);
	BWAPI::Unit *				getClosestDepot(BWAPI::Unit * worker);
	BWAPI::Unit *				getGasWorker(BWAPI::Unit * refinery);
	BWAPI::Unit *				getClosestEnemyUnit(BWAPI::Unit * worker);

	void						setMoveWorker(int m, int g, BWAPI::Position p);
	void						setCombatWorker(BWAPI::Unit * worker);
	void						smartAttackUnit(BWAPI::Unit * attacker, BWAPI::Unit * target);

	bool						willHaveResources(int mineralsRequired, int gasRequired, double distance);
	void						rebalanceWorkers();

	static UAWorkerManager &		Instance();

};
