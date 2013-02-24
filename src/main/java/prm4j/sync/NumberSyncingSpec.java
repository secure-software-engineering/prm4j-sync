package prm4j.sync;

import com.google.common.collect.Multiset;

import prm4j.api.fsm.*;
import prm4j.api.*;

/**
 * An {@link AbstractSyncingFSMMonitorSpec} that models the gap of events missed by the number
 * of missed events.
 */
public class NumberSyncingSpec<L>
	extends DefaultSyncingFSMMonitorSpec<L, NumberSyncingSpec<L>.AbstractionBySize>{
		
	/*public NumberSyncingTemplate(OpenFSMMonitorTemplate<L, K, V> delegate) {
		super(delegate);
	}*/
	
	public NumberSyncingSpec(L l, FSM fsm) {
		super(l, fsm);
	}
	
	protected AbstractionBySize abstraction(Multiset<Symbol<L>> symbols) {
		return new AbstractionBySize(symbols.size());
	}

	public class AbstractionBySize
		extends AbstractSyncingFSMMonitorSpec<L,AbstractionBySize>.SymbolMultisetAbstraction {

		private final int size;

		protected AbstractionBySize(int size) {
			this.size = size;
		}
		
		@Override
		public String toString() {
			return Integer.toString(size);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + size;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			@SuppressWarnings("unchecked")
			AbstractionBySize other = (AbstractionBySize) obj;
			if (size != other.size)
				return false;
			return true;
		}

		@Override
		protected AbstractionBySize add(Symbol<L> sym) {
			return new AbstractionBySize(size+1);
		}

		@Override
		protected boolean isSmallerOrEqualThan(AbstractionBySize other) {
			return other.size>=size;
		}
		
	}
		
}