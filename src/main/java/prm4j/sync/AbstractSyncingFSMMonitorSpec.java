package prm4j.sync;

import static java.util.Collections.unmodifiableSet;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;


import prm4j.api.fsm.*;
import prm4j.api.*;
import prm4j.spec.*;
import prm4j.indexing.BaseMonitorState;
import prm4j.indexing.realtime.BaseMonitor;
import prm4j.indexing.realtime.DefaultParametricMonitor;
import prm4j.indexing.realtime.StatefulMonitor;

/**
 * An abstract monitor template for a <i>syncing monitor</i>. Such a monitor may, for performance reasons
 * skip monitoring a certain number of events. Instead of dispatching those skipped events to the respective
 * monitor(s), the template instead gathers some summary information. At some point the template then
 * decides to start monitoring again, it <i>synchronizes</i>. In that case, the template transitions based
 * on the symbol of that monitored event and on the summary information computed for the gap that was skipped.
 *
 * The purpose of this template is to convert a monitor template for a regular FSM property into a template
 * that is capable of synchronizing. To implement the correct semantics, the alphabet, state set and transition
 * relation of the resulting automaton are expanded: transitions happen not just based on symbols but
 * based on a pair of symbol and the gap abstraction.  
 * 
 * The implementation of this class is independent of the particular abstraction used for modeling the
 * summary information. The abstraction must simply subclass {@link SymbolMultisetAbstraction}, implementing
 * equals and hashCode methods. The abstraction must, however, be independent of the order in which the skipped
 * events occur. Otherwise the algorithm used to create the transitions in question may be incorrect. 
 * 
 * @param <L> The type of labels used at transitions.
 * @param <K> The type of keys used in {@link IVariableBinding}s.
 * @param <V> The type of values used in {@link IVariableBinding}s.
 * @param <A> The type of abstraction used to model the summary information at monitoring gaps. The summary
 *            information must not contain any information about the order of skipped events, i.e.,
 *            abstraction(a b)=abstraction(b a) must hold for all events a,b.
 */
public abstract class AbstractSyncingFSMMonitorSpec<L, A extends AbstractSyncingFSMMonitorSpec<L, A>.SymbolMultisetAbstraction> extends FSMSpec
//extends OpenFSMMonitorTemplate<AbstractSyncingFSMMonitorTemplate<L,K,V,A>.AbstractionAndSymbol, K, V>{
{
    protected Alphabet<L> alphabet;	// Rahul
    protected int nextStateNum = 0; // Rahul
	
	
	/**
	 * The empty multi-set of symbols.
	 */
	private final ImmutableMultiset<Symbol<L>> EMPTY = ImmutableMultiset.<Symbol<L>>of();

	
	/**
	 * The monitor template this syncing monitor template is based on.
	 */
	//protected final OpenFSMMonitorTemplate<L,K, V> delegate;
	
	/**
	 * A mapping from states sets of the delegate to a compound state of this monitor template that represents
	 * the state set.
	 */
	protected final Map<Set<FSMState<L>>,FSMState<AbstractionAndSymbol>> stateSetToCompoundState = new HashMap<Set<FSMState<L>>, FSMState<AbstractionAndSymbol>>();
	
	/**
	 * A mapping used to record a transition relation over compound states, i.e., sets of states
	 * of the original automaton.
	 */
	protected final Map<Set<FSMState<L>>,Map<Symbol<AbstractionAndSymbol>,Set<FSMState<L>>>> transitions =
		new HashMap<Set<FSMState<L>>, Map<Symbol<AbstractionAndSymbol>,Set<FSMState<L>>>>();
	
	/**
	 * The multiset of skipped events.
	 */
	protected Multiset<Symbol<L>> skippedSymbols = HashMultiset.create();
	
	/**
	 * The intersection of the variable bindings of skipped events.
	 */
	// Is it required?
	//protected IVariableBinding<K, V> intersectionOfSkippedBindings = new VariableBinding<K, V>();
	
	/**
	 * The number of times we reenabled monitoring.
	 */
	protected long reenableTime;
	
	/**
	 * Boolean stating whether we monitored the last event.
	 */
	protected boolean didMonitorLastEvent = true;
	
	
	// Is it reuired?
	//@SuppressWarnings("serial")
	/*protected final IVariableBinding<K, V> INCOMPATIBLE_BINDING = new VariableBinding<K, V>() {
		public boolean isCompatibleWith(IVariableBinding<K,V> other) { return false; };
	};*/
	
	/**
	 * We use this random source to produce coin flips that tell us whether to turn sampling
	 * on or off during any given sampling period. We use a deterministic seed for
	 * reproducibility. 
	 */
	protected final Random random = new Random(0L);
	
	/**
	 * The length of any sampling period.
	 */
	protected final int samplingPeriod;

	/**
	 * The length of any skip period.
	 */
	protected final int skipPeriod;

	/**
	 * The current phase of the sampling period, where 0 is the start of the period.
	 */
	protected int phase;
	
	/**
	 * This boolean tells us whether or not we will process events in the current period.
	 */
	protected boolean processEventsInCurrentPeriod;
	
	protected final Set<Symbol<L>> criticalSymbols;
	
	/**
	 * @param delegate The monitor template this syncing monitor template is based on. The template will remain unmodified.
	 * @param max The maximal number of skipped events.
	 */
	//public AbstractSyncingFSMMonitorTemplate(OpenFSMMonitorTemplate<L, K, V> delegate) {
	public AbstractSyncingFSMMonitorSpec(L l, FSM fsm) 	{
		super(fsm);
		this.samplingPeriod = samplingPeriod();
		this.skipPeriod = (int) (1.0d/samplingRate() - 1) * samplingPeriod; 
		this.criticalSymbols = criticalSymbols();
		alphabet = fsm.getAlphabet();	// Rahul
		this.initialState = setupStatesAndTransitions();
	}

	
	/*protected void initialize() {
		alphabet = createAlphabet(); 
		//emptyBinding = createEmptyBinding();
		fillAlphabet(alphabet);
		//indexingStrategy = createIndexingStrategy();
		//initialized = true; // Don't think we need this
	}*/
    
	public Symbol<L> getSymbolByLabel(L label) {
		return alphabet.getSymbolByLabel(label);
	}
    
    public Set<BaseEvent> getBaseEvents() {
	return baseEvents;
    }

   
    public Set<BaseMonitorState> getStates() {
	return states;
    }

    public BaseMonitorState getInitialState() { // Rahul
    //public State<L> getInitialState() {
	return initialState;
    }

    public BaseMonitor getInitialMonitor() {
	return new StatefulMonitor(getInitialState());
    }

    public Set<Parameter<?>> getFullParameterSet() {
	return parameters;
    }
    
	public Alphabet<L> getAlphabet() {	// Rahul
		return alphabet;
	}
	
	/*protected void fillAlphabet(Alphabet<L> alphabet) {
		//alphabet is instead filled on demand
	}*/

	
	
	
	/**
	 * This methods implements the algorithm at the core of this monitor template. The algorithm creates
	 * transitions of the form (symbol,abstraction) where different abstractions of gaps during monitoring
	 * are possible. The algorithm uses two worklists, one for state sets of the delegate (which will become
	 * states in this automaton), and one for multisets of skipped symbols. For each reachable state set
	 * the algorithm computes all possible successor state sets under an expanded transition relation. This
	 * transition relation takes into account the abstractions of all possible multisets of skipped events
	 * up to {@link AbstractSyncingFSMMonitorSpec#MAX}. 
	 */
	
	protected FSMState<AbstractionAndSymbol> setupStatesAndTransitions() {
		//IAlphabet<L, K> alphabet = delegate.getAlphabet(); Rahul
		//Alphabet<L> alphabet = delegate.getAlphabet();
		Alphabet<L> alphabet = getAlphabet();

		
		Set<Set<FSMState<L>>> worklist = new HashSet<Set<FSMState<L>>>();
		//worklist.add(Collections.singleton((FSMState<L>)delegate.getInitialState()));
		worklist.add(Collections.singleton((FSMState<L>)getInitialState()));

		
		Set<Set<FSMState<L>>> statesVisited = new HashSet<Set<FSMState<L>>>();		
				
		while(!worklist.isEmpty()){
			//pop some element
			Iterator<Set<FSMState<L>>> iter = worklist.iterator();
			Set<FSMState<L>> currentStates = iter.next();
			iter.remove();
			
			//have visited current set of states; to terminate, don't visit again
			statesVisited.add(currentStates);

			//create a work list for abstractions of multisets of skipped symbols;
			//starting with the abstraction of the empty multiset
			Set<A> worklistSyms = new HashSet<A>();
			worklistSyms.add(abstraction(EMPTY)); //add empty multiset
			
			//this maps an abstraction of a gap info to all the states reachable through this gap
			//info (and any symbol) 
			Map<A,Set<FSMState<L>>> abstractionToStates = new HashMap<A, Set<FSMState<L>>>();
			A emptyAbstraction = abstraction(EMPTY);
			abstractionToStates.put(emptyAbstraction, currentStates);
			
			while(!worklistSyms.isEmpty()) {
				//pop entry off symbols worklist
				Iterator<A> symsIter = worklistSyms.iterator();
				A abstraction = symsIter.next();
				symsIter.remove();

				//compute abstraction for symbols and the set of states reachable by the abstraction
				Set<FSMState<L>> frontier = abstractionToStates.get(abstraction);
				
				//this set is used to register all newly computed successor state sets
				//it is important that this be an identity hash set because the contents of the element sets can change
				//during the course of the remaining algorithm
				Set<Set<FSMState<L>>> newStateSets = Collections.newSetFromMap(new IdentityHashMap<Set<FSMState<L>>, Boolean>());
				
				for (Symbol<L> sym : alphabet) {
					//compute successors of the current state set under sym
					Set<FSMState<L>> symSuccs = new HashSet<FSMState<L>>();
					for(FSMState<L> curr : frontier) {
						FSMState<L> succ = (FSMState<L>) curr.getSuccessor(sym);
						if(succ!=null)
							symSuccs.add(succ);
					}
					if(!symSuccs.isEmpty()) {						
						boolean subsumed = transitionForSymbolAlreadyExists(currentStates,sym,symSuccs);
						
						
						if(!subsumed) {
							//create label for new transition: (abstraction,sym)
							Symbol<AbstractionAndSymbol> compoundSymbol = (Symbol<AbstractionAndSymbol>)getSymbolByLabel((L)(new AbstractionAndSymbol(abstraction, sym)));
							//register possible target states under that transition
							Set<FSMState<L>> newTargets = addTargetStatesToTransition(currentStates, compoundSymbol, symSuccs);
							//register the new state set so that we can later-on add it to the worklist
							newStateSets.add(newTargets);
							
							A newAbstraction = abstraction.add(sym);
							worklistSyms.add(newAbstraction);
							Set<FSMState<L>> old = abstractionToStates.get(newAbstraction);
							if(old==null) {
								old = new HashSet<FSMState<L>>();
								abstractionToStates.put(newAbstraction, old);
							} 
							old.addAll(symSuccs);
							//for abstracted events we do not know whether they happened on the same object
							//or not; hence we could also have stayed in the same set of states
							old.addAll(frontier);
						}
					}
				}
				
				//push all newly discovered state sets not yet processed onto the worklist
				for (Set<FSMState<L>> states : newStateSets) {
					if(!statesVisited.contains(states)) {
						worklist.add(states);
					}
				}
			} 
			
		}
				
		createTransitions();
		
		//return stateFor(Collections.singleton((FSMState<L>)delegate.getInitialState()));
		return stateFor(Collections.singleton((FSMState<L>)getInitialState()));

	}
	
	/*protected Alphabet<AbstractSyncingFSMMonitorTemplate<L,K,V,A>.AbstractionAndSymbol> createAlphabet() {
		super.createAlphabet();
		return new DynamicAlphabet<AbstractSyncingFSMMonitorTemplate<L,K,V,A>.AbstractionAndSymbol, K>();
	}*/ // Is it really required? Check with Eric.
	
	private boolean transitionForSymbolAlreadyExists(Set<FSMState<L>> currentStates, Symbol<L> symbol, Set<FSMState<L>> symSuccs) {
		Map<Symbol<AbstractionAndSymbol>, Set<FSMState<L>>> symbolToTargets = transitions.get(currentStates);
		if(symbolToTargets==null) return false;
		for(Map.Entry<Symbol<AbstractionAndSymbol>, Set<FSMState<L>>> symbolAndTargets: symbolToTargets.entrySet()) {
			Symbol<AbstractionAndSymbol> sym = symbolAndTargets.getKey();
			if(sym.getLabel().getSymbol().equals(symbol)) {
				Set<FSMState<L>> targets = symbolAndTargets.getValue();
				if(targets.equals(symSuccs)) { 
					return true;
				}
			}
		}
		return false;
	}
		

	protected Set<FSMState<L>> addTargetStatesToTransition(Set<FSMState<L>> currentStates, Symbol<AbstractionAndSymbol> symbol, Set<FSMState<L>> someTargetStates) {
		Map<Symbol<AbstractionAndSymbol>, Set<FSMState<L>>> symbolToTargets = transitions.get(currentStates);
		if(symbolToTargets==null) {
			symbolToTargets = new HashMap<Symbol<AbstractionAndSymbol>, Set<FSMState<L>>>();
			transitions.put(currentStates, symbolToTargets);
		}
		Set<FSMState<L>> targets = symbolToTargets.get(symbol);
		if(targets==null) {
			targets = new HashSet<FSMState<L>>();
			symbolToTargets.put(symbol, targets);
		} 
		targets.addAll(someTargetStates);
		return new HashSet<FSMState<L>>(targets);
 	}

	private void createTransitions() {
		for (Entry<Set<FSMState<L>>, Map<Symbol<AbstractionAndSymbol>, Set<FSMState<L>>>> sourceAndMap : transitions.entrySet()) {
			Set<FSMState<L>> source = sourceAndMap.getKey();
			for (Entry<Symbol<AbstractionAndSymbol>, Set<FSMState<L>>> symbolAndTargetStates : sourceAndMap.getValue().entrySet()) {
				Symbol<AbstractionAndSymbol> compoundSymbol = symbolAndTargetStates.getKey();
				Set<FSMState<L>> targetStates = symbolAndTargetStates.getValue();
				stateFor(source).addTransition(compoundSymbol, stateFor(targetStates));
			}			
		}
		transitions.clear(); //free space
	}

	protected abstract A abstraction(Multiset<Symbol<L>> symbols);

	private FSMState<AbstractionAndSymbol> stateFor(Set<FSMState<L>> set) {
		FSMState<AbstractionAndSymbol> compoundState = stateSetToCompoundState.get(set);
		if(compoundState==null) {
			boolean isFinal = true;
			for (FSMState<L> state : set) {
				if(!state.isFinal() ){ 
					isFinal = false;
					break;
				}
			}			
			compoundState = makeState(isFinal);
			System.err.println(compoundState+" - "+set);
			stateSetToCompoundState.put(set, compoundState);
		}
		return compoundState;
	}
	
	public Set<Parameter<?>> getParameters() {
		return parameters;
	}

	/*@Override
	protected IIndexingStrategy<AbstractionAndSymbol, K, V> createIndexingStrategy() {
		//TODO can we somehow choose the strategy based on the one used for the delegate?
		return new StrategyB<DefaultFSMMonitor<AbstractionAndSymbol>, AbstractionAndSymbol, K, V>(this); 
	}*/ // Rahul: Not required?

	//@Override
	/*protected void fillAlphabet(Alphabet<AbstractionAndSymbol> alphabet) {
		//alphabet is instead filled on demand
	}*/ // Rahul: Not required?
	
	/**
	 * Maybe processes the event consisting of the symbol and bindings.
	 * Whether or not the event is processed depends on the return value of
	 * the predicate {@link #shouldMonitor(ISymbol, IVariableBinding, Multiset)}.
	 * @param symbolLabel the current event's symbol's label
	 * @param binding the current events's binding
	 */
	// public void maybeProcessEvent(L symbolLabel, IVariableBinding<K,V> binding) {
	public void maybeProcessEvent(L symbolLabel, Event e) {
		//Symbol<L> symbol = delegate.getAlphabet().getSymbolByLabel(symbolLabel);
		Symbol<L> symbol = getAlphabet().getSymbolByLabel(symbolLabel);
		//if(shouldMonitor(symbol,binding,skippedSymbols)) {
		if(shouldMonitor(symbol)) {
			if(!didMonitorLastEvent) reenableTime++;
			processEvent((L)(new AbstractionAndSymbol(abstraction(skippedSymbols), symbol)), e);
			skippedSymbols.clear();
			didMonitorLastEvent = true;
		} else {
			skippedSymbols.add(symbol);
			didMonitorLastEvent = false;
		}
	}
	
	/*public synchronized void processEvent(L label, Event e){
		//assert alphabet.variables().containsAll(binding.keySet()):
			//"Event has undefined variables: "+binding+" vs. "+alphabet;
		//assert initialized : "not initialized!";
		
		getIndexingStrategy().processEvent(getSymbolByLabel(label), e);
	}*/
	
	/**
	 * Returns the lenght of a sampling period for this monitor template. This size may actually depend on the 
	 * size or structure of the delegate, i.e., the property to be monitored.
	 */
	abstract protected int samplingPeriod();

	/**
	 * This is the rate at which we sample, given as a number from 0 to 1.
	 */
	abstract protected double samplingRate();
	
	/**
	 * Returns the set of critical symbols for the monitored property. Such symbols will always be monitored,
	 * no matter whether or not sampling is enabled.
	 */
	abstract protected Set<Symbol<L>> criticalSymbols();

	/**
	 * Determines whether the current event should be monitored.
	 * @param symbol the current event's symbol
	 * @param binding the current events's binding
	 * @param skippedSymbols the multiset of symbols of events skipped so far
	 * @return 
	 */
	//protected boolean shouldMonitor(Symbol<L> symbol, IVariableBinding<K, V> binding, Multiset<ISymbol<L, K>> skippedSymbols) { // Rahul
	protected boolean shouldMonitor(Symbol<L> symbol){ // Rahul changed it
		if(phase==0) {
			processEventsInCurrentPeriod = random.nextBoolean();
		}
		int periodLength = processEventsInCurrentPeriod ? samplingPeriod : skipPeriod;
		phase = (phase+1) % periodLength;
		return processEventsInCurrentPeriod || criticalSymbols.contains(symbol);
	}

	
	public class AbstractionAndSymbol {
		private final A abstraction;
		private final Symbol symbol;
		public AbstractionAndSymbol(A abstraction, Symbol symbol) {
			this.abstraction = abstraction;
			this.symbol = symbol;
		}
		public A getAbstraction() {
			return abstraction;
		}
		public Symbol getSymbol() {
			return symbol;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((abstraction == null) ? 0 : abstraction.hashCode());
			result = prime * result
					+ ((symbol == null) ? 0 : symbol.hashCode());
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
			AbstractionAndSymbol other = (AbstractionAndSymbol) obj;
			if (abstraction == null) {
				if (other.abstraction != null)
					return false;
			} else if (!abstraction.equals(other.abstraction))
				return false;
			if (symbol == null) {
				if (other.symbol != null)
					return false;
			} else if (!symbol.equals(other.symbol))
				return false;
			return true;
		}
		@Override
		public String toString() {
			return "<"+abstraction+";"+symbol+">";
		}
	}
	
	//@Override
	public SyncFSMMonitor createMonitorPrototype() {
		return new SyncFSMMonitor((FSMState)getInitialState());
	}
	
	/*@Override
	public IVariableBinding<K, V> createEmptyBinding() {
		return delegate.createEmptyBinding();
	}*/ // Rahul: Not required?
	
	//@Override
	protected FSMState<AbstractionAndSymbol> makeState(boolean isFinal) {
		//return new SyncState(getAlphabet(),isFinal,Integer.toString(nextStateNum++));
		return new SyncState((Alphabet<AbstractionAndSymbol>)getAlphabet(),isFinal, nextStateNum++);
	}
	
	public class SyncState extends FSMState<AbstractionAndSymbol> {
		
		//Map<ISymbol<L, K>,A> symToMaxAbstraction = new HashMap<ISymbol<L, K>,A>(); //Rahul
		Map<Symbol<L>,A> symToMaxAbstraction = new HashMap<Symbol<L>,A>();
		//Map<ISymbol<L, K>,ISymbol<AbstractionAndSymbol, ?>> symToMaxSymbol = new HashMap<ISymbol<L, K>,ISymbol<AbstractionAndSymbol, ?>>(); // Rahul
		Map<Symbol<L>,Symbol<AbstractionAndSymbol>> symToMaxSymbol = new HashMap<Symbol<L>,Symbol<AbstractionAndSymbol>>();

		//public SyncState(IAlphabet<AbstractionAndSymbol, ?> alphabet, boolean isFinal, String label) { Rahul
		public SyncState(Alphabet<AbstractionAndSymbol> alphabet, boolean isFinal, int label) {
			//super(alphabet, isFinal, label);
			super(label, alphabet, isFinal, null, Integer.toString(label));
		}
		
		//@Override Rahul
		public FSMState<AbstractionAndSymbol> successor(Symbol<AbstractionAndSymbol> sym) {
			A max = symToMaxAbstraction.get(sym.getLabel().getSymbol());
			if(max!=null && max.isSmallerOrEqualThan(sym.getLabel().getAbstraction())) {
				sym = symToMaxSymbol.get(sym.getLabel().getSymbol());
			}
			return (FSMState<AbstractionAndSymbol>)super.getSuccessor(sym);
		}
		
		@Override //Rahul
		//public void addTransition(ISymbol<AbstractionAndSymbol, ?> sym, State<AbstractionAndSymbol> succ) { // Rahul
		public void addTransition(Symbol<AbstractionAndSymbol> sym, FSMState<AbstractionAndSymbol> succ) {
			super.addTransition(sym, succ);
			A abstraction = sym.getLabel().getAbstraction();
			//ISymbol<L, K> symbol = sym.getLabel().getSymbol(); //Rahul
			Symbol<L> symbol = sym.getLabel().getSymbol();
			symToMaxAbstraction.put(symbol,abstraction);
			symToMaxSymbol.put(symbol,(Symbol<AbstractionAndSymbol>)getAlphabet().getSymbolByLabel((L) new AbstractionAndSymbol(abstraction, sym.getLabel().getSymbol())));
		}
		
	}

	public class SyncFSMMonitor<L> extends DefaultParametricMonitor {

		protected long lastAccess;
		protected FSMState<L> currentState;
		
		public SyncFSMMonitor(FSMState<L> initialState) {
			super(initialState);
			this.currentState = initialState;
		}
		
		public void processEvent(Event e) {
			//as input we get a syncing symbol; now we must check whether we actually need
			//to sync; if not, then we modify the symbol to a non-syncing one
			
			if(reenableTime==lastAccess) {
				//don't sync
				s = getAlphabet().getSymbolByLabel((L)new AbstractionAndSymbol(abstraction(EMPTY), s.getLabel().getSymbol()));
			} else {
				lastAccess = reenableTime;
			}
						
			super.processEvent(e);
		}
	}

	public abstract class SymbolMultisetAbstraction {		
		public abstract int hashCode();

		public abstract boolean equals(Object obj);		
		
		protected abstract A add(Symbol<L> sym);
		
		protected abstract boolean isSmallerOrEqualThan(A other);
	}

}