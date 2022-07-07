package mineplex.core.poll;

import mineplex.core.account.permissions.PermissionGroup;

public enum DisplayType
{
	ALL,
	RANKED,
	NOT_RANKED;

	public boolean shouldDisplay(PermissionGroup group)
	{
		switch (this)
		{
			case RANKED:
				return group != PermissionGroup.PLAYER;
			case NOT_RANKED:
				return group == PermissionGroup.PLAYER;
			default:
				return true;
		}
	}
}