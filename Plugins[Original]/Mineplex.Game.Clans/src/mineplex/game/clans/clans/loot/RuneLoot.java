package mineplex.game.clans.clans.loot;

import org.bukkit.Color;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.items.runes.RuneManager.RuneAttribute;

public class RuneLoot implements ILoot
{
	private RuneAttribute[] _types;
	
	public RuneLoot(RuneAttribute... types)
	{
		if (types.length == 0)
		{
			_types = RuneAttribute.values();
		}
		else
		{
			_types = types;
		}
	}
	
	@Override
	public void dropLoot(Location location)
	{
		RuneAttribute attribute = UtilMath.randomElement(_types);
		ItemStack item = ClansManager.getInstance().getGearManager().getRuneManager().getRune(attribute);
		UtilFirework.playFirework(location.clone().add(0, 3, 0), Type.BALL, Color.PURPLE, true, false);
		location.getWorld().dropItemNaturally(location.clone().add(0, 3, 0), item);
	}
	
	@Override
	public ItemStack getItemStack()
	{
		RuneAttribute attribute = UtilMath.randomElement(_types);
		return ClansManager.getInstance().getGearManager().getRuneManager().getRune(attribute);
	}
}