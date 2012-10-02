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
package prm4j.api.fsm;

import static org.junit.Assert.*;

import org.junit.Test;

import prm4j.AbstractTest;
import prm4j.logic.StatefulSpec;

public class FSMSpecTest extends AbstractTest {

    @Test
    public void getPropertyEnableSet() throws Exception {
	FSM_unsafeMapIterator unsafeMapIterator = new FSM_unsafeMapIterator();
	StatefulSpec fsmSpec = new FSMSpec<Void>(unsafeMapIterator.fsm);
    }

}
