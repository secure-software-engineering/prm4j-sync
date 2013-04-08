package prm4j.sync;

//import static java.util.Collections.unmodifiableSet;

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
import prm4j.indexing.BaseMonitorState;
import prm4j.indexing.Monitor;
import prm4j.indexing.realtime.BaseMonitor;
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
public abstract class AbstractSyncingSpec<L, A extends AbstractSyncingSpec<L, A>.SymbolMultisetAbstraction> extends FSMSpec
<AbstractSyncingSpec<L,A>.AbstractionAndSymbol> {
    protected int nextStateNum = 0; // Rahul
    
    protected final FSMSpec<L> delegate;
    
    
    //final ParametricMonitor parametricMonitor; 
    protected ParametricMonitor parametricMonitor; 
	
	/**
	 * The empty multi-set of symbols.
	 */
	private final ImmutableMultiset<Symbol<L>> EMPTY = ImmutableMultiset.<Symbol<L>>of();

	
	/**
	 * The monitor template this syncing monitor template is based on.
	 */
	
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
	
	/**
	 * The number of times we reenabled monitoring.
	 */
	protected long reenableTime;
	
	/**
	 * Boolean stating whether we monitored the last event.
	 */
	protected boolean didMonitorLastEvent = true;
	
	static public long recordCounter = -1;
	static public long newWindowRecordCounter = -1;
	static public boolean isBase = false;
	
	
	/**
	 * We use this random source to produce coin flips that tell us whether to turn sampling
	 * on or off during any given sampling period. We use a deterministic seed for
	 * reproducibility. 
	 */
	protected final Random random;
	
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
		
	protected A maxAbstraction = abstraction(EMPTY);
	
	
	protected abstract void updateHistory(Symbol<L> sym);
	
	protected abstract void deleteHistory();
	
	protected Map<Symbol<L>,Symbol<AbstractionAndSymbol>> symbolWithEmptyAbstraction = new HashMap<Symbol<L>,Symbol<AbstractionAndSymbol>>();
	
	protected Map<Symbol<L>,Symbol<AbstractionAndSymbol>> symbolWithMaxAbstraction = new HashMap<Symbol<L>,Symbol<AbstractionAndSymbol>>();	

	
	/**
	 * @param delegate The monitor template this syncing monitor template is based on. The template will remain unmodified.
	 * @param max The maximal number of skipped events.
	 */
	public AbstractSyncingSpec(FSMSpec<L> delegate) 	{
		super(delegate);
		this.delegate = delegate;

		this.criticalSymbols = delegate.criticalSymbols();
		this.initialState = setupStatesAndTransitions();
		this.parametricMonitor = ParametricMonitorFactory.createParametricMonitor(this);
		//maxAbstraction = maxAbstraction.abstractionExcludingSymbols(criticalSymbols);
		
		this.samplingPeriod = samplingPeriod();
		System.out.println("sampling period:" + samplingPeriod);
		this.skipPeriod = (int) ((1.0d/samplingRate() - 1) * samplingPeriod); 
		System.out.println("skip period:" + skipPeriod);
		
		if(this.seed <= 0)
			random = new Random();
		else
			random = new Random(seed);
		
		printSyncSpec();
	}
	
	public void setParametricMonitor(){
		this.parametricMonitor = ParametricMonitorFactory.createParametricMonitor(this);
	}

	private void printSyncSpec(){
		System.out.println("///////////////////////////////");
		System.out.println("\nDumping FSMSpec's fsm details:");
		System.out.println("Alphabet Symbols: " + alphabet.getSymbols());
		System.out.println("Alphabet Parameters: " + alphabet.getParameters());
		System.out.println("\nSymbols: " + baseEvents);
		System.out.println("Symbol details:");
		for(BaseEvent base: baseEvents){
			@SuppressWarnings("unchecked")
			Symbol<AbstractionAndSymbol> sym = (Symbol<AbstractionAndSymbol>)base;
			System.out.println("sym label: " + sym.getLabel().getSymbol().getLabel());
			System.out.println("sym abs: " + sym.getLabel().getAbstraction());
			System.out.println("sym index: " + sym.getIndex());
			for(Parameter<?> par: sym.getParameters()){
				System.out.println("Param: " + par + ", index: " + par.getIndex());
			}			
		}
		System.out.println("\nCritical Symbol details:");
		for(Symbol<L> sym: criticalSymbols){
			System.out.println("sym label: " + sym.getLabel());
		}
		System.out.println("\nParameters: " + parameters);
		System.out.println("States: " + states);
		System.out.println("Initial state: " + initialState);
		System.out.println("Sampling rate: " + samplingRate);
		if(seed <= 0)
			System.out.println("No seed specified.");
		else
			System.out.println("Seed: " + seed);
		System.out.println("Maximum Abstraction: " + maxAbstraction);
		System.out.println("///////////////////////////////\n");

	}

	 
    public Set<BaseEvent> getBaseEvents() {
    	return baseEvents;
    }

   
    public Set<BaseMonitorState> getStates() {
    	return states;
    }

    @SuppressWarnings("unchecked")
	public FSMState<L> getInitialState() { // Rahul
    	return (FSMState<L>)initialState;
    }

    @SuppressWarnings("unchecked")
	public BaseMonitor getInitialMonitor() {
    	return new SyncFSMMonitor((FSMState<AbstractionAndSymbol>) getInitialState());
    }

    public Set<Parameter<?>> getFullParameterSet() {
    	return parameters;
    }
    
	/**
	 * Subclasses may override this method to create a custom kind of alphabet. 
	 */
	protected Alphabet<AbstractSyncingSpec<L,A>.AbstractionAndSymbol> createAlphabet() {		
		return new Alphabet<AbstractSyncingSpec<L,A>.AbstractionAndSymbol>();
	}
	
	
	
	
	/**
	 * This methods implements the algorithm at the core of this monitor template. The algorithm creates
	 * transitions of the form (symbol,abstraction) where different abstractions of gaps during monitoring
	 * are possible. The algorithm uses two worklists, one for state sets of the delegate (which will become
	 * states in this automaton), and one for multisets of skipped symbols. For each reachable state set
	 * the algorithm computes all possible successor state sets under an expanded transition relation. This
	 * transition relation takes into account the abstractions of all possible multisets of skipped events
	 * up to {@link AbstractSyncingSpec#MAX}. 
	 */
	
	@SuppressWarnings("unchecked")
	protected FSMState<AbstractionAndSymbol> setupStatesAndTransitions() {
		//System.out.println("In setupStatesAndTransitions");
		Alphabet<L> alphabet = delegate.getAlphabet();
		Set<Set<FSMState<L>>> worklist = new HashSet<Set<FSMState<L>>>();
		worklist.add(Collections.singleton(((FSMState<L>)delegate.getInitialState())));
		
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
				
				//////////////////////////
				//Rahul added following to find the maximum overall abstraction
				// which will be used to generate a skip transition for which
				// we do not have history.
				//////////////////////////
				if(maxAbstraction!=null && maxAbstraction.isSmallerOrEqualThan(abstraction)){
					//System.out.println("max Abst: " + maxAbstraction);
					//System.out.println("sym Abst: " + abstraction);
					maxAbstraction = abstraction;
				}
				//////////////////////////

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
						boolean subsumed = transitionForSymbolAlreadyExists(currentStates,sym,symSuccs, abstraction);
						if(!subsumed) {
							//create label for new transition: (abstraction,sym)
							Symbol<AbstractionAndSymbol> compoundSymbol = this.getSymbolByLabel(new AbstractionAndSymbol(abstraction, sym));

							//register possible target states under that transition
							Set<FSMState<L>> newTargets = addTargetStatesToTransition(currentStates, compoundSymbol, symSuccs);
							//register the new state set so that we can later-on add it to the worklist
							newStateSets.add(newTargets);
							
							if(!criticalSymbols.contains(sym)){//Rahul
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
							
							baseEvents.add(compoundSymbol);	// Rahul
							
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
		
		addCompoundStatesToStates();
		//printCompoundSymbols();
		//printParameters();
		
		return stateFor(Collections.singleton(((FSMState<L>)delegate.getInitialState())));

	}
	
	private void addCompoundStatesToStates(){
		for(Set<FSMState<L>> states: stateSetToCompoundState.keySet()){
			//System.out.println("Key: " + states);
			FSMState<AbstractionAndSymbol> state = stateSetToCompoundState.get(states);
			//System.out.println("Value: " + state);
			this.states.add(state);
		}
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	private void printCompoundSymbols(){
		System.out.println("Symbols description:");
		for(BaseEvent sym: this.baseEvents){
			System.out.println("\n" + "Index: " + ((Symbol<AbstractionAndSymbol>)sym).getIndex());
			System.out.println("Label: " + ((Symbol<AbstractionAndSymbol>)sym).getLabel().getSymbol().getLabel());
			System.out.println("Abstraction: " + ((Symbol<AbstractionAndSymbol>)sym).getLabel().getAbstraction());
			System.out.println("Params: " + ((Symbol<AbstractionAndSymbol>)sym).getParameters());
		}
	}
	
	@SuppressWarnings("unused")
	private void printParameters(){
		System.out.println("Parameters in the current template: " + this.getParameters());
		//System.out.println("Parameters in the current template: " + this.getFullParameterSet());
		for(Parameter<?> param: this.getParameters()){
			System.out.println("Param: " + param + " : " + param.getIndex());
		}
	}
	
	
	private boolean transitionForSymbolAlreadyExists(Set<FSMState<L>> currentStates, Symbol<L> symbol, Set<FSMState<L>> symSuccs, A abstraction) {
		Map<Symbol<AbstractionAndSymbol>, Set<FSMState<L>>> symbolToTargets = transitions.get(currentStates);
		if(symbolToTargets==null) return false;
		for(Map.Entry<Symbol<AbstractionAndSymbol>, Set<FSMState<L>>> symbolAndTargets: symbolToTargets.entrySet()) {
			Symbol<AbstractionAndSymbol> sym = symbolAndTargets.getKey();
			
			//if(sym.getLabel().getSymbol().equals(symbol)) { // Rahul
			if(sym.getLabel().getSymbol().equals(symbol) && sym.getLabel().getAbstraction().equals(abstraction)) {
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
				//System.out.println("Adding Transition: from " + stateFor(source).getIndex() + " using " + compoundSymbol.getIndex() + " to " + stateFor(targetStates).getIndex());
			}			
		}
		transitions.clear(); //free space
	}

	protected abstract A abstraction(Multiset<Symbol<L>> symbols); 
	
	//protected abstract A abstraction(long lastAccess);

	private FSMState<AbstractionAndSymbol> stateFor(Set<FSMState<L>> set) {
		FSMState<AbstractionAndSymbol> compoundState = stateSetToCompoundState.get(set);
		if(compoundState==null) {
			boolean isAccepting = true;			
			for (FSMState<L> state : set) {
				if(!state.isAccepting() ){ 
					isAccepting = false;
					break;
				}
			}		
			compoundState = makeState(isAccepting);
			System.err.println(compoundState+" - "+set + " "+ (compoundState.isAccepting()?"Accepting":"NonAccepting"));
			stateSetToCompoundState.put(set, compoundState);
		}
		return compoundState;
	}
	
	public Set<Parameter<?>> getParameters() {
		return parameters;
	}

	
	/**
	 * Maybe processes the event consisting of the symbol and bindings.
	 * Whether or not the event is processed depends on the return value of
	 * the predicate {@link #shouldMonitor(ISymbol, IVariableBinding, Multiset)}.
	 * @param symbolLabel the current event's symbol's label
	 * @param binding the current events's binding
	 */
	@SuppressWarnings("unchecked")
	public void maybeProcessEvent(Event e) {
		++recordCounter;
		Symbol<L> symbol = (Symbol<L>)(e.getBaseEvent());
		if(shouldMonitor(symbol)) {
			//System.out.println("*");
			if(!didMonitorLastEvent){
				reenableTime++;
				newWindowRecordCounter = recordCounter;
			}
			//We need to build the abstraction later, specific to a monitor so do it inside SyncMonitor.
			Symbol<AbstractionAndSymbol> sym = symbolWithEmptyAbstraction.get(symbol);
			if(sym == null){
				Symbol<AbstractionAndSymbol> targetSymbol = getAlphabet().getSymbolByLabel(new AbstractionAndSymbol(abstraction(EMPTY), symbol));
				e.setBaseEvent(targetSymbol);
				symbolWithEmptyAbstraction.put(symbol, targetSymbol);
			} else
				e.setBaseEvent(sym);
			parametricMonitor.processEvent(e);
			didMonitorLastEvent = true;
		} else {
			if(didMonitorLastEvent)
				deleteHistory();
			updateHistory(symbol);
			didMonitorLastEvent = false;
		}
	}
	

	/**
	 * Returns the length of a sampling period for this monitor template. This size may actually depend on the 
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
	//abstract protected Set<Symbol<L>> criticalSymbols();

	/**
	 * Determines whether the current event should be monitored.
	 * @param symbol the current event's symbol
	 * @param binding the current events's binding
	 * @param skippedSymbols the multiset of symbols of events skipped so far
	 * @return 
	 */

	protected boolean shouldMonitor(Symbol<L> symbol){	
		if(phase==0) {
			processEventsInCurrentPeriod = random.nextBoolean();
		}
		int periodLength = processEventsInCurrentPeriod ? samplingPeriod : skipPeriod;
		phase = (phase+1) % periodLength;
		return processEventsInCurrentPeriod || criticalSymbols.contains(symbol);
		//return true;
	}

	
	public class AbstractionAndSymbol{
		private final A abstraction;
		private final Symbol<L> symbol;
		public AbstractionAndSymbol(A abstraction, Symbol<L> symbol) {
			this.abstraction = abstraction;
			this.symbol = symbol;
		}
		public A getAbstraction() {
			return abstraction;
		}
		public Symbol<L> getSymbol() {
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
	@SuppressWarnings("unchecked")
	public SyncFSMMonitor createMonitorPrototype() {
		return new SyncFSMMonitor((FSMState<AbstractionAndSymbol>)getInitialState());
	}
	
	
	//@Override
	protected SyncState makeState(boolean isFinal) {
		return new SyncState((Alphabet<AbstractionAndSymbol>)getAlphabet(),isFinal, nextStateNum++);
	}
	
	public class SyncState extends FSMState<AbstractionAndSymbol> {
		
		Map<Symbol<L>,A> symToMaxAbstraction = new HashMap<Symbol<L>,A>();
		Map<Symbol<L>,Symbol<AbstractionAndSymbol>> symToMaxSymbol = new HashMap<Symbol<L>,Symbol<AbstractionAndSymbol>>();

		public SyncState(Alphabet<AbstractionAndSymbol> alphabet, boolean isAccepting, int label) {
			super(label, alphabet, isAccepting, null, Integer.toString(label));
		}
		
		//@Override
		@SuppressWarnings("unchecked")
		public FSMState<AbstractionAndSymbol> getSuccessor(Symbol<AbstractionAndSymbol> sym) {
			//System.out.println("Entered getSuccessor of SyncState");
			A max = symToMaxAbstraction.get(sym.getLabel().getSymbol());
			if(max!=null && max.isSmallerOrEqualThan(sym.getLabel().getAbstraction())) {
				sym = symToMaxSymbol.get(sym.getLabel().getSymbol());
			}
			return (FSMState<AbstractionAndSymbol>)super.getSuccessor(sym);
		}
		
		@Override //Rahul
		public void addTransition(Symbol<AbstractionAndSymbol> sym, FSMState<AbstractionAndSymbol> succ) {
			//System.out.println("Entered addTransition of SyncState");
			super.addTransition(sym, succ);
			A abstraction = sym.getLabel().getAbstraction();
			Symbol<L> symbol = sym.getLabel().getSymbol();
			symToMaxAbstraction.put(symbol,abstraction);
			symToMaxSymbol.put(symbol,getAlphabet().getSymbolByLabel(new AbstractionAndSymbol(abstraction, sym.getLabel().getSymbol())));
		}
		
	}

	public class SyncFSMMonitor extends StatefulMonitor {

		protected long lastAccess;
		
		public final long createRecordCounter;
		
		public SyncFSMMonitor(FSMState<AbstractionAndSymbol> initialState) {
			super(initialState);
			this.state = initialState;
			this.createRecordCounter = recordCounter;
		}
		
		
		@SuppressWarnings("unchecked")
		public boolean processEvent(Event e) {
			//as input we get a syncing symbol; now we must check whether we actually need
			//to sync; if not, then we modify the symbol to a non-syncing one

			BaseEvent be;
			boolean status;
			
			if(reenableTime==lastAccess) {
				// don't sync
				status = super.processEvent(e);
			} else if(reenableTime == (lastAccess + 1)){
				lastAccess = reenableTime;
				be = e.getBaseEvent();
				e.setBaseEvent(getAlphabet().getSymbolByLabel((new AbstractionAndSymbol(abstraction(skippedSymbols), ((Symbol<AbstractionAndSymbol>)be).getLabel().getSymbol()))));
				status = super.processEvent(e);
				e.setBaseEvent(be);
			} else {
				lastAccess = reenableTime;
				be = e.getBaseEvent();
				Symbol<L> baseSymbol = ((Symbol<AbstractionAndSymbol>)be).getLabel().getSymbol();
				Symbol<AbstractionAndSymbol> sym = symbolWithMaxAbstraction.get(baseSymbol);
				if(sym == null){
					Symbol<AbstractionAndSymbol> targetSymbol = getAlphabet().getSymbolByLabel(new AbstractionAndSymbol(maxAbstraction, baseSymbol));
					e.setBaseEvent(targetSymbol);
					symbolWithMaxAbstraction.put(baseSymbol, targetSymbol);
				} else
					e.setBaseEvent(sym);
				status = super.processEvent(e);
				e.setBaseEvent(be);
			}
			
			return status;
		}
		
		@SuppressWarnings("unchecked")
		@Override
	    public Monitor copy() {
		return new SyncFSMMonitor((FSMState<AbstractionAndSymbol>)super.state);
	    }
	}

	public abstract class SymbolMultisetAbstraction {		
		public abstract int hashCode();

		public abstract boolean equals(Object obj);		
		
		protected abstract A add(Symbol<L> sym);
		
		protected abstract boolean isSmallerOrEqualThan(A other);
		
		protected abstract A abstractionExcludingSymbols(Set<Symbol<L>> syms);
	}

}
