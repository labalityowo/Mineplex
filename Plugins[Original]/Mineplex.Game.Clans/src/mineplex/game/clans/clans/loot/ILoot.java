package mineplex.game.clans.clans.loot;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public interface ILoot
{
	public void dropLoot(Location location);

	public ItemStack getItemStack();

}
