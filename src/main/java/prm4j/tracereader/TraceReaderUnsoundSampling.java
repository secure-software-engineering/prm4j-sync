package prm4j.tracereader;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import prm4j.api.Event;
import prm4j.api.Parameter;
import prm4j.api.ParametricMonitor;
import prm4j.api.ParametricMonitorFactory;
import prm4j.api.Symbol;
import prm4j.api.fsm.FSMSpec;
import prm4j.indexing.realtime.DefaultNodeStore;
import prm4j.indexing.realtime.DefaultParametricMonitor;
import prm4j.indexing.realtime.StatefulMonitor;

import prm4j.util.*;

/*
 * This is a variant of the Hello World example in which events are read from a file.
 */
public class TraceReaderUnsoundSampling {
	//static int phase;
	//static boolean processEventsInCurrentPeriod;
	//static int samplingPeriod;
	//static int skipPeriod;
	//static Random random = new Random();
	


	public static void main(String[] args) throws IOException {
		if(args.length < 6) {
			System.err.println("USAGE: <pathToTraceFile> (full|multiset|set) (fsi|...) (critical symbols: yes/no) samplingThreshold seed converge/noIter cpuIdleTimeFile");
		}
		
		String filePath = args[0];
		String abstName = args[1];
		String propName = args[2];
		boolean criticalSymbolApplication = false;
		if(args[3].equals("yes") || args[3].equals("Yes") || args[3].equals("YES"))
			criticalSymbolApplication = true;
		double samplingThreshold = Double.parseDouble(args[4]);

		Random random = new Random();
		int seed = random.nextInt();
		if(args.length >= 6){
			seed = Integer.parseInt(args[5]);
			random = new Random(seed);
		}
		
		int noIter = 1;
		boolean converge = false;
		String cpuIdleFile = null;

		if(args.length > 6){
			if(args[6].equals("converge") || args[6].equals("CONVERGE")){
				converge = true;
				noIter = 20;
			} else{
				noIter = Integer.parseInt(args[6]);
			}
			cpuIdleFile = args[7];
		}
		

		
		FSM_Base fsm_base;
		
		
		if(propName.equals("hasnext")) {
			fsm_base = new FSM_HasNext();
		} else if(propName.equals("fsi")) {
			fsm_base = new FSM_SafeIterator();
		} else if(propName.equals("fmi")) {
			fsm_base = new FSM_SafeMapIterator(criticalSymbolApplication);
		} else if(propName.equals("tokenizer")) {
			fsm_base = new FSM_StringTokenizer(criticalSymbolApplication);
		} else if(propName.equals("fsc")) {
			fsm_base = new FSM_SafeSyncCollection(criticalSymbolApplication);
		} else if(propName.equals("fsm")) {
			fsm_base = new FSM_SafeSyncMap(criticalSymbolApplication);
		} else{
			throw new IllegalArgumentException("invalid monitor spec: "+propName);
		}
		
		FSMSpec fsmspec = new FSMSpec(fsm_base.getFSM());
		
		Set<String> symbols = new HashSet<String>();		
		for (Symbol<String> sym: (Set<Symbol<String>>)fsm_base.getAlphabet().getSymbols()) {
			symbols.add(sym.getLabel());
		}
		
		int totalErrorsCaptured = 0;
		int totalIterationsPerformed = 0;
		ArrayList<Double> executionTimes = new ArrayList<Double>();
		
		/*samplingPeriod = fsmspec.getStates().size()+4;
		skipPeriod = (int) ((1.0d/samplingThreshold - 1) * samplingPeriod); */
		
		////////////////////////////////////////////////////////////////////////////
		///// Reading CPU load figures from the file and then build a circular list
		////////////////////////////////////////////////////////////////////////////
		
		BufferedReader cpuIdleReader = new BufferedReader(new InputStreamReader(new FileInputStream(cpuIdleFile)));
		
		String line;
		int cpuIdleEvents = 0;
		
		while((line=cpuIdleReader.readLine())!=null) {
			//skip non-symbol lines
			cpuIdleEvents++;

		}
				
		double[] cit = new double[cpuIdleEvents];
		cpuIdleReader = new BufferedReader(new InputStreamReader(new FileInputStream(cpuIdleFile)));
		int index = 0;
		while((line=cpuIdleReader.readLine())!=null) {
			//skip non-symbol lines
			cit[index++] = Double.parseDouble(line);
		}		
		////////////////////////////////////////////////////////////////////////////
		

		
		for(int i=0; i < noIter; i++){
			
			System.out.println("Starting " + (i+1) + "th iteration:");
			
			ParametricMonitor parametricMonitor = ParametricMonitorFactory.createParametricMonitor(fsmspec);

			long recordCounter = 0;
			int offset = random.nextInt(cpuIdleEvents);
		
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));		
			//String line;
			System.out.println("file: "+filePath);
			
			double startTime = (double)System.currentTimeMillis();
			
			while((line=reader.readLine())!=null) {
				//skip non-symbol lines
				boolean foundSym = false;
				for(String sym: symbols) {
					if(line.startsWith(sym+" ")) {
						foundSym = true;
						break;
					}
				}
				if(!foundSym) continue;
				
				double cpuIdle = cit[(int)((offset+recordCounter)%cpuIdleEvents)];
				
				recordCounter++;

				String[] split = line.split(" ");
				
				String symbolName = split[0];
				Symbol<String> symbol = fsm_base.getAlphabet().getSymbolByLabel(symbolName);
	
				Object[] parameterValues = new Object[fsm_base.getTotalParams()];
								
				Set<Parameter<?>> params = symbol.getParameters();
				Iterator<Parameter<?>> it = params.iterator();
				while(it.hasNext()){
					Parameter<?> param = it.next();				
					int trInd = fsm_base.getParameterOrder(symbol.getLabel()).indexOf(param);
					parameterValues[param.getIndex()] = split[trInd + 1].intern();
				}
							
				Event e = new Event(symbol, parameterValues);
				
				if(shouldMonitor(samplingThreshold, cpuIdle))
					parametricMonitor.processEvent(e);
			}
			long endTime = System.currentTimeMillis();
			
			parametricMonitor.reset();
			
			System.gc();
			
			totalIterationsPerformed = i + 1;
			System.out.println("Time taken by " + totalIterationsPerformed + "th Iteration: " + (endTime - startTime));
			System.out.println("Records processed: " + recordCounter);
			System.out.println("Errors captured: " + StatefulMonitor.countError);
			
			recordCounter = 0;
			totalErrorsCaptured += StatefulMonitor.countError;
			StatefulMonitor.countError = 0;
			executionTimes.add(endTime - startTime);
			if(converge && isStable(executionTimes)){
				System.out.println("Converged after " + totalIterationsPerformed + "th Iteration: " + (endTime - startTime));
				break;
			}
			
			System.gc();
		}
		
		System.out.println("Total Errors captured: " + totalErrorsCaptured);
		System.out.println("Total iterations: " + totalIterationsPerformed);
		System.out.println("Errors per iteration: " + ((double)totalErrorsCaptured)/totalIterationsPerformed);

	}
	
	protected static Parameter<?> getParam(Set<Parameter<?>> params, int index){
		Iterator<Parameter<?>> it = params.iterator();
		while(it.hasNext()){
			Parameter<?> param = it.next();
			if(param.getIndex() == index)
				return param;
		}
		return null;
	}
	
	/*protected static boolean shouldMonitor(){	
		if(phase==0) {
			processEventsInCurrentPeriod = random.nextBoolean();
		}
		int periodLength = processEventsInCurrentPeriod ? samplingPeriod : skipPeriod;
		phase = (phase+1) % periodLength;
		return processEventsInCurrentPeriod;
	}*/
	
	protected static boolean shouldMonitor(double samplingThreshold, double cpuIdle){	
		return (cpuIdle > samplingThreshold);
	}
	
	protected static boolean isStable(List<Double> times){
		if(times.size() < 3)
			return false;
		else {
			if((((times.get(times.size()-1) - times.get(times.size()-2)) < 
					(0.03 * times.get(times.size()-1))) && 
					(times.get(times.size()-2) - times.get(times.size()-1)) < 
					(0.03 * times.get(times.size()-1))) && 
					(((times.get(times.size()-2) - times.get(times.size()-3)) < 
							(0.03 * times.get(times.size()-2))) &&
							((times.get(times.size()-3) - times.get(times.size()-2))) < 
							(0.03 * times.get(times.size()-2)))){
				return true;
			}
					
		}
		return false;
	}
	
}