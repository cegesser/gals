package gesser.gals.analyser;


public class AnalysisError extends Exception
{	
	private int position;
	
	public AnalysisError(String msg, int position)
	{
		super(msg);
		this.position = position;
	}
	
	public AnalysisError(String msg)
	{
		super(msg);
		this.position = -1;
	}
	
	public int getPosition()
	{
		return position;
	}
	
	public void setPosition(int pos)
	{
		position = pos;
	}
	
	public String toString()
	{
		return super.toString() + ", em "+position;
	}
}
