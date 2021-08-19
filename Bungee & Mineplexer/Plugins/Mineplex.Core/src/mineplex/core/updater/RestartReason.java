package mineplex.core.updater;

public enum RestartReason
{

	SINGLE_COMMAND("is restarting."),
	GROUP_COMMAND("is restarting."),
	JAR_UPDATE("is restarting for an update.");

	private final String _description;

	RestartReason(String description)
	{
		_description = description;
	}

	public String getDescription()
	{
		return _description;
	}
}
