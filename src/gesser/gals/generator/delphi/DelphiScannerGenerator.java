package gesser.gals.generator.delphi;

import gesser.gals.generator.Options;
import gesser.gals.generator.scanner.FiniteAutomata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static gesser.gals.generator.Options.Input.*;
import static gesser.gals.generator.Options.ScannerTable.*;
/**
 * @author Carlos E. Gesser
 */

public class DelphiScannerGenerator
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
		
		result.put("U"+classname+".pas", scanner);
		
		return result;
	}

	private String buildScanner(FiniteAutomata fa, Options options)
	{
		String classname = options.scannerName;
		
		String inType;
		String inInit;
		String inDef;
		String inUses;
		if(options.input == STREAM)
		{
			inType = "TStream";
			inInit = 
			"var\n"+
			"    strStream: TStringStream;\n"+
			"begin\n"+
			"    strStream := TStringStream.Create('');\n"+
			"\n"+
			"    if input <>  nil then\n"+
			"        strStream.CopyFrom(input, input.Size);\n"+
			"\n"+
			"    self.input := strStream.DataString;\n"+
			"    setPosition(1);\n"+
			"    setEnd(Length(self.input));\n"+
			"\n"+
			"    strStream.Destroy;\n"+
			"end;\n"+
			"";
			inDef = "setInput(nil);";
			inUses = ", classes";
		}
		else if(options.input == STRING)
		{
			inType = "string";
			inInit = 
				"begin\n"+
				"    self.input := input;\n"+
				"    setPosition(1);\n"+
				"    setEnd(Length(input));\n"+
				"end;\n";
			inDef = "setInput('');";
			inUses = "";
		}
		else
		{
			//nunca acontece
			inType = "";
			inInit = "";
			inDef  = "";
			inUses = "";
		}
		
		return 
			"unit U"+classname+";\n"+
			"\n"+
			"interface\n"+
			"\n"+
			"uses UToken, ULexicalError, UConstants"+inUses+", SysUtils;\n"+
			"\n"+
			"type\n"+
			"    T"+classname+" = class\n"+
			"    public\n"+
			"        constructor create; overload;\n"+
			"        constructor create(input : "+inType+"); overload;\n"+
			"\n"+
			"        procedure setInput(input : "+inType+");\n"+
			"        procedure setPosition(pos : integer);\n"+
			"        procedure setEnd(endPos : integer);\n"+
			"        function nextToken : TToken; //raises ELexicalError\n"+
			"\n"+
			"    private\n"+
			"        input : string;\n"+
			"        position : integer;\n"+
			"        endPos : integer;\n"+
			"\n"+
			"        function nextState(c : char; state : integer) : integer;\n"+
			"        function tokenForState(state : integer) : integer;\n"+
			(lookup ?
			"        function lookupToken(base : integer; key : string) : integer;\n":"")+
			"\n"+
			"        function hasInput : boolean;\n"+
			"        function nextChar : char;\n"+
			"    end;\n"+
			"\n"+
			"implementation\n"+
			"\n"+
			"constructor T"+classname+".create;\n"+
			"begin\n"+
			"    "+inDef+"\n"+
			"end;\n"+
			"\n"+
			"constructor T"+classname+".create(input : "+inType+");\n"+
			"begin\n"+
			"    setInput(input);\n"+
			"end;\n"+
			"\n"+
			"procedure T"+classname+".setInput(input : "+inType+");\n"+
			inInit+
			"\n"+
			"function T"+classname+".nextToken : TToken;\n"+
			"var\n"+
			"    start,\n"+
			"    oldState,\n"+
			"    state,\n"+
			"    endState,\n"+
			"    endPos,\n"+
			(fa.hasContext() ?
			"    ctxtState;\n"+
			"    ctxtEnd;\n" : "")+
			"    token : integer;\n"+
			"    lexeme : string;\n"+
			"begin\n"+
			"    if not hasInput then\n"+
			"        result := nil\n"+
			"    else\n"+
			"    begin\n"+
			"        start := position;\n"+
			"\n"+
			"        state := 0;\n"+
			"        oldState := 0;\n"+
			"        endState := -1;\n"+
			"        endPos := -1;\n"+
			(fa.hasContext() ?
			"        ctxtState := -1;\n"+
			"        ctxtEnd := -1;\n" : "")+
			"\n"+
			"        while hasInput do\n"+
			"        begin\n"+
			"            oldState := state;\n"+
			"            state := nextState(nextChar, state);\n"+
			"\n"+
			"            if state < 0 then\n"+
			"                break\n"+
			"\n"+
			"            else\n"+
			"            begin\n"+
			"                if tokenForState(state) >= 0 then\n"+
			"                begin\n"+
			"                    endState := state;\n"+
			"                    endPos := position;\n"+
			"                end;\n"+
			(fa.hasContext() ? 
			"                if SCANNER_CONTEXT[state][0] = 1 then\n" +
			"                begin\n" +
			"                    ctxtState := state;\n" +
			"                    ctxtEnd := position;\n" +
			"                end\n" : "")+
			"            end;\n"+
			"        end;\n"+
			"        if (endState < 0) or ( (endState <> state) and (tokenForState(oldState) = -2) ) then\n"+
			"            raise ELexicalError.create(SCANNER_ERROR[oldState], start);\n"+
			"\n"+
			(fa.hasContext() ? 
			"        if (ctxtState <> -1) and (SCANNER_CONTEXT[endState][1] = ctxtState) then\n"+
			"            endPos := ctxtEnd;\n"+
			"\n" : "" )+
			"        position := endPos;\n"+
			"\n"+
			"        token := tokenForState(endState);\n"+
			"\n"+
			"        if token = 0 then\n"+
			"            result := nextToken\n"+
			"        else\n"+
			"        begin\n"+
			"            lexeme := Copy(input, start, endPos-start);\n"+
			(lookup ?
			"            token  := lookupToken(token, lexeme);\n" : "")+
			"            result := TToken.create(token, lexeme, start);\n"+
			"        end;\n"+
			"    end;\n"+
			"end;\n"+
			"\n"+
			"procedure T"+classname+".setPosition(pos : integer);\n"+
			"begin\n"+
			"    position := pos;\n"+
			"end;\n"+
			"\n"+
			"procedure T"+classname+".setEnd(endPos : integer);\n"+
			"begin\n"+
			"    self.endPos := endPos;\n"+
			"end;\n"+
			"\n"+
			"function T"+classname+".nextState(c : char; state : integer) : integer;\n"+
			"begin\n"+
			nextStateImpl(fa, options)+
			"end;\n"+
			"\n"+
			"function T"+classname+".tokenForState(state : integer) : integer;\n"+
			"begin\n"+
			"    if (state >= 0) and (state < STATES_COUNT) then\n"+
			"        result := TOKEN_STATE[state]\n"+
			"    else\n"+
			"        result := -1;\n"+
			"end;\n"+
			"\n"+
			(lookup ? 
			"function T"+classname+".lookupToken(base : integer; key : string) : integer;\n"+
			"var\n"+
			"    start, end_, half : integer;\n"+
			"    str : string;\n"+
			"begin\n"+			
			"    result := base;\n"+
			"\n"+
			"    start := SPECIAL_CASES_INDEXES[base];\n"+
			"    end_  := SPECIAL_CASES_INDEXES[base+1]-1;\n"+			
			"\n"+
			(sensitive?"":
			"    key := UpperCase(key);\n"+
			"\n")+
			"    while start <= end_ do\n"+
			"    begin\n"+
			"        half := (start+end_) div 2;\n"+
			"        str := SPECIAL_CASES_KEYS[half];\n"+
			"\n"+
			"        if str = key then\n"+
			"        begin\n"+
			"            result := SPECIAL_CASES_VALUES[half];\n"+
			"            break;\n"+
			"        end\n"+
			"        else if str < key then\n"+
			"            start := half+1\n"+
			"        else  //str > key\n"+
			"            end_ := half-1;\n"+
			"    end;\n"+
			"end;\n"+
			"\n" : "" )+
			"function T"+classname+".hasInput : boolean;\n"+
			"begin\n"+
			"    result := position <= endPos;\n"+
			"end;\n"+
			"\n"+
			"function T"+classname+".nextChar : char;\n"+
			"begin\n"+
			"    if hasInput then\n"+
			"    begin\n"+
			"        result := input[position];\n"+
			"        position := position + 1;\n"+
			"    end\n"+
			"    else\n"+
			"        result := char(0);\n"+
			"end;\n"+
			"\n"+
			"end.\n"+
			"";
	}

	private String nextStateImpl(FiniteAutomata fa, Options opt)
	{
		switch (opt.scannerTable)
		{
			case FULL:
			case COMPACT:
				return "    result := SCANNER_TABLE[state][c];\n";
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
				"        "+i+": case integer(c) of\n");

					for ( Map.Entry<Character, Integer> entry : m.entrySet() )
					{
						Character ch = entry.getKey();
						Integer it = entry.getValue();
						casesState.append(
				"            "+((int)ch.charValue())+": result := "+it+";\n");
					}

					casesState.append(
				"            else result := -1;\n"+
				"        end;\n");
				}

				return 
				"    case state of\n"+
				casesState.toString()+
				"        else result := -1;\n"+
				"    end;\n";
			}
			default:
				return null;
		}
	}


	private String buildEmptyScanner(Options options)
	{
		String classname = options.scannerName;
		
		return 
			"unit U"+classname+";\n"+
			"\n"+
			"interface\n"+
			"\n"+
			"uses UToken, ULexicalError;\n"+
			"\n"+
			"type\n"+
			"    T"+classname+" = class\n"+
			"    public\n"+
			"        function nextToken : TToken; //raises ELexicalError\n"+
			"    end;\n"+
			"\n"+
			"implementation\n"+
			"\n"+
			"function T"+classname+".nextToken : TToken;\n"+
			"begin\n"+
			"    result := nil;\n"+
			"end;\n"+
			"\n"+
			"end.\n"+
			"";
	}
}
