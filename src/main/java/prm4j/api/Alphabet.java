/*
 * Copyright (c) 2012 Mateusz Parzonka, Eric Bodden
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mateusz Parzonka - initial API and implementation
 */
package prm4j.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import prm4j.sync.AbstractSyncingSpec.AbstractionAndSymbol;


/**
 * TODO implement auto-naming.
 */

public class Alphabet<L> implements Iterable<Symbol<L>>{

    private int symbolCount = 0;
    private int parameterCount = 0;
    private final Set<Parameter<?>> parameters;
    private final Set<Symbol<L>> symbols;	//Rahul
    
    //private Set<Symbol<L>> backingSet = new HashSet<Symbol<L>>(); // Rahul
    private Map<L,Symbol<L>> labelToSymbol = new HashMap<L,Symbol<L>>(); // Rahul
    
    

    public Alphabet() {
	super();
	parameters = new HashSet<Parameter<?>>();
	symbols = new HashSet<Symbol<L>>();
    }
    
	@Override
	public Iterator<Symbol<L>> iterator() {	// Rahul added
		return symbols.iterator();
		//return backingSet.iterator();
	}
	
	public Symbol<L> getSymbolByLabel(L l) {
		//System.out.println("Calling getSymbolByLabel with " + l);
		/*for (Symbol<L> sym : this) {
			if(sym.getLabel().equals(l)) {
				return sym;
			}
		}*/
		//System.out.println("////////////////");
		//System.out.println("Calling getSymbolByLabel with " + l);
		/////////////////////////// Rahul added this
		if(l instanceof AbstractionAndSymbol){
			Symbol<L> sym = labelToSymbol.get(l);
			if(sym==null) {
				Set<Parameter<?>> params = ((AbstractionAndSymbol)l).getSymbol().getParameters();
				if(params.size() == 0){
					sym = createSymbol0(l);
				} else if (params.size() == 1){
					Iterator <Parameter<?>> it = params.iterator();
					Parameter<?> param1 = it.next();
					sym = createSymbol1(l,param1);
					//System.out.println("Param11 is: " + param1);
				} else if (params.size() == 2){
					Iterator <Parameter<?>> it = params.iterator();
					Parameter<?> param1 = it.next();
					//System.out.println("Param21 is: " + param1);
					Parameter<?> param2 = it.next();
					//System.out.println("Param22 is: " + param2);
					if(param1.getIndex()<param2.getIndex())
						sym = createSymbol2(l,param1, param2);
					else
						sym = createSymbol2(l,param2, param1);
				}
				labelToSymbol.put(l, sym);			
				//symbols.add(sym);
				//System.out.println("Symbol count: " + symbolCount);
				//System.out.println("Symbol added is: " + sym.getLabel());
				
			}
			//System.out.println("Symbol returned is: " + sym.getLabel());
			return sym;
		}
		////////////////////////////
		/*System.out.println("Calling getSymbolByLabel with " + l);
		for (Symbol<L> sym : this) {	
			System.out.println("2");
			if(sym.getLabel().equals(l)) {
				System.out.println("Symbol returned is: " + sym.getLabel());
				return sym;
			}
		}
		System.out.println("////////////////");*/
		Symbol<L> symbol = labelToSymbol.get(l);
		if(symbol == null){
			for (Symbol<L> sym : this) {	
				if(sym.getLabel().equals(l)) {
					//System.out.println("Symbol returned is: " + sym.getLabel());
					labelToSymbol.put(l, sym);		
					return sym;
				}
			}
		}else
			return symbol;
		
		throw new IllegalArgumentException("Unknown symbol:" +l);
	}

	

    /**
     * Creates a parameter of type {@link Object}.
     *
     * @return the parameter
     */
    public Parameter<Object> createParameter() {
	Parameter<Object> parameter = new Parameter<Object>("TODO");
	parameters.add(parameter);
	parameterCount++;
	return parameter;
    }

    /**
     * Creates a parameter of given type. To create parameters with complex types, use the
     * {@link #addParameter(Parameter) addParameter} method.
     *
     * @param parameterObjectType
     * @return the parameter
     */
    public <P> Parameter<P> createParameter(Class<P> parameterObjectType) {
	return createParameter("p" + parameterCount, parameterObjectType);
    }

    /**
     * Creates a parameter of given type and optional name. To create parameters with complex types, use the
     * {@link #addParameter(Parameter) addParameter} method.
     *
     * @param parameterObjectType
     * @return the parameter
     */
    public <P> Parameter<P> createParameter(String optionalName, Class<P> parameterObjectType) {
	Parameter<P> parameter = new Parameter<P>(optionalName);
	parameters.add(parameter);
	// we index parameters in order of appearance, this may be not optimal
	parameter.setIndex(parameterCount++);
	return parameter;
    }

    /**
     * Adds an existing parameter to the alphabet. Use this for fully type-safe specifications.
     *
     * @param parameter
     * @return
     */
    public <P> Parameter<P> addParameter(Parameter<P> parameter) {
	if (parameter.getIndex() != -1) {
	    throw new IllegalArgumentException("This parameter was already added to some alphabet!");
	}
	parameters.add(parameter);
	// we index parameters in order of appearance, this may be not optimal
	parameter.setIndex(parameterCount++);
	return parameter;
    }

    public <P> Parameter<P> addCopiedParameter(Parameter<P> parameter) {
    	parameters.add(parameter);
    	parameterCount++;
    	return parameter;
        }
    
    /**
     * Creates a symbol without any parameters.
     *
     * @return the symbol
     */
    public Symbol0<L> createSymbol0() {
	Symbol0<L> symbol = new Symbol0(this, symbolCount, "Symbol_" + symbolCount);
	symbols.add(symbol);
	symbolCount++;
	return symbol;
    }

    /**
     * Creates a symbol without any parameters providing an optional name.
     *
     * @param optionalName
     * @return the symbol
     */
    public Symbol0<L> createSymbol0(L optionalName) {
	Symbol0<L> symbol = new Symbol0<L>(this, symbolCount, optionalName);
	symbols.add(symbol);
	symbolCount++;
	return symbol;
    }

    /**
     * Creates a symbol associated with one parameter.
     *
     * @param param1
     * @return the symbol
     */
    public <P1> Symbol1<L,P1> createSymbol1(Parameter<P1> param1) {
	Symbol1<L, P1> symbol = new Symbol1<L, P1>(this, symbolCount, "Symbol_" + symbolCount, param1);
	symbols.add(symbol);
	symbolCount++;
	return symbol;
    }

    /**
     * Creates a symbol associated with one parameter providing an optional name.
     *
     * @param optionalName
     * @param param1
     * @return the symbol
     */
    public <P1> Symbol1<L, P1> createSymbol1(L optionalName, Parameter<P1> param1) {
	Symbol1<L, P1> symbol = new Symbol1<L,P1>(this, symbolCount, optionalName, param1);
	symbols.add(symbol);

	symbolCount++;
	//System.out.println("Creating symbol with " + optionalName + " and " + "param1: " + param1);
	return symbol;
    }

    /**
     * Creates a symbol associated with two parameters.
     *
     * @param param1
     * @param param2
     * @return the symbol
     */
    public <P1, P2> Symbol2<L, P1, P2> createSymbol2(Parameter<P1> param1, Parameter<P2> param2) {
	Symbol2<L, P1, P2> symbol = new Symbol2<L, P1, P2>(this, symbolCount, "Symbol_" + symbolCount, param1, param2);
	symbols.add(symbol);
	symbolCount++;
	return symbol;
    }

    /**
     * Creates a symbol associated with two parameter providing an optional name.
     *
     * @param optionalName
     * @param param1
     * @param param2
     * @return the symbol
     */
    public <P1, P2> Symbol2<L, P1, P2> createSymbol2(L optionalName, Parameter<P1> param1, Parameter<P2> param2) {
	Symbol2<L, P1, P2> symbol = new Symbol2<L, P1, P2>(this, symbolCount, optionalName, param1, param2);
	symbols.add(symbol);
	symbolCount++;
	//System.out.println("Creating symbol with " + optionalName + " and " + "param1: " + param1 + " and param2: "+ param2);
	return symbol;
    }

    
    public Set<Symbol<L>> getSymbols() { // Rahul
	return symbols;
    }

    public int size() {
	return symbolCount;
    }

    public Set<Parameter<?>> getParameters() {
	return parameters;
    }

    public int getParameterCount() {
	return parameterCount;
    }


}
