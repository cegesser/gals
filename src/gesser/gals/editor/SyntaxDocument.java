package gesser.gals.editor;

import javax.swing.event.UndoableEditListener;
import javax.swing.text.*;

public abstract class SyntaxDocument extends DefaultStyledDocument
{
	protected abstract void apply(int startOffset, int endOffset, String input) throws BadLocationException;
	
	public String getText()
	{
		try 
		{
			return getText(0, getLength());
		}
		catch (BadLocationException e) 
		{
			e.printStackTrace();
			return "";
		}
	}
	
	public void insertString(int offset, String str, AttributeSet a) throws BadLocationException 
	{	
		super.insertString(offset, str, a);
		
		int start = offset;
		int end = start+str.length();
		
		int length = getLength();		
		
		String text = getText(0, length);
		
		start--;
		while (start >= 0 && text.charAt(start) != '\n')
			start--;
		start++;
		
		while (end < length && text.charAt(end) != '\n')
			end++;

		refresh(start, end, text);
	}
	
	public void remove(int offset, int length) throws BadLocationException
	{
		super.remove(offset, length);
		
		int start = offset;
		int end = start;
		
		length = getLength();

		String text = getText(0, length);

		start--;
		while (start >= 0 && text.charAt(start) != '\n')
			start--;
		start++;

		while (end < length && text.charAt(end) != '\n')
			end++;

		refresh(start, end, text);
	}

	protected void refresh(int start, int end, String text)
		throws BadLocationException
	{
		UndoableEditListener[] listeners = getUndoableEditListeners();
		for (UndoableEditListener l : listeners)
		{
			removeUndoableEditListener(l);
		}
		apply(start, end, text);
		for (UndoableEditListener l : listeners)
		{
			addUndoableEditListener(l);
		}
	}
}
