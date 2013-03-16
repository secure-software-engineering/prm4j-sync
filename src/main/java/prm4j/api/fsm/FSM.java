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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import prm4j.api.Alphabet;
import prm4j.api.MatchHandler;
import prm4j.api.Symbol;

/**
 * A finite state automaton.
 */
public class FSM<L> {

    private final Alphabet<L> alphabet;
    private final Set<FSMState<L>> states;
    private final Set<String> usedNames;
    private int stateCount = 0;
    private FSMState<L> initialState;
    private Set<Symbol<L>> criticalSymbols;	// Rahul

    public FSM(Alphabet<L> alphabet) {
	this.alphabet = alphabet;
	states = new HashSet<FSMState<L>>();
	usedNames = new HashSet<String>();
	criticalSymbols = new HashSet<Symbol<L>>(); // Rahul
    }

    /**
     * Create a new state which is labeled with the name <code>"initial"</code>.
     *
     * @return the created state
     */
    public FSMState<L> createInitialState() {
	if (initialState != null)
	    throw new IllegalStateException("Initial state already created!");
	initialState = createState("initial");
	return initialState;
    }

    /**
     * Create a new state which is labeled with a generated name of the form <code>"state_NUMBER"</code>.
     *
     * @return the created state
     */
    public FSMState<L> createState() {
	return createState(generateStateName());
    }

    /**
     * Create a new state labeled with the given optional name.
     *
     * @return the created state
     */
    public FSMState<L> createState(String optionalName) {
	return createState(false, null, optionalName);
    }

    /**
     * Create a new accepting state which is labeled with a generated name of the form <code>"state_NUMBER"</code>.
     *
     * @return the created accepting state
     */
    public FSMState<L> createAcceptingState(MatchHandler matchHandler) {
	return createAcceptingState(matchHandler, generateAcceptingStateName());
    }

    /**
     * Create a new accepting state labeled with the given optional name.
     *
     * @return the created accepting state
     */
    public FSMState<L> createAcceptingState(MatchHandler matchHandler, String optionalName) {
	if (matchHandler == null) {
	    throw new NullPointerException("MatchHandler may not be null!");
	}
	return createState(true, matchHandler, optionalName);
    }

    private String generateStateName() {
	return "state " + stateCount;
    }

    private String generateAcceptingStateName() {
	return "state " + stateCount + " (accepting)";
    }

    private FSMState<L> createState(boolean isAccepting, MatchHandler eventHandler, String name) {
	if (usedNames.contains(name))
	    throw new IllegalArgumentException("The name [" + name + "] has already been used!");
	usedNames.add(name);
	FSMState<L> state = new FSMState<L>(stateCount++, alphabet, isAccepting, eventHandler, name);
	states.add(state);
	return state;
    }

    /**
     * Returns the underlying alphabet for this FSM.
     *
     * @return the alphabet
     */
    public Alphabet<L> getAlphabet() {
	return alphabet;
    }

    /**
     * Returns all created states.
     *
     * @return an unmodifiable set of created states
     */
    public Set<FSMState<L>> getStates() {
	return states;
    }

    /**
     * Returns the number of created states.
     *
     * @return the number of created states
     */
    public int getStateCount() {
	return stateCount;
    }

    /**
     * Returns the names which where used for the states.
     *
     * @return an unmodifiable set of the used names
     */
    public Set<String> getUsedNames() {
	return Collections.unmodifiableSet(usedNames);
    }

    /**
     * Returns the initial state.
     *
     * @return the initial state
     */
    public FSMState<L> getInitialState() {
	if (initialState == null)
	    throw new IllegalStateException("No initial state created!");
	return initialState;
    }
    
    /**
     * Returns the set of critical symbols.
     *
     * @return the set of critical symbols
     */
	public Set<Symbol<L>> getCriticalSymbols(){	// Rahul
		return criticalSymbols;
	}
	
    /**
     * Adds a new symbol to the critical symbol set.
     *
     * @return void
     */
    public void addCriticalSymbol(Symbol<L> sym) {
    	criticalSymbols.add(sym);
    }


}
