package gesser.gals.util;

import gesser.gals.analyser.AnalysisError;

/**
 * @author Gesser
 */

public class MetaException extends Exception
{
	public enum Mode { DEFINITION, TOKEN, GRAMMAR }
	
	private Mode mode;
	private int index;
	
	public MetaException(Mode mode, int index, AnalysisError cause)
	{
		super(cause);
		this.mode = mode;
		this.index = index;
	}
	
	public int getIndex()
	{
		return index;
	}

	public Mode getMode()
	{
		return mode;
	}

}
