package mineplex.core.friend.data;

import java.util.UUID;

import mineplex.core.friend.FriendStatusType;
import mineplex.core.friend.FriendVisibility;

public class FriendStatus
{
	public String Name;
	public UUID UUID;
	public String ServerName;
	public boolean Online;
	/**
	 * This seems like it should be unmodified without current time subtracted when set
	 */
	public long LastSeenOnline;
	public FriendStatusType Status;
	public FriendVisibility Visibility;
	public boolean Favourite;

	public boolean isOnline()
	{
		return Online && Visibility != FriendVisibility.INVISIBLE;
	}
}
