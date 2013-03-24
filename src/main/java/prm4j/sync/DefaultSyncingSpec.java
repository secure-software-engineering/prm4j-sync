package prm4j.sync;

import prm4j.api.fsm.*;
public abstract class DefaultSyncingSpec<L, A extends AbstractSyncingSpec<L, A>.SymbolMultisetAbstraction> extends AbstractSyncingSpec<L, A> {

	public DefaultSyncingSpec(FSMSpec<L> delegate){
		super(delegate);
	}

	@Override
	protected int samplingPeriod() {
		System.out.println("States size: " + getStates().size());
		return getStates().size()+4;
	}

	@Override
	protected double samplingRate() {
		return samplingRate;
	}
}
