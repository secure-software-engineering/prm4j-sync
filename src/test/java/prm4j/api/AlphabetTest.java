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

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import prm4j.AbstractTest;
import prm4j.sync.AbstractSyncingSpec.AbstractionAndSymbol;


public class AlphabetTest extends AbstractTest {

    @Test
    @SuppressWarnings("rawtypes")
    public void getIndex_getParameterCount_getParameters() throws Exception {
	final Alphabet alphabet = new Alphabet();

	final Parameter<Collection> c = alphabet.createParameter(Collection.class);
	final Parameter<Iterator> i = alphabet.createParameter(Iterator.class);
	final Symbol1<AbstractionAndSymbol, Collection> updateColl = alphabet.createSymbol1(c);
	final Symbol2<AbstractionAndSymbol, Collection, Iterator> createIter = alphabet.createSymbol2(c, i);
	final Symbol1<AbstractionAndSymbol, Iterator> updateIter = alphabet.createSymbol1(i);

	assertEquals(0, updateColl.getIndex());
	assertEquals(1, updateColl.getParameterCount());
	assertEquals(asSet(c), updateColl.getParameters());

	assertEquals(1, createIter.getIndex());
	assertEquals(2, createIter.getParameterCount());
	assertEquals(asSet(c, i), createIter.getParameters());

	assertEquals(2, updateIter.getIndex());
	assertEquals(1, updateIter.getParameterCount());
	assertEquals(asSet(i), updateIter.getParameters());
    }

}
