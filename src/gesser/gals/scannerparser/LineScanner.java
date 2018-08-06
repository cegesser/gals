package gesser.gals.scannerparser;

import gesser.gals.analyser.Token;

/**
 * @author Gesser
 */

public class LineScanner 
{
	public static final int ID      = 0;
	public static final int STR     = 1;
	public static final int RE      = 2;
	public static final int COLON   = 3;
	public static final int EQUALS  = 4;
	public static final int COMMENT  = 5;
	public static final int ERROR   = 6;
	
	private String text;
	private int pos = 0;
	private int endPos;
	private boolean reMode = false;
	private boolean scMode = false;	
	
	public void setText(String text)
	{
		this.text = text;
		setRange(0, text.length());
		reMode = false;
		scMode = false;
	}
	
	public void setRange(int start, int end)
	{
		pos = start;
		endPos = end;
	}
	
	public Token nextToken()
	{
		if ( ! hasMoreChars() )
			return null;
		
		if (reMode)
		{
			if (scMode)
			{
				scMode = false;
				reMode= false;
				return nextToken();
			}
			return parseRE();
		}
		else
		{
			while ( hasMoreChars() )
			{
				int start = pos;
				char c = nextChar();
				
				switch (c)
				{
					case '\n':
					case '\r':
						scMode = false;
						reMode = false;
					case ' ':
					case '\t':					
						continue;
					case ':':
						reMode = true;
						return new Token(COLON, ":", start);
					case '=':
						scMode = true;
						return new Token(EQUALS, "=", start);
					case '"':
						return getString();
					case '/':
						return getComment();
					default:
						if (Character.isLetter(c))
							return getId();
						else
							return getError();
				}
			}
			return null;
		}
	}

	private Token parseRE()
	{
		int start = pos;
		reMode = false;
		
		while ( hasMoreChars() )
		{
			char c = nextChar();
			if (c == '\n')
			{
				pos--;
				break;
			}
			else if (c == '/')
			{
				if ( hasMoreChars() )
				{
					if (nextChar() == '/')
					{
						pos -= 2;
						reMode = false;
						return new Token(RE, text.substring(start, pos), start);
					}
					pos--;			
				}
			}
		}
		String tok = text.substring(start, pos);
		return new Token(RE, tok, start);
	}

	private Token getString()
	{
		int start = pos-1;
		
		while ( hasMoreChars() )
		{
			char c = nextChar();
			if (c == '\n')
				break;
			else if (c == '"')
			{
				if (hasMoreChars())
				{
					if (nextChar() != '"')
					{
						pos--;
						return new Token(STR, text.substring(start, pos), start);
					}
				}
				else
					return new Token(STR, text.substring(start, pos), start);
			}
		}
		return new Token(ERROR, text.substring(start, pos), start);
	}
	
	private Token getId()
	{
		int start = pos-1;
		
		while ( hasMoreChars() )
		{
			char c = nextChar();
			if (! Character.isLetterOrDigit(c) && c != '_' )
			{
				pos--;
				break;
			}
		}
			
		return new Token(ID, text.substring(start, pos), start);
	}
	
	private Token getError()
	{
		int start = pos-1;
		
		while ( hasMoreChars() )
		{
			if ( " \t\n\r".indexOf(nextChar()) == -1 )
			{
				pos--;
				break;
			}
		}
			
		return new Token(ERROR, text.substring(start, pos), start);
	}
	
	private Token getComment()
	{
		int start = pos-1;
		
		if ( hasMoreChars() )
		{
			if (nextChar() == '/')
			{
				while ( hasMoreChars() )
				{
					if (nextChar() == '\n')
					{
						pos--;
						break;
					}
				}
				return new Token(COMMENT, text.substring(start, pos), start);
			}
			pos--;			
		}
		return new Token(ERROR, text.substring(start, pos), start);
	}
	
	private boolean hasMoreChars()
	{
		return pos < endPos;
	}
	
	private char nextChar()
	{
		if (hasMoreChars())
			return text.charAt(pos++);
		else
			return (char) -1;
	}
}
