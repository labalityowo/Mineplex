package nautilus.game.arcade.managers.chat;

/**
 * Created by TeddyDev on 10/01/2016.
 */
public class ChatStatData
{
	private String _stat;
	private String _display;
	private boolean _isValue;

	public ChatStatData()
	{

	}

	public ChatStatData(String stat, String display, boolean isValue)
	{
		_stat = stat;
		_display = display;
		_isValue = isValue;
	}

	public ChatStatData blankLine()
	{
		_stat = null;
		_display = " ";
		_isValue = false;

		return this;
	}

	public ChatStatData plainText(String text)
	{
		_stat = null;
		_display = text;
		_isValue = false;

		return this;
	}

	public String getStat()
	{
		return _stat;
	}

	public String getDisplay()
	{
		return _display;
	}

	public boolean isValue()
	{
		return _isValue;
	}
}
