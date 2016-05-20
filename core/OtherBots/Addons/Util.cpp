#include "Util.h"
#include <fstream>
#include <stdarg.h>
#include <sys/stat.h>
#include <iostream>
#include <string>
#include <stdio.h>
#include <time.h>
char buffer[1024];
bool logFileReadyToWrite = false;
FILE *outfile;
void activateLogger() {
	//if (fopen_s(&outfile, "bwapi-data\\logs\\hierarchical-gda-bot.log", "a+")==0) {
	//	// uncomment to turn logging on (alhough it will be delayed, won't be updated until game ends)
	//	//logFileReadyToWrite = true;
	//}
}

void closeLogger() {
	/*if (logFileReadyToWrite && outfile != NULL) {
		fclose(outfile);
	}*/
}

void log(const char* text, ...)
{
  /*const int BUFFER_SIZE = 1024;
  char buffer[BUFFER_SIZE];

  va_list ap;
  va_start(ap, text);
  vsnprintf_s(buffer, BUFFER_SIZE, BUFFER_SIZE, text, ap);
  va_end(ap);

  if (logFileReadyToWrite && outfile != NULL) {
	fprintf_s(outfile, buffer);
	fflush(outfile);
  }*/
}

std::string currentDateTime() {
	
	// code borrowed from:
	// http://social.msdn.microsoft.com/Forums/vstudio/en-US/d621b718-fda9-4b8b-8458-4e303ece1520/help-with-localtimes-and-strftime
	char str[70];
	time_t rawtime;
	struct tm timeinfo;
	time(&rawtime);
	localtime_s(&timeinfo,&rawtime);
    strftime(str, sizeof(str), "%Y-%m-%d %X", &timeinfo);
    return str;
	
}
