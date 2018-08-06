package gesser.gals.generator.parser.ll;

import gesser.gals.generator.parser.Grammar;
import gesser.gals.util.IntegerSet;


public interface LLConflictSolver
{
	int resolve(Grammar g, IntegerSet conflict, int input, int stackTop);
}
