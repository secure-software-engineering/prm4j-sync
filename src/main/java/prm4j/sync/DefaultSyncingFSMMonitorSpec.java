package prm4j.sync;

import java.util.Collections;
import java.util.Set;

import prm4j.api.fsm.*;
import prm4j.api.*;
public abstract class DefaultSyncingFSMMonitorSpec<L, A extends AbstractSyncingFSMMonitorSpec<L, A>.SymbolMultisetAbstraction> extends AbstractSyncingFSMMonitorSpec<L, A> {

	//public DefaultSyncingFSMMonitorTemplate(AbstractSyncingFSMMonitorTemplate<L, A> delegate)
	public DefaultSyncingFSMMonitorSpec(L l, FSM fsm){
		super(l, fsm);
	}

	@Override
	protected int samplingPeriod() {
		return getStates().size()+2;
	}

	@Override
	protected double samplingRate() {
		return 0.2d;
	}

	@Override
	//protected Set<ISymbol<L, K>> criticalSymbols() {
	protected Set<Symbol<L>> criticalSymbols() {
		return Collections.emptySet();
	}
}
