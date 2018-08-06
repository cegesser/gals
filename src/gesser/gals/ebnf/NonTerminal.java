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

import gesser.gals.ebnf.Production;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Carlos Gesser
 */
public class NonTerminal extends Symbol
{
	private boolean fake;
	Set<Terminal> first = new HashSet<Terminal>();
	Set<Terminal> follow = new HashSet<Terminal>();
	Set<Production> productions = new HashSet<Production>();
	
	protected NonTerminal(String lexeme, int number, boolean fake)
	{
		super(lexeme, number);
		this.fake = fake;
	}
	
	protected NonTerminal(String lexeme, int number)
	{
		this(lexeme, number, false);
	}
	
	public boolean isFake() 
	{
		return fake;
	}
	
	public Set<Terminal> getFirst() 
	{
		return first;
	}	
	
	public Set<Terminal> getFollow() 
	{
		return follow;
	}
	
	public Set<Production> getProductions() 
	{
		return productions;
	}
}
