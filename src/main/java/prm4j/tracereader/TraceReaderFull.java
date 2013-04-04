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
import prm4j.indexing.realtime.StatefulMonitor;

import prm4j.sync.AbstractSyncingSpec;
import prm4j.util.*;

/*
 * This is a variant of the Hello World example in which events are read from a file.
 */
public class TraceReaderFull {


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
		
		long recordCounter = 0;
		System.err.println("Size of long: " + Long.SIZE);
		System.err.println("Max value of long: " + Long.MAX_VALUE);
		System.err.println("Min value of long: " + Long.MIN_VALUE);


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
				parameterValues[param.getIndex()] = Long.parseLong(split[trInd +1]);
			}
						
			Event e = new Event(symbol, parameterValues);
			
			parametricMonitor.processEvent(e);
		}
		long endTime = System.currentTimeMillis();
		
		System.out.println("Time taken: " + (endTime - startTime));
		System.out.println("Records processed: " + recordCounter);
		System.out.println("Errors captured: " + StatefulMonitor.countError);

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
	
}