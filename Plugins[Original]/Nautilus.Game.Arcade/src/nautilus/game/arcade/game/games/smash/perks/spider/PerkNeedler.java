package nautilus.game.arcade.game.games.smash.perks.spider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkNeedler extends SmashPerk
{
	
	private long _cooldownNormal;
	private long _cooldownSmash;
	private double _damage;
	private int _maxTicks;
	
	private Map<UUID, Integer> _active = new HashMap<>();
	private Set<Arrow> _arrows = new HashSet<>();

	public PerkNeedler()
	{
		super("Needler", new String[] { C.cYellow + "Hold Block" + C.cGray + " to use " + C.cGreen + "Needler" });
	}

	@Override
	public void setupValues()
	{
		_cooldownNormal = getPerkInt("Cooldown Normal (ms)");
		_cooldownSmash = getPerkInt("Cooldown Smash (ms)");
		_damage = getPerkDouble("Damage");
		_maxTicks = getPerkInt("Max Ticks");
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

		if (!hasPerk(player))
		{
			return;
		}

		if (!Recharge.Instance.use(player, GetName(), isSuperActive(player) ? _cooldownSmash : _cooldownNormal, !isSuperActive(player), !isSuperActive(player)))
		{
			return;
		}
		
		_active.put(player.getUniqueId(), 7);

		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(GetName()) + "."));
	}

	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player cur : UtilServer.getPlayers())
		{
			UUID key = cur.getUniqueId();
			
			if (!_active.containsKey(key))
			{
				continue;
			}
			
			if (!cur.isBlocking())
			{
				_active.remove(key);
				continue;
			}

			int count = _active.get(key) - 1;

			if (count <= 0)
			{
				_active.remove(key);
				continue;
			}
			else
			{
				_active.put(key, count);
			}

			Arrow arrow = cur.getWorld().spawnArrow(cur.getEyeLocation().add(cur.getLocation().getDirection()), cur.getLocation().getDirection(), 1.2f, 6);
			arrow.setShooter(cur);
			_arrows.add(arrow);

			// Sound
			cur.getWorld().playSound(cur.getLocation(), Sound.SPIDER_IDLE, 0.8f, 2f);
		}
	}

	@EventHandler
	public void ArrowDamamge(CustomDamageEvent event)
	{
		if (event.GetProjectile() == null)
		{
			return;
		}
		
		if (event.GetDamagerPlayer(true) == null)
		{
			return;
		}
			
		if (!(event.GetProjectile() instanceof Arrow))
		{
			return;
		}
		
		Player damager = event.GetDamagerPlayer(true);

		if (!hasPerk(damager))
		{
			return;
		}
		
		event.SetCancelled("Needler Cancel");

		event.GetProjectile().remove();

		// Damage Event
		Manager.GetDamage().NewDamageEvent(event.GetDamageeEntity(), damager, null, DamageCause.THORNS, _damage, true, true, false, damager.getName(), GetName());

		if (!isTeamDamage(damager, event.GetDamageePlayer()))
		{
			Manager.GetCondition().Factory().Poison(GetName(), event.GetDamageeEntity(), damager, 2, 0, false, false, false);
		}
	}

	@EventHandler
	public void Clean(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}
		
		for (Iterator<Arrow> arrowIterator = _arrows.iterator(); arrowIterator.hasNext();)
		{
			Arrow arrow = arrowIterator.next();

			if (arrow.isOnGround() || !arrow.isValid() || arrow.getTicksLived() > _maxTicks)
			{
				arrowIterator.remove();
				arrow.remove();
			}
		}
	}
}
