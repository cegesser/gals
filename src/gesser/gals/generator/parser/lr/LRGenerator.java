package gesser.gals.generator.parser.lr;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gesser.gals.HTMLDialog;
import gesser.gals.generator.parser.Grammar;
import gesser.gals.generator.parser.ConflictSolver;
import gesser.gals.generator.parser.Production;
import gesser.gals.util.IntegerSet;

/**
 * @author Gesser
 */
public abstract class LRGenerator
{
	protected Grammar g;
	protected List<List<LRItem>> itemList;
	protected int semanticStart;
	protected int firstSementicAction;

	public LRGenerator(Grammar g)
	{	
		semanticStart = g.FIRST_SEMANTIC_ACTION();
		firstSementicAction = g.FIRST_SEMANTIC_ACTION();// g.SEMANTIC_ACTION_COUNT;
	
		this.g = g.asNormalLR();
	
		itemList = computeItems();
	}

	

	public List<String> getErrors(Command[][] table)
	{
		List<String> result = new ArrayList<String>();
		
		for (int state=0; state<table.length; state++)
		{
			IntegerSet bs = new IntegerSet();
			for (int j = 1; j<g.FIRST_NON_TERMINAL; j++)
			{
				if (table[state][j-1].getType() != Command.Type.ERROR)
					bs.add(j);
			}
			StringBuffer bfr = new StringBuffer();
			int total = bs.size(), count = 0;
			for (Integer i : bs)
			{
				if (i.intValue() == 1)//DOLAR
					bfr.append("fim de sentença");
				else
					bfr.append(g.getSymbols()[i.intValue()]);
		
				if (total - count == 2)
					bfr.append(" ou ");
				else if (total - count > 2)
				bfr.append(", ");
				
				++count;
			}
			result.add(bfr.toString());
		}
	
		return result;
	}
		
	public Grammar getGrammar()
	{
		return g;
	}

	public int getFirstSemanticAction()
	{
		return firstSementicAction;
	}
	
	protected abstract List<LRItem> closure(List<LRItem> items);
	protected abstract List<LRItem> goTo(List<LRItem> items, int s);
	protected abstract List<List<LRItem>> computeItems();
	public abstract Command[][] buildTable();
	
	public int[][][] buildIntTable()
	{
		Command[][] commands = buildTable();
	
		int[][][] result = new int[commands.length][commands[0].length][2];
	
		for (int i=0; i<result.length; i++)
			for (int j=0; j<result[i].length; j++)
			{
				result[i][j][0] = commands[i][j].getType().ordinal();
				result[i][j][1] = commands[i][j].getParameter();
			}
	 
		return result;
	}

	protected Command[][] resolveConflicts(Set<Command>[][] table)
	{
		Command[][] result = new Command[table.length][table[0].length];
	
		Command error = Command.createError();
		for (int i=0; i<result.length; i++)
		{
			for (int j=0; j<table[0].length; j++)
			{
				switch (table[i][j].size())
				{
					case 0:
						result[i][j] = error;
						break;
					case 1:
						result[i][j] = table[i][j].iterator().next();
						break;
					default:
						result[i][j] = solve(table[i][j], i, j);
						break;
				}
			}
		}
	
		return result;
	}

	private Command solve(Set<Command> set, int state, int input)
	{
		Command[] cmds = new Command[set.size()];
		int i=0;
		for (Command c : set)
		{
			cmds[i] = c;
			i++;
		}
	
		boolean equals = true;
		for (int j = 1; j < cmds.length; j++)
		{
			equals = equals && cmds[j-1].equals(cmds[j]);
			if (!equals)
				break;
		}
	
		if (equals)
			return cmds[0];
		else
		{
			return cmds[ConflictSolver.getInstance().resolve(g, cmds, state, input)];
		}
	}



	public String tableAsHTML()
	{
		StringBuffer result = new StringBuffer();
	
		result.append(
			"<HTML>"+
			"<HEAD>"+
			"<TITLE>Tabela SLR(1)</TITLE>"+
			"</HEAD>"+
			"<BODY><FONT face=\"Verdana, Arial, Helvetica, sans-serif\">"+
			"<TABLE border=1 cellspacing=0>");

		Command[][] table = buildTable();
	
		result.append("<TR>");
		result.append("<TD  align=center rowspan=2 bgcolor=black nowrap><FONT color=white><B>ESTADO</B></FONT></TD>");
		result.append("<TD  align=center colspan="+(g.FIRST_NON_TERMINAL-1)+" bgcolor=black nowrap><FONT color=white><B>AÇÃO</B></FONT></TD>");
		result.append("<TD  align=center colspan="+(g.FIRST_SEMANTIC_ACTION()-g.FIRST_NON_TERMINAL)+" bgcolor=black nowrap><FONT color=white><B>DESVIO</B></FONT></TD>");
		result.append("</TR>");
	
		result.append("<TR>");
		//result.append("<TD  align=center bgcolor=black>&nbsp;</TD>");
		for (int i=0; i<table[0].length-1; i++)
		{					
			result.append("<TD  align=center bgcolor=black nowrap><FONT color=white><B>"+HTMLDialog.translateString(g.getSymbols()[i+1])+"</B></FONT></TD>");
		}
		result.append("</TR>");
	
		for (int i=0; i<table.length; i++)
		{
			Command[] line = table[i];
		
			result.append("<TR>");
		
			result.append("<TD bgcolor=black align=right nowrap><FONT color=white><B>"+i+"</B></FONT></TD>");
		
			for (int j=0; j<line.length-1; j++)
			{	
				Command cmd = line[j];
				String value = "";
			
				if (cmd!= null)
					value = cmd.toString();
			
				String color = j+1<g.FIRST_NON_TERMINAL?"#F5F5F5":"#E6E6E6";
				
				result.append("<TD bgcolor="+color+" align=center nowrap>"+value+"</TD>");
			}	
			result.append("</TR>");		
		}
		
		result.append(
			"</TABLE>"+
			"</FONT></BODY>"+
			"</HTML>");
		
		return result.toString();
	}

	public String itemsAsHTML()
	{
		StringBuffer result = new StringBuffer();
	
		result.append(
			"<HTML>"+
			"<HEAD>"+
			"<TITLE>Itens SLR(1)</TITLE>"+
			"</HEAD>"+
			"<BODY><FONT face=\"Verdana, Arial, Helvetica, sans-serif\">"+
			"<TABLE border=1 cellspacing=0>");

		List<List<LRItem>> l = itemList;
	
		result.append("<TR>");
		result.append("<TD  align=center bgcolor=black><FONT color=white><B>Estado</B></FONT></TD>");
		result.append("<TD  align=center bgcolor=black><FONT color=white><B>Itens</B></FONT></TD>");
		result.append("<TD  align=center bgcolor=black><FONT color=white><B>Desvio</B></FONT></TD>");
		result.append("</TR>");
	
		for (int i=0; i<l.size(); i++)
		{
			String color = i%2==0?"#F5F5F5":"#E6E6E6";
		
			List<LRItem> item = l.get(i);
		
			result.append("<TR>");
			result.append("<TD bgcolor="+color+" align=right rowspan="+item.size()+">"+i+"</TD>");
			result.append("<TD bgcolor="+color+" nowrap>"+HTMLDialog.translateString(item.get(0).toString())+"</TD>");
		
			LRItem it = item.get(0);
			Production p = it.getProduction();
			if (p.get_rhs().size() > it.getPosition())
			{			
				int x = p.get_rhs().get(it.getPosition());
				List<LRItem> next = goTo(item, x);
				int pos = l.indexOf(next);
				result.append("<TD bgcolor="+color+" align=right>"+pos+"</TD>");
			}
			else
				result.append("<TD bgcolor="+color+" align=right>"+"&nbsp"+"</TD>");
			result.append("</TR>");
		
			for (int j=1; j<item.size(); j++)
			{
				result.append("<TR>");
				result.append("<TD bgcolor="+color+" nowrap>"+HTMLDialog.translateString(item.get(j).toString())+"</TD>");
			
				it = item.get(j);
				p = it.getProduction();
				if (p.get_rhs().size() > it.getPosition())
				{			
					int x = p.get_rhs().get(it.getPosition());
					List<LRItem> next = goTo(item, x);
					int pos = l.indexOf(next);
					result.append("<TD bgcolor="+color+" align=right>"+pos+"</TD>");
				}
				else
					result.append("<TD bgcolor="+color+" align=right>"+"&nbsp"+"</TD>");
				result.append("</TR>");
			}
		
			result.append("</TR>");
		}
		
	
		
		result.append(
			"</TABLE>"+
			"</FONT></BODY>"+
			"</HTML>");
		
		return result.toString();
	}
}
