package gesser.gals;

import gesser.gals.ebnf.EbnfGrammar;
import gesser.gals.ebnf.decl.ProductionDecl;
import gesser.gals.generator.Options;
import gesser.gals.generator.OptionsDialog;
import gesser.gals.generator.parser.Grammar;
import gesser.gals.generator.scanner.FiniteAutomata;
import gesser.gals.util.GalsData;
import gesser.gals.util.MetaException;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.*;

public class MainWindow extends JFrame 
{
	private static final ImageIcon GALS = new ImageIcon(ClassLoader.getSystemResource("icons/gals.gif"));
	
	private static MainWindow instance = new MainWindow();
	
	public static MainWindow getInstance()
	{
		return instance;
	}
		
	JToolBar toolbar = new JToolBar();
		
	private JMenuBar menuBar = new JMenuBar();
	private JMenu grammar = new JMenu("Verificar");
	private JMenuItem lexTable = new JMenuItem(Actions.viewLexTable);
    private JMenuItem syntTable = new JMenuItem(Actions.showTable);
    private JMenuItem ff = new JMenuItem(Actions.ff);
	private JMenuItem useless = new JMenuItem(Actions.useless);
    private JMenuItem itemSet = new JMenuItem(Actions.showItemSet);
	
	private InputPane inPane = new InputPane();
	
	private boolean needRebuildFA = true;
	private boolean needRebuildGram = true;	
	private FiniteAutomata fa = null;
	private EbnfGrammar gram = null;
	
	public void setChanged()
	{
		needRebuildFA = true;
		needRebuildGram = true;
	}
	
	private MainWindow()
	{
		super ("GALS - Gerador de Analisadores Léxicos e Sintáticos");
		
		setIconImage(GALS.getImage());
		
		createMenu();
		createToolBar();
	
		getContentPane().add(toolbar, BorderLayout.NORTH);
		getContentPane().add(inPane);
     
     
        
        pack();
        setSize(600,500);
        
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter(){
        	public void windowClosing(WindowEvent e)
			{
				Actions.close.actionPerformed(null);
			}
        });
        
		grammar.setVisible(false);
	}
	
	public EbnfGrammar getEbnfGrammar() throws MetaException
    {
    	if (needRebuildGram || gram == null)
    	{
    		gram = inPane.getGrammar();
    		needRebuildGram = false;
    	}
    	return gram;
    }
	
	public Grammar getGrammar() throws MetaException
    {
    	return new Grammar(inPane.getGrammar());
    }
    
    public FiniteAutomata getFiniteAutomata() throws MetaException 
    {    	
    	if (needRebuildFA || fa == null)
    	{
    		fa = inPane.getFiniteAutomata();
    		needRebuildFA = false;
    	}
    	return fa;
    }
        
    public Options getOptions()
    {
    	return OptionsDialog.getInstance().getOptions();
    }
    
	public void updateData(GalsData lsd)
	{
		reset();
		
		if (lsd.getOptions() != null)
		{
			OptionsDialog.getInstance().setOptions(lsd.getOptions());
			setPanesMode(OptionsDialog.getInstance().getMode());
		}
			
		inPane.setData(lsd.getData());
	}
	
	public InputPane.Data getData()
	{
		return inPane.getData();
	}
	
	public void setData(InputPane.Data data)
	{
		inPane.setData(data);
	}

	public void reset()
	{
		inPane.reset();	
		setPanesMode(OptionsDialog.getInstance().getMode());	
	}
	
	public List<String> getTokens() throws MetaException
	{
		List<String> tokens = inPane.getTokens();
		
		while ( tokens.remove("\n") )
			;
			
		return tokens;
	}
	
	public void handleError(MetaException e)
	{
		inPane.handleError(e);
	}
	
	private void createMenu()
    {	
    	JMenu file = new JMenu("Arquivo");
    	
    	file.add(new JMenuItem(Actions.new_));
    	file.add(new JMenuItem(Actions.load));
    	file.addSeparator();
    	file.add(new JMenuItem(Actions.save));
    	file.add(new JMenuItem(Actions.saveAs));
    	file.addSeparator();		
		file.add(new JMenuItem(Actions.importGAS));
		file.addSeparator();
    	file.add(new JMenuItem(Actions.close));
    	
    	JMenu tools = new JMenu("Ferramentas");

    	tools.add(new JMenuItem(Actions.verify));
		tools.add(new JMenuItem(Actions.genCode));
		tools.addSeparator();
		tools.add(new JMenuItem(Actions.simulate));
    	tools.add(grammar);
		tools.add(useless);
		tools.addSeparator();
		tools.add(new JMenuItem(Actions.options));
    	
    	grammar.add(new JMenuItem(Actions.factored));
    	grammar.add(new JMenuItem(Actions.recursion));
    	grammar.add(new JMenuItem(Actions.condition3));
    	
    	JMenu doc = new JMenu("Documentação");
    	
    	doc.add(lexTable);
    	doc.add(syntTable); //Sintatico - LL(1)
		doc.add(itemSet);     //Sintatico - LR(1)
    	doc.add(ff);     //Sintatico    	
    	
    	/*
    	JMenu transform = new JMenu("Transformações");
    	
    	transform.add(new JMenuItem(Actions.undo));
    	Actions.undo.setEnabled(false);
    	transform.addSeparator();
    	transform.add(Actions.factorate);
    	transform.add(Actions.removeRecursion);
    	transform.add(Actions.removeUnitary);
    	transform.add(Actions.removeUseless);
    	transform.add(Actions.removeEpsilon);*/

		JMenu help = new JMenu("Ajuda");
    	
		help.add(Actions.doc);
    	help.add(Actions.about);

		menuBar.add(file);
		//menuBar.add(transform);
		menuBar.add(tools);
		menuBar.add(doc);
		menuBar.add(help);

        setJMenuBar(menuBar);
    }
    
	private void createToolBar()
	{
		toolbar.setFloatable(false);
		
		toolbar.add(Actions.new_);
		toolbar.add(Actions.load);
		toolbar.add(Actions.save);
		
		toolbar.addSeparator();
		
		toolbar.add(Actions.verify);
		
		toolbar.addSeparator();
		
		toolbar.add(Actions.simulate);
		toolbar.add(Actions.genCode);
		
		toolbar.addSeparator();
		
		toolbar.add(Actions.options);
	}
    
	public void setPanesMode(InputPane.Mode mode)
	{
		inPane.setMode(mode);
		
		boolean lex  = mode == InputPane.Mode.LEXICAL || mode == InputPane.Mode.BOTH;
		boolean synt = mode == InputPane.Mode.SYNTATIC || mode == InputPane.Mode.BOTH;
		Options.Parser type = OptionsDialog.getInstance().getOptions().parser;
		boolean ll = type == Options.Parser.LL || type == Options.Parser.RD;
		
		lexTable.setVisible(lex);
		
		syntTable.setVisible(synt);
		ff.setVisible(synt);
		grammar.setVisible(synt && ll);
		
		itemSet.setVisible( synt && ! ll);
	}
	
	public void setErrors(List err)
	{
	    inPane.setErrors(err);
	}
	
	public void setTreeData(List<ProductionDecl> g)
	{
	    inPane.setTreeData(g);
	}
}

