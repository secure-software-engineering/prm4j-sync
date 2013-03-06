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

import java.util.Collection;
import java.util.Iterator;

import prm4j.api.Alphabet;
import prm4j.api.MatchHandler;
import prm4j.api.Parameter;
import prm4j.api.Symbol1;
import prm4j.api.Symbol2;
import prm4j.api.fsm.FSM;
import prm4j.api.fsm.FSMState;

@SuppressWarnings("rawtypes")
public class FSM_SafeIterator implements FSM_Base{

    public final Alphabet alphabet = new Alphabet();

    public final Parameter<Collection> c = alphabet.createParameter("c", Collection.class);
    public final Parameter<Iterator> i = alphabet.createParameter("i", Iterator.class);

    public final Symbol2<String, Collection, Iterator> createIter = alphabet.createSymbol2("create", c, i);
    public final Symbol1<String, Collection> updateColl = alphabet.createSymbol1("update", c);
    public final Symbol1<String, Iterator> useIter = alphabet.createSymbol1("next", i);

    public final FSM fsm = new FSM<String>(alphabet);

    public final MatchHandler matchHandler = MatchHandler.NO_OP;

    public final FSMState initial = fsm.createInitialState();
    public final FSMState s1 = fsm.createState();
    public final FSMState s2 = fsm.createState();
    public final FSMState s3 = fsm.createState();
    public final FSMState error = fsm.createAcceptingState(matchHandler);

    public FSM_SafeIterator() {
	initial.addTransition(updateColl, initial);
	initial.addTransition(createIter, s1);
	s1.addTransition(useIter, s1);
	s1.addTransition(updateColl, s2);
	s2.addTransition(updateColl, s2);
	s2.addTransition(useIter, error);
    }
    
	public FSM getFSM(){
		return fsm;
	}
	
	public Alphabet getAlphabet(){
		return alphabet;
	}
}
