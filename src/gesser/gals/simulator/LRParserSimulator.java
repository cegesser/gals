package gesser.gals.simulator;

import java.util.List;
import java.util.Stack;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import gesser.gals.analyser.AnalysisError;
import gesser.gals.analyser.LexicalError;
import gesser.gals.analyser.SemanticError;
import gesser.gals.analyser.SyntaticError;
import gesser.gals.analyser.Token;
import gesser.gals.generator.parser.Production;
import gesser.gals.generator.parser.lr.Command;
import gesser.gals.generator.parser.lr.LRGenerator;

import static gesser.gals.generator.parser.lr.Command.Type.*;

public class LRParserSimulator
{
	private Stack<Integer> stack = new Stack<Integer>();
	
	private BasicScanner scanner;
	private Token currentToken = null;
	private Token previousToken = null;
	
	private Command[][] table;
	private int[][] productions;
	private int semanticStart;
	
	private String[] symbols;
	private Stack<MutableTreeNode> nodeStack = new Stack<MutableTreeNode>();
	
	private List<String> errors;
	
	public static final int DOLLAR = 1;
	
	public LRParserSimulator(LRGenerator parser)
	{
		table = parser.buildTable();
		semanticStart = parser.getFirstSemanticAction();
		List<Production> pl = parser.getGrammar().getProductions();
		productions = new int[pl.size()][2];
		
		symbols = parser.getGrammar().getSymbols();
		
		for (int i=0; i<pl.size(); i++)
		{
			productions[i][0] = pl.get(i).get_lhs();
			productions[i][1] = pl.get(i).get_rhs().size();
		}
		
		errors = parser.getErrors(table);
	}
	
	public void parse(BasicScanner scanner, DefaultMutableTreeNode root) throws SemanticError, SyntaticError, SyntaticError, LexicalError
	{
		this.scanner = scanner;

		nodeStack.clear();

		stack.clear();
		stack.push(Integer.valueOf(0));
		
		currentToken = scanner.nextToken();
		
		try
		{
			while ( ! step() ) 
				; //faz nada
			root.add(nodeStack.pop());
		}
		catch(AnalysisError e)
		{
			for (int i=0; i<nodeStack.size(); i++)
				root.add(nodeStack.get(i));
			root.add(new DefaultMutableTreeNode(e.getMessage()));
			
			e.printStackTrace();
		}			
	}
	
	private boolean step() throws SyntaticError, SemanticError, LexicalError
	{
		int state = stack.peek().intValue();
		
		if (currentToken == null)
		{
			int pos = 0;
			if (previousToken != null)
				pos = previousToken.getPosition()+previousToken.getLexeme().length();

			currentToken = new Token(DOLLAR, "$", pos);
		}
		
    	int token = currentToken.getId();
		
		Command cmd = table[state][token-1];
		
		switch (cmd.getType())
		{
			case SHIFT:
				stack.push(Integer.valueOf(cmd.getParameter()));
				
				nodeStack.push(new DefaultMutableTreeNode(symbols[currentToken.getId()]));
				
				previousToken = currentToken;
				currentToken = scanner.nextToken();
				return false;
				
			case REDUCE:				
				int[] prod = productions[cmd.getParameter()];
				
				Stack<MutableTreeNode> tmp = new Stack<MutableTreeNode>();
				for (int i=0; i<prod[1]; i++)
				{
					stack.pop();
					tmp.push(nodeStack.pop());					
				}
				int oldState = stack.peek().intValue();
				stack.push(Integer.valueOf(table[oldState][prod[0]-1].getParameter()));
				
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(symbols[prod[0]]);				
				while (!tmp.isEmpty())
				{				
					node.add(tmp.pop());
				}
				nodeStack.push(node);
				return false;
				
			case ACTION:
				int action = semanticStart + cmd.getParameter() - 1;
				stack.push(Integer.valueOf(table[state][action].getParameter()));
				nodeStack.push(new DefaultMutableTreeNode("#"+cmd.getParameter()));
				//semanticAnalyser.executeAction(cmd.getParameter(), previousToken);
				return false;
			/*	
			case Command.GOTO:
				break;
			*/	
			case ACCEPT:
				return true;
				
			case ERROR:
				throw new SyntaticError("Era esperado: "+errors.get(state), currentToken.getPosition());
		}
		return false;
	}
}