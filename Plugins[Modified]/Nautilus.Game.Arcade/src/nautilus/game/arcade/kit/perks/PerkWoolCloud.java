package nautilus.game.arcade.kit.perks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.bukkit.DyeColor;
import org.bukkit.Effect;
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
import mineplex.core.common.util.UtilTime;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguiseSheep;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.kit.Perk;

public class PerkWoolCloud extends Perk
{
	
	private long _cooldown;
	private long _minVelocityTime;
	private long _maxVelocityTime;
	private int _damageRadius;
	private int _damage;
	private float _knockbackMangitude;
	
	private Map<UUID, Long> _active = new HashMap<>();

	public PerkWoolCloud()
	{
		super("Wooly Rocket", new String[] { C.cYellow + "Right-Click" + C.cGray + " with Spade to " + C.cGreen + "Wooly Rocket" });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_minVelocityTime = getPerkInt("Min Velocity Time (ms)");
		_maxVelocityTime = getPerkInt("Max Velocity Time (ms)");
		_damageRadius = getPerkInt("Damage Radius");
		_damage = getPerkInt("Damage");
		_knockbackMangitude = getPerkFloat("Knockback Magnitude");
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

		if (!UtilItem.isSpade(player.getItemInHand()))
		{
			return;
		}

		if (!hasPerk(player))
		{
			return;
		}

		// Recharge
		if (!Recharge.Instance.use(player, GetName(), _cooldown, true, true))
		{
			return;
		}
		
		UtilAction.velocity(player, new Vector(0, 1, 0), 1, false, 0, 0, 2, true);

		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(GetName()) + "."));

		player.getWorld().playEffect(player.getLocation(), Effect.BLAZE_SHOOT, 0);

		// Allow double jump
		player.setAllowFlight(true);

		setWoolColor(player, DyeColor.RED);

		_active.put(player.getUniqueId(), System.currentTimeMillis());
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		if (!Manager.GetGame().IsLive())
		{
			return;
		}
		
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
			
			UtilParticle.PlayParticle(ParticleType.FLAME, player.getLocation(), 0.2f, 0.2f, 0.2f, 0, 4, ViewDist.LONGER, UtilServer.getPlayers());

			if (!UtilTime.elapsed(_active.get(key), _minVelocityTime))
			{
				continue;
			}
			
			for (Player other : Manager.GetGame().GetPlayers(true))
			{
				if (player.equals(other))
				{
					continue;
				}
				
				if (UtilMath.offset(player, other) < _damageRadius)
				{
					// Damage Event
					Manager.GetDamage().NewDamageEvent(other, player, null, DamageCause.CUSTOM, _damage, true, false, false, player.getName(), GetName());

					UtilParticle.PlayParticle(ParticleType.EXPLODE, other.getLocation(), 0f, 0f, 0f, 0, 1, ViewDist.MAX, UtilServer.getPlayers());
					UtilParticle.PlayParticle(ParticleType.LAVA, player.getLocation(), 0.2f, 0.2f, 0.2f, 0, 10, ViewDist.MAX, UtilServer.getPlayers());
				}
			}

			if (UtilEnt.isGrounded(player) || UtilTime.elapsed(_active.get(key), _maxVelocityTime))
			{
				playerIterator.remove();
				setWoolColor(player, DyeColor.WHITE);
			}
		}
	}

	public void setWoolColor(Player player, DyeColor color)
	{
		DisguiseBase disguise = Manager.GetDisguise().getActiveDisguise(player);
		
		if (disguise != null && disguise instanceof DisguiseSheep)
		{
			DisguiseSheep sheep = (DisguiseSheep) disguise;
			sheep.setSheared(false);
			sheep.setColor(color);

			sheep.UpdateDataWatcher();
			Manager.GetDisguise().updateDisguise(disguise);
		}
	}

	@EventHandler
	public void knockback(CustomDamageEvent event)
	{
		if (event.GetReason() == null || !event.GetReason().contains(GetName()))
		{
			return;
		}
		
		event.AddKnockback(GetName(), _knockbackMangitude);
	}
}
