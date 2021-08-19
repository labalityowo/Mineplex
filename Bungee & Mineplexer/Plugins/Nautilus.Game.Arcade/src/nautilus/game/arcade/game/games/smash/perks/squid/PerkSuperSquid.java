package nautilus.game.arcade.game.games.smash.perks.squid;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkSuperSquid extends SmashPerk
{
	
	private int _cooldown;
	private int _velocityTime;
	
	private Map<UUID, Long> _active = new HashMap<>();

	public PerkSuperSquid()
	{
		super("Super Squid", new String[] { C.cYellow + "Hold Block" + C.cGray + " to use " + C.cGreen + "Super Squid", });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_velocityTime = getPerkInt("Velocity Time (ms)");
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
		
		if (isSuperActive(player))
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
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		for (Player cur : UtilServer.getPlayers())
		{
			if (!_active.containsKey(cur.getUniqueId()))
			{
				continue;
			}
			
			if (isSuperActive(cur))
			{
				continue;
			}
			
			if (!cur.isBlocking())
			{
				_active.remove(cur.getUniqueId());
				continue;
			}

			if (UtilTime.elapsed(_active.get(cur.getUniqueId()), _velocityTime))
			{
				_active.remove(cur.getUniqueId());
				continue;
			}

			UtilAction.velocity(cur, 0.6, 0.1, 1, true);

			cur.getWorld().playSound(cur.getLocation(), Sound.SPLASH2, 0.5f, 1f);
			UtilParticle.PlayParticle(ParticleType.SPLASH, cur.getLocation().add(0, 0.5, 0), 0.3f, 0.3f, 0.3f, 0, 60, ViewDist.LONG, UtilServer.getPlayers());
		}
	}

	@EventHandler
	public void DamageCancel(CustomDamageEvent event)
	{
		LivingEntity damagee = event.GetDamageeEntity();
		LivingEntity damager = event.GetDamagerEntity(false);

		if (_active.containsKey(damagee.getUniqueId()) || damager != null && event.GetCause() == DamageCause.ENTITY_ATTACK && _active.containsKey(damager.getUniqueId()))
		{
			event.SetCancelled(GetName());
		}
	}
}
