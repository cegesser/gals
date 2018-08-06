package gesser.gals.scannerparser;

import gesser.gals.analyser.SemanticError;
import gesser.gals.generator.OptionsDialog;
import gesser.gals.generator.scanner.*;
import gesser.gals.util.IntegerSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static gesser.gals.scannerparser.Constants.*;
/**
 * @author Gesser
 */

public class FiniteAutomataGenerator
{
	private Map<String, Node> definitions = new HashMap<String, Node>();
	private Map<String, Node> expressions = new HashMap<String, Node>();
	private Map<Integer, Map<String, Integer>> specialCases = new HashMap<Integer, Map<String, Integer>>();
	private Node root = null;
	private IntegerSet alphabet = new IntegerSet();
	private int lastPosition = -1;
	private List<String> tokenList = new ArrayList<String>();
	private boolean sensitive = true; 
	
	private int contextCount = 0;
	
	public FiniteAutomataGenerator()
	{		
		sensitive = OptionsDialog.getInstance().getOptions().scannerCaseSensitive;
	}
	
	private IntegerSet[] next;
	private Node[] nodes;
	
	public void addDefinition(String id, Node root) throws SemanticError
	{
		if (definitions.containsKey(id))
			throw new SemanticError("Definição repetida: "+id);
		
		definitions.put(id, root);
		
		alphabet.addAll(root.getAlphabet());
	}
	
	public Node getDefinition(String id)
	{
		return definitions.get(id);
	}	
	
	public void addExpression(String id, Node root, boolean backtrack) throws SemanticError
	{
		/*
		if (tokenList.contains(id))
			throw new SemanticError("Token '"+id+"' já definido");
		*/	
		alphabet.addAll(root.getAlphabet());
		
		if (!tokenList.contains(id))
			tokenList.add(id);
		
		int pos = tokenList.indexOf(id);
		
		Node end = Node.createEndNode(pos+2, backtrack);
		root = Node.createConcatNode(root, end);
		
		Node ctx = root.getLeft().getRight();
		if (ctx != null)
		{
			ctx = ctx.deepestLeft();
			if (ctx != null && ctx.getContext() >= 0)
			{
				contextCount++;
				ctx.setContext(contextCount);
				end.setContext(contextCount);				
			}
		}
				
		expressions.put(id, root);
		
				
		if (this.root == null)
			this.root = root;
		else
		{
			this.root = Node.createUnionNode(this.root, root);
		}
	}
	
	public void addIgnore(Node root, boolean backtrack)
	{
		alphabet.addAll(root.getAlphabet());
	
		Node end = Node.createEndNode(0, backtrack);
		root = Node.createConcatNode(root, end);
					
		if (this.root == null)
			this.root = root;
		else
		{
			this.root = Node.createUnionNode(this.root, root);
		}
	}
	
	public void addSpecialCase(String id, String base, String value) throws SemanticError
	{			
		if (! sensitive)
			value = value.toUpperCase();
			
		if (!expressions.containsKey(base))
			throw new SemanticError("Token '"+base+"' não definido");
			
		int b = tokenList.indexOf(base)+2;
		
		if (tokenList.contains(id))
			throw new SemanticError("Token '"+id+"' já definido");
		
		Integer i = Integer.valueOf(tokenList.size()+2);
		
		Map<String, Integer> s = specialCases.get(Integer.valueOf(b));
		
		if (s == null)
		{
			s = new TreeMap<String, Integer>();
			specialCases.put(Integer.valueOf(b), s);
		}
		else if (s.get(value) != null)
			throw new SemanticError("Já houve a definição de um caso especial de '"+base+"' com o valor\""+value+"\"");
			
		s.put(value, i);
		
		tokenList.add(id);
	}
	
	public FiniteAutomata generateAutomata() throws SemanticError
	{
		List<IntegerSet> states = new ArrayList<IntegerSet>();
		Map<Integer, Integer> context = new TreeMap<Integer, Integer>();
		Map<Integer, Integer> ctxMap = new TreeMap<Integer, Integer>();
		Map<Integer, Map<Character, Integer>> trans = new TreeMap<Integer, Map<Character, Integer>>();
		Map<Integer, Integer> finals = new TreeMap<Integer, Integer>();
		Map<Integer, Boolean> back = new TreeMap<Integer, Boolean>();
		
		if (root == null)
			throw new SemanticError("A Especificação Léxica deve conter a definição de pelo menos um Token");
		
		computeNext();
		
		states.add(root.metaData.first);
		for (int i=0; i< states.size(); i++)
		{
			IntegerSet T = states.get(i);
			for (Integer x : alphabet)
			{
				char c = (char)x.intValue();
				
				IntegerSet U = new IntegerSet();

				for (Integer p : T)
				{
					Node n = nodes[p.intValue()];					
					if (n.getEnd() >= 0)					
					{
						Integer in = Integer.valueOf(i);
						if (!finals.containsKey(in))
						{
							finals.put(in, Integer.valueOf(n.getEnd()));
							back.put(in, new Boolean(n.doBackTrack()));
							
							if (n.getContext() > 0)
							{
								if (! context.containsKey(in))
									context.put(in, ctxMap.get(Integer.valueOf(n.getContext())));
							}
						}						
					}
					if (n.getContext() >= 0)
					{
						if (! ctxMap.containsKey(Integer.valueOf(n.getContext())))
							ctxMap.put(Integer.valueOf(n.getContext()), Integer.valueOf(i));
					}
						
					if (n.getAlphabet().contains(c))
						U.addAll(next[p.intValue()]);
					
				}
				
				int pos = -1;
				if (! U.isEmpty() )
				{ 
					pos = states.indexOf(U);
					if (pos == -1)
					{
						states.add(U);
						pos = states.size()-1;
					}						
				}		
				Integer I = Integer.valueOf(i);
				if (! trans.containsKey(I))
					trans.put(I, new TreeMap<Character, Integer>());
				if (pos != -1)
					trans.get( I ).put(Character.valueOf(c), Integer.valueOf(pos));			
			}			
		}
		
		return makeAtomata(states, trans, finals, back, context);
				
	}

	public FiniteAutomata makeAtomata(List<IntegerSet> states, Map<Integer, Map<Character, Integer>> trans, Map<Integer, Integer> finals, Map<Integer, Boolean> back, Map<Integer, Integer> context)
		throws SemanticError
	{
		List<Map<Character, Integer>> transitions = 
			new ArrayList<Map<Character, Integer>>(trans.values());
		
		int[] fin = new int[states.size()];
		for (int i=0; i<fin.length; i++)
		{
			Integer expr = finals.get(Integer.valueOf(i));
			if (expr != null)
				fin[i] = expr.intValue();
			else
				fin[i] = -1;							
		}
		
		for (int i=0; i<fin.length; i++)
		{			
			Boolean b = back.get(Integer.valueOf(i));
			if (b != null && b.booleanValue() == false)
			{
				IntegerSet pre = computPrecedersOf(i, transitions);
				for (Integer state : pre)
				{
					if (fin[state.intValue()] <0)
						fin[state.intValue()] = -2;
				}
			}							
		}
				
		List<FiniteAutomata.KeyValuePar> scList = new ArrayList<FiniteAutomata.KeyValuePar>();
		int[][] scIndexes = new int[tokenList.size()+2][];
		for (int i=0; i<scIndexes.length; i++)
		{
			Map<String, Integer> m = specialCases.get(Integer.valueOf(i));
			int start = scList.size();
			if (m != null)
			{
				for (Map.Entry<String, Integer> e : m.entrySet())
				{
					String k = e.getKey();
					Integer v = e.getValue();
					
					scList.add(new FiniteAutomata.KeyValuePar(k, v.intValue()));
				}
			}
			int end = scList.size();
			scIndexes[i] = new int[]{start, end};
		}
		FiniteAutomata.KeyValuePar[] sc = new FiniteAutomata.KeyValuePar[scList.size()];
		System.arraycopy(scList.toArray(), 0, sc, 0, sc.length);
		int[][] cont = new int[states.size()][2];
		for (int i=0; i<cont.length; i++)
		{
			cont[i][0] = 0;
			cont[i][1] = -1;
		}
		for (Map.Entry<Integer, Integer> entry : context.entrySet())
		{
			Integer key = entry.getKey();
			Integer value = entry.getValue();
			
			cont[value.intValue()][0] = 1;
			cont[key.intValue()][1] = value.intValue();
			
		}
		
		return  new FiniteAutomata(alphabet, transitions, fin, scIndexes, sc, cont, tokenList);		
	}
	
	private IntegerSet computPrecedersOf(int state, List<Map<Character, Integer>> transitions)
	{		
		IntegerSet result = new IntegerSet();	
		result.add(state);
		
		boolean contin;
		do
		{
			contin = false;
			for (Integer st : result)
gathering:	{
				for (int i=0; i<transitions.size(); i++)
				{
					for (Integer next : transitions.get(i).values() )
					{
						if (result.contains(next.intValue()) && next.equals(st))
						{
							if (! result.contains(i))
							{
								result.add(i); 
								contin = true;
								break gathering;
							}						
						}
					}
				}
			}
		}
		while (contin);
		
		return result;
	}

	public void computeNext()
	{
		computeMetaData(root);
		
		next = new IntegerSet[lastPosition+1];
		nodes = new Node[lastPosition+1];
				
		for (int i=0; i<next.length; i++)
		{
			next[i] = new IntegerSet();
		}
		
		computeNext(root);
	}
	
	private void computeMetaData(Node root)
	{					
		if (root.getLeft() != null)
			computeMetaData(root.getLeft());
			
		if (root.getRight() != null)
			computeMetaData(root.getRight());
			
		Node.MetaData n = root.metaData;
		Node l = root.getLeft();
		Node r = root.getRight();
		
		switch (root.getId())
		{
			case CHAR:
				lastPosition++;	
				
				n.position = lastPosition;
				n.nullable = false;
				n.first.add(lastPosition);
				n.last.add(lastPosition);				
				break;
			
			case OPTIONAL:
			case CLOSURE:				
				n.nullable = true;
				n.first.addAll(l.metaData.first);
				n.last.addAll(l.metaData.last);
				break;
				
			case CLOSURE_OB:				
				n.nullable = false;
				n.first.addAll(l.metaData.first);
				n.last.addAll(l.metaData.last);
				break;
			
			case UNION:				
				n.nullable = l.metaData.nullable || r.metaData.nullable;
				
				n.first.addAll(l.metaData.first);
				n.first.addAll(r.metaData.first);
				
				n.last.addAll(l.metaData.last);
				n.last.addAll(r.metaData.last);
				break;
				
			case -1://concat
				n.nullable = l.metaData.nullable && r.metaData.nullable;
		
				n.first.addAll(l.metaData.first);
				if (l.metaData.nullable)
					n.first.addAll(r.metaData.first);
				
				n.last.addAll(r.metaData.last);
				if (r.metaData.nullable)
					n.last.addAll(l.metaData.last);
				break;
		}
	}
	
	private void computeNext(Node root)
	{	
		switch (root.getId())
		{
			case -1: //concat
				for (Integer i : root.getLeft().metaData.last)
				{
					next[i.intValue()].addAll(root.getRight().metaData.first);
				}
				break;
			case CLOSURE:
			case CLOSURE_OB:
				for (Integer i : root.getLeft().metaData.last)
				{
					next[i.intValue()].addAll(root.getLeft().metaData.first);
				}
				break;
			case CHAR:
				nodes[root.metaData.position] = root;
				break;
		}
		
		if (root.getLeft() != null)
			computeNext(root.getLeft());
			
		if (root.getRight() != null)
			computeNext(root.getRight());
	}

	
	public String toString()
	{
		StringBuffer result = new StringBuffer();
		result.append(root);
		
		return result.toString();
	}
}
