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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import prm4j.Util.Tuple;
import prm4j.api.BaseEvent;
import prm4j.api.Parameter;
import prm4j.indexing.staticdata.StaticDataConverter;
import static org.junit.Assert.fail;

public class ModelVerifier {

    public StaticDataConverter converter;

    public ModelVerifier(StaticDataConverter converter) {
	this.converter = converter;
    }

    public void findMaxOverParameterSets(BaseEvent baseEvent, List<Set<Parameter<?>>> parameterSets) {
	checkEquality(parameterSets, converter.getParametricProperty().getMaxData().get(baseEvent),
		"findMaxOverParameterSets");
    }

    /**
     * Returns all disable parameter sets for all join operations
     * 
     * @param baseEvent
     * @param parameterSets
     */
    public void disableParameterSets(BaseEvent baseEvent, Set<Parameter<?>> enableSetToJoinWith,
	    List<Set<Parameter<?>>> parameterSets) {
	checkEquality(parameterSets, converter.getDisableParameterSets().get(baseEvent, enableSetToJoinWith),
		"disableParameterSets");
    }

    /**
     * Verifies that the base event tries a join operation over the list of parameter sets.
     * 
     * @param baseEvent
     * @param parameterSets
     */
    public void joinOverParameterSets(BaseEvent baseEvent, List<Set<Parameter<?>>> parameterSets) {
	List<Set<Parameter<?>>> list = new ArrayList<Set<Parameter<?>>>();
	for (Tuple<Set<Parameter<?>>, Set<Parameter<?>>> tuple : converter.getParametricProperty().getJoinData()
		.get(baseEvent)) {
	    list.add(tuple.getRight());
	}
	checkEquality(parameterSets, list, "joinOverParameterSets");
    }

    public void joinOverCompatibleInstances(BaseEvent baseEvent, List<Set<Parameter<?>>> parameterSets) {
	List<Set<Parameter<?>>> list = new ArrayList<Set<Parameter<?>>>();
	for (Tuple<Set<Parameter<?>>, Set<Parameter<?>>> tuple : converter.getParametricProperty().getJoinData()
		.get(baseEvent)) {
	    list.add(tuple.getLeft());
	}
	checkEquality(parameterSets, list, "joinOverCompatibleInstances");
    }

    private void checkEquality(Object expected, Object actual, String method) {
	if (!expected.equals(actual)) {
	    fail("" + method + " failed!\nexp: " + expected.toString() + "\nwas: " + actual.toString());
	}
    }

}
