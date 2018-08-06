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

package gesser.gals.ebnf.parser.tokens;

import gesser.gals.editor.Style;

import javax.swing.text.SimpleAttributeSet;

/**
 * @author Carlos Gesser
 */

public abstract class Token
{
	private int start;
	private TokenId id;
	
	public Token(TokenId id, int start)
	{
		this.id = id;
		this.start = start;
	}
	
	public TokenId getId()
	{
		return id;
	}
	
	public int getStart()
	{
		return start;
	}
	
	public int getEnd()
	{
		return start + getLexeme().length();
	}
	
	public abstract String getLexeme();

	public String toString()
	{
		return getLexeme();
	}

	public boolean equals(Object obj)
	{
		if (obj.getClass() == this.getClass())
		{
			Token t = (Token) obj;
			return id == t.id && start == t.start && getLexeme().equals(t.getLexeme());
		}
		else
			return false;
	}
	
	public int hashCode()
	{
		return ((43 * start) + getLexeme().hashCode())*43 + id.hashCode();
	}

	public SimpleAttributeSet getStyle() 
	{
		return Style.NORMAL.getStyle();
	}
}
