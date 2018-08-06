package gesser.gals.editor;

import java.awt.Color;

import gesser.gals.ebnf.ParserManager;
import gesser.gals.ebnf.parser.tokens.Token;

import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * @author Gesser
 */
public class BNFDocument extends SyntaxDocument
{	
    private ProcessorThread pt = new ProcessorThread();
    private String highlighted = "";
    
    ParserManager pm = new ParserManager();
	
	protected void refresh(int start, int end, String text)
			throws BadLocationException {
		pt.stopWork();
		super.refresh(start, end, text);
		pt.awake();
	}
	
	protected void apply(int startOffset, int endOffset, String input) throws BadLocationException
	{	
	    if (startOffset >= endOffset)
			return;
	    
	    pm.setInput(getText());
	    
		for (Token t : pm.getTokensBetween(startOffset, endOffset))		
			applyStyle(t);
	}

	/**
	 * 
	 */
	public void updateHighlighted(String lexeme) 
	{
		highlighted = lexeme;
		
		pm.setInput(getText());
		
		for (Token t : pm.getTokens())
		{
			applyStyle(t);
		}
	}

	/**
	 * @param t
	 */
	private void applyStyle(Token t) {
		SimpleAttributeSet s = t.getStyle();
		if (t.getLexeme().equals(highlighted))
		{
			s = new SimpleAttributeSet(s);
			StyleConstants.setBackground(s, new Color(180, 255, 255));
		}
		setCharacterAttributes(t.getStart(), t.getLexeme().length(), s, true);
	}
}
