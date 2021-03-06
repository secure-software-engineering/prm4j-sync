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
package prm4j.indexing.realtime;

import prm4j.indexing.staticdata.MetaNode;

public class DirectNodeStore implements NodeStore {

    private final MetaNode metaTree;

    public DirectNodeStore(MetaNode metaTree, NodeManager nodeManager) {
	this.metaTree = metaTree;
	metaTree.setNodeManagerToTree(nodeManager);
    }

    @Override
    public Node getOrCreateNode(LowLevelBinding[] bindings) {
	if (bindings.length > 0) {
	    Node node = getNode(bindings[0], 0);
	    // we iterate over the rest { node1 , ..., nodeN }, traversing the tree
	    for (int i = 1; i < bindings.length; i++) {
		// traverse the node tree until the parameter instance is fully realized
		node = node.getOrCreateNode(i, bindings[i]);
	    }
	    return node;
	}
	return NullNode.instance;
    }

    @Override
    public Node getOrCreateNode(LowLevelBinding[] bindings, int[] parameterMask) {
	if (parameterMask.length > 0) {
	    Node node = getNode(bindings[parameterMask[0]], parameterMask[0]);
	    // we iterate over the rest { node1 , ..., nodeN }, traversing the tree
	    for (int i = 1; i < parameterMask.length; i++) {
		// traverse the node tree until the parameter instance is fully realized
		node = node.getOrCreateNode(parameterMask[i], bindings[parameterMask[i]]);
	    }

	    return node;
	}
	return NullNode.instance;
    }

    @Override
    public Node getNode(LowLevelBinding[] bindings) {
	if (bindings.length > 0) {
	    Node node = getNode(bindings[0], 0);
	    // we iterate over the rest { node1 , ..., nodeN }, traversing the tree
	    for (int i = 1; i < bindings.length; i++) {
		// traverse the node tree until the parameter instance is fully realized
		node = node.getNode(i, bindings[i]);
		if (node == null) {
		    return NullNode.instance;
		}
	    }
	    return node;
	}
	return NullNode.instance;
    }

    @Override
    public Node getNode(LowLevelBinding[] bindings, int[] parameterMask) {
	if (parameterMask.length > 0) {
	    Node node = getNode(bindings[parameterMask[0]], parameterMask[0]);
	    // we iterate over the rest { node1 , ..., nodeN }, traversing the tree
	    for (int i = 1; i < parameterMask.length; i++) {
		node = node.getNode(parameterMask[i], bindings[parameterMask[i]]);
		if (node == null) {
		    return NullNode.instance;
		}
	    }
	    return node;
	}
	return NullNode.instance;
    }

    public Node getRootNode() {
	return NullNode.instance;
    }

    private Node getNode(LowLevelBinding binding, int parameterIndex) {
	Node node = binding.getNode();
	if (node == null) {
	    node = metaTree.getMetaNode(parameterIndex).createNode(binding);
	    binding.setNode(node);
	}
	return node;
    }

    @Override
    public void reset() {
	// stateless
    }

}
