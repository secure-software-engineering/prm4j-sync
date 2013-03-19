package prm4j.sync;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import prm4j.api.fsm.*;
import prm4j.api.*;
/**
 * An {@link AbstractSyncingSpec} that models the gap of events missed as a multiset of missed events.
 */
public class MultisetSyncingSpec<L>
	extends DefaultSyncingSpec<L, MultisetSyncingSpec<L>.AbstractionAsMultiset>{
		
	public MultisetSyncingSpec(FSMSpec<L> delegate) {
		super(delegate);
	}
	
	
	protected AbstractionAsMultiset abstraction(Multiset<Symbol<L>> symbols) {
		return new AbstractionAsMultiset(symbols);
	}
	

	public class AbstractionAsMultiset
		extends AbstractSyncingSpec<L, AbstractionAsMultiset>.SymbolMultisetAbstraction {

		private final Multiset<Symbol<L>> symbols;

		public AbstractionAsMultiset(Multiset<Symbol<L>> symbols) {
			this.symbols = symbols;
		}
		

		public Multiset<Symbol<L>> getSymbols() {
			return symbols;
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
			AbstractionAsMultiset other = (AbstractionAsMultiset) obj;
			if (symbols == null) {
				if (other.symbols != null)
					return false;
			} else if (!symbols.equals(other.symbols))
				return false;
			return true;
		}

		@Override
		protected AbstractionAsMultiset add(Symbol<L> sym) {
			Multiset<Symbol<L>> copy = HashMultiset.create(symbols);
			copy.add(sym);
			return new AbstractionAsMultiset(copy);
		}

		protected boolean isSmallerOrEqualThan(AbstractionAsMultiset other) {
			return other.symbols.containsAll(symbols);
		}
	}	
}