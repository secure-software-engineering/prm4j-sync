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
package prm4j.indexing.realtime;

import prm4j.Globals;
import prm4j.api.BaseEvent;
import prm4j.api.Event;
import prm4j.api.MatchHandler;
import prm4j.api.Symbol;
import prm4j.api.fsm.FSMState;
import prm4j.indexing.BaseMonitorState;
import prm4j.indexing.Monitor;
import prm4j.sync.AbstractSyncingSpec.AbstractionAndSymbol;
import prm4j.sync.AbstractSyncingSpec.SyncFSMMonitor;
import prm4j.sync.AbstractSyncingSpec.SyncState;

/**
 * A base monitor holding a {@link BaseMonitorState} which is updated when processing {@link BaseEvent}s.
 */
public class StatefulMonitor extends BaseMonitor {

    protected BaseMonitorState state;

    public StatefulMonitor(BaseMonitorState state) {
	this.state = state;
    }

    @Override
    public boolean processEvent(Event event) {
    //System.out.println("Type of this monitor is " + this.getClass().getName());
	if (state == null) {
	    terminate();
	    return false;
	}
	final BaseEvent baseEvent = event.getEvaluatedBaseEvent(this);
	//final Symbol baseEvent = (Symbol)event.getEvaluatedBaseEvent(this);	//Rahul
	if (baseEvent == null) {
	    // the condition evaluated to false, no transition is taken, monitor was alive => stays alive
	    return true;
	}
	BaseMonitorState oldState = state;
	System.out.println("Transition from state: " + state.getIndex());
	///////////
	if(this instanceof SyncFSMMonitor){	// Rahul changed this part
		Symbol<AbstractionAndSymbol> sym = (Symbol<AbstractionAndSymbol>)baseEvent;
		SyncState syncState = (SyncState)state;
		state = syncState.getSuccessor(sym);
	} else {
	state = state.getSuccessor(baseEvent);
	}
	//////////
	System.out.println("to state: " + state.getIndex());
	if (state == null) {
	    terminate();
	    return false;
	}
	MatchHandler matchHandler = state.getMatchHandler();
	if (matchHandler != null) {
	    matchHandler.handleAndCountMatch(getBindings(), event.getAuxiliaryData());
	    // when a state is a accepting state, it is still possible we will reach another accepting state (or loop on
	    // an accepting state)
	}
	if(state.isAccepting() && !oldState.isAccepting()){
		System.out.println("Moved to the error state!");
	}
	if (state.isFinal()
		|| (Globals.CHECK_MONITOR_VALIDITY_ON_EACH_UPDATE && !getMetaNode().isAcceptingStateReachable(
			getLowLevelBindings()))) {
	    terminate();
	    return false;
	}
	return true;
    }

    @Override
    public Monitor copy() {
	return new StatefulMonitor(state);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The {@link StatefulMonitor} returns <code>true</code> if each of the following is true:
     * <ol>
     * <li>The monitor is not terminated.</li>
     * <li>Its state is not dead or a final state (a state where only dead states may be reached).</li>
     * <li>A subset of its bindings is alive that is necessary to reach an accepting state.</li>
     * </ol>
     */
    @Override
    public boolean isAcceptingStateReachable() {
	return !isTerminated() && state != null && !state.isFinal()
		&& getMetaNode().isAcceptingStateReachable(getLowLevelBindings());
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((state == null) ? 0 : state.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	StatefulMonitor other = (StatefulMonitor) obj;
	if (state == null) {
	    if (other.state != null) {
		return false;
	    }
	} else if (!state.equals(other.state)) {
	    return false;
	}
	return true;
    }

    @Override
    public boolean isDead() {
	return false;
    }

}