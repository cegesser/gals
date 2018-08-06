package gesser.gals.scannerparser;
/**
 * @author Gesser
 */

public final class Constants
{
	private Constants() {}
	
	public static final int EPSILON  = 0;
	public static final int DOLLAR   = 1;
    
	
	public static final int UNION             =  2; // |
	public static final int CLOSURE           =  3; // *
	public static final int CLOSURE_OB        =  4; // +
	public static final int OPTIONAL          =  5; // ?
	public static final int PARENTHESIS_OPEN  =  6; // (
	public static final int PARENTHESIS_CLOSE =  7; // )
	public static final int BRACKETS_OPEN     =  8; // [
	public static final int BRACKETS_CLOSE    =  9; // ]
	public static final int ALL               = 10; // .
	public static final int COMPLEMENT        = 11; // ^
	public static final int INTERVAL          = 12; //  -
	public static final int DEFINITION        = 13; // \{[a-zA-Z][a-zA-Z0-9_]*\}
	public static final int CHAR              = 14; // CHAR
	
	public static final int START_SYMBOL = 15;

	public static final int FIRST_NON_TERMINAL    = 15;
	public static final int FIRST_SEMANTIC_ACTION = 26;

	public static final int[][] SYNT_TABLE =
    {
        { -1, -1, -1, -1, -1,  0, -1,  0, -1, -1,  0, -1,  0,  0 },
        { -1, -1, -1, -1, -1,  1, -1,  1, -1, -1,  1, -1,  1,  1 },
        {  3,  2, -1, -1, -1, -1,  3, -1, -1, -1, -1, -1, -1, -1 },
        {  5,  5, -1, -1, -1,  4,  5,  4, -1, -1,  4, -1,  4,  4 },
        { -1, -1, -1, -1, -1,  6, -1,  6, -1, -1,  6, -1,  6,  6 },
        { 10, 10,  7,  8,  9, 10, 10, 10, -1, -1, 10, -1, 10, 10 },
        { -1, -1, -1, -1, -1, 11, -1, 12, -1, -1, 13, -1, 14, 15 },
        { -1, -1, -1, -1, -1, -1, -1, -1, -1, 16, -1, -1, -1, 17 },
        { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 18 },
        { -1, -1, -1, -1, -1, -1, -1, -1, 20, -1, -1, 19, -1, 20 },
        { -1, -1, -1, -1, -1, -1, -1, -1, 22, -1, -1, -1, -1, 21 }
    };

	public static final int[][] PRODUCTIONS = 
    {
        { 16, 27, 17 },
        { 19, 28, 18 },
        {  2, 16, 29, 17 },
        {  0 },
        { 19, 30, 18 },
        {  0 },
        { 21, 20 },
        {  3, 31 },
        {  4, 32 },
        {  5, 33 },
        {  0 },
        {  6, 34, 15,  7, 35 },
        {  8, 22 },
        { 11, 38 },
        { 13, 40 },
        { 14, 39 },
        { 10, 23, 25,  9, 36 },
        { 23, 25,  9 },
        { 14, 39, 24 },
        { 12, 14, 41 },
        {  0 },
        { 23, 25, 37 },
        {  0 }
    };
}
