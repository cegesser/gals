package gesser.gals.gas;

import gesser.gals.GALS;
import gesser.gals.MainWindow;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class KeyWordsSelector extends JDialog implements ChangeListener, ActionListener
{
	private JCheckBox[] checkBoxes = null;
	private JTextField base = new JTextField();
	private JTextField ignore = new JTextField();
	private JButton done = new JButton("Pronto");
	
	private String result;
	
	private JPanel checkBoxesPanel = new JPanel(new GridLayout(0, 1));
	
	private KeyWordsSelector()
	{
		super(MainWindow.getInstance(), "Seleção de Palavras-Chave", true);
		
		getContentPane().setLayout(new GridLayout(1, 2, 5, 5));
		
		JPanel left = new JPanel(new GridBagLayout());
		
		JScrollPane scroll = new JScrollPane(checkBoxesPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		getContentPane().add(left);
		getContentPane().add(scroll);
		
		left.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));
		
		GridBagConstraints constr = new GridBagConstraints();		
		constr.fill = GridBagConstraints.HORIZONTAL;
		
		constr.gridx = 0;		
		constr.weighty = 1;
		constr.weightx = 1;
		
		constr.gridy = 0;
		constr.anchor = GridBagConstraints.NORTH;
		left.add(new JLabel("<html>Selecione os Tokens que são palavras-chave</html>"), constr);
		
		constr.gridy = 1;
		constr.anchor = GridBagConstraints.SOUTH;
		left.add(new JLabel("<html>Token base para as palavras-chave</html>"), constr);
		constr.gridy = 2;
		constr.anchor = GridBagConstraints.NORTH;
		left.add(base, constr);
		
		constr.gridy = 3;
		constr.anchor = GridBagConstraints.SOUTH;
		left.add(new JLabel("Ignorar"), constr);
		constr.gridy = 4;
		constr.anchor = GridBagConstraints.NORTH;		
		left.add(ignore, constr);
		
		constr.gridy = 5;
		constr.anchor = GridBagConstraints.SOUTH;
		constr.fill = GridBagConstraints.NONE;
		left.add(done, constr);
		
		done.addActionListener(this);
			
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	}
	
	private void setItems(List<String> items)
	{
		checkBoxes = new JCheckBox[items.size()];
		checkBoxesPanel.removeAll();
		for (int i=0; i<items.size(); i++)
		{			
			checkBoxes[i] = new JCheckBox( items.get(i), false );
			checkBoxes[i].addChangeListener(this);
			checkBoxesPanel.add(checkBoxes[i]);
		}
		
		setSize(250, 350);
        GALS.centralize(this);
	}
	
	public static String process(List<String> tokens)
	{
		KeyWordsSelector sel = new KeyWordsSelector();
		sel.setItems(tokens);
		sel.setVisible(true);
	/*
		StringBuffer bfr = new StringBuffer();
		bfr.append("//tokens\n");
		
		for (int i=0; i<tokens.size(); i++)
		{
			String tok = (String) tokens.get(i);
			bfr.append(tok).append("\n");			
		}
		
		return bfr.toString();*/
		return sel.result;
	}
	
	public void stateChanged(ChangeEvent e)
	{
	}
	
	public void actionPerformed(ActionEvent e)
	{
		StringBuffer bfr = new StringBuffer();
		bfr.append("//tokens\n");

		for (int i=0; i<checkBoxes.length; i++)
		{
			if (!checkBoxes[i].isSelected())
			{
				bfr.append(checkBoxes[i].getText()).append(" : //inserir Expressão Regular\n");			
			}
		}

		bfr.append("\n//ignorar\n");
		bfr.append(" : ").append(ignore.getText()).append("\n");
		
		bfr.append("\n//palavras reservadas\n");

		for (int i=0; i<checkBoxes.length; i++)
		{
			if (checkBoxes[i].isSelected())
			{
				String txt = checkBoxes[i].getText();
				bfr.append(txt).append(" = ").append(base.getText()).append(" : \"").append(txt).append("\"\n");			
			}
		}
		
		result =  bfr.toString();
		
		dispose();
	}
}
