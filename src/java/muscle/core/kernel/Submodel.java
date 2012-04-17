/*
 * 
 */

package muscle.core.kernel;

/**
 * A submodel following the description of the Multiscale Modeling Language. This means it has the submodel
 * execution loop. The boundary condition and solving step operator are combined into one, into the solvingStep()
 * function.
 * @author Joris Borgdorff
 */
public abstract class Submodel extends Instance {	
	@Override
	protected void execute() {
		this.operationsAllowed = RECV;
		this.init();
		while (!willStop()) {
			this.operationsAllowed = SEND;
			this.intermediateObservation();
			this.operationsAllowed = RECV;
			this.solvingStep();
		}
		this.operationsAllowed = SEND;
		this.finalObservation();
	}
	
	/**
	 * Initializes the submodel.
	 * It will be called once at the start. Override to implement the f_init operator in MML. May only receive messages. 
	 */
	protected void init() {}
	
	/**
	 * Perform one step of the submodel.
	 * It will be called while the model is active. Override to implement the S and B operators in MML. During this step, messages may only received.
	 */
	protected void solvingStep() {}
	
	/**
	 * Send messages from within the loop.
	 * It will be called while the model is active. Override to implement the O_i operator in MML. During this step, messages may only be sent.
	 */
	protected void intermediateObservation() {}
	
	/**
	 * Send messages and clean up as a final operation.
	 * It will be called at the end of a submodel. Override to implement the O_f operator in MML. During this step, messages may only be sent.
	 */
	protected void finalObservation() {}
}
