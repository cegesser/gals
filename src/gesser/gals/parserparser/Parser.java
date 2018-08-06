package gesser.gals.parserparser;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import gesser.gals.analyser.*;
import gesser.gals.generator.parser.Grammar;
import gesser.gals.generator.parser.Production;
import gesser.gals.util.MetaException;

import static gesser.gals.util.MetaException.Mode.*;

import static gesser.gals.parserparser.Constants.*;

public class Parser
{	
	private Stack<Integer> stack = new Stack<Integer>();
	private Token currentToken;
	private Token previousToken;
	private Scanner scanner;	
	private SemanticAnalyser semanticAnalyser;	
			
	public Grammar parse(List<String> t, List<String> nt, String prods) throws MetaException
	{
		Map<String, Integer> map = new TreeMap<String, Integer>();
        map.put(Grammar.EPSILON_STR, Integer.valueOf(0));
        
        int pos = 2;

        Scanner scanner = new Scanner();

		int lineCount = 0;
		
		Set<String> s = new HashSet<String>();
		
		try
		{			
	        for (int i=0; i<t.size(); i++)
	        {
	        	String line = t.get(i);
	        	if (line.equals("\n"))
	        	{
	        		lineCount++;
	        		t.remove(i);
	        		i--;
	        		continue;
	        	}
	        	
	        	scanner.setInput(line);
	        	Token token = scanner.nextToken();
	        	if (token == null)//linha vazia
	        	{
	        		t.remove(i);
	        		i--;
	        	}
	        	else
	        	{
	        		if (token.getId() != TERM)
	        			throw new SemanticError("Era esperada a declaração de um terminal", token.getPosition());
	        		String str = token.getLexeme();
	        		
					if (s.contains(str))
						throw new SemanticError("Terminal repetido : "+str, token.getPosition());
					else
						s.add(str);
	        		
	        		t.set(i, str);
	        		map.put(str, Integer.valueOf(pos));
	        		pos++;
	        		if ((token=scanner.nextToken()) != null)// mais de um terminal por linha
	        		{
	        			throw new SemanticError("Cada linha deve conter a declaração de apenas um símbolo terminal", token.getPosition());
	        		}
	        	}
	        }
			if (t.size() == 0)
				throw new SemanticError("Conjunto de Terminais não pode ser vazio", 0);
		}
		catch(AnalysisError e)
		{
			throw new MetaException(TOKEN, lineCount, e);
		}
        
        lineCount = 0;
        
        s = new HashSet<String>();
        
        try
        {
	        for (int i=0; i<nt.size(); i++)
	        {
	        	String line = nt.get(i);
				if (line.equals("\n"))
				{
					lineCount++;
					nt.remove(i);
					i--;
					continue;
				}
	        	scanner.setInput(line);
	        	Token token = scanner.nextToken();
	        	if (token == null)//linha vazia
	        	{
	        		nt.remove(i);
	        		i--;
	        	}
	        	else
	        	{
	        		if (token.getId() != NON_TERM)
	        			throw new SemanticError("Era esperada a declaração de um não-terminal", token.getPosition());
					String str = token.getLexeme();
					
					if (s.contains(str))
						throw new SemanticError("Não-terminal repetido : "+str, token.getPosition());
					else
						s.add(str);
					
	        		nt.set(i, str);
	        		map.put(str, Integer.valueOf(pos));
	        		pos++;
	        		if ((token=scanner.nextToken()) != null)// mais de um terminal por linha
	        		{
	        			throw new SemanticError("Cada linha deve conter a declaração de apenas um símbolo não-terminal", token.getPosition());
	        		}
	        	}
	        }
        	if (nt.size() == 0)
        		throw new SemanticError("Conjunto de Não-Terminais não pode ser vazio", 0);
	    }
		catch(AnalysisError e)
		{
			//TODO:throw new MetaException(NON_TERMINAL, lineCount, e);
		}

		try
		{
			parse(prods, map);
		}
		catch(AnalysisError e)
		{
			throw new MetaException(GRAMMAR, -1, e);
		}
		
		List<Production> prodList = semanticAnalyser.getPoductions();
		
		int start = 2+t.size();
        return new Grammar(t, nt, prodList, start);
	}
	
	public void parse(String input, Map<String, Integer> symbols)
		throws LexicalError, SyntaticError, SemanticError
	{
		scanner = new Scanner(input);
		semanticAnalyser = new SemanticAnalyser(symbols);
		
		stack.push(Integer.valueOf(DOLLAR));
		stack.push(Integer.valueOf(START_SYMBOL));
		
		currentToken = scanner.nextToken();
		
		while ( ! step() ) 
			; //faz nada
	}
	
	public boolean step() throws LexicalError, SyntaticError, SemanticError
	{			
		int x = stack.pop().intValue();
		int a;
		
		if (currentToken == null)
			a = DOLLAR;
		else
			a = currentToken.getId();
				
		if (x == EPSILON)
		{
			return false;
		}
		else if (isTerminal(x))
		{
				if (x == a)
				{
					if (stack.empty())
						return true;
					else
					{
						previousToken = currentToken;
						currentToken = scanner.nextToken();
						return false;
					}
				}
				else
				{
					throw new SyntaticError("Era esperado "+EXPECTED_MESSAGE[x], scanner.getPosition());
				}
		}
		else if (isNonTerminal(x))
		{
			int p = TABLE[x-FIRST_NON_TERMINAL][a-1];
			if (p >= 0)
			{
				int[] production = PRODUCTIONS[p];
				//empilha a produção em ordem reversa
				for (int i=production.length-1; i>=0; i--)
				{
					stack.push(Integer.valueOf(production[i]));
				}
				return false;
			}
			else
			{
				throw new SyntaticError(PARSER_ERROR[x-FIRST_NON_TERMINAL], scanner.getPosition());
			}
		}
		else if (isSemanticAction(x))
		{
			semanticAnalyser.executeAction(x-FIRST_SEMANTIC_ACTION, previousToken);
			return false;
		}
		else
		{
			//ERRO: impossivel
			//assert false : "Erro Impossivel";
			return false;
		}
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
