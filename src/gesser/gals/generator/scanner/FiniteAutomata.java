package gesser.gals.generator.scanner;

import gesser.gals.HTMLDialog;
import gesser.gals.analyser.SemanticError;
import gesser.gals.simulator.FiniteAutomataSimulator;
import gesser.gals.util.IntegerSet;

import java.util.List;
import java.util.Map;

public class FiniteAutomata
{
	public static class KeyValuePar
	{
		public String key;
		public int value;
		
		public KeyValuePar(String key, int value)
		{
			this.key = key;
			this.value = value;
		}
		
		public String toString()
		{
			return "['"+key+"'->"+value+"]";
		}
	}

	private List<Map<Character, Integer>> transitions;
	private int[] finals;
	private int[][] context;
	private IntegerSet alphabet;
	private List<String> tokenNames;
	private String[] errors;
	private boolean hasContext = false;
	
	private int[][] specialCasesIndexes;
	private KeyValuePar[] specialCases;
	
	public List<Map<Character, Integer>> getTransitions()
	{
		return transitions;
	}
	
	public List<String> getTokens()
	{
		return tokenNames;
	}
	
	public KeyValuePar[] getSpecialCases()
	{
		return specialCases;
	}
	
	public int[][] getSpecialCasesIndexes()
	{
		return specialCasesIndexes;
	}
	
	public String getError(int state)
	{
		return errors[state];
	}
	
	public boolean isContext(int state)
	{
		return context[state][0] == 1;
	}
	
	public int getOrigin(int state)
	{
		return context[state][1];
	}
	
	public boolean hasContext()
	{
		return hasContext;
	}
	
	public FiniteAutomata(
		IntegerSet alphabet, List<Map<Character, Integer>> transitions, 
		int[] finals, int[][] specialCasesIndexes, 
		KeyValuePar[] specialCases, int[][] context, List<String> tokenNames) throws SemanticError
	{
		this.alphabet = alphabet;
		this.transitions = transitions;
		this.finals = finals;
		this.context = context;	
		this.specialCasesIndexes = specialCasesIndexes;
		this.specialCases = specialCases;
		this.tokenNames = tokenNames;
		
		for (int[] ctx : context)
		{
			if (ctx[0] == 1)
			{
				hasContext = true;
				break;
			}
		}
		
		buildErrors();
		
		checkSpecialCases();
	}
	
	private void checkSpecialCases() throws SemanticError
	{
		FiniteAutomataSimulator sim = new FiniteAutomataSimulator(this);
		for (int i = 0; i < specialCasesIndexes.length; i++)
		{
			int[] index = specialCasesIndexes[i];
			for (int j=index[0]; j<index[1]; j++)
			{
				if (sim.analyse(specialCases[j].key) != i)
					throw new SemanticError("O valor \""+specialCases[j].key+
						"\" não é válido como caso especial de '"+tokenNames.get(i-2)+
                       "', na definição de '"+tokenNames.get(specialCases[j].value-2)+"'" );
			}
		}
	}
	
	public int nextState(char c, int state)
	{
		Integer in = transitions.get(state).get(Character.valueOf(c));
		if (in == null)
			return -1;
		else
			return in.intValue();
	}
	
	public int tokenForState(int state)
	{
		if (state < 0 || state >= finals.length)
			return -1;
			
		return finals[state];
	}
	
	public String toString()
	{
		int max = String.valueOf(transitions.size()).length();
		
		StringBuffer bfr = new StringBuffer();

		for (int i=0; i< max*2 + 1; i++)		
			bfr.append(' ');
		bfr.append('|');
		
		for (Integer i : alphabet)
		{
			char c = (char) i.intValue();
			for (int j = 0; j<max/2; j++)
				bfr.append(' ');
			bfr.append(c);
			for (int j = max/2+1; j < max; j++)
				bfr.append(' ');
			bfr.append('|');
		}
		bfr.append('\n');
		
		for (int it = 0; it < transitions.size(); it++ )
		{
			String f = "";
			if (finals[it] >= 0)
				f = String.valueOf(finals[it]) + '*';
			
			for (int i=0; i<max+1-f.length(); i++)
				bfr.append(' ');
			bfr.append(f);
				
			String s = String.valueOf(it);
			for (int i=0; i<max-s.length(); i++)
				bfr.append(' ');
			bfr.append(s);
			bfr.append('|');
			
			Map<Character, Integer> x = transitions.get(it);
			for (Integer i : alphabet)
			{
				Integer integ = x.get(Character.valueOf((char) i.intValue()));
				String str = "";
				if (integ.intValue() >= 0)
					str = integ.toString();
					
				for (int j = 0; j<max-str.length(); j++)
					bfr.append(' ');
				bfr.append(str);
				bfr.append('|');
			}
			bfr.append('\n');
		}

		return bfr.toString();
	}	
	
	public String asHTML()
	{
		StringBuffer result = new StringBuffer();

		result.append(
		"<HTML>"+
		"<HEAD>"+
		"<TITLE> Tabela de Transições </TITLE>"+
		"</HEAD>"+
		"<BODY><FONT face=\"Verdana, Arial, Helvetica, sans-serif\">"+
		"<TABLE border=1 cellspacing=0>");
		
		result.append(
                "<TR align=center>"+
                "<TD rowspan=\"2\" bgcolor=black><FONT color=white><B>ESTADO</B></FONT></TD>"+
                "<TD rowspan=\"2\" bgcolor=black><FONT color=white><B>TOKEN<BR>RETORNADO</B></FONT></TD>"+
                "<TD colspan=\""+alphabet.size()+"\" bgcolor=black><FONT color=white><B>ENTRADA</B></FONT></TD>"+
				"</TR>"+
				"<TR align=center>");
			
		for (Integer i : alphabet )
		{
			char c = (char)i.intValue();
			result.append("<TD bgcolor=#99FF66 nowrap><B>"+getChar(c)+"</B></TD>");
		}
		result.append("</TR>");
                      
		for (int it = 0; it < transitions.size(); it++ )
		{
			result.append("<TR align=center>"+
						  "<TD bgcolor=#99FF66><B>"+it+"</B></TD>");
			int t = finals[it];
			String clr = /*context[it] ? "#9999FF" : */null;
			
			if (t > 0)
			{
				if (clr == null)
					clr = "#FFFFCC";
					
				String caption = HTMLDialog.translateString(tokenNames.get(t-2));
				if (getOrigin(it) >= 0)
					caption += " / "+getOrigin(it);
				result.append("<TD bgcolor="+clr+" nowrap>"+caption+"</TD>");
			}
			else if (t == 0)
			{
				if (clr == null)
					clr = "#99CCFF";
				result.append("<TD bgcolor="+clr+"><B>:</B></TD>");
			}
			else if (t == -2)
				result.append("<TD bgcolor=#FF0000>?</TD>");
			else
			{
				if (clr == null)
					clr = "#FFCC99";
				result.append("<TD bgcolor="+clr+">?</TD>");
			}
				
			Map<Character, Integer> x = transitions.get(it);
			for (Integer i : alphabet)
			{
				result.append("<TD width=40 bgcolor=#F5F5F5>");
				Integer integ = x.get(Character.valueOf((char)i.intValue()));
				
				if (integ != null && integ.intValue() >= 0)
					result.append(integ);
				else
					result.append("-");
					
				result.append("</TD>");
			}
			result.append("</TR>");
		}
		
		result.append(
		"</TABLE>"+
		"</FONT></BODY>"+
		"</HTML>"+		"");
				
		return result.toString();
	}
	
	private String getChar(char c)
	{
		switch (c)
		{
			case '\n' : return "\\n";
			case '\r' : return "\\r";
			case '\t' : return "\\t";
			case ' ' : return "' '";
			
			case '"' : return "&quot;";
			case '&' : return "&amp;";
			case '<' : return "&lt;";
			case '>' : return "&gt;";
			
			default: 
				if ( (c>=32 && c <= 126) || (c>=161 && c<=255))
					return ""+c;
				else
					return ""+(int)c;
		}
	}
	
	private IntegerSet finalStatesFromState(int state)
	{
		IntegerSet visited = new IntegerSet();
		visited.add(state);

		boolean changed = true;

		loop: while (changed)
		{			
			changed = false;
			for (Integer st : visited)
			{
				for (Integer v : alphabet)
				{
					char c = (char)v.intValue();
					int next = nextState(c, st.intValue());
					if (next != -1 && !visited.contains(next))
					{
						visited.add(next);
						changed = true;
						continue loop;
					}
				}
			}
		}

		IntegerSet result = new IntegerSet();
		
		for (Integer i : visited)
		{
			int token = tokenForState(i.intValue());
			if (token >= 0)
				result.add(i.intValue());
		}
		
		return result;
	}
	
	private IntegerSet tokensFromState(int state)
	{
		IntegerSet visited = finalStatesFromState(state);
		
		IntegerSet result = new IntegerSet();
		
		for (Integer i : visited)
		{
			int token = tokenForState(i.intValue());
			if (token >= 0)
				result.add(token);
		}
		
		return result;
	}
	
	private void buildErrors()
	{
		errors = new String[transitions.size()];
		/*
		if (tokenForState(0) >= 0)
			errors[0] = "";
		else*/
			errors[0] = "Caractere não esperado";
		
		for (int i = 1; i < transitions.size(); i++ )
		{
			if (tokenForState(i) >= 0)
				errors[i] = "";
			else
			{				
				IntegerSet tokens = tokensFromState(i);
				StringBuffer bfr = new StringBuffer("Erro identificando ");
				for (Integer t : tokens)
				{
					if (t.intValue()  > 0)
						bfr.append(tokenNames.get(t.intValue() - 2));
					else
						bfr.append("<ignorar>");
					bfr.append(" ou ");
				}
				bfr.setLength(bfr.length()-4);
				errors[i] = bfr.toString();
			}
		}
	}	
}
