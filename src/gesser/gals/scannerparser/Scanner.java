package gesser.gals.scannerparser;

import gesser.gals.analyser.LexicalError;
import gesser.gals.analyser.Token;
import gesser.gals.simulator.BasicScanner;

import static gesser.gals.scannerparser.Constants.*;
/**
 * @author Gesser
 */

public class Scanner implements BasicScanner
{
	private String in;
	private int pos;
	private boolean quote = false;
	
	public Scanner()
	{
		this("");
	}
	
	public Scanner(String str)
	{
		setInput(str);
	}
	
	public void setInput(String in)
	{
		this.in = in;
		pos = 0;
	}	
	
	public int getPosition()
	{
		return pos;
	}
	
	public Token nextToken() throws LexicalError
	{	
		int start = pos;
		
		while (hasMoreChars())
		{
			start = pos;
			
			char c = nextChar();
			
			if (quote)
			{
				if (c == '"')
				{
					if (hasMoreChars())
					{
						c = nextChar();
						if (c == '"')
							return new Token(CHAR, "\"", pos-2);
						else
							pos--;
					}
					quote = false;
					continue;
				}
				else
					return createToken(CHAR, ""+c);
			}
			
			switch (c)
			{
				case ' ':
				case '\n':
				case '\r':
				case '\t':continue;
				
				case '"': quote = true; continue;
				case '|': return createToken( UNION, "|");
				case '*': return createToken( CLOSURE, "*");
				case '+': return createToken( CLOSURE_OB, "+");
				case '?': return createToken( OPTIONAL, "?");
				case '(': return createToken( PARENTHESIS_OPEN, "(");
				case ')': return createToken( PARENTHESIS_CLOSE, ")");				
				case '[': return createToken( BRACKETS_OPEN, "[");
				case ']': return createToken( BRACKETS_CLOSE, "]");
				case '^': return createToken( COMPLEMENT, "^");
				case '.': return createToken( ALL, ".");
				case '-': return createToken( INTERVAL, "-");
				case '\\': return processesAdvChar();
				case '{': return processesDefinition();
				default: return createToken(CHAR, ""+c);				
			}
		}
		
		if (quote)
			throw new LexicalError("Era esperado '\"'", start);
		
		return null;
	}
	
	private Token processesAdvChar() throws LexicalError
	{
		return new Token(CHAR, ""+getSpecialChar(), pos-1);
	}


	public Token createToken(int id, String lexeme)
	{
		return new Token(id, lexeme, pos-1);
	}
	
	/**
	 * Extrai o caracter especial de uma combinação de character especial
	 * */

	private char getSpecialChar() throws LexicalError
	{
		int start = pos;
		
		if (! hasMoreChars())
			throw new LexicalError("Era esperado um Caracter Especial", start);
			
		char c = nextChar();
			
		switch (c)
		{			
			case 'b': return '\b'; //BACKSPACE			
			case 'n': return '\n'; //LINE FEED
			case 'f': return '\f'; //FORM FEED
			case 'r': return '\r'; //CARRIAGE RETURN
			case 'e': return (char)27; //SCAPE
			case 't': return '\t'; //TAB
			case '\t': return '\t'; //TAB
			case 's': return ' '; //SPACE
			case ' ': return ' '; //SPACE
		
			case '"': return '"';
			case '\\': return '\\';	
			case '|': return '|';
			case '*': return '*';
			case '+': return '+';
			case '?': return '?';
			case '(': return '(';
			case ')': return ')';
			case '{': return '{';
			case '}': return '}';
			case '[': return '[';
			case ']': return ']';
			case '.': return '.';		
			case '^': return '^';
			case '-': return '-';
			
			default:				
				if (Character.isDigit(c))
					return getCharByCode(c);
				else
					throw new LexicalError("Caracter especial inválido: '"+c+"'", pos);
		}
	}

	private char getCharByCode(char c) throws LexicalError
	{		
		//c eh um digito de certeza
	
		int start = pos-1;
			
		if (hasMoreChars() && Character.isDigit(nextChar())) //2o char
		{
			if (hasMoreChars() && ! Character.isDigit(nextChar())) //3o char
			{
				pos--;
			}
		}
		else
			pos--;
			
		String n = in.substring(start, pos);
		int value = Integer.parseInt(n);
		if ( value > 255 )
			throw new LexicalError("Valor decimal inválido (>255)", start);
			
		return (char) value;
	}



	/**
	 * Processa os proximos caracteres e retorna um DEFINITION
	 * \{[a-zA-Z][a-zA-Z0-9_]*\}
	 */
	private Token processesDefinition() throws LexicalError
	{
		StringBuffer tok = new StringBuffer();
		int start = pos;
		
		char c = '{';
				
		while (hasMoreChars())
		{
			c = nextChar();		
			
			if (c == '}')
				break;
				
			if (c != '_' && ! Character.isLetterOrDigit(c))
				throw new LexicalError("Caracter inválido em uma definição: '"+c+"'", pos-1);
				
			tok.append(c);
		}
		
		if (c != '}' && !hasMoreChars())
			throw new LexicalError("Fim de expressão inesperado", pos);
		
		return new Token(DEFINITION, tok.toString(), start);
	}
	
	private boolean hasMoreChars()
	{
		return pos < in.length();
	}
	
	private char nextChar()
	{
		if (hasMoreChars())
			return in.charAt(pos++);
		else
			return (char) -1;
	}

}
