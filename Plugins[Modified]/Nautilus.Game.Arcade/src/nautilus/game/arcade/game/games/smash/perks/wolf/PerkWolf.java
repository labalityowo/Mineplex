package nautilus.game.arcade.game.games.smash.perks.wolf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkWolf extends SmashPerk
{

	private int _cooldownNormal;
	private int _cooldownSmash;
	private int _wolfHealth;
	private float _hitBox;
	private int _maxTicks;
	private int _tackleDamage;
	private int _strikeDamage;

	private static final String CUB_TACKLE = "Cub Tackle";
	private static final String WOLF_STRIKE = "Wolf Strike";

	private Map<Wolf, UUID> _owner = new HashMap<>();
	private Map<Wolf, LivingEntity> _tackle = new HashMap<>();

	private Map<UUID, Long> _strike = new HashMap<>();

	private Map<UUID, ArrayList<Long>> _repeat = new HashMap<>();

	private Map<LivingEntity, Long> _tacklestrike = new HashMap<>();

	public PerkWolf()
	{
		super("Wolf", new String[] {C.cGray + "Attacks give +1 Damage for 3 seconds. Stacks.", C.cYellow + "Right-Click" + C.cGray + " with Axe to use " + C.cGreen + CUB_TACKLE, C.cYellow
				+ "Right-Click" + C.cGray + " with Spade to use " + C.cGreen + WOLF_STRIKE, C.cGray + "Wolf Strike deals 300% Knockback to tackled opponents.",});
	}

	@Override
	public void setupValues()
	{
		_cooldownNormal = getPerkTime("Cooldown Normal");
		_cooldownSmash = getPerkInt("Cooldown Smash (ms)");
		_wolfHealth = getPerkInt("Wolf Health");
		_hitBox = getPerkFloat("Hit Box");
		_maxTicks = getPerkInt("Max Ticks");
		_tackleDamage = getPerkInt("Tackle Damage");
		_strikeDamage = getPerkInt("Strike Damage");
	}

	@EventHandler
	public void tackleTrigger(PlayerInteractEvent event)
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

		if (!UtilItem.isAxe(player.getItemInHand()))
		{
			return;
		}

		if (!hasPerk(player))
		{
			return;
		}

		boolean smash = isSuperActive(player);

		if (!Recharge.Instance.use(player, CUB_TACKLE, smash ? _cooldownSmash : _cooldownNormal, !smash, !smash))
		{
			return;
		}

		// Get Nearest Wolf
		Manager.GetGame().CreatureAllowOverride = true;
		Wolf wolf = player.getWorld().spawn(player.getEyeLocation().add(player.getLocation().getDirection()), Wolf.class);
		Manager.GetGame().CreatureAllowOverride = false;

		wolf.setBaby();

		wolf.setAngry(true);

		UtilEnt.vegetate(wolf);

		wolf.setMaxHealth(_wolfHealth);
		wolf.setHealth(wolf.getMaxHealth());

		UtilAction.velocity(wolf, player.getLocation().getDirection(), 1.8, false, 0, 0.2, 1.2, true);

		player.getWorld().playSound(wolf.getLocation(), Sound.WOLF_BARK, 1f, 1.8f);

		// Record
		_owner.put(wolf, player.getUniqueId());

		// Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(CUB_TACKLE) + "."));
	}

	@EventHandler
	public void tackleCollide(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		// Collide
		Iterator<Wolf> wolfIterator = _owner.keySet().iterator();

		while (wolfIterator.hasNext())
		{
			Wolf wolf = wolfIterator.next();

			// Hit Player
			for (Player other : Manager.GetGame().GetPlayers(true))
			{
				if (Manager.isSpectator(other))
				{
					continue;
				}

				if (UtilEnt.hitBox(wolf.getLocation(), other, _hitBox, null))
				{
					if (other.equals(tackleGetOwner(wolf)))
					{
						continue;

					}

					tackleCollideAction(tackleGetOwner(wolf), other, wolf);
					wolfIterator.remove();
					return;
				}
			}

			if (!wolf.isValid() || (UtilEnt.isGrounded(wolf) && wolf.getTicksLived() > _maxTicks))
			{
				wolf.remove();
				wolfIterator.remove();
			}
		}
	}

	public void tackleCollideAction(Player damager, LivingEntity damagee, Wolf wolf)
	{
		if (damager == null)
		{
			return;
		}

		if (damagee instanceof Player)
		{
			if (isTeamDamage(damager, (Player) damagee))
			{
				return;
			}
		}

		_tackle.put(wolf, damagee);

		wolf.setVelocity(new Vector(0, -0.6, 0));
		UtilAction.zeroVelocity(damagee);

		// Damage
		Manager.GetDamage().NewDamageEvent(damagee, damager, null, DamageCause.CUSTOM, _tackleDamage, false, true, false, damager.getName(), CUB_TACKLE);

		// Sound
		damagee.getWorld().playSound(damagee.getLocation(), Sound.WOLF_GROWL, 1.5f, 1.5f);

		// Inform
		UtilPlayer.message(damager, F.main("Game", "You hit " + F.name(UtilEnt.getName(damagee)) + " with " + F.skill(CUB_TACKLE) + "."));
		UtilPlayer.message(damagee, F.main("Game", F.name(damager.getName()) + " hit you with " + F.skill(CUB_TACKLE) + "."));
	}

	@EventHandler
	public void tackleUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		Iterator<Wolf> wolfIterator = _tackle.keySet().iterator();

		while (wolfIterator.hasNext())
		{
			Wolf wolf = wolfIterator.next();
			LivingEntity ent = _tackle.get(wolf);

			if (!wolf.isValid() || !ent.isValid() || wolf.getTicksLived() > _maxTicks)
			{
				wolf.remove();
				wolfIterator.remove();
				continue;
			}

			if (UtilMath.offset(wolf, ent) < _hitBox)
			{
				Manager.GetCondition().Factory().Slow(CUB_TACKLE, ent, wolf, 0.9, 1, false, false, false, false);
				UtilAction.velocity(ent, new Vector(0, -0.3, 0));
			}

			// Move
			Location loc = ent.getLocation();
			loc.add(UtilAlg.getTrajectory2d(ent, wolf).multiply(1));

			UtilEnt.CreatureMove(wolf, loc, 1);
		}
	}

	private Player tackleGetOwner(Wolf wolf)
	{
		if (_owner.containsKey(wolf))
		{
			return UtilPlayer.searchExact(_owner.get(wolf));
		}

		return null;
	}

	@EventHandler
	public void tackleTargetCancel(EntityTargetEvent event)
	{
		if (_owner.containsKey(event.getEntity()))
		{
			if (_owner.get(event.getEntity()).equals(event.getTarget()))
			{
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void tackleDamage(CustomDamageEvent event)
	{
		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
		{
			return;
		}

		LivingEntity damager = event.GetDamagerEntity(false);

		if (damager == null)
		{
			return;
		}

		if (damager instanceof Wolf)
		{
			event.SetCancelled("Wolf Cub");
		}
	}

	@EventHandler
	public void strikeTrigger(PlayerInteractEvent event)
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

		boolean smash = isSuperActive(player);

		if (!Recharge.Instance.use(player, WOLF_STRIKE, smash ? 1600 : 8000, !smash, !smash))
		{
			return;
		}

		// Velocity
		UtilAction.velocity(player, player.getLocation().getDirection(), 1.6, false, 1, 0.2, 1.2, true);

		// Record
		_strike.put(player.getUniqueId(), System.currentTimeMillis());

		player.getWorld().playSound(player.getLocation(), Sound.WOLF_BARK, 1f, 1.2f);

		// Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(WOLF_STRIKE) + "."));
	}

	@EventHandler
	public void strikeEnd(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		// Collide
		Iterator<UUID> playerIterator = _strike.keySet().iterator();

		while (playerIterator.hasNext())
		{
			UUID uuid = playerIterator.next();
			Player player = UtilPlayer.searchExact(uuid);

			if (player == null)
			{
				playerIterator.remove();
				continue;
			}

			for (Player other : Manager.GetGame().GetPlayers(true))
			{
				if (player.equals(other) || UtilPlayer.isSpectator(other) || isTeamDamage(player, other))
				{
					continue;
				}

				if (UtilEnt.hitBox(player.getLocation().add(0, 1, 0), other, _hitBox, null))
				{
					strikeHit(player, other);
					playerIterator.remove();
					return;
				}
			}

			if (!UtilEnt.isGrounded(player))
			{
				continue;
			}

			if (!UtilTime.elapsed(_strike.get(uuid), 1500))
			{
				continue;
			}

			playerIterator.remove();
		}
	}

	public void strikeHit(Player damager, LivingEntity damagee)
	{
		UtilAction.zeroVelocity(damager);

		// Remove tackle
		Iterator<Wolf> wolfIterator = _tackle.keySet().iterator();

		while (wolfIterator.hasNext())
		{
			Wolf wolf = wolfIterator.next();

			if (_tackle.get(wolf).equals(damagee))
			{
				wolf.remove();
				wolfIterator.remove();

				_tacklestrike.put(damagee, System.currentTimeMillis());
			}
		}

		Manager.GetDamage().NewDamageEvent(damagee, damager, null, DamageCause.CUSTOM, _strikeDamage, true, true, false, damager.getName(), WOLF_STRIKE);

		// Sound
		damagee.getWorld().playSound(damagee.getLocation(), Sound.WOLF_BARK, 1.5f, 1f);

		// Inform
		UtilPlayer.message(damager, F.main("Game", "You hit " + F.name(UtilEnt.getName(damagee)) + " with " + F.skill(WOLF_STRIKE) + "."));
		UtilPlayer.message(damagee, F.main("Game", F.name(damager.getName()) + " hit you with " + F.skill(WOLF_STRIKE) + "."));
	}

	@EventHandler
	public void strikeKnockback(CustomDamageEvent event)
	{
		if (event.GetReason() != null && event.GetReason().contains(WOLF_STRIKE))
		{
			if (_tacklestrike.containsKey(event.GetDamageeEntity()) && !UtilTime.elapsed(_tacklestrike.get(event.GetDamageeEntity()), 100))
			{
				event.AddKnockback(GetName(), 3.0);

				// Blood
				event.GetDamageeEntity().getWorld().playEffect(event.GetDamageeEntity().getLocation(), Effect.STEP_SOUND, 55);

				// Double Sound
				event.GetDamageeEntity().getWorld().playSound(event.GetDamageeEntity().getLocation(), Sound.WOLF_BARK, 2f, 1.5f);
			}
			else
			{
				event.AddKnockback(GetName(), 1.5);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void RepeatDamage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
		{
			return;
		}

		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
		{
			return;
		}

		Player damager = event.GetDamagerPlayer(false);

		if (damager == null)
		{
			return;
		}

		if (!hasPerk(damager))
		{
			return;
		}

		if (!_repeat.containsKey(damager.getUniqueId()))
		{
			_repeat.put(damager.getUniqueId(), new ArrayList<>(Collections.singletonList(System.currentTimeMillis())));

			// Exp
			damager.setExp(Math.min(0.99F, _repeat.get(damager.getUniqueId()).size() / 9f));

			return;
		}

		int count = _repeat.get(damager.getUniqueId()).size();

		if (count > 0)
		{
			event.AddMod(damager.getName(), "Ravage", Math.min(3, count), false);

			// Sound
			damager.getWorld().playSound(damager.getLocation(), Sound.WOLF_BARK, (float) (0.5 + count * 0.25), (float) (1 + count * 0.25));
		}

		_repeat.get(damager.getUniqueId()).add(System.currentTimeMillis());

		// Exp
		damager.setExp(Math.min(0.999f, _repeat.get(damager.getUniqueId()).size() / 9f));
	}

	@EventHandler
	public void RepeatExpire(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		Iterator<UUID> playerIterator = _repeat.keySet().iterator();

		while (playerIterator.hasNext())
		{
			UUID uuid = playerIterator.next();
			Player player = UtilPlayer.searchExact(uuid);

			if (player == null)
			{
				playerIterator.remove();
				continue;
			}

			_repeat.get(uuid).removeIf(time -> UtilTime.elapsed(time, 3000));

			// Exp
			player.setExp(Math.min(0.999f, _repeat.get(uuid).size() / 9f));

			if (_repeat.get(uuid).isEmpty())
			{
				playerIterator.remove();
			}
		}
	}
}
