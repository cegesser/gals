package gesser.gals.generator.cpp;

import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import gesser.gals.generator.Options;
import gesser.gals.generator.parser.Grammar;
import gesser.gals.generator.parser.Production;
import gesser.gals.generator.parser.ll.LLParser;
import gesser.gals.generator.parser.ll.NotLLException;
import gesser.gals.generator.parser.lr.Command;
import gesser.gals.generator.parser.lr.LRGeneratorFactory;
import gesser.gals.generator.scanner.FiniteAutomata;
import gesser.gals.util.IntList;

import static gesser.gals.generator.Options.Parser.*;
import static gesser.gals.generator.Options.ScannerTable.*;
/**
 * @author Gesser
 */

public class CppCommomGenerator
{
	int[][][] lrTable = null;
	
	public Map<String, String> generate(FiniteAutomata fa, Grammar g, Options options) throws NotLLException
	{
		Map<String, String> result = new HashMap<String, String>();
				
		result.put("Token.h", generateToken(options));
		result.put("Constants.h", generateConstantsH(fa, g, options));
		result.put("Constants.cpp", generateConstantsCpp(fa, g, options));
				
		result.put("AnalysisError.h", generateAnalysisError(options));
		result.put("LexicalError.h", generateLexicalError(options));
		result.put("SyntaticError.h", generateSyntaticError(options));
		result.put("SemanticError.h", generateSemanticError(options));	
		
		return result;
	}
	
	private String openNamespace(Options options)
	{
		String namespace = options.pkgName;
		
		if (namespace != null && !namespace.equals(""))
			return "namespace "+namespace+" {\n\n";
		else
			return "";
	}
	
	private String closeNamespace(Options options)
	{
		String namespace = options.pkgName;
		
		if (namespace != null && !namespace.equals(""))
			return "} //namespace "+namespace+"\n\n";
		else
			return "";
	}

	private String generateToken(Options options)
	{	
		return 
			"#ifndef TOKEN_H\n"+
			"#define TOKEN_H\n"+
			"\n"+
			"#include \"Constants.h\"\n"+
			"\n"+
			"#include <string>\n"+
			"\n"+
			openNamespace(options)+
			"class Token\n"+
			"{\n"+
			"public:\n"+
			"    Token(TokenId id, const std::string &lexeme, int position)\n"+
			"      : id(id), lexeme(lexeme), position(position) { }\n"+
			"\n"+
			"    TokenId getId() const { return id; }\n"+
			"    const std::string &getLexeme() const { return lexeme; }\n"+
			"    int getPosition() const { return position; }\n"+
			"\n"+
			"private:\n"+
			"    TokenId id;\n"+
			"    std::string lexeme;\n"+
			"    int position;\n"+
			"};\n"+
			"\n"+
			closeNamespace(options)+
			"#endif\n"+
			"";
	}

	private String generateAnalysisError(Options options)
	{
		return 
			"#ifndef ANALYSIS_ERROR_H\n"+
			"#define ANALYSIS_ERROR_H\n"+
			"\n"+
			"#include <string>\n"+
			"\n"+
			openNamespace(options)+
			"class AnalysisError\n"+
			"{\n"+
			"public:\n"+
			"\n"+
			"    AnalysisError(const std::string &msg, int position = -1)\n"+
			"      : message(msg), position(position) { }\n"+
			"\n"+
			"    const char *getMessage() const { return message.c_str(); }\n"+
			"    int getPosition() const { return position; }\n"+
			"\n"+
			"private:\n"+
			"    std::string message;\n"+
			"    int position;\n"+
			"};\n"+
			"\n"+
			closeNamespace(options)+
			"#endif\n"+			
			"";
	}
	
	private String generateLexicalError(Options options)
	{
		return 
			"#ifndef LEXICAL_ERROR_H\n"+
			"#define LEXICAL_ERROR_H\n"+
			"\n"+
			"#include \"AnalysisError.h\"\n"+
			"\n"+
			"#include <string>\n"+
			"\n"+
			openNamespace(options)+
			"class LexicalError : public AnalysisError\n"+
			"{\n"+
			"public:\n"+
			"\n"+
			"    LexicalError(const std::string &msg, int position = -1)\n"+
			"      : AnalysisError(msg, position) { }\n"+
			"};\n"+
			"\n"+
			closeNamespace(options)+
			"#endif\n"+
			"";
	}
	
	private String generateSyntaticError(Options options)
	{
		return 
			"#ifndef SYNTATIC_ERROR_H\n"+
			"#define SYNTATIC_ERROR_H\n"+
			"\n"+
			"#include \"AnalysisError.h\"\n"+
			"\n"+
			"#include <string>\n"+
			"\n"+
			openNamespace(options)+
			"class SyntaticError : public AnalysisError\n"+
			"{\n"+
			"public:\n"+
			"\n"+
			"    SyntaticError(const std::string &msg, int position = -1)\n"+
			"      : AnalysisError(msg, position) { }\n"+
			"};\n"+
			"\n"+
			closeNamespace(options)+
			"#endif\n"+
			"";
	}
	
	private String generateSemanticError(Options options)
	{
		return 
			"#ifndef SEMANTIC_ERROR_H\n"+
			"#define SEMANTIC_ERROR_H\n"+
			"\n"+
			"#include \"AnalysisError.h\"\n"+
			"\n"+
			"#include <string>\n"+
			"\n"+
			openNamespace(options)+
			"class SemanticError : public AnalysisError\n"+
			"{\n"+
			"public:\n"+
			"\n"+
			"    SemanticError(const std::string &msg, int position = -1)\n"+
			"      : AnalysisError(msg, position) { }\n"+
			"};\n"+
			"\n"+
			closeNamespace(options)+
			"#endif\n"+
			"";
	}
	
	private String generateConstantsH(FiniteAutomata fa, Grammar g, Options options)
	{
		return 
			"#ifndef CONSTANTS_H\n"+
			"#define CONSTANTS_H\n"+
			"\n"+
			openNamespace(options)+
			"enum TokenId \n"+
			"{\n"+
			"    EPSILON  = 0,\n"+
			"    DOLLAR   = 1,\n"+
			constList(fa, g)+
			"};\n"+
			"\n"+
			lexDecls(fa, options)+			
			syntDecls(g, options)+
			closeNamespace(options)+
			"#endif\n"+
			"";
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
				result.append("    t_TOKEN_"+(i+2)+" = "+(i+2)+", "+"//"+t+"\n");
			else
				result.append("    t_"+t+" = "+(i+2)+",\n");
		}
		
		result.setLength(result.length()-2);
		result.append("\n");
		
		return result.toString();
	}

	private String lexDecls(FiniteAutomata fa, Options options)
	{
		if (fa == null)
			return "";
			
		return 
			"const int STATES_COUNT = "+fa.getTransitions().size()+";\n"+
			(options.scannerTable == HARDCODE ? "" :
			"\n"+
			"extern int SCANNER_TABLE[STATES_COUNT][256];\n")+
			"\n"+
			"extern int TOKEN_STATE[STATES_COUNT];\n"+
			"\n"+	
			(fa.hasContext() ? 
			"extern int SCANNER_CONTEXT[STATES_COUNT][2];\n"+
			"\n" : ""
			)+		
			(fa.getSpecialCases().length > 0 ?
			"extern int SPECIAL_CASES_INDEXES["+(fa.getSpecialCasesIndexes().length+1)+"];\n"+
			"\n"+
			"extern const char *SPECIAL_CASES_KEYS["+fa.getSpecialCases().length+"];\n"+
			"\n"+
			"extern int SPECIAL_CASES_VALUES["+fa.getSpecialCases().length+"];\n"+
			"\n" : "")+
			"extern const char *SCANNER_ERROR[STATES_COUNT];\n"+
			"\n";
	}
	
	private String syntDecls(Grammar g, Options options)	
	{
		if (g == null)
			return "";
			
		
		switch (options.parser)
		{
			case RD:
			{
				int numNT = g.FIRST_SEMANTIC_ACTION()-g.FIRST_NON_TERMINAL;
				
				return 
					"extern const char *PARSER_ERROR["+(g.FIRST_NON_TERMINAL+numNT)+"];\n"+
					"\n";
			}
			case LL:
			{		
				int maxProd = 0;
				for (int i=0; i<g.getProductions().size(); i++)
				{
					int size = g.getProductions().get(i).get_rhs().size();
					if (size > maxProd)
						maxProd = size;
				}
				
				int numNT = g.FIRST_SEMANTIC_ACTION()-g.FIRST_NON_TERMINAL;
					
				return 
					"const int START_SYMBOL = "+g.getStartSymbol()+";\n"+
					"\n"+
					"const int FIRST_NON_TERMINAL    = "+g.FIRST_NON_TERMINAL+";\n"+
					"const int FIRST_SEMANTIC_ACTION = "+g.FIRST_SEMANTIC_ACTION()+";\n"+
					"\n"+
					"extern int PARSER_TABLE["+numNT+"]["+(g.FIRST_NON_TERMINAL-1)+"];\n"+
					"\n"+
					"extern int PRODUCTIONS["+g.getProductions().size()+"]["+(maxProd+1)+"];\n"+
					"\n"+
					"extern const char *PARSER_ERROR["+(g.FIRST_NON_TERMINAL+numNT)+"];\n"+
					"\n";
			}
			default: //SLR, LALR, LR
			{
				lrTable = LRGeneratorFactory.createGenerator(g).buildIntTable();
				return 
					"const int FIRST_SEMANTIC_ACTION = "+g.FIRST_SEMANTIC_ACTION()+";\n"+
					"\n"+
					"const int SHIFT  = 0;\n"+
					"const int REDUCE = 1;\n"+
					"const int ACTION = 2;\n"+
					"const int ACCEPT = 3;\n"+
					"const int GO_TO  = 4;\n"+
					"const int ERROR  = 5;\n"+
					"\n"+
					"extern const int PARSER_TABLE["+lrTable.length+"]["+lrTable[0].length+"][2];\n"+
					"\n"+
					"extern const int PRODUCTIONS["+g.getProductions().size()+"][2];\n"+
					"\n"+
					"extern const char *PARSER_ERROR["+lrTable.length+"];\n"+
					"\n";
			}
		}
	}
	
	private String generateConstantsCpp(
		FiniteAutomata fa,
		Grammar g,
		Options options) throws NotLLException
	{
		return 
			"#include \"Constants.h\"\n"+
			"\n"+
			openNamespace(options)+
			lexTables(fa, options)+
			syntTables(g, options)+
			closeNamespace(options)+
			"";
	}

	private String lexTables(FiniteAutomata fa, Options options)
	{
		if (fa == null)
			return "";
			
		int count;
		int max;
		StringBuffer result = new StringBuffer();
		
		result.append(scannerTable(fa, options)).append("\n");
		
		result.append("int TOKEN_STATE[STATES_COUNT] = {");
		count = fa.getTransitions().size();
		max = String.valueOf(count).length();
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
		result.append(" };\n\n");
		
		result.append(context(fa));
		
		result.append(specialCases(fa));
		
		result.append(
		"const char *SCANNER_ERROR[STATES_COUNT] =\n"+
		"{\n");

		count = fa.getTransitions().size();
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
		"\n};\n\n");
		
		return result.toString();
	}

	private Object context(FiniteAutomata fa)
	{
		if (! fa.hasContext())
			return "";
		
		StringBuffer result = new StringBuffer();
		
		result.append("int SCANNER_CONTEXT[STATES_COUNT][2] =\n"+
					  "{\n");
		
		for (int i=0; i<fa.getTransitions().size(); i++)
		{
			result.append("    {");
			result.append(fa.isContext(i)?"1":"0");
			result.append(", ");
			result.append(fa.getOrigin(i));
			result.append("},\n");
		}
		
		result.setLength(result.length()-2);
		result.append(
		"\n};\n\n");
		
		return result.toString();
	}

	private String scannerTable(FiniteAutomata fa, Options options)
	{
		if (options.scannerTable == HARDCODE)
			return "";
		
		StringBuffer result = new StringBuffer();
		
		result.append("int SCANNER_TABLE[STATES_COUNT][256] = \n");
		result.append("{\n");
		
		int count = fa.getTransitions().size();
		int max = String.valueOf(count).length();
		if (max == 1)
			max = 2;
			
		for (int i=0; i<count; i++)
		{
			result.append("    { ");
			for (char c = 0; c<256; c++)
			{
				String n = String.valueOf(fa.nextState(c, i));
				for (int j = n.length(); j<max; j++)
					result.append(" ");					
				result.append(n).append(", ");
				if (c == 200)
					result.append("\n      ");
			}
			result.setLength(result.length()-2);
			result.append(" },\n");
		}
		result.setLength(result.length()-2);
		
		result.append("\n};\n");
		
		return result.toString();
	}
	
	private String specialCases(FiniteAutomata fa)
	{			
		if (fa.getSpecialCases().length > 0)
		{
			int[][] indexes = fa.getSpecialCasesIndexes();
			FiniteAutomata.KeyValuePar[] sc = fa.getSpecialCases();
	
			StringBuffer result = new StringBuffer();
	
			int count = sc.length;
	
			result.append(
				"int SPECIAL_CASES_INDEXES["+(indexes.length+1)+"] =\n"+
				"    { ");
	
			count = indexes.length;
			for (int i=0; i<count; i++)
			{
				result.append(indexes[i][0]).append(", ");
			}
			result.append(indexes[count-1][1]);
			result.setLength(result.length()-2);
			result.append(" };\n\n");
	
			count = sc.length;
			result.append(
						"const char *SPECIAL_CASES_KEYS["+count+"] =\n"+
						"    { ");
						
			count = sc.length;
			for (int i=0; i<count; i++)
			{
				result.append("\"").append(sc[i].key).append("\", ");
			}
			result.setLength(result.length()-2);
		
			result.append(" };\n\n");
			
			result.append(
				"int SPECIAL_CASES_VALUES["+count+"] =\n"+
				"    { ");	
	
			for (int i=0; i<count; i++)
			{
				result.append(sc[i].value).append(", ");
			}
			result.setLength(result.length()-2);
		
			result.append(" };\n\n");
	
			return result.toString();		
		}
		else
			return "";
	}

	private String syntTables(Grammar g, Options options) throws NotLLException
	{
		if (g == null)
			return "";
			
		switch (options.parser)
		{
			case RD:
				return
					syntErrorsLL(g);	
			case LL:
				return
					syntTransTable(new LLParser(g))+
					productionsLL(g)+
					syntErrorsLL(g);	
			default: //slr, lalr, lr
				return syntTransTable(g)+
					productionsLR(g)+
					syntErrorsLR(g);
		}
	}

	private String productionsLR(Grammar g)
	{
		StringBuffer result = new StringBuffer();
		
		List<Production> prods = g.getProductions();

		result.append("const int PRODUCTIONS["+prods.size()+"][2] =\n");
		result.append("{\n");

		for (int i=0; i<prods.size(); i++)
		{
			result.append("    { ");
			result.append(prods.get(i).get_lhs());
			result.append(", ");
			result.append(prods.get(i).get_rhs().size());
			result.append(" },\n");
		}		
		result.setLength(result.length()-2);
		result.append("\n};\n");

		return result.toString();
	}
		
	private String syntTransTable(Grammar g)
	{
		StringBuffer result = new StringBuffer();

		result.append("const int PARSER_TABLE["+lrTable.length+"]["+lrTable[0].length+"][2] =\n");
		result.append("{\n");

		int max = lrTable.length;
		if (g.getProductions().size() > max)
			max = g.getProductions().size();
	
		max = (""+max).length();

		for (int i=0; i< lrTable.length; i++)
		{
			result.append("    {");
			for (int j=0; j<lrTable[i].length; j++)
			{
				result.append(" {");
				result.append(Command.CONSTANTS[lrTable[i][j][0]]);
				result.append(", ");
				String str = ""+lrTable[i][j][1];
				for (int k=str.length(); k<max; k++)
					result.append(" ");
				result.append(str).append("},");
			}
			result.setLength(result.length()-1);
			result.append(" },\n");
		}	
		result.setLength(result.length()-2);
		result.append("\n};\n");

		return result.toString();
	}

	private String syntTransTable(LLParser g)
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
		
		bfr.append("int PARSER_TABLE["+table.length+"]["+table[0].length+"] =\n");
		bfr.append("{\n");
		
		for (int i=0; i< table.length; i++)
		{
			bfr.append("    {");
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
		bfr.append("\n};\n\n");
		
		return bfr.toString();
	}

	private String productionsLL(Grammar g)
	{
		List<Production> pl = g.getProductions();
		String[][] productions = new String[pl.size()][];
		int max = 0;
		int longest = 0;
		for (int i=0; i< pl.size(); i++)
		{
			IntList rhs = pl.get(i).get_rhs();
			if (rhs.size() > longest)
				longest = rhs.size();
			if (rhs.size() > 0)
			{
				productions[i] = new String[rhs.size()+1];
				productions[i][0] = String.valueOf(rhs.size());
				for (int j=0; j<rhs.size(); j++)
				{
					productions[i][j+1] = String.valueOf(rhs.get(j));
					if (productions[i][j+1].length() > max)
						max = productions[i][j+1].length();
				}
			}
			else
			{
				productions[i] = new String[2];
				productions[i][0] = "1";
				productions[i][1] = "0";
			}
		}
		
		StringBuffer bfr = new StringBuffer();
		
		bfr.append("int PRODUCTIONS["+pl.size()+"]["+(longest+1)+"] = \n");
		bfr.append("{\n");
		
		for (int i=0; i< productions.length; i++)
		{
			bfr.append("    {");
			for (int j=0; j<productions[i].length; j++)
			{
				bfr.append(" ");
				for (int k = productions[i][j].length(); k<max; k++)
					bfr.append(" ");
				bfr.append(productions[i][j]).append(",");
			}
			for (int j=productions[i].length; j<=longest; j++)
			{
				bfr.append(" ");
				for (int k = 1; k<max; k++)
					bfr.append(" ");
				bfr.append("0").append(",");
			}
			bfr.setLength(bfr.length()-1);
	 		bfr.append(" },\n");
		}	
		bfr.setLength(bfr.length()-2);
		bfr.append("\n};\n\n");
		
		return bfr.toString();
	}


	private String syntErrorsLL(Grammar g)
	{
		String[] symbs = g.getSymbols();
		StringBuffer result = new StringBuffer();
		
		result.append(
		"const char *PARSER_ERROR["+g.FIRST_SEMANTIC_ACTION()+"] =\n"+
		"{\n"+
		"    \"\",\n"+
		"    \"Era esperado fim de programa\",\n");
		
		for (int i=2; i< g.FIRST_NON_TERMINAL; i++)
		{
			result.append("    \"Era esperado ");
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
			result.append("    \""+symbs[i]+" inválido\",\n");
			
		result.setLength(result.length()-2);
		result.append(
		"\n};\n\n");
		
		return result.toString();
	}

	private String syntErrorsLR(Grammar g)
	{
		StringBuffer result = new StringBuffer();
	
		result.append(
		"const char *PARSER_ERROR["+lrTable.length+"] =\n"+
		"{\n");
	
			for (int i=0; i< lrTable.length; i++)
				result.append("    \"Erro estado "+i+"\",\n");
		
		result.setLength(result.length()-2);
		result.append(
		"\n};\n\n");
	
		return result.toString();
	}
}
