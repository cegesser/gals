package gesser.gals.gas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gesser.gals.analyser.SemanticError;
import gesser.gals.analyser.Token;
import gesser.gals.generator.parser.Grammar;
import gesser.gals.generator.parser.Production;
import gesser.gals.util.IntList;

public class GASTranslator implements Constants
{
	private Token token = null;
	private List<String> terminals = new ArrayList<String>();
	private List<String> nonTerminals = new ArrayList<String>();
	private List<Production> prodList = new ArrayList<Production>();
	private Map<String, Integer> symbols = new HashMap<String, Integer>();
	private int start;
	private int lhs;
	private IntList rhs; 

	public Grammar getGrammar()
	{
		return new Grammar(terminals, nonTerminals, prodList, start);
	}

    public void executeAction(int action, Token currentToken)	throws SemanticError
    {
        token = currentToken;
        
        switch (action)
        {
        	case 1: action1(); break;
			case 2: action2(); break;
			case 3: action3(); break;
			case 4: action4(); break;
			case 5: action5(); break;
			case 6: action6(); break;
			case 7: action7(); break;
			case 8: action8(); break;
			case 9: action9(); break;
			case 10: action10(); break;
        }
    }	
    
	private void action1()
	{
		//#1 - salva nome gramatica
		
		terminals.clear();
		nonTerminals.clear();
		prodList.clear();
		symbols.clear();
	}
	
	private void action2() throws SemanticError
	{
		//#2 - add NT
		
		String nt = translateNT(token.getLexeme());
		
		if (nonTerminals.contains(nt))
			throw new SemanticError("Símbolo não-terminal "+nt+" já declarado", token.getPosition());
			
		nonTerminals.add(nt);
	}
	
	private void action3() throws SemanticError
	{
		//#3 - add T
		
		String t = translateToken(token.getLexeme());

		if (terminals.contains(t))
			throw new SemanticError("Símbolo terminal "+t+" já declarado", token.getPosition());
		
		terminals.add(t);
	}
	
	private void action4() throws SemanticError
	{
		//#4 - seta o simbolo inicial
		
		symbols.put("î", Integer.valueOf(0));
		symbols.put("$", Integer.valueOf(1));
		
		int value = 2;
		for (String t : terminals)
		{
			symbols.put(t, Integer.valueOf(value));
			value++;
		}
		for (String nt : nonTerminals)
		{
			symbols.put(nt, Integer.valueOf(value));
			value++;
		}
		
		Integer itg = symbols.get(translateNT(token.getLexeme()));
		if (itg == null)
			throw new SemanticError("Símbolo "+token.getLexeme()+" não declarado", token.getPosition());
		 
		start = itg.intValue();
		
		if (start < 2+terminals.size())
			throw new SemanticError("Símbolo inicial deve ser um símbolo não terminal", token.getPosition());
	}
	
	private void action5() throws SemanticError
	{
		//#5 - cria uma produçao, e seta o lado esquerdo
		
		Integer itg = symbols.get(translateNT(token.getLexeme()));
		if (itg == null)
			throw new SemanticError("Símbolo "+token.getLexeme()+" não declarado", token.getPosition());
 
		lhs = itg.intValue();

		if (lhs < 2+terminals.size())
			throw new SemanticError("Símbolo ao lado esquerdo de uma produção deve ser um símbolo não terminal", token.getPosition());
		
		rhs = new IntList();
	}
	
	private void action6()
	{
		//#6 - termina a produçao e adiciona
		
		prodList.add(new Production(null, lhs, rhs));
	}
	
	private void action7()
	{
		//#7 - cria uma produçao e seta o lado esquerdo = ao da ultima produçao
		
		rhs = new IntList();
	}
		
	private void action8()
	{
		//#8 - adiciona uma açao semantica à produçao
		
		String action = token.getLexeme();
		
		action = action.substring(1);
		
		rhs.add(symbols.size() + Integer.parseInt(action));
	}
	
	private void action9() throws SemanticError
	{
		//#9 - adiciona um NT à produçao
		
		Integer itg = symbols.get(translateNT(token.getLexeme()));
		if (itg == null)
			throw new SemanticError("Símbolo "+token.getLexeme()+" não declarado", token.getPosition());
 
		rhs.add(itg.intValue());
	}
	
	private void action10() throws SemanticError
	{
		//#10- adiciona um T à produçao
		
		Integer itg = symbols.get(translateToken(token.getLexeme()));
		if (itg == null)
			throw new SemanticError("Símbolo "+token.getLexeme()+" não declarado", token.getPosition());
 
		rhs.add(itg.intValue());
	}
	
	private String translateNT(String nt)
	{
		StringBuffer bfr = new StringBuffer();
			
		bfr.append('<');

		for (int i=1; i<nt.length()-1; i++)
		{
			char c = nt.charAt(i);
			if (Character.isLetterOrDigit(c))
				bfr.append(c);
			else
				bfr.append('_');
		}			
		bfr.append('>');

		return bfr.toString();
	}
	
	private String translateToken(String tok)
	{
		if (tok.charAt(0) == '"')
			return tok;
		else if (tok.charAt(0) != '\'')
		{
			StringBuffer bfr = new StringBuffer();
			
			for (int i=0; i<tok.length(); i++)
			{
				char c = tok.charAt(i);
				if (Character.isLetterOrDigit(c))
					bfr.append(c);
				else
					bfr.append('_');
			}			

			return bfr.toString();
		}
		else
		{
			StringBuffer bfr = new StringBuffer();
			
			bfr.append('"');
			
			for (int i=1; i<tok.length()-1; i++)
			{
				char c = tok.charAt(i);
				bfr.append(c);
				if (c == '"')
					bfr.append('"');
			}			
			bfr.append('"');
			
			return bfr.toString();
		}
	}
}
