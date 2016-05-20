#include "BWAPI.h"
#include "BWTA.h"
#include <map>
#include <boost\foreach.hpp>
#include <boost\tokenizer.hpp>
#include <boost\lexical_cast.hpp>
#include <boost\format.hpp>

#define SSTR( x ) dynamic_cast< std::ostringstream & >( \
	( std::ostringstream() << std::dec << x ) ).str()

class AttackClosestScript {

public:

	AttackClosestScript(std::set<BWAPI::Unit*>, BWAPI::Position);
	AttackClosestScript::AttackClosestScript();
	void update();
	bool finished();
	void setUnits( std::set<BWAPI::Unit*> availableUnits );
	std::set<BWAPI::Unit*> getAvailableUnits();

private:
	std::set<BWAPI::Unit*> unitsForCombat;
	std::set<BWAPI::Unit*> nearbyEnemyUnits;
	std::map<int, int> attackAssignments;

	BWAPI::Unit* findClosestEnemyUnit(BWAPI::Unit* myUnit, std::set<BWAPI::Unit*>);
	BWAPI::Unit* findClosestEnemyUnitNOK(BWAPI::Unit*, std::set<BWAPI::Unit*>, std::map<int,int>);
	
	BWAPI::Position destination;

};