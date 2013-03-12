/*
 * Copyright (c) 2012, 2013 Mateusz Parzonka
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mateusz Parzonka - initial API and implementation
 */
package prm4j.api.fsm;

import static java.util.Collections.unmodifiableSet;

import java.util.HashSet;
import java.util.Set;


import prm4j.api.Alphabet;
import prm4j.api.BaseEvent;
import prm4j.api.Parameter;
import prm4j.api.Symbol;
import prm4j.indexing.Monitor;
import prm4j.indexing.BaseMonitorState;
import prm4j.indexing.realtime.StatefulMonitor;
import prm4j.spec.FiniteSpec;

public class FSMSpec<L> implements FiniteSpec {

	protected Alphabet<L> alphabet; // Rahul
	protected Set<BaseEvent> baseEvents;
    protected Set<Parameter<?>> parameters;
    protected Set<BaseMonitorState> states;
    protected BaseMonitorState initialState;

    public FSMSpec(FSM<L> fsm) {
	baseEvents = unmodifiableSet(new HashSet<BaseEvent>(fsm.getAlphabet().getSymbols()));
	parameters = unmodifiableSet(fsm.getAlphabet().getParameters());
	states = unmodifiableSet(new HashSet<BaseMonitorState>(fsm.getStates()));
	initialState = fsm.getInitialState();
	alphabet = fsm.getAlphabet(); // Rahul
    }
    
    public FSMSpec(FSMSpec fs) {	// Rahul
    	//baseEvents = fs.getBaseEvents();
    	baseEvents = new HashSet<BaseEvent>();
    	parameters = fs.getFullParameterSet();
    	//states = fs.getStates();
    	states = new HashSet<BaseMonitorState>();
    	alphabet = new Alphabet();
    	for(Parameter<?> param: parameters){
    		alphabet.addCopiedParameter(param);
    	}
    }
    
    
    @Override
    public Set<BaseEvent> getBaseEvents() {
    return baseEvents;
    }

    @Override
    public Set<BaseMonitorState> getStates() {
	return states;
    }

    @Override
    public BaseMonitorState getInitialState() {
	return initialState;
    }

    @Override
    public Monitor getInitialMonitor() {
	return new StatefulMonitor(getInitialState());
    }

    @Override
    public Set<Parameter<?>> getFullParameterSet() {
	return parameters;
    }
    
	public Alphabet<L> getAlphabet() { // Rahul
		return alphabet;
	}
	
	public Symbol<L> getSymbolByLabel(L label) { // Rahul
		return alphabet.getSymbolByLabel(label);
	}

}
