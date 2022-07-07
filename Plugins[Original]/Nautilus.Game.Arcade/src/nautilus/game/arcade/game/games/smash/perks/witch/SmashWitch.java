package nautilus.game.arcade.game.games.smash.perks.witch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.disguise.disguises.DisguiseBat;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.smash.kits.KitWitch;
import nautilus.game.arcade.game.games.smash.perks.SmashKit;
import nautilus.game.arcade.game.games.smash.perks.SmashUltimate;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkDoubleJump;
import nautilus.game.arcade.kit.perks.data.SonicBoomData;

public class SmashWitch extends SmashUltimate
{
	
	private int _cooldown;
	private int _maxTime;
	private int _hitBox;
	private int _damageRadius;
	private int _damage;
	private int _flapCooldown;
	private int _knockbackMagnitude;
	
	private List<SonicBoomData> _sonic = new ArrayList<>();

	public SmashWitch()
	{
		super("Bat Form", new String[] {}, Sound.BAT_HURT, 0);
	}

	@Override
	public void setupValues()
	{
		super.setupValues();

		_cooldown = getPerkInt("Cooldown (ms)");
		_maxTime = getPerkTime("Max Time");
		_hitBox = getPerkInt("Hit Box");
		_damageRadius = getPerkInt("Damage Radius");
		_damage = getPerkInt("Damage");
		_flapCooldown = getPerkInt("Flap Cooldown (ms)");
		_knockbackMagnitude = getPerkInt("Knockback Magnitude");
	}

	@Override
	public void activate(Player player)
	{
		super.activate(player);
		
		if (Kit instanceof KitWitch)
		{
			KitWitch kit = (KitWitch) Kit;
			
			kit.giveSmashItems(player);
		}
		
		for (Perk perk : Kit.GetPerks())
		{
			if (perk instanceof PerkDoubleJump)
			{
				((PerkDoubleJump) perk).disableForPlayer(player);
			}
		}
		
		Manager.GetDisguise().undisguise(Manager.GetDisguise().getActiveDisguise(player));

		SmashKit kit = (SmashKit) Kit;
		kit.disguise(player, DisguiseBat.class);
	}

	@Override
	public void cancel(Player player)
	{
		super.cancel(player);
		
		for (Perk perk : Kit.GetPerks())
		{
			if (perk instanceof PerkDoubleJump)
			{
				((PerkDoubleJump) perk).enableForPlayer(player);
			}
		}
		
		Manager.GetDisguise().undisguise(Manager.GetDisguise().getActiveDisguise(player));

		SmashKit kit = (SmashKit) Kit;
		kit.disguise(player);
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
		
		event.SetCancelled("Bat Form Melee Cancel");
	}

	@EventHandler(priority = EventPriority.LOW) // Happen before activation of
												// Super
	public void sonicBoom(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (!isUsingUltimate(player))
		{
			return;
		}
		
		if (event.getAction() == Action.PHYSICAL)
		{
			return;
		}
		
		if (!Recharge.Instance.use(player, GetName() + " Screech", _cooldown, false, false))
		{
			return;
		}
		
		// Effect
		player.getWorld().playSound(player.getLocation(), Sound.BAT_HURT, 1f, 0.75f);

		_sonic.add(new SonicBoomData(player));
	}

	@EventHandler
	public void sonicBoomUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		Iterator<SonicBoomData> sonicIter = _sonic.iterator();

		while (sonicIter.hasNext())
		{
			SonicBoomData data = sonicIter.next();

			// Time Boom
			if (UtilTime.elapsed(data.Time, _maxTime))
			{
				sonicIter.remove();
				explode(data);
				continue;
			}

			// Block Boom
			if (!UtilBlock.airFoliage(data.Location.getBlock()))
			{
				sonicIter.remove();
				explode(data);
				continue;
			}

			// Proxy Boom
			for (Player player : Manager.GetGame().GetPlayers(true))
			{
				if (Manager.isSpectator(player))
				{
					continue;
				}
				
				if (player.equals(data.Shooter))
				{
					continue;
				}
				
				if (UtilMath.offset(player.getLocation().add(0, 1, 0), data.Location) < _hitBox)
				{
					sonicIter.remove();
					explode(data);
					continue;
				}
			}

			// Move
			data.Location.add(data.Direction.clone().multiply(1));

			// Effect
			UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, data.Location, 0, 0, 0, 0, 1, ViewDist.MAX, UtilServer.getPlayers());
			data.Location.getWorld().playSound(data.Location, Sound.FIZZ, 1f, 2f);
		}
	}

	private void explode(SonicBoomData data)
	{
		// Effect
		UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION, data.Location, 0, 0, 0, 0, 1, ViewDist.MAX, UtilServer.getPlayers());
		data.Location.getWorld().playSound(data.Location, Sound.EXPLODE, 1f, 1.5f);

		// Damage
		Map<LivingEntity, Double> targets = UtilEnt.getInRadius(data.Location, _damageRadius);
		
		for (LivingEntity cur : targets.keySet())
		{
			if (cur.equals(data.Shooter))
			{
				continue;
			}
			
			Manager.GetDamage().NewDamageEvent(cur, data.Shooter, null, DamageCause.CUSTOM, _damage * targets.get(cur) + 0.5, true, false, false, data.Shooter.getName(), GetName());
		}
	}

	@EventHandler
	public void flap(PlayerToggleFlightEvent event)
	{
		Player player = event.getPlayer();

		if (Manager.isSpectator(player))
		{
			return;
		}
		
		if (!isUsingUltimate(player))
		{
			return;
		}
		
		if (player.getGameMode() == GameMode.CREATIVE)
		{
			return;
		}
		
		event.setCancelled(true);
		player.setFlying(false);

		// Disable Flight
		player.setAllowFlight(false);

		// Velocity
		UtilAction.velocity(player, player.getLocation().getDirection(), 0.8, false, 0, 0.8, 1, true);

		// Sound
		player.getWorld().playSound(player.getLocation(), Sound.BAT_TAKEOFF, (float) (0.3 + player.getExp()), (float) (Math.random() / 2 + 0.5));

		// Set Recharge
		Recharge.Instance.use(player, GetName() + " Flap", _flapCooldown, false, false);
	}

	@EventHandler
	public void flapRecharge(UpdateEvent event)
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
			
			if (Manager.isSpectator(player))
			{
				continue;
			}
			
			if (UtilEnt.isGrounded(player) || UtilBlock.solid(player.getLocation().getBlock().getRelative(BlockFace.DOWN)))
			{
				player.setAllowFlight(true);
			}
			else if (Recharge.Instance.usable(player, GetName() + " Flap"))
			{
				player.setAllowFlight(true);
			}
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
