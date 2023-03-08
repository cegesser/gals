package gesser.gals.simulator;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import gesser.gals.analyser.*;
import gesser.gals.generator.parser.Grammar;

public class SimpleScanner
{
	private StringTokenizer tknzr;
	private Grammar grammar;
	private String[] symbols;
	private int pos;
	
	public SimpleScanner(Grammar g, String str)
	{
		grammar = g;
		symbols = grammar.getTerminals();
		tknzr = new StringTokenizer(str);
		pos = 0;
	}
	
	public int getPosition()
	{
		return pos;
	}
	
	public Token nextToken() throws LexicalError
	{
		String token;
		try
		{
			token = tknzr.nextToken();
		}
		catch (NoSuchElementException e)
		{
			return null;
		}
		
		for (int i=0; i<symbols.length; i++)
		{
			if (symbols[i].equals(token))
				return new Token(i+2, token, 0);
		}
		pos++;			
		
		throw new LexicalError("Símbolo Inválido", pos);
	}
}
