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
import java.util.Map;

import prm4j.api.Alphabet;
import prm4j.api.MatchHandler;
import prm4j.api.Parameter;
import prm4j.api.Symbol1;
import prm4j.api.Symbol2;
import prm4j.api.fsm.FSM;
import prm4j.api.fsm.FSMState;

@SuppressWarnings("rawtypes")
public class FSM_SafeSyncMap implements FSM_Base{

    public final Alphabet alphabet = new Alphabet();

    public final Parameter<Map> m = alphabet.createParameter("m", Map.class);
    public final Parameter<Collection> c = alphabet.createParameter("c", Collection.class);
    public final Parameter<Iterator> i = alphabet.createParameter("i", Iterator.class);

    public final Symbol1<String, Map> sync = alphabet.createSymbol1("sync", m);
    public final Symbol2<String, Map, Collection> createSet = alphabet.createSymbol2("createSet", m, c);
    public final Symbol2<String, Collection, Iterator> asyncCreateIter = alphabet.createSymbol2("asyncCreateIter", c, i);
    public final Symbol2<String, Collection, Iterator> syncCreateIter = alphabet.createSymbol2("syncCreateIter", c, i);
    public final Symbol1<String, Iterator> accessIter = alphabet.createSymbol1("accessIter", i);

    public final FSM fsm = new FSM(alphabet);

    public final  MatchHandler matchHandler = MatchHandler.NO_OP;

    public final FSMState initial = fsm.createInitialState();
    public final FSMState s1 = fsm.createState();
    public final FSMState s2 = fsm.createState();
    public final FSMState s3 = fsm.createState();
    public final FSMState error = fsm.createAcceptingState(matchHandler);

    public FSM_SafeSyncMap() {
	initial.addTransition(sync, s1);
	s1.addTransition(createSet, s2);
	s2.addTransition(asyncCreateIter, error);
	s2.addTransition(syncCreateIter, s2);
	s3.addTransition(accessIter, error);
    }

	public FSM getFSM(){
		return fsm;
	}
	
	public Alphabet getAlphabet(){
		return alphabet;
	}
}
