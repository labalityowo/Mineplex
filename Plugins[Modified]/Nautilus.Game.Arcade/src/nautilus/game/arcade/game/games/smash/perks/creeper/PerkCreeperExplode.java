package nautilus.game.arcade.game.games.smash.perks.creeper;

import mineplex.core.common.util.*;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguiseCreeper;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.smash.perks.SmashPerk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.*;

public class PerkCreeperExplode extends SmashPerk
{

	private int _cooldown;
	private int _warmup;
	private int _radiusNormal;
	private int _radiusSmash;
	private int _damageNormal;
	private int _damageSmash;
	private double _damageReduction;
	private int _spawnRemovalRadius;
	private float _knockbackMagnitude;
	private int _blockDestroyRadius;
	private int _blockRegeneration;

	private Map<UUID, Long> _active = new HashMap<>();
	private Map<Location, Long> _removedSpawns = new HashMap<>();

	public PerkCreeperExplode()
	{
		super("Explode", new String[]{C.cYellow + "Right-Click" + C.cGray + " with Shovel use " + C.cGreen + "Explosive Leap"});
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_warmup = getPerkInt("Warmup (ms)");
		_radiusNormal = getPerkInt("Radius Normal");
		_radiusSmash = getPerkInt("Radius Smash");
		_damageNormal = getPerkInt("Damage Normal");
		_damageSmash = getPerkInt("Damage Smash");
		_damageReduction = getPerkPercentage("Damage Reduction");
		_spawnRemovalRadius = (int) Math.pow(getPerkInt("Spawn Removal Radius"), 2);
		_knockbackMagnitude = getPerkFloat("Knockback Magnitude");
		_blockDestroyRadius = getPerkInt("Block Destroy Radius");
		_blockRegeneration = getPerkTime("Block Regeneration Time");
	}

	@EventHandler
	public void Activate(PlayerInteractEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		if (!UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}

		if (UtilBlock.usable(event.getClickedBlock()))
		{
			return;
		}

		Player player = event.getPlayer();

		if (!UtilItem.isSpade(player.getItemInHand()))
		{
			return;
		}

		if (!hasPerk(player))
		{
			return;
		}

		if (isSuperActive(player))
		{
			return;
		}

		if (!Recharge.Instance.use(player, GetName(), _cooldown, true, true))
		{
			return;
		}

		_active.put(player.getUniqueId(), System.currentTimeMillis());

		IncreaseSize(player);

		UtilPlayer.message(player, F.main("Skill", "You are charging " + F.skill(GetName()) + "."));
	}

	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		Iterator<UUID> chargeIterator = _active.keySet().iterator();

		while (chargeIterator.hasNext())
		{
			UUID key = chargeIterator.next();
			Player player = UtilPlayer.searchExact(key);

			if (player == null)
			{
				chargeIterator.remove();
				continue;
			}

			long elapsed = (System.currentTimeMillis() - _active.get(key));

			// Idle in Air
			UtilAction.zeroVelocity(player);

			// Sound
			float volume = (float) 0.5F + elapsed / 1000F;
			player.getWorld().playSound(player.getLocation(), Sound.CREEPER_HISS, volume, volume);

			IncreaseSize(player);

			player.setExp(Math.min(0.999f, elapsed / (float) _warmup));

			// Not Detonated
			if (!UtilTime.elapsed(_active.get(key), _warmup))
			{
				continue;
			}

			player.setExp(0);

			chargeIterator.remove();

			// Unpower
			DecreaseSize(player);

			// Explode
			if (!isSuperActive(player))
			{
				// Effect
				UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION, player.getLocation(), 0, 0, 0, 0, 1, ViewDist.MAX, UtilServer.getPlayers());
				player.getWorld().playSound(player.getLocation(), Sound.EXPLODE, 2f, 1f);
			}
			else
			{
				// Particles
				UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION, player.getLocation(), 5f, 5f, 5f, 0, 20, ViewDist.MAX, UtilServer.getPlayers());

				// Sound
				for (int i = 0; i < 4; i++)
				{
					player.getWorld().playSound(player.getLocation(), Sound.EXPLODE, (float) (2 + Math.random() * 4), (float) (Math.random() + 0.2));
				}

				// Blocks
				Collection<Block> blocks = UtilBlock.getInRadius(player.getLocation(), _blockDestroyRadius).keySet();

				blocks.removeIf(b -> b.getType() == Material.STATIONARY_LAVA || b.getType() == Material.LAVA || b.getType() == Material.BEDROCK);

				Manager.GetExplosion().BlockExplosion(blocks, player.getLocation(), false);

				// Remove Spawns
				Iterator<Location> iterator = Manager.GetGame().GetTeam(player).GetSpawns().iterator();

				while (iterator.hasNext())
				{
					Location spawn = iterator.next();

					if (UtilMath.offsetSquared(player.getLocation(), spawn) < _spawnRemovalRadius)
					{
						_removedSpawns.put(spawn, System.currentTimeMillis());
						iterator.remove();
					}
				}

				// If all spawns have been destroyed revert to using the
				// spectator spawn
				for (GameTeam team : Manager.GetGame().GetTeamList())
				{
					if (team.GetSpawns().isEmpty())
					{
						team.GetSpawns().add(Manager.GetGame().GetSpectatorLocation());
					}
				}
			}

			double maxRange = isSuperActive(player) ? _radiusSmash : _radiusNormal;
			double damage = isSuperActive(player) ? _damageSmash : _damageNormal;

			// Damage
			for (LivingEntity ent : UtilEnt.getInRadius(player.getLocation(), maxRange).keySet())
			{
				if (ent.equals(player))
				{
					continue;
				}

				double dist = UtilMath.offset(player.getLocation(), ent.getLocation());

				if (UtilPlayer.isSpectator(player))
				{
					continue;
				}

				LivingEntity livingEnt = (LivingEntity) ent;

				double scale = 0.1 + 0.9 * ((maxRange - dist) / maxRange);

				// Damage Event
				Manager.GetDamage().NewDamageEvent(livingEnt, player, null, DamageCause.CUSTOM, damage * scale, true, true, false, player.getName(), isSuperActive(player) ? "Atomic Blast" : GetName());
			}

			// Velocity
			UtilAction.velocity(player, 1.8, 0.2, 1.4, true);

			// Inform
			if (!isSuperActive(player))
			{
				UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(GetName()) + "."));
			}
		}
	}

	@EventHandler
	public void toggleSneak(PlayerToggleSneakEvent event)
	{
		Player player = event.getPlayer();

		if (!_active.containsKey(player.getUniqueId()) || isSuperActive(player))
		{
			return;
		}

		_active.remove(player.getUniqueId());
		DecreaseSize(player);
		UtilPlayer.message(player, F.main("Skill", "You cancelled " + F.skill(GetName()) + "."));
	}

	public void activate(Player player)
	{
		_active.put(player.getUniqueId(), System.currentTimeMillis());
	}

	public DisguiseCreeper GetDisguise(Player player)
	{
		DisguiseBase disguise = Manager.GetDisguise().getActiveDisguise(player);

		if (disguise == null)
		{
			return null;
		}

		if (!(disguise instanceof DisguiseCreeper))
		{
			return null;
		}

		return (DisguiseCreeper) disguise;
	}

	public int GetSize(Player player)
	{
		DisguiseCreeper creeper = GetDisguise(player);

		if (creeper == null)
		{
			return 0;
		}

		return creeper.bV();
	}

	public void DecreaseSize(Player player)
	{
		DisguiseCreeper creeper = GetDisguise(player);

		if (creeper == null)
		{
			return;
		}

		creeper.a(-1);

		Manager.GetDisguise().updateDisguise(creeper);
	}

	public void IncreaseSize(Player player)
	{
		DisguiseCreeper creeper = GetDisguise(player);

		if (creeper == null)
		{
			return;
		}

		creeper.a(1);

		Manager.GetDisguise().updateDisguise(creeper);
	}

	@EventHandler
	public void Knockback(CustomDamageEvent event)
	{
		if (event.GetReason() == null || !event.GetReason().contains(GetName()))
		{
			return;
		}

		event.AddMod(GetName(), event.GetDamage() * -0.25);
		event.AddKnockback(GetName(), _knockbackMagnitude);
	}

	@EventHandler
	public void Death(CombatDeathEvent event)
	{
		if (!(event.GetEvent().getEntity() instanceof Player))
		{
			return;
		}

		Player player = (Player) event.GetEvent().getEntity();

		if (!hasPerk(player))
		{
			return;
		}

		_active.remove(player.getUniqueId());

		DecreaseSize(player);
	}

	@EventHandler
	public void damageReduction(CustomDamageEvent event)
	{
		Player player = event.GetDamageePlayer();

		if (player == null)
		{
			return;
		}

		if (_active.containsKey(player.getUniqueId()))
		{
			event.AddMod("Damage Reduction", -event.GetDamage() * _damageReduction);
		}
	}

	@EventHandler
	public void addDestroyedSpawns(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
		{
			return;
		}

		GameTeam team = Manager.GetGame().GetTeamList().get(0);
		Iterator<Location> iterator = _removedSpawns.keySet().iterator();

		while (iterator.hasNext())
		{
			Location spawn = iterator.next();

			if (UtilTime.elapsed(_removedSpawns.get(spawn), _blockRegeneration))
			{
				team.GetSpawns().add(spawn);
				iterator.remove();
			}
		}
	}
}
