package gesser.gals.generator.parser;

import gesser.gals.util.IntList;

/**
 * Uma produção de uma gramática
 * Esta produçao é da forma A -> X1 X2 X3 ... Xn
 *
 * @author Carlos Eduardo Gesser
 */

public class Production implements Comparable
{
    /**
     * Lado esquerdo da produçao
     */
    private int lhs;

    /**
     * Lado direito da produção
     */
    private IntList rhs;
    
    private Grammar grammar;

	public Production(int lhs, int... rhs)
	{
		this(null, lhs, rhs);
	}

	public Production(Grammar g, int lhs, IntList rhs)
    {
    	this.grammar = g;
        this.lhs = lhs;
        this.rhs = rhs;
    }    

    public Production(Grammar g, int lhs, int... rhs)
    {
    	this(g, lhs, new IntList(rhs));
    }
    
    public int get_lhs() { return lhs; }
    
    public void set_lhs(int lhs)
    {
    	this.lhs = lhs;
    }
    
    public IntList get_rhs() { return rhs; }
    
    public int firstSymbol()
    {
    	for (int i=0; i<rhs.size(); i++)
    		if (! grammar.isSemanticAction(rhs.get(i)))
    			return rhs.get(i);
    	return 0;
    }
    
    public void setGrammar(Grammar g)
    {
    	grammar = g;
    }

	public Grammar getGrammar()
	{
		return grammar;
	}

    public String toString()
    {
        StringBuffer bfr = new StringBuffer();
        bfr.append(grammar.symbols[lhs]).append(" ::=");
        if (rhs.size() == 0)
        	bfr.append(" "+Grammar.EPSILON_STR);
        else
	        for (int j = 0; j < rhs.size(); j++)
	        {
	        	if (grammar.isSemanticAction(rhs.get(j)))
	        	{
	        		bfr.append(" #").append(rhs.get(j) - grammar.FIRST_SEMANTIC_ACTION());
	        	}
	        	else
	            	bfr.append(" ").append(grammar.symbols[rhs.get(j)]);
	        }
        return bfr.toString();
    }

    public boolean equals(Object obj)
    {
        Production p = (Production)obj;
        if (lhs != p.lhs)
            return false;
        else if (rhs.size() != p.rhs.size())
            return false;
        else
            for (int i=0; i< rhs.size(); i++)
                if (rhs.get(i) != p.rhs.get(i))
                    return false;
        return true;
    }

    public int compareTo(Object o)
    {
        Production p = (Production)o;
        if (lhs != p.lhs)
            return lhs - p.lhs;
        else
        {
        	boolean e1 = grammar.isEpsilon(rhs);
        	boolean e2 = grammar.isEpsilon(p.rhs);
        	if (e1 && e2)
        		return 0;
        	else if (e1)
        		return 1;
        	else if (e2)
        		return -1;
        	else
	            for (int i=0; i<rhs.size() && i<p.rhs.size(); i++)
	            { 
	                if (rhs.get(i) != p.rhs.get(i))
	                {
	                    return rhs.get(i) - p.rhs.get(i);
	                }
	            }
        }
        // Se ficou tudo igual até agora, a maior é a que ainda tem coisa;
        return p.rhs.size() - rhs.size();
    }
}
