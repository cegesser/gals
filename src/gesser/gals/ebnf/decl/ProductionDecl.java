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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import gesser.gals.ebnf.parser.tokens.NonTerminalToken;

/**
 * @author Carlos Gesser
 */

public class ProductionDecl
{
	private NonTerminalToken lhs;
	private List<Sequence> rhs;
	
	public ProductionDecl(NonTerminalToken lhs, List<Sequence> rhs)
	{
		this.lhs = lhs;
		this.rhs = rhs;
	}
	
	public NonTerminalToken getLhs()
	{
		return lhs;
	}
	
	public List<Sequence> getRhs()
	{
		return Collections.unmodifiableList(rhs);
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		boolean first = true;
		
		char[] data = new char[lhs.getLexeme().toString().length()];
		Arrays.fill(data, ' ');
		
		for (Sequence x: rhs)
		{
			if (first)
			{
				first = false;
				sb.append(lhs.getLexeme()).append(" ::= ");
			}
			else
			{
				sb.append(data).append("   | ");
			}
			sb.append(x).append("\n");
		}
		
		return sb.toString();
	}
}
