package gesser.gals.ebnf.parser;

import gesser.gals.analyser.AnalysisError;
import gesser.gals.analyser.SyntaticError;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import gesser.gals.ebnf.SymbolManager;
import gesser.gals.ebnf.decl.ProductionDecl;
import static gesser.gals.ebnf.parser.ParserConstants.*;
import gesser.gals.ebnf.parser.tokens.ErrorToken;
import gesser.gals.ebnf.parser.tokens.IgnorableToken;
import gesser.gals.ebnf.parser.tokens.Token;
import gesser.gals.ebnf.parser.tokens.TokenId;
import static gesser.gals.ebnf.parser.tokens.TokenId.*;

public class Parser
{
    private Stack<Integer> stack = new Stack<Integer>();
    private Token current;
    private Token previous;
    private Iterator<Token> scanner;
    private Generator gen;

    private static final boolean isTerminal(int x)
    {
        return x < FIRST_NON_TERMINAL;
    }

    private static final boolean isNonTerminal(int x)
    {
        return x >= FIRST_NON_TERMINAL && x < FIRST_SEMANTIC_ACTION;
    }

    private boolean step() throws AnalysisError
    {
    	while (current instanceof IgnorableToken)
    	{
    		current = scanner.next();
    	}
    	
        int x = stack.pop().intValue();
        
        TokenId a = current.getId();

        if (a == null)
        {
        	ErrorToken et = (ErrorToken) current;
        	throw et.getError();
        }
        else if (x == 0)
        {
            return true;
        }
        else if (isTerminal(x))
        {
            if (x == a.ordinal())
            {
                if (stack.empty())
                    return false;
                else
                {
                    previous = current;
                    current = scanner.next();
                    return true;
                }
            }
            else
            {
                throw new SyntaticError(PARSER_ERROR[x], current.getStart());
            }
        }
        else if (isNonTerminal(x))
        {
            if (pushProduction(x, a))
                return true;
            else
                throw new SyntaticError(PARSER_ERROR[x], current.getStart());
        }
        else // isSemanticAction(x)
        {
            gen.executeAction(x-FIRST_SEMANTIC_ACTION, previous);
            return true;
        }
    }
    
    private boolean pushProduction(int topStack, TokenId input)
    {
        int p = PARSER_TABLE[topStack-FIRST_NON_TERMINAL][input.ordinal()-1];
        if (p >= 0)
        {
            int[] production = PRODUCTIONS[p];
            //empilha a produção em ordem reversa
            for (int i=production.length-1; i>=0; i--)
            {
                stack.push(new Integer(production[i]));
            }
            return true;
        }
        else
            return false;
    }

    public List<ProductionDecl> parse(List<Token> input, SymbolManager sm) throws AnalysisError
    {
    	this.scanner = input.iterator();
        this.gen = new Generator(sm);

        stack.clear();
        stack.push(new Integer(DOLLAR.ordinal()));
        stack.push(new Integer(START_SYMBOL));

        current = scanner.next();

        while ( step() )
            ;
        
        return gen.getProductions();
    }
}
