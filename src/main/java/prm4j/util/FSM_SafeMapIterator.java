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
public class FSM_SafeMapIterator implements FSM_Base{

	public final Alphabet alphabet = new Alphabet();

	public final Parameter<Long> m = alphabet.createParameter("m", Long.class);
	public final Parameter<Long> c = alphabet.createParameter("c", Long.class);
	public final Parameter<Long> i = alphabet.createParameter("i", Long.class);

	public final Symbol2<String, Long, Long> createColl = alphabet.createSymbol2("createColl", m, c);
	public final Symbol2<String, Long, Long> createIter = alphabet.createSymbol2("createIter", c, i);
	public final Symbol1<String, Long> updateMap = alphabet.createSymbol1("update", m);
	public final Symbol1<String, Long> next = alphabet.createSymbol1("next", i);
	

	public final FSM fsm = new FSM(alphabet);

	public final  MatchHandler matchHandler = MatchHandler.NO_OP;
	
	public Map<String, List<Parameter<?>>> order = new HashMap<String, List<Parameter<?>>>();
	    
	public final int totParams = 3;

	public final FSMState initial = fsm.createInitialState();
	public final FSMState s1 = fsm.createState();
	public final FSMState s2 = fsm.createState();
	public final FSMState s3 = fsm.createState();
	public final FSMState error = fsm.createAcceptingState(matchHandler);

	public FSM_SafeMapIterator() {
		this(false);
    }
	
	public FSM_SafeMapIterator(boolean criticalSymbolApplication) {
	
	    initial.addTransition(createColl, s1);
	    initial.addTransition(updateMap, initial);
	    s1.addTransition(updateMap, s1);
	    s1.addTransition(createIter, s2);
	    s2.addTransition(next, s2);
	    s2.addTransition(updateMap, s3);
	    s3.addTransition(updateMap, s3);
	    s3.addTransition(next, error);
	    
		if(criticalSymbolApplication){
			fsm.addCriticalSymbol(createColl);
			fsm.addCriticalSymbol(createIter);

		}
		
		
	    List<Parameter<?>> lcc = new LinkedList<Parameter<?>>();
	    lcc.add(m);
	    lcc.add(c);
	    order.put("createColl", lcc);
	    
	    List<Parameter<?>> lci = new LinkedList<Parameter<?>>();
	    lcc.add(c);
	    lcc.add(i);
	    order.put("createIter", lci);
	    
	    List<Parameter<?>> lu = new LinkedList<Parameter<?>>();
	    lu.add(m);
	    order.put("update", lu);
	    
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
