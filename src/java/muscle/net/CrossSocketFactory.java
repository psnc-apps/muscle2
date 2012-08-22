package muscle.net;

import java.io.*;
import java.net.*;
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
 * 
 * @author Mariusz Mamonski
 */

public class CrossSocketFactory extends SocketFactory implements jade.imtp.leap.JICP.SocketFactory {
	
	public static class LoggableOutputStream extends FilterOutputStream {
		protected String id;
		
		public LoggableOutputStream(String id, OutputStream out) {
			super(out);
			logger.log(Level.FINEST, "id = {0}", new Object[] {id});
			this.id = id;
		}
		@Override
		public void write(int b) throws IOException {
			logger.log(Level.FINEST, "id = {0}, b = {1}", new Object[] {id, b});
			out.write(b);
		}
		@Override
		public void close() throws IOException {
			logger.log(Level.FINEST, "id = {0}", new Object[] {id});
			out.close();
		}
		@Override
		public void flush() throws IOException {
			logger.log(Level.FINEST, "id = {0}", new Object[] {id});
			out.flush();
		}
		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			logger.log(Level.FINEST, "id = {0}, off = {1}, len = {2}, b[off] = {3}, b[last] = {4}", new Object[] {id, off, len, b[off], b[off+len-1]});
			out.write(b, off, len);
		}

		
	}

	public static class LoggableInputStream extends FilterInputStream {
		protected String id;
		
		public LoggableInputStream(String id, InputStream in) {
			super(in);
			logger.log(Level.FINEST, "id = {0}", new Object[] {id});
			this.id = id;
		}
		@Override
		public int read() throws IOException {
			int b = in.read();
			logger.log(Level.FINEST, "id = {0}, b = {1}", new Object[] {id, b});
			return b;
		}
		@Override
		public void close() throws IOException {
			logger.log(Level.FINEST, "id = {0}", new Object[] {id});
			in.close();
		}
		@Override
		public int available() throws IOException {
			int av = in.available();
			logger.log(Level.FINEST, "id = {0}, available bytes = {1}", new Object[] {id, av});
			return av;
		}
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			logger.log(Level.FINEST, "trying to read: id = {0}, off = {1}, len = {2} ", new Object[] {id, off, len});
			
			try {
				int bread = in.read(b, off, len);
				
				if (bread != -1)
					logger.log(Level.FINEST, "id = {0},  bread = {1}, b[{2}] = {3}, b[{4}] = {5}", new Object[] {id, bread, off, b[off], off+bread-1, b[off+bread-1]});
				else 
					logger.log(Level.FINEST, "id = {0},  bread = {1}", new Object[] {id, bread});

				return bread;
			} catch (IOException ex) {
				logger.log(Level.WARNING, "read failed: {0}", new Object[] {ex});
				throw ex;
			} catch (Exception ex) {
				logger.log(Level.SEVERE, "Unchecked exception: {0}", new Object[] {ex});
				throw new AssertionError(ex);
			}
			/* not reached */
		}	
	}
	
	private final static Logger logger = Logger.getLogger(CrossSocketFactory.class.getName());
	
	public static final String PROP_MAIN_PORT = "pl.psnc.mapper.muscle.magicPort";
	public static final String PROP_MTO_ADDRESS = "pl.psnc.mapper.muscle.mto.address";
	public static final String PROP_MTO_PORT = "pl.psnc.mapper.muscle.mto.port";
	public static final String PROP_MTO_TRACE = "pl.psnc.mapper.muscle.mto.trace";


	public static final String ENV_COORDINATOR_URL = "QCG_COORDINATOR_URL";
	public static final String ENV_SESSION_ID = "SESSION_ID";

	public static final String PUT_MSG_TEMPLATE_1 = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"> "
			+ "<SOAP-ENV:Body><smoacoordinator:PutProcessEntry xmlns:smoacoordinator=\"http://schemas.qoscosgrid.org/coordinator/2009/04/service\"><smoacoordinator:ProcessEntry>"
			+ "<smoacoordinator:ProcessEntryHeader><smoacoordinator:Key>";
	public static final String PUT_MSG_TEMPLATE_2 = /* @SESSION_KEY@ */"</smoacoordinator:Key></smoacoordinator:ProcessEntryHeader>"
			+ "<smoacoordinator:ProcessData><items><items>";
	public static final String PUT_MSG_TEMPLATE_3 = /* @MASTER_HOST@ */"</items><items>";
	public static final String PUT_MSG_TEMPLATE_4 = /* @MASTER_PORT@ */"</items></items></smoacoordinator:ProcessData>"
			+ "</smoacoordinator:ProcessEntry></smoacoordinator:PutProcessEntry></SOAP-ENV:Body></SOAP-ENV:Envelope>";

	public static final String GET_MSG_TEMPLATE_1 = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">"
			+ "<SOAP-ENV:Body><smoacoordinator:GetProcessEntry xmlns:smoacoordinator=\"http://schemas.qoscosgrid.org/coordinator/2009/04/service\">"
			+ "<smoacoordinator:ProcessEntryHeader><smoacoordinator:Key>";
	public static final String GET_MSG_TEMPLATE_2 = /* @SESSION_KEY@ */"</smoacoordinator:Key></smoacoordinator:ProcessEntryHeader></smoacoordinator:GetProcessEntry></SOAP-ENV:Body></SOAP-ENV:Envelope>";

	protected int magicPort = 22;
	protected String socketFile = "socket.file";
	protected InetAddress mtoAddr = null;
	protected int mtoPort = -1;

	public CrossSocketFactory() {
		if (System.getProperty(PROP_MAIN_PORT) != null) {
			magicPort = Integer.valueOf(System.getProperty(PROP_MAIN_PORT));
		}

		if (System.getProperty(PROP_MTO_ADDRESS) != null) {
			try {
				mtoAddr = InetAddress.getByName(System.getProperty(PROP_MTO_ADDRESS));
			} catch (UnknownHostException e) {
				logger.log(Level.SEVERE, "Provided MTO address unresolvable: " + e, e);
			}
		}

		if (System.getProperty(PROP_MTO_PORT) != null) {
			mtoPort = Integer.valueOf(System.getProperty(PROP_MTO_PORT));
		}
	}

	public ServerSocket createServerSocket(int port, int backlog,
			InetAddress addr) throws IOException {
		
		if (port == magicPort) {
			logger.log(Level.FINE, "binding socket on MAIN port and addr {0}", addr);
		} else if (port == 0) {
			logger.log(Level.FINE, "binding socket on ANY port and addr {0}", addr);
		} else {
			logger.log(Level.FINE, "binding socket on port {0} and addr {1}", new Object[]{port, addr});
			throw new java.net.BindException();
		}

		ServerSocket ss = createServerSocketInRange(backlog, addr);

		logger.log(Level.FINE, "bound to port: {0}", ss.getLocalPort());

		if (mtoAddr != null && mtoPort != -1) {
			try {
				mtoRegisterListening((InetSocketAddress) ss.getLocalSocketAddress());
			} catch (IOException ex) {
				throw new IOException("Could not register a server socket at the MTO: " + ex.getMessage());
			}

			logger.info("Registered to MTO");
		} else {
			logger.fine("Missing MTO address / port. MTO will not be used.");
		}

		if (port == magicPort) {
			putConnectionData(InetAddress.getLocalHost()
					.getHostAddress(), ss.getLocalPort());

		}
		return ss;
	}

	private void putConnectionData(String hostAddress, int localPort)
			throws IOException {
		String coordinatorURL = System.getenv(ENV_COORDINATOR_URL);
		String sessionID = System.getenv(ENV_SESSION_ID);
		StringBuilder message = new StringBuilder(4096);

		if (coordinatorURL == null) {
			throw new IOException(ENV_COORDINATOR_URL + " env variable not set");
		}

		if (sessionID == null) {
			throw new IOException(ENV_SESSION_ID + " env variable not set");
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

	private String sendHTTPRequest(URL url, String request) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		StringBuffer response = new StringBuffer(4096);
		OutputStreamWriter osw = null;
		BufferedReader brd = null;

		try {
			logger.log(Level.FINEST, "Request Message: {0}", request);

			conn.setDoOutput(true);
			conn.setRequestMethod("POST");

			osw = new OutputStreamWriter(conn.getOutputStream());
			osw.write(request);
			osw.flush();

			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new IOException("Call to " + url + " failed with code "
						+ conn.getResponseCode());
			}

			brd = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			String line;
			while ((line = brd.readLine()) != null) {
				response.append(line);
			}

			logger.log(Level.FINEST, "Response Message: {0}", response);

		} catch (RuntimeException e) {
			throw e;
		} finally {

			if (osw != null) {
				osw.close();
			}

			if (brd != null) {
				brd.close();
			}
		}

		return response.toString();
	}

	private InetSocketAddress getConnectionData() throws IOException {
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
			throw new IOException("Failed to parse coordinator response: " + ex);
		} catch (ParserConfigurationException ex) {
			throw new AssertionError(ex);
		}

		logger.log(Level.FINE, "Master host: {0}", host);
		logger.log(Level.FINE, "Master port: {0}", port);

		try {
			/**
			 * very ugly but what else we can do if bounded socket is not enough
			 * for JADE...
			 */
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			logger.warning("Wait to resolve socket interrupted.");
		}

		return new InetSocketAddress(host, port);
	}

	public Socket createSocket() {
		logger.fine("creating new client socket");
		return new CrossSocket();
	}
	

	protected class CrossSocket extends Socket {
		protected InetSocketAddress processedEndpoint;
		
		@Override
		public void connect(SocketAddress endpoint, int timeout)
				throws IOException {
			logger.log(Level.FINE, "connecting to: {0}", endpoint);
			processedEndpoint = (InetSocketAddress) processEndpoint(endpoint);

			int port = processedEndpoint.getPort();
			if (port >= portMin && port <= portMax) {
				// direct connection
				super.connect(processedEndpoint, timeout);
			} else {
				if (mtoPort == -1 || mtoAddr == null) {
					logger.severe("Connection out of port range, but MTO is not configured. Trying local");
					super.connect(processedEndpoint, timeout);
					return;
				}

				mtoConnect(timeout, processedEndpoint);
			}
		}
		
		@Override
		public InputStream getInputStream() throws IOException {
			if (System.getProperty(PROP_MTO_TRACE) == null )
				return super.getInputStream();
			else {
				logger.log(Level.FINE, "id = {0} remote = {1}", new Object[]{processedEndpoint,  super.getRemoteSocketAddress()});
				return new LoggableInputStream(processedEndpoint != null ? processedEndpoint.toString() :  super.getRemoteSocketAddress().toString(), super.getInputStream());
			}
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			if (System.getProperty(PROP_MTO_TRACE) == null )
				return super.getOutputStream();
			else {
				logger.log(Level.FINE, "id = {0} remote = {1}", new Object[]{processedEndpoint,  super.getRemoteSocketAddress()});
				return new LoggableOutputStream(processedEndpoint != null ? processedEndpoint.toString() :  super.getRemoteSocketAddress().toString(), super.getOutputStream());
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

			if (iaddr.getPort() == magicPort) {
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
			MtoRequest req = new MtoRequest();
			req.setSource((InetSocketAddress) getLocalSocketAddress());
			req.setDestination(processedEndpoint);
			req.type = MtoRequest.TYPE_CONNECT;

			super.getOutputStream().write(req.write().array());

			// Read answer
			ByteBuffer buffer = ByteBuffer.allocate(MtoRequest.byteSize());
			super.getInputStream().read(buffer.array());
			MtoRequest res = MtoRequest.read(buffer);

			assert res.dstA.equals(req.dstA) && res.dstP == req.dstP
					&& res.srcA.equals(req.srcA) && res.srcP == req.srcP;
			assert res.type == MtoRequest.TYPE_CONNECT_RESPONSE;

			// React
			int response = new DataInputStream(super.getInputStream()).readInt();
			if (response != 0) {
				close();
				throw new IOException("MTO denied connection");
			}
			
			logger.log(Level.FINE, "connected via MTO: {0}:{1}", new Object[]{mtoAddr, mtoPort});
		}

	}

	private class XMLToListHandler extends DefaultHandler {
		protected List<String> parsedValues = new ArrayList<String>();
		protected String selectorName;
		protected boolean activated;

		public XMLToListHandler(String selectorName) {
			this.selectorName = selectorName;
		}

		public void characters(char[] ch, int start, int length)
				throws SAXException {

			if (activated) {
				parsedValues.add(new String(ch, start, length));
				activated = false;
			}
		}

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
		
		if( ! (isa.getAddress() instanceof Inet4Address ) ) {
			return;
		}
		
		
		MtoRequest r = new MtoRequest();
		r.type = MtoRequest.TYPE_REGISTER;
		r.setSource(isa);
		Socket s = new Socket();
		s.connect(new InetSocketAddress(mtoAddr, mtoPort));
		s.getOutputStream().write(r.write().array());
		s.close();
	}
}
