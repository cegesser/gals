package gesser.gals;

import gesser.gals.analyser.AnalysisError;
import gesser.gals.ebnf.EbnfGrammar;
import gesser.gals.gas.BNFImporter;
import gesser.gals.generator.Options;
import gesser.gals.generator.OptionsDialog;
import gesser.gals.generator.cpp.*;
import gesser.gals.generator.java.*;
import gesser.gals.generator.parser.*;
import gesser.gals.generator.parser.ll.*;
import gesser.gals.generator.parser.lr.*;
import gesser.gals.generator.delphi.*;
import gesser.gals.generator.scanner.FiniteAutomata;
import gesser.gals.simulator.SimulateWindow;
import gesser.gals.util.*;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.undo.UndoManager;

import static gesser.gals.generator.Options.Parser.*;
import static gesser.gals.generator.Options.Language.*;

public class Actions
{
	private static abstract class ToolTipedAction extends AbstractAction
	{
		public ToolTipedAction(String name, Icon icon, String tooltip)
		{
			super(name, icon);
			putValue(SHORT_DESCRIPTION, tooltip);
		}
		
		public ToolTipedAction(String name, Icon icon)
		{
			this(name, icon, name);
		}
	}
	
	private static final Icon NEW = new ImageIcon(ClassLoader.getSystemResource("icons/new.gif"));
	private static final Icon OPEN = new ImageIcon(ClassLoader.getSystemResource("icons/open.gif"));
	private static final Icon SAVE = new ImageIcon(ClassLoader.getSystemResource("icons/save.gif"));	
	private static final Icon VERIFY = new ImageIcon(ClassLoader.getSystemResource("icons/verify.gif"));
	private static final Icon SIMULATOR = new ImageIcon(ClassLoader.getSystemResource("icons/simulator.gif"));
	private static final Icon GENERATOR = new ImageIcon(ClassLoader.getSystemResource("icons/generator.gif"));
	private static final Icon OPTIONS = new ImageIcon(ClassLoader.getSystemResource("icons/options.gif"));
	private static final Icon UNDO = new ImageIcon(ClassLoader.getSystemResource("icons/undo.gif"));
	private static final Icon REDO = new ImageIcon(ClassLoader.getSystemResource("icons/redo.gif"));
	
	public static final JFileChooser FILE_CHOOSER = new JFileChooser();
		
	private static boolean saved = false;
	private static boolean changed = false;
	private static File file = null;
	
	public static void setSaved(boolean s)
	{
		if (s)
			UNDO_MAN.discardAllEdits();
			
		saved = s;
		changed = !s;		
	}
	
	public static boolean checkSaved()
	{
		if (! saved && changed)
		{
			switch (JOptionPane.showConfirmDialog(MainWindow.getInstance(),"Salvar Alterações?"))
			{
				case JOptionPane.YES_OPTION:
					save.actionPerformed(null);
					break;
				case JOptionPane.NO_OPTION:
					return true;
				case JOptionPane.CANCEL_OPTION:
					return false;	
				default:
					return false;				
			}
		}
		return true;
	}
	
	public static final Action close = new AbstractAction("Fechar") 
	{
		public void actionPerformed(ActionEvent e) 
		{
			if (checkSaved())
        		System.exit(0);
		}		
	};
	
	public static final Action about = new AbstractAction("Sobre")
	{
		public void actionPerformed(ActionEvent e) 
		{
			String msg = 
				"G.A.L.S.\n"+
				"Gerador de Analisadores\n"+
				"Léxicos e Sintáticos (Versão 2003.10.03)\n"+
				"\n"+
				"Carlos Eduardo Gesser\n"+
				"gals.sourceforge.net";
        	JOptionPane.showMessageDialog(MainWindow.getInstance(),msg);
		}		
	};
	
	public static final Action doc = new AbstractAction("Documentação")
	{
		public void actionPerformed(ActionEvent e) 
		{
			URL url = ClassLoader.getSystemResource("help.html");
			HTMLDialog.getInstance().show("Documentação", url);
		}		
	};
	
	public static final Action options = new ToolTipedAction("Opções", OPTIONS)
	{
		public void actionPerformed(ActionEvent e) 
		{
			OptionsDialog.getInstance().setVisible(true);
		}		
	};
	
	public static final Action save = new ToolTipedAction("Salvar", SAVE)
	{
		public void actionPerformed(ActionEvent e) 
		{
			if (file == null/*! saved*/)
				saveAs.actionPerformed(e);
			else
			{
                try
                {
                	InputPane.Data inData = MainWindow.getInstance().getData();
                    GalsData data = new GalsData(MainWindow.getInstance().getOptions(), inData);
                    
                    OutputStream os = new FileOutputStream(file);
                    XMLProcessor.store(data, os);                    
                    setSaved(true);
                }
                catch (FileNotFoundException e1)              
                {
                	e1.printStackTrace();
                }
			}
		}		
	};
	
	public static final Action saveAs = new AbstractAction("Salvar Como...")
	{
		public void actionPerformed(ActionEvent e) 
		{
            FILE_CHOOSER.setFileFilter(FileFilters.GALS_FILTER);
            if (FILE_CHOOSER.showSaveDialog(MainWindow.getInstance()) == JFileChooser.APPROVE_OPTION)
            {
            	file = FILE_CHOOSER.getSelectedFile();
            	String name = file.getPath();
            	if (name.length() < 5 || !name.substring(name.length()-5).equals(".gals"))
            	{
            		name = name+".gals";
            		file = new File(name);
            	}                    	
            	save.actionPerformed(e);
            }
    	}		
	};
	
	public static final Action new_ = new ToolTipedAction("Novo", NEW, "Criar Novo Arquivo")
	{
		public void actionPerformed(ActionEvent e) 
		{
			if (checkSaved())
			{
				reset();
				options.actionPerformed(e);
			}
		}					
	};
	
	public static void reset()
	{
		MainWindow.getInstance().reset();
		saved = false;
		changed = false;
		file = null;
		UNDO_MAN.discardAllEdits();
	}	
	
	public static final Action load = new ToolTipedAction("Abrir...", OPEN, "Abrir Arquivo")
	{
		public void actionPerformed(ActionEvent e) 
		{
			if (checkSaved())
			{
            	String msg;
                try
                {
                	FILE_CHOOSER.setFileFilter(FileFilters.GALS_FILTER);
                    if (FILE_CHOOSER.showOpenDialog(MainWindow.getInstance()) == JFileChooser.APPROVE_OPTION)
                    {
                    	file = FILE_CHOOSER.getSelectedFile();
                    	InputStream is = new FileInputStream(file);
                    	GalsData lsd = XMLProcessor.load(is);
                    	MainWindow.getInstance().updateData(lsd);
                    	setSaved(true);
                    }
                }
                catch (XMLParsingException e1)
                {    
                	e1.printStackTrace();            	
                    msg = "Arquivo inválido!!!";
                    JOptionPane.showMessageDialog(MainWindow.getInstance(),msg);
                }    
                catch (IOException e1)
                {
                	e1.printStackTrace();
                }
			}
		}		
	};
	
	public static final Action showTable = new AbstractAction("Tabela de Análise Sintática")
	{
		public void actionPerformed(ActionEvent e) 
		{
        	try
            {
            	EbnfGrammar g = MainWindow.getInstance().getEbnfGrammar();

				switch (OptionsDialog.getInstance().getOptions().parser)
				{
					case RD:
					case LL:
						LLParser llg = new LLParser(g);
						HTMLDialog.getInstance().show("Tabela de Análise LL(1)", llg.tableAsHTML());
						break;
					case SLR:
					case LALR:
					case LR:
            	//		LRGenerator parser = LRGeneratorFactory.createGenerator(g);
         			//	HTMLDialog.getInstance().show("Tabela de Análise SLR(1)", parser.tableAsHTML());
            			break;
				}
			} 
            catch(MetaException e1)
            {
            	MainWindow.getInstance().handleError(e1);
            }       
			catch (NotLLException e1)
			{
				String msg = "Esta gramática não é LL(1): "+e1.getMessage();
				JOptionPane.showMessageDialog(MainWindow.getInstance(),msg);
				e1.printStackTrace();
			}       
		}		
	};
	
	public static final Action simulate = new ToolTipedAction("Simulador", SIMULATOR)
	{
		public void actionPerformed(ActionEvent e) 
		{	
            try
            {
            	FiniteAutomata fa = MainWindow.getInstance().getFiniteAutomata();
            	
            	Grammar g = MainWindow.getInstance().getGrammar();
            	
            	List<String> terminals = MainWindow.getInstance().getTokens();
				
				switch (OptionsDialog.getInstance().getOptions().parser)
				{
					case RD:
					case LL:
						SimulateWindow.getInstance().simulateLL(fa, g,  terminals);
						break;
					case SLR:
					case LALR:
					case LR:
						SimulateWindow.getInstance().simulateSLR(fa, g,  terminals);
						break;
				}				
            }
            catch(MetaException e1)
            {
            	MainWindow.getInstance().handleError(e1);
            }
            catch (NotLLException e1)
			{
				String msg = "Esta gramática não é LL(1): "+e1.getMessage();
				JOptionPane.showMessageDialog(MainWindow.getInstance(),msg);
				e1.printStackTrace();
			}            
		}		
	};
	
	public static final Action factored = new AbstractAction("Fatoração")
	{
		public void actionPerformed(ActionEvent e) 
		{        	
            try
            {
            	String msg;
            	
                Grammar g = MainWindow.getInstance().getGrammar();
                IntegerSet bs = g.getNonFactoratedProductions();
                if (bs.size() == 0)
                    msg = "Está fatorada";
                else
                {
                    StringBuffer bfr = new StringBuffer();
                    bfr.append("As produções\n");
                    for (Integer i : bs)
                    {
                        bfr.append(i).append(": ")
                            .append(g.getProductions().get(i.intValue()))
                            .append('\n');
                    }
                    bfr.append("Não estão fatoradas");
                    msg = bfr.toString();
                }
				JOptionPane.showMessageDialog(MainWindow.getInstance(),msg);
            }   
            catch(MetaException e1)
            {
            	MainWindow.getInstance().handleError(e1);
            }                  
		}		
	};
	
	public static final Action recursion = new AbstractAction("Recursão à Esquerda")
	{
		public void actionPerformed(ActionEvent e) 
		{        	
            try
            {
            	String msg;
            	
                Grammar g = MainWindow.getInstance().getGrammar(); 
                int s = g.getLeftRecursiveSimbol();
                if (s == -1)
                    msg = "NÃO possui recursão";
                else
                    msg = "Foi detectada recursão à esquerda (direta ou indireta)\n" +                        " em produções iniciadas por \""+g.getSymbols()[s]+"\"";
                    /*                   
                if (g.hasLeftRecursion())
                    msg = "Possui recursão";
                else
                    msg = "NÃO possui recursão";
                */
                JOptionPane.showMessageDialog(MainWindow.getInstance(),msg);
            }
            catch(MetaException e1)
            {
            	MainWindow.getInstance().handleError(e1);
            }                     
		}		
	};
	
	public static final Action condition3 = new AbstractAction("Terceira Condição LL(1)")
	{
		public void actionPerformed(ActionEvent e) 
		{
            try
            {
            	String msg;
            	
                Grammar g = MainWindow.getInstance().getGrammar();
                if (g.passThirdCondition())
                    msg = "Passou na 3a condição";
                else
                    msg = "NÃO passou na 3a condição";
                    
				JOptionPane.showMessageDialog(MainWindow.getInstance(),msg);
            }          
            catch(MetaException e1)
            {
            	MainWindow.getInstance().handleError(e1);
            }
        }		
	};
	
	public static final Action ff = new AbstractAction("First & Follow")
	{
		public void actionPerformed(ActionEvent e) 
		{			
            try
            {
            	//String msg;
            	
                Grammar g = MainWindow.getInstance().getGrammar();
                //msg = g.stringFirstFollow();
                
                //JOptionPane.showMessageDialog(MainWindow.getInstance(),msg);
                
				HTMLDialog.getInstance().show("First & Follow", g.ffAsHTML());
            }
            catch(MetaException e1)
            {
            	MainWindow.getInstance().handleError(e1);
            }                      
        }		
	};
	
	public static final Action showItemSet = new AbstractAction("Conjunto de Itens ")
	{
		public void actionPerformed(ActionEvent e) 
		{			
            try
            {
                Grammar g = MainWindow.getInstance().getGrammar();
                LRGenerator parser = LRGeneratorFactory.createGenerator(g);
                List<String> l = parser.getErrors(parser.buildTable());
                int i=0;
                for (String element : l)
				{
					System.out.println(i+"->"+element);
				}
         		HTMLDialog.getInstance().show("Itens SLR(1)", parser.itemsAsHTML());
            }
            catch(MetaException e1)
            {
            	MainWindow.getInstance().handleError(e1);
            }                      
        }		
	};
	
	public static final Action viewLexTable = new AbstractAction("Tabela de Análise Léxica")
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					FiniteAutomata fa = MainWindow.getInstance().getFiniteAutomata();
					HTMLDialog.getInstance().show("Automato Finito", fa.asHTML());
				}
				catch(MetaException e1)
				{
					MainWindow.getInstance().handleError(e1);
				}
			}
		};
	
	public static final Action verify = new ToolTipedAction("Verificar Erros", VERIFY)
	{
		public void actionPerformed(ActionEvent e) 
		{			
            try
            {	
            	MainWindow.getInstance().getFiniteAutomata();            	
            	MainWindow.getInstance().getGrammar();
                JOptionPane.showMessageDialog(MainWindow.getInstance(), "Nenhum erro foi encontrado");
            }
            catch(MetaException e1)
            {
            	MainWindow.getInstance().handleError(e1);
            }                   
        }		
	};
	
	
	//TODO: startar a thread de processamento qd um undo ou redo aontece
	public static final UndoManager UNDO_MAN = new UndoManager();
	
	public static final Action undo = new ToolTipedAction("Desfazer", UNDO)
	{
		{
			putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
			
		}
	
		public void actionPerformed(ActionEvent e) 
		{
			if (UNDO_MAN.canUndo())
				UNDO_MAN.undo();
			else
				Toolkit.getDefaultToolkit().beep();
		}	
	};
	
	public static final Action redo = new ToolTipedAction("Resfazer", REDO)
	{
		{
			putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK)); 
		}

		public void actionPerformed(ActionEvent e) 
		{
			if (UNDO_MAN.canRedo())
				UNDO_MAN.redo();
			else
				Toolkit.getDefaultToolkit().beep();
		}	
	};
	
	public static final Action useless = new AbstractAction("Símbolos inúteis")
	{
		public void actionPerformed(ActionEvent e) 
		{			
			try
			{
				Grammar g = MainWindow.getInstance().getGrammar();
				HTMLDialog.getInstance().show("Símbolos inúties", g.uselessSymbolsHTML());
			}
			catch(MetaException e1)
			{
				MainWindow.getInstance().handleError(e1);
			}                              
		}
	};
	
	public static final Action genCode = new ToolTipedAction("Gerar Código", GENERATOR)
	{
		public void actionPerformed(ActionEvent e) 
		{			
			final String lb = System.getProperty("line.separator");
            try
			{				
				FiniteAutomata fa = MainWindow.getInstance().getFiniteAutomata();
				Grammar g = MainWindow.getInstance().getGrammar();				
            	
            	Options options = MainWindow.getInstance().getOptions();
				
				if (options != null)
				{            	
	            	FILE_CHOOSER.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	            	FILE_CHOOSER.setFileFilter(FileFilters.DIRECTORY_FILTER);
	            	String title = FILE_CHOOSER.getDialogTitle();
	            	FILE_CHOOSER.setDialogTitle("Escolher Pasta");
	            							
					if (FILE_CHOOSER.showSaveDialog(MainWindow.getInstance()) == JFileChooser.APPROVE_OPTION)
					{
						String path = FILE_CHOOSER.getSelectedFile().getPath();
	            		            	
						TreeMap<String, String> allFiles = new TreeMap<String, String>();
						
						switch (options.language)
						{
							case JAVA:
								allFiles.putAll( new JavaCommonGenerator().generate(fa, g, options) );
								allFiles.putAll( new JavaScannerGenerator().generate(fa, options) );							
								allFiles.putAll( new JavaParserGenerator().generate(g, options));
								break;
							case CPP:
								allFiles.putAll( new CppCommomGenerator().generate(fa, g, options) );
								allFiles.putAll( new CppScannerGeneretor().generate(fa, options) );
								allFiles.putAll( new CppParserGenerator().generate(g, options) );
								break;
							case DELPHI:
								allFiles.putAll( new DelphiCommomGenerator().generate(fa, g, options) );
								allFiles.putAll( new DelphiScannerGenerator().generate(fa, options) );
								allFiles.putAll( new DelphiParserGenerator().generate(g, options));
								break;						
						}
						
						try
						{
							if (FileGenerationSelector.getInstance().show(allFiles))
								for (String f : allFiles.keySet() )
								{
									BufferedWriter writer = new BufferedWriter(new FileWriter(path + "/" + f));
									StringBuffer bfr = new StringBuffer(allFiles.get(f));
									
									for (int j=0; j<bfr.length(); j++)
									{
										if (bfr.charAt(j) == '\n')
										{
											bfr.replace(j, j+1, lb);
											j += lb.length() - 1;
										}
									}
									
									writer.write( bfr.toString() );
									writer.close();									
								}
						}
						catch (IOException ee)
						{
							ee.printStackTrace();
							System.exit(1);
						}
	            	}
	            	FILE_CHOOSER.setFileSelectionMode(JFileChooser.FILES_ONLY);
	            	FILE_CHOOSER.setDialogTitle(title);
				}
			}
			catch(MetaException e1)
            {
            	MainWindow.getInstance().handleError(e1);
            }		
			catch (NotLLException e1)
			{
				JOptionPane.showMessageDialog(MainWindow.getInstance(), "Esta gramática não é LL(1): "+e1.getMessage());
				e1.printStackTrace();
			}
        }
	};
	
	public static final Action importGAS = new AbstractAction("Importar BNF")
	{
		public void actionPerformed(ActionEvent e)
		{
			try
			{					
				GalsData lsd = new BNFImporter().importGAS();
				if (lsd != null)
					MainWindow.getInstance().updateData(lsd);				
			}
			catch (AnalysisError ae)
			{
				JOptionPane.showMessageDialog(MainWindow.getInstance(), "Não foi possível importar o arquivo");
				ae.printStackTrace();
			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
			}
		}	
	};
}