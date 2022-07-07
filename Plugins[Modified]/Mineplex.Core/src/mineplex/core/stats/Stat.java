package mineplex.core.stats;

public class Stat
{
	private int id;
	private String name;

	public Stat(int id, String name)
	{
		this.id = id;
		this.name = name;
	}

	public int getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}
}
