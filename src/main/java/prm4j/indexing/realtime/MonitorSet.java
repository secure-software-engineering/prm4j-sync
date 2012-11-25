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
package prm4j.indexing.realtime;

import java.util.Arrays;

import prm4j.api.Event;
import prm4j.indexing.BaseMonitor;
import prm4j.indexing.staticdata.ChainData;

public class MonitorSet {

    protected static final int DEFAULT_CAPACITY = 16;

    private int capacity;
    private int size = 0;
    private BaseMonitor[] monitorSet;

    public MonitorSet() {
	capacity = DEFAULT_CAPACITY;
	monitorSet = new BaseMonitor[capacity];
    }

    public void add(BaseMonitor monitor) {
	monitorSet[size++] = monitor;
	ensureCapacity();
    }

    private void ensureCapacity() {
	if (size >= capacity) {
	    capacity = (capacity * 3) / 2 + 1;
	    monitorSet = Arrays.copyOf(monitorSet, capacity);
	}
    }

    public void processEvent(Event event) {
	int aliveMonitors = 0;
	for (int i = 0; i < size; i++) { // 63
	    final BaseMonitor monitor = monitorSet[i];
	    if (monitor.isTerminated()) {
		continue;
	    } else {
		monitorSet[aliveMonitors++] = monitor;
	    }
	    monitor.processEvent(event);
	}
	for (int i = aliveMonitors; i < size; i++) {
	    monitorSet[i] = null;
	}
	size = aliveMonitors;
    }

    public void join(NodeStore nodeStore, LowLevelBinding[] bindings, Event event,
	    final LowLevelBinding[] joinableBindings, boolean someBindingsAreKnown, long tmax, int[] copyPattern) {
	// iterate over all compatible nodes
	LowLevelBinding[] joinable = joinableBindings.clone(); // 62

	int aliveMonitors = 0;
	for (int i = 0; i < size; i++) { // 63

	    final BaseMonitor compatibleMonitor = monitorSet[i];
	    if (compatibleMonitor == null) {
		break;
	    }
	    if (someBindingsAreKnown && compatibleMonitor.getCreationTime() < tmax) { // 64
		aliveMonitors++; // this monitor may be still alive, we just avoid joining with it
		continue; // 65
	    }
	    final LowLevelBinding[] compatibleBindings = compatibleMonitor.getLowLevelBindings();
	    if (compatibleBindings == null) {
		continue; // this monitor will be removed from the set
	    }
	    createJoin(joinable, compatibleBindings, copyPattern); // 67 - 71
	    final Node lastNode = nodeStore.getOrCreateNode(joinable);
	    if (lastNode.getMonitor() == null) { // 72
		// inlined DefineTo // 73
		final BaseMonitor monitor = compatibleMonitor.copy(joinable); // 102-105
		if (monitor.processEvent(event)) { // 103
		    monitorSet[aliveMonitors++] = compatibleMonitor;
		}
		lastNode.setMonitor(monitor); // 106
		// chain phase
		for (ChainData chainData : lastNode.getMetaNode().getChainDataArray()) {
		    nodeStore.getOrCreateNode(joinable, chainData.getNodeMask())
			    .getMonitorSet(chainData.getMonitorSetId()).add(monitor);
		} // 99
		joinable = joinableBindings.clone(); // 74
	    }
	}
	for (int i = aliveMonitors; i < size; i++) {
	    monitorSet[i] = null;
	}
    }

    private static void createJoin(LowLevelBinding[] joinableBindings, LowLevelBinding[] joiningBindings,
	    int[] copyPattern) {
	// fill in the missing bindings into the duplicate from the old monitor
	for (int j = 0; j < copyPattern.length; j += 2) {
	    // copy from j to j+1
	    joinableBindings[copyPattern[j + 1]] = joiningBindings[copyPattern[j]];
	}
    }

    /**
     * Searches the set linearly if monitor is contained, testing for object identity. Should be used only for
     * diagnostic purposes.
     *
     * @param monitor
     * @return true if monitor is contained
     */
    public boolean contains(BaseMonitor monitor) {
	for (int i = 0; i < size; i++) {
	    if (monitorSet[i] == monitor) {
		return true;
	    }
	}
	return false;
    }

    public int getSize() {
	return size;
    }

    @Override
    public String toString() {
	return Arrays.toString(monitorSet);
    }

}
