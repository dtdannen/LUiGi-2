#pragma once
#include <BWAPI.h>
#include <BWTA.h>
#include <sstream>
#include <iostream>
#include <fstream>
#include <boost/algorithm/string/replace.hpp>
#include <boost\foreach.hpp>
#include <boost\tokenizer.hpp>
#include <boost\lexical_cast.hpp>
#include <boost\format.hpp>

//#include <ctime>
//#define _WINSOCKAPI_
//#include <Windows.h>

// macro for converting ints to strings
#define SSTR( x ) dynamic_cast< std::ostringstream & >( \
	( std::ostringstream() << std::dec << x ) ).str()

class GamestateDumper {

public:
	void setup(bool); // to be called during the onStart() method of a BWAPI module
	// takes a boolean that represents if BWTA is finished analyzing
	void dumpGameState(); // to be called whenever the agent would like to dump game state

	void finish();

	std::map<BWTA::Region*,int> getRegionIDs();
	

private:
	// filename where JSON formatted game state data will be written
	std::string dataFilenamePrefix;
	std::string logFilename;
	std::string errorLogFilename;
	std::ofstream errorLogOutputFile;

	// maps every region to an id using the BWAPI::Position of the center of the region as its
	// unique identifier
	std::map<BWTA::Region*, int> regionIDs;

	std::map<const BWTA::Chokepoint*, int> chokePointIDs;

	const std::string currentDateTime();

	// returns the value from the settings.properties configuration file
	std::string getPropValue(std::string key);
	int getClosestRegionId(BWAPI::Position pos);
};