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

package gesser.gals.editor;

import gesser.gals.MainWindow;
import gesser.gals.analyser.AnalysisError;
import gesser.gals.ebnf.SymbolManager;
import gesser.gals.ebnf.decl.ProductionDecl;
import gesser.gals.ebnf.parser.Parser;
import gesser.gals.ebnf.parser.Scanner;
import gesser.gals.ebnf.parser.tokens.EofToken;
import gesser.gals.ebnf.parser.tokens.Token;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gesser
 */

public class ProcessorThread extends Thread
{    
	private transient boolean working = false;
	
    private SymbolManager sm = new SymbolManager();
    private Parser parser = new Parser();
    
    private List<List<Token>> tokens = new ArrayList<List<Token>>();
    private List<ProductionDecl> productions = new ArrayList<ProductionDecl>();
    private List<String> errors = new ArrayList<String>();
    
    
    public ProcessorThread()
    {
        start();        
    }
    
    public SymbolManager getSymbolManager()
    {
    	return sm;
    }
    
    public boolean isWorking()
    {
    	boolean result;
    	synchronized (this)
		{
    		result = working;
		}
    	return result;
    }
    
    public void awake()
    {
        synchronized (this)
        {
        	working = true;            
            notify();            
        }
    }
    
    public void stopWork()
    {
        synchronized (this)
        {
        	working = false;
        }
    }
    
    protected void updateErrors()
    {
    	EventQueue.invokeLater(new Runnable()
    	{
    		public void run() 
    		{
    			MainWindow.getInstance().setErrors(errors);
    	 	}
    	});
    }
    
    protected void updateTree()
    {
    	EventQueue.invokeLater(new Runnable()
    	{
    		public void run() 
    		{
    			MainWindow.getInstance().setTreeData(productions);
			}
    	});
    }
    
    protected void work()
    {   
    	sm.clear();
        productions.clear();
        errors.clear();
        
        updateErrors();
        
        String grammar = MainWindow.getInstance().getData().getGrammar();
                     
        tokens.clear();
        tokens.add(new ArrayList<Token>());
    	for (Token t : new Scanner(grammar))
    	{    
    		if (! isWorking())
    		    return;
    		
    	    tokens.get(tokens.size()-1).add(t);
    	    
    	    if (t.getLexeme().equals(";"))
    	    {
    	    	tokens.get(tokens.size()-1).add(new EofToken(t.getStart()+1));
    	    	tokens.add(new ArrayList<Token>());
    	    }
    	}
    	
    	if (tokens.size()>1 && tokens.get(tokens.size()-1).get(0) instanceof EofToken)
    	{
    		tokens.remove(tokens.size()-1);
    	}
    	 
        for (List<Token> lt : tokens)
        {
        	if (! isWorking())
    		    return;
        	
            try
            {
                List<ProductionDecl> n = parser.parse(lt, sm);
                productions.addAll(n);
            }
            catch (AnalysisError e)
            {
                errors.add(e.getMessage());
                productions.add(null);
            }
        }

        updateTree();
        updateErrors();
    }
    
    public void run()
    {
        while (true)
        {
            synchronized (this)
            {
                try
                {
                    wait();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }

            try
            {
            	work();            	
            }
            catch (Throwable t)
            {
            	t.printStackTrace();
            }
        }
    }
}
