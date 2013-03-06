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
package prm4j.api;

import java.util.Arrays;

import prm4j.indexing.Monitor;

/**
 * An event contains a generic base event and a number of {@link Binding}s.
 */
public class Event {

    private BaseEvent baseEvent;  // Rahul
    private final Object[] boundObjects;
    private final Object auxiliaryData;
    private final Condition condition;

    public Event(BaseEvent baseEvent, Object[] parameterValues) {
	this(baseEvent, parameterValues, null, null);
    }

    public Event(BaseEvent baseEvent, Object[] parameterValues, Condition condition, Object auxiliaryData) {
	this.baseEvent = baseEvent;
	boundObjects = parameterValues;
	this.auxiliaryData = auxiliaryData;
	this.condition = condition;
    }
    
    public void setBaseEvent(BaseEvent baseEvent) {  // Rahul
    	this.baseEvent = baseEvent;
        }

    public BaseEvent getEvaluatedBaseEvent(Monitor baseMonitor) {
	if (condition == null) {
	    return baseEvent;
	}
	return condition.eval(baseMonitor) ? baseEvent : null;
    }

    public BaseEvent getBaseEvent() {
	return baseEvent;
    }

    public Object getBoundObject(int parameterId) {
	return boundObjects[parameterId];
    }

    public Object[] getBoundObjects() {
	return boundObjects;
    }

    public Object getAuxiliaryData() {
	return auxiliaryData;
    }

    @Override
    public String toString() {
	return baseEvent.toString() + Arrays.toString(boundObjects);
    }

}
