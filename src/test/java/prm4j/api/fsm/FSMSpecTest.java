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
package prm4j.api.fsm;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import prm4j.AbstractTest;
import prm4j.AbstractTest.FSM_unsafeMapIterator;
import prm4j.api.Symbol;
import prm4j.logic.MonitorState;
import prm4j.logic.StatefulSpec;
import prm4j.logic.StatefulSpecProcessor;

public class FSMSpecTest extends AbstractTest {

    @Test
    public void getPropertyEnableSet_unsafeMapIterator() throws Exception {
	FSM_unsafeMapIterator u = new FSM_unsafeMapIterator();
	StatefulSpec fsmSpec = new FSMSpec<Void>(u.fsm);
	Map<Symbol, Set<Set<Symbol>>> actual = fsmSpec.getPropertyEnableSets();

	Map<Symbol, Set<Set<Symbol>>> expected = new HashMap<Symbol, Set<Set<Symbol>>>();
	for (Symbol symbol : u.alphabet.getSymbols()) {
	    expected.put(symbol, new HashSet<Set<Symbol>>());
	}
	expected.get(u.createColl).add(Collections.<Symbol> emptySet());
	expected.get(u.createIter).add(asSet(u.createColl));
	expected.get(u.createIter).add(asSet(u.createColl, u.updateMap));
	expected.get(u.useIter).add(asSet(u.createColl, u.createIter));
	expected.get(u.useIter).add(asSet(u.createColl, u.createIter, u.updateMap));
	expected.get(u.updateMap).add(asSet(u.createColl));
	expected.get(u.updateMap).add(asSet(u.createColl, u.createIter));
	expected.get(u.updateMap).add(asSet(u.createColl, u.createIter, u.useIter));

	assertEquals(expected, actual);
    }

    @Test
    public void getCreationSymbols_unsafeMapIterator() {
	FSM_unsafeMapIterator u = new FSM_unsafeMapIterator();
	StatefulSpec fsmSpec = new FSMSpec<Void>(u.fsm);
	Set<Symbol> actual = fsmSpec.getCreationSymbols();

	Set<Symbol> expected = new HashSet<Symbol>();
	expected.add(u.createColl);
	expected.add(u.updateMap);
	expected.add(u.createIter);
	expected.add(u.useIter);

	assertEquals(expected, actual);
    }

    @Test
    public void getStatePropertyCoEnableSets_unsafeMapIterator() throws Exception {
	FSM_unsafeMapIterator u = new FSM_unsafeMapIterator();
	FSM<Void> fsm = u.fsm;
	StatefulSpec fsmSpec = new FSMSpec<Void>(fsm);

	Map<MonitorState<?>, Set<Set<Symbol>>> actual = fsmSpec.getStatePropertyCoEnableSets();

	Map<MonitorState<?>, Set<Set<Symbol>>> expected = new HashMap<MonitorState<?>, Set<Set<Symbol>>>();
	for (MonitorState<?> state : u.fsm.getStates()){
	    expected.put(state, new HashSet<Set<Symbol>>());
	}
	expected.get(u.initial).add(asSet(u.createColl, u.createIter, u.updateMap, u.useIter));
	expected.get(u.s1).add(asSet(u.createIter, u.useIter, u.updateMap));
	expected.get(u.s2).add(asSet(u.useIter, u.updateMap));
	expected.get(u.s3).add(asSet(u.useIter));
	expected.get(u.error).add(Collections.<Symbol> emptySet());

	assertEquals(expected, actual);
    }

}