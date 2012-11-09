// Definition of the Socket class

#ifndef Socket_class
#define Socket_class


#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <unistd.h>
#include <string>
#include <arpa/inet.h>


const int MAXHOSTNAME = 200;
const int MAXCONNECTIONS = 5;
const int WINSIZE = 1*1024*1024;

class Socket
{
 public:
  Socket();
  virtual ~Socket();

  // Server initialization
  bool create();
  bool bind ( const int port );
  bool listen() const;
  bool accept ( Socket& ) const;

  // Client initialization
  bool connect ( const std::string host, const int port );

  // Data Transimission
  bool send (const char* s, long long int size ) const;
  int recv (char* s, long long int size ) const;

  // Light-weight, non-blocking
  int isend (const char* s, long long int size ) const;
  int irecv (char* s, long long int size ) const;

  // Check if the socket is readable / writable. Timeout is 2 minutes.
  int select_me (int mask) const;
  int select_me (int mask, int timeout_val) const;

  void set_non_blocking ( const bool );

  void setWin(int size);

  bool is_valid() const { return m_sock != -1; }

  void close();

  void closeServer();

  int getSock() const { return m_sock; }

 private:

  int m_sock;
  int s_sock; //socket descriptor for server.
  sockaddr_in m_addr;
  #ifdef MSG_NOSIGNAL
    static const int tcp_send_flag = MSG_NOSIGNAL;
  #else //OSX Case
    static const int tcp_send_flag = SO_NOSIGPIPE;
  #endif

};


#endif
