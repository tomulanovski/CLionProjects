#include "../include/Game.h"
Game::Game(string GameName): GameName(GameName) , General_UpdatesMap() , teamAStats() , teamBStats() ,
events("Game event reports:\n"){}

void Game::UpdateGame(unordered_map<string, string> general , unordered_map<string,string> teamA , unordered_map<string,string> teamB , string event ) {
for (auto stat: general) {
    General_UpdatesMap[stat.first] = stat.second;
}
for (auto stat: teamA) {
    teamAStats[stat.first] = stat.second;
}
for (auto stat:teamB) {
    teamBStats[stat.first] = stat.second;
}
events = events + event;
}
string Game::PrintSummary() {
    int index = GameName.find('_');
    string toPrint = GameName.substr(0,index) + " vs "+ GameName.substr(index+1) + "\nGame stats:\n";
    for (auto i:General_UpdatesMap) {
        toPrint = toPrint + i.first + ":" + i.second + '\n';
    }
    toPrint = toPrint + GameName.substr(0,index) + " stats:\n";
    for (auto i:teamAStats) {
        toPrint = toPrint + i.first + ":" + i.second + '\n';
    }
    toPrint = toPrint + GameName.substr(index+1)+ " stats:\n";
    for (auto i:teamBStats) {
        toPrint = toPrint + i.first + ":" + i.second + '\n';
    }
    toPrint = toPrint + events;
    return toPrint;
}