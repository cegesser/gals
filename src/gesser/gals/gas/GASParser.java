package gesser.gals.gas;

import gesser.gals.analyser.AnalysisError;
import gesser.gals.analyser.SyntaticError;
import gesser.gals.analyser.Token;

import java.util.Stack;

public class GASParser implements Constants
{
    private Stack<Integer> stack = new Stack<Integer>();
    private Token currentToken;
    private Token previousToken;
    private GASScanner scanner;
    private GASTranslator semanticAnalyser;

    public void parse(GASScanner scanner, GASTranslator semanticAnalyser) throws AnalysisError
    {
        this.scanner = scanner;
        this.semanticAnalyser = semanticAnalyser;

        stack.clear();
        stack.push(Integer.valueOf(0));

        currentToken = scanner.nextToken();

        while ( ! step() )
            ;
    }

    private boolean step() throws AnalysisError
    {
        int state = stack.peek().intValue();

        if (currentToken == null)
        {
            int pos = 0;
            if (previousToken != null)
                pos = previousToken.getPosition()+previousToken.getLexeme().length();

            currentToken = new Token(DOLLAR, "$", pos);
        }

        int token = currentToken.getId();
        int[] cmd = SYNT_TABLE[state][token-1];

        switch (cmd[0])
        {
            case SHIFT:
                stack.push(Integer.valueOf(cmd[1]));
                previousToken = currentToken;
                currentToken = scanner.nextToken();
                return false;

            case REDUCE:
                int[] prod = PRODUCTIONS[cmd[1]];

                for (int i=0; i<prod[1]; i++)
                    stack.pop();

                int oldState = stack.peek().intValue();
                stack.push(Integer.valueOf(SYNT_TABLE[oldState][prod[0]-1][1]));
                return false;

            case ACTION:
                int action = FIRST_SEMANTIC_ACTION + cmd[1] - 1;
                stack.push(Integer.valueOf(SYNT_TABLE[state][action][1]));
                semanticAnalyser.executeAction(cmd[1], previousToken);
                return false;

            case ACCEPT:
                return true;

            case 5:
                throw new SyntaticError("Erro sintático", currentToken.getPosition());
        }
        return false;
    }

}
