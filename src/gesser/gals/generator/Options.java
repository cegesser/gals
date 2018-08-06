package gesser.gals.generator;

import gesser.gals.util.XMLParsingException;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.StringTokenizer;

public class Options
{
	public String scannerName  = "Lexico";
	public String parserName   = "Sintatico";
	public String semanticName = "Semantico";
	public String pkgName      = "";
	
	public boolean generateScanner = true;
	public boolean generateParser  = true;
	
	public enum Language 
	{ 
		JAVA   { public String toString() { return "Java"; } }, 
		CPP    { public String toString() { return "C++"; } }, 
		DELPHI { public String toString() { return "Delphi"; } } 
	}
	
	public Language language = Language.JAVA;
	
	public enum Parser { LR, LALR, SLR, LL, RD }
	
	public Parser parser = Parser.SLR;
	
	public boolean scannerCaseSensitive = true;
	
	public enum ScannerTable { FULL, COMPACT, HARDCODE }

	public ScannerTable scannerTable = ScannerTable.FULL;

	public enum Input { STRING, STREAM }
	
	public Input input = Input.STRING;
	
	public String toString()
	{
		StringWriter bfr = new StringWriter();
		PrintWriter out = new PrintWriter(bfr);
		
		out.println("GenerateScanner = "+generateScanner);
		out.println("GenerateParser = "+generateParser);
		
		out.println("Language = " + language);
		
		out.println("ScannerName = "+scannerName);
		if (generateParser)
		{
			out.println("ParserName = "+parserName);
			out.println("SemanticName = "+semanticName);
		}
		if (pkgName.length() > 0)
			out.println("Package = "+pkgName);
		if (generateScanner)
		{
			out.println("ScannerCaseSensitive = "+scannerCaseSensitive);
			
			out.println("ScannerTable = " + scannerTable);
			
			out.println("Input = " + input);			
		}
		if (generateParser)
		{
			out.println("Parser = " + parser);
		}		
		out.flush();
		return bfr.toString();
	}

	public static Options fromString(String str) throws XMLParsingException
	{
		Options o = new Options();
		
		LineNumberReader in = new LineNumberReader(new StringReader(str));
		String line = null;
		try
		{
			while ( (line = in.readLine()) != null)
			{   
				StringTokenizer tknzr = new StringTokenizer(line);
				
                if (! tknzr.hasMoreTokens())
                    continue;
                
				String name = tknzr.nextToken();
				if (!tknzr.hasMoreTokens())
					throw new XMLParsingException("Erro processando arquivo");
				tknzr.nextToken();//=
				String value = "";
				if (tknzr.hasMoreTokens())
					value = tknzr.nextToken();
				
				o.setOption(name, value);
			}
		}
		catch (IOException e)
		{
			throw new XMLParsingException("Erro processando arquivo");
		}
		
		return o;
	}

	/**
	 * @param name
	 * @param value
	 */
	private void setOption(String name, String value) throws XMLParsingException
	{
		if (name.equalsIgnoreCase("GenerateScanner"))
			generateScanner = Boolean.valueOf(value).booleanValue();
		else if (name.equalsIgnoreCase("GenerateParser"))
			generateParser = Boolean.valueOf(value).booleanValue();
		else if (name.equalsIgnoreCase("Language"))
		{
			if (value.equalsIgnoreCase("C++"))
				language = Language.CPP;
			else if (value.equalsIgnoreCase("Java"))
				language = Language.JAVA;
			else if (value.equalsIgnoreCase("Delphi"))
				language = Language.DELPHI;
			else
				throw new XMLParsingException("Erro processando arquivo");
		}
		else if (name.equalsIgnoreCase("ScannerName"))
			scannerName = value;
		else if (name.equalsIgnoreCase("ParserName"))
			parserName = value;
		else if (name.equalsIgnoreCase("SemanticName"))
			semanticName = value;
		else if (name.equalsIgnoreCase("Package"))
			pkgName = value;
		else if (name.equalsIgnoreCase("ScannerCaseSensitive"))
			scannerCaseSensitive = Boolean.valueOf(value).booleanValue();
		else if (name.equalsIgnoreCase("ScannerTable"))
		{
			if (value.equalsIgnoreCase("Full"))
				scannerTable = ScannerTable.FULL;
			else if (value.equalsIgnoreCase("Compact"))
				scannerTable = ScannerTable.COMPACT;
			else if (value.equalsIgnoreCase("Hardcode"))
				scannerTable = ScannerTable.HARDCODE;
			else
				throw new XMLParsingException("Erro processando arquivo");
		}
		else if (name.equalsIgnoreCase("Input"))
		{
			if (value.equalsIgnoreCase("Stream"))
				input = Input.STREAM;
			else if (value.equalsIgnoreCase("String"))
				input = Input.STREAM;
			else
				throw new XMLParsingException("Erro processando arquivo");
		}
		else if (name.equalsIgnoreCase("Parser"))
		{
			if (value.equalsIgnoreCase("LR"))
				parser = Parser.LR;
			else if (value.equalsIgnoreCase("LALR"))
				parser = Parser.LALR;
			else if (value.equalsIgnoreCase("SLR"))
				parser = Parser.SLR;
			else if (value.equalsIgnoreCase("LL"))
				parser = Parser.LL;
			else if (value.equalsIgnoreCase("RD"))
				parser = Parser.RD;
			else
				throw new XMLParsingException("Erro processando arquivo");
		}
		else
			throw new XMLParsingException("Erro processando arquivo");						
	}	
	
	public static void main(String[] args) throws XMLParsingException
	{
		String in = 
		"GenerateScanner = true\n"+
		"GenerateParser = true\n"+
		"Language = C++\n"+
		"ScannerName = Lexico\n"+
		"ParserName = Sintatico\n"+
		"SemanticName = Semantico\n"+
		"Package = \n"+
		"ScannerCaseSensitive = true\n"+
		"ScannerTable = Compact\n"+
		"Input = Stream\n"+
		"Parser = LALR\n";
		
		System.out.println(Options.fromString(in));
	}
}
