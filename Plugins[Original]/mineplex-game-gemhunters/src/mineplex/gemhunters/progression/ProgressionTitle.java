package mineplex.gemhunters.progression;

public class ProgressionTitle
{

	private String _title;
	private int _requiredGems;

	public ProgressionTitle(String title, int requiredGems)
	{
		_title = title;
		_requiredGems = requiredGems;
	}

	public String getTitle()
	{
		return _title;
	}

	public int getRequiredGems()
	{
		return _requiredGems;
	}
}
