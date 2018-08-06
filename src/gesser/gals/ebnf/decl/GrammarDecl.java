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

package gesser.gals.ebnf.decl;

import gesser.gals.ebnf.SymbolManager;
import gesser.gals.ebnf.parser.tokens.NonTerminalToken;

import java.util.Collections;
import java.util.List;

/**
 * @author Carlos Gesser
 */

public class GrammarDecl
{
	private SymbolManager stm = new SymbolManager();
	
	private List<ProductionDecl> productions;
	private NonTerminalToken startSymbol;
	
	public GrammarDecl(List<ProductionDecl> productions, SymbolManager sm)
	{
		this.stm = sm;		
		
		this.productions = productions;
		startSymbol = productions.get(0).getLhs();
	}
	
	public GrammarDecl()
	{	
	}
	
	public SymbolManager getSymbolTokenMapping()
	{
		return stm;
	}
	
	public NonTerminalToken getStartSymbol()
	{
		return startSymbol;
	}
	
	public List<ProductionDecl> getProductions()
	{
		return Collections.unmodifiableList(productions);
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (ProductionDecl p : productions)
			sb.append(p).append("\n");
		
		return sb.toString();
	}
}
