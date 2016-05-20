#include <RectangleArray.h>
#include <map>
void activateLogger();
void closeLogger();
void log(const char* text, ...);
std::map<int, int> computeAssignments(Util::RectangleArray< double> &cost);
std::string currentDateTime();