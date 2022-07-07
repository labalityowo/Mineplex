package mineplex.core.account;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.serverdata.Utility;

public class CoreClient
{
	private int _accountId = -1;
	private long _networkSessionLoginTime;
	private String _name;
	private UUID _uuid;
	private Player _player;
	private PermissionGroup _primary;
	private Set<PermissionGroup> _extra = new HashSet<>();

	/*
	 * Disguise info
	 */
	private String _disguisedName;
	private String _disguisedSkin;
	private UUID _disguisedUUID;
	private PermissionGroup _disguisedPrimary;

	public CoreClient(Player player)
	{
		_player = player;
		_uuid = player.getUniqueId();
		_name = player.getName();
		_networkSessionLoginTime = Utility.currentTimeMillis();
	}

	public CoreClient(String name, UUID uuid)
	{
		_name = name;
		_uuid = uuid;
	}

	public UUID getUniqueId()
	{
		return _uuid;
	}

	public String getName()
	{
		return _name;
	}

	public Player GetPlayer()
	{
		return _player;
	}

	public void SetPlayer(Player player)
	{
		_player = player;
	}

	public int getAccountId()
	{
		return _accountId;
	}

	public void setAccountId(int accountId)
	{
		_accountId = accountId;
	}
	
	protected PermissionGroup getRawPrimaryGroup()
	{
		return _primary;
	}

	public PermissionGroup getPrimaryGroup()
	{
		if (_primary == null)
		{
			_primary = PermissionGroup.PLAYER;
		}

		return _primary;
	}
	
	public Set<PermissionGroup> getAdditionalGroups()
	{
		return _extra;
	}

	public boolean inheritsFrom(PermissionGroup group)
	{
		return _primary.inheritsFrom(group) || _extra.stream().anyMatch(memberGroup -> memberGroup.inheritsFrom(group));
	}

	public boolean hasPermission(Permission permission)
	{
		return _primary.hasPermission(permission) || _extra.stream().anyMatch(memberGroup -> memberGroup.hasPermission(permission));
	}

	public boolean isMemberOf(PermissionGroup group)
	{
		return group == _primary || _extra.contains(group);
	}
	
	public void setPrimaryGroup(PermissionGroup group)
	{
		if (group != null && !group.canBePrimary())
		{
			return;
		}

		_primary = group;
	}

	public void addAdditionalGroup(PermissionGroup group)
	{
		if (!isMemberOf(group))
		{
			_extra.add(group);
		}
	}
	
	public void removeAdditionalGroup(PermissionGroup group)
	{
		_extra.remove(group);
	}

	public long getNetworkSessionLoginTime()
	{
		return _networkSessionLoginTime;
	}

	public void undisguise()
	{
		_disguisedName = null;
		_disguisedSkin = null;
		_disguisedPrimary = null;
		_disguisedUUID = null;
	}

	public String getDisguisedAs()
	{
		return _disguisedName;
	}

	public String getDisguisedSkin()
	{
		return _disguisedSkin;
	}

	public PermissionGroup getDisguisedPrimaryGroup()
	{
		return _disguisedPrimary;
	}

	public UUID getDisguisedAsUUID()
	{
		return _disguisedUUID;
	}

	public boolean isDisguised()
	{
		if (_disguisedName == null)
		{
			return false;
		}
		return !_name.equalsIgnoreCase(_disguisedName);
	}

	public void disguise(String name, UUID uuid, PermissionGroup group)
	{
		_disguisedName = name;
		_disguisedUUID = uuid;
		_disguisedPrimary = group;
	}

	public PermissionGroup getRealOrDisguisedPrimaryGroup()
	{
		if (_disguisedPrimary != null)
		{
			return _disguisedPrimary;
		}
		return getPrimaryGroup();
	}

	public void setNetworkSessionLoginTime(long loginTime)
	{
		_networkSessionLoginTime = loginTime;
	}

	public String getRealOrDisguisedName()
	{
		if (getDisguisedAs() != null)
		{
			return getDisguisedAs();
		}
		return getName();
	}

	@Override
	public String toString()
	{
		return "CoreClient{" +
				"_accountId=" + _accountId +
				", _name='" + _name + '\'' +
				", _uuid=" + _uuid +
				", _player=" + _player +
				", _primary=" + _primary +
				", _extra=[" + _extra.stream().map(PermissionGroup::toString).collect(Collectors.joining(", ")) + "]" +
				'}';
	}
}