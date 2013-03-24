/*
 * Copyright (c) 2012 Mateusz Parzonka, Eric Bodden
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Eric Bodden - initial API and implementation
 * Mateusz Parzonka - adapted API and implementation
 */
package prm4j.api.fsm;

import prm4j.api.Alphabet;
import prm4j.api.BaseEvent;
import prm4j.api.MatchHandler;
import prm4j.api.Symbol;
import prm4j.indexing.BaseMonitorState;
import prm4j.sync.AbstractSyncingSpec.AbstractionAndSymbol;

public class FSMState <L> extends BaseMonitorState {

    private FSMState<L>[] successorTable;
    private final boolean isAccepting;
    private boolean isFinal;
    private final String label;// for display purposes only
    private final Alphabet<L> alphabet;
    private final MatchHandler matchHandler;

    public FSMState(int index, Alphabet<L> alphabet, boolean isAccepting, MatchHandler matchHandler, String label) {
	super(index);
	//System.out.println("Creating state with index: " + index);
	this.isAccepting = isAccepting;
	this.isFinal = true; // a state is final if it has no successor
	this.label = label;
	this.alphabet = alphabet;
	this.matchHandler = matchHandler;
	//System.out.println("Alphabet size in successor table: " + alphabet.size());
	successorTable = new FSMState[alphabet.size()];
    }

    public void addTransition(Symbol<L> symbol, FSMState<L> successor) {
		//System.out.println("Entered addTransition of FSMState");
    	System.out.println("Adding Transition: from " + this.getIndex() + " using " + symbol.getIndex() + " to " + successor.getIndex());

	assert successorTable[symbol.getIndex()] == null : "successor already set";
	if (!alphabet.getSymbols().contains(symbol)) {
	    throw new IllegalArgumentException("Symbol for transition is not contained in alphabet!");
	}
	//System.out.println("Adding successor at " + symbol.getIndex());
	successorTable[symbol.getIndex()] = successor;
	isFinal = false;
    }

   public BaseMonitorState getSuccessor(BaseEvent baseEvent) {
    	//System.out.println("Entered FSMState's getSuccessor" + baseEvent.getIndex());    	
	return successorTable[baseEvent.getIndex()];
    }
    

    @Override
    public String toString() {
	return label;
    }

    @Override
    public boolean isAccepting() {
	return isAccepting;
    }

    @Override
    public MatchHandler getMatchHandler() {
	return matchHandler;
    }

    @Override
    public boolean isFinal() {
	return isFinal;
    }

    
}
