package gesser.gals.generator.parser.lr;

import gesser.gals.generator.parser.Grammar;
import gesser.gals.generator.parser.Production;
import gesser.gals.util.IntList;
import gesser.gals.util.IntegerSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SLRGenerator extends LRGenerator
{   
	public SLRGenerator(Grammar g)
	{	
		super(g);
	}
	
	protected List<LRItem> closure(List<LRItem> items)
    {
    	List<LRItem> result = new ArrayList<LRItem>(items);
    	
    	for (int i=0; i<result.size(); i++)
    	{
    		LRItem it = result.get(i);
    		
    		Production p = it.getProduction();
    		if (it.getPosition() < p.get_rhs().size())
    		{
	    		int s = p.get_rhs().get(it.getPosition());
	    		if (g.isNonTerminal(s))
	    		{
	    			IntegerSet bs = g.productionsFor(s);
	    			for (Production p2 : g.getProductions(bs))
	    			{
		    			LRItem n = new LRItem(p2, 0);
		    			if ( ! result.contains(n) )
		    				result.add(n);
	    			}
	    		}
    		}
    	}
    	
    	return result;
    }
    
	protected List<LRItem> goTo(List<LRItem> items, int s)
    {
    	List<LRItem> result = new ArrayList<LRItem>();
    	
    	for (LRItem item : items)
		{
			Production p = item.getProduction();
			
			if (item.getPosition() < p.get_rhs().size())
			{
				int symb = p.get_rhs().get(item.getPosition());
				
				if (symb == s)
				{
					result.add(new LRItem(item.getProduction(), item.getPosition()+1));
				}
			}
		}
		
		return closure(result);
    }
    
    /**
     * Calcula os itens LR
     * @return List
     */
    
	protected List<List<LRItem>> computeItems()
    {
    	
    	List<LRItem> s = new ArrayList<LRItem>();
    	IntegerSet sp = g.productionsFor(g.getStartSymbol());
    	int f = sp.first();
    	s.add(new LRItem(g.getProductions().get(f), 0));
    	List<List<LRItem>> c = new ArrayList<List<LRItem>>();
    	c.add(closure(s));
    	
    	boolean repeat = true;
    	    	    	
    	while (repeat)
    	{
    		start:
			{
	    		repeat = false;
	    			    		
	    		for (List<LRItem> items : c )
	    		{	    			
	    			for (int i=0; i<items.size(); i++)
	    			{
	    				LRItem m = items.get(i);
	    				
	    				Production p = m.getProduction();
	    				if (p.get_rhs().size() > m.getPosition())
	    				{
	    					List<LRItem> gt = goTo(items, p.get_rhs().get(m.getPosition()));
		    				if (gt.size() != 0 && ! c.contains(gt))
		    				{
		    					c.add(gt);
		    					repeat = true;
		    					break start;
		    				}
	    				}
	    			}
	    		}
			}
    	}
    	return c;
    }
    
    /**
     * Cria a tabale de parse SLR
     * 
     * */
    public Command[][] buildTable()
    {
    	Set<Command>[][] result = new Set[itemList.size()][g.getSymbols().length-1];
		
    	for (int i=0; i<result.length; i++)
    	{
    		for (int j=0; j<result[i].length; j++)
    		{
    			result[i][j] = new HashSet<Command>();
    		}
    	}
    	
    	for (int i=0; i<result.length; i++)
    	{
    		List<LRItem> items = itemList.get(i);
    		
    		for (int j=0; j<items.size(); j++)
    		{
    			LRItem item = items.get(j);
    			
    			Production p = item.getProduction();
    			IntList rhs = p.get_rhs();
    			
    			if (rhs.size() > item.getPosition())
    			{
    				int s = rhs.get(item.getPosition());
    				List<LRItem> next = goTo(items, s);
    				
    				if (g.isTerminal(s))        
    				{
    					result[i][s-1].add(Command.createShift(itemList.indexOf(next)));
    				}
    				else //nonTerminal
    				{
    					result[i][s-1].add(Command.createGoTo(itemList.indexOf(next)));
    				}
    			}
    			else
    			{
    				int lhs = p.get_lhs();
    				
    				if (lhs == g.getStartSymbol())
    				{
    					result[i][0].add(Command.createAccept());
    				}
    				else
    				{
    					IntegerSet follow = g.followSet[lhs];
	    				for (Integer a : follow)
	    				{
	    					Command cmd;
	    					if (lhs < semanticStart)
	    						cmd = Command.createReduce(g.getProductions().indexOf(p));
	    					else
	    						cmd = Command.createAction(lhs-semanticStart);
	    						
	    					result[i][a.intValue()-1].add(cmd);
	    				}
    				}
    			}
    		}
    	}
    	
    	return resolveConflicts(result);
    }
}
