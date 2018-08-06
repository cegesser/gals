package gesser.gals.generator.cpp;

import gesser.gals.generator.Options;
import gesser.gals.generator.scanner.FiniteAutomata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gesser.gals.generator.Options.Input.*;
import static gesser.gals.generator.Options.ScannerTable.*;
/**
 * @author Gesser
 */

public class CppScannerGeneretor
{
	boolean sensitive = true;
	boolean lookup = true;
	
	public Map<String, String> generate(FiniteAutomata fa, Options options)
	{		
		Map<String, String> result = new HashMap<String, String>();
		
		String classname = options.scannerName;
		
		String scannerH;
		String scannerCpp;
		
		if (fa != null)
		{
			sensitive = options.scannerCaseSensitive;
			lookup = fa.getSpecialCases().length > 0;
		 	scannerH = buildScannerH(fa, options);
		 	scannerCpp = buildScannerCpp(fa, options);
		}
		else
		{
			scannerH = buildEmptyScannerH(options);
			scannerCpp = buildEmptyScannerCpp(options);
		}
		
		result.put(classname+".h", scannerH);
		result.put(classname+".cpp", scannerCpp);
		
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
	
	private String buildScannerH(FiniteAutomata fa, Options options)
	{
		StringBuffer result = new StringBuffer();
		
		String classname = options.scannerName;
		
		String inType;
		String inInc;
		String constr;
		
		if(options.input == STREAM)
		{
			inType = "std::istream &";
			inInc = "#include <iostream>\n";
			constr = 
				"    "+classname+"("+inType+"input) { setInput(input); }\n"+
				"    "+classname+"() : input(\"\"), position(0) { }\n";
		}
		else if(options.input == STRING)
		{
			inType = "const char *";
			inInc = "";
			constr = 
				"    "+classname+"("+inType+"input = \"\") { setInput(input); }\n";
		}
		else
		{
			inType = null;
			inInc = null;
			constr = null;
		}
		
		result.append("#ifndef ").append(classname.toUpperCase()).append("_H\n");
		result.append("#define ").append(classname.toUpperCase()).append("_H\n");

		result.append(
			"\n"+
			"#include \"Token.h\"\n"+
			"#include \"LexicalError.h\"\n"+
			"\n"+
			"#include <string>\n"+
			inInc+
			"\n" );
			
		result.append(openNamespace(options));
		
		String cls = 
			"class "+classname+"\n"+
			"{\n"+
			"public:\n"+
			constr+
			"\n"+
			"    void setInput("+inType+"input);\n"+
			"    void setPosition(unsigned pos) { position = pos; }\n"+
			"    Token *nextToken() throw (LexicalError);\n"+
			"\n"+
    		"private:\n"+
			"    unsigned position;\n"+
			"    std::string input;\n"+
			"\n"+
			"    int nextState(unsigned char c, int state) const;\n"+
			"    TokenId tokenForState(int state) const;\n"+
		    (lookup?
			"    TokenId lookupToken(TokenId base, const std::string &key);\n":"")+
    		"\n"+
			"    bool hasInput() const { return position < input.size(); }\n"+
			"    char nextChar() { return hasInput() ? input[position++] : (char) -1; }\n"+
			"};\n"+

			"\n";
			
		result.append(cls);
			
		result.append(closeNamespace(options));
		
		result.append("#endif\n");
				
		return result.toString();
	}

	private String buildScannerCpp(FiniteAutomata fa, Options options)
	{
		StringBuffer result = new StringBuffer();
		
		String classname = options.scannerName;
		
		result.append("#include \""+classname+".h\"\n\n");
		
		if (!sensitive)
			result.append("#include <cctype>\n\n");
		
		result.append(openNamespace(options));
		
		String inType;
		String inInit;
		if(options.input == STREAM)
		{
			inType = "std::istream &";
			inInit = 
			"    std::istreambuf_iterator<char> in(input);\n"+
			"    std::istreambuf_iterator<char> eof;\n"+
			"\n"+
			"    this->input.assign(in, eof);\n"+
			"\n"+
			"";
		}
		else if(options.input == STRING)
		{
			inType = "const char *";
			inInit = "    this->input = input;\n";
		}
		else
		{
			inType = null;
			inInit = null;
		}
		
		String funcs = 
			"void "+classname+"::setInput("+inType+"input)\n"+
			"{\n"+
			inInit+
			"    setPosition(0);\n"+
			"}\n"+
			"\n"+
			"Token *"+classname+"::nextToken() throw (LexicalError)\n"+
			"{\n"+
			"    if ( ! hasInput() )\n"+
			"        return 0;\n"+
			"\n"+
			"    unsigned start = position;\n"+
			"\n"+
			"    int state = 0;\n"+
			"    int oldState = 0;\n"+
			"    int endState = -1;\n"+
			"    int end = -1;\n"+
			(fa.hasContext() ?
			"    int ctxtState = -1;\n"+
			"    int ctxtEnd = -1;\n" : "")+
			"\n"+
			"    while (hasInput())\n"+
			"    {\n"+
			"        oldState = state;\n"+
			"        state = nextState(nextChar(), state);\n"+
			"\n"+
			"        if (state < 0)\n"+
			"            break;\n"+
			"\n"+
			"        else\n"+
			"        {\n"+
			"            if (tokenForState(state) >= 0)\n"+
			"            {\n"+
			"                endState = state;\n"+
			"                end = position;\n"+
			"            }\n"+
			(fa.hasContext() ? 
			"            if (SCANNER_CONTEXT[state][0] == 1)\n" +
			"            {\n" +
			"                ctxtState = state;\n" +
			"                ctxtEnd = position;\n" +
			"            }\n" : "")+
			"        }\n"+
			"    }\n"+
			"    if (endState < 0 || (endState != state && tokenForState(oldState) == -2))\n"+
			"        throw LexicalError(SCANNER_ERROR[oldState], start);\n"+
			"\n"+
			(fa.hasContext() ? 
			"    if (ctxtState != -1 && SCANNER_CONTEXT[endState][1] == ctxtState)\n"+
			"        end = ctxtEnd;\n"+
			"\n" : "" )+
			"    position = end;\n"+
			"\n"+
			"    TokenId token = tokenForState(endState);\n"+
			"\n"+
			"    if (token == 0)\n"+
			"        return nextToken();\n"+
			"    else\n"+
			"    {\n"+
			"            std::string lexeme = input.substr(start, end-start);\n"+
			(lookup?
			"            token = lookupToken(token, lexeme);\n":"")+
			"            return new Token(token, lexeme, start);\n"+
			"    }\n"+
			"}\n"+
			"\n"+
			"int "+classname+"::nextState(unsigned char c, int state) const\n"+
			"{\n"+
			nextStateImpl(fa, options)+
			"}\n"+
			"\n"+
			"TokenId "+classname+"::tokenForState(int state) const\n"+
			"{\n"+
			"    int token = -1;\n"+
			"\n"+
			"    if (state >= 0 && state < STATES_COUNT)\n"+
			"        token = TOKEN_STATE[state];\n"+
			"\n"+
			"    return static_cast<TokenId>(token);\n"+
			"}\n"+
			"\n"+
			(lookup ? 
			"TokenId "+classname+"::lookupToken(TokenId base, const std::string &key)\n"+
			"{\n"+			
			"    int start = SPECIAL_CASES_INDEXES[base];\n"+
			"    int end   = SPECIAL_CASES_INDEXES[base+1]-1;\n"+			
			"\n"+
			(sensitive?"":
			"    std::string key_u = key;\n"+
			"    for (int i=0; i<key.size(); i++)\n"+
			"        key_u[i] = std::toupper(key_u[i]);\n"+
			"\n")+
			"    while (start <= end)\n"+
			"    {\n"+
			"        int half = (start+end)/2;\n"+
			"        const std::string current = SPECIAL_CASES_KEYS[half];\n"+      
			"\n"+
			(sensitive ?
			"        if (current == key)\n" :
			"        if (current == key_u)\n")+
			"            return static_cast<TokenId>(SPECIAL_CASES_VALUES[half]);\n"+
			(sensitive ?
			"        else if (current < key)\n" :
			"        else if (current < key_u)\n" )+
			"            start = half+1;\n"+
			"        else  //(current > key)\n"+
			"            end = half-1;\n"+
			"    }\n"+
			"\n"+
			"    return base;\n"+
			"}\n"+
			"\n" : "");
			
		result.append(funcs);
			
		result.append(closeNamespace(options));
		
		return result.toString();
	}

	private String nextStateImpl(FiniteAutomata fa, Options opt)
	{
		switch (opt.scannerTable)
		{
			case FULL:
			case COMPACT:
				return 
					"    int next = SCANNER_TABLE[state][c];\n"+
					"    return next;\n";
			case HARDCODE:
			{
				List<Map<Character, Integer>> trans = fa.getTransitions();
				StringBuffer casesState = new StringBuffer();
				for (int i=0; i<trans.size(); i++)
				{
					Map<Character, Integer> m = trans.get(i);
					if (m.size() == 0)
						continue;
						
					casesState.append(
				"        case "+i+":\n"+
				"            switch (c)\n"+
				"            {\n");

					for (Map.Entry<Character, Integer> entry : m.entrySet())
					{
						Character ch = entry.getKey();
						Integer it = entry.getValue();
						casesState.append(
				"                case "+((int)ch.charValue())+": return "+it+";\n");
					}

					casesState.append(
				"                default: return -1;\n"+
				"            }\n");
				}

				return 
				"    switch (state)\n"+
				"    {\n"+
				casesState.toString()+
				"        default: return -1;\n"+
				"    }\n";
			}
			default:
				return null;
		}
	}
	
	private String buildEmptyScannerH(Options options)
	{
		StringBuffer result = new StringBuffer();
		
		String classname = options.scannerName;
		
		
		result.append("#ifndef ").append(classname.toUpperCase()).append("_H\n");
		result.append("#define ").append(classname.toUpperCase()).append("_H\n");

		result.append(
			"\n"+
			"#include \"Token.h\"\n"+
			"#include \"LexicalError.h\"\n"+
			"\n" );
			
		result.append(openNamespace(options));
		
		String cls = 
			"class "+classname+"\n"+
			"{\n"+
			"public:\n"+
			"\n"+
			"    Token *nextToken() throw (LexicalError);\n"+
			"\n"+
    		"};\n"+
			"\n";
			
		result.append(cls);
			
		result.append(closeNamespace(options));
		
		result.append("#endif\n");
				
		return result.toString();
	}
	
	private String buildEmptyScannerCpp(Options options)
	{
		StringBuffer result = new StringBuffer();
		
		String classname = options.scannerName;
		
		result.append("#include \""+classname+".h\"\n\n");
		
		result.append(openNamespace(options));
		
		String funcs = 
			"Token *"+classname+"::nextToken() throw (LexicalError)\n"+
			"{\n"+
			"    return 0;\n"+
			"}\n"+
			"\n";
			
		result.append(funcs);
			
		result.append(closeNamespace(options));
		
		return result.toString();
	}
}
