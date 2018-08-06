package gesser.gals.generator.parser.lr;

import gesser.gals.generator.parser.Grammar;
import gesser.gals.generator.parser.Production;


public class LRItem implements Comparable
{
	private Production production;
	private int position;
	private int lookahead;
	private Grammar g;
	
	public LRItem(Production production, int position, int lookahead)
	{
		this.production = production;
		this.position = position;
		this.lookahead = lookahead;
		this.g = production.getGrammar();
	}
	
	public LRItem(Production production, int position)
	{
		this(production, position, 0);
	}
	
	public int getPosition()
	{
		return position;
	}
	
	public int getLookahead()
	{
		return lookahead;
	}

	public Production getProduction()
	{
		return production;
	}
	
	public boolean equals(Object obj)
	{
		try
		{
			LRItem it = (LRItem) obj;
			return it.production == production && it.position == position && lookahead == it.lookahead;
		}
		catch (ClassCastException e)
		{
			return false;
		}
	}

	public String toString()
	{
		StringBuffer bfr = new StringBuffer();
		bfr.append(g.getSymbols()[production.get_lhs()]).append(" ::= ");
		
		for (int i=0; i<production.get_rhs().size() && i<position; i++)
		{
			int s = production.get_rhs().get(i);
			if (g.isSemanticAction(s))
				bfr.append("#").append(s-g.FIRST_SEMANTIC_ACTION()).append(" ");
			else
				bfr.append(g.getSymbols()[s]).append(" ");
		}
		
		bfr.append("o ");
		
		for (int i=position; i<production.get_rhs().size(); i++)
		{
			int s = production.get_rhs().get(i);
			if (g.isSemanticAction(s))
				bfr.append("#").append(s-g.FIRST_SEMANTIC_ACTION()).append(" ");
			else
				bfr.append(g.getSymbols()[s]).append(" ");
		}
		
		if (lookahead != 0)
		{
			bfr.append(", ");
			bfr.append(g.getSymbols()[lookahead]);
		}
		
		return bfr.toString();
	}
	
	protected Object clone() throws CloneNotSupportedException
	{
		return new LRItem(production, position, lookahead);
	}

	public int compareTo(Object o)
	{
		LRItem it = (LRItem) o;
		
		int cmp = production.compareTo(it.production);
		if (cmp != 0)
			return cmp;
		else
		{
			cmp = position - it.position;
			if (cmp != 0)
				return cmp;
			else
				return lookahead - it.lookahead;
		}
	}

}
