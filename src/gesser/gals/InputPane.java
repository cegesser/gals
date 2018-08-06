package gesser.gals;

import gesser.gals.analyser.AnalysisError;
import gesser.gals.ebnf.EbnfGrammar;
import gesser.gals.ebnf.SymbolManager;
import gesser.gals.ebnf.decl.GrammarDecl;
import gesser.gals.ebnf.decl.ProductionDecl;
import gesser.gals.ebnf.parser.Parser;
import gesser.gals.ebnf.parser.Scanner;
import gesser.gals.ebnf.parser.tokens.Token;
import gesser.gals.editor.BNFDocument;
import gesser.gals.editor.DefinitionsDocument;
import gesser.gals.editor.TokensDocument;
import gesser.gals.generator.OptionsDialog;
import gesser.gals.generator.scanner.FiniteAutomata;
import gesser.gals.scannerparser.LineParser;
import gesser.gals.util.MetaException;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.text.StyledEditorKit;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import static gesser.gals.util.MetaException.Mode.*;
import static gesser.gals.InputPane.Mode.*;



/**
 * @author Gesser
 */

public class InputPane extends JPanel implements MouseListener, UndoableEditListener
{
	public enum Mode { LEXICAL, SYNTATIC, BOTH }
	
	private JEditorPane grammar      = new JEditorPane();
	private JEditorPane tokens       = new JEditorPane();
	private JEditorPane definitions  = new JEditorPane();
	
	private BNFDocument grammarDoc = new BNFDocument();
	
	private JPanel base = new JPanel(new BorderLayout());
	private JList errorList = new JList();
	
	private JPanel pnlGrammar 		= createPanel(" Gramática", grammar, grammarDoc);
	private JPanel pnlTokens 		= createPanel(" Tokens", tokens, new TokensDocument());
	private JPanel pnlDefinitions  = createPanel(" Definições Regulares", definitions, new DefinitionsDocument());
	
	private GrammarTreeModel model = new GrammarTreeModel();
	private JTree productions = new JTree(model);
	
	private Mode mode;
	
	public InputPane()
	{
		super (new BorderLayout());
		
		productions.setRootVisible(false);
		DefaultTreeCellRenderer r = new DefaultTreeCellRenderer();
		r.setLeafIcon(null);
		r.setClosedIcon(null);
		r.setOpenIcon(null);
		productions.setCellRenderer(r);
		productions.expandPath(new TreePath(new Object[]{productions.getModel().getRoot()}));
		productions.setExpandsSelectedPaths(true);
		productions.getModel().addTreeModelListener(new TreeModelListener()
		{	
		    public void treeNodesChanged(TreeModelEvent e) { }
		    public void treeNodesInserted(TreeModelEvent e)
            {	
		    	TreePath t = e.getTreePath();
		    	if (! productions.isExpanded(t))
		    		productions.expandPath(t);		    	
            }
		    public void treeNodesRemoved(TreeModelEvent e) { }
		    public void treeStructureChanged(TreeModelEvent e) { }
		});
		
		productions.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) 
			{
				if (productions.getSelectionPath() == null)
					return;
				
				DefaultMutableTreeNode n = (DefaultMutableTreeNode) productions.getSelectionPath().getLastPathComponent();
				if (n.getParent() != null)
				{
					if ( ! (n.getUserObject() instanceof ProdHolder))
						n = (DefaultMutableTreeNode) n.getParent();
					
					int position = ((ProdHolder)n.getUserObject()).getP().getLhs().getStart();
					
					grammar.setSelectionStart(position);
					grammar.setSelectionEnd(position);
					grammar.requestFocus();
					
					grammarDoc.updateHighlighted(((ProdHolder)n.getUserObject()).getP().getLhs().toString());
				}
			}
		});
			
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, base, new JScrollPane(errorList));
		split.setResizeWeight(0.9);
		add(split);
		
		setMode(Mode.BOTH);
		
		split.setDividerLocation(355);
		
		errorList.addMouseListener(this);
	}
	
	public EbnfGrammar getGrammar() throws MetaException
	{
		errorList.setListData(new Object[]{});
		
		if (mode == Mode.LEXICAL)
			return null;
		
		List<String> tokens = getTokens();
		
		SymbolManager sm = new SymbolManager();
		
		for (String t : tokens)
			if (t.trim().length() > 0)
				sm.createTerminal(t);
		
		
		try
		{
			List<Token> tkns = new ArrayList<Token>();
	    	for (Token t : new Scanner(grammar.getText()))
	    	{
	    		tkns.add(t);
	    	}
			GrammarDecl g = new GrammarDecl(new Parser().parse(tkns, sm), sm);
			return new EbnfGrammar(g);		
		}
		catch(AnalysisError e)
		{
			throw new MetaException(TOKEN, 0, e);
		}
	}
	
	public FiniteAutomata getFiniteAutomata() throws MetaException
	{
		errorList.setListData(new Object[]{});
		
		if (mode == Mode.SYNTATIC)
			return null;
		
		LineParser lp = new LineParser();
						
		return lp.parseFA(definitions.getText(), tokens.getText());
	}
	
	public void setTreeData(List<ProductionDecl> g)
	{
	    model.setGrammar(g);
	}
	
	public List<String> getTokens() throws MetaException
	{
		List<String> result = new ArrayList<String>();
		if (mode != SYNTATIC)
		{
			List<String> tokens = getFiniteAutomata().getTokens();
			for (int i=0; i<tokens.size(); i++)
			{
				result.add(tokens.get(i));
				result.add("\n");
			}
		}
		else 
		{
			StringTokenizer tknzr = new StringTokenizer(tokens.getText(), "\n", true);
			
			while (tknzr.hasMoreTokens())
				result.add(tknzr.nextToken());
		}
		return result;
	}
	
	public Data getData()
	{
		boolean lex = mode == Mode.LEXICAL || mode == Mode.BOTH;
		boolean synt = mode == Mode.SYNTATIC || mode == Mode.BOTH;
		
		return 
			new Data(
				lex ? definitions.getText() : "", 
				tokens.getText(),
				"", 
				synt ? grammar.getText() : "");
	}
	
	public void setData(Data d)
	{
		definitions.setText(d.getDefinitions());
		tokens.setText(d.getTokens());
		grammar.setText(d.getGrammar());
		
		definitions.setCaretPosition(0);
		tokens.setCaretPosition(0);
		grammar.setCaretPosition(0);
		
		MainWindow.getInstance().setChanged();
	}
	
	public void reset()
	{
		grammar.setText("");
		tokens.setText("");
		definitions.setText("");
		OptionsDialog.getInstance().reset();
		MainWindow.getInstance().setChanged();
	}
	
	public void setMode(Mode mode)
	{
		this.mode = mode;
		
		switch (mode)
		{
			case LEXICAL:  setLex(); break;
			case SYNTATIC: setSynt(); break;
			case BOTH:     setBoth(); break;
		}
	}
	
	private void setBoth()
	{
		base.removeAll();
		
		JSplitPane top = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pnlDefinitions, pnlTokens);
		
		top.setResizeWeight(0.25);
		
		JSplitPane bottom = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(productions), pnlGrammar);
		
		bottom.setResizeWeight(0.2);
		
		JSplitPane main = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
		
		main.setResizeWeight(0.5);
		
		base.add(main);
		
		validate();
		repaint();
		
		top.setDividerLocation(0.25);
		main.setDividerLocation(0.5);
	}

	private void setSynt()
	{
		base.removeAll();
		
		JSplitPane bottom = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(productions), pnlGrammar);
		
		bottom.setResizeWeight(0.2);
		
		JSplitPane main = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pnlTokens, bottom);
		
		main.setResizeWeight(0.15);
		
		base.add(main);
		
		validate();
		repaint();
		
		main.setDividerLocation(0.15);
	}

	
	private void setLex()
	{
		base.removeAll();
		
		JSplitPane main = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pnlDefinitions, pnlTokens);
		
		main.setResizeWeight(0.25);
		
		base.add(main);
		
		validate();
		repaint();
		
		main.setDividerLocation(0.25);
	}
	
	public void undoableEditHappened(UndoableEditEvent e)
	{
		Actions.UNDO_MAN.addEdit(e.getEdit());
		Actions.setSaved(false);
		MainWindow.getInstance().setChanged();
	}
				
	private JPanel createPanel(String caption, final JEditorPane comp, Document doc)
	{
		JPanel pnl = new JPanel(new BorderLayout());
		
		comp.setEditorKit(new StyledEditorKit());
		comp.setDocument(doc);
		
		comp.getDocument().addUndoableEditListener(this);
		comp.getKeymap().addActionForKeyStroke((KeyStroke)Actions.undo.getValue(Action.ACCELERATOR_KEY), Actions.undo);
		comp.getKeymap().addActionForKeyStroke((KeyStroke)Actions.redo.getValue(Action.ACCELERATOR_KEY), Actions.redo);
					
		pnl.add(new JLabel(caption), BorderLayout.NORTH);
		
		JPanel tmp = new JPanel(new BorderLayout());
		tmp.add(comp);
		
		JScrollPane scroll = new JScrollPane(tmp);
		scroll.getVerticalScrollBar().setUnitIncrement(10);
		scroll.getHorizontalScrollBar().setUnitIncrement(10);
		pnl.add(scroll);
		
		return pnl;
	}
	
	
	private static class ErrorData
	{
		int index, position;
		MetaException.Mode mode;
		String message;
		
		ErrorData(String message, int index, int position, MetaException.Mode mode)
		{
			this.message = message;
			this.index = index;
			this.position = position;
			this.mode = mode;
		}
		
		public String toString()
		{
			return message /*+ ", linha: "+index+", coluna "+position*/;
		}
	}
	public void handleError(MetaException e)
	{			
		AnalysisError ae = (AnalysisError) e.getCause();
		int line = e.getIndex();
		String msg = "";
		switch (e.getMode())
		{
			case DEFINITION :
				msg = "Erro em Definição Regular: ";
				break;
			case TOKEN :
				msg = "Erro na Especificação de Tokens: ";
				break;
			case GRAMMAR :
				msg = "Erro na Especificação da Gramática: ";
				break;
		}
		msg += ae.getMessage();
		
		ErrorData ed = new ErrorData(msg, line, ae.getPosition(), e.getMode());
		
		ErrorData[] errors = {ed};
		
		errorList.setListData(errors);
		e.printStackTrace();
		Toolkit.getDefaultToolkit().beep();
		mouseClicked(null);
	}
	
	public void setErrors(List err)
	{
	    errorList.setListData(err.toArray());
	}
	
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}

	public void mouseClicked(MouseEvent e)
	{
		ErrorData error = (ErrorData) errorList.getSelectedValue();

		if (error != null)
		{
			switch (error.mode)
			{
				case DEFINITION:
					setPosition(definitions, error.index, error.position);
					definitions.requestFocus();
					break;
				case TOKEN :
					setPosition(tokens, error.index, error.position);
					tokens.requestFocus();
					break;
				case GRAMMAR :
					grammar.getCaret().setDot(error.position);
					grammar.requestFocus();
					break;
			}
		}
	}
	
	private void setPosition(JEditorPane pane, int line, int col )
	{
		String text = pane.getText();
		int pos = 0;
		int strpos = 0;
		while (line>0)
		{
			while (strpos < text.length() && text.charAt(strpos) != '\n')
			{				
				if (text.charAt(strpos) != '\r')
					pos++;
				strpos++;				
			}
			strpos++;
			pos++;
			line--;
		}
		pos += col;
		pane.setCaretPosition(pos);
	}

	public static class Data
	{
		private String definitions = "";
		private String tokens = "";
		private String nonTerminals = "";
		private String grammar = "";
		
		public Data(String definitions, String tokens, String nonTerminals, String grammar)
		{
			this.definitions = definitions;
			this.tokens = tokens;
			this.nonTerminals = nonTerminals;
			this.grammar = grammar;
		}
		
		public String getDefinitions()
		{
			return definitions;
		}

		public String getGrammar()
		{
			return grammar;
		}

		public String getNonTerminals()
		{
			return nonTerminals;
		}

		public String getTokens()
		{
			return tokens;
		}
		
		public void setGrammar(String string)
		{
			this.grammar = string;
		}

		public void setNonTerminals(String string)
		{
			this.nonTerminals = string;
		}
	}
}
