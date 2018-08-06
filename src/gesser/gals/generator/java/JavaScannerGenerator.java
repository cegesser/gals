package gesser.gals.generator.java;

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

public class JavaScannerGenerator
{
	boolean sensitive = true;
	boolean lookup = true;
	
	public Map<String, String> generate(FiniteAutomata fa, Options options)
	{
		Map<String, String> result = new HashMap<String, String>();
		
		String classname = options.scannerName;
		
		String scanner;
		if (fa != null)
		{
			sensitive = options.scannerCaseSensitive;
			lookup = fa.getSpecialCases().length > 0;
		 	scanner = buildScanner(fa, options);
		}
		else
			scanner = buildEmptyScanner(options);
		
		result.put(classname+".java", scanner);
		
		return result;
	}

	private String buildEmptyScanner(Options options)
	{
		StringBuffer result = new StringBuffer();
		
		String package_ = options.pkgName;
		
		result.append(emitPackage(package_));
		
		String cls = 
		"public class "+options.scannerName+" implements Constants\n"+
		"{\n"+
		"    public Token nextToken() throws LexicalError\n"+
		"    {\n"+
		"        return null;\n"+
		"    }\n"+
		"}\n"+
		"";
		
		result.append(cls);
		
		return result.toString();
	}

	private String buildScanner(FiniteAutomata fa, Options options)
	{
		String inType;
		String inInit;
		String inDef;
		if(options.input == STREAM)
		{
			inType = "java.io.Reader";
			inInit = 
			        "StringBuffer bfr = new StringBuffer();\n"+
			"        try\n"+
			"        {\n"+
			"            int c = input.read();\n"+			
			"            while (c != -1)\n"+
			"            {\n"+
			"                bfr.append((char)c);\n"+
			"                c = input.read();\n"+
			"            }\n"+
			"            this.input = bfr.toString();\n"+
			"        }\n"+
			"        catch (java.io.IOException e)\n"+
			"        {\n"+
			"            e.printStackTrace();\n"+
			"        }\n"+
			"";
			inDef = "this(new java.io.StringReader(\"\"));";
		}
		else if(options.input == STRING)
		{
			inType = "String";
			inInit = "this.input = input;";
			inDef = "this(\"\");";
		}
		else
		{
			//nunca acontece
			inType = "";
			inInit = "";
			inDef  = "";
		}
		
		String package_ = options.pkgName;
		
		String cls = 
		emitPackage(package_)+
		"public class "+options.scannerName+" implements Constants\n"+
		"{\n"+
		"    private int position;\n"+
		"    private String input;\n"+
		"\n"+
		"    public "+options.scannerName+"()\n"+
		"    {\n"+
		"        "+inDef+"\n"+
		"    }\n"+
		"\n"+
		"    public "+options.scannerName+"("+inType+" input)\n"+
		"    {\n"+
		"        setInput(input);\n"+
		"    }\n"+
		"\n"+
		"    public void setInput("+inType+" input)\n"+
		"    {\n"+
		"        "+inInit+"\n"+
		"        setPosition(0);\n"+
		"    }\n"+
		"\n"+
		"    public void setPosition(int pos)\n"+
		"    {\n"+
		"        position = pos;\n"+	
		"    }\n\n"+
		
		mainDriver(fa)+
		"\n"+
		auxFuncions(fa, options)+
		
		"}\n"+
		"";
		
		return cls;
	}
	
	private String emitPackage(String package_)
	{
		if (package_ != null && !package_.equals(""))
			return "package " + package_ + ";\n\n";
		else
			return "";
	}
	
	private String mainDriver(FiniteAutomata fa)
	{
		return 
		"    public Token nextToken() throws LexicalError\n"+
		"    {\n"+
		"        if ( ! hasInput() )\n"+
		"            return null;\n"+
		"\n"+		
		"        int start = position;\n"+
		"\n"+		
		"        int state = 0;\n"+
		"        int lastState = 0;\n"+
		"        int endState = -1;\n"+
		"        int end = -1;\n"+
		(fa.hasContext() ?
		"        int ctxtState = -1;\n"+
		"        int ctxtEnd = -1;\n" : "")+
		"\n"+
		"        while (hasInput())\n"+
		"        {\n"+
		"            lastState = state;\n"+
		"            state = nextState(nextChar(), state);\n"+
		"\n"+
		"            if (state < 0)\n"+
		"                break;\n"+
		"\n"+
		"            else\n"+
		"            {\n"+
		"                if (tokenForState(state) >= 0)\n"+
		"                {\n"+
		"                    endState = state;\n"+
		"                    end = position;\n"+
		"                }\n"+
		(fa.hasContext() ? 
		"                if (SCANNER_CONTEXT[state][0] == 1)\n" +		"                {\n" +		"                    ctxtState = state;\n" +		"                    ctxtEnd = position;\n" +		"                }\n" : "")+
		"            }\n"+
		"        }\n"+
		"        if (endState < 0 || (endState != state && tokenForState(lastState) == -2))\n"+
		"            throw new LexicalError(SCANNER_ERROR[lastState], start);\n"+
		"\n"+
		(fa.hasContext() ? 
		"        if (ctxtState != -1 && SCANNER_CONTEXT[endState][1] == ctxtState)\n"+		"            end = ctxtEnd;\n"+
		"\n" : "" )+
		"        position = end;\n"+
		"\n"+
		"        int token = tokenForState(endState);\n"+
		"\n"+
		"        if (token == 0)\n"+
		"            return nextToken();\n"+
		"        else\n"+
		"        {\n"+
		"            String lexeme = input.substring(start, end);\n"+
		(lookup ?
		"            token = lookupToken(token, lexeme);\n" : "")+
		"            return new Token(token, lexeme, start);\n"+
		"        }\n"+
		"    }\n"+
		"";
	}
	
	private String auxFuncions(FiniteAutomata fa, Options options)
	{		 
		String nextState;
		
		switch (options.scannerTable)
		{
			case FULL:
				nextState =
					"    private int nextState(char c, int state)\n"+
					"    {\n"+
					"        int next = SCANNER_TABLE[state][c];\n"+
					"        return next;\n"+
					"    }\n";
				break;
			case COMPACT:
				nextState =
					
					"    private int nextState(char c, int state)\n"+
					"    {\n"+
					"        int start = SCANNER_TABLE_INDEXES[state];\n"+
					"        int end   = SCANNER_TABLE_INDEXES[state+1]-1;\n"+
					"\n"+
					"        while (start <= end)\n"+
					"        {\n"+
					"            int half = (start+end)/2;\n"+
					"\n"+
					"            if (SCANNER_TABLE[half][0] == c)\n"+
					"                return SCANNER_TABLE[half][1];\n"+
					"            else if (SCANNER_TABLE[half][0] < c)\n"+
					"                start = half+1;\n"+
					"            else  //(SCANNER_TABLE[half][0] > c)\n"+
					"                end = half-1;\n"+
					"        }\n"+
					"\n"+
					"        return -1;\n"+
					"    }\n";
				break;
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
				"            case "+i+":\n"+
				"                switch (c)\n"+
				"                {\n");
				
					for (Map.Entry<Character, Integer> entry : m.entrySet() )
					{
						Character ch = entry.getKey();
						Integer it = entry.getValue();
						casesState.append(
				"                    case "+((int)ch.charValue())+": return "+it+";\n");
					}
				
					casesState.append(
				"                    default: return -1;\n"+
				"                }\n");
				}
				
				nextState = 
				"    private int nextState(char c, int state)\n"+
				"    {\n"+
				"        switch (state)\n"+
				"        {\n"+
				casesState.toString()+
				"            default: return -1;\n"+
				"        }\n"+
				"    }\n";
			}
				break;
			default:
				//nunca acontece
				nextState = null;
		}
		
		return 
		nextState+
		"\n"+
		"    private int tokenForState(int state)\n"+
		"    {\n"+
		"        if (state < 0 || state >= TOKEN_STATE.length)\n"+
		"            return -1;\n"+
		"\n"+
		"        return TOKEN_STATE[state];\n"+
		"    }\n"+
		"\n"+
		(lookup ?
		"    public int lookupToken(int base, String key)\n"+
		"    {\n"+
		"        int start = SPECIAL_CASES_INDEXES[base];\n"+
		"        int end   = SPECIAL_CASES_INDEXES[base+1]-1;\n"+
		"\n"+
		(sensitive?"":
		"        key = key.toUpperCase();\n"+
		"\n")+
		"        while (start <= end)\n"+
		"        {\n"+
		"            int half = (start+end)/2;\n"+
		"            int comp = SPECIAL_CASES_KEYS[half].compareTo(key);\n"+
		"\n"+
		"            if (comp == 0)\n"+
		"                return SPECIAL_CASES_VALUES[half];\n"+
		"            else if (comp < 0)\n"+
		"                start = half+1;\n"+
		"            else  //(comp > 0)\n"+
		"                end = half-1;\n"+
		"        }\n"+		
		"\n"+
		"        return base;\n"+
		"    }\n"+
		"\n":"")+
		"    private boolean hasInput()\n"+
		"    {\n"+
		"        return position < input.length();\n"+
		"    }\n"+
		"\n"+
		"    private char nextChar()\n"+
		"    {\n"+
		"        if (hasInput())\n"+
		"            return input.charAt(position++);\n"+
		"        else\n"+
		"            return (char) -1;\n"+
		"    }\n"+
		"";
	}
}
