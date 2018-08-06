package gesser.gals.generator.java;

import gesser.gals.generator.Options;
import gesser.gals.generator.RecursiveDescendent;
import gesser.gals.generator.parser.Grammar;
import gesser.gals.generator.parser.ll.NotLLException;
import gesser.gals.util.IntList;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static gesser.gals.generator.Options.Parser.*;

public class JavaParserGenerator
{
	public Map<String, String> generate(Grammar g, Options options) throws NotLLException
	{
		Map<String, String> result = new HashMap<String, String>();
		
		if (g != null)
		{		
			String classname = options.parserName;
			
			String parser;
			
			switch (options.parser)
			{
				case RD:
					parser = buildRecursiveDecendantParser(g, options);
					break;
				case LL:
					parser = buildLLParser(g, options);
					break;
				case SLR:
				case LALR:
				case LR:	
					parser = buildLRParser(g, options);
					break;
				default:
					parser = null;
			}
							
			result.put(classname+".java", parser);
			
			result.put(options.semanticName + ".java", generateSemanticAnalyser(options));
		}
		
		return result;
	}

	private String buildRecursiveDecendantParser(Grammar g, Options parserOptions) throws NotLLException
	{
		StringBuffer result = new StringBuffer();
	
		String package_ = parserOptions.pkgName;
	
		result.append(emitPackage(package_));
	
		result.append(emitRecursiveDecendantClass(g, parserOptions));
	
		return result.toString();
	}

	private String buildLLParser(Grammar g, Options parserOptions)
	{
		StringBuffer result = new StringBuffer();
		
		String package_ = parserOptions.pkgName;
		
		result.append(emitPackage(package_));
		
		result.append(emitImports());
		
		result.append(emitLLClass(g, parserOptions));
		
		return result.toString();
	}

	private String buildLRParser(Grammar g, Options parserOptions)
	{
		StringBuffer result = new StringBuffer();
	
		String package_ = parserOptions.pkgName;
	
		result.append(emitPackage(package_));
	
		result.append(emitImports());
	
		result.append(emitLRClass(g, parserOptions));
	
		return result.toString();
	}

	private String emitPackage(String package_)
	{
		if (package_ != null && !package_.equals(""))
			return "package " + package_ + ";\n\n";
		else
			return "";
	}

	private String emitImports()
	{
		return 
			"import java.util.Stack;\n"+
			"\n";
	}

	private String emitLRClass(Grammar g, Options parserOptions)
	{
		StringBuffer result = new StringBuffer();
	
		String classname = parserOptions.parserName;
		result.append("public class ").append(classname).append(" implements Constants\n{\n");
	
		String scannerName = parserOptions.scannerName;
		String semanName = parserOptions.semanticName;
	
		String variables = 
		"    private Stack stack = new Stack();\n"+
		"    private Token currentToken;\n"+
		"    private Token previousToken;\n"+
		"    private "+scannerName+" scanner;\n"+
		"    private "+semanName+" semanticAnalyser;\n"+
		"\n";
		
		result.append(variables);
			
		result.append(
		
		"    public void parse("+scannerName+" scanner, "+semanName+" semanticAnalyser) throws LexicalError, SyntaticError, SemanticError\n"+
		"    {\n"+
		"        this.scanner = scanner;\n"+
		"        this.semanticAnalyser = semanticAnalyser;\n"+
		"\n"+
		"        stack.clear();\n"+
		"        stack.push(new Integer(0));\n"+
		"\n"+
		"        currentToken = scanner.nextToken();\n"+
		"\n"+
		"        while ( ! step() )\n"+
		"            ;\n"+
		"    }\n"+		
		"\n"+
		"    private boolean step() throws LexicalError, SyntaticError, SemanticError\n"+
		"    {\n"+
		"        if (currentToken == null)\n"+
        "        {\n"+
		"            int pos = 0;\n"+
		"            if (previousToken != null)\n"+
		"                pos = previousToken.getPosition()+previousToken.getLexeme().length();\n"+
		"\n"+
        "            currentToken = new Token(DOLLAR, \"$\", pos);\n"+
        "        }\n"+
        "\n"+
        "        int token = currentToken.getId();\n"+
		"        int state = ((Integer)stack.peek()).intValue();\n"+
		"\n"+
        "        int[] cmd = PARSER_TABLE[state][token-1];\n"+
		"\n"+
		"        switch (cmd[0])\n"+
		"        {\n"+
		"            case SHIFT:\n"+
		"                stack.push(new Integer(cmd[1]));\n"+
		"                previousToken = currentToken;\n"+
		"                currentToken = scanner.nextToken();\n"+
		"                return false;\n"+
		"\n"+
		"            case REDUCE:\n"+
		"                int[] prod = PRODUCTIONS[cmd[1]];\n"+
		"\n"+
		"                for (int i=0; i<prod[1]; i++)\n"+
		"                    stack.pop();\n"+
		"\n"+
		"                int oldState = ((Integer)stack.peek()).intValue();\n"+
		"                stack.push(new Integer(PARSER_TABLE[oldState][prod[0]-1][1]));\n"+
		"                return false;\n"+
		"\n"+
		"            case ACTION:\n"+
		"                int action = FIRST_SEMANTIC_ACTION + cmd[1] - 1;\n"+
		"                stack.push(new Integer(PARSER_TABLE[state][action][1]));\n"+
		"                semanticAnalyser.executeAction(cmd[1], previousToken);\n"+
		"                return false;\n"+
		"\n"+
		"            case ACCEPT:\n"+
		"                return true;\n"+
		"\n"+
		"            case ERROR:\n"+
		"                throw new SyntaticError(PARSER_ERROR[state], currentToken.getPosition());\n"+
		"        }\n"+
		"        return false;\n"+
		"    }\n"+
		"\n"
	
		);
		result.append("}\n");

		return result.toString();
	}

	private String emitLLClass(Grammar g, Options parserOptions)
	{
		StringBuffer result = new StringBuffer();
		
		String classname = parserOptions.parserName;
		result.append("public class ").append(classname).append(" implements Constants\n{\n");
		
		String scannerName = parserOptions.scannerName;
		String semanName = parserOptions.semanticName;
		
		String variables = 
		"    private Stack stack = new Stack();\n"+
		"    private Token currentToken;\n"+
		"    private Token previousToken;\n"+
		"    private "+scannerName+" scanner;\n"+
		"    private "+semanName+" semanticAnalyser;\n"+
		"\n";
		
		result.append(variables);
				
		result.append(emitLLFunctions(parserOptions));
		
		result.append("}\n");

		return result.toString();
	}

	private String emitLLFunctions(Options parserOptions)
	{
		StringBuffer result = new StringBuffer();
		
		result.append(emitTesters());
		
		result.append("\n");
		
		result.append(emitStep());
		
		result.append("\n");
		
		result.append(emitDriver(parserOptions));
		
		
		return	result.toString();
	}

	private String emitTesters()
	{
		return 
		"    private static final boolean isTerminal(int x)\n"+
		"    {\n"+
		"        return x < FIRST_NON_TERMINAL;\n"+
		"    }\n"+
		"\n"+
		"    private static final boolean isNonTerminal(int x)\n"+
		"    {\n"+
		"        return x >= FIRST_NON_TERMINAL && x < FIRST_SEMANTIC_ACTION;\n"+
		"    }\n"+
		"\n"+
		"    private static final boolean isSemanticAction(int x)\n"+
		"    {\n"+
		"        return x >= FIRST_SEMANTIC_ACTION;\n"+
		"    }\n"+
		"";
	}
	
	private String emitDriver(Options parserOptions)
	{
		String scannerName = parserOptions.scannerName;
		String semanName   = parserOptions.semanticName;
				
		return 
		"    public void parse("+scannerName+" scanner, "+semanName+" semanticAnalyser) throws LexicalError, SyntaticError, SemanticError\n"+
	    "    {\n"+
		"        this.scanner = scanner;\n"+
		"        this.semanticAnalyser = semanticAnalyser;\n"+
		"\n"+
		"        stack.clear();\n"+
		"        stack.push(new Integer(DOLLAR));\n"+
		"        stack.push(new Integer(START_SYMBOL));\n"+
		"\n"+
		"        currentToken = scanner.nextToken();\n"+
		"\n"+
		"        while ( ! step() )\n"+
		"            ;\n"+
	    "    }\n"+		
		"";
	}

	private String emitStep()
	{
		return 
		"    private boolean step() throws LexicalError, SyntaticError, SemanticError\n"+
		"    {\n"+			
		"        if (currentToken == null)\n"+
        "        {\n"+
		"            int pos = 0;\n"+
		"            if (previousToken != null)\n"+
		"                pos = previousToken.getPosition()+previousToken.getLexeme().length();\n"+
		"\n"+
        "            currentToken = new Token(DOLLAR, \"$\", pos);\n"+
        "        }\n"+
        "\n"+
		"        int x = ((Integer)stack.pop()).intValue();\n"+
		"        int a = currentToken.getId();\n"+
		"\n"+
		"        if (x == EPSILON)\n"+
		"        {\n"+
		"            return false;\n"+
		"        }\n"+
		"        else if (isTerminal(x))\n"+
		"        {\n"+
		"            if (x == a)\n"+
		"            {\n"+
		"                if (stack.empty())\n"+
		"                    return true;\n"+
		"                else\n"+
		"                {\n"+
		"                    previousToken = currentToken;\n"+
		"                    currentToken = scanner.nextToken();\n"+
		"                    return false;\n"+
		"                }\n"+
		"            }\n"+
		"            else\n"+
		"            {\n"+
		"                throw new SyntaticError(PARSER_ERROR[x], currentToken.getPosition());\n"+
		"            }\n"+
		"        }\n"+
		"        else if (isNonTerminal(x))\n"+
		"        {\n"+
		"            if (pushProduction(x, a))\n"+
		"                return false;\n"+
		"            else\n"+
		"                throw new SyntaticError(PARSER_ERROR[x], currentToken.getPosition());\n"+
		"        }\n"+
		"        else // isSemanticAction(x)\n"+
		"        {\n"+
		"            semanticAnalyser.executeAction(x-FIRST_SEMANTIC_ACTION, previousToken);\n"+
		"            return false;\n"+
		"        }\n"+
		"    }\n"+
		"\n"+
		"    private boolean pushProduction(int topStack, int tokenInput)\n"+
		"    {\n"+
		"        int p = PARSER_TABLE[topStack-FIRST_NON_TERMINAL][tokenInput-1];\n"+
		"        if (p >= 0)\n"+
		"        {\n"+
		"            int[] production = PRODUCTIONS[p];\n"+
		"            //empilha a produção em ordem reversa\n"+
		"            for (int i=production.length-1; i>=0; i--)\n"+
		"            {\n"+
		"                stack.push(new Integer(production[i]));\n"+
		"            }\n"+
		"            return true;\n"+
		"        }\n"+
		"        else\n"+
		"            return false;\n"+
		"    }\n"+
		"";
	}
	
	private String emitRecursiveDecendantClass(Grammar g, Options parserOptions) throws NotLLException
	{
		RecursiveDescendent rd = new RecursiveDescendent(g);
		
		StringBuffer result = new StringBuffer();

		String classname = parserOptions.parserName;
		result.append("public class ").append(classname).append(" implements Constants\n{\n");

		String scannerName = parserOptions.scannerName;
		String semanName = parserOptions.semanticName;

		String variables = 
		"    private Token currentToken;\n"+
		"    private Token previousToken;\n"+
		"    private "+scannerName+" scanner;\n"+
		"    private "+semanName+" semanticAnalyser;\n"+
		"\n";
	
		result.append(variables);
		
		result.append(	
		"    public void parse("+scannerName+" scanner, "+semanName+" semanticAnalyser) throws AnalysisError\n"+
		"    {\n"+
		"        this.scanner = scanner;\n"+
		"        this.semanticAnalyser = semanticAnalyser;\n"+
		"\n"+
		"        currentToken = scanner.nextToken();\n"+
		"        if (currentToken == null)\n"+		"            currentToken = new Token(DOLLAR, \"$\", 0);\n"+
		"\n"+
		"        "+rd.getStart()+"();\n"+
		"\n"+
		"        if (currentToken.getId() != DOLLAR)\n"+
		"            throw new SyntaticError(PARSER_ERROR[DOLLAR], currentToken.getPosition());\n"+
		"    }\n"+		
		"\n"+
		"    private void match(int token) throws AnalysisError\n"+
		"    {\n"+
		"        if (currentToken.getId() == token)\n"+
		"        {\n"+
		"            previousToken = currentToken;\n"+
		"            currentToken = scanner.nextToken();\n"+
		"            if (currentToken == null)\n"+
		"            {\n"+
		"                int pos = 0;\n"+
		"                if (previousToken != null)\n"+
		"                    pos = previousToken.getPosition()+previousToken.getLexeme().length();\n"+
		"\n"+
		"                currentToken = new Token(DOLLAR, \"$\", pos);\n"+
		"            }\n"+
		"        }\n"+
		"        else\n"+
		"            throw new SyntaticError(PARSER_ERROR[token], currentToken.getPosition());\n"+
		"    }\n"+
		"\n");

		Map<String, RecursiveDescendent.Function> funcs = rd.build();

		for (int symb=g.FIRST_NON_TERMINAL; symb<g.FIRST_SEMANTIC_ACTION(); symb++)
		{
			String name = rd.getSymbols(symb);
			RecursiveDescendent.Function f = funcs.get(name);
			
			result.append(
						"    private void "+name+"() throws AnalysisError\n"+
						"    {\n"+
						"        switch (currentToken.getId())\n"+
						"        {\n" );
					
			List<Integer> keys = new LinkedList<Integer>(f.input.keySet());
					
			for (int i = 0; i<keys.size(); i++)
			{
				IntList rhs = f.input.get(keys.get(i));
				int token = keys.get(i).intValue();
	
				result.append(
						"            case "+token+": // "+rd.getSymbols(token)+"\n");
				for (int j=i+1; j<keys.size(); j++)
				{
					IntList rhs2 = f.input.get(keys.get(j));
					if (rhs2.equals(rhs))
					{
						token = keys.get(j).intValue();
						result.append(
						"            case "+token+": // "+rd.getSymbols(token)+"\n");
						keys.remove(j);
						j--;
					}
				}
				
				if (rhs.size() == 0)
					result.append(
						"                // EPSILON\n");	
			
				for (int k=0; k<rhs.size(); k++)
				{
					int s = rhs.get(k);
					if (g.isTerminal(s))
					{
						result.append(
						"                match("+s+"); // "+rd.getSymbols(s)+"\n");	
					}
					else if (g.isNonTerminal(s))
					{
						result.append(
						"                "+rd.getSymbols(s)+"();\n");	
					}
					else //isSemanticAction(s)
					{
						result.append(
						"                semanticAnalyser.executeAction("+(s-g.FIRST_SEMANTIC_ACTION())+", previousToken);\n");
					}
				}
			
				result.append(
						"                break;\n");
			}

			result.append(
						"            default:\n"+
						"                throw new SyntaticError(PARSER_ERROR["+f.lhs+"], currentToken.getPosition());\n"+
						"        }\n"+
						"    }\n"+
						"\n");
		}
		
		result.append("}\n");

		return result.toString();
	}
	
	private String generateSemanticAnalyser(Options options)
	{
		StringBuffer result = new StringBuffer();
		
		String package_ = options.pkgName;
		if (package_ != null && !package_.equals(""))
			result.append("package " + package_ + ";\n\n");
			
		String cls = 
		"public class "+options.semanticName+" implements Constants\n"+
		"{\n"+
		"    public void executeAction(int action, Token token)	throws SemanticError\n"+
		"    {\n"+
		"        System.out.println(\"Ação #\"+action+\", Token: \"+token);\n"+
		"    }	\n"+
		"}\n"+
		"";
		
		result.append(cls);
		
		return result.toString();
	}
}
