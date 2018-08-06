package gesser.gals.scannerparser;

import java.util.StringTokenizer;

import gesser.gals.analyser.AnalysisError;
import gesser.gals.analyser.LexicalError;
import gesser.gals.analyser.SyntaticError;
import gesser.gals.analyser.Token;
import gesser.gals.generator.scanner.FiniteAutomata;
import gesser.gals.util.MetaException;

import static gesser.gals.util.MetaException.Mode.*;

/**
 * @author Gesser
 */

public class LineParser
{
	
	private LineScanner scanner = new LineScanner();
	private int pos = 0;
	
	private FiniteAutomataGenerator gen = null;
	
	public FiniteAutomata parseFA(String defs, String tokens) throws MetaException
	{
		gen = new FiniteAutomataGenerator();
		
		parseDefs(defs);
		parseTokens(tokens);
		
		try
		{
			return gen.generateAutomata();
		}
		catch (AnalysisError ee)
		{
			throw new MetaException(TOKEN, 0, ee);
		}
	}
	
	private void parseDefs(String string) throws MetaException
	{
		StringTokenizer tknzr = new StringTokenizer(string, "\n");
		int lineCount = 0;
		
		while (tknzr.hasMoreTokens())
		{
			String line = tknzr.nextToken();
			
			if (line.equals("\n"))
			{
				lineCount++;
				continue;
			}
			
			scanner.setText(line);
			
			try
			{
				Token t=nextToken();
				
				pos = 0;
				if (t != null && t.getId() == LineScanner.ID)
				{
					String id = t.getLexeme();
					pos = t.getPosition()+id.length();
					t = nextToken();
					if (t != null && t.getId() == LineScanner.COLON)
					{
						pos = t.getPosition()+1;
						t = nextToken();
						if (t != null && t.getId() == LineScanner.RE)
						{
							String re = t.getLexeme();
							
							try
							{
								gen.addDefinition(id, parseRE(re));
							}
							catch (AnalysisError e)
							{
								e.setPosition(e.getPosition()+ pos);
								throw e;
							}	
						}
						else
							throw new SyntaticError("Era esperado uma Expressão Regular", pos);
					}
					else
						throw new SyntaticError("Era esperado ':'", pos);
				}
				else if (t == null)
					continue;
				else
					throw new SyntaticError("Era esperado um identificador", pos);
			}
			catch (AnalysisError e)
			{
				throw new MetaException(DEFINITION, lineCount, e);
			}
		}
	}
	
	private void parseTokens(String string) throws MetaException
	{
		int lineCount = 0;
		
		StringTokenizer tknzr = new StringTokenizer(string, "\n", true);
		
		while (tknzr.hasMoreTokens())
		{
			String line = tknzr.nextToken();			
			
			if (line.equals("\n"))
			{
				lineCount++;
				continue;
			}
			
			scanner.setText(line);
			
			try
			{
				Token t=nextToken();
				
				pos = 0;
				
				if (t != null)
				{
					pos = t.getPosition() + t.getLexeme().length();
					switch (t.getId())
					{
						case LineScanner.COLON:
							parseIgnore();
							break;
						case LineScanner.ID:
						case LineScanner.STR:
							parseId(t);
							break;
						default:
							throw new SyntaticError("Era esperado um identificador", 0);
					}
				}
			}
			catch (AnalysisError e)
			{
				throw new MetaException(TOKEN, lineCount, e);
			}
		}
	}

	private void parseIgnore() throws AnalysisError
	{
		Token t = nextToken();
		if (t != null && t.getId() == LineScanner.RE)
		{
			String re = t.getLexeme();
			
			try
			{
				if (re.charAt(0) == '!')
					gen.addIgnore(parseRE(re.substring(1)), false);
				else
					gen.addIgnore(parseRE(re), true);
			}
			catch (AnalysisError e)
			{
				e.setPosition(e.getPosition()+t.getPosition());
				throw e;
			}	
		}
		else
			throw new SyntaticError("Era esperado uma Expressão Regular", pos);
	}

	private void parseId(Token t) throws AnalysisError
	{
		String id = t.getLexeme();
		
		t = nextToken();
		
		if (t == null)
		{
			try
			{
				gen.addExpression(id, parseRE(id), true);
			}
			catch (AnalysisError e)
			{
				e.setPosition(e.getPosition()+t.getPosition());
				throw e;
			}			
		}
		else
		{
			pos = t.getPosition() + t.getLexeme().length();
			
			switch (t.getId())
			{
				case LineScanner.COLON:
					parseIdEnd(id);
					break;
				case LineScanner.EQUALS:
					parseSpecialCase(id);
					break;
				default:
					pos = t.getPosition();
					throw new SyntaticError("Era esperado ':' ou '='", pos);
			}
		}
	}

	private void parseIdEnd(String id) throws AnalysisError
	{
		Token t = nextToken();
		
		if (t == null || t.getId() != LineScanner.RE)
			throw new SyntaticError("Era esperado uma Expressão Regular", pos);
		
		String re = t.getLexeme();
		
		try
		{
			if (re.charAt(0) == '!')
				gen.addExpression(id, parseRE(re.substring(1)), false);
			else
				gen.addExpression(id, parseRE(re), true);
		}
		catch (AnalysisError e)
		{
			e.setPosition(e.getPosition()+t.getPosition());
			throw e;
		}
	}

	private void parseSpecialCase(String id) throws AnalysisError
	{
		Token t=nextToken();
				
		if (t != null && t.getId() == LineScanner.ID)
		{
			String id2 = t.getLexeme();
			pos = t.getPosition()+id.length();
			t = nextToken();
			if (t != null && t.getId() == LineScanner.COLON)
			{
				pos = t.getPosition()+1;
				t = nextToken();
				if (t != null && t.getId() == LineScanner.STR)
				{
					String re = t.getLexeme();
					re = re.substring(1, re.length()-1);
					
					try
					{
						gen.addSpecialCase(id, id2, re);
					}
					catch (AnalysisError e)
					{
						e.setPosition(e.getPosition()+t.getPosition());
						throw e;
					}
					
					t = nextToken();
					if (t != null)
						throw new SyntaticError("Só é permitido uma definição por linha", t.getPosition());
				}
				else
					throw new SyntaticError("Era esperado uma Expressão Regular", pos);
			}
			else
				throw new SyntaticError("Era esperado ':'", pos);
		}
		else
			throw new SyntaticError("Era esperado um Identificador", pos);
	}
	
	private Token nextToken() throws LexicalError
	{
		Token t = scanner.nextToken();
		
		if (t != null)
		{
			if (t.getId() == LineScanner.COMMENT)
				t = nextToken();
			else if (t.getId() == LineScanner.ERROR)
				throw new LexicalError("Token inválido", t.getPosition());
		}
		
		return t;
	}	
	
	private Node parseRE(String re) throws AnalysisError
	{
		REParser parser = new REParser();
		
		return parser.parse(re, gen);
	}
}
