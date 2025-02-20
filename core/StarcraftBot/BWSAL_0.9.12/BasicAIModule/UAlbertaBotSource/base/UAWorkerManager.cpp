#include "../Common.h"
#include "UAWorkerManager.h"

UAWorkerManager::UAWorkerManager() : workersPerRefinery(3) {}

void UAWorkerManager::update() 
{
	// worker bookkeeping
	updateWorkerStatus();

	// set the gas workers
	handleGasWorkers();

	// handle idle workers
	handleIdleWorkers();

	// handle move workers
	handleMoveWorkers();

	// handle combat workers
	handleCombatWorkers();

	drawResourceDebugInfo();
	//drawWorkerInformation(450,20);

	//workerData.drawDepotDebugInfo();
}

void UAWorkerManager::updateWorkerStatus() 
{
	// for each of our Workers
	BOOST_FOREACH (BWAPI::Unit * worker, workerData.getWorkers())
	{
		if (!worker->isCompleted())
		{
			continue;
		}

		// if it's idle
		if (worker->isIdle() && 
			(workerData.getWorkerJob(worker) != WorkerData::Build) && 
			(workerData.getWorkerJob(worker) != WorkerData::Move) &&
			(workerData.getWorkerJob(worker) != WorkerData::Scout)) 
		{
			//printf("Worker %d set to idle", worker->getID());
			// set its job to idle
			workerData.setWorkerJob(worker, WorkerData::Idle, NULL);
		}

		// if its job is gas
		if (workerData.getWorkerJob(worker) == WorkerData::Gas)
		{
			BWAPI::Unit * refinery = workerData.getWorkerResource(worker);

			// if the refinery doesn't exist anymore
			if (!refinery || !refinery->exists() ||	refinery->getHitPoints() <= 0)
			{
				setMineralWorker(worker);
			}
		}
	}
}


void UAWorkerManager::handleGasWorkers() 
{
	// for each unit we have
	BOOST_FOREACH (BWAPI::Unit * unit, BWAPI::Broodwar->self()->getUnits())
	{
		// if that unit is a refinery
		if (unit->getType().isRefinery() && unit->isCompleted())
		{
			// get the number of workers currently assigned to it
			int numAssigned = workerData.getNumAssignedWorkers(unit);

			// if it's less than we want it to be, fill 'er up
			for (int i=0; i<(workersPerRefinery-numAssigned); ++i)
			{
				BWAPI::Unit * gasWorker = getGasWorker(unit);
				if (gasWorker)
				{
					workerData.setWorkerJob(gasWorker, WorkerData::Gas, unit);
				}
			}
		}
	}

}

void UAWorkerManager::handleIdleWorkers() 
{
	// for each of our workers
	BOOST_FOREACH (BWAPI::Unit * worker, workerData.getWorkers())
	{
		// if it is idle
		if (workerData.getWorkerJob(worker) == WorkerData::Idle) 
		{
			// send it to the nearest mineral patch
			setMineralWorker(worker);
		}
	}
}

// bad micro for combat workers
void UAWorkerManager::handleCombatWorkers()
{
	BOOST_FOREACH (BWAPI::Unit * worker, workerData.getWorkers())
	{
		if (workerData.getWorkerJob(worker) == WorkerData::Combat)
		{
			BWAPI::Broodwar->drawCircleMap(worker->getPosition().x(), worker->getPosition().y(), 4, BWAPI::Colors::Yellow, true);
			BWAPI::Unit * target = getClosestEnemyUnit(worker);

			if (target)
			{
				smartAttackUnit(worker, target);
			}
		}
	}
}

BWAPI::Unit * UAWorkerManager::getClosestEnemyUnit(BWAPI::Unit * worker)
{
	BWAPI::Unit * closestUnit = NULL;
	double closestDist = 10000;

	BOOST_FOREACH (BWAPI::Unit * unit, BWAPI::Broodwar->enemy()->getUnits())
	{
		double dist = unit->getDistance(worker);

		if ((dist < 400) && (!closestUnit || (dist < closestDist)))
		{
			closestUnit = unit;
			closestDist = dist;
		}
	}

	return closestUnit;
}

void UAWorkerManager::finishedWithCombatWorkers()
{
	BOOST_FOREACH (BWAPI::Unit * worker, workerData.getWorkers())
	{
		if (workerData.getWorkerJob(worker) == WorkerData::Combat)
		{
			setMineralWorker(worker);
		}
	}
}

void UAWorkerManager::handleMoveWorkers() 
{
	// for each of our workers
	BOOST_FOREACH (BWAPI::Unit * worker, workerData.getWorkers())
	{
		// if it is a move worker
		if (workerData.getWorkerJob(worker) == WorkerData::Move) 
		{
			WorkerMoveData data = workerData.getWorkerMoveData(worker);
			
			worker->move(data.position);
		}
	}
}

// set a worker to mine minerals
void UAWorkerManager::setMineralWorker(BWAPI::Unit * unit)
{
	if (unit == NULL) 
	{
		assert(false);
	}

	// check if there is a mineral available to send the worker to
	BWAPI::Unit * depot = getClosestDepot(unit);

	// if there is a valid mineral
	if (depot)
	{
		// update workerData with the new job
		workerData.setWorkerJob(unit, WorkerData::Minerals, depot);
	}
	else
	{
		// BWAPI::Broodwar->printf("No valid depot for mineral worker");
	}
}

BWAPI::Unit * UAWorkerManager::getClosestDepot(BWAPI::Unit * worker)
{
	if (worker == NULL) 
	{
		assert(false);
	}

	BWAPI::Unit * closestDepot = NULL;
	double closestDistance = 0;

	BOOST_FOREACH (BWAPI::Unit * unit, BWAPI::Broodwar->self()->getUnits())
	{
		if (unit->getType().isResourceDepot() && unit->isCompleted() && !workerData.depotIsFull(unit))
		{
			double distance = unit->getDistance(worker);
			if (!closestDepot || distance < closestDistance)
			{
				closestDepot = unit;
				closestDistance = distance;
			}
		}
	}

	return closestDepot;
}


// other managers that need workers call this when they're done with a unit
void UAWorkerManager::finishedWithWorker(BWAPI::Unit * unit) 
{
	if (unit == NULL)
	{
		BWAPI::Broodwar->printf("finishedWithWorker() called with NULL unit");
		return; 
	}

	//BWAPI::Broodwar->printf("BuildingManager finished with worker %d", unit->getID());
	if (workerData.getWorkerJob(unit) != WorkerData::Scout)
	{
		workerData.setWorkerJob(unit, WorkerData::Idle, NULL);
	}
}

BWAPI::Unit * UAWorkerManager::getGasWorker(BWAPI::Unit * refinery)
{
	if (refinery == NULL) 
	{
		assert(false);
	}

	BWAPI::Unit * closestWorker = NULL;
	double closestDistance = 0;

	BOOST_FOREACH (BWAPI::Unit * unit, workerData.getWorkers())
	{
		if (workerData.getWorkerJob(unit) == WorkerData::Minerals)
		{
			double distance = unit->getDistance(refinery);
			if (!closestWorker || distance < closestDistance)
			{
				closestWorker = unit;
				closestDistance = distance;
			}
		}
	}

	return closestWorker;
}

// gets a builder for BuildingManager to use
// if setJobAsBuilder is true (default), it will be flagged as a builder unit
// set 'setJobAsBuilder' to false if we just want to see which worker will build a building
BWAPI::Unit * UAWorkerManager::getBuilder(Building & b, bool setJobAsBuilder)
{
	// variables to hold the closest worker of each type to the building
	BWAPI::Unit * closestMovingWorker = NULL;
	BWAPI::Unit * closestMiningWorker = NULL;
	double closestMovingWorkerDistance = 0;
	double closestMiningWorkerDistance = 0;

	// look through each worker that had moved there first
	BOOST_FOREACH (BWAPI::Unit * unit, workerData.getWorkers())
	{
		// mining worker check
		if (unit->isCompleted() && (workerData.getWorkerJob(unit) == WorkerData::Minerals))
		{
			// if it is a new closest distance, set the pointer
			double distance = unit->getDistance(BWAPI::Position(b.finalPosition));
			if (!closestMiningWorker || distance < closestMiningWorkerDistance)
			{
				closestMiningWorker = unit;
				closestMiningWorkerDistance = distance;
			}
		}

		// moving worker check
		if (unit->isCompleted() && (workerData.getWorkerJob(unit) == WorkerData::Move))
		{
			// if it is a new closest distance, set the pointer
			double distance = unit->getDistance(BWAPI::Position(b.finalPosition));
			if (!closestMovingWorker || distance < closestMovingWorkerDistance)
			{
				closestMovingWorker = unit;
				closestMovingWorkerDistance = distance;
			}
		}
	}

	// if we found a moving worker, use it, otherwise using a mining worker
	BWAPI::Unit * chosenWorker = closestMovingWorker ? closestMovingWorker : closestMiningWorker;

	// if the worker exists (one may not have been found in rare cases)
	if (chosenWorker && setJobAsBuilder)
	{
		workerData.setWorkerJob(chosenWorker, WorkerData::Build, b.type);
	}

	// return the worker
	return chosenWorker;
}

// sets a worker as a scout
void UAWorkerManager::setScoutWorker(BWAPI::Unit * worker)
{
	if (worker == NULL) 
	{
		assert(false);
	}

	workerData.setWorkerJob(worker, WorkerData::Scout, NULL);
}

// gets a worker which will move to a current location
BWAPI::Unit * UAWorkerManager::getMoveWorker(BWAPI::Position p)
{
	// set up the pointer
	BWAPI::Unit * closestWorker = NULL;
	double closestDistance = 0;

	// for each worker we currently have
	BOOST_FOREACH (BWAPI::Unit * unit, workerData.getWorkers())
	{
		// only consider it if it's a mineral worker
		if (unit->isCompleted() && workerData.getWorkerJob(unit) == WorkerData::Minerals)
		{
			// if it is a new closest distance, set the pointer
			double distance = unit->getDistance(p);
			if (!closestWorker || distance < closestDistance)
			{
				closestWorker = unit;
				closestDistance = distance;
			}
		}
	}

	// return the worker
	return closestWorker;
}

// sets a worker to move to a given location
void UAWorkerManager::setMoveWorker(int mineralsNeeded, int gasNeeded, BWAPI::Position p)
{
	// set up the pointer
	BWAPI::Unit * closestWorker = NULL;
	double closestDistance = 0;

	// for each worker we currently have
	BOOST_FOREACH (BWAPI::Unit * unit, workerData.getWorkers())
	{
		// only consider it if it's a mineral worker
		if (unit->isCompleted() && workerData.getWorkerJob(unit) == WorkerData::Minerals)
		{
			// if it is a new closest distance, set the pointer
			double distance = unit->getDistance(p);
			if (!closestWorker || distance < closestDistance)
			{
				closestWorker = unit;
				closestDistance = distance;
			}
		}
	}

	if (closestWorker)
	{
		//BWAPI::Broodwar->printf("Setting worker job Move for worker %d", closestWorker->getID());
		workerData.setWorkerJob(closestWorker, WorkerData::Move, WorkerMoveData(mineralsNeeded, gasNeeded, p));
	}
	else
	{
		//BWAPI::Broodwar->printf("Error, no worker found");
	}
}

// will we have the required resources by the time a worker can travel a certain distance
bool UAWorkerManager::willHaveResources(int mineralsRequired, int gasRequired, double distance)
{
	// if we don't require anything, we will have it
	if (mineralsRequired <= 0 && gasRequired <= 0)
	{
		return true;
	}

	// the speed of the worker unit
	double speed = BWAPI::Broodwar->self()->getRace().getWorker().topSpeed();

	// how many frames it will take us to move to the building location
	// add a second to account for worker getting stuck. better early than late
	double framesToMove = (distance / speed) + 50;

	// magic numbers to predict income rates
	double mineralRate = getNumMineralWorkers() * 0.045;
	double gasRate     = getNumGasWorkers() * 0.07;

	// calculate if we will have enough by the time the worker gets there
	if (mineralRate * framesToMove >= mineralsRequired &&
		gasRate * framesToMove >= gasRequired)
	{
		return true;
	}
	else
	{
		return false;
	}
}

void UAWorkerManager::setCombatWorker(BWAPI::Unit * worker)
{
	if (worker == NULL) 
	{
		assert(false);
	}

	workerData.setWorkerJob(worker, WorkerData::Combat, NULL);
}

void UAWorkerManager::onUnitMorph(BWAPI::Unit * unit)
{
	if (unit == NULL) 
	{
		assert(false);
	}

	// if something morphs into a worker, add it
	if (unit->getType().isWorker() && unit->getPlayer() == BWAPI::Broodwar->self() && unit->getHitPoints() >= 0)
	{
		workerData.addWorker(unit);
	}

	// if something morphs into a building, it was a worker?
	if (unit->getType().isBuilding() && unit->getPlayer() == BWAPI::Broodwar->self() && unit->getPlayer()->getRace() == BWAPI::Races::Zerg)
	{
		//BWAPI::Broodwar->printf("A Drone started building");
		workerData.workerDestroyed(unit);
	}
}

void UAWorkerManager::onUnitShow(BWAPI::Unit * unit)
{
	if (unit == NULL) 
	{
		assert(false);
	}

	// add the depot if it exists
	if (unit->getType().isResourceDepot() && unit->getPlayer() == BWAPI::Broodwar->self())
	{
		workerData.addDepot(unit);
	}

	// if something morphs into a worker, add it
	if (unit->getType().isWorker() && unit->getPlayer() == BWAPI::Broodwar->self() && unit->getHitPoints() >= 0)
	{
		//BWAPI::Broodwar->printf("A worker was shown %d", unit->getID());
		workerData.addWorker(unit);
	}
}


void UAWorkerManager::rebalanceWorkers()
{
	// for each worker
	BOOST_FOREACH (BWAPI::Unit * worker, workerData.getWorkers())
	{
		// we only care to rebalance mineral workers
		if (!workerData.getWorkerJob(worker) == WorkerData::Minerals)
		{
			continue;
		}

		// get the depot this worker works for
		BWAPI::Unit * depot = workerData.getWorkerDepot(worker);

		// if there is a depot and it's full
		if (depot && workerData.depotIsFull(depot))
		{
			// set the worker to idle
			workerData.setWorkerJob(worker, WorkerData::Idle, NULL);
		}
		// if there's no depot
		else if (!depot)
		{
			// set the worker to idle
			workerData.setWorkerJob(worker, WorkerData::Idle, NULL);
		}
	}
}

void UAWorkerManager::onUnitDestroy(BWAPI::Unit * unit) 
{
	if (unit == NULL) 
	{
		assert(false);
	}

	// remove the depot if it exists
	if (unit->getType().isResourceDepot() && unit->getPlayer() == BWAPI::Broodwar->self())
	{
		workerData.removeDepot(unit);
	}

	// if the unit that was destroyed is a worker
	if (unit->getType().isWorker() && unit->getPlayer() == BWAPI::Broodwar->self()) 
	{
		// tell the worker data it was destroyed
		workerData.workerDestroyed(unit);
	}

	if (unit->getType() == BWAPI::UnitTypes::Resource_Mineral_Field)
	{
		//BWAPI::Broodwar->printf("A mineral died, rebalancing workers");

		rebalanceWorkers();
	}
}

void UAWorkerManager::smartAttackUnit(BWAPI::Unit * attacker, BWAPI::Unit * target)
{
	// if we have issued a command to this unit already this frame, ignore this one
	if (attacker->getLastCommandFrame() >= BWAPI::Broodwar->getFrameCount() || attacker->isAttackFrame())
	{
		return;
	}

	// get the unit's current command
	BWAPI::UnitCommand currentCommand(attacker->getLastCommand());

	// if we've already told this unit to attack this target, ignore this command
	if (currentCommand.getType() == BWAPI::UnitCommandTypes::Attack_Unit &&	currentCommand.getTarget() == target)
	{
		return;
	}

	// if nothing prevents it, attack the target
	attacker->attack(target);
}

void UAWorkerManager::drawResourceDebugInfo() {

	BOOST_FOREACH (BWAPI::Unit * worker, workerData.getWorkers()) {

		char job = workerData.getJobCode(worker);

		BWAPI::Position pos = worker->getTargetPosition();

		if (Options::Debug::DRAW_UALBERTABOT_DEBUG) BWAPI::Broodwar->drawTextMap(worker->getPosition().x(), worker->getPosition().y() - 5, "\x07%c", job);

		if (Options::Debug::DRAW_UALBERTABOT_DEBUG) BWAPI::Broodwar->drawLineMap(worker->getPosition().x(), worker->getPosition().y(), pos.x(), pos.y(), BWAPI::Colors::Cyan);

		BWAPI::Unit * depot = workerData.getWorkerDepot(worker);
		if (depot)
		{
			if (Options::Debug::DRAW_UALBERTABOT_DEBUG) BWAPI::Broodwar->drawLineMap(worker->getPosition().x(), worker->getPosition().y(), depot->getPosition().x(), depot->getPosition().y(), BWAPI::Colors::Orange);
		}
	}
}

void UAWorkerManager::drawWorkerInformation(int x, int y) {

	if (Options::Debug::DRAW_UALBERTABOT_DEBUG) BWAPI::Broodwar->drawTextScreen(x, y, "\x04 Workers %d", workerData.getNumMineralWorkers());
	if (Options::Debug::DRAW_UALBERTABOT_DEBUG) BWAPI::Broodwar->drawTextScreen(x, y+20, "\x04 UnitID");
	if (Options::Debug::DRAW_UALBERTABOT_DEBUG) BWAPI::Broodwar->drawTextScreen(x+50, y+20, "\x04 State");

	int yspace = 0;

	BOOST_FOREACH (BWAPI::Unit * unit, workerData.getWorkers())
	{
		if (Options::Debug::DRAW_UALBERTABOT_DEBUG) BWAPI::Broodwar->drawTextScreen(x, y+40+((yspace)*10), "\x03 %d", unit->getID());
		if (Options::Debug::DRAW_UALBERTABOT_DEBUG) BWAPI::Broodwar->drawTextScreen(x+50, y+40+((yspace++)*10), "\x03 %c", workerData.getJobCode(unit));
	}

}

bool UAWorkerManager::isFree(BWAPI::Unit * worker)
{
	return workerData.getWorkerJob(worker) == WorkerData::Minerals || workerData.getWorkerJob(worker) == WorkerData::Idle;
}

bool UAWorkerManager::isWorkerScout(BWAPI::Unit * worker)
{
	return (workerData.getWorkerJob(worker) == WorkerData::Scout);
}

bool UAWorkerManager::isBuilder(BWAPI::Unit * worker)
{
	return (workerData.getWorkerJob(worker) == WorkerData::Build);
}

int UAWorkerManager::getNumMineralWorkers() 
{
	return workerData.getNumMineralWorkers();	
}

int UAWorkerManager::getNumIdleWorkers() 
{
	return workerData.getNumIdleWorkers();	
}

int UAWorkerManager::getNumGasWorkers() 
{
	return workerData.getNumGasWorkers();
}


UAWorkerManager & UAWorkerManager::Instance() {

	static UAWorkerManager instance;
	return instance;
}