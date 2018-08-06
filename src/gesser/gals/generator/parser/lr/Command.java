package gesser.gals.generator.parser.lr;

import static gesser.gals.generator.parser.lr.Command.Type.*;

public class Command
{
	private int parameter;
	private Type type;
	
	protected Command(Type type, int parameter)
	{
		this.type = type;
		this.parameter = parameter;
	}
	
	public enum Type { SHIFT, REDUCE, ACTION, ACCEPT, GOTO, ERROR }
	
	public static final String[] CONSTANTS = 
	{
		"SHIFT ",
		"REDUCE",
		"ACTION",
		"ACCEPT",
		"GO_TO ",
		"ERROR "
	};
	
	public Type getType()
	{
		return type;
	}
	
	public int getParameter()
	{
		return parameter;
	}
	
	public static Command createShift(int state)
	{
		return new Command(Type.SHIFT, state);
	}
	
	public static Command createReduce(int production)
	{
		return new Command(Type.REDUCE, production);
	}
	
	public static Command createAction(int production)
	{
		return new Command(Type.ACTION, production);
	}
	
	public static Command createAccept()
	{
		return new Command(Type.ACCEPT, 0);
	}
	
	public static Command createGoTo(int state)
	{
		return new Command(Type.GOTO, state);
	}
	
	public static Command createError()
	{
		return new Command(Type.ERROR, 0);
	}
	
	public String toString()
	{
		switch (type)
		{
			case SHIFT: return "SHIFT("+parameter+")";
			case REDUCE: return "REDUCE("+parameter+")";
			case ACTION: return "SEM.ACT("+parameter+")";
			case ACCEPT: return "ACCEPT";
			case GOTO: return ""+parameter;
			case ERROR: return "-";
			default: return "???";
		}
	}
	
	public boolean equals(Object obj)
	{
		try
		{
			Command other = (Command) obj;
			
			return 
				type == other.type &&
				parameter == other.parameter;
		}
		catch (ClassCastException e)
		{
			return false;
		}
	}
	
	public int hashCode()
	{
		int result = 43;
		result = result*parameter + 17;
		result = result*type.hashCode() + 17;
		return result;
	}
}
