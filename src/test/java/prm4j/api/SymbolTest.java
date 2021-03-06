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

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import prm4j.AbstractTest;

public class SymbolTest extends AbstractTest {

    @Test
    public void getParameterIndices_UnsafeMapIterator() throws Exception {
	FSM_SafeMapIterator fsm = new FSM_SafeMapIterator();

	assertArrayEquals(array(0), fsm.updateMap.getParameterMask());
	assertArrayEquals(array(0, 1), fsm.createColl.getParameterMask());
	assertArrayEquals(array(1, 2), fsm.createIter.getParameterMask());
	assertArrayEquals(array(2), fsm.useIter.getParameterMask());
    }

}
