package gesser.gals.generator.csharp;



import gesser.gals.generator.Options;

import gesser.gals.generator.scanner.FiniteAutomata;



import java.util.HashMap;

import java.util.List;

import java.util.Map;



import static gesser.gals.generator.Options.Input.*;

import static gesser.gals.generator.Options.ScannerTable.*;

/**
 * 
 * @author Gustavo
 * @see gesser.gals.generator.java.JavaScannerGenerator
 */

public class CSharpScannerGenerator

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

		
		StringBuffer scannerBuffer = new StringBuffer();
		
		scannerBuffer.append(scanner);
		
		CSharpCommonGenerator.colocarNamespace(scannerBuffer, options);
		
		scannerBuffer.insert(0, "\n");
		
		scannerBuffer.insert(0, CSharpCommonGenerator.emitStaticImport(options, "ScannerConstants"));
		scannerBuffer.insert(0, CSharpCommonGenerator.emitStaticImport(options, "ParserConstants"));
		scannerBuffer.insert(0, CSharpCommonGenerator.emitStaticImport(options, "Constants"));
		scannerBuffer.insert(0, "using System.IO;\n");
		scannerBuffer.insert(0, "using System;\n");
		
		
		result.put(classname+".cs", scannerBuffer.toString());

		

		return result;

	}

	private String buildEmptyScanner(Options options)

	{

		StringBuffer result = new StringBuffer();

		String cls = 

		"    public class " + options.scannerName + "\n"+

		"    {\n"+

		"        public Token NextToken()\n"+

		"        {\n"+

		"            return null;\n"+

		"        }\n"+

		"    }\n"+

		"";

		result.append(cls);

		CSharpCommonGenerator.colocarNamespace(result, options);
		
		result.insert(0, CSharpCommonGenerator.emitStaticImport(options, "Constants"));

		return result.toString();

	}

	private String buildScanner(FiniteAutomata fa, Options options)
	
	{
		
		String cls = 

		"    public class "+options.scannerName+"\n"+

		"    {\n";
		if (options.input == STRING)
		{
			cls +=    "        private int _position = 0;\n"
					+ "        \n"
					+ "        private string _input;\n"
					+ "        public string Input \n"
					+ "        {\n"
					+ "            get => _input;\n"
					+ "            set \n"
					+ "            { \n"
					+ "                _input = value; \n"
					+ "                _position = 0;\n"
					+ "            }\n"
					+ "        }\n"
					+ "\n"
					+ "        public " + options.scannerName + "() : this(\"\") { }\n"
					+ "\n"
					+ "        public " + options.scannerName + "(string input) => Input = input;\n"
					+ "\n"
					+ "        private char NextChar()\n"
					+ "        {\n"
					+ "            if (HasInput())\n"
					+ "                return Input[_position++];\n"
					+ "            else\n"
					+ "                return char.MaxValue;\n"
					+ "        }\n"
					+ "\n"
					+ "        private char PeekNextChar()\n"
					+ "\n"
					+ "        {\n"
					+ "            return Input[_position];\n"
					+ "        }\n"
					+ "\n"
					+ "        private bool HasInput()\n"
					+ "        {\n"
					+ "            return _position < Input.Length;\n"
					+ "        }\n"
					+ "\n";			
		}
		else
		{
			cls +=    "        private int _position = 0;\n"
					+ "\n"
					+ "        private StreamReader _streamReader;\n"
					+ "\n"
					+ "        private Stream _input;\n"
					+ "        public Stream Input\n"
					+ "        {\n"
					+ "            get => _input;\n"
					+ "            set\n"
					+ "            {\n"
					+ "                _streamReader?.Dispose();\n"
					+ "\n"
					+ "                _streamReader = null;\n"
					+ "                _input = value;\n"
					+ "                if (_input != null)\n"
					+ "                {\n"
					+ "                    _input.Position = 0;\n"
					+ "                    _streamReader = new StreamReader(_input);\n"
					+ "                }\n"
					+ "            }\n"
					+ "        }\n"
					+ "\n"
					+ "        public " + options.scannerName + "() : this(null) { }\n"
					+ "\n"
					+ "        public " + options.scannerName + "(Stream input) => Input = input;\n"
					+ "\n"
					+ "        private char NextChar()\n"
					+ "        {\n"
					+ "            _position++;\n"
					+ "            return (char)_streamReader.Read();\n"
					+ "        }\n"
					+ "\n"
					+ "        private char PeekNextChar()\n"
					+ "        {\n"
					+ "            return (char)_streamReader.Peek();\n"
					+ "        }\n"
					+ "\n"
					+ "        private bool HasInput()\n"
					+ "        {\n"
					+ "            return !_streamReader.EndOfStream;\n"
					+ "        }"
					+ "\n";
		}
		cls +=
	
		"\n"+

		mainDriver(fa, options)+

		"\n"+

		auxFuncions(fa, options)+

		

		"    }\n"+

		"";

		

		return cls;

	}

	private String mainDriver(FiniteAutomata fa, Options options)

	{

		return 

		"        public Token NextToken()\n"+

		"        {\n"+

		"            if ( ! HasInput() )\n"+

		"                return null;\n"+

		"\n"+		

		"            int start = _position;\n"+

		"\n"+		

		"            int state = 0;\n"+

		"            int lastState = 0;\n"+

		"            int endState = -1;\n"+
		
		"            string lexeme = \"\";\n"+

		(fa.hasContext() ?

		"            int ctxtState = -1;\n"+

		"            int ctxtEnd = -1;\n" : "")+

		"\n"+

		"            while (HasInput())\n"+

		"            {\n"+

		"                lastState = state;\n"+

		"                state = NextState(PeekNextChar(), state);\n"+

		"\n"+

		"                if (state < 0)\n"+

		"                    break;\n"+

		"\n"+

		"                else\n"+

		"                {\n"+
		
		"                    lexeme += NextChar();\n"+

		"                    if (TokenForState(state) >= 0)\n"+

		"                    {\n"+

		"                        endState = state;\n"+

		"                    }\n"+

		(fa.hasContext() ? 

		"                    if (SCANNER_CONTEXT[state][0] == 1)\n" +
		"                    {\n" +
		"                        ctxtState = state;\n" +
		"                        ctxtEnd = _position;\n" +
		"                    }\n" : "")+

		"                }\n"+

		"            }\n"+

		"            if (endState < 0 || (endState != state && TokenForState(lastState) == -2))\n"+

		"                throw new LexicalError(SCANNER_ERROR[lastState], start);\n"+

		"\n"+

		(fa.hasContext() ? 

		"            if (ctxtState != -1 && SCANNER_CONTEXT[endState][1] == ctxtState)\n"+
		"                end = ctxtEnd;\n"+

		"\n" : "" )+

		"\n"+

		"            int token = TokenForState(endState);\n"+

		"\n"+

		"            if (token == 0)\n"+

		"                return NextToken();\n"+

		"            else\n"+

		"            {\n"+

		(lookup ?

		"                token = LookupToken(token, lexeme);\n" : "")+

		"                return new Token(token, lexeme, start);\n"+

		"            }\n"+

		"        }\n"+

		"";

	}

	private String auxFuncions(FiniteAutomata fa, Options options)

	{		 

		String nextState;

		

		switch (options.scannerTable)

		{

			case FULL:

				nextState =

					"        private int NextState(char c, int state)\n"+

					"        {\n"+

					"            int next = SCANNER_TABLE[state][c];\n"+

					"            return next;\n"+

					"        }\n";

				break;

			case COMPACT:

				nextState =

					

					"        private int NextState(char c, int state)\n"+

					"        {\n"+

					"            int start = SCANNER_TABLE_INDEXES[state];\n"+

					"            int end   = SCANNER_TABLE_INDEXES[state+1]-1;\n"+

					"\n"+

					"            while (start <= end)\n"+

					"            {\n"+

					"                int half = (start+end)/2;\n"+

					"\n"+

					"                if (SCANNER_TABLE[half][0] == c)\n"+

					"                    return SCANNER_TABLE[half][1];\n"+

					"                else if (SCANNER_TABLE[half][0] < c)\n"+

					"                    start = half+1;\n"+

					"                else  //(SCANNER_TABLE[half][0] > c)\n"+

					"                    end = half-1;\n"+

					"            }\n"+

					"\n"+

					"            return -1;\n"+

					"        }\n";

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

				"                switch ((byte)c)\n"+

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

				"    private int NextState(char c, int state)\n"+

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

		"        private int TokenForState(int state)\n"+

		"        {\n"+

		"            if (state < 0 || state >= TOKEN_STATE.Length)\n"+

		"                return -1;\n"+

		"\n"+

		"            return TOKEN_STATE[state];\n"+

		"        }\n"+

		"\n"+

		(lookup ?

		"        public int LookupToken(int base, string key)\n"+

		"        {\n"+

		"            int start = SPECIAL_CASES_INDEXES[base];\n"+

		"            int end   = SPECIAL_CASES_INDEXES[base+1]-1;\n"+

		"\n"+

		(sensitive?"":

		"            key = key.ToUpper();\n"+

		"\n")+

		"            while (start <= end)\n"+

		"            {\n"+

		"                int half = (start+end)/2;\n"+

		"                int comp = SPECIAL_CASES_KEYS[half].CompareTo(key);\n"+

		"\n"+

		"                if (comp == 0)\n"+

		"                    return SPECIAL_CASES_VALUES[half];\n"+

		"                else if (comp < 0)\n"+

		"                    start = half+1;\n"+

		"                else  //(comp > 0)\n"+

		"                    end = half-1;\n"+

		"            }\n"+		

		"\n"+

		"            return base;\n"+

		"        }\n"+

		"\n":"")+

		"\n";

	}

}

