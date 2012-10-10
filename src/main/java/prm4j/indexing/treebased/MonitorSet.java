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
package prm4j.indexing.treebased;

import prm4j.indexing.AbstractBaseMonitor;
import prm4j.indexing.Event;

/**
 * @param <E>
 *            the type of base event processed by monitors
 */
public interface MonitorSet<E> {

    public void add(AbstractBaseMonitor<E> monitor);

    public void processEvent(Event<E> event);

    public MonitorSetIterator<E> getIterator();

}
