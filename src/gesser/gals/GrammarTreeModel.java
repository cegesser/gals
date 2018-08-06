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

package gesser.gals;

import java.util.Collections;
import java.util.List;
import gesser.gals.ebnf.decl.ProductionDecl;
import gesser.gals.ebnf.decl.Sequence;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

class ProdHolder
{
	private ProductionDecl p;
	
	public ProdHolder(ProductionDecl p) 
	{
		this.p = p;
	}
	
	public ProductionDecl getP() 
	{
		return p;
	}
	
	public String toString() 
	{
		return p == null ? "..." : p.getLhs().toString();
	}
	
	public boolean equals(Object obj) 
	{
		if (obj == null || getClass() != obj.getClass())
			return false;
		else
		{
			ProductionDecl p2 = ((ProdHolder)obj).p;
			return p == p2 || (p != null && p2 != null && p2.getLhs().getLexeme().equals(p.getLhs().getLexeme()));
		}
	}
}

/**
 * @author gesser
 */

public class GrammarTreeModel extends  DefaultTreeModel
{
	private DefaultMutableTreeNode root = new DefaultMutableTreeNode();
	
	public GrammarTreeModel()
	{
		super(null);		
		setRoot(root);
	}
	
    public void setGrammar(List<ProductionDecl> g)
    {
    	int i;
    	for (i=0; i<g.size() && i<root.getChildCount(); i++)
    	{
    		ProductionDecl p = g.get(i);
    		
    		DefaultMutableTreeNode n = (DefaultMutableTreeNode) root.getChildAt(i);
    		n.setUserObject(new ProdHolder(p));
    		
    		nodesChanged(root, new int[]{i});
    		
    		adjust(p, n);
    		
    		reload(n);
    	}
    	if (i<root.getChildCount())
    	{
    		for ( ;i<root.getChildCount(); )
    			removeNodeFromParent((MutableTreeNode) root.getChildAt(i));
    	}
    	else if (i<g.size())
    	{
    		for ( ; i<g.size(); i++)
    		{
    			ProductionDecl p = g.get(i);
    			
    			DefaultMutableTreeNode x = new DefaultMutableTreeNode(new ProdHolder(p));
    			insertNodeInto(x, root, root.getChildCount());
    			
    			if (p != null)
	    			for (Sequence s : p.getRhs())
	    			{	    				
	    				DefaultMutableTreeNode sn = new DefaultMutableTreeNode(s.toString());
	    				
	    				insertNodeInto(sn, x, x.getChildCount());
	    			}
    		}
    	}
    }

	/**
	 * @param p
	 * @param n
	 */
	private void adjust(ProductionDecl p, DefaultMutableTreeNode n) 
	{
		int k=0;
		List<Sequence> rhs = p == null ? Collections.<Sequence>emptyList() : p.getRhs();
		for ( ; k < rhs.size() && k < n.getChildCount(); k++)
		{
			Sequence s = rhs.get(k);
			
			DefaultMutableTreeNode c = (DefaultMutableTreeNode) n.getChildAt(k);
			
			valueForPathChanged(new TreePath(c.getPath()), s);
		}
		if (k<n.getChildCount())
		{
			for ( ;k<n.getChildCount(); )
				removeNodeFromParent((MutableTreeNode) n.getChildAt(k));
		}
		else if (k<rhs.size())
		{
			for ( ; k<rhs.size(); k++)
			{
				DefaultMutableTreeNode x = new DefaultMutableTreeNode(rhs.get(k));
				insertNodeInto(x, n, n.getChildCount());				
			}
		}
	} 
}
