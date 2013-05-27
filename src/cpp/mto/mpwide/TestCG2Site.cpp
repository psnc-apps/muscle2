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

// Maximum size of a C++ string on galactica was 1073741820 bytes.
// char sequence can be obtained by calling member function .c_str()

int main(int argc, char** argv){

  string a;

  cout << "Maximum size = " << a.max_size() << endl;

  /* Initialize */

  int flag = atoi(argv[2]);

  string host = (string) argv[1];

  /* Define 8 different streams */
  int size = 64;

  if(argc>3) {

    int arg_size = atoi(argv[3]);
    if(arg_size < 125 && arg_size > 0) {
      size = arg_size;
    }
    else {
      cout << "INPUT ERROR: Too many or too few streams." << endl;
      exit(0);
    }
  }


  string hosts[size]; // = {host,host,host,host,host,host,host,host};
  int sports[size];    // = {6000,6001,6002,6003,6004,6005,6006,6007};
  int cports[size];
  int flags[size];    // = {flag,flag,flag,flag,flag,flag,flag,flag};

  for(int i=0; i<size; i++) {
    hosts[i] = host;
    sports[i] = 6000+i;
    cports[i] = 6000+size+i;
    flags[i] = flag;
  }

  MPW_Init(hosts,sports,size);

//  MPW_TinyTest();

  cerr << "\nSmall test completed, now commencing large test.\n" << endl;


  long long int len = 640*1024*1024; //testing_size/numstreams;
  long long int len2 = 200*1024*1024;
  long long int len3 = 5*1024*1024;

  char* msg  = (char*) malloc(len);
  char* msg2 = (char*) malloc(len);
  char* msg3 = (char*) malloc(len2);
  char* msg4 = (char*) malloc(len2);
  char* msg5 = (char*) malloc(len3);
  char* msg6 = (char*) malloc(len3);

  int channels[size];

  msg[10000000] = 'y';

  for(int i=0; i<size; i++) {
    channels[i] = i;
  }

  for(int i=0; i<2000; i++) {

    MPW_PSendRecvWrapper(msg3,len2,msg4,len2,channels,size);

sleep(1);
//    sleep(10);

    MPW_PSendRecvWrapper(msg,len,msg2,len,channels,size);

sleep(1);
//    sleep(100);



    MPW_PSendRecvWrapper(msg5,len3,msg6,len3,channels,size);
    MPW_PSendRecvWrapper(msg3,len2,msg4,len2,channels,size);

sleep(1);

//    sleep(10);
    cout << "End of iteration " << i << "." << endl;
  }
  free(msg);
  free(msg2);
  free(msg3);
  free(msg4);
  free(msg5);
  free(msg6);

  MPW_Finalize();


  return 1;
}
