package gesser.gals.simulator;

import java.util.List;
import java.util.Stack;

import javax.swing.tree.DefaultMutableTreeNode;

import gesser.gals.analyser.*;
import gesser.gals.generator.parser.*;
import gesser.gals.generator.parser.ll.*;
import gesser.gals.util.IntList;

public class LL1ParserSimulator
{
	public static final int EPSILON = 0;
    public static final int DOLLAR = 1;
    public static final int FIRST_TERMINAL = 2;
    public final int FIRST_NON_TERMINAL;
    public final int FIRST_SEMANTIC_ACTION;
    public final int LAST_SEMANTIC_ACTION;
	
    public final int START_SYMBOL;
    
    private Grammar grammar;
	private BasicScanner scanner;	
	
	private int[][] table;
	private int[][] productions;	
	
	private Stack<Integer> stack = new Stack<Integer>();
	private Token currentToken;
	
	String[] symb;
	DefaultMutableTreeNode node;
	
	public LL1ParserSimulator(LLParser parser) 
	{
		this.grammar = parser.getGrammar();
		table = parser.generateTable();
		FIRST_NON_TERMINAL = grammar.FIRST_NON_TERMINAL;
		FIRST_SEMANTIC_ACTION = grammar.FIRST_SEMANTIC_ACTION();
		LAST_SEMANTIC_ACTION = grammar.LAST_SEMANTIC_ACTION();
		START_SYMBOL = grammar.getStartSymbol();
		
		List<Production> p = grammar.getProductions();
		productions = new int[p.size()][];
		for (int i=0; i< p.size(); i++)
		{
			IntList rhs = p.get(i).get_rhs();
			if (rhs.size() > 0)
			{
				productions[i] = new int[rhs.size()];
				for (int j=0; j<rhs.size(); j++)
					productions[i][j] = rhs.get(j);
			}
			else
				productions[i] = new int[]{0};
		}
		
		symb = grammar.getSymbols();
	}
	
	Stack<Integer> nodeCount = new Stack<Integer>();
	
	public boolean step() throws LexicalError, SyntaticError, SemanticError
	{		
		if (currentToken == null)
		{
			currentToken = new Token(DOLLAR, "$", 0);
		}
		int x = stack.pop().intValue();
		int a = currentToken.getId();
				
		if (x == EPSILON)
		{
			node.add(new DefaultMutableTreeNode("EPSILON"));
			
			Integer itg = nodeCount.pop();
			while (itg.intValue() == 1)
			{
				node = (DefaultMutableTreeNode) node.getParent();
				if ( nodeCount.size() > 0 )
					itg = nodeCount.pop();
				else
					break;
			}
			nodeCount.push(Integer.valueOf(itg.intValue()-1));			
			
			return false;
		}
		else if (isTerminal(x))
		{
			node.add(new DefaultMutableTreeNode(symb[a]));
			
			Integer itg = nodeCount.pop();
			while (itg.intValue() == 1)
			{
				node = (DefaultMutableTreeNode) node.getParent();
				if ( nodeCount.size() > 0 )
					itg = nodeCount.pop();
				else
					break;
			}
			nodeCount.push(Integer.valueOf(itg.intValue()-1));	
			
			if (x == a)
			{
				if (stack.empty())
					return true;
				else
				{
					currentToken = scanner.nextToken();
					return false;
				}
			}
			else
			{
				node.add(new DefaultMutableTreeNode("ERRO SINTÁTICO: Era esperado "+symb[x]));
				throw new SyntaticError("Era esperado "+symb[x], currentToken.getPosition());
			}
		}
		else if (isNonTerminal(x))
		{
			int p = table[x-FIRST_NON_TERMINAL][a-1];
			if (p != -1)
			{	
				int[] production = productions[p];
				//empilha a produção em ordem reversa
				for (int i=production.length-1; i>=0; i--)
				{
					stack.push(Integer.valueOf(production[i]));
				}
				DefaultMutableTreeNode n = new DefaultMutableTreeNode(symb[x]);
				node.add(n);
				node = n;
				nodeCount.push(Integer.valueOf(production.length));
				return false;
			}
			else
			{
				node.add(new DefaultMutableTreeNode("ERRO SINTÁTICO: "+symb[a]+" inesperado"));
				throw new SyntaticError(symb[a]+" inesperado", currentToken.getPosition());
			}
		}
		else if (isSemanticAction(x))
		{
			node.add(new DefaultMutableTreeNode("#"+(x-FIRST_SEMANTIC_ACTION)));
			
			Integer itg = nodeCount.pop();
			while (itg.intValue() == 1)
			{
				node = (DefaultMutableTreeNode) node.getParent();
				if ( nodeCount.size() > 0 )
					itg = nodeCount.pop();
				else
					break;
			}
			nodeCount.push(Integer.valueOf(itg.intValue()-1));	
			
			return false;
		}
		else
		{
			//ERRO: impossivel
			//assert false : "Erro Impossivel";
			return false;
		}
	}
	
	public void parse(BasicScanner scnr, DefaultMutableTreeNode root) throws LexicalError, SyntaticError, SemanticError
	{			
		scanner = scnr;
		node = root;
		nodeCount.clear();
		stack.clear();
		
		stack.push(Integer.valueOf(DOLLAR));
		stack.push(Integer.valueOf(START_SYMBOL));
		
		currentToken = scanner.nextToken();
		
		while ( ! step() ) 
			; //faz nada
	}
	
	/**
     * @return TRUE se x eh um símbolo terminal
     */
    private final boolean isTerminal(int x)
    {
        return x >= 0 && x < FIRST_NON_TERMINAL;
    }

    /**
     * @return TRUE se x eh um símbolo não terminal
     */
    private final boolean isNonTerminal(int x)
    {
        return x >= FIRST_NON_TERMINAL && x < FIRST_SEMANTIC_ACTION;
    }
    
    /**
     * @return TRUE se x eh uma Ação Semântica
     */
    private final boolean isSemanticAction(int x)
    {
        return x >= FIRST_SEMANTIC_ACTION && x <= LAST_SEMANTIC_ACTION;
    }
}
