package nautilus.game.arcade.game.games.smash.perks.zombie;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.games.smash.TeamSuperSmash;
import nautilus.game.arcade.game.games.smash.perks.SmashUltimate;
import nautilus.game.arcade.kit.perks.data.NightLivingDeadData;

public class SmashZombie extends SmashUltimate
{
	private static final int DURATION = 30000;
	
	private List<NightLivingDeadData> _night = new ArrayList<>();

	private HashSet<Material> _ignoreList = new HashSet<>();;

	public SmashZombie()
	{
		super("Night of the Living Dead", new String[] {}, Sound.AMBIENCE_CAVE, DURATION);
	}

	@Override
	public void activate(Player player)
	{
		super.activate(player);
		
		_night.add(new NightLivingDeadData(player));
	}
	
	@Override
	public void cancel(Player player)
	{
		super.cancel(player);
		
		Iterator<NightLivingDeadData> nightIter = _night.iterator();

		while (nightIter.hasNext())
		{
			NightLivingDeadData data = nightIter.next();

			// Expire
			if (data.Player.equals(player))
			{
				nightIter.remove();

				for (Zombie zombie : data.Zombies)
				{
					zombie.damage(1000);
				}
				
				return;
			}
		}

	}

	@EventHandler
	public void timeUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !Manager.GetGame().IsLive())
		{
			return;
		}
		
		if (_night.isEmpty() && Manager.GetGame().WorldTimeSet != 12000)
		{
			Manager.GetGame().WorldTimeSet = (Manager.GetGame().WorldTimeSet + 50) % 24000;
		}
		else if (!_night.isEmpty() && Manager.GetGame().WorldTimeSet != 18000)
		{
			Manager.GetGame().WorldTimeSet = (Manager.GetGame().WorldTimeSet + 50) % 24000;
		}
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		Iterator<NightLivingDeadData> nightIter = _night.iterator();

		while (nightIter.hasNext())
		{
			NightLivingDeadData data = nightIter.next();

			// Expire
			if (UtilTime.elapsed(data.Time, DURATION))
			{
				nightIter.remove();

				for (Zombie zombie : data.Zombies)
				{
					zombie.damage(1000);
				}
				
				continue;
			}

			// Spawn
			if (UtilTime.elapsed(data.LastSpawn, 1000))
			{
				Location origin = UtilAlg.Random(Manager.GetGame().GetPlayers(true)).getLocation();
				Location loc = findSpawn(origin);

				if (Math.abs(loc.getY() - origin.getY()) > 6)
				{
					continue;
				}
				
				if (!UtilBlock.airFoliage(loc.getBlock()) || !UtilBlock.airFoliage(loc.getBlock().getRelative(BlockFace.UP)))
				{
					continue;
				}
				
				// Set Spawned
				data.LastSpawn = System.currentTimeMillis();

				// Move Down
				loc.subtract(0, 1, 0);

				// Spawn
				Manager.GetGame().CreatureAllowOverride = true;
				Zombie zombie = loc.getWorld().spawn(loc, Zombie.class);
				Manager.GetGame().CreatureAllowOverride = false;

				data.Zombies.add(zombie);

				// Pop up
				zombie.setVelocity(new Vector(0, 0.4, 0));
				// zombie.addPotionEffect(new
				// PotionEffect(PotionEffectType.SPEED, 9999, 1, true));

				// Effect
				zombie.getWorld().playSound(zombie.getLocation(), Sound.ZOMBIE_IDLE, 1f, 0.75f);

				loc.getWorld().playEffect(loc, Effect.STEP_SOUND, loc.getBlock().getType());
			}
		}
	}

	@EventHandler
	public void target(EntityTargetEvent event)
	{
		for (NightLivingDeadData data : _night)
		{
			if (data.Zombies.contains(event.getEntity()))
			{
				if (Manager.GetGame() instanceof TeamSuperSmash && event.getTarget() instanceof Player)
				{
					TeamSuperSmash smash = (TeamSuperSmash) Manager.GetGame();
					Player targetPlayer = (Player) event.getTarget();

					if (smash.GetTeam(data.Player).equals(smash.GetTeam(targetPlayer)))
					{
						event.setCancelled(true);
					}
				}
				if (data.Player.equals(event.getTarget()))
				{
					event.setCancelled(true);
				}
			}
		}
	}

	public Location findSpawn(Location area)
	{
		return UtilBlock.getHighest(area.getWorld(), (int) (area.getX() + Math.random() * 24 - 12), (int) (area.getZ() + Math.random() * 24 - 12), _ignoreList).getLocation().add(0.5, 0.5, 0.5);
	}
}
