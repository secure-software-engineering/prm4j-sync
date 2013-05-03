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
public class FSM_SafeSyncMap implements FSM_Base{

    public final Alphabet alphabet = new Alphabet();

    public final Parameter<Long> m = alphabet.createParameter("m", Long.class);
    public final Parameter<Long> c = alphabet.createParameter("c", Long.class);
    public final Parameter<Long> i = alphabet.createParameter("i", Long.class);

    public final Symbol1<String, Long> sync = alphabet.createSymbol1("sync", m);
    public final Symbol2<String, Long, Long> createSet = alphabet.createSymbol2("createSet", m, c);
    public final Symbol2<String, Long, Long> asyncCreateIter = alphabet.createSymbol2("asyncCreateIter", c, i);
    public final Symbol2<String, Long, Long> syncCreateIter = alphabet.createSymbol2("syncCreateIter", c, i);
    public final Symbol1<String, Long> next = alphabet.createSymbol1("next", i);

    public final FSM fsm = new FSM(alphabet);

    public final  MatchHandler matchHandler = MatchHandler.NO_OP;

    public final FSMState initial = fsm.createInitialState();
    public final FSMState s1 = fsm.createState();
    public final FSMState s2 = fsm.createState();
    public final FSMState s3 = fsm.createState();
    public final FSMState error = fsm.createAcceptingState(matchHandler);
    
	public Map<String, List<Parameter<?>>> order = new HashMap<String, List<Parameter<?>>>();
    
	public final int totParams = 3;

    public FSM_SafeSyncMap() {
    	this(false);
    }
    	
    public FSM_SafeSyncMap(boolean criticalSymbolApplication) {
	initial.addTransition(sync, s1);
	s1.addTransition(createSet, s2);
	s2.addTransition(asyncCreateIter, error);
	s2.addTransition(syncCreateIter, s2);
	s3.addTransition(next, error);
	
	if(criticalSymbolApplication){
		fsm.addCriticalSymbol(sync);
		fsm.addCriticalSymbol(createSet);
		fsm.addCriticalSymbol(asyncCreateIter);
		fsm.addCriticalSymbol(syncCreateIter);
	}

	
    List<Parameter<?>> ls = new LinkedList<Parameter<?>>();
    ls.add(m);
    order.put("sync", ls);
    
    List<Parameter<?>> lcs = new LinkedList<Parameter<?>>();
    lcs.add(m);
    lcs.add(c);
    order.put("createSet", lcs);
    
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
