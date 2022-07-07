package mineplex.game.clans.clans.loot;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.game.clans.items.economy.GoldToken;

public class GoldTokenLoot implements ILoot
{
	private int _minGold;
	private int _maxGold;

	public GoldTokenLoot(int minGold, int maxGold)
	{
		_minGold = minGold;
		_maxGold = maxGold;
	}

	@Override
	public void dropLoot(Location location)
	{
		int gold = _minGold + UtilMath.r(_maxGold - _minGold);

		GoldToken token = new GoldToken(gold);
		UtilFirework.playFirework(location.clone().add(0, 3, 0), Type.BALL, Color.YELLOW, true, false);
		location.getWorld().dropItemNaturally(location.clone().add(0, 3, 0), token.toItemStack());
	}

	@Override
	public ItemStack getItemStack()
	{
		int gold = _minGold + UtilMath.r(_maxGold - _minGold);
		GoldToken token = new GoldToken(gold);
		return token.toItemStack();
	}
}