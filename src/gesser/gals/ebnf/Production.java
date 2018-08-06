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

import java.util.Collections;
import java.util.List;

/**
 * @author Carlos Gesser
 */
public class Production
{
	private NonTerminal lhs;
	private List<Symbol> rhs;
	
	
	public Production(NonTerminal lhs, List<Symbol> rhs)
	{
		this.lhs = lhs;
		this.rhs = rhs;
	}
	
	public NonTerminal getLhs()
	{
		return lhs;
	}
	
	public List<Symbol> getRhs()
	{
		return Collections.unmodifiableList(rhs);
	}
	
	public String toString() 
	{
		StringBuilder b = new StringBuilder();
		
		b.append(lhs).append(" ::= ").append(rhs);
		
		return b.toString();
	}
	
	public boolean equals(Object obj) 
	{
		if (obj == null || getClass() != obj.getClass())
			return false;
		else
		{
			Production p = (Production) obj;
			return lhs.equals(p.lhs) && rhs.equals(p.rhs);
		}
	}
}
