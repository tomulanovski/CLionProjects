cmake_minimum_required(VERSION 3.23)
project(client)

set(CMAKE_CXX_STANDARD 11)
#
#add_executable(client main.cpp)
add_executable(client src/StompClient.cpp src/event.cpp src/echoClient.cpp src/ConnectionHandler.cpp src/StompProtocol.cpp include/Game.h src/Game.cpp include/KeyBoardThread.h src/KeyBoardThread.cpp)
