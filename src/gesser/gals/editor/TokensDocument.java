package gesser.gals.editor;

import gesser.gals.analyser.Token;
import gesser.gals.scannerparser.LineScanner;

import javax.swing.text.BadLocationException;

/**
 * @author Gesser
 */

public class TokensDocument extends SyntaxDocument
{
	LineScanner ls = new LineScanner();
	
	protected void apply(int startOffset, int endOffset, String input) throws BadLocationException
	{
		if (startOffset >= endOffset)
			return;
		
		ls.setText( input );
		ls.setRange(startOffset, endOffset);
		
		int oldPos = startOffset;
		int pos = startOffset;
		Token t=null;
		
		t = ls.nextToken();
				
		while (t != null)
		{
			pos = t.getPosition();
			int length = t.getLexeme().length();
			
			Style att = Style.NORMAL;
			
			switch (t.getId())
			{						
				case LineScanner.COLON: 
				case LineScanner.EQUALS: 
					att = Style.OPERATOR;
					break;
				case LineScanner.ID: 
					att = Style.NORMAL;
					break;
				case LineScanner.STR: 
					att = Style.STRING;
					break;
				case LineScanner.RE: 
					if (t.getLexeme().charAt(0) == '!')
					{
						setCharacterAttributes(oldPos, pos-oldPos, Style.NORMAL.getStyle(), true);
						setCharacterAttributes(pos, 1, Style.OPERATOR.getStyle(), true);
						pos++;
						length--;
						oldPos = pos;
					}
					att = Style.REG_EXP;
					break;
				case LineScanner.COMMENT: 
					att = Style.COMMENT;
					break;
				case LineScanner.ERROR: 
					att = Style.ERROR;
					break;
			}
			setCharacterAttributes(oldPos, pos-oldPos, Style.NORMAL.getStyle(), true);
			setCharacterAttributes(pos, length, att.getStyle(), true);
			oldPos = pos+length;
			
			t = ls.nextToken();
		}			
			
		
		setCharacterAttributes(oldPos, endOffset-oldPos, Style.NORMAL.getStyle(), true);
	}
}
