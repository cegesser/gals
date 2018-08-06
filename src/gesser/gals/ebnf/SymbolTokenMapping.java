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

import static gesser.gals.ebnf.parser.tokens.TokenId.*;

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
public class SymbolTokenMapping
{
	private Map<SymbolToken, Symbol> tokenSymbol = new HashMap<SymbolToken, Symbol>();
	
	private Map<Symbol, Set<SymbolToken>> symbolToken = new HashMap<Symbol, Set<SymbolToken>>();
	
	private Set<Symbol> symbols = new HashSet<Symbol>();
	private Map<String, Terminal> terminals = new HashMap<String, Terminal>();
	private Map<String, NonTerminal> nonTerminals = new HashMap<String, NonTerminal>();
	private Map<String, SemanticAction> semanticActions = new HashMap<String, SemanticAction>();
	
	public void add(SymbolToken token)
	{
		Symbol s = create(token);
		
		tokenSymbol.put(token, s);
		
		Set<SymbolToken> tokens = symbolToken.get(s);
		if (tokens == null)
		{
			tokens = new HashSet<SymbolToken>();
			symbolToken.put(s, tokens);
		}
		tokens.add(token);
	}
	
	public Symbol get(SymbolToken token)
	{
		return tokenSymbol.get(token);
	}
	
	public Set<SymbolToken> get(Symbol s)
	{
		return Collections.unmodifiableSet(symbolToken.get(s));
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
	
	public Set<Symbol> getSymbols()
	{
		return Collections.unmodifiableSet(symbols);
	}
	
	private Symbol create(SymbolToken token)
	{
		switch (token.getId())
		{
			case SEMANTIC_ACTION:
			{
				SemanticAction sa = semanticActions.get(token.getLexeme());
				if (sa == null)
				{
					sa = new SemanticAction(token.getLexeme());
					semanticActions.put(token.getLexeme(), sa);
					symbols.add(sa);
				}
				return sa;
			}
			case NON_TERMINAL:
			{
				NonTerminal nt = nonTerminals.get(token.getLexeme());
				if (nt == null)
				{
					nt = new NonTerminal(token.getLexeme(), nonTerminals.size());
					nonTerminals.put(token.getLexeme(), nt);
					symbols.add(nt);
				}
				return nt;
			}
			case TERMINAL:
			{
				Terminal t = terminals.get(token.getLexeme());
				if (t == null)
				{
					t = new Terminal(token.getLexeme(), terminals.size());
					terminals.put(token.getLexeme(), t);
					symbols.add(t);
				}
				return t;
			}
			default:
				throw new NoSuchElementException(token.toString());
		}
	}

	
}
