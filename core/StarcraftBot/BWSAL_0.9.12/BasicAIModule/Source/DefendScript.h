#include "BWAPI.h"
#include "BWTA.h"
#include <map>
#include <boost\foreach.hpp>
#include <boost\tokenizer.hpp>
#include <boost\lexical_cast.hpp>
#include <boost\format.hpp>
#include "UnitProductionManager.h"

#define SSTR( x ) dynamic_cast< std::ostringstream & >( \
	( std::ostringstream() << std::dec << x ) ).str()

class DefendScript {

public:

	DefendScript(std::set<BWAPI::Unit*>, BWAPI::Position);
	DefendScript::DefendScript();
	void update();
	bool finished();

private:
	std::set<BWAPI::Unit*> unitsForCombat;
	std::set<BWAPI::Unit*> nearbyEnemyUnits;
	std::map<int, int> attackAssignments;

	BWAPI::Unit* findClosestEnemyUnit(BWAPI::Unit* myUnit, std::set<BWAPI::Unit*>);
	BWAPI::Unit* findClosestEnemyUnitNOK(BWAPI::Unit*, std::set<BWAPI::Unit*>, std::map<int,int>);

	BWAPI::Position destination;

};