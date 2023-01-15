#include <stdlib.h>
#include "../include/ConnectionHandler.h"
#include "../include/StompProtocol.h"
#include <thread>
#include "../include/KeyBoardThread.h"
using namespace std;
int main(int argc, char *argv[]) {
    ConnectionHandler *handler = new ConnectionHandler();
    StompProtocol *protocol = new StompProtocol(*handler);
     KeyBoardThread function(*handler, *protocol);
    thread keyboardThread(&KeyBoardThread::KeyBoardRun, &function);
    while(1){
        if(handler->isConnected()) {
            string ans;
            if (!handler->getFrameAscii(ans, '\0')) {
                std::cout << "Disconnected. Exiting...\n" << std::endl;
                break;
            }
               protocol->ServerProcess(ans);
        }
    }
    keyboardThread.join();
   if (handler) delete (handler);
   if(protocol) delete (protocol);
   return 0;
}