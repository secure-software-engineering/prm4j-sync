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
package prm4j.util;

import java.util.Iterator;

import prm4j.api.Alphabet;
import prm4j.api.MatchHandler;
import prm4j.api.Parameter;
import prm4j.api.Symbol1;
import prm4j.api.fsm.FSM;
import prm4j.api.fsm.FSMState;

@SuppressWarnings("rawtypes")
public interface FSM_Base {

	public FSM getFSM();
	public Alphabet getAlphabet();
}
