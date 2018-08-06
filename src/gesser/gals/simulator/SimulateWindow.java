package gesser.gals.simulator;

import gesser.gals.GALS;
import gesser.gals.MainWindow;
import gesser.gals.analyser.*;
import gesser.gals.analyser.LexicalError;
import gesser.gals.analyser.Token;
import gesser.gals.editor.SyntaxDocument;
import gesser.gals.generator.parser.Grammar;
import gesser.gals.generator.parser.ll.*;
import gesser.gals.generator.parser.lr.*;
import gesser.gals.generator.scanner.FiniteAutomata;
import gesser.gals.generator.scanner.LexicalData;
import gesser.gals.util.MetaException;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

/**
 * @author Gesser
 */

public class SimulateWindow extends JDialog implements ActionListener
{
	private JButton lex = new JButton("Simular Lexico");
	private JButton synt = new JButton("Simular Sintático");
	private JButton close = new JButton("Fechar");
	
	private JTextArea ta = new JTextArea();

	private TokenTableModel tokensModel = new TokenTableModel();
	private JTree tokensTree = new JTree();
	
	private JPanel inputPanel = new JPanel(new BorderLayout());
	private JPanel tokensPanel = new JPanel(new BorderLayout());
	private JPanel treePanel = new JPanel(new BorderLayout());
	
	private JSplitPane split;
	
	//private Font def = SyntaxDocument.FONT;
	
	private static SimulateWindow instance;
	
	public static SimulateWindow getInstance()
	{
		if (instance == null)
			instance = new SimulateWindow();
			
		return instance;
	}
	
	private SimulateWindow()
	{
		super(MainWindow.getInstance(), "Testar Analisador", true);
		
		JComponent c = (JComponent) getContentPane();
		
		c.setLayout(new BorderLayout(10, 10));
		c.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
		inputPanel.add(new JScrollPane(ta));
		//ta.setFont(def);
		
		JTable tbl = new JTable(tokensModel);
		tbl.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tokensPanel.add(new JScrollPane(tbl));	
		{
			tbl.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			    public void valueChanged(ListSelectionEvent e) {
			        //Ignore extra messages.
			        if (e.getValueIsAdjusting()) return;
			        
			        ListSelectionModel lsm =
			            (ListSelectionModel)e.getSource();
			        if (! lsm.isSelectionEmpty()) 
			        {
			        	ta.requestFocus();
			            int selectedRow = lsm.getMinSelectionIndex();
			            Token t = tokensModel.getToken(selectedRow);
			            ta.getCaret().setDot(t.getPosition());
			            if (t.getId() != -1)
				            ta.getCaret().moveDot(t.getPosition() + t.getLexeme().length());
			        }
			    }
			});
		}	
		
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		renderer.setLeafIcon(null);
		renderer.setOpenIcon(null);
		renderer.setClosedIcon(null);
		tokensTree.setCellRenderer(renderer);

		treePanel.add(new JScrollPane(tokensTree));
		
		JPanel btns = new JPanel(new GridLayout(1, 0, 10, 10));
		
		btns.add(lex);
		btns.add(synt);
		btns.add(close);
		
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputPanel, tokensPanel);
		split.setResizeWeight(0.5);
		c.add(split);
		c.add(btns, BorderLayout.SOUTH);
		
		lex.addActionListener(this);
		synt.addActionListener(this);
		close.addActionListener(this);
		
		pack();
		setSize(450, 350);
        GALS.centralize(this);
	}	
	
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == lex)
			lexClick();
			
		else if (e.getSource() == synt)
			syntClick();
			
		else if (e.getSource() == close)
			closeClick();
	}
	
	BasicScanner faSim = null;
	LL1ParserSimulator ll1Sim = null;
	LRParserSimulator lrSim = null;
	List<String> tokenNameList;
	
	public void simulateLL(FiniteAutomata fa, Grammar g, List<String> tokenNameList) throws NotLLException
	{	
		lex.setEnabled(fa != null);
		synt.setEnabled(g != null);
		
		this.tokenNameList = tokenNameList;
		
		if (fa != null)
		{
			faSim = new FiniteAutomataSimulator(fa);
		}
		else
		{
			faSim = new FiniteAutomataSimulator(generateTokenListAutomata());
		}
			
		if (g != null)
		{
			LLParser ll1 = new LLParser(g);
			ll1Sim = new LL1ParserSimulator(ll1);
			lrSim = null;
		}
		
		setVisible(true);
	}
	
	public void simulateSLR(FiniteAutomata fa, Grammar g, List<String> tokenNameList)
	{	
		lex.setEnabled(fa != null);
		synt.setEnabled(g != null);
		
		this.tokenNameList = tokenNameList;
		
		if (fa != null)
		{
			faSim = new FiniteAutomataSimulator(fa);
		}
		else
		{
			faSim = new FiniteAutomataSimulator(generateTokenListAutomata());
		}
			
		if (g != null)
		{
			LRGenerator parser = LRGeneratorFactory.createGenerator(g);
			lrSim = new LRParserSimulator(parser);
			ll1Sim = null;			
		}
		
		setVisible(true);
	}

	private void lexClick()
	{	
		split.setRightComponent(tokensPanel);		
		validate();
		
		tokensModel.clear();
		
		faSim.setInput(ta.getText());
		try
		{
			Token t = faSim.nextToken();
			
			while (t != null)
			{
				String name = tokenNameList.get(t.getId() - 2);
				tokensModel.add(t, name);
		
				t = faSim.nextToken();
			}
		}
		catch (LexicalError e)
		{
			Token dummy = new Token(-1, e.getMessage(), e.getPosition());
		
			tokensModel.add(dummy, "ERRO LÉXICO");
			e.printStackTrace();
		}
	}
	
	private void syntClick()
	{
		split.setRightComponent(treePanel);	

		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Derivação");
		tokensTree.setModel(new DefaultTreeModel(root));
		validate();

		faSim.setInput(ta.getText());
		
		try
		{
			if (ll1Sim != null)
			{		
				ll1Sim.parse(faSim, root);
			}		
			else if (lrSim != null)
			{
				lrSim.parse(faSim, root);			
			}
		}
		catch(AnalysisError e)
		{
			e.printStackTrace();
		}
		tokensTree.expandRow(0);		
	}
	
	private void closeClick()
	{
		setVisible(false);
	}
	
	private FiniteAutomata generateTokenListAutomata()
	{
		try
		{
			LexicalData ld = new LexicalData();
			for (int i=0; i<tokenNameList.size(); i++)
			{
				String token = tokenNameList.get(i);
				ld.addToken(token, token);
			}
			ld.addIgnore("[\\ \\n\\r\\t]");
			return ld.getFA();
		}
		catch (MetaException e)
		{
			e.printStackTrace();
			return null;
		}
	}
}

class TokenTableModel extends AbstractTableModel
{
	private List<Token> tokens = new ArrayList<Token>();
	private List<String> tokenNames = new ArrayList<String>();
	
	public void clear()
	{
		tokens.clear();
		tokenNames.clear();
		fireTableDataChanged();
	}
	
	public void add(Token token, String tokenName)
	{
		tokens.add(token);
		tokenNames.add(tokenName);
		fireTableRowsInserted(tokens.size()-1, tokens.size()-1);
	}
	
	public Token getToken(int index)
	{
		return tokens.get(index);
	}
	
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		switch (columnIndex)
		{
			case 0:
				return tokenNames.get(rowIndex);
			case 1:
				return tokens.get(rowIndex).getLexeme();
			case 2:
				return Integer.valueOf(tokens.get(rowIndex).getPosition());
			default:
				return null;
		}
	}

	public int getColumnCount()
	{
		return 3;
	}

	public int getRowCount()
	{
		return tokens.size();
	}

	public String getColumnName(int column)
	{
		switch (column)
		{
			case 0:
				return "Token";
			case 1:
				return "Lexema";
			case 2:
				return "Posição";
			default:
				return null;
		}
	}

	public Class<?> getColumnClass(int columnIndex)
	{
		switch (columnIndex)
		{
			case 0:
				return String.class;
			case 1:
				return String.class;
			case 2:
				return Integer.class;
			default:
				return null;
		}
	}	
}
