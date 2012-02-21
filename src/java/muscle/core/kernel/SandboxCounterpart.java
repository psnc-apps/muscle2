///*
//Copyright 2008,2009 Complex Automata Simulation Technique (COAST) consortium
//
//GNU Lesser General Public License
//
//This file is part of MUSCLE (Multiscale Coupling Library and Environment).
//
//    MUSCLE is free software: you can redistribute it and/or modify
//    it under the terms of the GNU Lesser General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    MUSCLE is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU Lesser General Public License for more details.
//
//    You should have received a copy of the GNU Lesser General Public License
//    along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
//*/
//
//package muscle.core.kernel;
//
//
//import java.util.ArrayList;
//
//import utilities.MiscTool;
//import muscle.core.CxADescription;
//import muscle.core.DataTemplate;
//import muscle.core.Portal;
//import muscle.core.ConduitExit;
//import muscle.core.ConduitEntrance;
////import muscle.core.DataPattern;
//
//import muscle.exception.MUSCLERuntimeException;
//import java.lang.reflect.Array;
//import javatool.ClassTool;
//import javatool.ArraysTool;
//import javax.measure.DecimalMeasure;
//import javax.measure.quantity.Length;
//import muscle.core.Sandbox.MOCKPortalID;
//
//
///**
//a generic kernel whose entrances and exits can be defined at runtime<br>
//the two action methods simply send/receive dummy data to/from the portals
//@author Jan Hegewald
//*/
//public class SandboxCounterpart extends muscle.core.kernel.CAController {
//
//	// intermix entrances and exits because we need their correct ordering
//	private Portal[] portals;
//	private int time;
//	private ArrayList<DecimalMeasure<Length>> allDx;
//
//	// args helper class
//	public static class Arguments {
//
//		private Portal[] portals;
//
//		public Arguments(Portal ... newPortals) {
//			portals = newPortals;
//		}
//
//		static public Arguments newInstance(Object ... rawArgs) {
//			if(rawArgs == null || rawArgs.length == 0)
//				throw new IllegalArgumentException("can not initialize without arguments");
//
//			// find our args
//			int index = ArraysTool.indexForInstanceOf(rawArgs, Arguments.class);
//			if(index > -1) {
//				return (Arguments)rawArgs[index];
//			}
//
//			throw new MUSCLERuntimeException("initialization from <"+MiscTool.toString(rawArgs)+"> is not supported");
//		}
//	}
//
//
//	//
//	public muscle.core.Scale getScale() {
//
//		// use standard dt
//		// use dx from first moc portal
//		javax.measure.DecimalMeasure<javax.measure.quantity.Duration> dt = javax.measure.DecimalMeasure.valueOf(new java.math.BigDecimal(1), javax.measure.unit.SI.SECOND);
//		assert allDx != null;
//		return new muscle.core.Scale(dt,allDx);
//	}
//
//
//	//
//	protected void addPortals() {
//
//		Arguments args = Arguments.newInstance(getArguments());
//		portals = args.portals;
//
//		// use all dx from fist portal for this sandbox kernel
//		if(portals.length > 0)
//			allDx = portals[0].getDataTemplate().getScale().getAllDx();
//
//		// add our portals
//		for(Portal p : portals) {
//			p.setOwner(this);
//
//			if(p instanceof ConduitEntrance)
//				addEntrance((ConduitEntrance)p);
//			if(p instanceof ConduitExit)
//				addExit((ConduitExit)p);
//		}
//	}
//
//
//	//
//	protected void execute() {
//
//		int globalStepCount = CxADescription.ONLY.getIntProperty(CxADescription.Key.MAX_TIMESTEPS);
//		getLogger().info("\n\n---------- "+javatool.ClassTool.getName(getClass())+" is starting"+"\nglobalStepCount:"+globalStepCount);
//
//		for(time = 0; !stop(); time++) {
//			getLogger().info("  -----  time:"+time+"  -----");
//
//			try {
//				for(Portal p : portals) {
//					if(p instanceof ConduitEntrance)
//						action((ConduitEntrance)p, time);
//					if(p instanceof ConduitExit)
//						action((ConduitExit)p, time);
//				}
//			}
//			catch(MUSCLERuntimeException e) {
//				// we will get a java.lang.InterruptedException wrapped in a MUSCLERuntimeException if a mock is being killed while it is running
//				// this might be fine if the 'real' agent has already terminated itself
//			}
//		}
//	}
//
//
//	//
//	private void action(ConduitEntrance<?> entrance, int time) {
//
////		int dataSize = entrance.getDataTemplate().getSize();
////		if(dataSize == DataTemplate.ANY_SIZE) {
////			int s = 42;
////			getLogger().info("substituting DataTemplate.ANY_SIZE with <"+s+">");
////			dataSize = s;
////		}
//		int dataSize = 42;
//		String envval = System.getenv("musclesandbox_entrance_"+((MOCKPortalID)entrance.getPortalID()).getOriginalStrippedName());
//		if( envval != null )
//			dataSize = Integer.parseInt(envval);
//
//		// create and fill our data array
//		Object rawData = null;
//		if(entrance.getDataTemplate().getDataClass().isArray()) {
//			rawData = Array.newInstance(entrance.getDataTemplate().getDataClass().getComponentType(), dataSize);
//			Class componentWrapperClass = null;
//			try {
//				componentWrapperClass = ClassTool.wrapperClassForPrimitiveClass(entrance.getDataTemplate().getDataClass().getComponentType());
//			}
//			catch(java.lang.ClassNotFoundException e) {
//				throw new MUSCLERuntimeException(e);
//			}
//			// only fill with custom data if content type can be converted to a number
//			if( java.lang.Number.class.isAssignableFrom(componentWrapperClass) ) {
//				for(int i = 0; i < dataSize; i++) {
//					Object val = new Integer(time);
//					Array.set(rawData, i, val);
//				}
//			}
//		}
//		else {
//			rawData = Array.newInstance(entrance.getDataTemplate().getDataClass(), dataSize);
//			Class componentWrapperClass = null;
//			try {
//				componentWrapperClass = ClassTool.wrapperClassForPrimitiveClass(entrance.getDataTemplate().getDataClass());
//			}
//			catch(java.lang.ClassNotFoundException e) {
//				throw new MUSCLERuntimeException(e);
//			}
//			// only fill with custom data if content type can be converted to a number
//			if( java.lang.Number.class.isAssignableFrom(componentWrapperClass) ) {
//				for(int i = 0; i < dataSize; i++) {
//					Object val = new Integer(time);
//					Array.set(rawData, i, val);
//				}
//			}
//		}
//
//		assert dataSize >= 0;
//
//		entrance.send(rawData);
//	}
//
//
//	//
//	private void action(ConduitExit exit, int time) {
//
//		Class<?> dataClass = exit.getDataTemplate().getDataClass();
////		int targetSize = exit.getDataTemplate().getSize();
//		int targetSize = 42;
//		String envval = System.getenv("musclesandbox_exit_"+((MOCKPortalID)exit.getPortalID()).getOriginalStrippedName());
//		if( envval != null )
//			targetSize = Integer.parseInt(envval);
//
//		// get unwrapped data
//		Object actualData = exit.receive();
//
//		if( !dataClass.isInstance(actualData) )
//			throw new MUSCLERuntimeException("datatype mismatch <"+dataClass.getName()+"> vs. <"+actualData.getClass().getName()+">");
//
//		// try to compare the data
//		if(actualData.getClass().isArray()) {
//			if( targetSize != DataTemplate.ANY_SIZE && targetSize != Array.getLength(actualData) )
//				throw new MUSCLERuntimeException("datasize mismatch for exit <"+exit.getLocalName()+"> <"+targetSize+"> vs. <"+Array.getLength(actualData)+">");
//		}
//	}
//
//
//	private boolean stop() {
//
//		int maxFineSteps = CxADescription.ONLY.getIntProperty(CxADescription.Key.MAX_TIMESTEPS);
//
//		return time > maxFineSteps;
//	}
//
//}
