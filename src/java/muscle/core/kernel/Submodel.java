/*
 * 
 */

package muscle.core.kernel;

import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.ConduitEntranceController;
import muscle.core.ConduitExitController;
import muscle.core.model.Distance;
import muscle.core.model.Timestamp;

/**
 * A submodel following the description of the Multiscale Modeling Language. This means it has the submodel
 * execution loop. The boundary condition and solving step operator are combined into one, into the solvingStep()
 * function.
 * @author Joris Borgdorff
 */
public abstract class Submodel extends Instance {
	private final static Logger logger = Logger.getLogger(Submodel.class.getName());
	private Timestamp currentTime = null;
	private Distance previousDt;
	
	@Override
	protected void execute() {
		previousDt = getScale().getDt();
		while (true) {
			this.operationsAllowed = RECV;
			originTime = this.init(originTime);
			if (currentTime != null && currentTime.compareTo(originTime) > 0) {
				logger.log(Level.WARNING, "Jumping back in time when restarting submodel; from {0} to {1}", new Object[]{currentTime, originTime});
			}
			currentTime = originTime;
			for (ConduitExitController exit : this.exits.values()) {
				exit.resetTime(originTime);
			}
			for (ConduitEntranceController entrance : this.entrances.values()) {
				entrance.resetTime(originTime);
			}
			this.resetDt();
			while (!endCondition()) {
				this.operationsAllowed = SEND;
				this.intermediateObservation();
				this.operationsAllowed = RECV;
				this.solvingStep();
				this.resetDt();
				this.currentTime = this.currentTime.add(getScale().getDt());
			}
			this.resetLastDt();
			this.operationsAllowed = SEND;
			this.finalObservation();
			this.resetDt();
			this.operationsAllowed = RECV;
			if (!restartSubmodel()) {
				break;
			}
		}
	}
	
	private void resetDt() {
		Distance dt = getScale().getDt();
		if (!previousDt.equals(dt)) {
			for (ConduitEntranceController ec : this.entrances.values()) {
				ec.getEntrance().setDt(dt);
			}
			previousDt = dt;
		}
	}

	private void resetLastDt() {
		Distance omegaT = getScale().getOmegaT();
		if (!previousDt.equals(omegaT)) {
			for (ConduitEntranceController ec : this.entrances.values()) {
				if (ec.getSITime().equals(originTime)) {
					ec.getEntrance().setDt(omegaT);
				}
			}
			previousDt = omegaT;
		}
	}
	
	/**
	 * Initializes the submodel, determining at what time it will start.
	 * It will be called once at the start. Override to implement the f_init operator in MML. May only receive messages. The default implementation is
	 * to start at origin 0, and to increase this with getScale().getOmegaT() every next run.
	 * @param previousOrigin the previous time origin. This is null if there was no previous origin.
	 * @return the new time origin. This can be determined for instance by the timestamp of a conduitExit.receiveObservation().getTimestamp() or simply 0.
	 */
	protected Timestamp init(Timestamp previousOrigin) {
		if (previousOrigin == null) {
			return Timestamp.ZERO;
		} else {
			return previousOrigin.add(getScale().getOmegaT());
		}
	}
	
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

	/**
	 * Determines whether the submodel will exit the submodel execution loop, and jump to the final observation.
	 * @return by default, the value of willStop().
	 */
	protected boolean endCondition() {
		return willStop();
	}

	/**
	 * Determines whether a submodel will be restarted, once it is finished.
	 * @return false by default.
	 */
	protected boolean restartSubmodel() {
		return false;
	}
	
	protected Timestamp getCurrentTime() {
		return this.currentTime;
	}

	@Override
	public boolean willStop() {
		logger.log(Level.FINER, "Submodel time at willstop: {0}; origin time t_0: {1}; end time T+t_0: {2}", new Object[]{currentTime, originTime, originTime.add(getScale().getOmegaT())});
		return super.willStop() || currentTime.compareTo(originTime.add(getScale().getOmegaT())) >= 0 || currentTime.compareTo(maxTime) >= 0;
	}
}
