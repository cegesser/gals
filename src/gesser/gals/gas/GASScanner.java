package gesser.gals.gas;

import gesser.gals.analyser.LexicalError;
import gesser.gals.analyser.Token;

public class GASScanner implements Constants
{
    private int position;
    private String input;

    public GASScanner()
    {
        this(new java.io.StringReader(""));
    }

    public GASScanner(java.io.Reader input)
    {
        setInput(input);
    }

    public void setInput(java.io.Reader input)
    {
        StringBuffer bfr = new StringBuffer();		
        try
        {
	      	int c = input.read();
            while (c != -1)
            {
                bfr.append((char)c);
                c = input.read();
            }
            this.input = bfr.toString();
        }
        catch (java.io.IOException e)
        {
            e.printStackTrace();
        }

        setPosition(0);
    }

    public void setPosition(int pos)
    {
        position = pos;
    }

    public Token nextToken() throws LexicalError
    {
        if ( ! hasInput() )
            return null;

        int start = position;

        int state = 0;
        int endState = -1;
        int end = -1;

        while (true)
        {
            if (! hasInput())
                break;

            state = nextState(nextChar(), state);

            if (state < 0)
            {
                break;
            }
            else
            {
                if (tokenForState(state) >= 0)
                {
                    endState = state;
                    end = position;
                }
            }
        }
        if (endState < 0)
            handleError(start, position-1);

        position = end;

        int token = tokenForState(endState);

        if (token == 0)
            return nextToken();
        else
        {
            String lexeme = input.substring(start, end);
            token = lookupToken(token, lexeme);
            return new Token(token, lexeme, start);
        }
    }

    private void handleError(int tokenStart, int position) throws LexicalError
    {
        throw new LexicalError("Token inválido: "+input.substring(tokenStart, position), tokenStart);
    }
    private int nextState(char c, int state)
    {
		int start = SCANNER_TABLE_INDEXES[state];
		int end   = SCANNER_TABLE_INDEXES[state+1]-1;
		
		while (start <= end)
		{
			int half = (start+end)/2;
			
			if (SCANNER_TABLE[half][0] == c)
				return SCANNER_TABLE[half][1];
			else if (SCANNER_TABLE[half][0] < c)
				start = half+1;
			else  //(SCANNER_TABLE[half][0] > c)
				end = half-1;
		}
		
		return -1;
    }

    private int tokenForState(int state)
    {
        if (state < 0 || state >= TOKEN_STATE.length)
            return -1;

        return TOKEN_STATE[state];
    }

    public int lookupToken(int base, String key)
    {
        int start = SPECIAL_CASES_INDEXES[base];
        int end   = SPECIAL_CASES_INDEXES[base+1]-1;

        while (start <= end)
        {
            int half = (start+end)/2;
            int comp = SPECIAL_CASES_KEYS[half].compareTo(key);

            if (comp == 0)
                return SPECIAL_CASES_VALUES[half];
            else if (comp < 0)
                start = half+1;
            else  //(comp > 0)
                end = half-1;
        }

        return base;
    }

    private boolean hasInput()
    {
        return position < input.length();
    }

    private char nextChar()
    {
        if (hasInput())
            return input.charAt(position++);
        else
            return (char) -1;
    }
}
