package nautilus.game.arcade.game.games.smash.perks.slime;

import mineplex.core.common.util.*;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.smash.perks.SmashPerk;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PerkSlimeSlam extends SmashPerk
{

	private int _cooldown;
	private int _hitBox;
	private int _startTime;
	private int _damageRateLimit;
	private int _damage;
	private int _knockbackMagnitude;
	
	private Map<UUID, Long> _live = new HashMap<>();

	public PerkSlimeSlam()
	{
		super("Slime Slam", new String[] { C.cYellow + "Right-Click" + C.cGray + " with Axe to " + C.cGreen + "Slime Slam" });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_hitBox = getPerkInt("Hit Box");
		_startTime = getPerkTime("Start Time");
		_damageRateLimit = getPerkInt("Damage Rate Limit (ms)");
		_damage = getPerkInt("Damage");
		_knockbackMagnitude = getPerkInt("Knockback Magnitude");
	}

	@EventHandler
	public void Leap(PlayerInteractEvent event)
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

		if (!Recharge.Instance.use(player, GetName(), _cooldown, true, true))
		{
			return;
		}

		UtilAction.velocity(player, player.getLocation().getDirection(), 1.2, false, 0, 0.2, 1.2, true);

		// Record
		_live.put(player.getUniqueId(), System.currentTimeMillis());

		// Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));
	}

	@EventHandler
	public void End(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		List<Player> alivePlayers = Manager.GetGame().GetPlayers(true);
		
		// Collide
		for (Player player : alivePlayers)
		{
			if (!_live.containsKey(player.getUniqueId()))
			{
				continue;
			}

			for (Player other : alivePlayers)
			{
				if (player.equals(other) || UtilPlayer.isSpectator(other) || isTeamDamage(player, other))
				{
					continue;
				}
				
				if (UtilMath.offset(player, other) < _hitBox)
				{
					doSlam(player, other);
					_live.remove(player.getUniqueId());
					return;
				}
			}
		}

		// End
		for (Player player : alivePlayers)
		{
			UUID key = player.getUniqueId();
			
			if (!UtilEnt.isGrounded(player))
			{
				continue;
			}
			
			if (!_live.containsKey(key))
			{
				continue;
			}
			
			if (!UtilTime.elapsed(_live.get(key), _startTime))
			{
				continue;
			}
			
			_live.remove(key);
		}
	}

	public void doSlam(Player damager, LivingEntity damagee)
	{
		if (damagee instanceof Player)
		{
			if (!Recharge.Instance.use((Player) damagee, GetName() + " Hit", _damageRateLimit, false, false))
			{
				return;
			}
		}
		
		// Recoil Event
		if (!isSuperActive(damager))
		{
			Manager.GetDamage().NewDamageEvent(damager, damagee, null, DamageCause.CUSTOM, _damage / 4, true, true, false, damager.getName(), GetName() + " Recoil");
		
			// Damage Event
			Manager.GetDamage().NewDamageEvent(damagee, damager, null, DamageCause.CUSTOM, _damage, true, true, false, damager.getName(), GetName());
		}

		// Inform
		UtilPlayer.message(damager, F.main("Game", "You hit " + F.name(UtilEnt.getName(damagee)) + " with " + F.skill(GetName()) + "."));
		UtilPlayer.message(damagee, F.main("Game", F.name(damager.getName()) + " hit you with " + F.skill(GetName()) + "."));
	}

	@EventHandler
	public void Knockback(CustomDamageEvent event)
	{
		if (event.GetReason() == null || !event.GetReason().contains(GetName()))
		{
			return;
		}
		
		event.AddKnockback(GetName(), _knockbackMagnitude);
	}
}
