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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gesser.gals.analyser.AnalysisError;
import gesser.gals.ebnf.decl.ProductionDecl;
import gesser.gals.ebnf.parser.Parser;
import gesser.gals.ebnf.parser.Scanner;
import gesser.gals.ebnf.parser.tokens.Token;

/**
 * @author gesser
 */

public class ParserManager 
{
	private Scanner scanner = new Scanner();
	private Parser parser = new Parser();
	private SymbolManager symbolManager = new SymbolManager();
	
	private List<Token> tokens = new ArrayList<Token>();
	private List<ProductionDecl> productions = new ArrayList<ProductionDecl>();
	
	public void setInput(String input)
	{
		tokens.clear();
		productions.clear();
		symbolManager.clear();
		
		scanner.setInput(input);
		
		for (Token t : scanner)
			tokens.add(t);
		
		//tokens = Collections.unmodifiableList(tokens);
		
		try 
		{
			productions.addAll(parser.parse(tokens, symbolManager));
		}
		catch (AnalysisError e) 
		{
			//e.printStackTrace();
		}
	}
	
	public List<Token> getTokensBetween(int a, int b)
	{
		int start = 0;
		for (int i=0; i < tokens.size(); i++)
		{
			Token t = tokens.get(i);
			if (t.getEnd() >= a)
			{
				start = i;
				break;
			}
		}
		int end = tokens.size();
		for (int i=start; i < tokens.size(); i++)
		{
			Token t = tokens.get(i);
			if (t.getStart() > b)
			{
				end = i;
				break;
			}
		}
		
		return tokens.subList(start, end);
	}

	public List<Token> getTokens() 
	{
		return tokens;
	}
}
