package examples.simplempi;

public class Test extends muscle.core.kernel.CAController {

	static {
		System.loadLibrary("example_simplempi");
	}

	private native void callNative();

	public void addPortals() {	
	}

	protected void execute() {
		callNative();
	}
}
