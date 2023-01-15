#include "../include/KeyBoardThread.h"
#include "../include/StompProtocol.h"
using namespace std;
KeyBoardThread::KeyBoardThread(ConnectionHandler & handler, StompProtocol & protocol) :
mhandler(&handler) , mprotocol(&protocol) {}
KeyBoardThread::KeyBoardThread(const KeyBoardThread & functions) : mhandler(functions.mhandler) , mprotocol(functions.mprotocol) {}
KeyBoardThread &KeyBoardThread::operator=(const KeyBoardThread& functions) {
    mhandler = functions.mhandler;
    *mprotocol = *functions.mprotocol;
    return *this;
}
KeyBoardThread::~KeyBoardThread() {
    if (mhandler) delete mhandler;
    if(mprotocol) delete mprotocol;
}

void KeyBoardThread::KeyBoardRun() {
    while(1) {
        const short bufsize = 1024;
        char buf[bufsize];
        cin.getline(buf, bufsize);
        string msg(buf);
        string out = mprotocol->keyboardProcess(msg);
        if(out!="") {
            mhandler->sendFrameAscii(out, '\0');
        }
    }
}
