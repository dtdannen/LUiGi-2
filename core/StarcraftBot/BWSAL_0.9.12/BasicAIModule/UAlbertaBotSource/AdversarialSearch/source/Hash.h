#pragma once

#include "ASCommon.h"
#include <ctime>
#include <boost/random/mersenne_twister.hpp>
#include <boost/random/uniform_int_distribution.hpp>
#include <boost/array.hpp>

namespace MicroSearch
{
namespace Hash
{
	typedef std::vector<HashType> HashVec;

	class HashValues
	{
		HashType	unitPositionHash[Search::Constants::Num_Players];
		HashType	timeCanAttackHash[Search::Constants::Num_Players];
		HashType	timeCanMoveHash[Search::Constants::Num_Players];
		HashType	unitTypeHash[Search::Constants::Num_Players];
		HashType	currentHPHash[Search::Constants::Num_Players];

	public:

		HashValues(int seed = 0);
		
		const HashType getAttackHash		(const size_t & player, const size_t & value) const;
		const HashType getMoveHash			(const size_t & player, const size_t & value) const;
		const HashType getUnitTypeHash		(const size_t & player, const size_t & value) const;
		const HashType getCurrentHPHash		(const size_t & player, const size_t & value) const;
		const HashType positionHash			(const IDType & player, const PositionType & x, const PositionType & y) const;
	};

	// some data storage
	extern HashType			unitIndexHash[Search::Constants::Num_Players][Search::Constants::Max_Units];
	extern HashValues		values[Search::Constants::Num_Hashes];
	
	// good hashing functions
	void			initHash();
	int				hash32shift(int key);
	const size_t	jenkinsHash( size_t a);
	const size_t	magicHash(const HashType & hash, const size_t & player, const size_t & index);

	const int jenkinsHashCombine(const HashType & hash, const int val);
};
}