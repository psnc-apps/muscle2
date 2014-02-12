//
//  csocket.h
//  CMuscle
//
//  Created by Joris Borgdorff on 17-04-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#ifndef __CMuscle__csocket__
#define __CMuscle__csocket__

#include "msocket.h"
#include "mtime.h"

#include <string>
#include <unistd.h>
#include <sys/socket.h>

namespace muscle {
	namespace net {
		class CServerSocket;
		
		class csocket : virtual public msocket
		{
		public:
			virtual void setBlocking(bool);
			
			// Check if the socket is readable / writable. Timeout is MUSCLE_SOCKET_TIMEOUT seconds.
			// Override MUSCLE_SOCKET_TIMEOUT to choose a different number of seconds
			virtual int select(int mask) const;
			virtual int select(int mask, muscle::util::duration timeout) const;
			
			virtual bool operator < (const csocket & s1) const { return sockfd < s1.sockfd; }
			virtual bool operator == (const csocket & s1) const { return sockfd == s1.sockfd; }
		protected:
			csocket();
			csocket(int sockfd);
			
			virtual ~csocket() { ::shutdown(sockfd, SHUT_RDWR); ::close(sockfd); }
			
			void create();
            
			virtual void setOpts(const socket_opts& opts);
			
			void setWin(int size);
			
			static const muscle::util::duration SELECT_TIMEOUT;
		}; // end class socket
		
		class CClientSocket : public ClientSocket, public csocket
		{
		public:
			CClientSocket(const ServerSocket& parent, int sockfd, const socket_opts& opts);
			CClientSocket(endpoint& ep, async_service *service, const socket_opts& opts);
			virtual ~CClientSocket() { async_cancel(); }
			virtual void setCork(bool);
			virtual void setDelay(bool);
			
			// Data Transmission
			virtual ssize_t send (const void* s, size_t size);
			virtual ssize_t recv (void* s, size_t size);
			//added by mohamed belgacem
			/**
			 * @brief isConnected:
			 * Checks if the socket connection with the server (NativeGateway) is alive.
			 * @return true if the socket is alive. False otherwise.
			 */
			bool isConnected();

			/**
			 * @brief closeAndReconnect: F
			 * Forces the client socket to a clean close and reconnect to the server (NativeGateway).
			 */
			void closeAndReconnect();

			/** -- my be private --
			 * @brief needCheckConnection:
			 * Tells if it needed to check the client socket connection status or not. This is useful to avoid
			 * cheking the status of the socket since the problem of dead half peer
			 * can occure: the server is dead and the client isn't notified
			 * @return True if closeAndReconnect() is required. False otherwise. 
			 */
			bool needCheckConnection();

			/** 
			 * @brief setCheckConnection:
			 * change the needCheckConnection() status.
			 * @param check need to check the client socket status or not.
			 */
			void setCheckConnection(bool check);
			//	
			virtual int hasError();
			virtual void async_cancel();
		protected:
			virtual void connect(bool blocking);

		private:
			CClientSocket(const CClientSocket& other) {}
			bool has_delay, has_cork;
			bool isConnectFirstTime;//to ckeck if it is the 1st time to connect to the java controller
			bool isCheckConnection;
		};

		class CServerSocket : public ServerSocket, public csocket
		{
			public:
				CServerSocket(endpoint& ep, async_service *service, const socket_opts& opts);
				virtual ClientSocket *accept(const socket_opts& opts);

				virtual ~CServerSocket() { async_cancel(); }
			protected:
				virtual void init();
				virtual void listen(int max_connections);
				virtual void async_cancel();
		};

		class CSocketFactory : public SocketFactory
		{
			public:
				CSocketFactory(async_service *service) : SocketFactory(service) {}
				virtual ClientSocket *connect(endpoint& ep, const socket_opts& opts);
				virtual ServerSocket *listen(endpoint& ep, const socket_opts& opts);
		};
	}
} // end namespace muscle

#endif /* defined(__CMuscle__csocket__) */
