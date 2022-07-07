package mineplex.game.clans.clans.loot;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import mineplex.game.clans.items.GearManager;

public class GearLoot implements ILoot
{
	private GearManager _gearManager;
	
	public GearLoot(GearManager gearManager)
	{
		_gearManager = gearManager;
	}
	
	@Override
	public void dropLoot(Location location)
	{
		_gearManager.spawnItem(location.clone().add(0, 3, 0));
	}
	
	@Override
	public ItemStack getItemStack()
	{
		return _gearManager.generateItem().toItemStack();
	}
}