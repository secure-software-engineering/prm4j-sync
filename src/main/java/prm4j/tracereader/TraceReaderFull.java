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

import prm4j.sync.AbstractSyncingSpec;
import prm4j.util.*;

/*
 * This is a variant of the Hello World example in which events are read from a file.
 */
public class TraceReaderFull {


	public static void main(String[] args) throws IOException {
		if(args.length < 2) {
			System.err.println("USAGE: <pathToTraceFile> (fsi|...) converge/noIter");
		}
		
		String filePath = args[0];
		String propName = args[1];
		boolean converge = false;
		int noIter = 1;
		if(args.length > 2){
			if(args[2].equals("converge") || args[2].equals("CONVERGE")){
				converge = true;
				noIter = 20;
			} else{
				noIter = Integer.parseInt(args[2]);
			}
		}
		
		FSM_Base fsm_base;
		
		
		if(propName.equals("hasnext")) {
			fsm_base = new FSM_HasNext();
		} else if(propName.equals("fsi")) {
			fsm_base = new FSM_SafeIterator();
		} else if(propName.equals("tokenizer")) {
			fsm_base = new FSM_StringTokenizer();
		} else if(propName.equals("fmi")) {
				fsm_base = new FSM_SafeMapIterator();
		} else if(propName.equals("fsc")) {
				fsm_base = new FSM_SafeSyncCollection();
		} else if(propName.equals("fsm")) {
				fsm_base = new FSM_SafeSyncMap();
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
		
		for(int i=0; i < noIter; i++){
			System.out.println("Starting " + (i+1) + "th iteration:");
			ParametricMonitor parametricMonitor = ParametricMonitorFactory.createParametricMonitor(fsmspec);
	
			long recordCounter = 0;
	
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));		
			String line;
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
				
				parametricMonitor.processEvent(e);
			}
			double endTime = (double)System.currentTimeMillis();
			
			System.gc();
			parametricMonitor.reset();
			
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