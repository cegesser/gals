package gesser.gals.util;

import gesser.gals.InputPane;
import gesser.gals.InputPane.Data;
import gesser.gals.generator.Options;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PushbackInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import static gesser.gals.generator.Options.Parser.*;
import static gesser.gals.generator.Options.Language.*;
import static gesser.gals.generator.Options.Input.*;
import static gesser.gals.generator.Options.ScannerTable.*;

/**
 * @author Gesser
 */

public class XMLProcessor
{
	private static Document doc = null;
	
	public static GalsData load(InputStream input) throws XMLParsingException, IOException
	{
		PushbackInputStream pbis = new PushbackInputStream(input);
		int c = pbis.read();
		pbis.unread(c);
		
		if (c == '#')
			return newLoad(pbis);
		else
			return xmlLoad(pbis);
	}
	
	private static GalsData newLoad(InputStream input) throws XMLParsingException
	{	
		return GalsData.fromReader(new InputStreamReader(input));
	}

	public static GalsData xmlLoad(InputStream input) throws XMLParsingException, IOException
	{	
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try
		{
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.parse(input);
			
			Element root = (Element)doc.getFirstChild();
			
			Element opt = (Element) root.getFirstChild();
			
			Options options = getOptions(opt);
			
			Element d = (Element) opt.getNextSibling();
			
			InputPane.Data data = getData(d);		
			return new GalsData(options, data);				
		}
		catch (SAXException e)
		{
			throw new XMLParsingException(e.getMessage());
		}
		catch (ParserConfigurationException e)
		{
			throw new XMLParsingException(e.getMessage());
		}				
	}
		
	public static void store(GalsData data, OutputStream output)
	{
		PrintStream out = new PrintStream(output);
		
		out.print(data.toString());
		out.flush();
	}
		
	private static Options getOptions(Element opt)
	{
		Options options = new Options();
		
		Element current = (Element) opt.getFirstChild();
		
		while (current != null)
		{
			String name  = current.getChildNodes().item(0).getFirstChild().getNodeValue();
			String value = "";
			Node n = current.getChildNodes().item(1).getFirstChild();
			if (n != null)
				value = n.getNodeValue();
			
			setOption(options, name, value);
			
			current = (Element) current.getNextSibling();
		}
		
		return options;
	}
	
	private static void setOption(Options options, String name, String value)
	{
		if (name.equals("scanner.gen"))
		{
			options.generateScanner = value.equals("true");
		}
		else if (name.equals("parser.gen"))
		{
			options.generateParser = value.equals("true");
		}
		else if (name.equals("scanner.sensitive"))
		{
			options.scannerCaseSensitive = value.equals("true");
		}
		else if (name.equals("scanner.name"))
		{
			options.scannerName = value;
		}
		else if (name.equals("parser.name"))
		{
			options.parserName = value;
		}
		else if (name.equals("semantic.name"))
		{
			options.semanticName = value;
		}
		else if (name.equals("package"))
		{
			options.pkgName = value;
		}
		else if (name.equals("language"))
		{
			if (value.equals("java"))
				options.language = JAVA;
			else if (value.equals("c++"))
				options.language = CPP;
			else //lang == pascal
				options.language = DELPHI;
		}
		else if (name.equals("parser"))
		{
			if (value.equals("RD"))
				options.parser = RD;
			else if (value.equals("LL"))
				options.parser = LL;
			else if (value.equals("SLR"))
				options.parser = SLR;
			else if (value.equals("LALR"))
				options.parser = LALR;
			else //parser == LR
				options.parser = LR;
		}
		else if (name.equals("input"))
		{
			if (value.equals("stream"))
				options.input = STREAM;
			else //in == string
				options.input = STRING;
		}
		else if (name.equals("table.scanner"))
		{
			if (value.equals("full"))
				options.scannerTable = FULL;
			else if (value.equals("compact"))
				options.scannerTable = COMPACT;
			else //lex == hard
				options.scannerTable = HARDCODE;
		}
	}
	
	private static Data getData(Element d)
	{
		Element defsE = (Element) d.getFirstChild();
		Element tokensE = (Element) defsE.getNextSibling();
		Element ntE = (Element) tokensE.getNextSibling();
		Element gE = (Element) ntE.getNextSibling();
		
		String defs = "";
		String tokens = "";
		String nt = "";
		String g = "";
		
		Node n = defsE.getFirstChild();
		if (n != null)
			defs = n.getNodeValue();
			
		n = tokensE.getFirstChild();
		if (n != null)
			tokens = n.getNodeValue();
			
		n = ntE.getFirstChild();
		if (n != null)
			nt = n.getNodeValue();
			
		n = gE.getFirstChild();
		if (n != null)
			g = n.getNodeValue();
		
		return new InputPane.Data(defs, tokens, nt, g);
	}	
}
