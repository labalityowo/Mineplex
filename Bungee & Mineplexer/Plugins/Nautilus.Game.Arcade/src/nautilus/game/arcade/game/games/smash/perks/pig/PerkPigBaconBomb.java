package nautilus.game.arcade.game.games.smash.perks.pig;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguisePigZombie;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkPigBaconBomb extends SmashPerk
{
	
	private float _energyPig;
	private float _energyPigDisguiseFactor;
	private int _cooldown;
	private int _maxTicks;
	private float _pigSpeed;
	private int _explodeRadius;
	private int _explodeDamage;
	private int _explodeDamageRadius;
	
	private Map<UUID, Set<Pig>> _pigs = new HashMap<>();

	public PerkPigBaconBomb()
	{
		super("Baby Bacon Bomb", new String[] { C.cYellow + "Right-Click" + C.cGray + " with Spade to " + C.cGreen + "Baby Bacon Bomb" });
	}

	@Override
	public void setupValues()
	{
		_energyPig = getPerkFloat("Energy Per Pig");
		_energyPigDisguiseFactor = getPerkFloat("Energy Per Pig Disguise Factor");
		_cooldown = getPerkInt("Cooldown (ms)");
		_maxTicks =  getPerkInt("Pig Max Ticks");
		_pigSpeed = getPerkFloat("Pig Speed");
		_explodeRadius = getPerkInt("Pig Explode Radius");
		_explodeDamage = getPerkInt("Pig Explode Damage");
		_explodeDamageRadius = getPerkInt("Pig Explode Damage Radius");
	}

	@EventHandler
	public void Skill(PlayerInteractEvent event)
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

		float energy = _energyPig;

		DisguiseBase disguise = Manager.GetDisguise().getActiveDisguise(player);
		
		if (disguise != null && disguise instanceof DisguisePigZombie)
		{
			energy = energy * _energyPigDisguiseFactor;
		}
		
		// Energy
		if (player.getExp() < energy)
		{
			UtilPlayer.message(player, F.main("Energy", "Not enough Energy to use " + F.skill(GetName()) + "."));
			return;
		}

		// Recharge
		if (!Recharge.Instance.use(player, GetName(), _cooldown, false, false))
		{
			return;
		}
		
		// Use Energy
		player.setExp(Math.max(0f, player.getExp() - energy));

		// Velocity
		UtilAction.velocity(player, player.getLocation().getDirection(), 0.8, true, 1.2, 0, 1, true);

		// Sound
		player.getWorld().playSound(player.getLocation(), Sound.PIG_IDLE, 2f, 0.75f);

		// Pig
		Manager.GetGame().CreatureAllowOverride = true;
		Pig pig = player.getWorld().spawn(player.getLocation(), Pig.class);
		pig.setHealth(5);
		pig.setVelocity(new Vector(0, -0.4, 0));
		Manager.GetGame().CreatureAllowOverride = false;

		pig.setBaby();
		UtilEnt.vegetate(pig);
		UtilEnt.ghost(pig, true, false);

		UUID key = player.getUniqueId();
		
		// Store
		if (!_pigs.containsKey(key))
		{
			_pigs.put(key, new HashSet<>());
		}
		
		_pigs.get(key).add(pig);

		// Inform
		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(GetName()) + "."));
	}

	@EventHandler
	public void Check(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (UUID key : _pigs.keySet())
		{
			Player player = UtilPlayer.searchExact(key);
			
			if (player == null)
			{
				continue;
			}
			
			Iterator<Pig> pigIterator = _pigs.get(key).iterator();

			while (pigIterator.hasNext())
			{
				Pig pig = pigIterator.next();

				if (!pig.isValid() || pig.getTicksLived() > _maxTicks)
				{
					PigExplode(pigIterator, pig, player);
					continue;
				}

				List<Player> targets = UtilPlayer.getNearby(pig.getLocation(), 20);
				
				for (Player target : targets)
				{
					if (player.equals(target) || isTeamDamage(player, target))
					{
						continue;
					}

					UtilEnt.CreatureMoveFast(pig, target.getLocation(), _pigSpeed);

					if (UtilMath.offset(target, pig) < _explodeRadius)
					{
						PigExplode(pigIterator, pig, player);
					}
				}
			}
		}
	}

	public void PigExplode(Iterator<Pig> pigIterator, Pig pig, Player owner)
	{
		// Effect
		UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, pig.getLocation().add(0, 0.5, 0), 0, 0, 0, 0, 1, ViewDist.MAX, UtilServer.getPlayers());

		// Sound
		pig.getWorld().playSound(pig.getLocation(), Sound.EXPLODE, 0.6f, 2f);
		pig.getWorld().playSound(pig.getLocation(), Sound.PIG_DEATH, 1f, 2f);

		// Damage
		Map<LivingEntity, Double> targets = UtilEnt.getInRadius(pig.getLocation(), _explodeDamageRadius);
		
		for (LivingEntity cur : targets.keySet())
		{
			if (cur.equals(owner))
			{
				continue;
			}
			
			// Damage Event
			Manager.GetDamage().NewDamageEvent(cur, owner, null, DamageCause.CUSTOM, _explodeDamage, false, true, false, owner.getName(), GetName());
		}

		// Remove
		pigIterator.remove();
		pig.remove();
	}
}
