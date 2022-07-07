package nautilus.game.arcade.game.games.smash.perks.slime;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguiseSlime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.smash.perks.SmashUltimate;

public class SmashSlime extends SmashUltimate
{

	private int _hitBox;
	private int _damage;

	public SmashSlime()
	{
		super("Giga Slime", new String[] {}, Sound.SLIME_ATTACK, 0);
	}

	@Override
	public void setupValues()
	{
		super.setupValues();

		_hitBox = getPerkInt("Hit Box");
		_damage = getPerkInt("Damage");
	}

	@Override
	public void activate(Player player)
	{
		super.activate(player);

		player.getInventory().remove(Material.IRON_SWORD);
		
		DisguiseBase disguise = Manager.GetDisguise().getActiveDisguise(player);
		if (disguise != null && disguise instanceof DisguiseSlime)
		{
			DisguiseSlime slime = (DisguiseSlime) disguise;

			slime.SetSize(14);
			Manager.GetDisguise().updateDisguise(slime);
		}

		player.setExp(0.99f);

		Manager.GetCondition().Factory().Speed(GetName(), player, player, getLength() / 1000, 2, false, false, false);

	}

	@Override
	public void cancel(Player player)
	{
		super.cancel(player);
		
		Manager.GetCondition().EndCondition(player, ConditionType.SPEED, GetName());
	}

	@EventHandler(priority = EventPriority.LOW)
	public void immunityDamagee(CustomDamageEvent event)
	{
		if (event.GetDamageePlayer() == null || event.GetDamagerEntity(true) == null)
		{
			return;
		}
		
		if (isUsingUltimate(event.GetDamageePlayer()))
		{
			event.SetCancelled(GetName());
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void immunityDamager(CustomDamageEvent event)
	{
		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
		{
			return;
		}
		
		if (event.GetDamagerPlayer(true) == null)
		{
			return;
		}
		
		if (isUsingUltimate(event.GetDamagerPlayer(true)))
		{
			event.SetCancelled(GetName());
		}
	}

	@EventHandler
	public void collide(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		List<Player> alivePlayers = Manager.GetGame().GetPlayers(true);
		
		for (Player player : alivePlayers)
		{
			
			if (!isUsingUltimate(player))
			{
				continue;
			}
			
			for (Player other : alivePlayers)
			{
				if (player.equals(other))
				{
					continue;
				}
				
				if (UtilPlayer.isSpectator(other))
				{
					continue;
				}
				
				if (UtilMath.offset(player.getLocation().add(0, 3, 0), other.getLocation()) < _hitBox)
				{
					Manager.GetDamage().NewDamageEvent(other, player, null, DamageCause.CUSTOM, _damage, true, false, false, player.getName(), GetName());

					UtilParticle.PlayParticle(ParticleType.SLIME, other.getLocation().add(0, 0.6, 0), 1f, 1f, 1f, 0, 20, ViewDist.LONG, UtilServer.getPlayers());

					player.getWorld().playSound(other.getLocation(), Sound.SLIME_ATTACK, 3f, 1f);
				}
			}
		}
	}

}
