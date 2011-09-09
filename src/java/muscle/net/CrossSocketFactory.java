package muscle.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.BindException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import jade.imtp.leap.JICP.SocketFactory;

public class CrossSocketFactory  implements SocketFactory {
	
	public static final String PROP_PORT_RANGE_MIN = "pl.psnc.mapper.muscle.portrange.min";
	public static final String PROP_PORT_RANGE_MAX = "pl.psnc.mapper.muscle.portrange.max";
	public static final String PROP_MAIN_PORT = "pl.psnc.mapper.muscle.mainport";
	public static final String PROP_DEBUG = "pl.psnc.mapper.muscle.debug";
	public static final String PROP_TRACE = "pl.psnc.mapper.muscle.trace";
	public static final String ENV_COORDINATOR_URL = "GRMS_COORDINATOR_URL";
	public static final String ENV_SESSION_ID = "SESSION_ID";
	
	public static final String PUT_MSG_TEMPLATE_1 = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"> " + 
		"<SOAP-ENV:Body><smoacoordinator:PutProcessEntry xmlns:smoacoordinator=\"http://schemas.smoa-project.com/coordinator/2009/04/service\"><smoacoordinator:ProcessEntry>" +
		"<smoacoordinator:ProcessEntryHeader><smoacoordinator:Key>";
	public static final String PUT_MSG_TEMPLATE_2 = /* @SESSION_KEY@ */	"</smoacoordinator:Key></smoacoordinator:ProcessEntryHeader>" +
		"<smoacoordinator:ProcessData><items><items>";
	public static final String PUT_MSG_TEMPLATE_3 = /* @MASTER_HOST@ */ "</items><items>";
	public static final String PUT_MSG_TEMPLATE_4 = /* @MASTER_PORT@ */ "</items></items></smoacoordinator:ProcessData>" +
		"</smoacoordinator:ProcessEntry></smoacoordinator:PutProcessEntry></SOAP-ENV:Body></SOAP-ENV:Envelope>";
	
	public static final String GET_MSG_TEMPLATE_1 = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">" + 
		"<SOAP-ENV:Body><smoacoordinator:GetProcessEntry xmlns:smoacoordinator=\"http://schemas.smoa-project.com/coordinator/2009/04/service\">" + 
		"<smoacoordinator:ProcessEntryHeader><smoacoordinator:Key>";
	public static final String GET_MSG_TEMPLATE_2 =	/* @SESSION_KEY@ */ "</smoacoordinator:Key></smoacoordinator:ProcessEntryHeader></smoacoordinator:GetProcessEntry></SOAP-ENV:Body></SOAP-ENV:Envelope>";
	
	protected int portMin = 9000;
	protected int portMax = 9500;
	protected int mainPort = 22;
	protected boolean debug = true;
	protected boolean trace = false;
	protected String socketFile = "socket.file";
	
	public CrossSocketFactory() {
		if ( System.getProperty(PROP_PORT_RANGE_MIN) != null ) {
			portMin = Integer.valueOf(System.getProperty(PROP_PORT_RANGE_MIN));
		}
		
		if ( System.getProperty(PROP_PORT_RANGE_MAX) != null ) {
			portMax = Integer.valueOf(System.getProperty(PROP_PORT_RANGE_MAX));
		}
		
		if ( System.getProperty(PROP_MAIN_PORT) != null ) {
			mainPort = Integer.valueOf(System.getProperty(PROP_MAIN_PORT));
		}
		
		if ( System.getProperty(PROP_DEBUG) != null ) {
			debug = Boolean.valueOf(System.getProperty(PROP_DEBUG));
		}
		
		if ( System.getProperty(PROP_TRACE) != null ) {
			trace = Boolean.valueOf(System.getProperty(PROP_TRACE));
		}
	}


	@Override
	public ServerSocket createServerSocket(int port, int backlog, InetAddress addr) throws IOException {
		logDebug("binding socket on port " + port + " and addr " + addr );
		trace();
		
		if (port == mainPort || port == 0) {
			ServerSocket ss = null;
			BindException lastEx = null;
			
			for (int i = portMin; i <= portMax; i++) {
				try {
					logDebug("Trying to bind on port: " + i);
					ss = new ServerSocket(i, backlog, addr);
					break;
				} catch (BindException ex) {
					logDebug("BindFailed: " + ex.getMessage());
					lastEx = ex;
				}
			}
			
			if (ss != null) {
				System.err.println("Binded to port:");
				System.err.println(ss.getLocalPort());
				
				if (port == mainPort) {
					putConnectionData( InetAddress.getLocalHost().getHostAddress(), ss.getLocalPort());

				}
				return ss;
			} else {
				throw lastEx;
			}
			
		} else {
			throw new java.net.BindException();
		}
	}

	private void putConnectionData(String hostAddress, int localPort) throws IOException {
		String coordinatorURL = System.getenv(ENV_COORDINATOR_URL);
		String sessionID = System.getenv(ENV_SESSION_ID);
		StringBuffer message = new StringBuffer(4096);
		
		if (coordinatorURL == null)
			throw new IOException(ENV_COORDINATOR_URL + " env variable not set");

		if (sessionID == null)
			throw new IOException(ENV_SESSION_ID + " env variable not set");
		
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
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		StringBuffer response = new StringBuffer(4096);
		OutputStreamWriter osw = null;
		BufferedReader brd = null; 
	
		try {
			
			if (trace) 
				logDebug("Request Message: " + request);
			
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			
			osw = new OutputStreamWriter(conn.getOutputStream());
			osw.write(request);
			osw.flush();
	
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK)
				throw new IOException("Call to " + url + " failed with code " + conn.getResponseCode());
	
		    brd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = brd.readLine()) != null) {
				response.append(line);
			}
			
			if (trace)
				logDebug("Response Message: " + response);
			
		} finally {
			
			if (osw != null)
				osw.close();
			
			if (brd != null)
				brd.close();
			
			
		}
		
		return response.toString();
	}
	
	private InetSocketAddress getConnectionData() throws IOException {
		String coordinatorURL = System.getenv(ENV_COORDINATOR_URL);
		String sessionID = System.getenv(ENV_SESSION_ID);
		StringBuffer message = new StringBuffer(4096);
		
		logDebug("Acquiring connection data (" +  coordinatorURL + ")");
		
		if (coordinatorURL == null)
			throw new IOException(ENV_COORDINATOR_URL + " env variable not set");

		if (sessionID == null)
			throw new IOException(ENV_SESSION_ID + " env variable not set");

		URL url = new URL(coordinatorURL);
		
		message.append(GET_MSG_TEMPLATE_1);
		message.append(sessionID);
		message.append(GET_MSG_TEMPLATE_2);

		String response = sendHTTPRequest(url, message.toString());
		String host = null;
		int port = -1;
		
		try {
			/* This little code save couple of MB needed for WebService framework */
			SAXParserFactory factory = SAXParserFactory.newInstance();
		    SAXParser saxParser = factory.newSAXParser();
		    XMLToListHandler saxHandler = new XMLToListHandler("items");
		    saxParser.parse(new InputSource(new StringReader(response)), saxHandler);
		    
		    if (saxHandler.getParsedValues().size() != 2) {
			    for (String value : saxHandler.getParsedValues()) {
			    	logDebug(" parsed value: " + value);
			    }
		    	throw new IOException("Unexpected coordinator response (items count = " + saxHandler.getParsedValues().size() + " )");
		    }
		    
		    host = saxHandler.getParsedValues().get(0);
		    port = Integer.parseInt(saxHandler.getParsedValues().get(1));
		} catch (SAXException ex) {
			throw new IOException("Failed to parse coordinator response: " + ex);
		} catch (ParserConfigurationException ex) {
			throw new AssertionError(ex);
		}
		
		logDebug("Master host: " + host);
		logDebug("Master port: " + port);
		
		
		try {
			/** very ugly but what else we can do if bounded socket is not enough for JADE... */
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return new InetSocketAddress(host, port);
	}


	protected void trace() {
		if (trace) {
			System.err.println("---TRACE---");
			try {
				throw new Exception();
			} catch ( Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	protected void logDebug(String msg) {
		if (debug) {
			System.err.println("-- XSS -- : " + msg);
		}
	}

	@Override
	public Socket createSocket() {
		logDebug("creating new client socket");
		trace();
		return new CrossSocket();
	}
	
	public class CrossSocket extends Socket {

		@Override
		public void connect(SocketAddress endpoint, int timeout)
				throws IOException {
			logDebug("connecting to:" +  endpoint);
			super.connect(processEndpoint(endpoint), timeout);
		}

		@Override
		public void connect(SocketAddress endpoint) throws IOException {
			logDebug("connecting to:" + endpoint);
			super.connect(processEndpoint(endpoint));
		}

		private SocketAddress processEndpoint(SocketAddress endpoint) throws IOException {
			InetSocketAddress iaddr = (InetSocketAddress)endpoint;
			
			if (iaddr.getPort() == mainPort)
				return getConnectionData();
			else
				return endpoint;
		}
		
	}
	
	private class XMLToListHandler extends DefaultHandler {
		protected List<String> parsedValues = new ArrayList<String>();
		protected String selectorName;
		protected boolean activated;
		
		public XMLToListHandler(String selectorName) {
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
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (qName.endsWith(selectorName))
				activated = true;
		}
		
		public List<String> getParsedValues() {
			return parsedValues;
		}
		
	}


}
