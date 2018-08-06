package gesser.gals;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * @author Gesser
 */

public class HTMLDialog extends JDialog implements ActionListener, HyperlinkListener
{
	private JEditorPane pane = new JEditorPane();
	private JButton ok = new JButton("Fechar");
	private JButton save = new JButton("Salvar");
	
	private static HTMLDialog instance = null;
	
	public static HTMLDialog getInstance()
	{
		if (instance == null)
			instance = new HTMLDialog();
		return instance;
	}
	
	private HTMLDialog()
	{
		super(MainWindow.getInstance(), "Tabela", true);
		
		getContentPane().add(new JScrollPane(pane));
		pane.setEditable(false);
		pane.setEditorKit(new javax.swing.text.html.HTMLEditorKit());
		
		setSize(400, 300);
        GALS.centralize(this);
		
		Box bottom = new Box(BoxLayout.X_AXIS);
		bottom.add(Box.createVerticalStrut(35));
		bottom.add(Box.createHorizontalGlue());
		bottom.add(save);
		bottom.add(Box.createHorizontalStrut(5));
		bottom.add(ok);
		bottom.add(Box.createHorizontalStrut(5));		
		
		//JPanel pnl = new JPanel();
		//pnl.add(ok);
		ok.setSize(75, 25);
		save.setSize(75, 25);
		
		ok.addActionListener(this);	
		save.addActionListener(this);
		
		pane.addHyperlinkListener(this);
		
		getContentPane().add(bottom, BorderLayout.SOUTH);
	}
	
	public void show(String title, URL url)
	{
		try
		{
			pane.setPage(url);
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
		
		setTitle(title);
		pane.setCaretPosition(0);

		save.setVisible(false);
		
		super.setVisible(true);
	}
	
	public void show(String title, String html)
	{
		setTitle(title);
		pane.setText(html);
		pane.setCaretPosition(0);
		
		save.setVisible(true);
		
		super.setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == ok)
			setVisible(false);
		else if (e.getSource() == save)
		{
			Actions.FILE_CHOOSER.setFileFilter(FileFilters.HTML_FILTER);
			if (Actions.FILE_CHOOSER.showSaveDialog(MainWindow.getInstance()) == JFileChooser.APPROVE_OPTION)
			{
				File file = Actions.FILE_CHOOSER.getSelectedFile();
				String name = file.getPath();
				if ((name.length() < 5 || !name.substring(name.length()-5).equals(".html")) && (name.length() < 4 || !name.substring(name.length()-4).equals(".htm")))
				{
					name = name+".html";
					file = new File(name);
				}
				try
				{
					Writer w = new BufferedWriter(new FileWriter(file));
					String txt = pane.getText();
					w.write(txt);
					w.close();
				}
				catch (IOException ex)
				{
					ex.printStackTrace();
				}   
			}			
		}
	}
	
	public void hyperlinkUpdate(HyperlinkEvent e) 
	{
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
		{
			try
			{
				pane.setPage(e.getURL());
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
		}
	}

	
	public static String translateString(String str)
	{
		StringBuffer result = new StringBuffer();
		for (int i=0; i<str.length(); i++)
		{
			char c = str.charAt(i);
			switch (c)
			{
				case '"':
					result.append("&quot;");
					break;
				case '&':
					result.append("&amp;");
					break;
				case '<':
					result.append("&lt;");
					break;
				case '>':
					result.append("&gt;");
					break;
				default:
					result.append(c);
			}
		}
			
		return result.toString();
	}
}
