package muscle.core.standalone;

import java.util.Arrays;
import java.util.List;
import muscle.core.CxADescription;

public class MPIKernel extends NativeKernel {

	/**
	 *  Default serial versionUID
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void buildCommand(List<String> command) {
		if (CxADescription.ONLY.containsKey(getLocalName() + ":mpiexec_command"))
			command.add(CxADescription.ONLY.getProperty(getLocalName() + ":mpiexec_command"));
		else
			command.add("mpiexec");
		
		if (CxADescription.ONLY.containsKey(getLocalName() + ":mpiexec_args")) {
			String args[] = CxADescription.ONLY.getProperty(getLocalName() + ":mpiexec_args").split(" ");
			command.addAll(Arrays.asList(args));
		}
		
		super.buildCommand(command);
	}

}
