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

/**
 * @author Carlos Gesser
 */

public abstract class Item
{
	public enum Cardinality 
	{ 
		ONCE         { public String toString() { return ""; } }, 
		ONE_OR_MORE  { public String toString() { return "+"; } }, 
		ZERO_OR_MORE { public String toString() { return "*"; } }, 
		OPTIONAL     { public String toString() { return "?"; } }
	}
	
	private Cardinality cardinality;
	private CardinalityToken cardinalityToken;
	
	protected Item(Cardinality cardinality)
	{
		this.cardinality = cardinality;
	}
	
	protected Item(CardinalityToken cardinalityToken)
	{
		this.cardinalityToken = cardinalityToken;
		if (cardinalityToken == null)
			cardinality = Cardinality.ONCE;
		else
		{
			switch (cardinalityToken.getLexeme().charAt(0))
			{
				case '*':
					cardinality = Cardinality.ZERO_OR_MORE;
					break;
				case '+':
					cardinality = Cardinality.ONE_OR_MORE;
					break;
				case '?':
					cardinality = Cardinality.OPTIONAL;
					break;
			}
		}
	}
	
	public CardinalityToken getCardinalityToken()
	{
		return cardinalityToken;
	}
	
	public Cardinality getCardinality()
	{
		return cardinality;
	}
	
	public abstract int getStart();
	
	public abstract int getLength();

	public abstract String getLexeme();
	
	public abstract Item copy(Cardinality cardinality);
}
