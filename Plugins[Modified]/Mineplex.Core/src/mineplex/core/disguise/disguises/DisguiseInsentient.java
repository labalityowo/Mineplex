package mineplex.core.disguise.disguises;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;

import mineplex.core.account.permissions.PermissionGroup;
import net.minecraft.server.v1_8_R3.EntityInsentient;

public abstract class DisguiseInsentient extends DisguiseLiving
{
	private boolean _showArmor;

	public DisguiseInsentient(EntityType disguiseType, org.bukkit.entity.Entity entity)
	{
		super(disguiseType, entity);

		DataWatcher.a(3, Byte.valueOf((byte) 0), EntityInsentient.META_CUSTOMNAME_VISIBLE, false);
		DataWatcher.a(2, "", EntityInsentient.META_CUSTOMNAME, "");
	}

	public void setName(String name)
	{
		setName(name, null);
	}

	public void setName(String name, PermissionGroup group)
	{
		if (group != null)
		{
			if (!group.getDisplay(false, false, false, false).isEmpty())
			{
				name = group.getDisplay(true, true, true, false) + " " + ChatColor.RESET + name;
			}
		}

		DataWatcher.watch(2, name, EntityInsentient.META_CUSTOMNAME, name);
	}

	public boolean hasCustomName()
	{
		return DataWatcher.getString(2).length() > 0;
	}

	public void setCustomNameVisible(boolean visible)
	{
		DataWatcher.watch(3, (byte) (visible ? 1 : 0), EntityInsentient.META_CUSTOMNAME_VISIBLE, visible);
	}

	public boolean getCustomNameVisible()
	{
		return DataWatcher.getByte(11) == 1;
	}

	public boolean armorVisible()
	{
		return _showArmor;
	}

	public void showArmor()
	{
		_showArmor = true;
	}

	public void hideArmor()
	{
		_showArmor = false;
	}
}