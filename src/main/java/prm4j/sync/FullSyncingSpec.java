package prm4j.sync;

import com.google.common.collect.Multiset;

import prm4j.api.fsm.*;
import prm4j.api.*;
/**
 * An {@link AbstractSyncingSpec} that models the gap of events missed by the number
 * of missed events.
 */
public class FullSyncingSpec<L>
	extends DefaultSyncingSpec<L, FullSyncingSpec<L>.FullAbstraction>{
		
	
	public FullSyncingSpec(FSMSpec<L> delegate) {
		super(delegate);
	}
	
	protected FullAbstraction abstraction(Multiset<Symbol<L>> symbols) {
		return new FullAbstraction(!symbols.isEmpty());
	}
	
	
	public class FullAbstraction
	extends AbstractSyncingSpec<L, FullAbstraction>.SymbolMultisetAbstraction {

		private final boolean skippedSomething;

		protected FullAbstraction(boolean skippedSomething) {
			this.skippedSomething = skippedSomething;
		}
		
		@Override
		public String toString() {
			return skippedSomething ? "*" : "{}";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (skippedSomething ? 1231 : 1237);
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FullAbstraction other = (FullAbstraction) obj;
			if (skippedSomething != other.skippedSomething)
				return false;
			return true;
		}

		@Override
		protected FullAbstraction add(Symbol<L> sym) {
			//something was added, so we certainly skipped *something*
			return new FullAbstraction(true);
		}

		@Override
		protected boolean isSmallerOrEqualThan(FullAbstraction other) {
			return !skippedSomething;
		}
		
	}
		
}