// Author: Dustin Dannenhauer

// Note: Much of this code was borrowed from:
// http://www.codeproject.com/Articles/412511/Simple-client-server-network-using-Cplusplus-and-W

//#include "stdafx.h"
#pragma once
#include "PlannerClient.hpp"
#include <string>
#include "../Addons/Util.h"
#include "BWAPI.h"
#include <process.h>
//#include "PlanExecutor.h"

using namespace BWAPI;
#include <winsock2.h>
#include <windows.h>

#pragma comment(lib,"ws2_32.lib")

#define IDC_EDIT_IN		101
#define IDC_EDIT_OUT		102
#define IDC_MAIN_BUTTON		103
#define WM_SOCKET		104

char *szServer="localhost";
int nPort = 64321;

HWND hEditIn=NULL;
HWND hEditOut=NULL;
SOCKET Socket=NULL;
char szHistory[10000];

void setup();
void Thread(void* pParams);

LRESULT CALLBACK WinProc(HWND hWnd,UINT message,WPARAM wParam,LPARAM lParam);

int PlannerClient::numberOfServerRequests = 0;
std::string PlannerClient::currPlanStepStr = "";
PlanExecutor* PlannerClient::planExecutor = NULL;

PlannerClient::PlannerClient() {
	numberOfServerRequests = 0;
	
}

// note, setPlanExecutor() should be called before invoking this
void PlannerClient::start() {
	setup();
	_beginthread( Thread, 0, NULL);
}

void PlannerClient::setPlanExecutor(PlanExecutor* planExecutor) {
	PlannerClient::planExecutor = planExecutor;
}


void setup() {
	// Set up Winsock
	WSADATA WsaDat;
	int nResult=WSAStartup(MAKEWORD(2,2),&WsaDat);
	if(nResult!=0)
	{
		MessageBoxA(NULL,
			"Winsock initialization failed",
			"CriticalError",
			MB_ICONERROR);


	}

	Socket=socket(AF_INET,SOCK_STREAM,IPPROTO_TCP);
	if(Socket==INVALID_SOCKET)
	{
		MessageBoxA(NULL,
			"Socket creation failed",
			"Critical Error",
			MB_ICONERROR);


	}

	nResult=WSAAsyncSelect(Socket,NULL,WM_SOCKET,(FD_CLOSE|FD_READ));
	if(nResult != -1 && nResult)
	{
		MessageBoxA(NULL,
			std::string(SSTR(nResult)).append(" code that caused critical error").c_str(),
			"WSAAsyncSelect failed",
			MB_ICONERROR);

	}

	// Resolve IP address for hostname
	struct hostent *host;
	if((host=gethostbyname(szServer))==NULL)
	{
		MessageBoxA(NULL,
			"Unable to resolve host name",
			"Critical Error",
			MB_ICONERROR);

	}

	// Set up our socket address structure
	SOCKADDR_IN SockAddr;
	SockAddr.sin_port=htons(nPort);
	SockAddr.sin_family=AF_INET;
	SockAddr.sin_addr.s_addr=*((unsigned long*)host->h_addr);

	connect(Socket,(LPSOCKADDR)(&SockAddr),sizeof(SockAddr));
}

void Thread(void* pParams) {
	while (1) {
		char szIncoming[1024];
		ZeroMemory(szIncoming,sizeof(szIncoming));

		int inDataLength=recv(Socket,
			(char*)szIncoming,
			sizeof(szIncoming)/sizeof(szIncoming[0]),
			0);

		PlannerClient::setCurrentPlanStepStr(std::string(szIncoming));

		//strncat(szHistory,szIncoming,inDataLength);
		//strcat(szHistory,"\r\n");
	}
}


int WINAPI PlannerClient::CreateWinMain(HINSTANCE hInst,HINSTANCE hPrevInst,LPSTR lpCmdLine,int nShowCmd)
{
	WNDCLASSEX wClass;
	ZeroMemory(&wClass,sizeof(WNDCLASSEX));
	wClass.cbClsExtra=NULL;
	wClass.cbSize=sizeof(WNDCLASSEX);
	wClass.cbWndExtra=NULL;
	wClass.hbrBackground=(HBRUSH)COLOR_WINDOW;
	wClass.hCursor=LoadCursor(NULL,IDC_ARROW);
	wClass.hIcon=NULL;
	wClass.hIconSm=NULL;
	wClass.hInstance=hInst;
	wClass.lpfnWndProc=WNDPROC(WinProc);
	wClass.lpszClassName=LPCWSTR("Window Class");
	wClass.lpszMenuName=NULL;
	wClass.style=CS_HREDRAW|CS_VREDRAW;

	if(!RegisterClassEx(&wClass))
	{
		int nResult=GetLastError();
		MessageBoxA(NULL,
			"Window class creation failed\r\nError code:",
			"Window Class Failed",
			MB_ICONERROR);
	}

	HWND hWnd=CreateWindowEx(NULL,
		LPCWSTR("Window Class"),
		LPCWSTR("Windows Async Client"),
		WS_OVERLAPPEDWINDOW,
		200,
		200,
		640,
		480,
		NULL,
		NULL,
		hInst,
		NULL);

	if(!hWnd)
	{
		int nResult=GetLastError();

		MessageBox(NULL,
			LPCWSTR("Window creation failed\r\nError code:"),
			LPCWSTR("Window Creation Failed"),
			MB_ICONERROR);
	}

	ShowWindow(hWnd,nShowCmd);

	MSG msg;
	ZeroMemory(&msg,sizeof(MSG));

	while(GetMessage(&msg,NULL,0,0))
	{
		TranslateMessage(&msg);
		DispatchMessage(&msg);
	}

	return 0;
}

LRESULT CALLBACK WinProc(HWND hWnd,UINT msg,WPARAM wParam,LPARAM lParam)
{
	switch(msg)
	{
	case WM_CREATE:
		{
			ZeroMemory(szHistory,sizeof(szHistory));

			// Create incoming message box
			hEditIn=CreateWindowEx(WS_EX_CLIENTEDGE,
				LPCWSTR("EDIT"),
				LPCWSTR(""),
				WS_CHILD|WS_VISIBLE|ES_MULTILINE|
				ES_AUTOVSCROLL|ES_AUTOHSCROLL,
				50,
				120,
				400,
				200,
				hWnd,
				(HMENU)IDC_EDIT_IN,
				GetModuleHandle(NULL),
				NULL);
			if(!hEditIn)
			{
				MessageBox(hWnd,
					LPCWSTR("Could not create incoming edit box."),
					LPCWSTR("Error"),
					MB_OK|MB_ICONERROR);
			}
			HGDIOBJ hfDefault=GetStockObject(DEFAULT_GUI_FONT);
			SendMessage(hEditIn,
				WM_SETFONT,
				(WPARAM)hfDefault,
				MAKELPARAM(FALSE,0));
			SendMessage(hEditIn,
				WM_SETTEXT,
				NULL,
				(LPARAM)"Attempting to connect to server...");

			// Create outgoing message box
			hEditOut=CreateWindowEx(WS_EX_CLIENTEDGE,
				LPCWSTR("EDIT"),
				LPCWSTR(""),
				WS_CHILD|WS_VISIBLE|ES_MULTILINE|
				ES_AUTOVSCROLL|ES_AUTOHSCROLL,
				50,
				50,
				400,
				60,
				hWnd,
				(HMENU)IDC_EDIT_IN,
				GetModuleHandle(NULL),
				NULL);
			if(!hEditOut)
			{
				MessageBox(hWnd,
					LPCWSTR("Could not create outgoing edit box."),
					LPCWSTR("Error"),
					MB_OK|MB_ICONERROR);
			}

			SendMessage(hEditOut,
				WM_SETFONT,(WPARAM)hfDefault,
				MAKELPARAM(FALSE,0));
			SendMessage(hEditOut,
				WM_SETTEXT,
				NULL,
				(LPARAM)"Type message here...");

			// Create a push button
			HWND hWndButton=CreateWindow( 
				LPCWSTR("BUTTON"),
				LPCWSTR("Send"),
				WS_TABSTOP|WS_VISIBLE|
				WS_CHILD|BS_DEFPUSHBUTTON,
				50,	
				330,
				75,
				23,
				hWnd,
				(HMENU)IDC_MAIN_BUTTON,
				GetModuleHandle(NULL),
				NULL);

			SendMessage(hWndButton,
				WM_SETFONT,
				(WPARAM)hfDefault,
				MAKELPARAM(FALSE,0));


		}
		break;

	case WM_COMMAND:
		switch(LOWORD(wParam))
		{
		case IDC_MAIN_BUTTON:
			{
				PlannerClient::sendMessageToGDAServer("");
			}
			break;
		}
		break;

	case WM_DESTROY:
		{
			PostQuitMessage(0);
			shutdown(Socket,SD_BOTH);
			closesocket(Socket);
			WSACleanup();
			return 0;
		}
		break;

	case WM_SOCKET:
		{
			if(WSAGETSELECTERROR(lParam))
			{	
				MessageBox(hWnd,
					LPCWSTR("Connection to server failed"),
					LPCWSTR("Error"),
					MB_OK|MB_ICONERROR);
				SendMessage(hWnd,WM_DESTROY,NULL,NULL);
				break;
			}
			switch(WSAGETSELECTEVENT(lParam))
			{
			case FD_READ:
				{
					char szIncoming[1024];
					ZeroMemory(szIncoming,sizeof(szIncoming));

					int inDataLength=recv(Socket,
						(char*)szIncoming,
						sizeof(szIncoming)/sizeof(szIncoming[0]),
						0);

					PlannerClient::setCurrentPlanStepStr(std::string(szIncoming));

					strncat(szHistory,szIncoming,inDataLength);
					strcat(szHistory,"\r\n");

					SendMessage(hEditIn,
						WM_SETTEXT,
						sizeof(szIncoming)-1,
						reinterpret_cast<LPARAM>(&szHistory));
				}
				break;

			case FD_CLOSE:
				{
					MessageBox(hWnd,
						LPCWSTR("Server closed connection"),
						LPCWSTR("Connection closed!"),
						MB_ICONINFORMATION|MB_OK);
					closesocket(Socket);
					SendMessage(hWnd,WM_DESTROY,NULL,NULL);
				}
				break;
			}
		} 
	}

	return DefWindowProc(hWnd,msg,wParam,lParam);
}

std::string PlannerClient::getCurrentPlanStepStr() {
	return currPlanStepStr;
}

// this method is gives the raw message from the GDA server to this bot
void PlannerClient::setCurrentPlanStepStr(std::string rawPlanStepStr) {
		std::string expectedMsgHeader = "[NEXT PLAN STEP]"; 
	if (rawPlanStepStr.find_first_of(expectedMsgHeader) == std::string::npos) {
		// ERROR - did not receive next plan step
		/*MessageBoxA(NULL,
			rawPlanStepStr.c_str(),
			"Bad Message from server",
			MB_ICONINFORMATION|MB_OK);*/
		//Broodwar->sendText("ERROR: bad message from server, expected next plan step\n");
	}
	std::string planStepStrNoHeader = rawPlanStepStr.substr(expectedMsgHeader.length(), rawPlanStepStr.length());
	
	if (strcmp(planStepStrNoHeader.c_str(),PlannerClient::currPlanStepStr.c_str()) != 0) {
		PlannerClient::currPlanStepStr = planStepStrNoHeader;
		/*MessageBoxA(NULL,
			PlannerClient::currPlanStepStr.c_str(),
			"New Plan Step from GDA Server",
			MB_ICONINFORMATION|MB_OK);*/

		if (PlannerClient::planExecutor != NULL) {

			PlannerClient::planExecutor->createNewPlanStep(PlannerClient::currPlanStepStr);
		}
	}
}

void PlannerClient::sendMessageToGDAServer(std::string msg) {

	char szBuffer[1024];

	int test=sizeof(szBuffer);
	ZeroMemory(szBuffer,sizeof(szBuffer));

	msg.append("\n");

	strcpy(szBuffer, msg.c_str());
	send(Socket,szBuffer,strlen(szBuffer),0);
}


void PlannerClient::requestNextPlanStep(PlanStep* currPlanStep) {
	//	log("\nRequesting next plan step from GDA Java");
	
	std::string requestMsg = + " msg"+SSTR(numberOfServerRequests) + " Bot requesting next step of (new) plan";

	if (currPlanStep != NULL) {
		//requestMsg = "PLAN-ID" + SSTR(currPlanStep->getPlanID()) + " msg"+SSTR(numberOfServerRequests) + " Bot requesting next step of plan";
		requestMsg = "PLAN-ID " + SSTR(currPlanStep->getPlanID()) + " msg" + SSTR(numberOfServerRequests)+" Bot requesting next step of plan, UNITS ";
		std::set<BWAPI::Unit*> currPlanStepUnits = currPlanStep->getUnits();

		if (!currPlanStepUnits.empty()) {
			std::string unitsStr = "";
			BOOST_FOREACH(BWAPI::Unit* u, currPlanStepUnits) {
				unitsStr += SSTR(u->getID()) + " ";
			}
			requestMsg += unitsStr;
		}
	}

	numberOfServerRequests++;

	/*MessageBoxA(NULL,
			requestMsg.c_str(),
			"Sent request for new plan step from server",
			MB_ICONINFORMATION|MB_OK);*/

	sendMessageToGDAServer(requestMsg);
}

void PlannerClient::close() {
	PostQuitMessage(0);
	shutdown(Socket,SD_BOTH);
	closesocket(Socket);
	WSACleanup();
	//return 0;
}

//
//PlannerClient::PlannerClient() {
//	// create WSADATA object
//	WSADATA wsaData;
//
//	// socket
//	ConnectSocket = INVALID_SOCKET;
//
//	// holds address info for socket to connect to
//	struct addrinfo *result = NULL,
//		*ptr = NULL,
//		hints;
//
//	// Initialize Winsock
//	iResult = WSAStartup(MAKEWORD(2,2), &wsaData);
//
//	if (iResult != 0) {
//		printf("WSAStartup failed with error: %d\n", iResult);
//		exit(1);
//	}
//
//
//	// set address info
//	ZeroMemory( &hints, sizeof(hints) );
//	hints.ai_family = AF_UNSPEC;
//	hints.ai_socktype = SOCK_STREAM;
//	hints.ai_protocol = IPPROTO_TCP;  //TCP connection!!!
//
//	//resolve server address and port 
//	iResult = getaddrinfo("127.0.0.1", DEFAULT_PORT, &hints, &result);
//
//	if( iResult != 0 ) 
//	{
//		printf("getaddrinfo failed with error: %d\n", iResult);
//		WSACleanup();
//		exit(1);
//	}
//
//	// Attempt to connect to an address until one succeeds
//	for(ptr=result; ptr != NULL ;ptr=ptr->ai_next) {
//
//		// Create a SOCKET for connecting to server
//		ConnectSocket = socket(ptr->ai_family, ptr->ai_socktype, 
//			ptr->ai_protocol);
//
//		if (ConnectSocket == INVALID_SOCKET) {
//			printf("socket failed with error: %ld\n", WSAGetLastError());
//			WSACleanup();
//			exit(1);
//		}
//
//		// Connect to server.
//		iResult = connect( ConnectSocket, ptr->ai_addr, (int)ptr->ai_addrlen);
//
//		if (iResult == SOCKET_ERROR)
//		{
//			closesocket(ConnectSocket);
//			ConnectSocket = INVALID_SOCKET;
//			printf ("The server is down... did not connect");
//		}
//	}
//
//
//	// no longer need address info for server
//	freeaddrinfo(result);
//
//	// if connection failed
//	if (ConnectSocket == INVALID_SOCKET) 
//	{
//		printf("Unable to connect to server!\n");
//		WSACleanup();
//		exit(1);
//	}
//
//	// Set the mode of the socket to be nonblocking
//	//u_long iMode = 1;
//
//	// Dustin - here I want a blocking socket, so this is set to 0 instead
//	u_long iMode = 0;
//
//	iResult = ioctlsocket(ConnectSocket, FIONBIO, &iMode);
//	if (iResult == SOCKET_ERROR)
//	{
//		log("ioctlsocket failed with error: %d\n", WSAGetLastError());
//		closesocket(ConnectSocket);
//		WSACleanup();
//		exit(1);        
//	}
//
//	//disable nagle
//	//char value = 1;
//	//setsockopt( ConnectSocket, IPPROTO_TCP, TCP_NODELAY, &value, sizeof( value ) );
//}
//
//void PlannerClient::close() {
//	do_close();
//}
//
//std::string PlannerClient::requestNextPlanStep(PlanStep* currPlanStep) {
//	log("\nRequesting next plan step from GDA Java");
//
//	std::string requestMsg = "Bot requesting next step of plan \n";
//	if (currPlanStep != NULL) {
//		std::set<BWAPI::Unit*> currPlanStepUnits = currPlanStep->getUnits();
//		
//		if (!currPlanStepUnits.empty()) {
//			std::string unitsStr = "";
//			BOOST_FOREACH(BWAPI::Unit* u, currPlanStepUnits) {
//				unitsStr += SSTR(u->getID()) + " ";
//			}
//			requestMsg = "Bot requesting next step of plan, UNITS "+unitsStr+" \n";
//		}
//	}
//
//
//	sendMessage(ConnectSocket, (char*)requestMsg.c_str(), requestMsg.size());
//	log("\nMessage sent to GDA Java: %s", requestMsg.c_str());
//	// now call receiveMessage to recieve a message from the server
//	char serverMessage[DEFAULT_BUFLEN];
//	//MessageBoxA(NULL,"After calling sendMessage","Test",0);
//	receiveMessage(ConnectSocket, serverMessage, DEFAULT_BUFLEN);
//	//MessageBoxA(NULL,"After calling recieveMessage","Test",0);
//	//while (receiveMessage(ConnectSocket, serverMessage, DEFAULT_BUFLEN) != 0) {
//	//	log("\nWaiting for message from server, last error was %d", WSAGetLastError());
//	//	Sleep(500);
//	//}
//
//	log("\n[FROM SERVER] %s", serverMessage);
//
//	std::string receivedMsg = std::string(serverMessage);
//	std::string expectedMsgHeader = "[NEXT PLAN STEP]"; 
//	if (receivedMsg.find_first_of(expectedMsgHeader) == std::string::npos) {
//		// ERROR - did not receive next plan step
//		Broodwar->sendText("ERROR: bad message from server, expected next plan step\n");
//	}
//	std::string planStepStr = receivedMsg.substr(expectedMsgHeader.length(), receivedMsg.length());
//	/*
//	int recvResult;
//	if ((recvResult = receiveMessage(ConnectSocket, serverMessage, DEFAULT_BUFLEN)) == 0) {
//	log("\n[SERVER] ");
//	for (int i = 0; i < 20; i++) {
//	log("%c",serverMessage[i]);
//	}
//	}else{
//	log("\n[SERVER] recieve message failed: %d", WSAGetLastError());
//	}
//	*/
//	//log("\n[SERVER] %s",serverMessage);
//	return planStepStr;
//}
//
//void PlannerClient::do_close() {
//	std::string gameOverMsg = "Game Over\n";
//	sendMessage(ConnectSocket, (char*)gameOverMsg.c_str(), gameOverMsg.size());
//	closesocket(ConnectSocket);
//	WSACleanup();
//}
//
//void PlannerClient::do_write() {
//
//}
//
//int PlannerClient::sendMessage(SOCKET curSocket, char * message, int messageSize)
//{
//	return send(curSocket, message, messageSize, 0);
//}
//
//int PlannerClient::receiveMessage(SOCKET curSocket, char * buffer, int bufSize)
//{
//	return recv(curSocket, buffer, bufSize, 0);
//}
//
