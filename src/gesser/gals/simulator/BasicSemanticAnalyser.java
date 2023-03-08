package gesser.gals.simulator;

import gesser.gals.analyser.SemanticError;
import gesser.gals.analyser.Token;

public interface BasicSemanticAnalyser
{
	void executeAction(int action, Token currentToken) throws SemanticError;
}
