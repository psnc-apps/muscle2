package examples.mpiring;

import muscle.core.Scale;
import muscle.core.model.Distance;

public class PSB2 extends muscle.core.standalone.MPIKernel {

	public Scale getScale() {
		Distance delta = new Distance(1);
		return new Scale(delta,delta);
	}

	public void addPortals() {
		addEntrance("pipe-out", double[].class);
	}

}
