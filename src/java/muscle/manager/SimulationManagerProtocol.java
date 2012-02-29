/*
 * 
 */
package muscle.manager;

/**
 *  Operation codes of the SimulationManagerProtocol.
 * 
 * These operation codes should be used by both client and server. The protocol is as follows: a client requests
 * an operation. This operation number that will be executed is repeated by the server, which then executes it, and then returns a boolean for success. The operations are the following
 * <dl>
 *	<dd>LOCATE</dd>
 *	<dt>For clients to find other clients. Client sends name of ID it wants to locate. SimulationManager tries to locate given ID, and returns
 *              a boolean for success. If it succeeds, this is followed by the TcpLocation of the found ID. This is a
 *              <em>blocking</em> call.</dt>
 *      <dd>REGISTER</dd>
 *      <dt>For clients to register themselves. Client sends the name of its ID, and then its TcpLocation. SimulationManager tries to add the ID; it
 *             will return a false boolean code if it already existed, or true if it was not entered yet.</dt>
 *      <dd>DEREGISTER</dd>
 *     <dt>For clients to deregister themselves. Client sends the name of its ID. SimulationManager tries to remove this ID, and return a boolean
 *             for success.</dt>
 *      <dd>UNSUPPORTED</dd>
 *      <dt>If an operation code is sent that the server does not understand, it will respond with UNSUPPORTED.</dt>
 * </dl>
 * 
 * @author Joris Borgdorff
 */
public enum SimulationManagerProtocol {
	LOCATE, REGISTER, DEREGISTER,
	UNSUPPORTED;
}
