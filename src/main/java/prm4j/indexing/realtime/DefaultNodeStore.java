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
package prm4j.indexing.realtime;

import prm4j.indexing.staticdata.MetaNode;

public class DefaultNodeStore implements NodeStore {

    private Node rootNode;

    public DefaultNodeStore(MetaNode metaTree) {
	 rootNode = metaTree.createRootNode();
    }

    @Override
    public Node getNode(LowLevelBinding[] bindings) {
	// fast track for the parameterless instance
	if (bindings.length == 0) {
	    return rootNode;
	}
	// retrieve the head of the array pointing to the first node we will use
	Node node = rootNode.getNode(bindings[0]);
	// we iterate over the rest { node1 , ..., nodeN }, traversing the tree
	for (int i = 1; i < bindings.length; i++) {
	    // traverse the node tree until the parameter instance is fully realized
	    node = node.getNode(bindings[i]);
	}
	return node;
    }

    @Override
    public Node getNode(LowLevelBinding[] bindings, int[] parameterMask) {
	// fast track for the parameterless instance
	if (parameterMask.length == 0) {
	    return rootNode;
	}
	// retrieve the head of the array pointing to the first node we will use
	Node node = rootNode.getNode(bindings[parameterMask[0]]);
	// we iterate over the rest { node1 , ..., nodeN }, traversing the tree
	for (int i = 1; i < parameterMask.length; i++) {
	    // traverse the node tree until the parameter instance is fully realized
	    node = node.getNode(bindings[parameterMask[i]]);
	}
	return node;
    }

}
