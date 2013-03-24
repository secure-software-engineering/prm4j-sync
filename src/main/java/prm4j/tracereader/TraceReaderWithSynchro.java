package prm4j.tracereader;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import prm4j.api.Event;
import prm4j.api.Parameter;
import prm4j.api.Symbol;
import prm4j.api.fsm.FSMSpec;
import prm4j.indexing.realtime.DefaultNodeStore;
import prm4j.indexing.realtime.DefaultParametricMonitor;
import prm4j.sync.AbstractSyncingSpec;
import prm4j.sync.FullSyncingSpec;
import prm4j.sync.MultisetSyncingSpec;
import prm4j.sync.NumberAndSymbolSetSyncingSpec;
import prm4j.sync.NumberSyncingSpec;
import prm4j.sync.SymbolSetSyncingSpec;
import prm4j.util.*;


public class TraceReaderWithSynchro {

	public static void main(String[] args) throws IOException {
		if(args.length!=6) {
			System.err.println("USAGE: <pathToTraceFile> (full|multiset|set) (fsi|...) (critical symbols: yes/no)");
		}
		
		String filePath = args[0];
		String abstName = args[1];
		String propName = args[2];
		boolean criticalSymbolApplication = false;
		if(args[3].equals("yes") || args[3].equals("Yes") || args[3].equals("YES"))
			criticalSymbolApplication = true;
		double samplingRate = Double.parseDouble(args[4]);
		int seed = Integer.parseInt(args[5]);
		
		FSM_Base fsm_base;
		
		AbstractSyncingSpec<String, ?> syncingSpec = null;
		if(propName.equals("hasnext")) {
			fsm_base = new FSM_HasNext(criticalSymbolApplication);
		} else if(propName.equals("fsi")) {
			fsm_base = new FSM_SafeIterator(criticalSymbolApplication);
		} else if(propName.equals("tokenizer")) {
			fsm_base = new FSM_StringTokenizer(criticalSymbolApplication);
		}else{
			throw new IllegalArgumentException("invalid monitor spec: "+propName);
		}

		if(abstName.contentEquals("set")) {		
			syncingSpec = new SymbolSetSyncingSpec(new FSMSpec<String>(fsm_base.getFSM(), samplingRate, seed));
		} else if(abstName.contentEquals("num")) {
			syncingSpec = new NumberSyncingSpec(new FSMSpec(fsm_base.getFSM(), samplingRate, seed));
		} else if(abstName.contentEquals("full")) {			
			syncingSpec = new FullSyncingSpec(new FSMSpec(fsm_base.getFSM(), samplingRate, seed));
		} else if(abstName.contentEquals("multiset")) {			
			syncingSpec = new MultisetSyncingSpec(new FSMSpec(fsm_base.getFSM(), samplingRate, seed));
		} else if(abstName.contentEquals("sym")) {			
			syncingSpec = new SymbolSetSyncingSpec(new FSMSpec(fsm_base.getFSM(), samplingRate, seed));
		} else if(abstName.contentEquals("numsym")) {			
			syncingSpec = new NumberAndSymbolSetSyncingSpec(new FSMSpec(fsm_base.getFSM(), samplingRate, seed));
		} else {
			throw new IllegalArgumentException("invalid abstraction: "+abstName);
		}
		
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
						
			Set<Parameter<?>> params = symbol.getParameters();
			Iterator<Parameter<?>> it = params.iterator();
			while(it.hasNext()){
				Parameter<?> param = it.next();				
				int trInd = fsm_base.getParameterOrder(symbol.getLabel()).indexOf(param);
				parameterValues[param.getIndex()] = Long.parseLong(split[trInd +1]);
				//System.out.println("Parameter " + param.toString() + " has " + param.getIndex() + " index " + "trInd: " + trInd);
			}
						
			Event e = new Event(symbol, parameterValues);
			
			/*System.out.print("\n" + ((Symbol<String>)e.getBaseEvent()).getLabel());
			it = params.iterator();
			Parameter<?> param1;
			Parameter<?> param2;

			
			if(it.hasNext()){
				param1 = it.next();
				System.out.print(" " + e.getBoundObject(param1.getIndex())); 
			}
			if(it.hasNext()){
				param2 = it.next();
				System.out.print(" " + e.getBoundObject(param2.getIndex())); 
			}
			
			System.out.println("");*/
			
			
			syncingSpec.maybeProcessEvent(e);
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
	

	
}