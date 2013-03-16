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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import prm4j.api.Alphabet;
import prm4j.api.Parameter;
import prm4j.api.Symbol1;
import prm4j.api.Symbol2;
import prm4j.api.fsm.FSM;
import prm4j.api.fsm.FSMSpec;
import prm4j.api.fsm.FSMState;
import prm4j.indexing.realtime.AwareMatchHandler.AwareMatchHandler1;
import prm4j.spec.FiniteSpec;
import prm4j.sync.AbstractSyncingSpec.AbstractionAndSymbol;

public class DefaultParametricMonitor_a_ab_with_initial_ab_loop_Test extends AbstractDefaultParametricMonitorTest {

    public static class FSM_a_ab_with_initial_ab_loop {

	public final Alphabet<String> alphabet = new Alphabet();

	public final Parameter<String> p1 = alphabet.createParameter("p1", String.class);
	public final Parameter<String> p2 = alphabet.createParameter("p2", String.class);

	public final Symbol1<String,String> e1 = alphabet.createSymbol1("e1", p1);
	public final Symbol2<String,String, String> e2 = alphabet.createSymbol2("e2", p1, p2);

	public final AwareMatchHandler1<String> matchHandler = AwareMatchHandler.create(p1);

	public final FSM<String> fsm = new FSM<String>(alphabet);

	public final FSMState<String> initial = fsm.createInitialState();
	public final FSMState<String> s1 = fsm.createState();
	public final FSMState<String> error = fsm.createAcceptingState(matchHandler);

	public FSM_a_ab_with_initial_ab_loop() {
	    initial.addTransition(e1, s1);
	    initial.addTransition(e2, initial);
	    s1.addTransition(e2, error);
	}

    }

    FSM_a_ab_with_initial_ab_loop fsm;
    final String a1 = "a1";
    final String b1 = "b1";
    final String a2 = "a2";
    final String b2 = "b2";

    @Before
    public void init() {
	fsm = new FSM_a_ab_with_initial_ab_loop();
	FiniteSpec finiteSpec = new FSMSpec<String>(fsm.fsm);
	createDefaultParametricMonitorWithAwareComponents(finiteSpec);
    }

    @Test
    public void ab_a_model() throws Exception {
	// verify
	assertEquals(asSet(fsm.e1), fpp.getCreationEvents());

    }

    @Test
    public void a1_a1b1_match() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1));
	pm.processEvent(fsm.e2.createEvent(a1, b1));

	// verify
	assertEquals(1, fsm.matchHandler.getHandledMatches().size());
    }

    @Test
    public void a1_a1_a1b1_nomatch() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1));
	pm.processEvent(fsm.e1.createEvent(a1));
	pm.processEvent(fsm.e2.createEvent(a1, b1));

	// verify
	assertEquals(0, fsm.matchHandler.getHandledMatches().size());
    }

    @Test
    public void a1_a2_a1b1_match() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a1));
	pm.processEvent(fsm.e1.createEvent(a2));
	pm.processEvent(fsm.e2.createEvent(a1, b1));

	// verify
	assertEquals(1, fsm.matchHandler.getHandledMatches().size());
    }

    @Test
    public void a1b1_a1_a1b1_match() throws Exception {
	// exercise
	pm.processEvent(fsm.e2.createEvent(a1, b1));
	pm.processEvent(fsm.e1.createEvent(a1));
	pm.processEvent(fsm.e2.createEvent(a1, b1));

	// verify
	assertEquals(1, fsm.matchHandler.getHandledMatches().size());
    }

    @Test
    public void a2b1_a1_a1b1_match() throws Exception {
	// exercise
	pm.processEvent(fsm.e2.createEvent(a2, b1));
	pm.processEvent(fsm.e1.createEvent(a1));
	pm.processEvent(fsm.e2.createEvent(a1, b1));

	// verify
	assertEquals(1, fsm.matchHandler.getHandledMatches().size());
    }

    @Test
    public void a1b2_a1_a1b1_match() throws Exception {
	// exercise
	pm.processEvent(fsm.e2.createEvent(a1, b2));
	pm.processEvent(fsm.e1.createEvent(a1));
	pm.processEvent(fsm.e2.createEvent(a1, b1));

	// verify
	assertEquals(1, fsm.matchHandler.getHandledMatches().size());
    }

}
