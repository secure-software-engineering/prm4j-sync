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

public class Symbol1<L, P1> extends Symbol<L> {

    private final Parameter<P1> param1;

    Symbol1(Alphabet<L> alphabet, int uniqueId, L uniqueName, Parameter<P1> param1) {
	super(alphabet, uniqueId, uniqueName);
	this.param1 = param1;
	setParameters(param1);
    }

    public Symbol1(Alphabet<L> alphabet, int uniqueId, String uniqueName,
			Parameter<P1> param1) {
    	super(alphabet, uniqueId, uniqueName);
    	this.param1 = param1;
    	setParameters(param1);
	}


	public Event createEvent(P1 obj1) {
	Object[] boundObjects = createObjectArray();
	bindObject(this.param1, obj1, boundObjects);
	return new Event(this, boundObjects);
    }

    public Event createEvent(P1 obj1, Object auxiliaryData) {
	Object[] boundObjects = createObjectArray();
	bindObject(this.param1, obj1, boundObjects);
	return new Event(this, boundObjects, null, auxiliaryData);
    }

    public Event createConditionalEvent(P1 obj1, Condition condition) {
	Object[] boundObjects = createObjectArray();
	bindObject(this.param1, obj1, boundObjects);
	return new Event(this, boundObjects, condition, null);
    }

    public Event createConditionalEvent(P1 obj1, Condition condition, Object auxiliaryData) {
	Object[] boundObjects = createObjectArray();
	bindObject(this.param1, obj1, boundObjects);
	return new Event(this, boundObjects, condition, auxiliaryData);
    }

}
