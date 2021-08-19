package mineplex.core.botspam;

public class SpamText
{
	private int _id;
	private String _text;
	private int _punishments;
	private boolean _enabled;
	private String _createdBy;
	private String _disabledBy;
	private String _enabledBy;

	public SpamText(int id, String text, int punishments, boolean enabled, String createdBy, String enabledBy, String disabledBy)
	{
		_id = id;
		_text = text;
		_punishments = punishments;
		_enabled = enabled;
		_createdBy = createdBy;
		_enabledBy = enabledBy;
		_disabledBy = disabledBy;
	}

	public boolean isSpam(String message)
	{
		//System.out.println(message.toLowerCase() + " vs " + _text.toLowerCase() + " == " + message.toLowerCase().contains(_text.toLowerCase()));
		return message.toLowerCase().contains(_text.toLowerCase());
	}

	public int getId()
	{
		return _id;
	}

	public String getText()
	{
		return _text;
	}

	public int getPunishments()
	{
		return _punishments;
	}

	public boolean isEnabled()
	{
		return _enabled;
	}

	public void setEnabled(boolean enabled)
	{
		_enabled = enabled;
	}

	public String getCreatedBy()
	{
		return _createdBy;
	}

	public String getEnabledBy()
	{
		return _enabledBy;
	}

	public String getDisabledBy()
	{
		return _disabledBy;
	}

	public void setEnabledBy(String enabledBy)
	{
		_enabledBy = enabledBy;
	}

	public void setDisabledBy(String disabledBy)
	{
		_disabledBy = disabledBy;
	}
}
