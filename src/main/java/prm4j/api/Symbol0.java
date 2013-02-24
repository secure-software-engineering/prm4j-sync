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
package prm4j.api;

/**
 * Parameterless symbol.
 */
public class Symbol0 <L> extends Symbol<L> {

    Symbol0(Alphabet <L>alphabet, int uniqueId, L uniqueName) {
	super(alphabet, uniqueId, uniqueName);
	setParameters();
    }

    public Event createEvent() {
	Object[] boundObjects = createObjectArray();
	return new Event(this, boundObjects);
    }

    public Event createEvent(Condition condition) {
	Object[] boundObjects = createObjectArray();
	return new Event(this, boundObjects, condition, null);
    }

    public Event createConditionalEvent(Object auxiliaryData) {
	Object[] boundObjects = createObjectArray();
	return new Event(this, boundObjects, null, auxiliaryData);
    }

    public Event createConditionalEvent(Condition condition, Object auxiliaryData) {
	Object[] boundObjects = createObjectArray();
	return new Event(this, boundObjects, condition, auxiliaryData);
    }

}
