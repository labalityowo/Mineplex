package mineplex.serverdata.commands;

public class TwoFactorResetCommand extends ServerCommand
{
	private String _adminName;
	private String _adminUUID;
	private String _targetName;
	private String _targetUUID;

	public TwoFactorResetCommand(String adminName, String adminUUID, String targetName, String targetUUID)
	{
		_adminName = adminName;
		_adminUUID = adminUUID;
		_targetName = targetName;
		_targetUUID = targetUUID;
	}
}
