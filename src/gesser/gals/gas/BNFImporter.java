package gesser.gals.gas;

import gesser.gals.Actions;
import gesser.gals.FileFilters;
import gesser.gals.GALS;
import gesser.gals.InputPane;
import gesser.gals.MainWindow;
import gesser.gals.InputPane.Data;
import gesser.gals.analyser.AnalysisError;
import gesser.gals.generator.Options;
import gesser.gals.generator.OptionsDialog;
import gesser.gals.generator.parser.Grammar;
import gesser.gals.util.GalsData;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class BNFImporter extends JDialog implements ActionListener
{
	private JButton prods = new JButton("Importar Produções para Projeto Atual");
	private JButton synt = new JButton("Gerar Analisador Sintático");
	private JButton full = new JButton("Gerar Analisador Léxico e Sintático");
	
	private int result;
	
	public BNFImporter() 
	{
		super(MainWindow.getInstance(), "Importar Gramática BNF", true);
		
		getContentPane().setLayout(new BorderLayout(10, 10));
		
		getContentPane().add(
			new JLabel("<html><center>Arquivo processado com sucesso.<br>O que você deseja fazer?</center></html>", SwingConstants.CENTER),
			BorderLayout.NORTH
			);
		
		JPanel btns = new JPanel(new GridLayout(0, 1, 5, 5));
		btns.add(prods);
		btns.add(synt);
		btns.add(full);
		getContentPane().add(btns);
		
		prods.addActionListener(this);
		synt.addActionListener(this);
		full.addActionListener(this);
		
		pack();
		setResizable(false);
        GALS.centralize(this);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	}
	
	public GalsData importGAS() throws FileNotFoundException, AnalysisError
	{
		if (!Actions.checkSaved())
			return null;
		
		Actions.FILE_CHOOSER.setFileFilter(FileFilters.BNF_FILTER);
		if (Actions.FILE_CHOOSER.showOpenDialog(MainWindow.getInstance()) == JFileChooser.APPROVE_OPTION)
		{
			File file = Actions.FILE_CHOOSER.getSelectedFile();
		
			GASScanner scanner = new GASScanner(new FileReader(file));
			GASParser  parser = new GASParser();
			GASTranslator translator = new GASTranslator(); 
		
			parser.parse(scanner, translator);
		
			Grammar g = translator.getGrammar();
		
			return getData(g);
		}
		else
			return null;
	}

	private GalsData getData(Grammar g)
	{
		setVisible(true);
		
		switch (result)
		{
			case 0: return productionsImported(g);
			case 1: return newParser(g);
			case 2: return newFull(g);
			default : return null;
		}		
	}
	
	private GalsData productionsImported(Grammar g)
	{
		Data data = MainWindow.getInstance().getData();
		
		data.setGrammar(g.toString());
		Options opts = OptionsDialog.getInstance().getOptions();
		
		if (! opts.generateParser)
		{
			opts.generateParser = true;
			StringBuffer bfrNT = new StringBuffer();
			String[] nt = g.getNonTerminals();
			for (int i=0; i<nt.length; i++)
			{
				bfrNT.append(nt[i]).append("\n");
			}
			data.setNonTerminals(bfrNT.toString());
		}
				
		Actions.setSaved(false);
				
		return new GalsData(opts, data);
	}

	private GalsData newParser(Grammar g)
	{
		Actions.reset();
		
		StringBuffer bfrT = new StringBuffer();
		String[] toks = g.getTerminals();
		for (int i=0; i<toks.length; i++)
		{
			bfrT.append(toks[i]).append("\n");
		}
		StringBuffer bfrNT = new StringBuffer();
		String[] nt = g.getNonTerminals();
		for (int i=0; i<nt.length; i++)
		{
			bfrNT.append(nt[i]).append("\n");
		}
		
		String gram = g.toString();
		InputPane.Data data = new InputPane.Data("", bfrT.toString(), bfrNT.toString(), gram);
		Options opts = new Options();
		opts.generateScanner = false;
		
		return new GalsData(opts, data);
	}
	
	private GalsData newFull(Grammar g)
	{
		Actions.reset();
		
		StringBuffer bfrNT = new StringBuffer();
		String[] nt = g.getNonTerminals();
		for (int i=0; i<nt.length; i++)
		{
			bfrNT.append(nt[i]).append("\n");
		}

		String gram = g.toString();
		InputPane.Data data = new InputPane.Data("", getTokens(g.getTerminals()), bfrNT.toString(), gram);
		Options opts = new Options();
		
		return new GalsData(opts, data);
	}
	
	private String getTokens(String[] t)
	{
		List<String> tokens = new ArrayList<String>(Arrays.asList(t));
		StringBuffer bfr = new StringBuffer();
		
		bfr.append("//operadores\n");	
		
		for (int i=0; i<tokens.size(); i++)
		{
			String tok = tokens.get(i);
			if (tok.charAt(0) == '"')
			{
				bfr.append(tok).append("\n");
				tokens.remove(i);
				i--;
			}
		}		 
		
		bfr.append("\n");
		
		bfr.append(KeyWordsSelector.process(tokens));
		
		return bfr.toString();
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == prods)
		{
			result = 0;			
			dispose();
		}
		else if (e.getSource() == synt)
		{
			result = 1;
			dispose();
		}
		else if (e.getSource() == full)
		{
			result = 2;
			dispose();
		}
	}
}
