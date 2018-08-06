/*
 * Created on 06/09/2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package gesser.gals.generator.parser.lr;

import gesser.gals.generator.parser.Grammar;

/**
 * @author Gesser
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface LRConflictSolver
{
    int resolve(Grammar g, Command[] conflict, int state, int input);
}