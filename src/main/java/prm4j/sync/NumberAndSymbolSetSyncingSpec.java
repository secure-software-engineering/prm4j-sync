package prm4j.sync;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Multiset;

import prm4j.api.fsm.*;
import prm4j.api.*;


/**
 * An {@link AbstractSyncingSpec} that models the gap of events missed by the number
 * and kind of missed events.
 */
public class NumberAndSymbolSetSyncingSpec<L>
	extends DefaultSyncingSpec<L, NumberAndSymbolSetSyncingSpec<L>.AbstractionBySizeAndSymbols>{
		
	public NumberAndSymbolSetSyncingSpec(FSMSpec<L> delegate) {
		super(delegate);
	}
	

	protected AbstractionBySizeAndSymbols abstraction(Multiset<Symbol<L>> symbols) {
		return new AbstractionBySizeAndSymbols(symbols.size(),symbols.elementSet());
	}
	

	public class AbstractionBySizeAndSymbols
		extends AbstractSyncingSpec<L,AbstractionBySizeAndSymbols>.SymbolMultisetAbstraction {

		private final int size;
		private final Set<Symbol<L>> symbols;

		protected AbstractionBySizeAndSymbols(int size, Set<Symbol<L>> symbols) {
			this.size = size;
			this.symbols = symbols;
		}
		

		@Override
		public String toString() {
			return "("+size+","+symbols+")";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + size;
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
			AbstractionBySizeAndSymbols other = (AbstractionBySizeAndSymbols) obj;
			if (size != other.size)
				return false;
			if (symbols == null) {
				if (other.symbols != null)
					return false;
			} else if (!symbols.equals(other.symbols))
				return false;
			return true;
		}

		@Override
		protected AbstractionBySizeAndSymbols add(Symbol<L> sym) {
			HashSet<Symbol<L>> copy = new HashSet<Symbol<L>>(symbols);
			copy.add(sym);
			return new AbstractionBySizeAndSymbols(size+1, copy);
		}

		@Override
		protected boolean isSmallerOrEqualThan(AbstractionBySizeAndSymbols other) {
			return other.symbols.containsAll(symbols) && other.size>=size;
		}

		@Override
		protected AbstractionBySizeAndSymbols abstractionExcludingSymbols(Set<Symbol<L>> syms){
			HashSet<Symbol<L>> copy = new HashSet<Symbol<L>>(symbols);
			copy.removeAll(syms);			
			return new AbstractionBySizeAndSymbols(size, copy);
		}
		
	}	
}