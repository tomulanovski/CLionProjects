#pragma once
#include "../include/ConnectionHandler.h"
#include "../include/StompProtocol.h"
class ConnectionHandler;
class KeyBoardThread{
private:
    ConnectionHandler* mhandler;
    StompProtocol* mprotocol;
public:
    KeyBoardThread(ConnectionHandler&, StompProtocol&);
    ~KeyBoardThread();
    KeyBoardThread& operator=(const KeyBoardThread&);
    KeyBoardThread(const KeyBoardThread&);
    void KeyBoardRun();
};