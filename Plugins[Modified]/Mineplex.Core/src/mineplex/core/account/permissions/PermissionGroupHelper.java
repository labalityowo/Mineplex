package mineplex.core.account.permissions;

public class PermissionGroupHelper
{
	public static PermissionGroup getGroupFromLegacy(String legacyValue)
	{
		if (legacyValue == null)
		{
			return null;
		}
		
		String legacy = legacyValue.toLowerCase();
		final PermissionGroup current;

		switch (legacy)
		{
			case "developer":
				current = PermissionGroup.DEV;
				break;
			case "jnr_dev":
				current = PermissionGroup.PLAYER;
				break;
			case "event_moderator":
				current = PermissionGroup.EVENTMOD;
				break;
			case "snr_moderator":
				current = PermissionGroup.SRMOD;
				break;
			case "moderator":
				current = PermissionGroup.MOD;
				break;
			case "helper":
				current = PermissionGroup.TRAINEE;
				break;
			case "mapdev":
				current = PermissionGroup.BUILDER;
				break;
			case "media":
				current = PermissionGroup.PLAYER;
				break;
			case "youtube_small":
				current = PermissionGroup.YT;
				break;
			case "all":
				current = PermissionGroup.PLAYER;
				break;
			default:
				current = PermissionGroup.getGroup(legacy).get();
				break;
		}
		
		return current;
	}
}