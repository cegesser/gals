﻿/*

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

import java.util.Set;



/**

 * @author Carlos Gesser

 */

public class SemanticAction extends Symbol

{	

	private static final Set<Terminal> FIRST = Collections.singleton(SymbolManager.EPSILON);

	public SemanticAction(String lexeme)

	{

		super(lexeme, Integer.parseInt(lexeme.substring(1)));

	}

	

	public int getValue()

	{

		return getNumber();

	}

	

	public Set<Terminal> getFirst() 

	{

		return FIRST;

	}

}

