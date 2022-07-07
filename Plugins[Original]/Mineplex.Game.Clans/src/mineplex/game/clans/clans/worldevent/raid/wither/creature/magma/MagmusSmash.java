package mineplex.game.clans.clans.worldevent.raid.wither.creature.magma;

import java.util.Map;

import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.game.clans.clans.worldevent.api.BossPassive;

public class MagmusSmash extends BossPassive<Magmus, MagmaCube>
{
	private long _lastUse;
	
	public MagmusSmash(Magmus creature)
	{
		super(creature);
		_lastUse = System.currentTimeMillis();
	}
	
	private void slam()
	{
		//Action
		Map<Player, Double> targets = UtilPlayer.getInRadius(getLocation(), 5.5d + 0.5 * 5);
		getBoss().TeleportBackASAP = false;
		getEntity().setVelocity(new Vector(0, 5, 0));
		UtilServer.runSyncLater(() -> getBoss().TeleportBackASAP = true, 3 * 20);
		for (Player player : targets.keySet())
		{
			getBoss().getEvent().getDamageManager().NewDamageEvent(player, getEntity(), null, 
					DamageCause.CUSTOM, 6 * targets.get(player) + 0.5, false, true, false,
					getEntity().getName(), "Smash");

			//Velocity
			UtilAction.velocity(player, 
					UtilAlg.getTrajectory2d(getLocation().toVector(), player.getLocation().toVector()), 
					2 + 2 * targets.get(player), true, 0, 1.2 + 1.0 * targets.get(player), 3, true);

			//Condition
			getBoss().getEvent().getCondition().Factory().Falling("Smash", player, getEntity(), 10, false, true);
		}
	}
	
	@Override
	public int getCooldown()
	{
		return 10;
	}
	
	@Override
	public boolean isProgressing()
	{
		return false;
	}

	@Override
	public void tick()
	{
		if (getBoss().HeatingRoom)
		{
			return;
		}
		if (UtilTime.elapsed(_lastUse, getCooldown() * 1000))
		{
			_lastUse = System.currentTimeMillis();
			slam();
		}
	}
}