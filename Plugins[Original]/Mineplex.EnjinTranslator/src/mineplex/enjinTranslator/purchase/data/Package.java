package mineplex.enjinTranslator.purchase.data;

public class Package 
{
	private int _id;
	private String _name;
	
	public Package(int id, String name)
	{
		_id = id;
		_name = name;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public String getName()
	{
		return _name;
	}
}
