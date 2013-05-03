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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import prm4j.api.Alphabet;
import prm4j.api.MatchHandler;
import prm4j.api.Parameter;
import prm4j.api.Symbol1;
import prm4j.api.Symbol2;
import prm4j.api.fsm.FSM;
import prm4j.api.fsm.FSMState;

@SuppressWarnings("rawtypes")
public class FSM_SafeSyncCollection implements FSM_Base{

    public final Alphabet alphabet = new Alphabet();

    public final Parameter<Long> c = alphabet.createParameter("c", Long.class);
    public final Parameter<Long> i = alphabet.createParameter("i", Long.class);

    public final Symbol1<String, Long> sync = alphabet.createSymbol1("sync", c);
    public final Symbol2<String, Long, Iterator> asyncCreateIter = alphabet.createSymbol2("asyncCreateIter", c, i);
    public final Symbol2<String, Long, Iterator> syncCreateIter = alphabet.createSymbol2("syncCreateIter", c, i);
    public final Symbol1<String, Long> next = alphabet.createSymbol1("next", i);

    public final FSM fsm = new FSM(alphabet);

    public final  MatchHandler matchHandler = MatchHandler.NO_OP;
    
	public Map<String, List<Parameter<?>>> order = new HashMap<String, List<Parameter<?>>>();
    
	public final int totParams = 2;

    public final FSMState initial = fsm.createInitialState();
    public final FSMState s1 = fsm.createState();
    public final FSMState s2 = fsm.createState();
    public final FSMState error = fsm.createAcceptingState(matchHandler);
    
    public FSM_SafeSyncCollection() {
    	this(false);
    }
    	
    public FSM_SafeSyncCollection(boolean criticalSymbolApplication) {

	initial.addTransition(sync, s1);
	s1.addTransition(asyncCreateIter, error);
	s1.addTransition(syncCreateIter, s2);
	s2.addTransition(next, error);
    
	if(criticalSymbolApplication){
		fsm.addCriticalSymbol(sync);
		fsm.addCriticalSymbol(asyncCreateIter);
		fsm.addCriticalSymbol(syncCreateIter);
	}
	
	
    List<Parameter<?>> lu = new LinkedList<Parameter<?>>();
    lu.add(c);
    order.put("sync", lu);
    
    List<Parameter<?>> laci = new LinkedList<Parameter<?>>();
    laci.add(c);
    laci.add(i);
    order.put("asyncCreateIter", laci);
    
    List<Parameter<?>> lsci = new LinkedList<Parameter<?>>();
    lsci.add(c);
    lsci.add(i);
    order.put("syncCreateIter", lsci);
    

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
