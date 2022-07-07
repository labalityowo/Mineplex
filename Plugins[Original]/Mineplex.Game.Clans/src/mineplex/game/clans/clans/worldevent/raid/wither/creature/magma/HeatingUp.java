package mineplex.game.clans.clans.worldevent.raid.wither.creature.magma;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.UtilMath;
import mineplex.game.clans.clans.worldevent.raid.wither.challenge.six.ChallengeSix;

public class HeatingUp extends Cataclysm
{
	private Location _center;
	private int _ticks;
	
	public HeatingUp(ChallengeSix challenge, Magmus magmus)
	{
		super(challenge, magmus);
	}

	@Override
	protected String getAnnouncement()
	{
		return "The room is heating up! Quickly, head to a safe location!";
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onStart()
	{
		Magmus.HeatingRoom = true;
		_center = Challenge.getRaid().getWorldData().getCustomLocs("C_SIX_C1S").get(0);
		for (int x = -1; x <= 1; x++)
		{
			for (int z = -1; z <= 1; z++)
			{
				_center.getBlock().getRelative(x, -1, z).setTypeIdAndData(Material.STAINED_GLASS.getId(), DyeColor.GREEN.getWoolData(), true);
			}
		}
	}
	
	@Override
	protected void onEnd()
	{
		for (int x = -1; x <= 1; x++)
		{
			for (int z = -1; z <= 1; z++)
			{
				_center.getBlock().getRelative(x, -1, z).setType(Material.STONE);
			}
		}
		Magmus.HeatingRoom = false;
	}

	@Override
	protected void tick()
	{
		_ticks++;
		if (_ticks > (20 * 10) && _ticks <= (20 * 30))
		{
			for (Player player : Challenge.getRaid().getPlayers())
			{
				if (UtilMath.offset(player.getLocation(), _center) > 1)
				{
					player.setFireTicks(1);
					Challenge.getRaid().getDamageManager().NewDamageEvent(player, Magmus.getEntity(), null, DamageCause.FIRE, 1, false, true, true, Magmus.getEntity().getName(), "Heat Room");
				}
			}
		}
		if (_ticks > (20 * 33))
		{
			end();
		}
	}
}