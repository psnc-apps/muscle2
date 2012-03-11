/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.core;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.kernel.InstanceController;

/**
 *
 * @author Joris Borgdorff
 */
public class LocalManager {
	private final List<String> agentNames;
	private final List<InstanceController> controllers;
	private final static Logger logger = Logger.getLogger(LocalManager.class.getName());
	
	private LocalManager(LocalManagerOptions opts) {
		agentNames = opts.getAgents();
		if (agentNames.isEmpty()) {
			opts.printUsage();
			System.exit(1);
		}
		controllers = new ArrayList<InstanceController>(agentNames.size());
	}
	
	private void init() {
		for (String name : agentNames) {
			
		}
	}
	
	private void start() {
		
	}
	
	public static void main(String[] args) {
		instance = new LocalManager(new LocalManagerOptions(args));
		
	}

	private static LocalManager instance;

	public static LocalManager getInstance() {
		return instance;
	}
}
