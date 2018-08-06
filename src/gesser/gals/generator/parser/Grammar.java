package gesser.gals.generator.parser;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import gesser.gals.HTMLDialog;
import gesser.gals.ebnf.NonTerminal;
import gesser.gals.ebnf.SemanticAction;
import gesser.gals.ebnf.Symbol;
import gesser.gals.ebnf.SymbolManager;
import gesser.gals.ebnf.Terminal;
import gesser.gals.util.IntList;
import gesser.gals.util.IntegerSet;

/**
 * A classe Grammar representa as Gramáticas Livres de Contexto, utilizadas
 * pelos análisadores sintáticos
 *
 * @author Carlos Eduardo Gesser
 */

public class Grammar implements Cloneable
{
    public static final int EPSILON = 0;
    public static final int DOLLAR = 1;
    public static final int FIRST_TERMINAL = EPSILON+2;
    
    public static final String EPSILON_STR = "î";

    protected String[] symbols;
    public int FIRST_NON_TERMINAL = 0;
    public int FIRST_SEMANTIC_ACTION() { return symbols.length; }
    public int LAST_SEMANTIC_ACTION() { return FIRST_SEMANTIC_ACTION()+SEMANTIC_ACTION_COUNT; }
    public int SEMANTIC_ACTION_COUNT = 0;
    protected int startSymbol;
    
    public IntegerSet[] firstSet;
    public IntegerSet[] followSet;
    
    private boolean normalLR = false;

    protected List<Production> productions = new ArrayList<Production>();
    
    public Grammar(gesser.gals.ebnf.EbnfGrammar g)
    {
    	System.out.println("FROM:");
    	System.out.println(g);
    	
    	SymbolManager sm = g.getSymbolManager();
    	
		String[] t = new String[sm.getTerminals().size()-2];
    	for (Terminal x : sm.getTerminals())
    		if (x.getNumber() >=2)
    			t[x.getNumber()-2] = x.getLexeme();
    	
    	String[] nt = new String[sm.getNonTerminals().size()];
    	for (NonTerminal x : sm.getNonTerminals())
    		nt[x.getNumber()] = x.getLexeme();
    	
    	setSymbols(t, nt, sm.getFirstNonTerminal().getNumber()+t.length+2);
    	
    	List<Production> prods = new ArrayList<Production>();
    	for (gesser.gals.ebnf.Production p : g.getProductions())
    	{
    		int[] rhs = new int[p.getRhs().size()];
    		for (int i=0; i<rhs.length; i++)
    		{
    			Symbol s = p.getRhs().get(i);
    			if (s instanceof NonTerminal)
    				rhs[i] = t.length + 2 + s.getNumber();
    			else if (s instanceof SemanticAction)
    				rhs[i] = t.length + 2 + nt.length + s.getNumber();
    			else
    				rhs[i] = s.getNumber();
    		}
    		
    		prods.add(new Production(t.length+2+p.getLhs().getNumber(), rhs));
    	}
    	
    	setProductions(prods);
        fillFirstSet();
        fillFollowSet();
        
        System.out.println(this);
    }
    
    /**
     * Contrói um objeto do tipo Grammar
     *
     * @param t símbolos terminais
     * @param n símbolos não terminais
     * @param p produções
     * @param startSymbol súimbolo inicial da gramática
     */
    public Grammar(String[] t, String[] n, List<Production> p, int startSymbol)
    {        
        setSymbols(t, n, startSymbol);
        setProductions(p);
        fillFirstSet();
        fillFollowSet();        
    }

	/**
     * Contrói um objeto do tipo Grammar
     *
     * @param t símbolos terminais
     * @param n símbolos não terminais
     * @param p produções
     * @param startSymbol súimbolo inicial da gramática
     */
	public Grammar(List<String> t, List<String> n, List<Production> p, int start)
	{
		String[] T = new String[t.size()];
    	System.arraycopy(t.toArray(), 0, T, 0, T.length);
    	String[] N = new String[n.size()];
    	System.arraycopy(n.toArray(), 0, N, 0, N.length);
		
		setSymbols(T, N, start);
        setProductions(p);
        fillFirstSet();
        fillFollowSet();
	}

    
    /**
     * Preenche os símbolos e inicializa arrays;
     *
     * @param t símbolos terminais
     * @param n símbolos não terminais
     */
    private void setSymbols(String[] t, String[] n, int startSymbol)
    {
        symbols = new String[t.length + n.length + 2];
        FIRST_NON_TERMINAL = t.length + 2;
        symbols[EPSILON] = EPSILON_STR;
        symbols[DOLLAR] = "$";
        for (int i = 0, j = FIRST_TERMINAL; i < t.length; i++, j++)
            symbols[j] = t[i];

        for (int i = 0, j = FIRST_NON_TERMINAL; i < n.length; i++, j++)
            symbols[j] = n[i];

        this.startSymbol = startSymbol;
    }

    /**
     * @param p produções
     */
    private void setProductions(List<Production> lp)
    {
        productions.addAll(lp);
        int max = 0;
        for (Production p : productions)
        {
        	p.setGrammar(this);
        	for (int j=0; j<p.get_rhs().size(); j++)
        		if (p.get_rhs().get(j) > max)
        			max = p.get_rhs().get(j);
        }
        SEMANTIC_ACTION_COUNT = max - FIRST_SEMANTIC_ACTION();
    }

    /**
     * @return TRUE se x eh um símbolo terminal
     */
    public final boolean isTerminal(int x)
    {
        return x < FIRST_NON_TERMINAL;
    }

    /**
     * @return TRUE se x eh um símbolo não terminal
     */
    public final boolean isNonTerminal(int x)
    {
        return x >= FIRST_NON_TERMINAL && x < FIRST_SEMANTIC_ACTION();
    }
    
    public final boolean isSemanticAction(int x)
    {
        return x >= FIRST_SEMANTIC_ACTION();
    }

	public List<Production> getProductions()
	{
		return productions;
	}
	
	public List<Production> getProductions(IntegerSet bs)	
	{
		List<Production> result = new ArrayList<Production>();
		
		for (Integer i : bs)
			result.add(productions.get(i.intValue()));
		
		return result;
	}

	public String[] getSymbols()
	{
		return symbols;
	}
	
	public String[] getTerminals()
	{
		String[] terminals = new String[FIRST_NON_TERMINAL-2];
		System.arraycopy(symbols,2,terminals,0,terminals.length);
		return terminals;
	}

	public String[] getNonTerminals()
	{
		String[] nonTerminals = new String[FIRST_SEMANTIC_ACTION() - FIRST_NON_TERMINAL];
		System.arraycopy(symbols,FIRST_NON_TERMINAL,nonTerminals,0,nonTerminals.length);
		return nonTerminals;
	}
	
	public int getStartSymbol()
	{
		return startSymbol;
	}

	public Grammar asNormalLR()
	{
		if (normalLR)
			return this;
			
		String[] t = getTerminals();

		int newSymbols = 1+SEMANTIC_ACTION_COUNT+1;

		String[] nt_old = getNonTerminals();
		String[] nt = new String[nt_old.length+newSymbols];
		System.arraycopy(nt_old, 0, nt, 0, nt_old.length);

		List<Production> p = new ArrayList<Production>(getProductions());

		for (int i=0; i<SEMANTIC_ACTION_COUNT+1; i++)
		{
			nt[nt_old.length+i] = "<#"+i+">";
			p.add(new Production(null,FIRST_SEMANTIC_ACTION()+i));			
		}

		nt[nt.length-1] = "<-START->";                   
		p.add(new Production(null,FIRST_SEMANTIC_ACTION()+newSymbols-1, getStartSymbol()));

		Grammar g = new Grammar(t, nt, p, FIRST_SEMANTIC_ACTION()+newSymbols-1);
		
		g.normalLR = true;
		
		return g;
	}

	/**
	 * Cria uma nova produção. Se a produção criada já existe na gramática,
	 * null é retornado.
	 * 
	 * @param lhs lado esquerdo da produção
	 * @param rhs lado direito da produção
	 * 
	 * @return produção gerada, ou null se esta já existir
	 * */
	public Production createProduction(int lhs, int... rhs)
	{
		Production p = new Production(this, lhs, rhs);
		for (int i = 0; i < productions.size(); i++)
			if (productions.get(i).equals( p ))
				return null;
				
		return p;
	}
		
	/**
	 * Cria uma nova produção. Se a produção criada já existe na gramática,
	 * null é retornado.
	 * 
	 * @param lhs lado esquerdo da produção
	 * @param rhs lado direito da produção
	 * 
	 * @return produção gerada, ou null se esta já existir
	 * */
	public Production createProduction(int lhs, IntList rhs)
	{
		Production p = new Production(this, lhs, rhs);
		for (int i = 0; i < productions.size(); i++)
			if (productions.get(i).equals( p ))
				return null;
				
		return p;
	}
	
	protected boolean isEpsilon(IntList x, int start)
	{
		for (int i=start; i<x.size(); i++)
			if (! isSemanticAction(x.get(i)))
				return false;
		return true;
	}
	
	protected boolean isEpsilon(IntList x)
	{		
		return isEpsilon(x, 0);
	}
	
	/**
     * @return BitSet indicando os symbolos que derivam Epsilon
     */
    private IntegerSet markEpsilon()
    {
    	IntegerSet result = new IntegerSet();

        for (int i = 0; i < productions.size(); i++)
        {
            Production P = productions.get(i);
            if (isEpsilon(P.get_rhs()))
                result.add(P.get_lhs());
        }
        for (int i=FIRST_SEMANTIC_ACTION(); i <= LAST_SEMANTIC_ACTION(); i++)
        	result.add(i);
        	
        boolean change = true;
        while (change)
        {
            change = false;
            boolean derivesEpsilon;
            for (int i = 0; i < productions.size(); i++)
            {
                Production P = productions.get(i);
                derivesEpsilon = true;
                for (int j = 0; j < P.get_rhs().size(); j++)
                {
                    derivesEpsilon = derivesEpsilon && result.add(P.get_rhs().get(j));
                }
                if (derivesEpsilon && !result.contains(P.get_lhs()))
                {
                    change = true;
                    result.add(P.get_lhs());
                }
            }
        }
        return result;
    }
	
	private static final IntegerSet EMPTY_SET = new IntegerSet();
	static { EMPTY_SET.add(EPSILON); }
	
	public IntegerSet first(int symbol)
	{
		if (isSemanticAction(symbol))
			return EMPTY_SET;
		else
			return firstSet[symbol];
	}
	
	public IntegerSet first(IntList x)
	{
		return first(x, 0);
	}
	
	public IntegerSet first(IntList x, int start)
	{
		IntegerSet result = new IntegerSet();
		
		if (x.size()-start == 1 && x.get(start) == DOLLAR)
			result.add(DOLLAR);
		if (isEpsilon(x, start))
			result.add(EPSILON);
		else
		{
			int k = x.size();
			while (isSemanticAction(x.get(start)))
				start++;
				
			IntegerSet f = new IntegerSet(first(x.get(start)));
            f.remove(EPSILON);
            result.addAll(f);
            int i=start;
            while (i < k-1 && first(x.get(i)).contains(EPSILON))
            {
                i++;
                f = new IntegerSet(first(x.get(i)));
                f.remove(EPSILON);
                result.addAll(f);
            }
            if (i == k-1 && first(x.get(i)).contains(EPSILON))
                result.add(EPSILON);
		}
		return result;
	}
	
	/**
     * Calcula os conjuntos FIRST de todos os símbolos de Gramática
     */
    private void fillFirstSet()
    {
    	IntegerSet derivesEpsilon = markEpsilon();
        firstSet = new IntegerSet[symbols.length];
        for (int i = 0; i < firstSet.length; i++)
        {
            firstSet[i] = new IntegerSet();
        }

        for (int A = FIRST_NON_TERMINAL; A < FIRST_SEMANTIC_ACTION(); A++)
        {
            if (derivesEpsilon.contains(A))
                firstSet[A].add(EPSILON);
        }
        for (int a = FIRST_TERMINAL; a < FIRST_NON_TERMINAL; a++)
        {
            firstSet[a].add(a);
            for (int A = FIRST_NON_TERMINAL; A < FIRST_SEMANTIC_ACTION(); A++)
            {
                boolean exists = false;
                for (int i = 0; i < productions.size(); i++)
                {
                    Production P = productions.get(i);
                    if (P.get_lhs() == A && !isEpsilon(P.get_rhs()) && P.firstSymbol() == a)
                    {
                        exists = true;
                        break;
                    }
                }
                if (exists)
                    firstSet[A].add(a);
            }
        }
        boolean changed;
        do
        {
            changed = false;
            for (int i = 0; i < productions.size(); i++)
            {
                Production P = productions.get(i);
                IntegerSet old = new IntegerSet(firstSet[P.get_lhs()]);
                firstSet[P.get_lhs()].addAll(first(P.get_rhs()));
                if (!changed && !old.equals(first(P.get_lhs())) )
                    changed = true;
            }
        }
        while (changed);
    }
	
	/**
     * Calcula os conjuntos FOLLOW de todos os símbolos não terminais de Gramática
     */
    private void fillFollowSet()
    {
        followSet = new IntegerSet[symbols.length];
        for (int i = 0; i < followSet.length; i++)
        {
            followSet[i] = new IntegerSet();
        }
        followSet[startSymbol].add(DOLLAR);
        boolean changes;
        do
        {
            changes = false;
            for (int i = 0; i < productions.size(); i++)
            {
                Production P = productions.get(i);
                for (int j=0;j<P.get_rhs().size(); j++)
                {
                    if (isNonTerminal(P.get_rhs().get(j)))
                    {
                    	IntegerSet s = first(P.get_rhs(), j+1);
                        boolean deriveEpsilon = s.contains(EPSILON);

                        if( P.get_rhs().size() > j+1 )
                        {
                            s.remove(EPSILON);
                            IntegerSet old = new IntegerSet(followSet[P.get_rhs().get(j)]);
                            followSet[P.get_rhs().get(j)].addAll(s);
                            if (!changes && !followSet[P.get_rhs().get(j)].equals(old))
                                changes = true;
                        }

                        if (deriveEpsilon)
                        {
                        	IntegerSet old = new IntegerSet(followSet[P.get_rhs().get(j)]);
                            followSet[P.get_rhs().get(j)].addAll(followSet[P.get_lhs()]);
                            if (!changes && !followSet[P.get_rhs().get(j)].equals(old))
                                changes = true;
                        }
                    }
                }
            }
        }
        while (changes);
    }
    
    /**
     * Gera uma representação String dos conjuntos First e Follow
     * @return First e Follow como uma String
     */
    public String stringFirstFollow()
    {
        StringBuffer result = new StringBuffer();
        for (int i = FIRST_NON_TERMINAL; i < firstSet.length; i++)
        {
            StringBuffer bfr = new StringBuffer();
            bfr.append("FIRST(").append(symbols[i]).append(") = { ");
            for (int j = 0; j < firstSet[i].size(); j++)
            {
                if (firstSet[i].contains(j))
                    bfr.append("").append(symbols[j]).append(" ");
            }
            bfr.append("}");
            result.append(bfr).append('\n');
        }
        for (int i = FIRST_NON_TERMINAL; i < followSet.length; i++)
        {
            StringBuffer bfr = new StringBuffer();
            bfr.append("FOLLOW(").append(symbols[i]).append(") = { ");
            for (int j = 0; j < followSet[i].size(); j++)
            {
                if (followSet[i].contains(j))
                    bfr.append(symbols[j]).append(" ");
            }
            bfr.append("}");
            result.append(bfr).append('\n');
        }
        return result.toString();
    }
    
    public String ffAsHTML()
    {
    	StringBuffer result = new StringBuffer();

		result.append(
			"<HTML>"+
			"<HEAD>"+
			"<TITLE>First &amp; Follow</TITLE>"+
			"</HEAD>"+
			"<BODY><FONT face=\"Verdana, Arial, Helvetica, sans-serif\">"+
			"<TABLE border=1 cellspacing=0>");
			
		result.append(
			"<TR align=center>"+
			"<TD bgcolor=black><FONT color=white><B>SÍMBOLO</B></FONT></TD>"+
			"<TD bgcolor=black><FONT color=white><B>FIRST</B></FONT></TD>"+
			"<TD bgcolor=black><FONT color=white><B>FOLLOW</B></FONT></TD>"+
			"</TR>");
			
		for (int i = FIRST_NON_TERMINAL; i < FIRST_SEMANTIC_ACTION(); i++)
        {
        	result.append("<TR align=center>");
			
			result.append("<TD nowrap bgcolor=#F5F5F5><B>"+HTMLDialog.translateString(symbols[i])+"</B></TD>");
			
			StringBuffer bfr = new StringBuffer("  ");
            for (int j = 0; j < firstSet[i].size(); j++)
            {
                if (firstSet[i].contains(j))
                    bfr.append(symbols[j]).append(", ");
            }
            bfr.setLength(bfr.length()-2);
            
            result.append("<TD nowrap bgcolor=#F5F5F5>"+HTMLDialog.translateString(bfr.toString())+"</TD>");
			
            bfr = new StringBuffer("  ");
            for (int j = 0; j < followSet[i].size(); j++)
            {
                if (followSet[i].contains(j))
                    bfr.append(symbols[j]).append(", ");
            }
            bfr.setLength(bfr.length()-2);
            
            result.append("<TD nowrap bgcolor=#F5F5F5>"+HTMLDialog.translateString(bfr.toString())+"</TD>");
            
            result.append("</TR>");
        }
			
		result.append(
			"</TABLE>"+
			"</FONT></BODY>"+
			"</HTML>");
			
		return result.toString();
    }

    /**
     * Remove os estados improdutivos da gramática
     * @throws EmptyGrammarException se o símbolo inicial for removido
     */
    protected void removeImproductiveSymbols() throws EmptyGrammarException
    {
    	IntegerSet SP = getProductiveSymbols();

        updateSymbols(SP);
    }

    /**
     * Remove os estados inúteis, os inprodutívos e os inalcansáveis
     * @throws EmptyGrammarException se o símbolo inicial for removido
     */
    public void removeUselessSymbols() throws EmptyGrammarException
    {
        removeImproductiveSymbols();
        removeUnreachableSymbols();
        //removeRepeatedProductions();
    }

    /**
     * Calcula as produções cujo lado esquerdo é <code>symbol</code>
     * @return BitSet indicando essas produções
     */
    public IntegerSet productionsFor(int symbol)
    {       
    	IntegerSet result = new IntegerSet();
        for (int i = 0; i < productions.size(); i++)
        {
            if (productions.get(i).get_lhs() == symbol)
                result.add(i);
        }
        return result;
    }

    /**
     * Transforma as recursões à esquerda indiretas em recusões diretas
     * @param prods produções para serem processadas
     * @return lista de produçoes sem recursão indireta
     */
    private List<Production> transformToFindRecursion(List<Production> prods)
    {
    	List<Production> prodList = new ArrayList<Production>(prods);
    	
        for (int i=FIRST_NON_TERMINAL; i<FIRST_SEMANTIC_ACTION(); i++ )
        {
            for (int j=FIRST_NON_TERMINAL; j<i; j++)
            {
                for (int it = 0; it < prodList.size(); it++)
                {
                    Production P = prodList.get(it);
                    if (P.get_lhs() == i && P.firstSymbol() == j)
                    {
                        prodList.remove(it);
                        it--;
                        IntList actions = new IntList();
                        for (int k = 0; k < P.get_rhs().size() && isSemanticAction(P.get_rhs().get(k)); k++)
							actions.add(P.get_rhs().get(k));
							
                        for (int it2 = 0; it2 < prodList.size(); it2++)
                        {
                            Production P2 = prodList.get(it2);
                            if (P2.get_lhs() == j)
                            {
                                int[] rhs = new int[P2.get_rhs().size() + P.get_rhs().size()-1];
                                int k = 0;
                                for ( ; k<actions.size(); k++)
                                	rhs[k] = actions.get(k);
                                int m = k;
                                for ( k = 0 ; k<P2.get_rhs().size(); k++)
                                    rhs[k + m] = P2.get_rhs().get(k);
                                m = m + k - (actions.size() + 1);
                                for ( k = actions.size() + 1; k<P.get_rhs().size(); k++)
                                    rhs[k + m] = P.get_rhs().get(k);
                                
                                Production newProduction = createProduction(P.get_lhs(), rhs);
                                if (newProduction != null)
                                    prodList.add(newProduction);
                            }
                        }
                    }
                }
            }
        }
        return prodList;
    }

    /**
     * Reordena os símbolos e as produções
     */
    public void sort()
    {    	
    	for (int i=FIRST_NON_TERMINAL; i < FIRST_SEMANTIC_ACTION(); i++)
    	{
    		String s = symbols[i].substring(0, symbols[i].length()-1) + "_T>";
    		int j=i+1;
    		for ( ; j < FIRST_SEMANTIC_ACTION(); j++)
    			if (symbols[j].equals( s ))
    				break;
    		if (j < FIRST_SEMANTIC_ACTION()) //achou
    		{
    			int to = i+1, 
    			    from = j;
    			    
    			if (to != from)
    			{
    				moveSymbol(from, to);
    			}
    		}
    	}
    	moveSymbol(startSymbol, FIRST_NON_TERMINAL);
    	
    	Collections.sort(productions);
    }
    
	private void moveSymbol(int from, int to)
	{
		String s = symbols[from];
		for (int k=from; k > to; k--)
			symbols[k] = symbols[k-1];
		symbols[to] = s;
		
		if (startSymbol == from)
			startSymbol = to;
		else if (startSymbol >= to && startSymbol < from)
			startSymbol++;
		
		for (Production p : productions)
		{
			if (p.get_lhs() == from)
				p.set_lhs(to);
			else if (p.get_lhs() >= to && p.get_lhs() < from)
				p.set_lhs(p.get_lhs() + 1);
			IntList rhs = p.get_rhs();
			for (int k=0; k < rhs.size(); k++)
			{
				if (rhs.get(k) == from)
					rhs.set(k, to);
				else if (rhs.get(k) >= to && rhs.get(k) < from)
					rhs.set(k, rhs.get(k) + 1);
			}
		}
	}


    /**
     * Verifica as condições para esta gramática ser LL
     */
    public boolean isLL()
    {
        return 
        	isFactored() && 
        	!hasLeftRecursion() &&         	
        	passThirdCondition();
    }

    /**
     * Verifica se esta gramática possui recursão à esquerda
     */
    public boolean hasLeftRecursion()
    {
    	List<Production> prods = transformToFindRecursion(productions);
        
		for (int i = 0; i < prods.size(); i++)
        {
            if (prods.get(i).get_lhs() == prods.get(i).firstSymbol())
            {
            	return true;
            }
            
        }
        return false;
    }
    
    public int getLeftRecursiveSimbol()
    {
    	List<Production> prods = transformToFindRecursion(productions);
    
        for (int i = 0; i < prods.size(); i++)
        {
            if (prods.get(i).get_lhs() == prods.get(i).firstSymbol())
            {
                return prods.get(i).get_lhs();
            }
        
        }
        return -1;
    }

    /**
     * 
     * @return um BitSet contendo produçoes não fatoradas
     */
    public IntegerSet getNonFactoratedProductions()
    {
    	IntegerSet result = new IntegerSet();
        
        for (int i=0; i< productions.size(); i++)
        {
            Production p1 = productions.get(i);
            for (int j=i+1; j< productions.size(); j++)
            {
                Production p2 = productions.get(j);

                if (p1.get_lhs() == p2.get_lhs())
                {
                	IntegerSet first = first(p1.get_rhs());
                    first.retainAll(first(p2.get_rhs()));
                    if (! first.isEmpty())
                    {
                        result.add(i);
                        result.add(j);
                    }
                }
            }
            if (result.size() > 0)
                break;
        }
        
        return result;
    }

    /**
     * Verifica se esta gramática está fatorada
     */
    public boolean isFactored()
    {
        for (int i=0; i< productions.size(); i++)
        {
            Production P1 = productions.get(i);
            for (int j=i+1; j< productions.size(); j++)
            {
                Production P2 = productions.get(j);

                if (P1.get_lhs() == P2.get_lhs())
                {
                	IntegerSet first = first(P1.get_rhs());
                    first.retainAll(first(P2.get_rhs()));
                    if (! first.isEmpty())
                        return false;
                }
            }
        }
        return true;
    }

    /**
     * Verifica a terceira condição LL
     */
    public boolean passThirdCondition()
    {
    	IntegerSet derivesEpsilon = markEpsilon();
        for (int i=FIRST_NON_TERMINAL; i<FIRST_SEMANTIC_ACTION(); i++)
        {
            if (derivesEpsilon.contains(i))
            {
            	IntegerSet first = new IntegerSet(firstSet[i]);
                first.retainAll(followSet[i]);
                if (! first.isEmpty())
                    return false;
            }
        }
        return true;
    }

    /**
     * Calcula os estados produtivos
     * @return conjunto dos estados produtivos
     */
    private IntegerSet getProductiveSymbols()
    {
    	IntegerSet SP = new IntegerSet();
        for (int i=FIRST_TERMINAL; i< FIRST_NON_TERMINAL; i++)
            SP.add(i);

        for (int i=FIRST_SEMANTIC_ACTION(); i<= LAST_SEMANTIC_ACTION(); i++)
            SP.add(i);
            
        SP.add(EPSILON);
        boolean change;

        do
        {
            change = false;
            IntegerSet Q = new IntegerSet();
            for (int i=FIRST_NON_TERMINAL; i<FIRST_SEMANTIC_ACTION(); i++)
            {
                if (! SP.contains(i))
                {
                    for (int j=0; j< productions.size(); j++)
                    {
                        Production P = productions.get(j);
                        if (P.get_lhs() == i)
                        {
                            boolean pass = true;
                            for (int k=0; k<P.get_rhs().size(); k++)
                                pass = pass && SP.contains(P.get_rhs().get(k));
                            if (pass)
                            {
                                Q.add(i);
                                change = true;
                            }
                        }
                    }
                }
            }
            SP.addAll(Q);
        }
        while (change);
        return SP;
    }

    /**
     * Remove os símbolos inalcançáveis da gramática
     * @throws EmptyGrammarException se o símbolo inicial for removido
     */
    protected void removeUnreachableSymbols() throws EmptyGrammarException
    {
    	IntegerSet SA = getReachableSymbols();

        updateSymbols(SA);
    }

    /**
     * Calcula os símbolos que são alcansáveis
     *
     * @return BitSet indicando os symbolos alcansáveis
     */
    private IntegerSet getReachableSymbols()
    {
    	IntegerSet SA = new IntegerSet();
        SA.add(startSymbol);
        boolean change;
        do
        {
            change = false;
            IntegerSet M = new IntegerSet();
            for (int i=0; i<symbols.length; i++)
            {
                if (! SA.contains(i))
                {
                    for (int j=0; j< productions.size(); j++)
                    {
                        Production P = productions.get(j);
                        if (SA.contains(P.get_lhs()))
                        {
                            for (int k=0; k<P.get_rhs().size(); k++)
                            {
                                if (P.get_rhs().get(k) == i)
                                {
                                    M.add(i);
                                    change = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            SA.addAll(M);
        }
        while (change);
        return SA;
    }

	public String uselessSymbolsHTML()
	{
		Grammar clone = (Grammar) clone();
		
		try
		{
			clone.removeUselessSymbols();
		}
		catch (EmptyGrammarException e)
		{
		}
		
		String[] cs = clone.symbols;
		
		IntegerSet s = new IntegerSet();
		
		
		for (int i=2; i<symbols.length; i++)
		{
			for (int j=0; j<cs.length; j++)
			{
				if (cs[j].equals(symbols[i]))
				{
					s.add(i);
					break;
				}
			}
		}
		
		StringBuffer result = new StringBuffer();
		
		result.append(
					"<HTML>"+
					"<HEAD>"+
					"<TITLE>Símbolos inúteis</TITLE>"+
					"</HEAD>"+
					"<BODY><FONT face=\"Verdana, Arial, Helvetica, sans-serif\">");
		
		int count = 0;
		for (int i=2; i<symbols.length; i++)
		{
			if (!s.contains(i))
			{
				result.append(HTMLDialog.translateString(symbols[i])+"<br>");
				count++;
			}
		}
		if (count == 0)
			result.append("Não há símbolos inúteis");
		
		result.append(
					"</TABLE>"+
					"</FONT></BODY>"+
					"</HTML>");
		
		return result.toString();
	}

    /**
     * Gera uma representação de um BitSet utilizando os símbolos da Gramática
     *
     * @param b BitSet a ser convertido
     *
     * @return representação do BitSet
     */
    public String setToStr(IntegerSet b)
    {
    	StringBuffer bfr = new StringBuffer("{ ");
        for (int j = 0; j < b.size(); j++)
        {
            if (b.contains(j))
                bfr.append("\"").append(symbols[j]).append("\" ");
        }
        bfr.append("}");
        return bfr.toString();
    }

    /**
     * Executa uma derivação mais a esquerda na produção passada como parametro
     *
     * @param p produção a sofrer a derivação
     */
    public List<Production> leftMostDerive(Production p)
    {
    	if (isTerminal(p.firstSymbol()))
            return new ArrayList<Production>();
        else
        {
        	List<Production> newProds = new ArrayList<Production>();
            int symb = p.firstSymbol();
            IntList actions = new IntList();
            for (int i=0; i<p.get_rhs().size() && isSemanticAction(p.get_rhs().get(i)); i++)
            	actions.add(p.get_rhs().get(i));

            for (Production p1 : getProductions(productionsFor(symb)) )
            {
                IntList rhs = new IntList();
                for (int i=0; i<actions.size(); i++)
                	rhs.add(actions.get(i));
                for (int i=0; i<p1.get_rhs().size(); i++)
                	rhs.add(p1.get_rhs().get(i));
                for (int i=actions.size()+1; i<p.get_rhs().size(); i++)
                	rhs.add(p.get_rhs().get(i));
                
                Production n = createProduction(p.get_lhs(), rhs);
                if (n != null && !newProds.contains(n))
 	               newProds.add(n);
            }
            return newProds;
        }
    }

	/**
	 * Calcula o prefixo comum de um conjunto de produções.
	 *
	 * @param prods conjunto de produções com prefixo comum.
	 * 
	 * @return prefixo comum entre as produções. 
	 * 
	 */
	
    public String toString()
    {
        StringBuffer bfr = new StringBuffer();
        String lhs = "";
        boolean first = true;
        for (int i = 0; i < productions.size(); i++)
        {
            Production P = productions.get(i);
            if (! symbols[P.get_lhs()].equals(lhs))
            {
            	if (! first)
            	{            	
            		bfr.append(";\n\n");
            	}
            	first = false;
            	lhs = symbols[P.get_lhs()];
            	bfr.append(lhs).append(" ::=");
            }
            else
            {
            	bfr.append("\n");
            	for (int j=0; j<lhs.length(); j++)
            		bfr.append(" ");
				bfr.append("   |");            	
            }	
            if (P.get_rhs().size() == 0)
            {
				bfr.append(" "+EPSILON_STR);
            }
            else
            {
	            for (int j = 0; j < P.get_rhs().size(); j++)
	            {
	                bfr.append(" ");
	                if (isSemanticAction(P.get_rhs().get(j)))
	                {
	                	int action = P.get_rhs().get(j) - FIRST_SEMANTIC_ACTION();
	                	bfr.append("#"+action);
	                }	
	                else
	                {
	                	String s = symbols[P.get_rhs().get(j)];
	                	bfr.append(s);
	                }
	            }
            }
        }
        bfr.append(";\n");
        return bfr.toString();
    }

    /**
     * Cria uma cópia da Gramática
     */
    public Object clone()
    {
    	try
		{
			Grammar g = (Grammar) super.clone();
			
			String[] T = new String[FIRST_NON_TERMINAL-2];
			String[] N = new String[FIRST_SEMANTIC_ACTION() - FIRST_NON_TERMINAL];
			for (int i = 0; i < T.length; i++)
			     T[i] = new String(symbols[i+2]);
			for (int i = 0; i < N.length; i++)
			     N[i] = new String(symbols[i+FIRST_NON_TERMINAL]);
			List<Production> P = new ArrayList<Production>();
			for (int i = 0; i < productions.size(); i++)
			{
			    int[] rhs = new int[productions.get(i).get_rhs().size()];
			    for (int j=0; j<rhs.length; j++)
			        rhs[j] = productions.get(i).get_rhs().get(j);
			    P.add( new Production(null, productions.get(i).get_lhs(), rhs));
			}
			
			g.setSymbols(T, N, startSymbol);
			g.setProductions(P);
			g.fillFirstSet();
			g.fillFollowSet();  
					
			return g;
		}
		catch (CloneNotSupportedException e)
		{
			throw new InternalError();
		}
    }
    
    private void removeSymbol(int s)
    {
    	String[] newSymbols = new String[symbols.length-1];
    	System.arraycopy(symbols, 0, newSymbols, 0, s);
    	System.arraycopy(symbols, s+1, newSymbols, s, symbols.length - s - 1);
    	symbols = newSymbols;
    	
    	if (startSymbol > s)
    		startSymbol--;
    	if (FIRST_NON_TERMINAL > s)
    		FIRST_NON_TERMINAL--;
    	for (Iterator<Production> i = productions.iterator(); i.hasNext();)
		{
			Production p = i.next();
			
			if (p.get_lhs() == s)
			{
				i.remove();
				continue;
			}
			else if (p.get_lhs() > s)
				p.set_lhs(p.get_lhs()-1);
				
			for (int j=0; j<p.get_rhs().size(); j++)
			{
				if (p.get_rhs().get(j) == s)
				{
					i.remove();
					break;
				}
				if (p.get_rhs().get(j) > s)
					p.get_rhs().set(j, p.get_rhs().get(j) - 1);
			}			
		}
    }
    
    /**
     * Remove todos os symbolos, exceto os que devem ser mantidos;
     * @paramam keep conjunto dos símbolos a serem mantidos
     * @throws EmptyGrammarException se o símbolo inicial for removido
     */
    private void updateSymbols(IntegerSet keep) throws EmptyGrammarException
    {
        keep.add(EPSILON);
        keep.add(DOLLAR);

		/*
        if (checkEmpty && ! keep.get(startSymbol))
            throw new EmptyGrammarException();
        */
        int removed = 0;
        for (int i=0; i<symbols.length; i++)
        	if (! keep.contains(i) )
        	{
        		removeSymbol(i - removed);
        		removed++;
        	}
        
        fillFirstSet();
        fillFollowSet();
    }
}