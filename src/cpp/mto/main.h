#include <boost/asio/ip/tcp.hpp>

struct Header;
struct MtoHello;
class PeerConnectionHandler;

PeerConnectionHandler * getPeer(unsigned short port);
boost::asio::ip::tcp::socket * getPeerSocket(unsigned short port);
void startConnectingToPeer(boost::asio::ip::tcp::endpoint where);
void helloReceived(Header h, PeerConnectionHandler * receiver);
void parseHello(const MtoHello & hello, PeerConnectionHandler * handler);
void peerDied(PeerConnectionHandler *, bool reconnect);