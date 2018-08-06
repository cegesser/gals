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

import java.util.Set;

/**
 * @author Carlos Gesser
 */
public abstract class Symbol
{
	private String lexeme;
	private int number;
	
	protected Symbol(String lexeme, int number)
	{
		this.lexeme = lexeme;
		this.number = number;
	}
	
	public String getLexeme()
	{
		return lexeme;
	};
	
	public int getNumber() 
	{
		return number;
	}

	public String toString()
	{
		return lexeme;
	}
	
	public abstract Set<Terminal> getFirst();

	public boolean equals(Object obj)
	{
		if (obj.getClass() == this.getClass())
		{
			Symbol s = (Symbol) obj;
			return number == s.number && lexeme.equals(s.lexeme);
		}
		else
			return false;
	}
	
	public int hashCode()
	{
		return (number*37) + lexeme.hashCode();
	}
	
	public boolean isFake()
	{
		return false;
	}
}
