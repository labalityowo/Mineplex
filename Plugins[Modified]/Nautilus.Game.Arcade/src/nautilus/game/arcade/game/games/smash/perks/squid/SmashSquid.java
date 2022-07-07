package nautilus.game.arcade.game.games.smash.perks.squid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.smash.kits.KitSkySquid;
import nautilus.game.arcade.game.games.smash.perks.SmashUltimate;

public class SmashSquid extends SmashUltimate
{

	private int _rate;
	private int _maxRange;
	private int _damage;
	private int _damageRadius;
	private int _knockbackMagnitude;

	public SmashSquid()
	{
		super("Storm Squid", new String[] {}, Sound.SPLASH2, 0);
	}

	@Override
	public void setupValues()
	{
		super.setupValues();

		_rate = getPerkInt("Rate (ms)");
		_maxRange = getPerkInt("Max Range");
		_damage = getPerkInt("Damage");
		_damageRadius = getPerkInt("Damage Radius");
		_knockbackMagnitude = getPerkInt("Knockback Magnitude");
	}

	@Override
	public void activate(Player player)
	{
		super.activate(player);
		
		Game game = Manager.GetGame();
		
		game.WorldWeatherEnabled = true;
		game.WorldData.World.setStorm(true);
		game.WorldData.World.setThundering(true);
		game.WorldData.World.setThunderDuration(9999);
		
		if (Kit instanceof KitSkySquid)
		{
			KitSkySquid squid = (KitSkySquid) Kit;
			
			squid.giveSmashItems(player);
		}
	}

	@Override
	public void cancel(Player player)
	{
		super.cancel(player);

		Game game = Manager.GetGame();
		
		game.WorldWeatherEnabled = false;
		
		player.setFlying(false);
	}

	@EventHandler(priority = EventPriority.LOW) // Happen before activation of
												// Super
	public void lightningStrike(PlayerInteractEvent event)
	{
		final Player player = event.getPlayer();

		if (!isUsingUltimate(player))
		{
			return;
		}

		if (event.getAction() == Action.PHYSICAL)
		{
			return;
		}

		Block block = UtilPlayer.getTarget(player, UtilBlock.blockAirFoliageSet, _maxRange);

		if (block == null)
		{
			return;
		}

		final Location loc = block.getLocation().add(0.5, 0.5, 0.5);

		if (!Recharge.Instance.use(player, GetName() + " Strike", _rate, false, false))
		{
			return;
		}

		// Warning
		UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, loc.clone().add(0, 0.5, 0), 1f, 1f, 1f, 0.1f, 40, ViewDist.MAX, UtilServer.getPlayers());
		UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, loc.clone().add(0, 0.5, 0), 0, 0, 0, 0, 1, ViewDist.MAX, UtilServer.getPlayers());

		Manager.runSyncLater(() ->
		{
			// Warning
			player.getWorld().spigot().strikeLightningEffect(loc, false);

			Map<LivingEntity, Double> targets = UtilEnt.getInRadius(loc, _damageRadius);

			for (LivingEntity cur : targets.keySet())
			{
				if (cur.equals(player))
				{
					continue;
				}

				// Damage Event
				Manager.GetDamage().NewDamageEvent(cur, player, null, DamageCause.CUSTOM, _damage * targets.get(cur), false, true, false, player.getName(), GetName());

				// Velocity
				UtilAction.velocity(cur, UtilAlg.getTrajectory(loc, cur.getLocation()), 3 * targets.get(cur), false, 0, 1 * targets.get(cur), 2, true);
			}
		}, 10);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void attackCancel(CustomDamageEvent event)
	{
		if (event.IsCancelled())
		{
			return;
		}

		Player player = event.GetDamagerPlayer(true);

		if (player == null)
		{
			return;
		}

		if (!isUsingUltimate(player))
		{
			return;
		}

		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
		{
			return;
		}

		event.SetCancelled("Wither Form Melee Cancel");
	}

	@EventHandler
	public void flight(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (UUID uuid : getLastUltimate().keySet())
		{
			Player player = UtilPlayer.searchExact(uuid);
			
			if (player == null)
			{
				continue;
			}
			
			if (player.isFlying())
			{
				continue;
			}

			player.setAllowFlight(true);
			player.setFlying(true);
		}
	}

	@EventHandler
	public void flightBump(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
		{
			return;
		}

		for (UUID uuid : getLastUltimate().keySet())
		{
			Player player = UtilPlayer.searchExact(uuid);
			
			if (player == null)
			{
				continue;
			}
			
			List<Location> collisions = new ArrayList<>();

			// Bump
			for (Block block : UtilBlock.getInRadius(player.getLocation().add(0, 0.5, 0), 1.5d).keySet())
			{
				if (!UtilBlock.airFoliage(block))
				{
					collisions.add(block.getLocation().add(0.5, 0.5, 0.5));
				}
			}

			Vector vec = UtilAlg.getAverageBump(player.getLocation(), collisions);

			if (vec == null)
			{
				continue;
			}

			UtilAction.velocity(player, vec, 0.6, false, 0, 0.4, 10, true);
		}
	}

	@EventHandler
	public void knockback(CustomDamageEvent event)
	{
		if (event.GetReason() == null || !event.GetReason().contains(GetName()))
		{
			return;
		}

		event.AddKnockback(GetName(), _knockbackMagnitude);
	}
}
