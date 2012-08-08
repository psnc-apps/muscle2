#include <iostream>
#include <boost/asio.hpp>
#include <boost/asio/ip/tcp.hpp>
#include "messages.hpp"
using namespace std;
using namespace boost;
using namespace boost::asio;
using namespace boost::asio::ip;
using namespace boost::system;


void doListen(char ** argv){
 Request r;
 
 r.type=Request::Register;
 
 io_service io;
 
 tcp::socket s(io);
 tcp::endpoint e = *tcp::resolver(io).resolve(tcp::resolver::query(string(argv[1]), string(argv[2])));
 s.connect(e);
 
 e = *tcp::resolver(io).resolve(tcp::resolver::query(string(argv[3]), string(argv[4])));
 r.srcAddress = e.address().to_v4().to_ulong();
 r.srcPort = e.port();
 
 tcp::acceptor acc(io, e);
 
 char * buf = new char[Request::getSize()];
 r.serialize(buf);
 s.send(buffer(buf, Request::getSize()));
 delete buf;
 
 tcp::socket sock(io);
 acc.accept(sock);
 
 buf = new char[1024];
 while(1){
  size_t cnt = sock.read_some(buffer(buf, 1024));
  //cout << string(buf, cnt) << flush;
  //for(int i = 0 ; i < cnt; ++i)
  //  buf[i]=toupper(buf[i]);
  write(sock, buffer(buf, cnt), transfer_all());
 }
}

void doConnect(char ** argv)
{
 Request r;
 
 r.type=Request::Connect;
 
 io_service io;
 
 tcp::socket s(io);
 tcp::endpoint e1 = *tcp::resolver(io).resolve(tcp::resolver::query(string(argv[1]), string(argv[2])));
 tcp::endpoint e2 = *tcp::resolver(io).resolve(tcp::resolver::query(string(argv[3]), string(argv[4])));
 tcp::endpoint e3 = *tcp::resolver(io).resolve(tcp::resolver::query(string(argv[5]), string(argv[6])));
 s.connect(e1);
 
 r.srcAddress = e2.address().to_v4().to_ulong();
 r.srcPort = e2.port();
 r.dstAddress = e3.address().to_v4().to_ulong();
 r.dstPort = e3.port();
 
 char * buf = new char[Request::getSize()];
 r.serialize(buf);
 s.send(buffer(buf, Request::getSize()));
 delete buf;
 
 buf = new char(Header::getSize());
 read(s, buffer(buf, Header::getSize()), transfer_all());
 Header h = Header::deserialize(buf); 
 delete buf;
 
 if(h.length){
   cout << "Got negative response!" << endl ;
   return;
 }
 
 
 while(1){
  string x;
  getline(cin, x);
  write(s, buffer(x), transfer_all());
  buf = new char[x.size()]; 
  read(s, buffer(buf, x.size()), transfer_all());
  if (memcmp(x.c_str(), buf, x.size()) != 0) {
    cout << "data courruption detected. Exiting" << endl;
    exit(1);
  }
  //string y(buf, x.size());
  //cout << y << flush;
 }
}


int main(int argc, char ** argv){
  
  printf("usage: %s MTO_addr MTO_port ME_a ME_p [IT_a IT_p]\n", argv[0]);
  
  if ( argc == 5 )
    doListen(argv);
  else
    doConnect(argv);
  
  return 0;
}
