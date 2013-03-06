package prm4j.sync;

import java.util.Collections;
import java.util.Set;

import prm4j.api.fsm.*;
import prm4j.api.*;
public abstract class DefaultSyncingSpec<L, A extends AbstractSyncingSpec<L, A>.SymbolMultisetAbstraction> extends AbstractSyncingSpec<L, A> {

	//public DefaultSyncingFSMMonitorTemplate(AbstractSyncingFSMMonitorTemplate<L, A> delegate)
	/*public DefaultSyncingSpec(FSM fsm){
		super(fsm);
	}*/
	
	public DefaultSyncingSpec(FSMSpec<L> delegate){
		super(delegate);
	}

	@Override
	protected int samplingPeriod() {
		return getStates().size()+2;
	}

	@Override
	protected double samplingRate() {
		return 0.5d;
	}

	@Override
	protected Set<Symbol<L>> criticalSymbols() {
		return Collections.emptySet();
	}
}
