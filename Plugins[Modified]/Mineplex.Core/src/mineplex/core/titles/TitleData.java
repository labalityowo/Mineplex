package mineplex.core.titles;

public class TitleData
{
	private final String _selectedTitle;

	public TitleData(String selectedTitle)
	{
		this._selectedTitle = selectedTitle;
	}

	public String getTrackId()
	{
		return _selectedTitle;
	}
}
