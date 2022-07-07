package mineplex.gemhunters.mount;

import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MountData
{

	private final Player _player;
	private Horse _entity;
	private long _cooldown;
	private ItemStack _item;

	MountData(Player player)
	{
		_player = player;
	}

	public Player getPlayer()
	{
		return _player;
	}

	public void onSpawn(Horse entity, long cooldown, ItemStack item)
	{
		_entity = entity;
		_cooldown = cooldown;
		_item = item;
	}

	public void onRemove()
	{
		_entity = null;
	}

	public Horse getEntity()
	{
		return _entity;
	}

	public long getCooldown()
	{
		return _cooldown;
	}

	public ItemStack getItem()
	{
		return _item;
	}
}
