package mineplex.core.punish;

import org.bukkit.Material;

import mineplex.core.account.permissions.Permission;

public enum Category
{
	ChatOffense("Chat", Material.BOOK_AND_QUILL, false, 3, Punish.Perm.PUNISHMENT_COMMAND),
	Exploiting("Gameplay", Material.HOPPER, true, 1, Punish.Perm.PUNISHMENT_COMMAND), // General Offense
	Hacking("Hacking", Material.IRON_SWORD, true, 3, Punish.Perm.PUNISHMENT_COMMAND), // Illegal Mods
	Warning("Warning", Material.PAPER, false, 1, Punish.Perm.PUNISHMENT_COMMAND),
	PermMute("Permanent Mute", Material.BOOK_AND_QUILL, false, 1, Punish.Perm.FULL_PUNISHMENT_ACCESS),
	ReportAbuse("Report Ban", Material.BOOK, false, 1, Punish.Perm.REPORT_BAN_ACCESS), // Abusing /report command
	Other("Permanent Ban", Material.REDSTONE_BLOCK, true, 1, Punish.Perm.FULL_PUNISHMENT_ACCESS); // Represents perm ban - (or old perm mutes)

	String _name;
	Material _icon;
	boolean _ban;
	int _maxSeverity;
	Permission _neededPermission;

	Category(String name, Material icon, boolean ban, int maxSeverity, Permission neededPermission)
	{
		_name = name;
		_icon = icon;
		_ban = ban;
		_maxSeverity = maxSeverity;
		_neededPermission = neededPermission;

	}

	public boolean isBan()
	{
		return _ban;
	}

	public String getName()
	{
		return _name;
	}

	public Material getIcon()
	{
		return _icon;
	}

	public int getMaxSeverity()
	{
		return _maxSeverity;
	}

	public Permission getNeededPermission()
	{
		return _neededPermission;
	}

	public static boolean contains(String s)
    {
        try 
        {
        	Category.valueOf(s);
            return true;
        } 
        catch (Exception e) 
        {
            return false;
        }
     }
}
