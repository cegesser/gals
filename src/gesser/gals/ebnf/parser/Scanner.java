package gesser.gals.ebnf.parser;

import java.util.Iterator;
import java.util.NoSuchElementException;

import gesser.gals.analyser.LexicalError;

import static gesser.gals.ebnf.parser.ScannerConstants.*;
import gesser.gals.ebnf.parser.tokens.EofToken;
import gesser.gals.ebnf.parser.tokens.ErrorToken;
import gesser.gals.ebnf.parser.tokens.Token;
import gesser.gals.ebnf.parser.tokens.TokenId;
import static gesser.gals.ebnf.parser.tokens.TokenId.*;

public class Scanner implements Iterable<Token>
{	
	private String input = "";
	private int start;
	private int end;
	
	public Scanner()
	{
		this("", 0, 0);
	}
	
	public Scanner(String input, int start, int end)
	{
		this.input = input;
		this.start = start;
		this.end = end;
	}
	
	public Scanner(String input, int start)
	{
		this(input, start, input.length());
	}
	
	public Scanner(String input)
	{
		this(input, 0, input.length());
	}
	
	public void setInput(String input, int start, int end)
	{
		this.input = input;
		this.start = start;
		this.end = end;
	}
	
	public void setInput(String input, int start)
	{
		setInput(input, start, input.length());
	}
	
	public void setInput(String input)
	{
		setInput(input, 0, input.length());
	}
	
	public Iterator<Token> iterator()
	{
		return new ScannerImplIterator(input, start, end);
	}
	
	private static class ScannerImplIterator implements Iterator<Token>
	{
		ScannerImpl s;
		Token current;
		boolean done = false;
		
		public ScannerImplIterator(String input, int start, int end)
		{
			s = new ScannerImpl(input, start, end);
			current = s.nextToken();
		}
		
		public boolean hasNext()
		{
			return ! done;
		}
		
		public Token next()
		{
			if (done)
				throw new NoSuchElementException();
			else
			{
				Token result = current;
				if (current instanceof EofToken)
					done = true;
				else
					current = s.nextToken();
				return result;
			}
		}
		
		public void remove()
		{
			throw new UnsupportedOperationException("remove");
		}
	}
}

class ScannerImpl 
{
    private int position;
    private int end;
    private String input;

    public ScannerImpl(String input, int start, int end)
    {    
        this.input = input;
        position = start;
        this.end = end;
    }

    public Token nextToken()
    {
        if ( ! hasInput() )
            return DOLLAR.createToken(input.length(), "");

        int start = position;

        int state = 0;
        int lastState = 0;
        int endState = -1;
        int end = -1;

        while (hasInput())
        {
            lastState = state;
            state = nextState(nextChar(), state);

            if (state < 0)
                break;

            else
            {
                if (tokenForState(state) != null)
                {
                    endState = state;
                    end = position;
                }
            }
        }
        if (endState < 0)
        {
        	LexicalError e = new LexicalError(SCANNER_ERROR[lastState], start);
        	
        	position = start+1;
        	while (position < input.length() && " \t\r\n()|+*?;<>".indexOf(input.charAt(position)) == -1)
        		position ++;
        	
        	return new ErrorToken(start, input.substring(start, position), e);        	
        }
        
        position = end;

        TokenId token = tokenForState(endState);

        String lexeme = input.substring(start, end);
        return token.createToken(start, lexeme);        
    }

    private int nextState(char c, int state)
    {
    	if (c > SCANNER_TABLE[state].length)
    		return -1;
    	else
    		return SCANNER_TABLE[state][c];
    }

    private TokenId tokenForState(int state)
    {
        if (state < 0 || state >= TOKEN_STATE.length)
            return null;

        return TOKEN_STATE[state];
    }

    private boolean hasInput()
    {
        return position < end;
    }

    private char nextChar()
    {
        if (hasInput())
            return input.charAt(position++);
        else
            return (char) -1;
    }
}
