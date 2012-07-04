package examples.simplempi;

import muscle.core.Scale;
import muscle.core.model.Distance;

public class Test extends muscle.core.kernel.CAController {

	static {
		System.loadLibrary("example_simplempi");
	}

	private native void callNative();

	public Scale getScale() {
		Distance delta = new Distance(1);
		return new Scale(delta,delta);
	}

	public void addPortals() {	
	}

	protected void execute() {
		callNative();
	}
}
