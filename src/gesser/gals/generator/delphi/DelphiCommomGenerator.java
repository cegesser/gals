package gesser.gals.generator.delphi;

import gesser.gals.generator.Options;
import gesser.gals.generator.OptionsDialog;
import gesser.gals.generator.parser.Grammar;
import gesser.gals.generator.parser.Production;
import gesser.gals.generator.parser.ll.LLParser;
import gesser.gals.generator.parser.ll.NotLLException;
import gesser.gals.generator.parser.lr.Command;
import gesser.gals.generator.parser.lr.LRGeneratorFactory;
import gesser.gals.generator.scanner.FiniteAutomata;
import gesser.gals.generator.scanner.FiniteAutomata.KeyValuePar;
import gesser.gals.util.IntList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gesser.gals.generator.Options.Parser.*;
import static gesser.gals.generator.Options.ScannerTable.*;

/**
 * @author Carlos E. Gesser
 */

public class DelphiCommomGenerator
{
	int[][][] lrTable = null;
	
	public Map<String, String> generate(FiniteAutomata fa, Grammar g, Options options) throws NotLLException
	{
		Map<String, String> result = new HashMap<String, String>();
				
		result.put("UToken.pas", generateToken());
		result.put("UConstants.pas", generateConstants(fa, g, options));	
		
		result.put("UAnalysisError.pas", generateAnalysisError());
		result.put("ULexicalError.pas",  generateLexicalError());
		result.put("USyntaticError.pas", generateSyntaticError());
		result.put("USemanticError.pas", generateSemanticError());	
		
		return result;
	}
			
	private String generateToken()
	{
		return 
			"unit UToken;\n"+
			"\n"+
			"interface\n"+
			"\n"+
			"uses UConstants;\n"+
			"\n"+
			"type\n"+
			"    TToken = class\n"+
			"    public\n"+
			"        constructor create(id:integer; lexeme:string; position:integer);\n"+
			"\n"+
			"        function getId : integer;\n"+
			"        function getLexeme : string;\n"+
			"        function getPosition : integer;\n"+
			"\n"+
			"    private\n"+
			"        id : integer;\n"+
			"        lexeme : string;\n"+
			"        position : integer\n"+
			"    end;\n"+
			"\n"+
			"implementation\n"+
			"\n"+
			"constructor TToken.create(id:integer; lexeme:string; position:integer);\n"+
			"begin\n"+
			"    self.id := id;\n"+
			"    self.lexeme := lexeme;\n"+
			"    self.position := position;\n"+
			"end;\n"+
			"\n"+
			"function TToken.getId : integer;\n"+
			"begin\n"+
			"    result := id;\n"+
			"end;\n"+
			"\n"+
			"function TToken.getLexeme : string;\n"+
			"begin\n"+
			"    result := lexeme;\n"+
			"end;\n"+
			"\n"+
			"function TToken.getPosition : integer;\n"+
			"begin\n"+
			"    result := position;\n"+
			"end;\n"+
			"\n"+
			"end.\n"+
			"";
	}
	
	private String generateAnalysisError()
	{
		return 
			"unit UAnalysisError;\n"+
			"\n"+
			"interface\n"+
			"\n"+
			"uses sysutils;\n"+
			"\n"+
			"type\n"+
			"    EAnalysisError = class(Exception)\n"+
			"    public\n"+
			"        constructor create(message:string; position:integer); overload;\n"+
			"        constructor create(message:string); overload;\n"+
			"\n"+
			"        function getMessage : string;\n"+
			"        function getPosition : integer;\n"+
			"\n"+
			"    private\n"+
			"        position : integer\n"+
			"    end;\n"+
			"\n"+
			"implementation\n"+
			"\n"+
			"constructor EAnalysisError.create(message:string; position:integer);\n"+
			"begin\n"+
			"    inherited create(message);\n"+
			"    self.position := position;\n"+
			"end;\n"+
			"\n"+
			"constructor EAnalysisError.create(message:string);\n"+
			"begin\n"+
			"    inherited create(message);\n"+
			"    self.position := -1;\n"+
			"end;\n"+
			"\n"+
			"function EAnalysisError.getMessage : string;\n"+
			"begin\n"+
			"    result := inherited Message;\n"+
			"end;\n"+
			"\n"+
			"function EAnalysisError.getPosition : integer;\n"+
			"begin\n"+
			"   result := position;\n"+
			"end;\n"+
			"\n"+
			"end.\n"+
			"";
	}
	
	private String generateLexicalError()
	{
		return 
			"unit ULexicalError;\n"+
			"\n"+
			"interface\n"+
			"\n"+
			"uses UAnalysisError;\n"+
			"\n"+
			"type\n"+
			"    ELexicalError = class(EAnalysisError)\n"+
			"    public\n"+
			"        constructor create(message:string; position:integer); overload;\n"+
			"        constructor create(message:string); overload;\n"+
			"    end;\n"+
			"\n"+
			"implementation\n"+
			"\n"+
			"constructor ELexicalError.create(message:string; position:integer);\n"+
			"begin\n"+
			"    inherited create(message, position);\n"+
			"end;\n"+
			"\n"+
			"constructor ELexicalError.create(message:string);\n"+
			"begin\n"+
			"    inherited create(message);\n"+
			"end;\n"+
			"\n"+
			"end.\n"+
			"";
	}
	
	private String generateSyntaticError()
	{
		return 
			"unit USyntaticError;\n"+
			"\n"+
			"interface\n"+
			"\n"+
			"uses UAnalysisError;\n"+
			"\n"+
			"type\n"+
			"    ESyntaticError = class(EAnalysisError)\n"+
			"    public\n"+
			"        constructor create(message:string; position:integer); overload;\n"+
			"        constructor create(message:string); overload;\n"+
			"    end;\n"+
			"\n"+
			"implementation\n"+
			"\n"+
			"constructor ESyntaticError.create(message:string; position:integer);\n"+
			"begin\n"+
			"    inherited create(message, position);\n"+
			"end;\n"+
			"\n"+
			"constructor ESyntaticError.Create(message:string);\n"+
			"begin\n"+
			"    inherited create(message);\n"+
			"end;\n"+
			"\n"+
			"end.\n"+
			"";
	}
	
	private String generateSemanticError()
	{
		return 
			"unit USemanticError;\n"+
			"\n"+
			"interface\n"+
			"\n"+
			"uses UAnalysisError;\n"+
			"\n"+
			"type\n"+
			"    ESemanticError = class(EAnalysisError)\n"+
			"    public\n"+
			"        constructor create(message:string; position:integer); overload;\n"+
			"        constructor create(message:string); overload;\n"+
			"    end;\n"+
			"\n"+
			"implementation\n"+
			"\n"+
			"constructor ESemanticError.Create(message:string; position:integer);\n"+
			"begin\n"+
			"    inherited create(message, position);\n"+
			"end;\n"+
			"\n"+
			"constructor ESemanticError.Create(message:string);\n"+
			"begin\n"+
			"    inherited create(message);\n"+
			"end;\n"+
			"\n"+
			"end.\n"+
			"";
	}
	
	private String generateConstants(FiniteAutomata fa, Grammar g, Options options) throws NotLLException
	{
		return 
			"unit UConstants;\n"+
			"\n"+
			"interface\n"+
			"\n"+
			"const\n"+
			"\n"+
			constants(fa, g)+
			lexTables(fa, options)+
			syntTables(g)+
			"implementation\n"+
			"\n"+
			"end.\n"+
			"";
	}
			
	private String constants(FiniteAutomata fa, Grammar g)
	{
		StringBuffer result = new StringBuffer();
		
		List<String> tokens = null;
		
		if (fa != null)
			tokens = fa.getTokens();
		else if (g != null)
			tokens = Arrays.asList(g.getTerminals());
		else
			throw new RuntimeException("Erro Interno");
		
		result.append(
			"    EPSILON = 0;\n"+
			"    DOLLAR  = 1;\n"+
			"\n"
		);
		
		for (int i=0; i<tokens.size(); i++)
		{
			String t = tokens.get(i);
			if (t.charAt(0) == '\"')
				result.append("    t_TOKEN_"+(i+2)+" = "+(i+2)+"; "+"//"+t+"\n");
			else
				result.append("    t_"+t+" = "+(i+2)+";\n");
		}
		
		result.append("\n");
		
		return result.toString();
	}
	
	private String lexTables(FiniteAutomata fa, Options options)
	{
		if (fa == null)
			return "";
			
		return 
			"    STATES_COUNT = "+fa.getTransitions().size()+";\n"+
			"\n"+
			mainLex(fa, options)+
			context(fa)+			
			(fa.getSpecialCases().length>0 ?
				lookup(fa) : "")+
			scanner_error(fa)+
			"";
	}
	
	private String context(FiniteAutomata fa)
	{
		if (!fa.hasContext())
			return "";
			
		StringBuffer result = new StringBuffer();
		
		result.append("    SCANNER_CONTEXT : array[0..STATES_COUNT-1][0..1] of integer =\n"+
					  "    (\n");

		for (int i=0; i<fa.getTransitions().size(); i++)
		{
			result.append("        (");
			result.append(fa.isContext(i)?"1":"0");
			result.append(", ");
			result.append(fa.getOrigin(i));
			result.append("),\n");
		}

		result.setLength(result.length()-2);
		result.append(
		"\n    );\n");

		return result.toString();
	}

	private String scanner_error(FiniteAutomata fa)
	{
		StringBuffer result = new StringBuffer();

		result.append(
		"    SCANNER_ERROR : array[0..STATES_COUNT-1] of string =\n"+
		"    (\n");

		int count = fa.getTransitions().size();
		for (int i=0; i< count; i++)
		{
			result.append("        '");
			
			String error = fa.getError(i);
			for (int j=0; j<error.length(); j++)
			{
				if (error.charAt(j) == '\'')
					result.append("''");
				else
					result.append(error.charAt(j));
			}
			
			result.append("',\n");
		}
		result.setLength(result.length()-2);
		result.append(
		"\n    );\n");

		return result.toString();
	}

	private String mainLex(FiniteAutomata fa, Options options)
	{
		StringBuffer result = new StringBuffer();
		
		int count;
		int max;
		
		result.append(scannerTable(fa, options));
		
		
		
		result.append("    TOKEN_STATE : array[0..STATES_COUNT-1] of integer =\n        ( ");
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
		result.append(" );\n\n");
		
		return result.toString();
	}

	private String scannerTable(FiniteAutomata fa, Options options)
	{
		if (options.scannerTable == HARDCODE)
				return "";
					
		StringBuffer result = new StringBuffer();
		
		result.append(
			"    SCANNER_TABLE : array[0..STATES_COUNT-1, char] of integer =\n"+
			"    ( \n"
		);
		
		int count = fa.getTransitions().size();
		int max = String.valueOf(count).length();
		if (max == 1)
			max = 2;
			
		for (int i=0; i<count; i++)
		{
			result.append("        ( ");
			for (char c = 0; c<256; c++)
			{
				String n = String.valueOf(fa.nextState(c, i));
				for (int j = n.length(); j<max; j++)
					result.append(" ");
				result.append(n).append(", ");
				
				if (c == 200)
					result.append("\n          ");
			}
			result.setLength(result.length()-2);
			result.append(" ),\n");
		}
		result.setLength(result.length()-2);
		
		result.append(
			"\n    );\n\n"
		);
		
		return result.toString();
	}
	
	private String lookup(FiniteAutomata fa)
	{
		StringBuffer result = new StringBuffer();
		
		int indexes[][] = fa.getSpecialCasesIndexes();
		
		result.append(
			"    SPECIAL_CASES_INDEXES : array[0.."+indexes.length+"] of integer =\n"+
			"        ( "
		);
			
		int count = indexes.length;
		
		for (int i=0; i<indexes.length; i++)
		{
			result.append(indexes[i][0]);
			result.append(", ");
		}
		result.append(indexes[count-1][1]);
		result.append(" );\n\n");
		
		KeyValuePar[] sc = fa.getSpecialCases();
		count = sc.length;
		
		result.append(
			"    SPECIAL_CASES_KEYS : array[0.."+(count-1)+"] of string =\n"+
			"        (  "
		);
				
		for (int i=0; i<count; i++)
		{
			result.append("'");
			result.append(sc[i].key);
			result.append("', ");
		}
		result.setLength(result.length()-2);
		result.append(" );\n\n");
		
		result.append(
			"    SPECIAL_CASES_VALUES : array[0.."+(count-1)+"] of integer =\n"+
			"        (  "
		);
				
		for (int i=0; i<count; i++)
		{
			result.append(sc[i].value);
			result.append(", ");
		}
		result.setLength(result.length()-2);
		result.append(" );\n\n");
		
		return result.toString();
	}
	
	private String syntTables(Grammar g) throws NotLLException
	{
		if (g == null)
			return "";
		
		switch (OptionsDialog.getInstance().getOptions().parser)
		{
			case RD:
				return 
					errorLL(g);
			case LL:	
				return 
					"    START_SYMBOL = "+g.getStartSymbol()+";\n"+
					"\n"+
					"    FIRST_NON_TERMINAL    = "+g.FIRST_NON_TERMINAL+";\n"+
					"    FIRST_SEMANTIC_ACTION = "+g.FIRST_SEMANTIC_ACTION()+";\n"+
					"\n"+
					transTablesLL(new LLParser(g))+
					prodsLL(g)+
					errorLL(g)+
					"";
			case SLR:
			case LALR:
			case LR:
				return 
					"    FIRST_SEMANTIC_ACTION = "+g.FIRST_SEMANTIC_ACTION()+";\n"+
					"\n"+		
					"    SHIFT  = 0;\n"+
					"    REDUCE = 1;\n"+
					"    ACTION = 2;\n"+
					"    ACCEPT = 3;\n"+
					"    GO_TO  = 4;\n"+
					"    ERROR  = 5;\n"+
					"\n"+
					transTablesLR(g)+
					"\n"+
					prodsLR(g)+
					"\n"+
					errorLR(g);
			default:
				return null;
		}
	}

	private String transTablesLR(Grammar g)
	{
		lrTable = LRGeneratorFactory.createGenerator(g).buildIntTable();
		
		StringBuffer result = new StringBuffer();
				
		result.append("    PARSER_TABLE : array[0.."+(lrTable.length-1)+", 0.."+(lrTable[0].length-1)+", 0..1] of integer =\n");
		result.append("    (\n");

		int max = lrTable.length;
		if (g.getProductions().size() > max)
			max = g.getProductions().size();
	
		max = (""+max).length();

		for (int i=0; i< lrTable.length; i++)
		{
			result.append("        (");
			for (int j=0; j<lrTable[i].length; j++)
			{
				result.append(" (");
				result.append(Command.CONSTANTS[lrTable[i][j][0]]);
				result.append(", ");
				String str = ""+lrTable[i][j][1];
				for (int k=str.length(); k<max; k++)
					result.append(" ");
				result.append(str).append("),");
			}
			result.setLength(result.length()-1);
			result.append(" ),\n");
		}	
		result.setLength(result.length()-2);
		result.append("\n    );\n");

		return result.toString();
	}
	
	private String prodsLR(Grammar g)
	{
		StringBuffer result = new StringBuffer();

		List<Production> prods = g.getProductions();

		result.append("    PRODUCTIONS : array[0.."+(prods.size()-1)+", 0..1] of Integer =\n");
		result.append("    (\n");

		for (int i=0; i<prods.size(); i++)
		{
			result.append("        ( ");
			result.append(prods.get(i).get_lhs());
			result.append(", ");
			result.append(prods.get(i).get_rhs().size());
			result.append(" ),\n");
		}		
		result.setLength(result.length()-2);
		result.append("\n    );\n");

		return result.toString();
	}

	private String transTablesLL(LLParser g)
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
		bfr.append("    PARSER_TABLE : array[0.."+(table.length-1)+", 0.."+(table[0].length-1)+"] of integer =\n");
		bfr.append("    (\n");
		
		for (int i=0; i< table.length; i++)
		{
			bfr.append("        (");
			for (int j=0; j<table[i].length; j++)
			{
				bfr.append(" ");
				for (int k = table[i][j].length(); k<max; k++)
					bfr.append(" ");
				bfr.append(table[i][j]).append(",");
			}
			bfr.setLength(bfr.length()-1);
	 		bfr.append(" ),\n");
		}	
		bfr.setLength(bfr.length()-2);
		bfr.append("\n    );\n\n");
		
		return bfr.toString();
	}
	
	private String prodsLL(Grammar g)
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
		
		bfr.append("    PRODUCTIONS : array[0.."+(pl.size()-1)+", 0.."+longest+"] of integer =\n");
		bfr.append("    (\n");
		
		for (int i=0; i< productions.length; i++)
		{
			bfr.append("        (");
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
	 		bfr.append(" ),\n");
		}	
		bfr.setLength(bfr.length()-2);
		bfr.append("\n    );\n\n");
		
		return bfr.toString();
	}
	
	private String errorLL(Grammar g)
	{
		String[] symbs = g.getSymbols();
		StringBuffer result = new StringBuffer();
		
		result.append(
		"    PARSER_ERROR : array [0.."+(g.getSymbols().length-1)+"] of string =\n"+
		"    (\n"+
		"        '',\n"+
		"        'Era esperado fim de programa',\n");
		
		for (int i=2; i< g.FIRST_NON_TERMINAL; i++)
		{
			result.append("        'Era esperado ");
			for (int j=0; j<symbs[i].length(); j++)
			{
				switch (symbs[i].charAt(j))
				{
					case '\'': result.append("''"); break;
					default: result.append(symbs[i].charAt(j));				
				}
			}
			
			result.append("',\n");
		}
					
		for (int i=g.FIRST_NON_TERMINAL; i< symbs.length; i++)
			result.append("        '"+symbs[i]+" inválido',\n");
			
		result.setLength(result.length()-2);
		result.append(
		"\n    );\n\n");
		
		return result.toString();
	}
	
	private String errorLR(Grammar g)
	{
		StringBuffer result = new StringBuffer();
	
		result.append(
		"    PARSER_ERROR : array [0.."+(lrTable.length-1)+"] of string =\n"+
		"    (\n");
		
		for (int i=0; i< lrTable.length; i++)
		{
			result.append("        'Erro estado "+i+"',\n");
		}
				
		result.setLength(result.length()-2);
		result.append(
		"\n    );\n\n");
	
		return result.toString();
	}
}
