package muscle.core.standalone;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MPIKernel extends NativeKernel {
	private final static Logger logger = Logger.getLogger(MPIKernel.class.getName());

	/**
	 *  Default serial versionUID
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void buildCommand(List<String> command) {
		if (hasProperty("mpiexec_command")) {			
			command.add(getProperty("mpiexec_command"));
		}
		else {
			logger.warning("MPI command variable ''mpiexec_command'' not given. Using mpiexec.");
			command.add("mpiexec");
		}
		
		if (hasProperty("mpiexec_args")) {
			String args[] = getProperty("mpiexec_args").trim().split(" ");
			command.addAll(Arrays.asList(args));
		} else {
			logger.log(Level.WARNING, "MPI arguments variable ''mpiexec_args'' for {0} not given. Not using arguments.", getLocalName());
		}
		
		super.buildCommand(command);
	}

}
