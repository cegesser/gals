package gesser.gals;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.SortedMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class FileGenerationSelector extends JDialog implements ActionListener
{
	private static FileGenerationSelector instance = null;
	public static FileGenerationSelector getInstance()
	{
		if (instance == null)
			instance = new FileGenerationSelector();
		return instance;
	}
	
	private boolean result = false;
	private JButton ok = new JButton("Ok");
	private JButton cancel = new JButton("Cancelar");
	private JPanel center = new JPanel(new GridLayout(0, 1, 5, 5));
	private JCheckBox[] checks;
	private Map<String, String> files;
	
	private FileGenerationSelector()
	{
		super(MainWindow.getInstance(), "Seletor de Arquivos", true);
		
		JComponent cp = (JComponent) getContentPane();
		cp.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		cp.setLayout(new BorderLayout(5, 5));
		
		cp.add(new JLabel("<html>Selecione os arquivos que devem ser gerados<br>(Arquivos existentes serão sobrescritos)</html>"), BorderLayout.NORTH);
		
		JPanel tmp = new JPanel(new BorderLayout());
		tmp.setBorder(BorderFactory.createLoweredBevelBorder());
		tmp.add(Box.createHorizontalStrut(10), BorderLayout.WEST);
		tmp.add(center);
		
		cp.add(tmp);
		
		Box bottom = new Box(BoxLayout.X_AXIS);
		bottom.add(Box.createHorizontalGlue());
		bottom.add(cancel);
		bottom.add(Box.createHorizontalStrut(5));
		bottom.add(ok);
		
		cp.add(bottom, BorderLayout.SOUTH);
		
		ok.addActionListener(this);
		cancel.addActionListener(this);		
	}
	
	public boolean show(SortedMap<String, String> files)
	{
		this.files = files;
		
		checks = new JCheckBox[files.size()];
		int count = 0;		
		center.removeAll();
		for (String s : files.keySet())
		{
			checks[count] = new JCheckBox(s, true);
			center.add(checks[count]);
			count++;
		}
		pack();
		
		GALS.centralize(this);
		
		super.setVisible(true);		
		return result;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == ok)
		{
			for (int i=0; i<checks.length; i++)
			{
				if (! checks[i].isSelected())
					files.remove(checks[i].getText());
			}			
			
			result = true;
			setVisible(false);
		}
		else if (e.getSource() == cancel)
		{
			result = false;
			setVisible(false);
		}
		
	}
}
