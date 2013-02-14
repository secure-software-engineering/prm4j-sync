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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static prm4j.Util.tuple;

import org.junit.Before;
import org.junit.Test;

import prm4j.api.fsm.FSMSpec;
import prm4j.spec.FiniteSpec;

public class DefaultParametricMonitor_ab_bc_c_Test extends AbstractDefaultParametricMonitorTest {

    FSM_ab_bc_c fsm;
    final String a = "a";
    final String b = "b";
    final String c = "c";

    @Before
    public void init() {
	fsm = new FSM_ab_bc_c();
	FiniteSpec finiteSpec = new FSMSpec(fsm.fsm);
	createDefaultParametricMonitorWithAwareComponents(finiteSpec);
    }

    // firstEvent_ab //////////////////////////////////////////////////////////////////

    @Test
    public void firstEvent_ab_createsOnlyOneMonitor() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a, b));

	// verify
	popNextCreatedMonitor();
	assertNoMoreCreatedMonitors();
    }

    @Test
    public void firstEvent_ab_updatesOnlyOneMonitor() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a, b));

	// verify
	popNextUpdatedMonitor();
	assertNoMoreUpdatedMonitors();
    }

    @Test
    public void firstEvent_ab_retrievesTwoNodes() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a, b));

	// verify
	popNextRetrievedNode();
	popNextRetrievedNode();
	assertNoMoreRetrievedNodes();
    }

    @Test
    public void firstEvent_ab_nodesAreNotNullNodes() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a, b));

	// verify
	assertNotSame(NullNode.instance, popNextRetrievedNode());
	assertNotSame(NullNode.instance, popNextRetrievedNode());
    }

    @Test
    public void firstEvent_ab_createsMonitorWithCreationTime0() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a, b));

	// verify
	assertEquals(0L, popNextUpdatedMonitor().getCreationTime());
    }

    @Test
    public void firstEvent_ab_createsCorrectTrace() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a, b));

	// verify
	assertTrace(popNextUpdatedMonitor(), fsm.e1);
    }

    @Test
    public void firstEvent_ab_monitorBindsAllItsParameters() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a, b));

	// verify
	assertBoundObjects(popNextUpdatedMonitor(), a, b);
    }

    @Test
    public void firstEvent_ab_noMatchDetected() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a, b));

	// verify
	assertTrue(fsm.matchHandler.getHandledMatches().isEmpty());
    }

    @Test
    public void firstEvent_ab_nodesHaveDifferentMetaNodes() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a, b));

	// verify
	assertNotSame(getNode(a, null, null).getMetaNode(), getNode(null, b, null).getMetaNode());
	assertNotSame(getNode(a, b, null).getMetaNode(), getNode(null, b, null).getMetaNode());
	assertNotSame(getNode(a, b, null).getMetaNode(), getNode(a, null, null).getMetaNode());
    }

    @Test
    public void firstEvent_ab_chainingIsPerformedCorrectly() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a, b));

	// verify
	assertEquals(0, getNode(a, null, null).getMonitorSets().length);
	assertEquals(1, getNode(null, b, null).getMonitorSets().length);
	assertEquals(1, getNode(null, b, null).getMonitorSet(0).getSize());
    }

    // twoEvents ab and bc //////////////////////////////////////////////////////////////////

    /*
     * We test if joining works
     */

    @Test
    public void joining_ab_bc_createsCorrectMonitors() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a, b));
	pm.processEvent(fsm.e2.createEvent(b, c));

	// verify
	assertBoundObjects(popNextCreatedMonitor(), a, b);
	assertBoundObjects(popNextCreatedMonitor(), a, b, c);
	assertNoMoreCreatedMonitors();
    }

    @Test
    public void joining_ab_bc_createsCorrectNodes() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a, b));
	pm.processEvent(fsm.e2.createEvent(b, c));

	// verify
	assertCreatedNodes(array(a, null, null), array(null, b, null), array(null, null, c), array(a, b, null),
		array(null, b, c), array(a, b, c));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void finiteParametricProperty_ensureCorrectUpdates() throws Exception {

	// verify
	assertEquals(
		asSet(tuple(asSet(fsm.p1, fsm.p2), asSet(fsm.p1, fsm.p2, fsm.p3)),
			tuple(asSet(fsm.p2, fsm.p3), asSet(fsm.p1, fsm.p2, fsm.p3)),
			tuple(asSet(fsm.p3), asSet(fsm.p1, fsm.p2, fsm.p3))), fpp.getUpdates());
    }

    @Test
    public void joining_ab_bc_chainingFromABtoABCexists() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a, b));
	pm.processEvent(fsm.e2.createEvent(b, c));

	// verify
	assertChaining(array(a, b, null), array(a, b, c));
    }

    @Test
    public void joining_ab_bc_chainingFromCtoABCexists() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a, b));
	pm.processEvent(fsm.e2.createEvent(b, c));

	// verify
	assertChaining(array(null, null, c), array(a, b, c));
    }

    @Test
    public void joining_ab_bc_chainingFromBCtoABCexists() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a, b));
	pm.processEvent(fsm.e2.createEvent(b, c));

	// verify
	assertChaining(array(null, b, c), array(a, b, c));
    }

    @Test
    public void joining_ab_bc_joinedMonitorHasCorrectTimestamp() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent("x", "y")); // time = 0
	pm.processEvent(fsm.e1.createEvent(a, b)); // time = 1
	pm.processEvent(fsm.e2.createEvent(b, c)); // derives 1 from (a, b)

	// verify
	assertEquals(1L, getNode(a, b, c).getMonitor().getCreationTime());
    }

    @Test
    public void joining_ab_bc_assertCorrectTraces() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a, b));
	pm.processEvent(fsm.e2.createEvent(b, c));

	// verify
	assertTrace(popNextCreatedMonitor(), fsm.e1);
	assertTrace(popNextCreatedMonitor(), fsm.e1, fsm.e2);
    }

    // moreEvents //////////////////////////////////////////////////////////////////

    @Test
    public void moreEvents_ab_bc_c_matchesTrace() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a, b));
	assertTrue(fsm.matchHandler.getHandledMatches().isEmpty());
	pm.processEvent(fsm.e2.createEvent(b, c));
	assertTrue(fsm.matchHandler.getHandledMatches().isEmpty());
	pm.processEvent(fsm.e3.createEvent(c));

	// verify
	assertTrue(!fsm.matchHandler.getHandledMatches().isEmpty());
    }

    @Test
    public void moreEvents_ab_bc_c_bc_ab_c_correctTrace() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a, b));
	pm.processEvent(fsm.e2.createEvent(b, c));
	pm.processEvent(fsm.e3.createEvent(c));
	pm.processEvent(fsm.e2.createEvent(b, c));
	pm.processEvent(fsm.e1.createEvent(a, b));
	pm.processEvent(fsm.e3.createEvent(c));

	// verify
	assertTrace(popNextCreatedMonitor(), fsm.e1, fsm.e1);
	assertTrace(popNextCreatedMonitor(), fsm.e1, fsm.e2, fsm.e3);
	// no more matches since the accepting state is a final state, and is cleaned from the monitor set
    }

    @Test
    public void moreEvents_ab_bc_c_c_matchesOnlyOneTrace() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a, b));
	pm.processEvent(fsm.e2.createEvent(b, c));
	pm.processEvent(fsm.e3.createEvent(c));
	pm.processEvent(fsm.e3.createEvent(c));

	// verify
	assertEquals(1, fsm.matchHandler.getHandledMatches().size());
    }

    @Test
    public void moreEvents_ab_bc_c_ab_bc_c_matchesOnlyOneTrace() throws Exception {
	// exercise
	pm.processEvent(fsm.e1.createEvent(a, b));
	pm.processEvent(fsm.e2.createEvent(b, c));
	pm.processEvent(fsm.e3.createEvent(c));
	pm.processEvent(fsm.e1.createEvent(a, b));
	pm.processEvent(fsm.e2.createEvent(b, c));
	pm.processEvent(fsm.e3.createEvent(c));

	// verify
	assertEquals(1, fsm.matchHandler.getHandledMatches().size());
    }

    // disabling //////////////////////////////////////////////////////////////////

    @Test
    public void disabling_bc_noNodesAreCreated() throws Exception {
	// exercise
	pm.processEvent(fsm.e2.createEvent(b, c));

	// verify
	assertCreatedNodes();
    }

    @Test
    public void disabling_bc_ab_noMonitorsAreCreated() throws Exception {

	// exercise
	pm.processEvent(fsm.e2.createEvent(b, c));
	pm.processEvent(fsm.e1.createEvent(a, b));

	// verify
	assertNoMoreCreatedMonitors();
    }

    @Test
    public void disabling_bc_ab_noNodesAreCreated() throws Exception {
	// exercise
	pm.processEvent(fsm.e2.createEvent(b, c));
	pm.processEvent(fsm.e1.createEvent(a, b));

	// verify
	assertCreatedNodes();
    }

}