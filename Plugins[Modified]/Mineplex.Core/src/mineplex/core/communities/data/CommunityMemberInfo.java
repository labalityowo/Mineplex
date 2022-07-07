package mineplex.core.communities.data;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilTime;
import mineplex.core.itemstack.ItemBuilder;

public class CommunityMemberInfo
{
	public String Name;
	public final UUID UUID;
	public final int AccountId;
	public CommunityRole Role;
	public boolean ReadingChat = true;
	private ItemStack _memberIcon, _outsiderIcon;
	private long _lastLogin;
	private boolean _online = false;
	private String _currentServer = "";
	
	public CommunityMemberInfo(String name, UUID uuid, int accountId, CommunityRole role, long lastLogin)
	{
		Name = name;
		UUID = uuid;
		AccountId = accountId;
		Role = role;
		_lastLogin = lastLogin;
		
		buildIcons();
	}
	
	private void buildIcons()
	{
		ItemBuilder builder = new ItemBuilder(Material.SKULL_ITEM).setTitle(C.cGreenB + Name).setLore(C.cRed, C.cYellow + "Role " + C.cWhite + Role.getDisplay());
		_outsiderIcon = builder.build();
		builder.setTitle(!_online ? C.cRedB + Name : C.cGreenB + Name);
		if (_online)
		{
			builder.addLore(C.cYellow + "Server " + C.cWhite + _currentServer);
		}
		else
		{
			builder.addLore(C.cYellow + "Last Seen " + C.cWhite + UtilTime.MakeStr(System.currentTimeMillis() - _lastLogin) + " Ago");
		}
		if (_online)
		{
			builder.setData((short)3);
			builder.setPlayerHead(Name);
		}
		_memberIcon = builder.build();
	}
	
	public void setOffline()
	{
		_online = false;
		_currentServer = "";
		buildIcons();
	}
	
	public void updateName(String name)
	{
		if (name == null)
		{
			return;
		}
		Name = name;
		buildIcons();
	}
	
	public void updateRole(CommunityRole role)
	{
		Role = role;
		buildIcons();
	}
	
	public void update(long lastLogin, boolean online, String currentServer)
	{
		_lastLogin = lastLogin;
		_online = online;
		_currentServer = currentServer;
		
		buildIcons();
	}
	
	public boolean isOnline()
	{
		return _online;
	}
	
	public ItemStack getRepresentation(CommunityRole viewerRole)
	{
		if (viewerRole == null)
		{
			return _outsiderIcon;
		}
		
		ItemBuilder builder = new ItemBuilder(_memberIcon);
		if ((viewerRole == CommunityRole.LEADER && Role != CommunityRole.LEADER) || (viewerRole.ordinal() < Role.ordinal()))
		{
			builder.addLore(C.cGold);
		}
		if (viewerRole == CommunityRole.LEADER && Role != CommunityRole.LEADER)
		{
			builder.addLore(C.cYellow + (Role == CommunityRole.COLEADER ? "Shift-" : "" ) + "Left Click " + C.cWhite + "Promote");
			if (Role != CommunityRole.MEMBER)
			{
				builder.addLore(C.cYellow + "Right Click " + C.cWhite + "Demote");
			}
		}
		if (viewerRole.ordinal() < Role.ordinal())
		{
			builder.addLore(C.cYellow + "Shift-Right Click " + C.cWhite + "Kick");
		}
		return builder.build();
	}
}