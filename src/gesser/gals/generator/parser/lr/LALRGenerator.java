package gesser.gals.generator.parser.lr;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import gesser.gals.generator.parser.Grammar;

public class LALRGenerator extends LRCanonicGenerator
{
	private boolean compress;
	
	public LALRGenerator(Grammar g)
	{
		super(g);
	}
	
	private Set<LRItem> core(List<LRItem> state)
	{
		Set<LRItem> result = new TreeSet<LRItem>();
		
		for (int i=0; i<state.size(); i++)
		{
			LRItem item = state.get(i);
			LRItem x = new LRItem(item.getProduction(), item.getPosition());
			
			if (! result.contains(x))
				result.add(x);
		}
		return result;
	}
	
	protected List<List<LRItem>> computeItems()
	{
		List<List<LRItem>> items = super.computeItems();
		
		for (int i=0; i<items.size(); i++)
		{
			List<LRItem> state = items.get(i);
			Set<LRItem> core = core(state);
			
			for (int j=i+1; j<items.size(); j++)
			{
				List<LRItem> state2 = items.get(j);
				Set<LRItem> core2 = core(state2);
				
				if (core.equals(core2))
				{
					for (int k=0; k<state2.size(); k++)
					{
						LRItem item = state2.get(k);
						if (!state.contains(item))
							state.add(item);
					}
					items.remove(j);
					j--;
				}
			}
		}
		
		this.compress = true;
		return items;
	}
	
	protected List<LRItem> goTo(List<LRItem> items, int s)
	{
		List<LRItem> x = super.goTo(items, s);
		
		if (compress)
		{
			Set<LRItem> core = core(x);
			
			for (int i=0; i<itemList.size(); i++)
			{
				List<LRItem> state = itemList.get(i);
				if (core.equals(core(state)))
					return state;
			}
			//se n achar... n deve acontecer
		}
		return x;
	}
}
