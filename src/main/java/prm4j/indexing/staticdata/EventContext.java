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

import prm4j.api.BaseEvent;

public class EventContext {

    private final JoinData[][] joinDataArray;
    private final MaxData[][] maxDataArray;
    private final boolean[] creationEvents;
    private final boolean[] disablingEvents;
    // baseEvent * numberOfExistingMonitorMasks * parameterMaskLength
    private final int[][][] existingMonitorMasks;

    public EventContext(JoinData[][] joinData, MaxData[][] maxData, boolean[] creationEvents,
	    boolean[] disablingEvents, int[][][] existingMonitorMasks) {
	joinDataArray = joinData;
	maxDataArray = maxData;
	this.creationEvents = creationEvents;
	this.disablingEvents = disablingEvents;
	this.existingMonitorMasks = existingMonitorMasks;
    }

    public MaxData[] getMaxData(BaseEvent baseEvent) {
	if (baseEvent.getIndex() >= maxDataArray.length) {
	    throw new ArrayIndexOutOfBoundsException("MaxData expected for baseEvent " + baseEvent
		    + ". MaxDataArray has length of " + maxDataArray.length);
	}
	return maxDataArray[baseEvent.getIndex()];
    }

    public JoinData[] getJoinData(BaseEvent baseEvent) {
	return joinDataArray[baseEvent.getIndex()];
    }

    public boolean isCreationEvent(BaseEvent baseEvent) {
	/*
	 * System.out.println("In isCreationEvent " +
	 * ((Symbol<AbstractionAndSymbol>)baseEvent).getLabel().getSymbol().getLabel() + " " +
	 * ((Symbol<AbstractionAndSymbol>)baseEvent).getLabel().getAbstraction() + " " + baseEvent.getIndex());
	 */
	return creationEvents[baseEvent.getIndex()];
    }

    public boolean isDisablingEvent(BaseEvent baseEvent) {
	return disablingEvents[baseEvent.getIndex()];
    }

    /**
     * @param baseEvent
     * @return numberOfExistingMonitorMasks * parameterMaskLength
     */
    public int[][] getExistingMonitorMasks(BaseEvent baseEvent) {
	return existingMonitorMasks[baseEvent.getIndex()];
    }

}
