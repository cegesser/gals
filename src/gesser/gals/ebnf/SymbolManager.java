/*
 * Copyright 2004 Carlos Eduardo Gesser
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, you can get it at http://www.gnu.org/licenses/gpl.txt
 */

package gesser.gals.ebnf;

import gesser.gals.ebnf.parser.tokens.SymbolToken;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * @author Carlos Gesser
 */
public class SymbolManager
{
	private Map<String, Set<SymbolToken>> symbolToTokens = new HashMap<String, Set<SymbolToken>>();
	//private Map<SymbolToken, Symbol> tokenToSymbol = new HashMap<SymbolToken, Symbol>();
	
	private Map<String, Symbol> symbols = new HashMap<String, Symbol>();
	private Map<String, Terminal> terminals = new HashMap<String, Terminal>();
	private Map<String, NonTerminal> nonTerminals = new HashMap<String, NonTerminal>();
	private Map<String, SemanticAction> semanticActions = new HashMap<String, SemanticAction>();
	
	private NonTerminal firstNonTerminal = null;
	
	public static final Terminal EPSILON = new Terminal("î", 0);
	public static final Terminal DOLLAR = new Terminal("$", 1);
	
	public SymbolManager()
	{
		clear();
	}
	
	public NonTerminal getFirstNonTerminal() 
	{
		return firstNonTerminal;
	}
	
	public void bind(Symbol s, SymbolToken token)
	{
		if (! symbols.containsKey(s.getLexeme()))
			throw new NoSuchElementException(s + " não declarado");
		
		//tokenToSymbol.put(token, s);
		Set<SymbolToken> tokens = symbolToTokens.get(s);
		if (tokens == null)
		{
			tokens = new HashSet<SymbolToken>();
			symbolToTokens.put(s.toString(), tokens);
		}
		tokens.add(token);
	}
	
	public Set<SymbolToken> getTokens(String lexeme)
	{
		return symbolToTokens.get(lexeme);
	}
	
	public NonTerminal createFakeNonTerminal(String lexeme)
	{
		NonTerminal result = nonTerminals.get(lexeme);
		if (result == null)
		{
			result = new NonTerminal(lexeme, nonTerminals.size(), true);
			
			nonTerminals.put(lexeme, result);
			symbols.put(lexeme, result);
		}
		return result;
	}
	
	public NonTerminal createNonTerminal(String lexeme)
	{
		NonTerminal result = nonTerminals.get(lexeme);
		if (result == null)
		{
			result = new NonTerminal(lexeme, nonTerminals.size());
			
			nonTerminals.put(lexeme, result);
			symbols.put(lexeme, result);
			
			if (firstNonTerminal == null)
				firstNonTerminal = result;
		}
		return result;
	}
	
	public Terminal createTerminal(String lexeme)
	{
		Terminal result = terminals.get(lexeme);
		if (result == null)
		{
			result = new Terminal(lexeme, terminals.size());
			
			terminals.put(lexeme, result);
			symbols.put(lexeme, result);
		}
		return result;
	}
	
	public SemanticAction createSemanticAction(String lexeme)
	{
		SemanticAction result = semanticActions.get(lexeme);
		if (result == null)
		{
			result = new SemanticAction(lexeme);
			
			semanticActions.put(lexeme, result);
			symbols.put(lexeme, result);
		}
		return result;
	}
	
	public NonTerminal getNonTerminal(String lexeme)
	{
		NonTerminal nt = nonTerminals.get(lexeme);
		if (nt == null)
			throw new NoSuchElementException(lexeme + getNonTerminals());
		
		return nt;
	}
	
	public Terminal getTerminal(String lexeme)
	{
		return terminals.get(lexeme);
	}
	
	public SemanticAction getSemanticAction(String lexeme)
	{
		return semanticActions.get(lexeme);
	}
	
	public Symbol get(String lexeme)
	{
		return symbols.get(lexeme);
	}
	
	public Collection<NonTerminal> getNonTerminals()
	{
		return Collections.unmodifiableCollection(nonTerminals.values());
	}
	
	public Collection<Terminal> getTerminals()
	{
		return Collections.unmodifiableCollection(terminals.values());
	}
	
	public Collection<SemanticAction> getSemanticActions()
	{
		return Collections.unmodifiableCollection(semanticActions.values());
	}
	
	public Collection<Symbol> getSymbols()
	{
		return Collections.unmodifiableCollection(symbols.values());
	}

    /**
     * 
     */
    public void clear()
    {
        symbolToTokens.clear();
		//tokenToSymbol.clear();
	
		symbols.clear();
		terminals.clear();
		nonTerminals.clear();
		semanticActions.clear();
		
		terminals.put(EPSILON.getLexeme(), EPSILON);
		symbols.put(EPSILON.getLexeme(), EPSILON);
		
		terminals.put(DOLLAR.getLexeme(), DOLLAR);
		symbols.put(DOLLAR.getLexeme(), DOLLAR);
    }
}
