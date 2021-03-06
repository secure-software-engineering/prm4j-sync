/*
 * Copyright (c) 2012 Mateusz Parzonka, Eric Bodden
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mateusz Parzonka - initial API and implementation
 */
package prm4j.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import prm4j.api.Alphabet;
import prm4j.api.MatchHandler;
import prm4j.api.Parameter;
import prm4j.api.Symbol;
import prm4j.api.Symbol1;
import prm4j.api.fsm.FSM;
import prm4j.api.fsm.FSMState;

@SuppressWarnings("rawtypes")
public class FSM_HasNext implements FSM_Base{

	public final Alphabet alphabet = new Alphabet();

	public final Parameter<Long> i = alphabet.createParameter("i", Long.class);

	public final Symbol1<String, Long> hasNext = alphabet.createSymbol1("hasNext", i);
	public final Symbol1<String, Long> next = alphabet.createSymbol1("next", i);

	public final FSM fsm = new FSM(alphabet);

	//public final  MatchHandler matchHandler = MatchHandler.NO_OP;
	public final  MatchHandler matchHandler = MatchHandler.SYS_OUT;


	public final FSMState initial = fsm.createInitialState();
	public final FSMState safe = fsm.createState();
	public final FSMState error = fsm.createAcceptingState(matchHandler);
	
	public final int totParams = 1;
	
	
    public Map<String, List<Parameter<?>>> order = new HashMap<String, List<Parameter<?>>>();
    
    public FSM_HasNext()  {
    	this(false);
    }

	public FSM_HasNext(boolean criticalSymbolApplication) {
	    initial.addTransition(hasNext, safe);
	    initial.addTransition(next, error);
	    safe.addTransition(hasNext, safe);
	    safe.addTransition(next, initial);
	    error.addTransition(next, error);
	    error.addTransition(hasNext, safe);
	    
   
	    List<Parameter<?>> lh = new LinkedList<Parameter<?>>();
	    lh.add(i);
	    order.put("hasNext", lh);
	    
	    List<Parameter<?>> ln = new LinkedList<Parameter<?>>();
	    ln.add(i);
	    order.put("next", ln);	    
	
	}
	
	public FSM getFSM(){
		return fsm;
	}
	
	public Alphabet getAlphabet(){
		return alphabet;
	}
	
	public List<Parameter<?>> getParameterOrder(String label){
		return order.get(label);
	}
	
	public int getTotalParams(){
		return totParams;
	}
	
}
