// Author: Dustin Dannenhauer
// This class represents the client that will be connected to the planner via sockets. This class
// is responsible for listening for new messages from the planner and sending messages to the
// planner

#ifndef PLANNER_CLIENT
#define PLANNER_CLIENT

#pragma once
#include <winsock2.h>
#include <Windows.h>
//#include <ws2tcpip.h>
//#include <stdio.h> 
#include <string>
//#include "boost/thread.hpp"
//#include "boost/asio.hpp"
//include "boost/bind.hpp"
//#include <iostream>
//#include <deque>
//#include <cstdlib>

#include "PlanStep.h"
#include "PlanExecutor.h"
//#include "BotAIModule.h"

// size of our buffer
#define DEFAULT_BUFLEN 512
// port to connect sockets through 
#define DEFAULT_PORT "64321"
// Need to link with Ws2_32.lib, Mswsock.lib, and Advapi32.lib
#pragma comment (lib, "Ws2_32.lib")
#pragma comment (lib, "Mswsock.lib")
#pragma comment (lib, "AdvApi32.lib")

//#include <cstdlib>
//#include <deque>
//#include <iostream>
//#include <boost/bind.hpp>
//#include <boost/asio.hpp>
//#include <boost/thread.hpp>
//#include <boost/asio/ip/tcp.hpp>

//using boost::asio::ip::tcp;
//using boost::asio::ip::tcp;

class PlanExecutor;

class PlannerClient {

public:
	// for error checking function calls in Winsock library
    int iResult;

	

    // socket for client to connect to server
    SOCKET ConnectSocket;
	//PlannerClient();
	PlannerClient();
	void connectToGDAServer();
	void disconnectFromGDAServer();
	static void requestNextPlanStep(PlanStep*);
	static std::string getCurrentPlanStepStr();
	static void setCurrentPlanStepStr(std::string);
	void close();
	//PlannerClient(boost::asio::io_service& io_service, tcp::resolver::iterator endpoint_iterator);
    static void sendMessageToGDAServer(std::string);
	static void setPlanExecutor(PlanExecutor*);
	static void start();
	static int numberOfServerRequests;

private:
	int static const port = 64321; 
	
	void do_close();
	void do_write();
	static int sendMessage(SOCKET curSocket, char * message, int messageSize);
	static int receiveMessage(SOCKET curSocket, char * buffer, int bufSize);
	

	// borrowed window functions
	//LRESULT CALLBACK WinProc(HWND hWnd,UINT msg,WPARAM wParam,LPARAM lParam);
	int WINAPI CreateWinMain(HINSTANCE hInst,HINSTANCE hPrevInst,LPSTR lpCmdLine,int nShowCmd);
	static std::string currPlanStepStr;
	static PlanExecutor* planExecutor;
};

#endif /*PLANNER_CLIENT*/