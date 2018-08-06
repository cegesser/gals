package gesser.gals.generator.cpp;

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
 * @author Gesser
 */

public class CppParserGenerator
{
	private RecursiveDescendent rd;
	
	public Map<String, String> generate(Grammar g, Options options) throws NotLLException
	{
		Map<String, String> result = new HashMap<String, String>();
		
		if (g != null)
		{
		
			String classname = options.parserName;
			
			result.put(classname+".h", parserH(g, options));
			result.put(classname+".cpp", parserCpp(g, options));			
			
			result.put(options.semanticName + ".cpp", semanticAnalyserCpp(options));
			result.put(options.semanticName + ".h", semanticAnalyserH(options));
		}
		
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

	private String semanticAnalyserH(Options options)
	{
		String classname = options.semanticName;
		return 
			"#ifndef "+classname.toUpperCase()+"_H\n"+
			"#define "+classname.toUpperCase()+"_H\n"+
			"\n"+
			"#include \"Token.h\"\n"+
			"#include \"SemanticError.h\"\n"+
			"\n"+
			openNamespace(options)+
			"class "+classname+"\n"+
			"{\n"+
			"public:\n"+
			"    void executeAction(int action, const Token *token) throw (SemanticError );\n"+
			"};\n"+
			"\n"+
			closeNamespace(options)+
			"#endif\n"+
			"";
	}

	private String semanticAnalyserCpp(Options options)
	{
		String classname = options.semanticName;
		
		return 
			"#include \""+classname+".h\"\n"+
			"#include \"Constants.h\"\n"+
			"\n"+
			"#include <iostream>\n"+
			"\n"+
			openNamespace(options)+
			"void "+classname+"::executeAction(int action, const Token *token) throw (SemanticError )\n"+
			"{\n"+
			"    std::cout << \"Ação: \" << action << \", Token: \"  << token->getId() \n"+
			"              << \", Lexema: \" << token->getLexeme() << std::endl;\n"+
			"}\n"+
			"\n"+
			closeNamespace(options)+
			"";
	}
	
	private String parserH(Grammar g, Options options) throws NotLLException
	{
		String scannerName  = options.scannerName;
		String parserName   = options.parserName;
		String semanticName = options.semanticName;
		
		Options.Parser type = options.parser;
		
		boolean descendant = type == RD;
		String recDescFuncs = "";
		
		if (descendant)
		{
			rd = new RecursiveDescendent(g);
			StringBuffer tmp = new StringBuffer();
			tmp.append( "    void match(int token) throw (AnalysisError);");
			for (int i=g.FIRST_NON_TERMINAL; i<g.FIRST_SEMANTIC_ACTION(); i++)
				tmp.append("    void ").append(rd.getSymbols(i)).append("() throw (AnalysisError);\n");
			recDescFuncs = tmp.toString();
		}
		
		String parser =  
			"#ifndef "+parserName+"_H\n"+
			"#define "+parserName+"_H\n"+
			"\n"+
			"#include \"Constants.h\"\n"+
			"#include \"Token.h\"\n"+
			"#include \""+scannerName+".h\"\n"+
			"#include \""+semanticName+".h\"\n"+
			"#include \"SyntaticError.h\"\n"+
			"\n"+
			(descendant ? "" :
			"#include <stack>\n"+
			"\n")+
			openNamespace(options)+
			"class "+parserName+"\n"+
			"{\n"+
			"public:\n"+
			"    "+parserName+"() : previousToken(0), currentToken(0) { }\n"+
			"\n"+
			"    ~"+parserName+"()\n"+
			"    {\n"+
			"        if (previousToken != 0 && previousToken != currentToken) delete previousToken;\n"+
			"        if (currentToken != 0)  delete currentToken;\n"+
			"    }\n"+
			"\n"+			
			"    void parse("+scannerName+" *scanner, "+semanticName+" *semanticAnalyser) throw (AnalysisError);\n"+
			"\n"+
			"private:\n"+
			(descendant ? "" :
			"    std::stack<int> stack;\n")+
			"    Token *currentToken;\n"+
			"    Token *previousToken;\n"+
			"    "+scannerName+" *scanner;\n"+
			"    "+semanticName+" *semanticAnalyser;\n"+
			"\n"+
			( descendant ? recDescFuncs :
			"    bool step() throw (AnalysisError);\n"+
			(type == LL ?
			"    bool pushProduction(int topStack, int tokenInput);\n"+
			"\n"+
			"    static bool isTerminal(int x) { return x < FIRST_NON_TERMINAL; }\n"+
			"    static bool isNonTerminal(int x) { return x >= FIRST_NON_TERMINAL && x < FIRST_SEMANTIC_ACTION; }\n"+
			"    static bool isSemanticAction(int x) { return x >= FIRST_SEMANTIC_ACTION; }\n" : "") )+
			"};\n"+
			"\n"+
			closeNamespace(options)+
			"#endif\n"+
			"";
		return parser;
	}
	
	private String parserCpp(Grammar g, Options options)
	{
		switch (options.parser)
		{
			case RD:
				return parserCppRecursiveDescendant(g, options);
						
			case LL:
				return parserCppLL(g, options);
		
			default: //slr, lalar, lr
				return parserCppLR(g, options);
		}
	}
	
	private String parserCppRecursiveDescendant(Grammar g, Options options)
	{
		String scannerName  = options.scannerName;
		String parserName   = options.parserName;
		String semanticName = options.semanticName;

		String top =  
			"#include \""+parserName+".h\"\n"+			
			"\n"+
			openNamespace(options)+
			"void "+parserName+"::parse("+scannerName+" *scanner, "+semanticName+" *semanticAnalyser) throw (AnalysisError)\n"+
			"{\n"+
			"    this->scanner = scanner;\n"+
			"    this->semanticAnalyser = semanticAnalyser;\n"+
			"\n"+
			"    if (previousToken != 0 && previousToken != currentToken)\n"+
			"        delete previousToken;\n"+
			"    previousToken = 0;\n"+
			"\n"+
			"    if (currentToken != 0)\n"+
			"        delete currentToken;\n"+
			"    currentToken = scanner->nextToken();\n"+
			"    if (currentToken == 0)\n" +			"        currentToken = new Token(DOLLAR, \"$\", 0);\n"+
			"\n"+
			"    "+rd.getStart()+"();\n"+
			"\n"+
			"    if (currentToken->getId() != DOLLAR)\n"+
			"        throw SyntaticError(PARSER_ERROR[DOLLAR], currentToken->getPosition());\n"+
			"}\n"+
			"\n"+
			
			"void "+parserName+"::match(int token) throw (AnalysisError)\n"+
			"{\n"+
			"    if (currentToken->getId() == token)\n"+
			"    {\n"+
			"        if (previousToken != 0)\n"+
			"            delete previousToken;\n"+
			"        previousToken = currentToken;\n"+
			"        currentToken = scanner->nextToken();\n"+
			"        if (currentToken == 0)\n"+
			"        {\n"+
			"            int pos = 0;\n"+
			"            if (previousToken != 0)\n"+
			"                pos = previousToken->getPosition()+previousToken->getLexeme().size();\n"+
			"\n"+
			"            currentToken = new Token(DOLLAR, \"$\", pos);\n"+
			"        }\n"+
			"    }\n"+
			"    else\n"+
			"        throw SyntaticError(PARSER_ERROR[token], currentToken->getPosition());\n"+
			"}\n";
			
		StringBuffer bfr = new StringBuffer();
			
		Map funcs = rd.build();

		for (int symb=g.FIRST_NON_TERMINAL; symb<g.FIRST_SEMANTIC_ACTION(); symb++)
		{
			String name = rd.getSymbols(symb);
			RecursiveDescendent.Function f = (RecursiveDescendent.Function) funcs.get(name);
	
			bfr.append(
						"\n"+
						"void "+parserName+"::"+name+"() throw (AnalysisError)\n"+
						"{\n"+
						"    switch (currentToken->getId())\n"+
						"    {\n" );
			
			List<Integer> keys = new LinkedList<Integer>(f.input.keySet());
					
			for (int i = 0; i<keys.size(); i++)
			{
				IntList rhs = f.input.get(keys.get(i));
				int token = keys.get(i).intValue();

				bfr.append(
						"        case "+token+": // "+rd.getSymbols(token)+"\n");
				for (int j=i+1; j<keys.size(); j++)
				{
					IntList rhs2 = f.input.get(keys.get(j));
					if (rhs2.equals(rhs))
					{
						token = keys.get(j).intValue();
						bfr.append(
						"        case "+token+": // "+rd.getSymbols(token)+"\n");
						keys.remove(j);
						j--;
					}
				}
	
				if (rhs.size() == 0)
					bfr.append(
						"            // EPSILON\n");
				for (int k=0; k<rhs.size(); k++)
				{
					int s = rhs.get(k);
					if (g.isTerminal(s))
					{
						bfr.append(
						"            match("+s+"); // "+rd.getSymbols(s)+"\n");	
					}
					else if (g.isNonTerminal(s))
					{
						bfr.append(
						"            "+rd.getSymbols(s)+"();\n");	
					}
					else //isSemanticAction(s)
					{
						bfr.append(
						"            semanticAnalyser->executeAction("+(s-g.FIRST_SEMANTIC_ACTION())+", previousToken);\n");
					}
				}
	
				bfr.append(
						"            break;\n");
			}

			bfr.append(
						"        default:\n"+
						"            throw SyntaticError(PARSER_ERROR["+f.lhs+"], currentToken->getPosition());\n"+
						"    }\n"+
						"}\n" );
		}
			
			
		String bottom = 
			"\n"+
			closeNamespace(options)+
			"";
			
		return top + bfr.toString() + bottom;			
	}
	
	private String parserCppLL(Grammar g, Options options)
	{
		String scannerName  = options.scannerName;
		String parserName   = options.parserName;
		String semanticName = options.semanticName;
		
		return 
			"#include \""+parserName+".h\"\n"+			
			"\n"+
			openNamespace(options)+
			"void "+parserName+"::parse("+scannerName+" *scanner, "+semanticName+" *semanticAnalyser) throw (AnalysisError)\n"+
			"{\n"+
			"    this->scanner = scanner;\n"+
			"    this->semanticAnalyser = semanticAnalyser;\n"+
			"\n"+
			"    //Limpa a pilha\n"+
    		"    while (! stack.empty())\n"+
        	"        stack.pop();\n"+
        	"\n"+
			"    stack.push(DOLLAR);\n"+
			"    stack.push(START_SYMBOL);\n"+
			"\n"+
			"    if (previousToken != 0 && previousToken != currentToken)\n"+
			"        delete previousToken;\n"+
			"    previousToken = 0;\n"+
			"\n"+
			"    if (currentToken != 0)\n"+
			"        delete currentToken;\n"+
			"    currentToken = scanner->nextToken();\n"+
			"\n"+
			"    while ( ! step() )\n"+
			"        ;\n"+
			"}\n"+
			"\n"+
			"bool "+parserName+"::step() throw (AnalysisError)\n"+
			"{\n"+
			"    if (currentToken == 0) //Fim de Sentenca\n"+
			"    {\n"+
			"        int pos = 0;\n"+
			"        if (previousToken != 0)\n"+
			"            pos = previousToken->getPosition() + previousToken->getLexeme().size();\n"+
			"\n"+
			"        currentToken = new Token(DOLLAR, \"$\", pos);\n"+
			"    }\n"+
			"\n"+
			"    int a = currentToken->getId();\n"+
			"    int x = stack.top();\n"+
			"\n"+
			"    stack.pop();\n"+
			"\n"+
			"    if (x == EPSILON)\n"+
			"    {\n"+
			"        return false;\n"+
			"    }\n"+
			"    else if (isTerminal(x))\n"+
			"    {\n"+
			"        if (x == a)\n"+
			"        {\n"+
			"            if (stack.empty())\n"+
			"                return true;\n"+
			"            else\n"+
			"            {\n"+
			"                if (previousToken != 0)\n"+
			"                    delete previousToken;\n"+
			"                previousToken = currentToken;\n"+
			"                currentToken = scanner->nextToken();\n"+
			"                return false;\n"+
			"            }\n"+
			"        }\n"+
			"        else\n"+
			"        {\n"+
			"            throw SyntaticError(PARSER_ERROR[x], currentToken->getPosition());\n"+
			"        }\n"+
			"    }\n"+
			"    else if (isNonTerminal(x))\n"+
			"    {\n"+
			"        if (pushProduction(x, a))\n"+
			"            return false;\n"+
			"        else\n"+
			"            throw SyntaticError(PARSER_ERROR[x], currentToken->getPosition());\n"+
			"    }\n"+
			"    else // isSemanticAction(x)\n"+
			"    {\n"+
			"        semanticAnalyser->executeAction(x-FIRST_SEMANTIC_ACTION, previousToken);\n"+
			"        return false;\n"+
			"    }\n"+
			"}\n"+
			"\n"+
			"bool "+parserName+"::pushProduction(int topStack, int tokenInput)\n"+
			"{\n"+
			"    int p = PARSER_TABLE[topStack-FIRST_NON_TERMINAL][tokenInput-1];\n"+
			"    if (p >= 0)\n"+
			"    {\n"+
			"        int *production = PRODUCTIONS[p];\n"+
			"        //empilha a produção em ordem reversa\n"+
			"        int length = production[0];\n"+
			"        for (int i=length; i>=1; i--)\n"+
			"        {\n"+
			"            stack.push( production[i] );\n"+
			"        }\n"+
			"        return true;\n"+
			"    }\n"+
			"    else\n"+
			"        return false;\n"+
			"}\n"+
			"\n"+
			closeNamespace(options)+
			"";
	}
	
	private String parserCppLR(Grammar g, Options options)
	{
		String scannerName  = options.scannerName;
		String parserName   = options.parserName;
		String semanticName = options.semanticName;

		return 
			"#include \""+parserName+".h\"\n"+			
			"\n"+
			openNamespace(options)+
			"void "+parserName+"::parse("+scannerName+" *scanner, "+semanticName+" *semanticAnalyser) throw (AnalysisError)\n"+
			"{\n"+
			"    this->scanner = scanner;\n"+
			"    this->semanticAnalyser = semanticAnalyser;\n"+
			"\n"+
			"    //Limpa a pilha\n"+
			"    while (! stack.empty())\n"+
			"        stack.pop();\n"+
			"\n"+
			"    stack.push(0);\n"+
			"\n"+
			"    if (previousToken != 0 && previousToken != currentToken)\n"+
			"        delete previousToken;\n"+
			"    previousToken = 0;\n"+
			"\n"+
			"    if (currentToken != 0)\n"+
			"        delete currentToken;\n"+
			"    currentToken = scanner->nextToken();\n"+
			"\n"+
			"    while ( ! step() )\n"+
			"        ;\n"+
			"}\n"+
			"\n"+
			"bool "+parserName+"::step() throw (AnalysisError)\n"+
			"{\n"+
			"    if (currentToken == 0) //Fim de Sentensa\n"+
			"    {\n"+
			"        int pos = 0;\n"+
			"        if (previousToken != 0)\n"+
			"            pos = previousToken->getPosition() + previousToken->getLexeme().size();\n"+
			"\n"+
			"        currentToken = new Token(DOLLAR, \"$\", pos);\n"+
			"    }\n"+
			"\n"+
			
			"    int token = currentToken->getId();\n"+
			"    int state = stack.top();\n"+
			"\n"+
			"    const int* cmd = PARSER_TABLE[state][token-1];\n"+
			"\n"+
			"    switch (cmd[0])\n"+
			"    {\n"+
			"        case SHIFT:\n"+
			"        {\n"+
			"            stack.push(cmd[1]);\n"+
			
			"            if (previousToken != 0)\n"+
			"                delete previousToken;\n"+
			"            previousToken = currentToken;\n"+
			"            currentToken = scanner->nextToken();\n"+
			"            return false;\n"+
			"        }\n"+
			"        case REDUCE:\n"+
			"        {\n"+
			"            const int* prod = PRODUCTIONS[cmd[1]];\n"+
			"\n"+
			"            for (int i=0; i<prod[1]; i++)\n"+
			"                stack.pop();\n"+
			"\n"+
			"            int oldState = stack.top();\n"+
			"            stack.push(PARSER_TABLE[oldState][prod[0]-1][1]);\n"+
			"            return false;\n"+
			"        }\n"+
			"        case ACTION:\n"+
			"        {\n"+
			"            int action = FIRST_SEMANTIC_ACTION + cmd[1] - 1;\n"+
			"            stack.push(PARSER_TABLE[state][action][1]);\n"+
			"            semanticAnalyser->executeAction(cmd[1], previousToken);\n"+
			"            return false;\n"+
			"        }\n"+
			"        case ACCEPT:\n"+
			"            return true;\n"+
			"\n"+
			"        case ERROR:\n"+
			"            throw SyntaticError(PARSER_ERROR[state], currentToken->getPosition());\n"+
			"    }\n"+
			"    return false;\n"+
			"}\n"+
			"\n"+
			closeNamespace(options)+
			"";
	}
}
