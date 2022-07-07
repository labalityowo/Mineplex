package nautilus.game.arcade.game.games.smash.perks.skeletalhorse;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguiseHorse;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkHorseKick extends SmashPerk
{
	
	private int _cooldown;
	private float _damage;
	private int _kickTime;
	private float _knockbackMagnitude;
	
	private Map<UUID, Long> _active = new HashMap<>();

	public PerkHorseKick()
	{
		super("Bone Kick", new String[] { C.cYellow + "Right-Click" + C.cGray + " with Axe to use " + C.cGreen + "Bone Kick" });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_damage = getPerkFloat("Damage");
		_kickTime = getPerkInt("Kick Time (ms)");
		_knockbackMagnitude = getPerkFloat("Knockback Magnitude");
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

		if (!UtilItem.isAxe(player.getItemInHand()))
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

		// Horse Animation
		DisguiseBase horse = Manager.GetDisguise().getActiveDisguise(player);

		if (horse != null && horse instanceof DisguiseHorse)
		{
			((DisguiseHorse) horse).kick();
			Manager.GetDisguise().updateDisguise(horse);
		}

		// Animation
		_active.put(player.getUniqueId(), System.currentTimeMillis());

		// AoE Area
		Location loc = player.getLocation();
		loc.add(player.getLocation().getDirection().setY(0).normalize().multiply(1.5));
		loc.add(0, 0.8, 0);

		for (LivingEntity other : UtilEnt.getInRadius(loc, 2.5).keySet())
		{
			if (UtilPlayer.isSpectator(other))
			{
				continue;
			}
			
			if (other.equals(player))
			{
				continue;
			}

			// Damage Event
			Manager.GetDamage().NewDamageEvent(other, player, null, DamageCause.CUSTOM, _damage, true, true, false, player.getName(), GetName());

			// Sound
			player.getWorld().playSound(player.getLocation(), Sound.SKELETON_HURT, 4f, 0.6f);
			player.getWorld().playSound(player.getLocation(), Sound.SKELETON_HURT, 4f, 0.6f);

			// Inform
			UtilPlayer.message(other, F.main("Skill", F.name(player.getName()) + " hit you with " + F.skill(GetName()) + "."));
		}

		// Inform
		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(GetName()) + "."));

		// Slow
		Manager.GetCondition().Factory().Slow(GetName(), player, player, 0.8, 3, false, false, true, false);
	}

	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		// Player
		Iterator<UUID> playerIterator = _active.keySet().iterator();

		while (playerIterator.hasNext())
		{
			UUID key = playerIterator.next();
			Player player = UtilPlayer.searchExact(key);
			
			if (player == null)
			{
				playerIterator.remove();
				continue;
			}

			if (!player.isValid() || player.getHealth() <= 0 || UtilTime.elapsed(_active.get(key), _kickTime))
			{
				playerIterator.remove();

				// Horse Animation
				DisguiseBase horse = Manager.GetDisguise().getActiveDisguise(player);
				
				if (horse != null && horse instanceof DisguiseHorse)
				{
					((DisguiseHorse) horse).stopKick();
					Manager.GetDisguise().updateDisguise(horse);
				}

				Manager.GetCondition().EndCondition(player, null, GetName());
			}
			else
			{
				Location loc = player.getLocation();
				loc.add(player.getLocation().getDirection().setY(0).normalize().multiply(1.5));
				loc.add(0, 0.8, 0);

				UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE, loc, 0.3f, 0.3f, 0.3f, 0, 2, ViewDist.LONG, UtilServer.getPlayers());
			}
		}
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
