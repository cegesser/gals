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

import java.util.Collections;
import java.util.List;

/**
 * @author Carlos Gesser
 */

public class Sequence
{
	private List<Item> items;
	
	public Sequence(List<Item> items)
	{
		this.items = items;
	}
	
	public List<Item> getItems()
	{
		return Collections.unmodifiableList(items);
	}

	public String toString()
	{
		if (items.isEmpty())
			return "EPSILON";
		else
		{
			StringBuilder sb = new StringBuilder();
			for (Item i : items)
				sb.append(i).append(" ");
			
			sb.setLength(sb.length()-1);
			
			return sb.toString();
		}
	}
}
