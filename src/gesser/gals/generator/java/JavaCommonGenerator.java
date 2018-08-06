package gesser.gals.generator.java;

import gesser.gals.generator.Options;
import gesser.gals.generator.parser.Grammar;
import gesser.gals.generator.parser.Production;
import gesser.gals.generator.parser.ll.LLParser;
import gesser.gals.generator.parser.ll.NotLLException;
import gesser.gals.generator.parser.lr.Command;
import gesser.gals.generator.parser.lr.LRGeneratorFactory;
import gesser.gals.generator.scanner.FiniteAutomata;
import gesser.gals.util.IntList;

import java.util.*;

import static gesser.gals.generator.Options.Parser.*;
import static gesser.gals.generator.Options.ScannerTable.*;

public class JavaCommonGenerator
{
	int[][][] lrTable = null;
	
	public Map<String, String> generate(FiniteAutomata fa, Grammar g, Options options) throws NotLLException
	{
		Map<String, String> result = new HashMap<String, String>();
				
		result.put("Token.java", generateToken(options));

		result.put("Constants.java", generateConstants(fa, g, options));
		if (fa != null)
			result.put("ScannerConstants.java", generateScannerConstants(fa, options));
		if (g != null)
			result.put("ParserConstants.java", generateParserConstants(g, options));	
		
		result.put("AnalysisError.java", generateAnalysisError(options));
		result.put("LexicalError.java", generateLexicalError(options));
		result.put("SyntaticError.java", generateSyntaticError(options));
		result.put("SemanticError.java", generateSemanticError(options));	
		
		return result;
	}
	
	private String generateToken(Options options)
	{
		StringBuffer result = new StringBuffer();
		
		String package_ = options.pkgName;
		if (package_ != null && !package_.equals(""))
			result.append("package " + package_ + ";\n\n");
			
		String cls = 
		"public class Token\n"+
		"{\n"+
		"    private int id;\n"+
		"    private String lexeme;\n"+
		"    private int position;\n"+
		"\n"+
		"    public Token(int id, String lexeme, int position)\n"+
		"    {\n"+
		"        this.id = id;\n"+
		"        this.lexeme = lexeme;\n"+
		"        this.position = position;\n"+
		"    }\n"+
		"\n"+	
		"    public final int getId()\n"+
		"    {\n"+
		"        return id;\n"+
		"    }\n"+
		"\n"+
		"    public final String getLexeme()\n"+
		"    {\n"+
		"        return lexeme;\n"+
		"    }\n"+
		"\n"+
		"    public final int getPosition()\n"+
		"    {\n"+
		"        return position;\n"+
		"    }\n"+
		"\n"+
		"    public String toString()\n"+
		"    {\n"+
		"        return id+\" ( \"+lexeme+\" ) @ \"+position;\n"+
		"    };\n"+
		"}\n"+
		"";
		
		result.append(cls);  
		
		return result.toString();
	}
	
	private String generateAnalysisError(Options options)
	{
		StringBuffer result = new StringBuffer();
		
		String package_ = options.pkgName;
		if (package_ != null && !package_.equals(""))
			result.append("package " + package_ + ";\n\n");
			
		String cls = 
		"public class AnalysisError extends Exception\n"+
		"{\n"+	
		"    private int position;\n"+
		"\n"+
		"    public AnalysisError(String msg, int position)\n"+
		"    {\n"+
		"        super(msg);\n"+
		"        this.position = position;\n"+
		"    }\n"+
		"\n"+	
		"    public AnalysisError(String msg)\n"+
		"    {\n"+
		"        super(msg);\n"+
		"        this.position = -1;\n"+
	    "    }\n"+
		"\n"+
		"    public int getPosition()\n"+
		"    {\n"+
		"        return position;\n"+
	    "    }\n"+
		"\n"+
		"    public String toString()\n"+
		"    {\n"+
		"        return super.toString() + \", @ \"+position;\n"+
		"    }\n"+
		"}\n"+
		"";
		
		result.append(cls);
		
		return result.toString();
	}

	private String generateLexicalError(Options options)
	{
		StringBuffer result = new StringBuffer();
		
		String package_ = options.pkgName;
		if (package_ != null && !package_.equals(""))
			result.append("package " + package_ + ";\n\n");
			
		String cls = 
		"public class LexicalError extends AnalysisError\n"+
		"{\n"+
		"    public LexicalError(String msg, int position)\n"+
		"	 {\n"+
		"        super(msg, position);\n"+
		"    }\n"+
		"\n"+
		"    public LexicalError(String msg)\n"+
		"    {\n"+
		"        super(msg);\n"+
		"    }\n"+
		"}\n"+
		"";
		
		result.append(cls);
		
		return result.toString();
	}
	
	private String generateSyntaticError(Options options)
	{
		StringBuffer result = new StringBuffer();
		
		String package_ = options.pkgName;
		if (package_ != null && !package_.equals(""))
			result.append("package " + package_ + ";\n\n");
			
		String cls = 
		"public class SyntaticError extends AnalysisError\n"+
		"{\n"+
		"    public SyntaticError(String msg, int position)\n"+
		"	 {\n"+
		"        super(msg, position);\n"+
		"    }\n"+
		"\n"+
		"    public SyntaticError(String msg)\n"+
		"    {\n"+
		"        super(msg);\n"+
		"    }\n"+
		"}\n"+
		"";
		
		result.append(cls);
		
		return result.toString();
	}
	
	private String generateSemanticError(Options options)
	{
		StringBuffer result = new StringBuffer();
		
		String package_ = options.pkgName;
		if (package_ != null && !package_.equals(""))
			result.append("package " + package_ + ";\n\n");
			
		String cls = 
		"public class SemanticError extends AnalysisError\n"+
		"{\n"+
		"    public SemanticError(String msg, int position)\n"+
		"	 {\n"+
		"        super(msg, position);\n"+
		"    }\n"+
		"\n"+
		"    public SemanticError(String msg)\n"+
		"    {\n"+
		"        super(msg);\n"+
		"    }\n"+
		"}\n"+
		"";
		
		result.append(cls);
		
		return result.toString();
	}
	
	private String generateConstants(FiniteAutomata fa, Grammar g, Options options) throws NotLLException
	{
		StringBuffer result = new StringBuffer();
		
		String package_ = options.pkgName;
		if (package_ != null && !package_.equals(""))
			result.append("package " + package_ + ";\n\n");
		
		String extInter = null;
		if (fa == null)
			extInter = "ParserConstants";
		else if (g == null)
			extInter = "ScannerConstants";
		else
			extInter = "ScannerConstants, ParserConstants";
		
		result.append(
		"public interface Constants extends "+extInter+"\n"+
		"{\n"+
		"    int EPSILON  = 0;\n"+
		"    int DOLLAR   = 1;\n"+
		"\n"+
		constList(fa, g)+
		"\n" );
		    	
    	result.append("}\n");
				
		return result.toString();
	}

	private String generateScannerConstants(FiniteAutomata fa, Options options)
	{
		StringBuffer result = new StringBuffer();
		
		String package_ = options.pkgName;
		if (package_ != null && !package_.equals(""))
			result.append("package " + package_ + ";\n\n");
		
		result.append(
		"public interface ScannerConstants\n"+
		"{\n");
		
		result.append(genLexTables(fa, options));
			
		result.append("}\n");
				
		return result.toString();
	}
	
	private String generateParserConstants(Grammar g, Options options) throws NotLLException
	{
		StringBuffer result = new StringBuffer();
		
		String package_ = options.pkgName;
		if (package_ != null && !package_.equals(""))
			result.append("package " + package_ + ";\n\n");
		
		result.append(
		"public interface ParserConstants\n"+
		"{\n");
		
		result.append(genSyntTables(g, options));
			
		result.append("}\n");
				
		return result.toString();
	}

	private String genLexTables(FiniteAutomata fa, Options options)
	{
		String lexTable;
		
		switch (options.scannerTable)
		{
			case FULL:
				lexTable = lex_table(fa);
				break;
			case COMPACT:
				lexTable = lex_table_compress(fa);
				break;
			case HARDCODE:
				lexTable = "";
				break;
			default:
				//nunca acontece
				lexTable = null;
				break;
		}
			
		return 
			lexTable+
			"\n"+
			token_state(fa)+
			(fa.hasContext() ? 
			"\n"+
			context(fa) : "")+
			"\n"+
			(fa.getSpecialCases().length > 0 ?
			special_cases(fa)+			
			"\n" : "")+
			scanner_error(fa)+
			"\n";
	}
	
	private String context(FiniteAutomata fa)
	{
		StringBuffer result = new StringBuffer();
		
		result.append("    int[][] SCANNER_CONTEXT =\n"+
		              "    {\n");
		
		for (int i=0; i<fa.getTransitions().size(); i++)
		{
			result.append("        {");
			result.append(fa.isContext(i)?"1":"0");
			result.append(", ");
			result.append(fa.getOrigin(i));
			result.append("},\n");
		}
		
		result.setLength(result.length()-2);
		result.append(
		"\n    };\n");
		
		return result.toString();
	}

	private String scanner_error(FiniteAutomata fa)
	{
		StringBuffer result = new StringBuffer();

		result.append(
		"    String[] SCANNER_ERROR =\n"+
		"    {\n");

		int count = fa.getTransitions().size();
		for (int i=0; i< count; i++)
		{
			result.append("        \"");
			
			String error = fa.getError(i);
			for (int j=0; j<error.length(); j++)
			{
				if (error.charAt(j) == '"')
					result.append("\\\"");
				else
					result.append(error.charAt(j));
			}
			
			result.append("\",\n");
		}
		result.setLength(result.length()-2);
		result.append(
		"\n    };\n");

		return result.toString();
	}
	
	private String genSyntTables(Grammar g, Options options) throws NotLLException
	{
		switch (options.parser)
		{
			case RD:
			case LL:
				return genLLSyntTables(g, options.parser);
			case SLR:
			case LALR:
			case LR:
				return genLRSyntTables(g);
			default:
				return null;
		}
	}
	
	private String genLRSyntTables(Grammar g)
	{
		lrTable = LRGeneratorFactory.createGenerator(g).buildIntTable();
		
		StringBuffer result = new StringBuffer(
			"    int FIRST_SEMANTIC_ACTION = "+g.FIRST_SEMANTIC_ACTION()+";\n"+
			"\n"+
			"    int SHIFT  = 0;\n"+
			"    int REDUCE = 1;\n"+
			"    int ACTION = 2;\n"+
			"    int ACCEPT = 3;\n"+
			"    int GO_TO  = 4;\n"+
			"    int ERROR  = 5;\n" );
		
		result.append("\n");
    	
		result.append(emitLRTable(g));
		
		result.append("\n");

		result.append(emitProductionsForLR(g));
		
		result.append("\n");

		result.append(emitErrorTableLR());
		
		return result.toString();
	}
	private Object emitProductionsForLR(Grammar g)
	{
		StringBuffer result = new StringBuffer();
		
		List<Production> prods = g.getProductions();
		
		result.append("    int[][] PRODUCTIONS =\n");
		result.append("    {\n");
		
		for (int i=0; i<prods.size(); i++)
		{
			result.append("        { ");
			result.append(prods.get(i).get_lhs());
			result.append(", ");
			result.append(prods.get(i).get_rhs().size());
			result.append(" },\n");
		}		
		result.setLength(result.length()-2);
		result.append("\n    };\n");
		
		return result.toString();
	}
	
	private String emitLRTable(Grammar g)
	{
		StringBuffer result = new StringBuffer();
				
		int[][][] tbl = lrTable;
		
		result.append("    int[][][] PARSER_TABLE =\n");
		result.append("    {\n");
		
		int max = tbl.length;
		if (g.getProductions().size() > max)
			max = g.getProductions().size();
			
		max = (""+max).length();
		
		for (int i=0; i< tbl.length; i++)
		{
			result.append("        {");
			for (int j=0; j<tbl[i].length; j++)
			{
				result.append(" {");
				result.append(Command.CONSTANTS[tbl[i][j][0]]);
				result.append(", ");
				String str = ""+tbl[i][j][1];
				for (int k=str.length(); k<max; k++)
					result.append(" ");
				result.append(str).append("},");
			}
			result.setLength(result.length()-1);
			result.append(" },\n");
		}	
		result.setLength(result.length()-2);
		result.append("\n    };\n");
		
		return result.toString();
	}
	
	private String genLLSyntTables(Grammar g, Options.Parser type ) throws NotLLException
	{
		StringBuffer result = new StringBuffer();
		
		if (type == LL)
		{	
			int start = g.getStartSymbol();
			int fnt = g.FIRST_NON_TERMINAL;
			int fsa = g.getSymbols().length;
			
			String syntConsts = 
			"    int START_SYMBOL = "+start+";\n"+
			"\n"+
			"    int FIRST_NON_TERMINAL    = "+fnt+";\n"+
	    	"    int FIRST_SEMANTIC_ACTION = "+fsa+";\n";
	    	
	    	result.append(syntConsts);
	    	
	    	result.append("\n");
	    	
	    	result.append(emitLLTable(new LLParser(g)));
				
			result.append("\n");
			
			result.append(emitProductionsForLL(g));
				
			result.append("\n");
				
			result.append(emitErrorTableLL(g));
			
			return result.toString();
		}
		else if (type == RD)
			return emitErrorTableLL(g);
		else
			return null;
	}
	
	private String constList(FiniteAutomata fa, Grammar g)
	{
		StringBuffer result = new StringBuffer();
		
		List<String> tokens = null;
		
		if (fa != null)
			tokens = fa.getTokens();
		else if (g != null)
			tokens = Arrays.asList(g.getTerminals());
		else
			throw new RuntimeException("Erro Interno");
		
		for (int i=0; i<tokens.size(); i++)
		{
			String t = tokens.get(i);
			if (t.charAt(0) == '\"')
				result.append("    int t_TOKEN_"+(i+2)+" = "+(i+2)+"; "+"//"+t+"\n");
			else
				result.append("    int t_"+t+" = "+(i+2)+";\n");
		}
		
		return result.toString();
	}
	
	private String lex_table_compress(FiniteAutomata fa)
	{
		StringBuffer result = new StringBuffer();
		
		List<Map<Character, Integer>> trans = fa.getTransitions();
		
		int[] sti = new int[trans.size()+1];
		int count = 0;
		for (int i=0; i<trans.size(); i++)
		{
			sti[i] = count;
			count += trans.get(i).size();
		}
		sti[sti.length-1] = count;
		
		int[][] st = new int[count][2];
		
		count = 0;
		for (int i=0; i<trans.size(); i++)
		{
			for (Map.Entry<Character, Integer> entry : trans.get(i).entrySet())
			{
				Character ch = entry.getKey();
				Integer itg =  entry.getValue(); 
				
				st[count][0] = ch.charValue();
				st[count][1] = itg.intValue();
				
				count++;
			}
		}
		
		result.append("    int[] SCANNER_TABLE_INDEXES = \n");
		result.append("    {\n");
		
		for (int i=0; i<sti.length; i++)
		{
			result.append("        ").append(sti[i]).append(",\n");
		}		
		
		result.setLength(result.length()-2);
		result.append("\n    };\n\n");	
		
		result.append("    int[][] SCANNER_TABLE = \n");
		result.append("    {\n");

		for (int i=0; i<st.length; i++)
		{
			result.append("        {").append(st[i][0]).append(", ").append(st[i][1]).append("},\n");
		}		

		result.setLength(result.length()-2);
		result.append("\n    };\n");	
		
		return result.toString();
	}
	
	private String lex_table(FiniteAutomata fa)
	{
		StringBuffer result = new StringBuffer();
		
		result.append("    int[][] SCANNER_TABLE = \n");
		result.append("    {\n");
		
		int count = fa.getTransitions().size();
		int max = String.valueOf(count).length();
		if (max == 1)
			max = 2;
			
		for (int i=0; i<count; i++)
		{
			result.append("        { ");
			for (char c = 0; c<256; c++)
			{
				String n = String.valueOf(fa.nextState(c, i));
				for (int j = n.length(); j<max; j++)
					result.append(" ");
				result.append(n).append(", ");
			}
			result.setLength(result.length()-2);
			result.append(" },\n");
		}
		result.setLength(result.length()-2);
		
		result.append("\n    };\n");
		
		return result.toString();
	}
	
	private String token_state(FiniteAutomata fa)
	{
		StringBuffer result = new StringBuffer();
		
		result.append("    int[] TOKEN_STATE = {");
		int count = fa.getTransitions().size();
		int max = String.valueOf(count).length();
		if (max == 1)
			max = 2; 
		
		for (int i=0; i<count; i++)
		{
			int fin = fa.tokenForState(i);
			String n = String.valueOf(fin);
			for (int j = n.length(); j<max; j++)
				result.append(" ");
			result.append(n).append(", ");
		}
		result.setLength(result.length()-2);		
		result.append(" };\n");
		
		return result.toString();
	}
	
	private String special_cases(FiniteAutomata fa)
	{
		int[][] indexes = fa.getSpecialCasesIndexes();
		FiniteAutomata.KeyValuePar[] sc = fa.getSpecialCases();
		
		StringBuffer result = new StringBuffer();
		
		int count = sc.length;
							
		result.append(
			"    int[] SPECIAL_CASES_INDEXES =\n"+
			"        { ");
		
		count = indexes.length;
		for (int i=0; i<count; i++)
		{
			result.append(indexes[i][0]).append(", ");
		}
		result.append(indexes[count-1][1]);
		result.append(" };\n\n");
				
		result.append(
					"    String[] SPECIAL_CASES_KEYS =\n"+
					"        {  ");
		count = sc.length;
		for (int i=0; i<count; i++)
		{
			result.append("\"").append(sc[i].key).append("\", ");
		}
		result.setLength(result.length()-2);
				
		result.append(" };\n\n");
		
		result.append(
					"    int[] SPECIAL_CASES_VALUES =\n"+
					"        {  ");
		count = sc.length;
		for (int i=0; i<count; i++)
		{
			result.append(sc[i].value).append(", ");
		}
		result.setLength(result.length()-2);
				
		result.append(" };\n");
		
		return result.toString();
	}
	
	private String emitProductionsForLL(Grammar g)
	{
		
		List<Production> pl = g.getProductions();
		String[][] productions = new String[pl.size()][];
		int max = 0;
		for (int i=0; i< pl.size(); i++)
		{
			IntList rhs = pl.get(i).get_rhs();
			if (rhs.size() > 0)
			{
				productions[i] = new String[rhs.size()];
				for (int j=0; j<rhs.size(); j++)
				{
					productions[i][j] = String.valueOf(rhs.get(j));
					if (productions[i][j].length() > max)
						max = productions[i][j].length();
				}
			}
			else
			{
				productions[i] = new String[1];
				productions[i][0] = "0";
			}
		}
		
		StringBuffer bfr = new StringBuffer();
		
		bfr.append("    int[][] PRODUCTIONS = \n");
		bfr.append("    {\n");
		
		for (int i=0; i< productions.length; i++)
		{
			bfr.append("        {");
			for (int j=0; j<productions[i].length; j++)
			{
				bfr.append(" ");
				for (int k = productions[i][j].length(); k<max; k++)
					bfr.append(" ");
				bfr.append(productions[i][j]).append(",");
			}
			bfr.setLength(bfr.length()-1);
	 		bfr.append(" },\n");
		}	
		bfr.setLength(bfr.length()-2);
		bfr.append("\n    };\n");
		
		return bfr.toString();
	}
	
	private String emitLLTable(LLParser g)
	{
		int[][] tbl = g.generateTable();
		String[][] table = new String[tbl.length][tbl[0].length];
		
		int max = 0;
		for (int i = 0; i < table.length; i++)
		{
			for (int j = 0; j < table[i].length; j++)
			{
				String tmp = String.valueOf(tbl[i][j]);
				table[i][j] = tmp;
				if (tmp.length() > max)
					max = tmp.length();
			}
		}
		
		StringBuffer bfr = new StringBuffer();
		
		bfr.append("    int[][] PARSER_TABLE =\n");
		bfr.append("    {\n");
		
		for (int i=0; i< table.length; i++)
		{
			bfr.append("        {");
			for (int j=0; j<table[i].length; j++)
			{
				bfr.append(" ");
				for (int k = table[i][j].length(); k<max; k++)
					bfr.append(" ");
				bfr.append(table[i][j]).append(",");
			}
			bfr.setLength(bfr.length()-1);
	 		bfr.append(" },\n");
		}	
		bfr.setLength(bfr.length()-2);
		bfr.append("\n    };\n");
		
		return bfr.toString();
	}
	
	private String emitErrorTableLR()
	{
		int count = lrTable.length;
		
		StringBuffer result = new StringBuffer();
	
		result.append(
		"    String[] PARSER_ERROR =\n"+
		"    {\n");
		
		for (int i=0; i< count; i++)
		{
			result.append("        \"Erro estado "+i+"\",\n");
		}
		
		result.setLength(result.length()-2);
		result.append(
		"\n    };\n");
	
		return result.toString();
	}
	
	private String emitErrorTableLL(Grammar g)
	{
		String[] symbs = g.getSymbols();
		StringBuffer result = new StringBuffer();
		
		result.append(
		"    String[] PARSER_ERROR =\n"+
		"    {\n"+
		"        \"\",\n"+
		"        \"Era esperado fim de programa\",\n");
		
		for (int i=2; i< g.FIRST_NON_TERMINAL; i++)
		{
			result.append("        \"Era esperado ");
			for (int j=0; j<symbs[i].length(); j++)
			{
				switch (symbs[i].charAt(j))
				{
					case '\"': result.append("\\\""); break;
					case '\\': result.append("\\\\"); break;
					default: result.append(symbs[i].charAt(j));				
				}
			}
			
			result.append("\",\n");
		}
					
		for (int i=g.FIRST_NON_TERMINAL; i< symbs.length; i++)
			result.append("        \""+symbs[i]+" inválido\",\n");
			
		result.setLength(result.length()-2);
		result.append(
		"\n    };\n");
		
		return result.toString();
	}
}
