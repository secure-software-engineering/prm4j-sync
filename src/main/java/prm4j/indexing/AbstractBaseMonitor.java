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
package prm4j.indexing;

import prm4j.indexing.treebased.LowLevelBinding;


/**
 * A concrete monitor instance, representing the internal state of a {@link ParametricMonitor} for one single concrete
 * variable binding.
 *
 * @param <E>
 *            the type of base event processed by monitors
 */
public abstract class AbstractBaseMonitor<E> implements BaseMonitor<E, AbstractBaseMonitor<E>> {

    // low level access
    private LowLevelBinding<E>[] bindings;
    // low level access
    private long tau;

    /**
     * Creates a low level deep copy of this monitor.
     *
     * @param bindings
     * @return
     */
    public final AbstractBaseMonitor<E> copy(LowLevelBinding<E>[] bindings) {
	AbstractBaseMonitor<E> copy = copy();
	copy.setBindings(bindings);
	copy.setTau(tau);
	return copy;
    }

    private final void setBindings(LowLevelBinding<E>[] bindings) {
	this.bindings = bindings;
    }

    public final LowLevelBinding<E>[] getLowLevelBindings() {
	return bindings;
    }

    protected final prm4j.indexing.Binding[] getBindings() {
	// upcast
	return bindings;
    }

    public final long getTau() {
	return tau;
    }

    final void setTau(long tau) {
	this.tau = tau;
    }

}
