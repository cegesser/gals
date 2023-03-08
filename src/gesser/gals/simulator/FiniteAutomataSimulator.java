package gesser.gals.simulator;

import gesser.gals.analyser.LexicalError;
import gesser.gals.analyser.Token;
import gesser.gals.generator.OptionsDialog;
import gesser.gals.generator.scanner.FiniteAutomata;

/**
 * @author Gesser
 */

public class FiniteAutomataSimulator implements BasicScanner
{
	private FiniteAutomata fa;
	private String input = "";
	private int position = 0;
	private boolean sensitive = true; 
	
	public FiniteAutomataSimulator(FiniteAutomata fa)
	{
		this.fa = fa;		
		sensitive = OptionsDialog.getInstance().getOptions().scannerCaseSensitive;
	}
	
	public int analyse(String str)
	{
		int state = 0;
		
		for (int i=0; i<str.length(); i++)
		{
			state = fa.nextState(str.charAt(i), state);
			
			if (state <= 0)
				return -1;
		}
		return fa.tokenForState(state);
	}
	
	public void setInput(String text)
	{
		this.input = text;
		position = 0;
	}
	
	public Token nextToken() throws LexicalError
	{
		if ( ! hasInput() )
			return null;
		
		int start = position;
		
		int state = 0;
		int lastState = 0;
		int endState = -1;
		int end = -1;
		int ctxtState = -1;
		int ctxtEnd = -1;
		
		while (hasInput())
		{	
			lastState = state;	
			state = fa.nextState(nextChar(), state);
			
			if (state < 0)
			{
				break;				
			}
			else
			{
				int tfs = fa.tokenForState(state);
				if (tfs >= 0)
				{
					endState = state;
					end = position;
				}
				if (fa.isContext(state))
				{
					ctxtState = state;
					ctxtEnd = position;
				}
			}
		}
				
		if (endState < 0 || (endState != state && fa.tokenForState(lastState) == -2))
			throw new LexicalError( fa.getError(lastState), start);
		
		if (ctxtState != -1 && fa.getOrigin(endState) == ctxtState)
			end = ctxtEnd;
		position = end;
		
		int token = fa.tokenForState(endState);
		
		if (token == 0)
			return nextToken();
		else
		{
			String lexeme = input.substring(start, end);
			token = lookupToken(token, lexeme);
			return new Token(token, lexeme, start);
		}
	}
	
	public int lookupToken(int base, String key)
	{
		int start = fa.getSpecialCasesIndexes()[base][0];
		int end   = fa.getSpecialCasesIndexes()[base][1]-1;

		if (!sensitive)
			key = key.toUpperCase();

		while (start <= end)
		{
			int half = (start+end)/2;
			int comp = fa.getSpecialCases()[half].key.compareTo(key);

			if (comp == 0)
				return fa.getSpecialCases()[half].value;
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
