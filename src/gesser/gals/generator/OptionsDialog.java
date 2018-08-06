package gesser.gals.generator;

import gesser.gals.Actions;
import gesser.gals.GALS;
import gesser.gals.InputPane;
import gesser.gals.MainWindow;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class OptionsDialog extends JDialog implements ActionListener
{
	private OptionsPane optionsPane = new OptionsPane();
	private JButton ok = new JButton("Ok");
	private JButton cancel = new JButton("Cancelar");
	private JButton apply = new JButton("Aplicar");
	private Options options;
	
	private static OptionsDialog instance = null;
	
	private OptionsDialog()
	{
		super(MainWindow.getInstance(), "Opções do Projeto", true);	
		getContentPane().add(optionsPane);
		
		JPanel btns = new JPanel(new GridLayout(1, 0, 5, 5));
		btns.add(ok);
		btns.add(cancel);
		btns.add(apply);
		
		Box b = new Box(BoxLayout.X_AXIS);
		
		b.add(Box.createHorizontalGlue());
		b.add(btns);
		
		b.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
		getContentPane().add(b, BorderLayout.SOUTH);
				
		pack();
		setResizable(false);
        GALS.centralize(this);
		
		options = optionsPane.getOptions();
		ok.addActionListener(this);
		cancel.addActionListener(this);
		apply.addActionListener(this);
	}
	
	public static OptionsDialog getInstance()
	{
		if (instance == null)
			instance = new OptionsDialog();
			
		return instance;
	}
	
	public Options getOptions()
	{
		return options;
	}
	
	public void setOptions(Options opt)
	{
		options = opt;
		optionsPane.setOptions(opt);
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == ok)
			okClick();
		else if (e.getSource() == cancel)
			cancelClick();
		else if (e.getSource() == apply)
			applyChanges();
	}

	private void okClick()
	{
		applyChanges();
		setVisible(false);
	}
	
	private void cancelClick()
	{
		setVisible(false);
	}
	
	public InputPane.Mode getMode()
	{
		boolean lex = optionsPane.genLex();
		boolean synt = optionsPane.genSynt();
		
		if (lex && synt)
			return InputPane.Mode.BOTH;
		else
		{
			if (lex)
				return InputPane.Mode.LEXICAL;
			else
				return InputPane.Mode.SYNTATIC;
		}
	}
	
	private void applyChanges()
	{
		Actions.setSaved(false);
		options = optionsPane.getOptions();
		
		MainWindow.getInstance().setChanged();
		MainWindow.getInstance().setPanesMode( getMode() );		
	}
	
	public void reset()
	{
		optionsPane.reset();
	}
}
