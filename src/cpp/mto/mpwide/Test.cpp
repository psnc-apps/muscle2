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
#include <fstream>
#include <string.h>
#include <stdlib.h>
#include <math.h>
#include <iostream>

using namespace std;

#include "MPWide.h"

int main(int argc, char** argv){

  /* Initialize */

  string host = (string) argv[1];

  int size = 1;

  if(argc==1) {
    printf("usage: ./MPWTest <ip address of other endpoint> <channels> <buffer [kb]> <pacing [MB/s]> <tcpwin [bytes]> \n All parameters after the first are optional.");
  }

  if(argc>2) {
    size = atoi(argv[2]);
  }

  int bufsize = 8;
  if(argc>3) {
    bufsize = atoi(argv[3]);
  }

  if(argc>4) {
    MPW_setPacingRate((atoi(argv[4]))*1024*1024);
  }

  int winsize = 16*1024*1024;
  if(argc>5) {
    winsize = atoi(argv[5]);
  }

  string hosts[size];
  int sports[size];   

  for(int i=0; i<size; i++) {
    hosts[i] = host;
    sports[i] = 16256+i;
  }

  int path_id = MPW_CreatePath(host, 16256, size); ///path version
//  MPW_Init(hosts, sports, size); ///non-path version.

  cerr << "\nSmall test completed, now commencing large test.\n" << endl;


  long long int len = bufsize*1024; 
//  long long int len2 = 8*1024*1024;
//  long long int len3 = 1*1024*1024;

  char* msg  = (char*) malloc(len);
  char* msg2 = (char*) malloc(len);
//  char* msg3 = (char*) malloc(len2);
//  char* msg4 = (char*) malloc(len2);
//  char* msg5 = (char*) malloc(len3);
//  char* msg6 = (char*) malloc(len3);

  int channels[size];

//  msg[1000000] = 'y';

  for(int i=0; i<size; i++) {
    channels[i] = i;
    setWin(i,winsize);
  }

  /* test loop */
  for(int i=0; i<100; i++) {

//    MPW_SendRecv(msg,len,msg2,len,channels,size); ///non-path version.
    MPW_SendRecv(msg,len,msg2,len,path_id); ///path version

    sleep(1);
    cout << "End of iteration " << i << "." << endl;
  }

  free(msg);
  free(msg2);
//  free(msg3);
//  free(msg4);
//  free(msg5);
//  free(msg6);

  MPW_Finalize();


  return 1;
}
