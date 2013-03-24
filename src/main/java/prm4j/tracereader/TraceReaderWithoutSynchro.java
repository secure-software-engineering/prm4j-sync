package prm4j.tracereader;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
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

import prm4j.util.*;

/*
 * This is a variant of the Hello World example in which events are read from a file.
 */
public class TraceReaderWithoutSynchro {
	static Random random = new Random(20);
	static int phase = 0;
	static boolean processEventsInCurrentPeriod;
	static double samplingRate = 0.2d;
	static int samplingPeriod = 10;
	static int skipPeriod = (int) ((1.0d/samplingRate - 1) * samplingPeriod);

	public static void main(String[] args) throws IOException {
		if(args.length!=2) {
			System.err.println("USAGE: <pathToTraceFile> (fsi|...)");
		}
		
		String filePath = args[0];
		String propName = args[1];
		
		FSM_Base fsm_base;
		
		
		if(propName.equals("hasnext")) {
			fsm_base = new FSM_HasNext();
		} else if(propName.equals("fsi")) {
			fsm_base = new FSM_SafeIterator();
		} else{
			throw new IllegalArgumentException("invalid monitor spec: "+propName);
		}
		
		FSMSpec fsmspec = new FSMSpec(fsm_base.getFSM());
		ParametricMonitor parametricMonitor = ParametricMonitorFactory.createParametricMonitor(fsmspec);

		
		Set<String> symbols = new HashSet<String>();		
		for (Symbol<String> sym: (Set<Symbol<String>>)fsm_base.getAlphabet().getSymbols()) {
			symbols.add(sym.getLabel());
		}
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));		
		String line;
		System.out.println("file: "+filePath);
		long startTime = System.currentTimeMillis();
		
		
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
			
			
			String[] split = line.split(" ");
			
			String symbolName = split[0];
			Symbol<String> symbol = fsm_base.getAlphabet().getSymbolByLabel(symbolName);

			Object[] parameterValues = new Object[fsm_base.getTotalParams()];
			
			//System.out.println("Total params: " + fsm_base.getTotalParams());
			
			Set<Parameter<?>> params = symbol.getParameters();
			Iterator<Parameter<?>> it = params.iterator();
			while(it.hasNext()){
				Parameter<?> param = it.next();				
				int trInd = fsm_base.getParameterOrder(symbol.getLabel()).indexOf(param);
				parameterValues[param.getIndex()] = Long.parseLong(split[trInd +1]);
				//System.out.println("Parameter " + param.toString() + " has " + param.getIndex() + " index " + "trInd: " + trInd);
			}
						
			//System.out.println("Creating Event: " + symbol.getLabel() + " with " + parameterValues[0] + " " + parameterValues[parameterValues.length - 1]);
			Event e = new Event(symbol, parameterValues);
			//System.out.println("\nProcessing event " + ((Symbol<String>)e.getBaseEvent()).getLabel());						
			
			if(shouldMonitor())
				parametricMonitor.processEvent(e);
		}
		long endTime = System.currentTimeMillis();
		
		System.out.println("Time taken: " + (endTime - startTime));
		System.out.println("counter: " + DefaultParametricMonitor.counter);

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
	
	protected static boolean shouldMonitor(){	
		if(phase==0) {
			processEventsInCurrentPeriod = random.nextBoolean();
		}
		int periodLength = processEventsInCurrentPeriod ? samplingPeriod : skipPeriod;
		phase = (phase+1) % periodLength;
		return processEventsInCurrentPeriod;
	}
	
}