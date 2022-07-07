package mineplex.game.clans.clans.loot;

import org.bukkit.Color;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.game.clans.clans.mounts.Mount.MountType;
import mineplex.game.clans.clans.mounts.MountClaimToken;

public class MountLoot implements ILoot
{
	private int _minStars, _maxStars;
	private MountType[] _types;
	
	public MountLoot(int minStars, int maxStars, MountType... types)
	{
		_minStars = Math.max(minStars, 1);
		_maxStars = Math.min(maxStars, 3);
		if (types.length == 0)
		{
			_types = MountType.values();
		}
		else
		{
			_types = types;
		}
	}
	
	@Override
	public void dropLoot(Location location)
	{
		MountClaimToken token = new MountClaimToken(UtilMath.rRange(_minStars, _maxStars), UtilMath.rRange(_minStars, _maxStars), UtilMath.rRange(_minStars, _maxStars), UtilMath.randomElement(_types));
		UtilFirework.playFirework(location.clone().add(0, 3, 0), Type.BALL, Color.SILVER, true, false);
		location.getWorld().dropItemNaturally(location.clone().add(0, 3, 0), token.toItem());
	}
	
	@Override
	public ItemStack getItemStack()
	{
		return new MountClaimToken(UtilMath.rRange(_minStars, _maxStars), UtilMath.rRange(_minStars, _maxStars), UtilMath.rRange(_minStars, _maxStars), UtilMath.randomElement(_types)).toItem();
	}
}