package examples.mpiring;

import muscle.core.Scale;
import muscle.core.model.Distance;

public class LHC2 extends muscle.core.standalone.MPIKernel {

	public Scale getScale() {
		Distance delta = new Distance(1);
		return new Scale(delta,delta);
	}

	public void addPortals() {
		addExit("pipe-in", double[].class);
	}

}
