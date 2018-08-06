package gesser.gals.editor;

import java.awt.Color;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * @author gesser
 */

public enum Style 
{
	NORMAL,
	STRING
	{
		{
			StyleConstants.setForeground(style, Color.RED);
		}
	},
	OPERATOR
	{
		{
			StyleConstants.setForeground(style, new Color(0, 0, 128));
		}
	},
	REG_EXP
	{
		{
			StyleConstants.setForeground(style, new Color(0, 128, 0));
		}
	},
	ERROR
	{
		{
			StyleConstants.setBackground(style, Color.RED);
			StyleConstants.setForeground(style, Color.WHITE);
			StyleConstants.setBold(style, true);
		}
	},
	COMMENT
	{
		{
			StyleConstants.setForeground(style, Color.DARK_GRAY);
			StyleConstants.setItalic(style, true);
		}
	},
	ACTION_SEM
	{
		{
			StyleConstants.setForeground(style, new Color(0, 128, 0));
		}
	},
	EPSILON
	{
		{
			StyleConstants.setForeground(style, Color.MAGENTA);
			StyleConstants.setBold(style, true);
		}
	},
	NON_TERMINAL
	{
		{
			StyleConstants.setForeground(style, Color.BLACK);
			StyleConstants.setBold(style, true);
		}
	};
	
	protected SimpleAttributeSet style = new SimpleAttributeSet();
	public SimpleAttributeSet getStyle() { return style; }
}
