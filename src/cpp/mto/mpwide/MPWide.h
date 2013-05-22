/*
* Copyright 2010-2013 Multiscale Applications on European e-Infrastructures (MAPPER) project
*
* GNU Lesser General Public License
* 
* This file is part of MUSCLE (Multiscale Coupling Library and Environment).
* 
* MUSCLE is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* MUSCLE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public License
* along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/
#include <iostream>
#include <cassert>
using namespace std;

/* Enable/disable software-based packet pacing. */
#define PacingMode 1


//void MPW_Init_c (char** url, int* ports, int numsockets);
//void MPW_Init1_c (char* url, int port);


char* MPW_DNSResolve(char* host);

/* Print all connections. */
void MPW_Print();

/* Print the number of available streams. */
int MPW_NumChannels();

// Return path id or negative error value. 
int MPW_CreatePath(string host, int server_side_base_port, int num_streams); 
// Return 0 on success (negative on failure).
int MPW_DestroyPath(int path);

//int MPW_Send(char* sendbuf, int sendsize, int path);
//int MPW_Recv(char* recvbuf, int recvsize, int path);
void MPW_SendRecv(char* sendbuf, long long int sendsize, char* recvbuf, long long int recvsize, int path);
// returns the size of the newly received data. 
int MPW_DSendRecv(char* sendbuf, long long int sendsize, char* recvbuf, long long int maxrecvsize, int path);

/* Initialize the Cosmogrid library. */
void MPW_Init(string* url, int* server_side_ports, int* client_side_ports, int num_channels);
void MPW_Init(string* url, int* server_side_ports, int num_channels); //this call omits client-side port binding.
void MPW_Init(string url, int port);
void MPW_Init(string url);
void MPW_Init();

/* Set tcp window size. */
void setWin(int channel, int size);

/* Close channels. */
void MPW_CloseChannels(int* channels , int num_channels);
/* Reopen them. */
void MPW_ReOpenChannels(int* channels, int num_channels);

/* Close all sockets and free data structures related to the library. */
int MPW_Finalize();

/* Exchanges buffers between the two machines. */
//string MPW_SendRecv(string buf);
//void MPW_SendRecv(char* sendbuf, long long int sendsize, char* recvbuf, long long int recvsize, int channel);

/* Perform this using multiple channels. */
void MPW_PSendRecv(char** sendbuf, long long int* sendsize, char** recvbuf, long long int* recvsize, int* channel, int num_channels);
void MPW_SendRecv ( char* sendbuf, long long int  sendsize, char*  recvbuf, long long int  recvsize, int* channel, int num_channels);

/* Dynamically-sized message exchanges. */
long long int MPW_DSendRecv(char* sendbuf, long long int sendsize, char* recvbuf, long long int maxrecvsize, int* channel, int num_channels);

/* Message relaying/forwarding for communication nodes. */
void MPW_Relay(int* channels, int* channels2, int num_channels);
void MPW_Relay1(int* channels, int* channels2, int num_channels);

/* Test the socket library. */
void MPW_Test(int num_channels);
void MPW_TinyTest(int num_channels, int flag);

/* Send data, receive nothing. */
void MPW_Send(char* buf, long long int size, int channel);

/* Returns the length of the received data. */
int MPW_Recv(char* buf, long long int size, int channel);

/* Recv from one set of channels. Send out through the other set. */
long long int MPW_DCycle(char* sendbuf, long long int sendsize, char* recvbuf, long long int recvsize,
             int* ch_send, int num_ch_send, int* ch_recv, int num_ch_recv);
void MPW_Cycle(char* sendbuf, long long int sendsize, char* recvbuf, long long int recvsize,
             int* ch_send, int num_ch_send, int* ch_recv, int num_ch_recv);

/* Simple buffer splitting function. Handy for PSendRecv calls. */
void MPW_splitBuf(char* buf, long long int bsize, int num_chunks, char** split_buf, long long int* chunk_sizes);

/* Synchronyze two processes on one stream. 
 * TODO: Implement collective barrier which operates on all streams simultaneously. */
void MPW_Barrier(int channel);

/* Adjust the global feeding pace. */
void MPW_setFeedingPace(int sending, int receiving);

#if PacingMode == 1
/* Get and set rates for pacing data. */
double MPW_getPacingRate();
void   MPW_setPacingRate(double rate);
#endif

extern "C" {
  void MPW_Init_c (char** url, int* ports, int numsockets); 
  void MPW_Init1_c (char* url, int port);
  void MPW_SendRecv1_c (char* sendbuf, long long int sendsize, char* recvbuf, long long int recvsize, int base_channel);
  void MPW_SendRecv_c (char* sendbuf, long long int sendsize, char* recvbuf, long long int recvsize, int* base_channel, int num_channels);
  void MPW_PSendRecv_c(char** sendbuf, long long int* sendsize, char** recvbuf, long long int* recvsize, int* channel, int num_channels);
}

