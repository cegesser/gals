package gesser.gals.analyser;

public class Token
{
	private int id;
	private String lexeme;
	private int position;
	
	public Token(int id, String lexeme, int position)
	{
		this.id = id;
		this.lexeme = lexeme;
		this.position = position;
	}
		
	public final int getId()
	{
		return id;
	}

	public final String getLexeme()
	{
		return lexeme;
	}
	
	public final int getPosition()
	{
		return position;
	}
	
	public String toString()
	{
		return id+"("+lexeme+")";
	}
}
