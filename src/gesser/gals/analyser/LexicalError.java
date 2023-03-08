package gesser.gals.analyser;

public class LexicalError extends AnalysisError
{
	public LexicalError(String msg, int position)
	{
		super(msg, position);
	}
	
	public LexicalError(String msg)
	{
		super(msg);
	}
}
