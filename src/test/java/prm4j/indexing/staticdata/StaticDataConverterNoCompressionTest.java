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
package prm4j.indexing.staticdata;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static prm4j.Util.toNodeMask;
import static prm4j.Visualizer.visualizeMetaTree;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import prm4j.AbstractTest;
import prm4j.api.Parameter;
import prm4j.api.fsm.FSM;
import prm4j.api.fsm.FSMSpec;
import prm4j.spec.FiniteParametricProperty;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

@SuppressWarnings("rawtypes")
public class StaticDataConverterNoCompressionTest extends AbstractTest {

    private final static Parameter p1 = new Parameter("p1");
    private final static Parameter p2 = new Parameter("p2");
    private final static Parameter p3 = new Parameter("p3");
    private final static Parameter p4 = new Parameter("p4");
    private final static Parameter p5 = new Parameter("p5");

    @Before
    public void initialize() {
	p1.setIndex(1);
	p2.setIndex(2);
	p3.setIndex(3);
	p4.setIndex(4);
	p5.setIndex(5);
    }

    // /////////////// getExtensionPattern ///////////////////////////

    @Test
    public void getExtensionPattern_p1p3_p3p5() {

	Set<Parameter<?>> ps1 = asSet(p1, p3);
	Set<Parameter<?>> ps2 = asSet(p3, p5);

	boolean[] actual = StaticDataConverterNoCompression.getExtensionPattern(ps1, ps2);

	boolean[] expected = { true, true, false };

	assertBooleanArrayEquals(expected, actual);
    }

    @Test
    public void getExtensionPattern_p3p5_p1p3() {

	Set<Parameter<?>> ps1 = asSet(p3, p5);
	Set<Parameter<?>> ps2 = asSet(p1, p3);

	boolean[] actual = StaticDataConverterNoCompression.getExtensionPattern(ps1, ps2);

	boolean[] expected = { false, true, true };

	assertBooleanArrayEquals(expected, actual);
    }

    @Test
    public void getExtensionPattern_p1_p5() {

	Set<Parameter<?>> ps1 = asSet(p1);
	Set<Parameter<?>> ps2 = asSet(p5);

	boolean[] actual = StaticDataConverterNoCompression.getExtensionPattern(ps1, ps2);

	boolean[] expected = { true, false };

	assertBooleanArrayEquals(expected, actual);
    }

    @Test
    public void getExtensionPattern_p5_p1() {

	Set<Parameter<?>> ps1 = asSet(p5);
	Set<Parameter<?>> ps2 = asSet(p1);

	boolean[] actual = StaticDataConverterNoCompression.getExtensionPattern(ps1, ps2);

	boolean[] expected = { false, true };

	assertBooleanArrayEquals(expected, actual);
    }

    @Test
    public void getExtensionPattern_p3p5_p1p5() {

	Set<Parameter<?>> ps1 = asSet(p3, p5);
	Set<Parameter<?>> ps2 = asSet(p1, p5);

	boolean[] actual = StaticDataConverterNoCompression.getExtensionPattern(ps1, ps2);

	boolean[] expected = { false, true, true };

	assertBooleanArrayEquals(expected, actual);
    }

    @Test
    public void getExtensionPattern_p3p4p5_p1p2p5() {

	Set<Parameter<?>> ps1 = asSet(p3, p4, p5);
	Set<Parameter<?>> ps2 = asSet(p1, p2, p5);

	boolean[] actual = StaticDataConverterNoCompression.getExtensionPattern(ps1, ps2);

	boolean[] expected = { false, false, true, true, true };

	assertBooleanArrayEquals(expected, actual);
    }

    @Test
    public void getExtensionPattern_p2p4_p1p3p5() {

	Set<Parameter<?>> ps1 = asSet(p2, p4);
	Set<Parameter<?>> ps2 = asSet(p1, p3, p5);

	boolean[] actual = StaticDataConverterNoCompression.getExtensionPattern(ps1, ps2);

	boolean[] expected = { false, true, false, true, false };

	assertBooleanArrayEquals(expected, actual);
    }

    @Test
    public void getExtensionPattern_p1p2p3p4_p2p3p4p5() {

	Set<Parameter<?>> ps1 = asSet(p1, p2, p3, p4);
	Set<Parameter<?>> ps2 = asSet(p2, p3, p4, p5);

	boolean[] actual = StaticDataConverterNoCompression.getExtensionPattern(ps1, ps2);

	boolean[] expected = { true, true, true, true, false };

	assertBooleanArrayEquals(expected, actual);
    }

    @Test
    public void getExtensionPattern_p3_p1p2p3() {

	Set<Parameter<?>> ps1 = asSet(p3);
	Set<Parameter<?>> ps2 = asSet(p1, p2, p3);

	boolean[] actual = StaticDataConverterNoCompression.getExtensionPattern(ps1, ps2);

	boolean[] expected = { false, false, true };

	assertBooleanArrayEquals(expected, actual);
    }

    @Test
    public void getExtensionPattern_unsafeMapIterator() {

	FSM_unsafeMapIterator u = new FSM_unsafeMapIterator();
	u.m.setIndex(0);
	u.c.setIndex(1);
	u.i.setIndex(2);

	Set<Parameter<?>> ps1 = asSet(u.m, u.c);
	Set<Parameter<?>> ps2 = asSet(u.c, u.i);

	boolean[] actual = StaticDataConverterNoCompression.getExtensionPattern(ps1, ps2);

	boolean[] expected = { true, true, false };

	assertBooleanArrayEquals(expected, actual);
    }

    // /////////////// getCopyPattern ///////////////////////////

    @Test
    public void getCopyPattern_p1_p5() {

	Set<Parameter<?>> ps1 = asSet(p1);
	Set<Parameter<?>> ps2 = asSet(p5);

	int[] actual = StaticDataConverterNoCompression.getCopyPattern(ps1, ps2);

	int[] expected = { 0, 1 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getCopyPattern_p1p5_p5() {

	Set<Parameter<?>> ps1 = asSet(p2, p3);
	Set<Parameter<?>> ps2 = asSet(p1, p2);

	int[] actual = StaticDataConverterNoCompression.getCopyPattern(ps1, ps2);

	int[] expected = { 0, 0 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getCopyPattern_p5_p1() {

	Set<Parameter<?>> ps1 = asSet(p5);
	Set<Parameter<?>> ps2 = asSet(p1);

	int[] actual = StaticDataConverterNoCompression.getCopyPattern(ps1, ps2);

	int[] expected = { 0, 0 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getCopyPattern_p1p3_p3p5() {

	Set<Parameter<?>> ps1 = asSet(p1, p3);
	Set<Parameter<?>> ps2 = asSet(p3, p5);

	int[] actual = StaticDataConverterNoCompression.getCopyPattern(ps1, ps2);

	int[] expected = { 1, 2 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getCopyPattern_p1p2p3p4_p2p3p4p5() {

	Set<Parameter<?>> ps1 = asSet(p1, p2, p3, p4);
	Set<Parameter<?>> ps2 = asSet(p2, p3, p4, p5);

	int[] actual = StaticDataConverterNoCompression.getCopyPattern(ps1, ps2);

	int[] expected = { 3, 4 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getCopyPattern_p3_p1p2p3() {

	Set<Parameter<?>> ps1 = asSet(p3);
	Set<Parameter<?>> ps2 = asSet(p1, p2, p3);

	int[] actual = StaticDataConverterNoCompression.getCopyPattern(ps1, ps2);

	int[] expected = { 0, 0, 1, 1 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getCopyPattern_p3_p1p2p3p4p5() {

	Set<Parameter<?>> ps1 = asSet(p3);
	Set<Parameter<?>> ps2 = asSet(p1, p2, p3, p4, p5);

	int[] actual = StaticDataConverterNoCompression.getCopyPattern(ps1, ps2);

	int[] expected = { 0, 0, 1, 1, 3, 3, 4, 4 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getCopyPattern_p1p2p5_p3p4p5() {

	Set<Parameter<?>> ps1 = asSet(p1, p2, p5);
	Set<Parameter<?>> ps2 = asSet(p3, p4, p5);

	int[] actual = StaticDataConverterNoCompression.getCopyPattern(ps1, ps2);

	int[] expected = { 0, 2, 1, 3 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getCopyPattern_p3p5_p1p2p4p5() {

	Set<Parameter<?>> ps1 = asSet(p3, p5);
	Set<Parameter<?>> ps2 = asSet(p1, p2, p4, p5);

	int[] actual = StaticDataConverterNoCompression.getCopyPattern(ps1, ps2);

	int[] expected = { 0, 0, 1, 1, 2, 3 };

	assertArrayEquals(expected, actual);
    }

    // /////////////// toParameterMask ///////////////////////////

    @Test
    public void toParameterMask_arbitrary() {

	int[] actual = StaticDataConverterNoCompression.toParameterMask(asSet(p2, p3, p5));

	int[] expected = { 2, 3, 5 };

	assertArrayEquals(expected, actual);
    }

    @Test
    public void toParameterMask_emptyset() {

	int[] actual = StaticDataConverterNoCompression.toParameterMask(EMPTY_PARAMETER_SET);

	int[] expected = {};

	assertArrayEquals(expected, actual);
    }

    // /////////////// getMaxData ///////////////////////////

    @Test
    public void getMaxData_unsafeMapIterator() {

	FSM_unsafeMapIterator u = new FSM_unsafeMapIterator();
	FSM fsm = u.fsm;
	u.m.setIndex(0);
	u.c.setIndex(1);
	u.i.setIndex(2);
	FiniteParametricProperty fpp = new FiniteParametricProperty(new FSMSpec(fsm));
	StaticDataConverterNoCompression sdc = new StaticDataConverterNoCompression(fpp);

	MaxData[][] actual = sdc.getMaxData();

	MaxData[][] expected = new MaxData[fpp.getBaseEvents().size()][];
	expected[u.createColl.getIndex()] = new MaxData[0];
	expected[u.updateMap.getIndex()] = new MaxData[0];
	expected[u.createIter.getIndex()] = new MaxData[0];
	expected[u.useIter.getIndex()] = new MaxData[0];

	assertArrayEquals(expected, actual);
    }

    @Test
    public void getJoinData_unsafeMapIterator() {

	FSM_unsafeMapIterator u = new FSM_unsafeMapIterator();
	FSM fsm = u.fsm;
	u.m.setIndex(0);
	u.c.setIndex(1);
	u.i.setIndex(2);
	FiniteParametricProperty fpp = new FiniteParametricProperty(new FSMSpec(fsm));
	StaticDataConverterNoCompression sdc = new StaticDataConverterNoCompression(fpp);

	JoinData[][] actual = sdc.getJoinData();

	JoinData[][] expected = new JoinData[fpp.getBaseEvents().size()][];
	expected[u.createColl.getIndex()] = new JoinData[0];

	expected[u.updateMap.getIndex()] = new JoinData[0];

	JoinData[] jd = new JoinData[1];
	// u.createIter.getParameters() = { c, i }
	// compatible node selected by { c }
	jd[0] = new JoinData(array(0), 0, array(false, true, true), array(0, 0), array(1));
	expected[u.createIter.getIndex()] = jd;

	expected[u.useIter.getIndex()] = new JoinData[0];

	assert2DimArrayEquals(expected, actual);
    }

    @Test
    public void getChainData_unsafeMapIterator() {

	FSM_unsafeMapIterator u = new FSM_unsafeMapIterator();
	FSM fsm = u.fsm;
	u.m.setIndex(0);
	u.c.setIndex(1);
	u.i.setIndex(2);
	FiniteParametricProperty fpp = new FiniteParametricProperty(new FSMSpec(fsm));
	StaticDataConverterNoCompression sdc = new StaticDataConverterNoCompression(fpp);

	SetMultimap<Set<Parameter<?>>, ChainData> actual = sdc.getChainData();

	SetMultimap<Set<Parameter<?>>, ChainData> expected = HashMultimap.create();
	expected.put(asSet(u.m, u.c, u.i), new ChainData(list(0), 0));
	expected.put(asSet(u.m, u.c, u.i), new ChainData(list(2), 0));
	expected.put(asSet(u.m, u.c, u.i), new ChainData(list(0, 1), 0));
	expected.put(asSet(u.m, u.c, u.i), new ChainData(list(1, 2), 0));
	// optimized away by recognizing non-state changing transitions:
	// expected.put(asSet(u.m, u.c), new ChainData(list(0), 0));
	expected.put(asSet(u.m, u.c), new ChainData(list(1), 0));

	assertEquals(expected, actual);
    }

    @Test
    public void getMetaTree_unsafeMapIterator() {

	final Set<ChainData> emptyChainDataSet = new HashSet<ChainData>();

	FSM_unsafeMapIterator u = new FSM_unsafeMapIterator();
	FSM fsm = u.fsm;
	u.m.setIndex(0);
	u.c.setIndex(1);
	u.i.setIndex(2);
	FiniteParametricProperty fpp = new FiniteParametricProperty(new FSMSpec(fsm));
	StaticDataConverterNoCompression sdc = new StaticDataConverterNoCompression(fpp);

	MetaNode actual = sdc.getMetaTree();
	MetaNode expected = new MetaNode(EMPTY_PARAMETER_SET, fpp.getParameters());

	// depth 1
	expected.getMetaNode(u.m).setChainData(emptyChainDataSet);
	expected.getMetaNode(u.m).setMonitorSetCount(1);

	expected.getMetaNode(u.c).setChainData(emptyChainDataSet);
	expected.getMetaNode(u.c).setMonitorSetCount(1);

	expected.getMetaNode(u.i).setChainData(emptyChainDataSet);
	expected.getMetaNode(u.i).setMonitorSetCount(1);

	// depth 2
	expected.getMetaNode(u.m, u.c).setChainData(
		asSet(new ChainData(toNodeMask(u.c), 0)));
	expected.getMetaNode(u.m, u.c).setMonitorSetCount(1);

	expected.getMetaNode(u.c, u.i).setChainData(emptyChainDataSet);
	expected.getMetaNode(u.c, u.i).setMonitorSetCount(1);

	// depth 3
	expected.getMetaNode(u.m, u.c, u.i).setChainData(
		asSet(new ChainData(toNodeMask(u.m), 0), new ChainData(toNodeMask(u.m, u.c), 0), new ChainData(
			toNodeMask(u.c, u.i), 0), new ChainData(toNodeMask(u.i), 0)));
	expected.getMetaNode(u.m, u.c, u.i).setMonitorSetCount(0);

	visualizeMetaTree(expected, "StaticDataConverterNoCompression/getMetaTree_unsafeMapIterator", "expected");
	visualizeMetaTree(actual, "StaticDataConverterNoCompression/getMetaTree_unsafeMapIterator", "actual");

	assertEquals(expected, actual);
    }

}