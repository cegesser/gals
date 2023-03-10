package gesser.gals.generator.csharp;

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
/**
 * 
 * @author Gustavo
 * @see gesser.gals.generator.java.JavaParserGenerator
 */
public class CSharpParserGenerator
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
							
			result.put(classname+".cs", parser);
			
			result.put(options.semanticName + ".cs", generateSemanticAnalyser(options));
		}
		
		return result;
	}

	private String buildRecursiveDecendantParser(Grammar g, Options parserOptions) throws NotLLException
	{
		StringBuffer result = new StringBuffer();
		result.append(emitRecursiveDecendantClass(g, parserOptions));
		CSharpCommonGenerator.colocarNamespace(result, parserOptions);
		result.insert(0, CSharpCommonGenerator.emitStaticImport(parserOptions, "Constants"));
		
		return result.toString();
	}

	private String buildLLParser(Grammar g, Options parserOptions)
	{
		StringBuffer result = new StringBuffer();
		result.append(emitLLClass(g, parserOptions));
		CSharpCommonGenerator.colocarNamespace(result, parserOptions);
		result.insert(0, emitImports(parserOptions));
		
		return result.toString();
	}

	private String buildLRParser(Grammar g, Options parserOptions)
	{
		StringBuffer result = new StringBuffer();
		result.append(emitLRClass(g, parserOptions));
		CSharpCommonGenerator.colocarNamespace(result, parserOptions);
		result.insert(0, emitImports(parserOptions));
		
		return result.toString();
	}
	private String emitImports(Options parserOptions)
	{
		return  "using System.Collections;\n"+
				CSharpCommonGenerator.emitStaticImport(parserOptions, "Constants")+
				CSharpCommonGenerator.emitStaticImport(parserOptions, "ParserConstants")+
				"\n";
	}

	private String emitLRClass(Grammar g, Options parserOptions)
	{
		StringBuffer result = new StringBuffer();
	
		String classname = parserOptions.parserName;
		result.append("    public sealed class ").append(classname).append(" \n    {\n");
	
		String scannerName = parserOptions.scannerName;
		String semanName = parserOptions.semanticName;
	
		String variables = 
		"        private readonly Stack _stack = new Stack();\n"+
		"        private Token _currentToken;\n"+
		"        private Token _previousToken;\n"+
		"        private "+scannerName+" _scanner;\n"+
		"        private "+semanName+" _semanticAnalyser;\n"+
		"\n";
		
		result.append(variables);
			
		result.append(
		
		"        public void Parse("+scannerName+" scanner, "+semanName+" semanticAnalyser)\n"+
		"        {\n"+
		"            _scanner = scanner;\n"+
		"            _semanticAnalyser = semanticAnalyser;\n"+
		"\n"+
		"            _stack.Clear();\n"+
		"            _stack.Push(0);\n"+
		"\n"+
		"            _currentToken = scanner.NextToken();\n"+
		"\n"+
		"            while (!Step());\n"+
		"        }\n"+		
		
		"\n"+
		"        private bool Step()\n"+
		"        {\n"+
		"            if (_currentToken == null)\n"+
        "            {\n"+
		"                int pos = 0;\n"+
		"                if (_previousToken != null)\n"+
		"                    pos = _previousToken.Position + _previousToken.Lexeme.Length;\n"+
		"\n"+
        "                _currentToken = new Token(DOLLAR, \"$\", pos);\n"+
        "            }\n"+
        "\n"+
        "            int token = _currentToken.Id;\n"+
		"            int state = (int)_stack.Peek();\n"+
		"\n"+
        "            int[] cmd = PARSER_TABLE[state][token-1];\n"+
		"\n"+
		"            switch (cmd[0])\n"+
		"            {\n"+
		"                case SHIFT:\n"+
		"                    _stack.Push(cmd[1]);\n"+
		"                    _previousToken = _currentToken;\n"+
		"                    _currentToken = _scanner.NextToken();\n"+
		"                    return false;\n"+
		"\n"+
		"                case REDUCE:\n"+
		"                    int[] prod = PRODUCTIONS[cmd[1]];\n"+
		"\n"+
		"                    for (int i=0; i<prod[1]; i++)\n"+
		"                        _stack.Pop();\n"+
		"\n"+
		"                    int oldState = (int)_stack.Peek();\n"+
		"                    _stack.Push(PARSER_TABLE[oldState][prod[0]-1][1]);\n"+
		"                    return false;\n"+
		"\n"+
		"                case ACTION:\n"+
		"                    int action = FIRST_SEMANTIC_ACTION + cmd[1] - 1;\n"+
		"                    _stack.Push(PARSER_TABLE[state][action][1]);\n"+
		"                    _semanticAnalyser.ExecuteAction(cmd[1], _previousToken);\n"+
		"                    return false;\n"+
		"\n"+
		"                case ACCEPT:\n"+
		"                    return true;\n"+
		"\n"+
		"                case ERROR:\n"+
		"                    throw new SyntaticError(PARSER_ERROR[state], _currentToken.Position);\n"+
		"            }\n"+
		"            return false;\n"+
		"        }\n"+
		"\n"
	
		);
		result.append("    }\n");

		return result.toString();
	}

	private String emitLLClass(Grammar g, Options parserOptions)
	{
		StringBuffer result = new StringBuffer();
		
		String classname = parserOptions.parserName;
		result.append("    public class ").append(classname).append("\n    {\n");
		
		String scannerName = parserOptions.scannerName;
		String semanName = parserOptions.semanticName;
		
		String variables = 
		"        private Stack _stack = new Stack();\n"+
		"        private Token _currentToken;\n"+
		"        private Token _previousToken;\n"+
		"        private "+scannerName+" _scanner;\n"+
		"        private "+semanName+" _semanticAnalyser;\n"+
		"\n";
		
		result.append(variables);
				
		result.append(emitLLFunctions(parserOptions));
		
		result.append("    }\n");

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
		"        private static bool IsTerminal(int x)\n"+
		"        {\n"+
		"            return x < FIRST_NON_TERMINAL;\n"+
		"        }\n"+
		"\n"+
		"        private static bool IsNonTerminal(int x)\n"+
		"        {\n"+
		"            return x >= FIRST_NON_TERMINAL && x < FIRST_SEMANTIC_ACTION;\n"+
		"        }\n"+
		"\n"+
		"        private static bool IsSemanticAction(int x)\n"+
		"        {\n"+
		"            return x >= FIRST_SEMANTIC_ACTION;\n"+
		"        }\n"+
		"";
	}
	
	private String emitDriver(Options parserOptions)
	{
		String scannerName = parserOptions.scannerName;
		String semanName   = parserOptions.semanticName;
				
		return 
		"        public void Parse("+scannerName+" scanner, "+semanName+" semanticAnalyser)\n"+
	    "        {\n"+
		"            _scanner = scanner;\n"+
		"            _semanticAnalyser = semanticAnalyser;\n"+
		"\n"+
		"            _stack.Clear();\n"+
		"            _stack.Push(DOLLAR);\n"+
		"            _stack.Push(START_SYMBOL);\n"+
		"\n"+
		"            _currentToken = _scanner.NextToken();\n"+
		"\n"+
		"            while ( ! Step() )\n"+
		"            ;\n"+
	    "        }\n"+		
		"";
	}

	private String emitStep()
	{
		return 
		"        private bool Step()\n"
		+ "        {\n"
		+ "            if (_currentToken == null)\n"
		+ "            {\n"
		+ "                int pos = 0;\n"
		+ "                if (_previousToken != null)\n"
		+ "                    pos = _previousToken.Position + _previousToken.Lexeme.Length;\n"
		+ "\n"
		+ "                _currentToken = new Token(DOLLAR, \"$\", pos);\n"
		+ "            }\n"
		+ "\n"
		+ "            int x = (int)_stack.Pop();\n"
		+ "            int a = _currentToken.Id;\n"
		+ "\n"
		+ "            if (x == EPSILON)\n"
		+ "            {\n"
		+ "                return false;\n"
		+ "            }\n"
		+ "            else if (IsTerminal(x))\n"
		+ "            {\n"
		+ "                if (x == a)\n"
		+ "                {\n"
		+ "                    if (_stack.Empty())\n"
		+ "                        return true;\n"
		+ "                    else\n"
		+ "                    {\n"
		+ "                        _previousToken = _currentToken;\n"
		+ "                        _currentToken = _scanner.NextToken();\n"
		+ "                        return false;\n"
		+ "                    }\n"
		+ "                }\n"
		+ "                else\n"
		+ "                {\n"
		+ "                    throw new SyntaticError(PARSER_ERROR[x], _currentToken.Position);\n"
		+ "                }\n"
		+ "            }\n"
		+ "            else if (IsNonTerminal(x))\n"
		+ "            {\n"
		+ "                if (PushProduction(x, a))\n"
		+ "                    return false;\n"
		+ "                else\n"
		+ "                    throw new SyntaticError(PARSER_ERROR[x], _currentToken.Position);\n"
		+ "            }\n"
		+ "            else // isSemanticAction(x)\n"
		+ "            {\n"
		+ "                _semanticAnalyser.ExecuteAction(x - FIRST_SEMANTIC_ACTION, _previousToken);\n"
		+ "                return false;\n"
		+ "            }\n"
		+ "        }\n"
		+ "\n"
		+ "        private bool PushProduction(int topStack, int tokenInput)\n"
		+ "        {\n"
		+ "            int p = PARSER_TABLE[topStack - FIRST_NON_TERMINAL][tokenInput - 1];\n"
		+ "            if (p >= 0)\n"
		+ "            {\n"
		+ "                int[] production = PRODUCTIONS[p];\n"
		+ "                //empilha a produção em ordem reversa\n"
		+ "                for (int i = production.Length - 1; i >= 0; i--)\n"
		+ "                {\n"
		+ "                    _stack.Push(production[i]);\n"
		+ "                }\n"
		+ "                return true;\n"
		+ "            }\n"
		+ "            else\n"
		+ "                return false;\n"
		+ "        }\n";
	}
	
	private String emitRecursiveDecendantClass(Grammar g, Options parserOptions) throws NotLLException
	{
		RecursiveDescendent rd = new RecursiveDescendent(g);
		
		StringBuffer result = new StringBuffer();

		String classname = parserOptions.parserName;
		result.append("    public class ").append(classname).append("\n{\n");

		String scannerName = parserOptions.scannerName;
		String semanName = parserOptions.semanticName;

		String variables = 
		"        private Token _currentToken;\n"+
		"        private Token _previousToken;\n"+
		"        private "+scannerName+" _scanner;\n"+
		"        private "+semanName+" _semanticAnalyser;\n"+
		"\n";
	
		result.append(variables);
		
		result.append(	
		"        public void Parse("+scannerName+" scanner, "+semanName+" semanticAnalyser)\n"+
		"        {\n"+
		"            _scanner = scanner;\n"+
		"            _semanticAnalyser = semanticAnalyser;\n"+
		"\n"+
		"            _currentToken = scanner.nextToken();\n"+
		"            if (_currentToken == null)\n"+
		"                _currentToken = new Token(DOLLAR, \"$\", 0);\n"+
		"\n"+
		"            "+rd.getStart()+"();\n"+
		"\n"+
		"            if (_currentToken.Id != DOLLAR)\n"+
		"                throw new SyntaticError(PARSER_ERROR[DOLLAR], _currentToken.Position);\n"+
		"        }\n"+		
		"\n"+
		"        private void Match(int token)\n"+
		"        {\n"+
		"            if (_currentToken.I) == token)\n"+
		"            {\n"+
		"                _previousToken = _currentToken;\n"+
		"                _currentToken = _scanner.NextToken();\n"+
		"                if (_currentToken == null)\n"+
		"                {\n"+
		"                    int pos = 0;\n"+
		"                    if (_previousToken != null)\n"+
		"                        pos = _previousToken.Position + _previousToken.Lexeme.Length;\n"+
		"\n"+
		"                    _currentToken = new Token(DOLLAR, \"$\", pos);\n"+
		"                }\n"+
		"            }\n"+
		"            else\n"+
		"                throw new SyntaticError(PARSER_ERROR[token], _currentToken.Position);\n"+
		"        }\n"+
		"\n");

		Map<String, RecursiveDescendent.Function> funcs = rd.build();

		for (int symb=g.FIRST_NON_TERMINAL; symb<g.FIRST_SEMANTIC_ACTION(); symb++)
		{
			String name = rd.getSymbols(symb);
			RecursiveDescendent.Function f = funcs.get(name);
			
			result.append(
						"        private void "+name+"()\n"+
						"        {\n"+
						"            switch (_currentToken.Id)\n"+
						"            {\n" );
					
			List<Integer> keys = new LinkedList<Integer>(f.input.keySet());
					
			for (int i = 0; i<keys.size(); i++)
			{
				IntList rhs = f.input.get(keys.get(i));
				int token = keys.get(i).intValue();
	
				result.append(
						"                case "+token+": // "+rd.getSymbols(token)+"\n");
				for (int j=i+1; j<keys.size(); j++)
				{
					IntList rhs2 = f.input.get(keys.get(j));
					if (rhs2.equals(rhs))
					{
						token = keys.get(j).intValue();
						result.append(
						"                case "+token+": // "+rd.getSymbols(token)+"\n");
						keys.remove(j);
						j--;
					}
				}
				
				if (rhs.size() == 0)
					result.append(
						"                    // EPSILON\n");	
			
				for (int k=0; k<rhs.size(); k++)
				{
					int s = rhs.get(k);
					if (g.isTerminal(s))
					{
						result.append(
						"                    Match("+s+"); // "+rd.getSymbols(s)+"\n");	
					}
					else if (g.isNonTerminal(s))
					{
						result.append(
						"                    "+rd.getSymbols(s)+"();\n");	
					}
					else //isSemanticAction(s)
					{
						result.append(
						"                    _semanticAnalyser.ExecuteAction("+(s-g.FIRST_SEMANTIC_ACTION())+", _previousToken);\n");
					}
				}
			
				result.append(
						"                    break;\n");
			}

			result.append(
						"                default:\n"+
						"                    throw new SyntaticError(PARSER_ERROR["+f.lhs+"], _currentToken.Position);\n"+
						"            }\n"+
						"        }\n"+
						"\n");
		}
		
		result.append("    }\n");

		return result.toString();
	}
	
	private String generateSemanticAnalyser(Options options)
	{
		StringBuffer result = new StringBuffer();
		String cls = 
		"    public class "+options.semanticName+"\n"+
		"    {\n" +
		"        public void ExecuteAction(int action, Token token)\n"+
		"        {\n"+
		"            System.Console.WriteLine($\"Ação #{action}, Token: {token}\");\n"+
		"        }\n"+
		"    }\n"+
		"";
		
		result.append(cls);
		CSharpCommonGenerator.colocarNamespace(result, options);
		
		result.insert(0, CSharpCommonGenerator.emitStaticImport(options, "Constants"));
		
		return result.toString();
	}
}
