package gesser.gals.scannerparser;

import gesser.gals.analyser.AnalysisError;
import gesser.gals.analyser.SyntaticError;
import gesser.gals.analyser.Token;

import static gesser.gals.scannerparser.Constants.*;
import static gesser.gals.scannerparser.ParserConstants.*;

public class REParser
{
	private Token currentToken;
	private Token previousToken;
	private Scanner scanner;
	private SemanticAnalyser semanticAnalyser;

	public Node parse(String str, FiniteAutomataGenerator gen) throws AnalysisError
	{
		scanner = new Scanner(str);
		semanticAnalyser = new SemanticAnalyser(gen);

		currentToken = scanner.nextToken();
		if (currentToken == null)
			currentToken = new Token(DOLLAR, "$", 0);

		reg_exp_ctxt();

		if (currentToken.getId() != DOLLAR)
			throw new SyntaticError(PARSER_ERROR[DOLLAR], currentToken.getPosition());
			
		return semanticAnalyser.getRoot();
	}

	private void match(int token) throws AnalysisError
	{
		if (currentToken.getId() == token)
		{
			previousToken = currentToken;
			currentToken = scanner.nextToken();
			if (currentToken == null)
			{
				int pos = 0;
				if (previousToken != null)
					pos = previousToken.getPosition()+previousToken.getLexeme().length();

				currentToken = new Token(DOLLAR, "$", pos);
			}
		}
		else
			throw new SyntaticError(PARSER_ERROR[token], currentToken.getPosition());
	}

	private void reg_exp_ctxt() throws AnalysisError
	{
		switch (currentToken.getId())
		{
			case 6: // "("
			case 8: // "["
			case 10: // "."
			case 13: // DEFINITION
			case 14: // CHAR
				reg_exp();
				context();
				break;
			default:
				throw new SyntaticError(PARSER_ERROR[15], currentToken.getPosition());
		}
	}

	private void reg_exp() throws AnalysisError
	{
		switch (currentToken.getId())
		{
			case 6: // "("
			case 8: // "["
			case 10: // "."
			case 13: // DEFINITION
			case 14: // CHAR
				exp();
				semanticAnalyser.executeAction(1, previousToken);
				reg_exp_c();
				break;
			default:
				throw new SyntaticError(PARSER_ERROR[16], currentToken.getPosition());
		}
	}

	private void reg_exp_c() throws AnalysisError
	{
		switch (currentToken.getId())
		{
			case 1: // $
			case 7: // ")"
			case 11: // "^"
				// EPSILON
				break;
			case 2: // "|"
				match(2); // "|"
				exp();
				semanticAnalyser.executeAction(2, previousToken);
				reg_exp_c();
				break;
			default:
				throw new SyntaticError(PARSER_ERROR[17], currentToken.getPosition());
		}
	}

	private void exp() throws AnalysisError
	{
		switch (currentToken.getId())
		{
			case 6: // "("
			case 8: // "["
			case 10: // "."
			case 13: // DEFINITION
			case 14: // CHAR
				term();
				semanticAnalyser.executeAction(4, previousToken);
				exp_c();
				break;
			default:
				throw new SyntaticError(PARSER_ERROR[18], currentToken.getPosition());
		}
	}

	private void exp_c() throws AnalysisError
	{
		switch (currentToken.getId())
		{
			case 1: // $
			case 2: // "|"
			case 7: // ")"
			case 11: // "^"
				// EPSILON
				break;
			case 6: // "("
			case 8: // "["
			case 10: // "."
			case 13: // DEFINITION
			case 14: // CHAR
				term();
				semanticAnalyser.executeAction(5, previousToken);
				exp_c();
				break;
			default:
				throw new SyntaticError(PARSER_ERROR[19], currentToken.getPosition());
		}
	}

	private void context() throws AnalysisError
	{
		switch (currentToken.getId())
		{
			case 1: // $
				// EPSILON
				break;
			case 11: // "^"
				match(11); // "^"
				reg_exp();
				semanticAnalyser.executeAction(3, previousToken);
				break;
			default:
				throw new SyntaticError(PARSER_ERROR[20], currentToken.getPosition());
		}
	}

	private void term() throws AnalysisError
	{
		switch (currentToken.getId())
		{
			case 6: // "("
			case 8: // "["
			case 10: // "."
			case 13: // DEFINITION
			case 14: // CHAR
				factor();
				op();
				break;
			default:
				throw new SyntaticError(PARSER_ERROR[21], currentToken.getPosition());
		}
	}

	private void op() throws AnalysisError
	{
		switch (currentToken.getId())
		{
			case 1: // $
			case 2: // "|"
			case 6: // "("
			case 7: // ")"
			case 8: // "["
			case 10: // "."
			case 11: // "^"
			case 13: // DEFINITION
			case 14: // CHAR
				// EPSILON
				break;
			case 3: // "*"
				match(3); // "*"
				semanticAnalyser.executeAction(6, previousToken);
				break;
			case 4: // "+"
				match(4); // "+"
				semanticAnalyser.executeAction(7, previousToken);
				break;
			case 5: // "?"
				match(5); // "?"
				semanticAnalyser.executeAction(8, previousToken);
				break;
			default:
				throw new SyntaticError(PARSER_ERROR[22], currentToken.getPosition());
		}
	}

	private void factor() throws AnalysisError
	{
		switch (currentToken.getId())
		{
			case 6: // "("
				match(6); // "("
				reg_exp();
				match(7); // ")"
				semanticAnalyser.executeAction(9, previousToken);
				break;
			case 8: // "["
				match(8); // "["
				end_class();
				break;
			case 10: // "."
				match(10); // "."
				semanticAnalyser.executeAction(10, previousToken);
				break;
			case 13: // DEFINITION
				match(13); // DEFINITION
				semanticAnalyser.executeAction(11, previousToken);
				break;
			case 14: // CHAR
				match(14); // CHAR
				semanticAnalyser.executeAction(12, previousToken);
				break;
			default:
				throw new SyntaticError(PARSER_ERROR[23], currentToken.getPosition());
		}
	}

	private void end_class() throws AnalysisError
	{
		switch (currentToken.getId())
		{
			case 11: // "^"
				match(11); // "^"
				item();
				class_c();
				match(9); // "]"
				semanticAnalyser.executeAction(13, previousToken);
				break;
			case 14: // CHAR
				item();
				class_c();
				match(9); // "]"
				break;
			default:
				throw new SyntaticError(PARSER_ERROR[24], currentToken.getPosition());
		}
	}

	private void class_c() throws AnalysisError
	{
		switch (currentToken.getId())
		{
			case 9: // "]"
				// EPSILON
				break;
			case 14: // CHAR
				item();
				class_c();
				semanticAnalyser.executeAction(14, previousToken);
				break;
			default:
				throw new SyntaticError(PARSER_ERROR[25], currentToken.getPosition());
		}
	}

	private void item() throws AnalysisError
	{
		switch (currentToken.getId())
		{
			case 14: // CHAR
				match(14); // CHAR
				semanticAnalyser.executeAction(12, previousToken);
				end_interval();
				break;
			default:
				throw new SyntaticError(PARSER_ERROR[26], currentToken.getPosition());
		}
	}

	private void end_interval() throws AnalysisError
	{
		switch (currentToken.getId())
		{
			case 9: // "]"
			case 14: // CHAR
				// EPSILON
				break;
			case 12: // "-"
				match(12); // "-"
				match(14); // CHAR
				semanticAnalyser.executeAction(15, previousToken);
				break;
			default:
				throw new SyntaticError(PARSER_ERROR[27], currentToken.getPosition());
		}
	}

}