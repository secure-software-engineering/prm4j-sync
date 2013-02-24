package prm4j.sync;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Multiset;

import prm4j.api.fsm.*;
import prm4j.api.*;

/**
 * An {@link AbstractSyncingFSMMonitorSpec} that models the gap of events missed by the set of symbols that were missed.
 */
public class SymbolSetSyncingSpec<L>
	extends DefaultSyncingFSMMonitorSpec<L, SymbolSetSyncingSpec<L>.AbstractionBySymbolSet>{
		
	//public SymbolSetSyncingTemplate(L l, AbstractSyncingFSMMonitorTemplate<SymbolSetSyncingTemplate<L>.AbstractionBySymbolSet> delegate, FSM fsm) {
	public SymbolSetSyncingSpec(L l, FSM fsm) {
		super(l, fsm);
	}
	
	//public SymbolSetSyncingTemplate(FSM fsm) {
		//super(fsm);
	//}
	
	protected AbstractionBySymbolSet abstraction(Multiset<Symbol<L>> symbols) {
		return new AbstractionBySymbolSet(symbols.elementSet());
	}

	public class AbstractionBySymbolSet
		extends AbstractSyncingFSMMonitorSpec<L,AbstractionBySymbolSet>.SymbolMultisetAbstraction {

		private final Set<Symbol<L>> symbols;

		protected AbstractionBySymbolSet(Set<Symbol<L>> symbols) {
			this.symbols = symbols;
		}
		
		@Override
		public String toString() {
			return symbols.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((symbols == null) ? 0 : symbols.hashCode());
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
			AbstractionBySymbolSet other = (AbstractionBySymbolSet) obj;
			if (symbols == null) {
				if (other.symbols != null)
					return false;
			} else if (!symbols.equals(other.symbols))
				return false;
			return true;
		}

		@Override
		protected AbstractionBySymbolSet add(Symbol<L> sym) {
			HashSet<Symbol<L>> copy = new HashSet<Symbol<L>>(symbols);
			copy.add(sym);
			return new AbstractionBySymbolSet(copy);
		}

		@Override
		protected boolean isSmallerOrEqualThan(AbstractionBySymbolSet other) {
			return other.symbols.containsAll(symbols);
		}
	}	
}