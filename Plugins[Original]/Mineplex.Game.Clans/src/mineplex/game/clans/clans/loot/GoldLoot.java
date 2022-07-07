package mineplex.game.clans.clans.loot;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilMath;
import mineplex.game.clans.economy.GoldManager;

public class GoldLoot implements ILoot
{
	private GoldManager _goldManager;
	private int _min;
	private int _max;

	public GoldLoot(GoldManager goldManager, int min, int max)
	{
		_goldManager = goldManager;
		_min = min;
		_max = max;
	}

	@Override
	public void dropLoot(Location location)
	{
		int count = _min + UtilMath.r(_max - _min);
		_goldManager.dropGold(location.clone().add(0, 1, 0), count);
	}

	@Override
	public ItemStack getItemStack()
	{
		return null;
	}
}