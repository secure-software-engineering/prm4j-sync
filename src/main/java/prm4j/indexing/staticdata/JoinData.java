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
package prm4j.indexing.staticdata;

import java.util.Arrays;

import prm4j.indexing.realtime.DeadMonitor;

/**
 * Represents all instances which are compatible with the event instance.
 * <p>
 * Used by {@link EventContext}
 */
public class JoinData {

    // identifies the node, which represents the compatible part of the instance, we want to join with
    private final int[] nodeMask;
    // identifies the monitor set, which contains the monitors carrying the bindings we will join with (they are
    // strictly more informative than the node, selected by the nodeMask)
    private final int monitorSetId;
    // prepares the event bindings for the join
    private final int[] extensionPattern;
    // identifies the bindings which will be used for the join, picking out only "new" parameters
    private final int[] copyPattern;

    private int[][] disableMasks;

    public JoinData(int[] nodeMask, int monitorSetId, int[] extensionPattern, int[] copyPattern, int[][] disableMasks) {
	super();
	this.nodeMask = nodeMask;
	this.monitorSetId = monitorSetId;
	this.extensionPattern = extensionPattern;
	this.copyPattern = copyPattern;
	this.disableMasks = disableMasks;
    }

    public int[] getNodeMask() {
	return nodeMask;
    }

    public int getMonitorSetId() {
	return monitorSetId;
    }

    public int[] getExtensionPattern() {
	return extensionPattern;
    }

    /**
     * 
     * { joiningBinding[i1], joinableBinding[j1], joiningBinding[i2], joinableBinding[j2], ... }
     * 
     * @return
     */
    public int[] getCopyPattern() {
	return copyPattern;
    }

    /**
     * ParameterMasks to be used with uncompressed bindings. Each parameterMask selects a instance to check if it has a
     * monitor. If it has a monitor, do not create a monitor (and node, if possible) in this join. The checked instances
     * are usually configured with the {@link DeadMonitor}, to define that they are disabled.
     * 
     * @return array of parameter masks
     */
    public int[][] getDisableMasks() {
	return disableMasks;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + Arrays.hashCode(copyPattern);
	result = prime * result + Arrays.hashCode(extensionPattern);
	result = prime * result + monitorSetId;
	result = prime * result + Arrays.hashCode(nodeMask);
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
	JoinData other = (JoinData) obj;
	if (!Arrays.equals(copyPattern, other.copyPattern)) {
	    return false;
	}
	if (!Arrays.equals(disableMasks, other.disableMasks)) {
	    return false;
	}
	if (!Arrays.equals(extensionPattern, other.extensionPattern)) {
	    return false;
	}
	if (monitorSetId != other.monitorSetId) {
	    return false;
	}
	if (!Arrays.equals(nodeMask, other.nodeMask)) {
	    return false;
	}
	return true;
    }

    @Override
    public String toString() {
	StringBuilder disableMasksString = new StringBuilder("[");
	for (int[] disableMask : disableMasks) {
	    disableMasksString.append(Arrays.toString(disableMask));
	    disableMasksString.append(" ");
	}
	disableMasksString.append("]");
	return "JoinData [nodeMask=" + Arrays.toString(nodeMask) + ", monitorSetId=" + monitorSetId
		+ ", extensionPattern=" + Arrays.toString(extensionPattern) + ", copyPattern="
		+ Arrays.toString(copyPattern) + ", disableMasks=" + disableMasksString.toString() + "]";
    }
}
