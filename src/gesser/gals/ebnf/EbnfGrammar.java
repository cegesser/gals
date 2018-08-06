/*
 * Copyright 2004 Carlos Eduardo Gesser
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, you can get it at http://www.gnu.org/licenses/gpl.txt
 */

package gesser.gals.ebnf;

import gesser.gals.ebnf.decl.ComplexItem;
import gesser.gals.ebnf.decl.GrammarDecl;
import gesser.gals.ebnf.decl.Item;
import gesser.gals.ebnf.decl.ProductionDecl;
import gesser.gals.ebnf.decl.Sequence;
import gesser.gals.ebnf.decl.SymbolItem;

import static gesser.gals.ebnf.decl.Item.Cardinality.*;
import gesser.gals.util.IntList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Carlos Gesser
 */
public class EbnfGrammar
{
	private List<Production> productions = new ArrayList<Production>();
	private SymbolManager sm;
	
	public EbnfGrammar(GrammarDecl decl)
	{		
		sm = decl.getSymbolTokenMapping();
		
		for (ProductionDecl p : decl.getProductions())
		{
			NonTerminal lhs = sm.getNonTerminal(p.getLhs().getLexeme());
			for (Sequence list : p.getRhs())
			{
				Production np = new Production(lhs, simplify(sm, list, this));
				if (! productions.contains(np))
					productions.add(np);
			}
		}
		
		setNonTerminalsProductions();		
		computeFirst();
		computeFollow();
	}
	
	private void setNonTerminalsProductions() 
	{
		for (Production p : productions)
		{
			p.getLhs().productions.add(p);
		}
		
		for (NonTerminal nt : sm.getNonTerminals())
		{
			nt.productions = Collections.unmodifiableSet(nt.productions);
		}
	}

	private void computeFirst() 
	{
		boolean changed = true;
		while (changed)
		{
			changed = false;
			for (Production p : productions)
			{
				Set<Terminal> fp = firstOf(p.getRhs());
				Set <Terminal> f = p.getLhs().first;
				changed = f.addAll( fp ) || changed;
			}
		}
		
		for (NonTerminal nt : sm.getNonTerminals())
		{
			nt.first = Collections.unmodifiableSet(nt.first);
		}
	}
	
	private void computeFollow() 
	{	
		sm.getFirstNonTerminal().follow.add(SymbolManager.DOLLAR);
		
		boolean changed = true;
		while (changed)
		{
			changed = false;
			for (Production p : productions)
			{
				List<Symbol> rhs = p.getRhs();
				for (int i=0; i<rhs.size(); i++)
				{
					if (rhs.get(i) instanceof NonTerminal)
					{
						NonTerminal nt = (NonTerminal) rhs.get(i);
						
						Set<Terminal> fp = firstOf( rhs.subList(i+1, rhs.size()) );
						if (fp.contains(SymbolManager.EPSILON))
						{
							fp = new HashSet<Terminal>(fp);
							fp.remove(SymbolManager.EPSILON);
							
							changed = nt.follow.addAll( p.getLhs().follow ) || changed;
						}
						changed = nt.follow.addAll( fp ) || changed;
					}
				}				
			}
		}
		
		for (NonTerminal nt : sm.getNonTerminals())
		{
			nt.follow = Collections.unmodifiableSet(nt.follow);
		}
	}
	
	public Set<Terminal> firstOf(List<Symbol> str)
	{
		if (str.isEmpty())
			return Collections.singleton(SymbolManager.EPSILON);
		else
		{
			Set<Terminal> f = str.get(0).getFirst();
			
			if (! f.contains(SymbolManager.EPSILON))
				return f;
			else
			{
				Set<Terminal> result = new HashSet<Terminal>(f);
				
				for (Symbol s : str.subList(1, str.size()))
				{
					f = s.getFirst();
					result.addAll(f);
					if (! f.contains(SymbolManager.EPSILON))
						break;
				}
				
				return result;
			}
		}
	}

	public SymbolManager getSymbolManager()
	{
		return sm;
	}
	
	public List<Production> getProductions() 
	{
		return Collections.unmodifiableList(productions);
	}
	
	public boolean isFactored()
    {
		for (NonTerminal nt : sm.getNonTerminals())
		{
			Set<Production> checked = new HashSet<Production>();
			for (Production p1 : nt.getProductions())
			{
				checked.add(p1);
				for (Production p2 : nt.getProductions())
				{
					if (! checked.contains(p2))
					{	
						Set<Terminal> inter = 
							intersection(firstOf(p1.getRhs()), firstOf(p2.getRhs()));
	                    if (! inter.isEmpty())
	                        return false;
					}
				}
			}
		}
        return true;
    }
	

	private static <T> Set<T> intersection(Set<T> a, Set<T> b)
	{
		Set<T> inter = new HashSet<T>(a);
		inter.retainAll(b);
		return inter;
	}
	
	private static List<Symbol>	simplify(SymbolManager sm, Sequence seq, EbnfGrammar g)
	{
		List<Symbol> newItems = new ArrayList<Symbol>();
		
		for (Item item : seq.getItems())
		{
			Symbol s;
			if (item.getCardinality() == ONCE && item instanceof SymbolItem)
			{
				SymbolItem si = (SymbolItem) item;
				s = sm.get(si.getSymbol().getLexeme());
			}
			else 
			{
				s = breakComplex( sm, item, g );
			}
			newItems.add(s);			
		}
		
		return newItems;
	}
			
	private static NonTerminal breakComplex(SymbolManager sm, Item item, EbnfGrammar g)
	{
		NonTerminal lhs = sm.createFakeNonTerminal(item.getLexeme());
		Item.Cardinality cardinality = item.getCardinality();
		
		List<List<Symbol>> generated = new ArrayList<List<Symbol>>(); 
		
		if (item instanceof ComplexItem)
		{
			ComplexItem ci = (ComplexItem) item;
			for (Sequence s : ci.getItems())
			{
				generated.add(simplify( sm, s, g));
			}
		}
		else
		{
			item = new SymbolItem(((SymbolItem)item).getSymbol(), ONCE);
		    
			List<Symbol> rhs = new ArrayList<Symbol>();
			rhs.add( sm.get(item.getLexeme()) );
			generated.add(rhs);
		}
	
		switch (cardinality)
		{
			case OPTIONAL:
			{
				generated.add(Collections.<Symbol>emptyList());
				break;
			}
			case ZERO_OR_MORE:
			{
				for (List<Symbol> sl : generated)
				{
					sl.add(lhs);			
				}
				generated.add(Collections.<Symbol>emptyList());
				break;
			}
			case ONE_OR_MORE:
			{
				Item i = item.copy(ZERO_OR_MORE);
				
				NonTerminal nt = breakComplex(sm, i, g);
				for (List<Symbol> sl : generated)
				{
					sl.add(nt);			
				}
				break;
			}
		}
		
		for (List<Symbol> rhs : generated)
		{
			Production np = new Production(lhs, rhs);
			if (! g.productions.contains(np))
				g.productions.add(np);
		}
		
		return lhs;
	}

	public String toString() 
	{
		StringBuilder b = new StringBuilder();
		
		for (Production p : productions)
			b.append(p).append("\n");
		
		return b.toString();
	}
}
