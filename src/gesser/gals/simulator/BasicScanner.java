package gesser.gals.simulator;

import gesser.gals.analyser.LexicalError;
import gesser.gals.analyser.Token;

/**
 * @author Gesser
 */

public interface BasicScanner
{
	void setInput(String text);
	Token nextToken() throws LexicalError;
}
