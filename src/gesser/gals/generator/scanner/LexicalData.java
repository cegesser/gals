package gesser.gals.generator.scanner;

import gesser.gals.analyser.AnalysisError;
import gesser.gals.analyser.SemanticError;
import gesser.gals.scannerparser.FiniteAutomataGenerator;
import gesser.gals.scannerparser.Node;
import gesser.gals.scannerparser.REParser;
import gesser.gals.util.MetaException;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import static gesser.gals.util.MetaException.Mode.*;

public class LexicalData
{
	public static class SpecialCaseValue
	{
		private String lexeme;
		private String base;
		
		public SpecialCaseValue(String lexeme, String base)
		{
			this.lexeme = lexeme;
			this.base = base;
		}
		
		public String getLexeme() { return lexeme; }
		public String getBase() { return base; }
	}	
	
	private Map<String, String> expressionFor = new HashMap<String, String>();
	private Map<String, SpecialCaseValue> specialCasesValues = new HashMap<String, SpecialCaseValue>();
	
	private List<String> definitions = new ArrayList<String>();	
	private List<String> tokens = new ArrayList<String>();
	private List<String> specialCases = new ArrayList<String>();
	private String ignore = "";
		
	public void addDefinition(String token, String expression)
	{
		definitions.add(token);
		expressionFor.put(token, expression);
	}
	
	public void addToken(String token, String expression)
	{
		tokens.add(token);
		expressionFor.put(token, expression);
	}
	
	public void clear()
	{
		definitions.clear();
		tokens.clear();
		specialCases.clear();
		expressionFor.clear();
		specialCasesValues.clear();		
	}
	
	public String expressionFor(String token)
	{
		return expressionFor.get(token);
	}
	
	public List<String> getTokens()
	{
		return tokens;
	}
	
	public List<String> getDefinitions()
	{
		return definitions;
	}

	public List<String> getSpecialCases()
	{
		return specialCases;
	}

	public String getIgnore()
	{
		return ignore;
	}

	public void addIgnore(String ignore)
	{
		if (this.ignore.length() > 0)
			this.ignore = this.ignore+"|"+ignore;
		else
			this.ignore = ignore;
	}

	public void addSpecialCase(String name, String value, String base)
	{		
		specialCases.add(name);
		specialCasesValues.put(name, new SpecialCaseValue(value, base));
	}
	
	public SpecialCaseValue getSpecialCase(String name) throws SemanticError
	{		
		return specialCasesValues.get(name);
	}

	public FiniteAutomata getFA() throws MetaException
    {
    	REParser parser = new REParser();
		FiniteAutomataGenerator gen = new FiniteAutomataGenerator();
		
		int i = -1;
		try
		{	
			for (i=0; i<definitions.size(); i++)
			{						
				Node n = parser.parse(expressionFor(definitions.get(i)), gen);
				
				gen.addDefinition(definitions.get(i), n);
			}
		}
		catch (AnalysisError ee)
		{
			throw new MetaException(DEFINITION, i, ee);
		}
			
		try
		{
			for (i=0; i<tokens.size(); i++)
			{						
				Node n = parser.parse(expressionFor(tokens.get(i)), gen);
				
				gen.addExpression(tokens.get(i), n, true);
			}
		}
		catch (AnalysisError ee)
		{
			throw new MetaException(TOKEN, i, ee);
		}
		
		try
		{
			for (i=0; i<specialCases.size(); i++)
			{					
				String t = 	specialCases.get(i);
				SpecialCaseValue v = specialCasesValues.get(t);
				
				gen.addSpecialCase(t, v.base, v.lexeme);
			}
		}
		catch (AnalysisError ee)
		{
			throw new MetaException(TOKEN, i, ee);
		}
		
		try
		{						
			if (ignore.length() > 0)
			{
				Node n = parser.parse(ignore, gen);
				
				gen.addIgnore(n, true);
			}
		}
		catch (AnalysisError ee)
		{
			throw new MetaException(TOKEN, tokens.size(), ee);
		}
		
		try
		{
			return gen.generateAutomata();
		}
		catch (AnalysisError ee)
		{
			throw new MetaException(TOKEN, tokens.size(), ee);
		}
    }
}
