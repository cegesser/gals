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

import gesser.gals.ebnf.parser.tokens.CardinalityToken;
import gesser.gals.ebnf.parser.tokens.SymbolToken;

/**
 * @author Carlos Gesser
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SymbolItem extends Item
{
	private SymbolToken symbol;
	
	public SymbolItem(SymbolToken symbol)
	{
		super((CardinalityToken)null);
		this.symbol = symbol;
	}
	
	public SymbolItem(SymbolToken symbol, CardinalityToken cardinality)
	{
		super(cardinality);
		this.symbol = symbol;
	}
	
	public SymbolItem(SymbolToken symbol, Cardinality cardinality)
	{
		super(cardinality);
		this.symbol = symbol;
	}
	
	public SymbolToken getSymbol()
	{
		return symbol;
	}

	public int getStart()
	{
		return symbol.getStart();
	}
	
	public int getLength()
	{
		if (getCardinality() == null)
			return symbol.getLexeme().length();
		else
			return getCardinalityToken().getStart() - symbol.getStart() + 1;
	}
	
	public String toString()
	{
		return symbol.getLexeme() + getCardinality();
	}
	
	public String getLexeme() 
	{
		if (getCardinality() == Cardinality.ONCE)
			return toString();
		else
			return "<" + toString() + ">";
	}
	
	public SymbolItem copy(Cardinality cardinality) 
	{
		if (cardinality == null)
			cardinality = getCardinality();
		
		return new SymbolItem(getSymbol(), cardinality);
	}
}
