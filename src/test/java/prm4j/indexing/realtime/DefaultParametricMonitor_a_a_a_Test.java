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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import prm4j.api.fsm.FSMSpec;
import prm4j.spec.FiniteSpec;

public class DefaultParametricMonitor_a_a_a_Test extends AbstractDefaultParametricMonitorTest {

    FSM_a_a_a fsm;
    final String a = "a";
    final String b = "b";
    final String c = "c";

    @Before
    public void init() {
	fsm = new FSM_a_a_a();
	FiniteSpec finiteSpec = new FSMSpec(fsm.fsm);
	createDefaultParametricMonitorWithAwareComponents(finiteSpec);
    }

    @Test
    public void verifyPostConditions() throws Exception {
	assertNoMoreCreatedMonitors();
	assertNoMoreUpdatedMonitors();
	assertNoMoreRetrievedNodes();
    }

    // firstEvent //////////////////////////////////////////////////////////////////

    @Test
    public void firstEvent_createsOnlyOneMonitor() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	popNextCreatedMonitor();
	assertNoMoreCreatedMonitors();
    }

    @Test
    public void firstEvent_updatesOnlyOneMonitor() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	popNextUpdatedMonitor();
	assertEquals(1, BaseMonitor.getUpdateddMonitorsCount());
	assertNoMoreUpdatedMonitors();
    }

    @Test
    public void firstEvent_createsMonitorWithCreationTime0() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	assertEquals(0L, popNextUpdatedMonitor().getTimestamp());
    }

    @Test
    public void firstEvent_createsCorrectTrace() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	assertTrace(popNextUpdatedMonitor(), fsm.e1);
    }

    @Test
    public void firstEvent_monitorBindsAllItsParameters() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	assertBoundObjects(popNextUpdatedMonitor(), a);
    }

    @Test
    public void firstEvent_noMatchDetected() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	assertTrue(fsm.matchHandler.getHandledMatches().isEmpty());
    }

    @Test
    public void firstEvent_onlyOneNodeIsRetrieved() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	assertNotNull(popNextRetrievedNode());
	assertNoMoreRetrievedNodes();
    }

    @Test
    public void firstEvent_nodeHasNoMonitorSets() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	assertArrayEquals(new MonitorSet[0], popNextRetrievedNode().getMonitorSets());
    }

    @Test
    public void firstEvent_metaNodeHasCorrectParameterSet() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	assertEquals(asSet(fsm.p1), popNextRetrievedNode().getMetaNode().getNodeParameterSet());
    }

    // recurringEvent = same event as first event again ////////////////////////////////

    @Test
    public void recurringEvent_updatesOneMonitorTwice() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	assertEquals(2, BaseMonitor.getUpdateddMonitorsCount());
    }

    @Test
    public void recurringEvent_doesNotCreateNewMonitor() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	popNextCreatedMonitor();
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	assertNoMoreCreatedMonitors();
    }

    @Test
    public void recurringEvent_updatesSameMonitor() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	assertEquals(popNextUpdatedMonitor(), popNextUpdatedMonitor());
	assertNoMoreUpdatedMonitors();
    }

    @Test
    public void recurringEvent_createsCorrectTrace() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	assertTrace(popNextCreatedMonitor(), fsm.e1, fsm.e1);
    }

    @Test
    public void recurringEvent_monitorHasSameTimestamp0() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	assertEquals(0L, popNextUpdatedMonitor().getTimestamp());
	assertEquals(0L, popNextUpdatedMonitor().getTimestamp());
    }

    @Test
    public void recurringEvent_monitorStillBindsSameObjects() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	assertBoundObjects(popNextUpdatedMonitor(), a);
    }

    @Test
    public void recurringEvent_bindingsAreReused() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	LowLevelBinding[] bindings1 = popNextRetrievedBinding();
	LowLevelBinding[] bindings2 = popNextRetrievedBinding();
	assertTrue(bindings1[0] == bindings2[0]);
    }

    // twoEvents = two different events ////////////////////////////////

    @Test
    public void twoEvents_secondEventDoesCreateASingleNewMonitor() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	popNextCreatedMonitor();
	pm.processEvent(fsm.e1.createEvent(b));

	// verify
	popNextCreatedMonitor();
	assertNoMoreCreatedMonitors();
    }

    @Test
    public void twoEvents_createdMonitorsAreDifferent() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e1.createEvent(b));

	// verify
	assertNotSame(popNextCreatedMonitor(), popNextCreatedMonitor());
    }

    @Test
    public void twoEvents_updatedMonitorsAreDifferent() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e1.createEvent(b));

	// verify
	assertNotSame(popNextUpdatedMonitor(), popNextUpdatedMonitor());
	assertNoMoreUpdatedMonitors();
    }

    @Test
    public void twoEvents_bothTracesAreCorrect() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e1.createEvent(b));

	// verify
	assertTrace(popNextUpdatedMonitor(), fsm.e1);
	assertTrace(popNextUpdatedMonitor(), fsm.e1);
    }

    @Test
    public void twoEvents_timestampsAreCorrect() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e1.createEvent(b));

	// verify
	assertEquals(0L, popNextCreatedMonitor().getTimestamp());
	assertEquals(1L, popNextCreatedMonitor().getTimestamp());
    }

    @Test
    public void twoEvents_boundObjectsAreCorrect() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e1.createEvent(b));

	// verify
	assertBoundObjects(popNextCreatedMonitor(), a);
	assertBoundObjects(popNextCreatedMonitor(), b);
    }

    // matchingTrace ////////////////////////////////////////////////

    @Test
    public void matchingTrace() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	assertEquals(1, fsm.matchHandler.getHandledMatches().size());
    }

    @Test
    public void matchingTracePlusOneDoesNotAddAnotherMatch() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e1.createEvent(a));
	pm.processEvent(fsm.e1.createEvent(a));

	// verify
	assertEquals(1, fsm.matchHandler.getHandledMatches().size());
    }

}
