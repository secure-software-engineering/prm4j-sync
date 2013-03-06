package prm4j.tracereader;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//import org.junit.Before;

import prm4j.api.BaseEvent;
import prm4j.api.Event;
import prm4j.api.Parameter;
import prm4j.api.Symbol;
import prm4j.api.fsm.FSM;
import prm4j.api.fsm.FSMSpec;
import prm4j.spec.FiniteParametricProperty;
import prm4j.sync.AbstractSyncingSpec;
import prm4j.sync.FullSyncingSpec;
import prm4j.sync.MultisetSyncingSpec;
import prm4j.sync.NumberAndSymbolSetSyncingSpec;
import prm4j.sync.NumberSyncingSpec;
import prm4j.sync.SymbolSetSyncingSpec;
//import prm4j.util.FSMDefinitions;
import prm4j.util.*;
//import prm4j.util.FSMDefinitions.FSM_HasNext;

import com.google.common.collect.Multiset;


/*
 * This is a variant of the Hello World example in which events are read from a file.
 */
public class TraceReader {

	public static void main(String[] args) throws IOException {
		if(args.length!=3) {
			System.err.println("USAGE: <pathToTraceFile> (full|multiset|set) (fsi|...)");
		}
		
		String filePath = args[0];
		String abstName = args[1];
		String propName = args[2];
		
		FSM_Base fsm_base;
		FSM_HasNext fsm_hn;
		FSM_SafeIterator fsm_fsi;
	    FiniteParametricProperty fpp;
	    FSM<String> fsm;
	   
		
		AbstractSyncingSpec<String, ?> syncingSpec = null;
		if(propName.equals("hasnext")) {
			fsm_base = new FSM_HasNext();
		} else if(propName.equals("fsi")) {
			fsm_base = new FSM_SafeIterator();
		} else{
			throw new IllegalArgumentException("invalid monitor spec: "+propName);
		}

		if(abstName.contentEquals("set")) {		
			syncingSpec = new SymbolSetSyncingSpec(new FSMSpec<String>(fsm_base.getFSM()));
			/*protected boolean shouldMonitor(ISymbol<String, String> symbol, IVariableBinding<String, Integer> binding,
						Multiset<ISymbol<String, String>> skippedSymbols) {
				return TraceReader.shouldMonitor();
			};*/
		} else if(abstName.contentEquals("num")) {
			syncingSpec = new NumberSyncingSpec(new FSMSpec(fsm_base.getFSM()));
		} else if(abstName.contentEquals("full")) {			
			syncingSpec = new FullSyncingSpec(new FSMSpec(fsm_base.getFSM()));
		} else if(abstName.contentEquals("multiset")) {			
			syncingSpec = new MultisetSyncingSpec(new FSMSpec(fsm_base.getFSM()));
		} else if(abstName.contentEquals("sym")) {			
			syncingSpec = new SymbolSetSyncingSpec(new FSMSpec(fsm_base.getFSM()));
		} else if(abstName.contentEquals("numsym")) {			
			syncingSpec = new NumberAndSymbolSetSyncingSpec(new FSMSpec(fsm_base.getFSM()));
		}else {
			throw new IllegalArgumentException("invalid abstraction: "+abstName);
		}
		
		fpp = new FiniteParametricProperty(syncingSpec);

		Set<String> symbols = new HashSet<String>();		
		for (Symbol<String> sym: (Set<Symbol<String>>)fsm_base.getAlphabet().getSymbols()) {
			symbols.add(sym.getLabel());
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));		
		String line;
		System.out.println("file: "+filePath);
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
			//
			
			String[] split = line.split(" ");
			
			String symbolName = split[0];
			Symbol<String> symbol = fsm_base.getAlphabet().getSymbolByLabel(symbolName);
			//String[] variableOrder = symbol.getVariables();

			Object[] parameterValues = new Object[split.length];
			Event e = new Event(symbol, parameterValues);
			
			Set<Parameter<?>> params = symbol.getParameters();
			
			for(int i=0; i<split.length-1; i++) {
				Long objectID = Long.parseLong(split[i+1]);
				System.out.println("objectID: " + objectID);
				Parameter<Long> param = getParam(params, i);
				System.out.println("param: " + param);
				symbol.bindObject(param, objectID, e.getBoundObjects());
			}
			
			syncingSpec.maybeProcessEvent(e);
		}
	
	}
	
	protected static Parameter getParam(Set<Parameter<?>> params, int index){
		Iterator<Parameter<?>> it = params.iterator();
		while(it.hasNext()){
			Parameter<?> param = it.next();
			if(param.getIndex() == index)
				return param;
		}
		return null;
	}
	
	/*static boolean shouldMonitor() {
		return true;
	}*/
	
	
}