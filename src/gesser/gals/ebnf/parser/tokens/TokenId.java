package gesser.gals.ebnf.parser.tokens;

public abstract enum TokenId
{
    EPSILON_VALUE
	{
    	public Token createToken(int position, String lexeme)
    	{
    		throw new UnsupportedOperationException();
    	}
	},
    DOLLAR
    {
    	public Token createToken(int position, String lexeme)
    	{
    		return new EofToken(position);
    	}
	},
    ASTERIX //"*"
    {
    	public Token createToken(int position, String lexeme)
    	{
    		return new AsterixToken(position);
    	}
	},
    PLUS //"+"
    {
    	public Token createToken(int position, String lexeme)
    	{
    		return new PlusToken(position);
    	}
	},
    INTERROGATION //"?"
    {
    	public Token createToken(int position, String lexeme)
    	{
    		return new InterrogationToken(position);
    	}
	},
    LEFT_PARENTHESIS //"("
    {
    	public Token createToken(int position, String lexeme)
    	{
    		return new LeftParenthesisToken(position);
    	}
	},
    RIGHT_PARENTHESIS //")"
    {
    	public Token createToken(int position, String lexeme)
    	{
    		return new RightParenthesisToken(position);
    	}
	},
    PIPE //"|"
    {
    	public Token createToken(int position, String lexeme)
    	{
    		return new PipeToken(position);
    	}
	},
    SEMI_COLON //";"
    {
    	public Token createToken(int position, String lexeme)
    	{
    		return new SemiColonToken(position);
    	}
	},
    DERIVES //"::="
    {
    	public Token createToken(int position, String lexeme)
    	{
    		return new DerivesToken(position);
    	}
	},
    TERMINAL
    {
    	public Token createToken(int position, String lexeme)
    	{
    		return new TerminalToken(position, lexeme);
    	}
	},
    NON_TERMINAL
    {
    	public Token createToken(int position, String lexeme)
    	{
    		return new NonTerminalToken(position, lexeme);
    	}
	},
    SEMANTIC_ACTION
    {
    	public Token createToken(int position, String lexeme)
    	{
    		return new SemanticActionToken(position, lexeme);
    	}
	},
    EPSILON
    {
    	public Token createToken(int position, String lexeme)
    	{
    		return new EpsilonToken(position);
    	}
	},
    WHITE_SPACE
    {
    	public Token createToken(int position, String lexeme)
    	{
    		return new WhiteSpaceToken(position, lexeme);
    	}
	},
    COMMENT
    {
    	public Token createToken(int position, String lexeme)
    	{
    		return new CommentaryToken(position, lexeme);
    	}
	};
	
	public abstract Token createToken(int position, String lexeme);
}
