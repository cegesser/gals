package gesser.gals.util;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;

import gesser.gals.InputPane;
import gesser.gals.generator.Options;

public class GalsData
{
	private InputPane.Data data;
	private Options options;
	
	public GalsData(Options options, InputPane.Data data)
	{
		this.options = options;
		this.data = data;
	}

	public InputPane.Data getData()
	{
		return data;
	}
	
	public Options getOptions()
	{
		return options;
	}
	
	public String toString()
	{
		StringBuffer bfr = new StringBuffer();
		
		bfr.append("#Options\n");
		bfr.append(options.toString());
		
		if (options.generateScanner)
		{
			bfr.append("#RegularDefinitions\n");
			bfr.append(data.getDefinitions()).append('\n');
		}
		bfr.append("#Tokens\n");
		bfr.append(data.getTokens()).append('\n');
		if (options.generateParser)
		{
			bfr.append("#NonTerminals\n");
			bfr.append(data.getNonTerminals()).append('\n');
			
			bfr.append("#Grammar\n");
			bfr.append(data.getGrammar()).append('\n');
		}
		
		return bfr.toString();
	}

	public static GalsData fromReader(Reader r) throws XMLParsingException
	{
		Options opt = null;
		String defs = "", tokens = "", nonTerms = "", gram = "";
		
		LineNumberReader in = new LineNumberReader(r);
		String line = null;
		try
		{
			String section = null;
			StringBuffer bfr = new StringBuffer();
			while ( (line = in.readLine()) != null )
			{
				String linet = line.trim();
				if (linet.length() > 0 && linet.charAt(0) == '#')
				{
					if (section != null)
					{
						if (bfr.length() > 0)
							bfr.setLength(bfr.length()-1);
					
						if (section.equalsIgnoreCase("#Options"))
							opt = Options.fromString(bfr.toString());
						else if (section.equalsIgnoreCase("#RegularDefinitions"))
							defs = bfr.toString();
						else if (section.equalsIgnoreCase("#Tokens"))
							tokens = bfr.toString();
						else if (section.equalsIgnoreCase("#NonTerminals"))
							nonTerms = bfr.toString();
						else if (section.equalsIgnoreCase("#Grammar"))
							gram = bfr.toString();
						else
							throw new XMLParsingException("Erro processando arquivo");
					}
					section = linet;
					bfr = new StringBuffer();
				}
				else
					bfr.append(line).append('\n');
			}
			if (section != null)
			{
				if (bfr.length() > 0)
					bfr.setLength(bfr.length()-1);
							
				if (section.equalsIgnoreCase("#Options"))
					opt = Options.fromString(bfr.toString());
				else if (section.equalsIgnoreCase("#RegularDefinitions"))
					defs = bfr.toString();
				else if (section.equalsIgnoreCase("#Tokens"))
					tokens = bfr.toString();
				else if (section.equalsIgnoreCase("#NonTerminals"))
					nonTerms = bfr.toString();
				else if (section.equalsIgnoreCase("#Grammar"))
					gram = bfr.toString();
				else
					throw new XMLParsingException("Erro processando arquivo");
			}
		}
		catch (IOException e)
		{
			throw new XMLParsingException("Erro processando arquivo");
		}
		
		return new GalsData(opt, new InputPane.Data(defs, tokens, nonTerms, gram));
	}

	public static GalsData fromString(String str) throws XMLParsingException
	{
		return fromReader(new StringReader(str));
	}
}
