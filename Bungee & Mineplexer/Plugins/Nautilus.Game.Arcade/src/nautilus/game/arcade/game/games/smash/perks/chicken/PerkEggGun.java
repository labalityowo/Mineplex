package nautilus.game.arcade.game.games.smash.perks.chicken;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.smash.perks.SmashPerk;
import org.bukkit.Sound;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PerkEggGun extends SmashPerk
{

	private int _cooldown;
	private int _duration;
	private int _damage;

	private Map<UUID, Long> _active = new HashMap<>();

	public PerkEggGun()
	{
		super("Egg Blaster", new String[] { C.cYellow + "Hold Block" + C.cGray + " to use " + C.cGreen + "Egg Blaster" });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkInt("Cooldown (ms)");
		_duration = getPerkInt("Duration (ms)");
		_damage = getPerkInt("Damage");
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

		if (!UtilItem.isSword(player.getItemInHand()))
		{
			return;
		}
		
		if (isSuperActive(event.getPlayer()))
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

		_active.put(player.getUniqueId(), System.currentTimeMillis());

		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(GetName()) + "."));
	}

	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !Manager.GetGame().IsLive())
		{
			return;
		}

		for (Player cur : UtilServer.getPlayers())
		{
			UUID key = cur.getUniqueId();
			
			if (!isSuperActive(cur))
			{
				if (!_active.containsKey(key))
				{
					continue;
				}

				if (!cur.isBlocking())
				{
					_active.remove(key);
					continue;
				}

				if (UtilTime.elapsed(_active.get(key), _duration))
				{
					_active.remove(key);
					continue;
				}
			}

			Vector offset = cur.getLocation().getDirection();
			
			if (offset.getY() < 0)
			{
				offset.setY(0);
			}
			
			Egg egg = cur.getWorld().spawn(cur.getLocation().add(0, 0.5, 0).add(offset), Egg.class);
			egg.setVelocity(cur.getLocation().getDirection().add(new Vector(0, 0.2, 0)));
			egg.setShooter(cur);

			// Effect
			cur.getWorld().playSound(cur.getLocation(), Sound.CHICKEN_EGG_POP, 0.5f, 1f);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void EggHit(CustomDamageEvent event)
	{
		if (event.GetProjectile() == null || !(event.GetProjectile() instanceof Egg))
		{
			return;
		}

		Player damagee = event.GetDamageePlayer();
		Player damager = event.GetDamagerPlayer(true);

		if (damager == null || !hasPerk(damager))
		{
			return;
		}

		event.AddMod("Negate", -event.GetDamage());
		event.AddMod(damager.getName(), "Egg Blaster", _damage, true);
		event.SetIgnoreRate(true);
		event.SetKnockback(false);

		if (damagee == null || !isTeamDamage(damagee, damager))
		{
			UtilAction.zeroVelocity(event.GetDamageeEntity());
		}
	}
}
