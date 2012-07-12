/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.client.communication;

/**
 *
 * @author bobby
 */
public enum TcpDataProtocol {
	OBSERVATION, SIGNAL, KEEPALIVE, FINISHED;
	public final static int MAGIC_NUMBER = 134405;
}
