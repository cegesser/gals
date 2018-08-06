package gesser.gals.parserparser;

import gesser.gals.analyser.*;
import gesser.gals.simulator.BasicScanner;

import static gesser.gals.parserparser.Constants.*;

public class Scanner implements BasicScanner
{	
	private String input;
	private int pos;
	private boolean returnComents = false;
	private int endPosition;
	
	public Scanner(String str)
	{
		setInput(str);
	}
	
	public Scanner()
	{
		this("");
	}
	
	public void setReturnComents(boolean rc)
	{
		returnComents = rc;
	}
	
	public void setInput(String input)
	{
		this.input = input;
		pos = 0;
		endPosition = input.length(); 
	}
	
	public Token nextToken() throws LexicalError
	{	
		while (hasMoreChars())
		{			
			int start = pos;
			char c = nextChar();
			
			switch (c)
			{
				case ' ':
				case '\n':
				case '\r':
				case '\t':
					continue;				
					
				case ':':
					return analyseDerives();
					
				case '|':
					return new Token(PIPE, "|", start);
				
				case ';':
					return new Token(SEMICOLON, ";", start);
		
				case '#':
					return analyseAction();
					
				case '<':
					return analyseNonTerminal();
					
				case '_':
				case '\"':
					return analyseTerminal(c);
					
				case '/':
				{
					Token t = analyseComent();
					if (returnComents)
						return t;
					else
						continue;
				}
					
				default:
					if (Character.isLetter(c))
					{
						return analyseTerminal(c);
					}
					throw new LexicalError("Caracter Inválido: '"+c+"'", start);
			}						
		}
		return null;
	}

	private Token analyseComent() throws LexicalError
	{
		int start = pos-1;
		if ( ! hasMoreChars() )
			throw new LexicalError("Caracter Inválido: '/'", start);
			
		char c = nextChar();
		
		if (c != '/')
		{
			pushChar();
			throw new LexicalError("Caracter Inválido: '/'", start);	
		}
		
		StringBuffer result = new StringBuffer("//");
		while (hasMoreChars())
		{
			c = nextChar();
			
			if (c == '\n')
			{
				pushChar();
				break;
			}
			result.append(c);
		}
		return new Token(-1, result.toString(), start);
	}

	private Token analyseDerives() throws LexicalError
	{
		int start = pos-1;

		if ( input.length() - start >= 3 )
		{
			char c = nextChar();
			if (c == ':')
			{
				c = nextChar();
				if (c == '=')
					return new Token(DERIVES, "::=", start);
			}
		}
		throw new LexicalError("Símbolo Inválido", start);
	}
	
	public int getPosition()
	{
		return pos;
	}
	
	public void setPosition(int pos)
	{
		this.pos = pos;
	}
		
	public void setEnd(int end)
	{
		endPosition = end;
	}
	
	public void setRange(int start, int end)
	{
		setPosition(start);
		setEnd(end);
	}
	
	private Token analyseTerminal(char c) throws LexicalError
	{ 
		int start = pos-1;
		StringBuffer bfr = new StringBuffer();
		bfr.append(c);
		if (c == '\"')
		{			
			boolean close = false;			
			while (hasMoreChars())
			{
				c = nextChar();
				bfr.append(c);
				if (c == '\"')
				{
					if (hasMoreChars())
					{
						c = nextChar();
						if (c == '\"')
							bfr.append(c);
						else
						{
							pushChar();
							close = true;
							break;
						}
					}
					else
						close = true;
				}
				else if (c == '\n')
					throw new LexicalError("Terminal inválido", start);
			}
			if (bfr.length() == 0 || !close)
				throw new LexicalError("Terminal inválido", start);
		}
		else
		{			
			while (hasMoreChars())
			{
				c = nextChar();
				if (c != '_' && ! Character.isLetterOrDigit(c))
				{
					pushChar();
					break;					
				}
				bfr.append(c);
			}			
		}
		return new Token(TERM, bfr.toString(), start);
	}

	private Token analyseNonTerminal() throws LexicalError
	{
		int start = pos-1;
		StringBuffer bfr = new StringBuffer();	
		char c = '<';
		while (hasMoreChars())
		{
			c = nextChar();
			if (c == '>')
				break;
			else if (!Character.isLetterOrDigit(c) && c != '_')
				throw new LexicalError("Não-Terminal inválido", start);
			bfr.append(c);
		}		
		if (bfr.length() == 0 || c != '>')
			throw new LexicalError("Não-Terminal inválido", start);
		else 
			return new Token(NON_TERM, "<"+bfr+">", start);
	}
	
	private Token analyseAction() throws LexicalError
	{
		int start = pos-1;
		
		StringBuffer bfr = new StringBuffer(); 		
		while (hasMoreChars())
		{
			char c = nextChar();
			
			if (! Character.isDigit(c) )
			{
				pushChar();
				break;
			}
			bfr.append(c);	
		}
		
		if (bfr.length() == 0)
			throw new LexicalError("Ação Semântica inválida", start);
		else
			return new Token(ACTION, bfr.toString(), start);
	}
	
	private boolean hasMoreChars()
	{
		return pos < endPosition;
	}
	
	private char nextChar()
	{		
		if (hasMoreChars())
			return input.charAt(pos++);
		else
			return (char)-1;
	}
	
	private void pushChar()
	{
		pos--;
	}
}
