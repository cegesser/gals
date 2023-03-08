package gesser.gals.analyser;


public class SyntaticError extends AnalysisError
{
	public SyntaticError(String msg, int position)
	{
		super(msg, position);
	}
	
	public SyntaticError(String msg)
	{
		super(msg);
	}
}
