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

package gesser.gals.ebnf.parser;

import gesser.gals.ebnf.SymbolManager;
import gesser.gals.ebnf.decl.ComplexItem;
import gesser.gals.ebnf.decl.Item;
import gesser.gals.ebnf.decl.ProductionDecl;
import gesser.gals.ebnf.decl.Sequence;
import gesser.gals.ebnf.decl.SymbolItem;
import gesser.gals.ebnf.parser.tokens.CardinalityToken;
import gesser.gals.ebnf.parser.tokens.NonTerminalToken;
import gesser.gals.ebnf.parser.tokens.SymbolToken;
import gesser.gals.ebnf.parser.tokens.TerminalToken;
import gesser.gals.ebnf.parser.tokens.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Generator
{   
	private SymbolManager sm;
	
	/**
	 * List of declared Productions
	 */
    private List<ProductionDecl> productions = new ArrayList<ProductionDecl>();
    
    /**
     * Stack of Lists of Sequences of Symbols
     */
    private Stack<List<Sequence>> sequenceListStack = new Stack<List<Sequence>>();
    
    /**
     * Stack of Lists of Symbols
     * Each of this lists will be a sequence
     */
    private Stack<List<Item>> itemListStack = new Stack<List<Item>>();
    
    private NonTerminalToken lhs = null;
	private SymbolToken lastSymbol;
	private List<Sequence> lastComplex;
	
	public Generator(SymbolManager sm)
	{
		this.sm = sm;
	}
	
	public SymbolManager getSymbolTokenMapping()
	{
		return sm;
	}
	
	public List<ProductionDecl> getProductions()
	{
		return productions;
	}
		
    public void executeAction(int action, Token token)
    {
    	switch (action)
		{
    		case 1:
    			action01(token);
    			break;
    		case 2:
    			action02(token);
    			break;
    		case 3:
    			action03(token);
    			break;
    		case 4:
    			action04(token);
    			break;
    		case 5:
    			action05(token);
    			break;
    		case 6:
    			action06(token);
    			break;
    		case 7:
    			action07(token);
    			break;
    		case 8:
    			action08(token);
    			break;
    		case 9:
    			action09(token);
    			break;
    		case 10:
    			action10(token);
    			break;
    		case 11:
    			action11(token);
    			break;
    		case 12:
    			action12(token);
    			break;
    		default:
    			System.out.println("Ação #"+action+", Token: "+token);
		}    	
    }
    
    private void action01(Token token)
    {
    	lhs = (NonTerminalToken) token;
    	sm.bind(sm.createNonTerminal(lhs.getLexeme()), lhs);    	
    	sequenceListStack.push(new ArrayList<Sequence>());
    }
    
    private void action02(Token token)
    {
    	productions.add(new ProductionDecl(lhs, sequenceListStack.pop()));	 
    }
    
    private void action03(Token token)
    {
    	itemListStack.push(new ArrayList<Item>());
    }
    
    private void action04(Token token)
    {
    	sequenceListStack.peek().add(new Sequence(itemListStack.pop()));
    }
    
    private void action05(Token token)
    {
    	lastSymbol = (SymbolToken) token;
    	lastComplex = null;
    }
        
    private void action06(Token token)
    {
    	lastSymbol = null;
    	lastComplex = sequenceListStack.pop();
    }
    
    /**
     * No Cardinality
     * @param token
     */
    private void action07(Token token)
    {    	
    	if (lastSymbol != null)
    	{
    		if (lastSymbol instanceof TerminalToken)
    			sm.bind(sm.createTerminal(lastSymbol.getLexeme()), lastSymbol);
    		else if (lastSymbol instanceof NonTerminalToken)
    			sm.bind(sm.createNonTerminal(lastSymbol.getLexeme()), lastSymbol);
    		else
    			sm.bind(sm.createSemanticAction(lastSymbol.getLexeme()), lastSymbol);
    		    		
    		itemListStack.peek().add(new SymbolItem(lastSymbol));
    	}
    	else
    		itemListStack.peek().add(new ComplexItem(lastComplex));
    }
    
    /**
     * Zero or More Cardinality
     * @param token
     */
    private void action08(Token token)
    {	
    	if (lastSymbol != null)
    	{
    		if (lastSymbol instanceof TerminalToken)
    			sm.bind(sm.createTerminal(lastSymbol.getLexeme()), lastSymbol);
    		else if (lastSymbol instanceof NonTerminalToken)
    			sm.bind(sm.createNonTerminal(lastSymbol.getLexeme()), lastSymbol);
    		else
    			sm.bind(sm.createSemanticAction(lastSymbol.getLexeme()), lastSymbol);
    		
    		itemListStack.peek().add(new SymbolItem(lastSymbol, (CardinalityToken) token));
    	}
    	else
    		itemListStack.peek().add(new ComplexItem(lastComplex, (CardinalityToken) token));
    }
    
    /**
     * One or More Cardinality
     * @param token
     */
    
    private void action09(Token token)
    {
    	if (lastSymbol != null)
    	{
    		if (lastSymbol instanceof TerminalToken)
    			sm.bind(sm.createTerminal(lastSymbol.getLexeme()), lastSymbol);
    		else if (lastSymbol instanceof NonTerminalToken)
    			sm.bind(sm.createNonTerminal(lastSymbol.getLexeme()), lastSymbol);
    		else
    			sm.bind(sm.createSemanticAction(lastSymbol.getLexeme()), lastSymbol);
    		
    		itemListStack.peek().add(new SymbolItem(lastSymbol, (CardinalityToken) token));
    	}
    	else
	    	itemListStack.peek().add(new ComplexItem(lastComplex, (CardinalityToken) token));    	
    }
    
    /**
     * Optional Cardinality
     * @param token
     */
    private void action10(Token token)
    {
    	if (lastSymbol != null)
    	{
    		if (lastSymbol instanceof TerminalToken)
    			sm.bind(sm.createTerminal(lastSymbol.getLexeme()), lastSymbol);
    		else if (lastSymbol instanceof NonTerminalToken)
    			sm.bind(sm.createNonTerminal(lastSymbol.getLexeme()), lastSymbol);
    		else
    			sm.bind(sm.createSemanticAction(lastSymbol.getLexeme()), lastSymbol);
    		
    		itemListStack.peek().add(new SymbolItem(lastSymbol, (CardinalityToken) token));    		
    	}
    	else
    		itemListStack.peek().add(new ComplexItem(lastComplex, (CardinalityToken) token));
    }
    
    private void action11(Token token)
    {
    	sequenceListStack.push(new ArrayList<Sequence>());
    }
    
    private void action12(Token token)
    {
    	//action 6 accurs now
    }
}

