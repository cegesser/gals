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

import java.util.Collections;
import java.util.List;

/**
 * @author Carlos Gesser
 */
public class ComplexItem extends Item
{
	private List<Sequence> items;
	
	public ComplexItem(List<Sequence> items)
	{
		super((CardinalityToken)null);
		this.items = items;
	}
	
	public ComplexItem(List<Sequence> items, CardinalityToken cardinality)
	{
		super(cardinality);
		this.items = items;
	}
	
	public ComplexItem(List<Sequence> items, Cardinality cardinality)
	{
		super(cardinality);
		this.items = items;
	}
	
	public List<Sequence> getItems()
	{
		return Collections.unmodifiableList(items);
	}

	public int getStart()
	{
		return items.get(0).getItems().get(0).getStart();
	}

	public int getLength()
	{
		if (getCardinality() == null)
		{
			List<Item> last = items.get(items.size()-1).getItems();
			Item i = last.get(last.size()-1);
			return i.getStart() + i.getLength() - getStart();
		}
		else
			return getCardinalityToken().getStart() - getStart() + 1;
	}	
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("(");
		boolean first = true;
		for (Sequence i : items)
		{
			if (first)
				first = false;
			else
				sb.append("|");
			
			sb.append(i);
		}
		sb.append(")").append(getCardinality());
		
		return sb.toString();
	}
	
	public String getLexeme() 
	{
		return "<"+toString()+">";
	}
	
	public Item copy(Cardinality cardinality) 
	{
		if (cardinality == null)
			cardinality = getCardinality();
		
		return new ComplexItem(getItems(), cardinality);
	}
}
