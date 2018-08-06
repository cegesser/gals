package gesser.gals.generator.parser.lr;

import gesser.gals.generator.OptionsDialog;
import gesser.gals.generator.parser.Grammar;

import static gesser.gals.generator.Options.Parser.*;

/**
 * @author Gesser
 */
public class LRGeneratorFactory
{
	private LRGeneratorFactory() {}
	
	
	public static LRGenerator createGenerator(Grammar g)
	{
		switch (OptionsDialog.getInstance().getOptions().parser)
		{
			case SLR: return new SLRGenerator(g);
			case LR: return new LRCanonicGenerator(g);
			case LALR : return new LALRGenerator(g);
			default: return null;
		}
	}
}
