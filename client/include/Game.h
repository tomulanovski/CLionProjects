#pragma once
#include <string>
#include <unordered_map>
#include "event.h"
#include <vector>
using namespace std;
class Game {
private:
    string GameName;
    unordered_map<string,string> General_UpdatesMap;
    unordered_map<string,string> teamAStats;
    unordered_map<string,string> teamBStats;
    string events;
public:
    Game (string GameName);
    void UpdateGame(unordered_map<string,string>, unordered_map<string,string> ,unordered_map<string,string> , string);
    string PrintSummary();

};