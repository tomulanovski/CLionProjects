#pragma once
#include <string>
#include <vector>
#include "../include/ConnectionHandler.h"
#include <sstream>
#include <unordered_map>
#include "event.h"
#include "../include/Game.h"
#include <fstream>
using namespace std;
class ConnectionHandler;

// TODO: implement the STOMP protocol
class StompProtocol
{
private:
    string username;
    ConnectionHandler* mhandler;
    int receiptIdCount;
    int subId;
    int disconnectReceipt;
    unordered_map<string,int> topicSubIdmap;
    unordered_map<string,pair<string,int>> ReceiptToSubIdMap;
    unordered_map<string,unordered_map<string,Game>> Games_UsersMap;
    string Login(vector<string>);
    string join(vector<string>);
    string exit(vector<string>);
    string report(vector<string>);
    void summary(vector<string>);
    string logout(vector<string>);
    void addSubscription(int , string );
    void removeSubscription(string);
    int getSubId(string);
    void receipt(string);
    void message(string , string);
    void error(string);
    unordered_map<string,string> stringToMap(string);

public:
    StompProtocol(ConnectionHandler& handler);
    ~StompProtocol();
    StompProtocol& operator=(const StompProtocol&);
    StompProtocol(const StompProtocol&);
    string keyboardProcess(string message);
    void ServerProcess(string message);
};
