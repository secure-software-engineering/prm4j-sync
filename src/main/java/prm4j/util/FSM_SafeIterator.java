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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import prm4j.api.Symbol2;
import prm4j.api.fsm.FSM;
import prm4j.api.fsm.FSMState;

@SuppressWarnings("rawtypes")
public class FSM_SafeIterator implements FSM_Base{

    public final Alphabet alphabet = new Alphabet();

    public final Parameter<Collection> c = alphabet.createParameter("c", Collection.class);
    public final Parameter<Iterator> i = alphabet.createParameter("i", Iterator.class);

    public final Symbol2<String, Collection, Iterator> createIter = alphabet.createSymbol2("create", c, i);
    public final Symbol1<String, Collection> updateColl = alphabet.createSymbol1("update", c);
    public final Symbol1<String, Iterator> useIter = alphabet.createSymbol1("next", i);

    public final FSM fsm = new FSM(alphabet);

    public final MatchHandler matchHandler = MatchHandler.NO_OP;

    public final FSMState initial = fsm.createInitialState();
    public final FSMState s1 = fsm.createState();
    public final FSMState s2 = fsm.createState();
    //public final FSMState trap = fsm.createState();
    public final FSMState error = fsm.createAcceptingState(matchHandler);
    
    
    public Map<String, List<Parameter<?>>> order = new HashMap<String, List<Parameter<?>>>();
    
    public final int totParams = 2;
    

    public FSM_SafeIterator(){
    	this(false);
    }

    public FSM_SafeIterator(boolean criticalSymbolApplication) {
	initial.addTransition(updateColl, initial);
	initial.addTransition(createIter, s1);
	s1.addTransition(useIter, s1);
	s1.addTransition(updateColl, s2);
	s2.addTransition(updateColl, s2);
	s2.addTransition(useIter, error);
	
	// newly added for completeness as required by synchro
	//initial.addTransition(useIter, trap);
	////
	
	if(criticalSymbolApplication)
		fsm.addCriticalSymbol(createIter);
	
	
    List<Parameter<?>> lc = new LinkedList<Parameter<?>>();
    lc.add(c);
    lc.add(i);
    order.put("create", lc);
    
    List<Parameter<?>> lu = new LinkedList<Parameter<?>>();
    lu.add(c);
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
