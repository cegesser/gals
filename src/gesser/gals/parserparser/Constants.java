package gesser.gals.parserparser;

public final class Constants
{
	private Constants() {}
	
	public static final int EPSILON = 0;
	public static final int DOLLAR  = 1;
	
	public static final int DERIVES = 2; // ::=
	public static final int PIPE=  3; // |
	public static final int SEMICOLON = 4; // ;
	public static final int TERM = 5;
	public static final int NON_TERM = 6; 
	public static final int ACTION = 7;
	
	
	public static final int START_SYMBOL          = 8;
	public static final int FIRST_NON_TERMINAL    = 8;
	public static final int FIRST_SEMANTIC_ACTION = 17;
	public static final int LAST_SEMANTIC_ACTION  = 22;
    
	public static final int[][] TABLE = 
		{
	 		{ -1, -1, -1, -1, -1,  0, -1 },
	 		{  2, -2, -2, -2, -2,  1, -2 },
	 		{ -3, -3, -3, -3, -3,  3, -3 },
	 		{ -4, -4,  4,  5, -4, -4, -4 },
	 		{ -5, -5, -5, -1,  6,  7,  8 },
	 		{ -6, -6, 10, 10,  9,  9,  9 },
	 		{ -7, -7, -7, -7, 11,  0, -7 },
	 		{ -8, -8, -8, -8, -8, 12, -8 },
	 		{ -9, -9, -9, -9, -9, -9, 13 }
		};
		
	public static final int[][] PRODUCTIONS = 
		{
			{ 10, 9 },   // <LISTA_PROD> ::= <PROD> <RESTO_LISTA_PROD>
			{ 8 },       // <RESTO_LISTA_PROD> ::= <LISTA_PROD>
			{ 0 },       // <RESTO_LISTA_PROD> ::= EPSILON
			{ 15, 17, 2, 12, 18, 11, 4 }, // <PROD> ::= <NT> #1 "::=" <RHS> #2 <RESTO_PROD> ";"
			{ 3, 12, 18, 11 }, // <RESTO_PROD> ::= "|" <RHS> #2 <RESTO_PROD>
			{ 0 },      // <RESTO_PROD> ::= EPSILON
			{ 14, 19, 13 }, // <RHS> ::= <T> #3 <RESTO_RHS>
			{ 15, 19, 13 }, // <RHS> ::= <NT> #3 <RESTO_RHS>
			{ 16, 20, 13 }, // <RHS> ::= <ACTION> #4 <RESTO_RHS>
			{ 12 },     // <RESTO_RHS> ::= <RHS>
			{ 0 },      // <RESTO_RHS> ::= EPSILON
			{ 5, 21 },      // <T> ::= terminal #5
			{ 6, 21 },      // <NT> ::= nao_terminal #5
			{ 7, 22 }       // <ACTION> ::= action #6
		};
		
	public static final String[] EXPECTED_MESSAGE = 
		{
			"î",
			"$",
			"::=",
			"|",
			";",
			"um símbolo terminal",
			"um símbolo não-terminal",
			"uma ação semântica"
		};
		
	public static final String[] PARSER_ERROR = 
		{
			"Era esperado um Não-Terminal (Início de produção)",
			"Era esperado um Não-Terminal (Início de produção)",
			"Era esperado um Não-Terminal",
			"Era esperado '|' ou ';'",
			"Era esperado um Terminal, um Não-Terminal, ou uma Ação Semântica",
			"Construção inválida",
			"Era esperado um Terminal",
			"Era esperado um Não-Terminal",
			"Era esperado uma Ação Semântica"
		};
}
