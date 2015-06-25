/*
* Copyright 2008, 2009 Complex Automata Simulation Technique (COAST) consortium
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
package muscle.net;

import muscle.util.data.LoggableInputStream;
import muscle.util.data.LoggableOutputStream;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** 
 * @author Mariusz Mamonski
 */

public class CrossSocketFactory extends SocketFactory {
	private final static Logger logger = Logger.getLogger(CrossSocketFactory.class.getName());
	
	private final static String PROP_MAIN_PORT = "pl.psnc.mapper.muscle.magicPort";
	private final static String PROP_MTO_ADDRESS = "pl.psnc.mapper.muscle.mto.address";
	private final static String PROP_MTO_PORT = "pl.psnc.mapper.muscle.mto.port";
	public final static String PROP_MTO_TRACE = "pl.psnc.mapper.muscle.mto.trace";


	private final static String ENV_COORDINATOR_URL = "QCG_COORDINATOR_URL";
	private final static String ENV_SESSION_ID = "SESSION_ID";
	private final static String ENV_QCG_COORDINATOR_VIA_MTO = "QCG_COORDINATOR_VIA_MTO";

	private final static String PUT_MSG_TEMPLATE_1 = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"> "
			+ "<SOAP-ENV:Body><smoacoordinator:PutProcessEntry xmlns:smoacoordinator=\"http://schemas.qoscosgrid.org/coordinator/2009/04/service\"><smoacoordinator:ProcessEntry>"
			+ "<smoacoordinator:ProcessEntryHeader><smoacoordinator:Key>";
	private final static String PUT_MSG_TEMPLATE_2 = /* @SESSION_KEY@ */"</smoacoordinator:Key></smoacoordinator:ProcessEntryHeader>"
			+ "<smoacoordinator:ProcessData><items><items>";
	private final static String PUT_MSG_TEMPLATE_3 = /* @MASTER_HOST@ */"</items><items>";
	private final static String PUT_MSG_TEMPLATE_4 = /* @MASTER_PORT@ */"</items></items></smoacoordinator:ProcessData>"
			+ "</smoacoordinator:ProcessEntry></smoacoordinator:PutProcessEntry></SOAP-ENV:Body></SOAP-ENV:Envelope>";

	private final static String GET_MSG_TEMPLATE_1 = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">"
			+ "<SOAP-ENV:Body><smoacoordinator:GetProcessEntry xmlns:smoacoordinator=\"http://schemas.qoscosgrid.org/coordinator/2009/04/service\">"
			+ "<smoacoordinator:ProcessEntryHeader><smoacoordinator:Key>";
	private final static String GET_MSG_TEMPLATE_2 = /* @SESSION_KEY@ */"</smoacoordinator:Key></smoacoordinator:ProcessEntryHeader></smoacoordinator:GetProcessEntry></SOAP-ENV:Body></SOAP-ENV:Envelope>";
    
	/** Port number that signifies that QCG Coordinator should be notified of the actually used port number. */
	protected final int qcgMagicPort;	
	protected final int mtoPort;
	protected final InetAddress mtoAddr;
	private boolean isRetrievingMainPort;
	private InetSocketAddress mainAddr;

	public CrossSocketFactory() {
		qcgMagicPort = Integer.valueOf(System.getProperty(PROP_MAIN_PORT, "22"));
		mtoPort = Integer.valueOf(System.getProperty(PROP_MTO_PORT, "-1"));
		
		InetAddress addr = null;
		if (System.getProperty(PROP_MTO_ADDRESS) != null) {
			try {
				addr = InetAddress.getByName(System.getProperty(PROP_MTO_ADDRESS));
			} catch (UnknownHostException e) {
				logger.log(Level.SEVERE, "Provided MTO address unresolvable.", e);
			}
		}
		mtoAddr = addr;
		mainAddr = null;
		isRetrievingMainPort = false;
	}

	/**
	 * Create a server socket.
	 * 
	 * If the MTO details are given, the socket is created within the given range and the MTO is notified of
	 * the new server socket. If the MTO details are not given, or only partially, create a standard Java server
	 * socket within the given range.
	 */
	public ServerSocket createServerSocket(int port, int backlog,
			InetAddress addr) throws IOException {
		
		ServerSocket ss;
		if (port == qcgMagicPort || port == 0) {
			logger.log(Level.FINE, "binding socket on ANY port and addr {0}", addr);
			ss = createServerSocketInRange(backlog, addr);
			logger.log(Level.FINE, "bound to port: {0}", ss.getLocalPort());
		} else {
			logger.log(Level.FINE, "binding socket on port {0} and addr {1}", new Object[]{port, addr});
			ss = new ServerSocket(port, backlog, addr);
		}

		if (mtoAddr == null || mtoPort == -1) {
			logger.fine("Missing MTO address / port. MTO will not be used.");
		} else {
			try {
				mtoRegisterListening((InetSocketAddress) ss.getLocalSocketAddress());
			} catch (IOException ex) {
				throw new IOException("Could not register a server socket at the MTO", ex);
			}

			logger.info("Registered to MTO");
		}

		if (port == qcgMagicPort) {
			try {
				putConnectionData(InetAddress.getLocalHost()
					.getHostAddress(), ss.getLocalPort());
				logger.info("Registered to QCG-Coordinator");
			} catch (IOException ex) {
				ss.close();
				throw new IOException("Cannot communicate with QCG-Coordinator", ex);
			}
		}
		return ss;
	}

	/**
	 * Put server socket connection data to the QCG Coordinator.
	 * 
	 * Requires environment variables ENV_COORDINATOR_URL (QCG location) and ENV_SESSION_ID (unique identifier of the simulation) to be set.
	 */
	private void putConnectionData(String hostAddress, int localPort)
			throws IOException {
		String coordinatorURL = System.getenv(ENV_COORDINATOR_URL);
		String sessionID = System.getenv(ENV_SESSION_ID);
		StringBuilder message = new StringBuilder(4096);

		if (coordinatorURL == null) {
			throw new IOException(ENV_COORDINATOR_URL + " environment variable not set, which is needed to connect to QCG.");
		}

		if (sessionID == null) {
			throw new IOException("QCG environment variable " + ENV_SESSION_ID + " not set; choose an arbitrary unique identifier for your simulation.");
		}

		message.append(PUT_MSG_TEMPLATE_1);
		message.append(sessionID);
		message.append(PUT_MSG_TEMPLATE_2);
		message.append(hostAddress);
		message.append(PUT_MSG_TEMPLATE_3);
		message.append(localPort);
		message.append(PUT_MSG_TEMPLATE_4);

		URL url = new URL(coordinatorURL);

		sendHTTPRequest(url, message.toString());
	}

	/**
	 * Custom implementation to send an HTTP request.
	 * 
	 * If the MTO details and ENV_QCG_COORDINATOR_VIA_MTO environment variable are set,
	 * the HTTP request will be performed through MTO.
	*/
	private String sendHTTPRequest(URL url, String request) throws IOException {
		StringBuffer response = new StringBuffer(4096);
		
        Socket s;
        
		if (System.getenv(ENV_QCG_COORDINATOR_VIA_MTO) == null) {
            s = new Socket();
        } else {
            logger.log(Level.INFO, "Connecting to QCG-Coordinator via MTO");
            s = new CrossSocket(true);
        }

        try {
            s.connect(new InetSocketAddress(url.getHost(), url.getPort()));

            OutputStreamWriter osw = new OutputStreamWriter( s.getOutputStream() );
            osw.write("POST ");
            osw.write(url.getFile());
            osw.write(" HTTP/1.1\r\nHost ");
            osw.write(url.getHost());
            osw.write("\r\nConnection: close\r\n\r\n");
            osw.write(request);
            osw.flush();

            BufferedReader brd = new BufferedReader(new InputStreamReader(s.getInputStream()));

            String line;
            boolean content = false;
            while ((line = brd.readLine()) != null) {
                if (line.matches(".*SOAP-ENV:Envelope.*")) {
                    content = true;
                }
                if (content) {
                    response.append(line);
                }
            }
        } finally {
            s.close();
        }
		
		logger.log(Level.FINEST, "Response Message: {0}", response);

		return response.toString();
	}

	/**
	 * Get the connection data from the QCG Coordinator.
	 *
	 * The connection data is only queried from the QCG coordinator as long as there was no successful
	 * result. Only one query to the Coordinator is performed at a time, managed by a wait/notify scheme.
	 */
	private InetSocketAddress getConnectionData() throws IOException {
		synchronized (this) {
			try {
				while (this.isRetrievingMainPort) {
					wait();
				}
			} catch (InterruptedException ex) {
				throw new IOException("Interrupted while retrieving main port", ex);
			}
			
			if (this.mainAddr != null) {
				return this.mainAddr;
			} else {
				this.isRetrievingMainPort = true;
			}
		}
		String coordinatorURL = System.getenv(ENV_COORDINATOR_URL);
		String sessionID = System.getenv(ENV_SESSION_ID);
		StringBuilder message = new StringBuilder(4096);

		logger.log(Level.FINE, "Acquiring connection data ({0})", coordinatorURL);

		if (coordinatorURL == null) {
			throw new IOException(ENV_COORDINATOR_URL + " env variable not set");
		}

		if (sessionID == null) {
			throw new IOException(ENV_SESSION_ID + " env variable not set");
		}

		URL url = new URL(coordinatorURL);

		message.append(GET_MSG_TEMPLATE_1);
		message.append(sessionID);
		message.append(GET_MSG_TEMPLATE_2);

		String response = sendHTTPRequest(url, message.toString());
		String host = null;
		int port = -1;

		try {
			/*
			 * This little code save couple of MB needed for WebService
			 * framework
			 */
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			XMLToListHandler saxHandler = new XMLToListHandler("items");
			saxParser.parse(new InputSource(new StringReader(response)),
					saxHandler);

			if (saxHandler.getParsedValues().size() != 2) {
				for (String value : saxHandler.getParsedValues()) {
					logger.log(Level.FINER, " parsed value: {0}", value);
				}
				throw new IOException(
						"Unexpected coordinator response (items count = "
								+ saxHandler.getParsedValues().size() + " )");
			}

			host = saxHandler.getParsedValues().get(0);
			port = Integer.parseInt(saxHandler.getParsedValues().get(1));
		} catch (SAXException ex) {
			throw new IOException("Failed to parse coordinator response: " + ex.getMessage() + " response: " + response );
		} catch (ParserConfigurationException ex) {
			throw new AssertionError(ex);
		}

		logger.log(Level.FINE, "Master host: {0}", host);
		logger.log(Level.FINE, "Master port: {0}", port);

		synchronized (this) {
			this.mainAddr = new InetSocketAddress(host, port);
			this.isRetrievingMainPort = false;
			this.notifyAll();
			return this.mainAddr;
		}
	}

	@Override
	public Socket createSocket() {
		logger.fine("creating new client socket");
        return new CrossSocket();
	}
	

	protected class CrossSocket extends Socket {
		protected InetSocketAddress processedEndpoint;
		private final boolean forceMto;
        
        CrossSocket(boolean forceMto) {
            super();
            this.forceMto = forceMto;
        }
        
        CrossSocket() {
            this(false);
        }
        
		@Override
		public void connect(SocketAddress endpoint, int timeout)
				throws IOException {
			logger.log(Level.FINE, "connecting to: {0}", endpoint);
			processedEndpoint = (InetSocketAddress) processEndpoint(endpoint);

            int port = processedEndpoint.getPort();
            if (forceMto || port < portMin || port > portMax) {
				if (mtoPort == -1 || mtoAddr == null) {
					logger.warning("non-default TCP port used and no MTO to resolve it.");
					super.connect(processedEndpoint, timeout);
					return;
				}

				mtoConnect(timeout, processedEndpoint);
			} else {
				// direct connection
				super.connect(processedEndpoint, timeout);			
            }
		}
		
		@Override
		public InputStream getInputStream() throws IOException {
			if (System.getProperty(PROP_MTO_TRACE) == null ) {
				return super.getInputStream();
			}
			else {
				logger.log(Level.FINE, "id = {0} remote = {1}", new Object[]{processedEndpoint,  super.getRemoteSocketAddress()});
                String id = processedEndpoint != null ? processedEndpoint.toString() :  super.getRemoteSocketAddress().toString();
				return new LoggableInputStream(logger, id, super.getInputStream());
			}
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			if (System.getProperty(PROP_MTO_TRACE) == null ) {
				return super.getOutputStream();
			} else {
				logger.log(Level.FINE, "id = {0} remote = {1}", new Object[]{processedEndpoint,  super.getRemoteSocketAddress()});
                String id = processedEndpoint != null ? processedEndpoint.toString() :  super.getRemoteSocketAddress().toString();
				return new LoggableOutputStream(logger, id, super.getOutputStream());
			}
		}
		
		@Override
		public SocketChannel getChannel() {
			logger.log(Level.FINE, "id = {0} remote = {1}", new Object[]{processedEndpoint,  super.getRemoteSocketAddress()});
			return super.getChannel();
		}

		@Override
		public void connect(SocketAddress endpoint) throws IOException {
			connect(endpoint, 0);
		}

		private SocketAddress processEndpoint(SocketAddress endpoint)
				throws IOException {
			InetSocketAddress iaddr = (InetSocketAddress) endpoint;

			if (iaddr.getPort() == qcgMagicPort) {
				return getConnectionData();
			}
			else {
				return endpoint;
			}
		}

		private void mtoConnect(int timeout, InetSocketAddress processedEndpoint)
				throws IOException {
			super.connect(new InetSocketAddress(mtoAddr, mtoPort), timeout);

			// prepare & send request
			MtoRequest req = new MtoRequest(MtoRequest.TYPE_CONNECT,
				(InetSocketAddress)getLocalSocketAddress(), processedEndpoint);
			
			super.getOutputStream().write(req.write().array());

			// Read answer
			ByteBuffer buffer = ByteBuffer.allocate(MtoRequest.byteSize());
			super.getInputStream().read(buffer.array());
			MtoRequest res = MtoRequest.read(buffer);

			assert res.dstA.equals(req.dstA) && res.dstP == req.dstP
					&& res.srcA.equals(req.srcA) && res.srcP == req.srcP;
			assert res.type == MtoRequest.TYPE_CONNECT_RESPONSE;

			// React
			long response = new DataInputStream(super.getInputStream()).readLong();
			if (response != 0) {
				close();
				throw new IOException("MTO denied connection");
			}
			
			logger.log(Level.FINE, "connected via MTO: {0}:{1}", new Object[]{mtoAddr, mtoPort});
		}

	}

	private class XMLToListHandler extends DefaultHandler {
		protected final List<String> parsedValues;
		protected final String selectorName;
		protected boolean activated;

		XMLToListHandler(String selectorName) {
            this.parsedValues = new ArrayList<String>(2);
			this.selectorName = selectorName;
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {

			if (activated) {
				parsedValues.add(new String(ch, start, length));
				activated = false;
			}
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if (qName.endsWith(selectorName)) {
				activated = true;
			}
		}

		public List<String> getParsedValues() {
			return parsedValues;
		}

	}

	private void mtoRegisterListening(InetSocketAddress isa) throws IOException {
		// If one tries to register a wildcard, register all known addresses
		if(isa.getAddress().isAnyLocalAddress())
		{
			for( NetworkInterface interf : Collections.list(NetworkInterface.getNetworkInterfaces())) {
				for(InetAddress addr : Collections.list(interf.getInetAddresses())) {
					mtoRegisterListening(new InetSocketAddress(addr, isa.getPort()));
				}
			}
			return;
		}
		
		// If one registers loopback, do it only if the MTO is on loopback as well
		if(isa.getAddress().isLoopbackAddress() && ! mtoAddr.isLoopbackAddress()) {
			return;
		}		
		
		MtoRequest r = new MtoRequest(MtoRequest.TYPE_REGISTER, isa, null);
		Socket s = new Socket();
		s.connect(new InetSocketAddress(mtoAddr, mtoPort));
		try {
			s.getOutputStream().write(r.write().array());
		} finally {
			s.close();
		}
	}
}
