package nautilus.game.arcade.game.games.smash.perks.spider;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.game.games.smash.perks.SmashUltimate;

public class SmashSpider extends SmashUltimate
{
	private Map<LivingEntity, Double> _preHealth = new HashMap<>();

	public SmashSpider()
	{
		super("Spider Nest", new String[] {}, Sound.SPIDER_DEATH, 0);
	}

	@Override
	public void activate(Player player)
	{
		super.activate(player);

		// Nest
		Map<Block, Double> blocks = UtilBlock.getInRadius(player.getLocation().getBlock(), 16);

		for (Block block : blocks.keySet())
		{
			if (blocks.get(block) > 0.07)
			{
				continue;
			}

			if (!UtilBlock.airFoliage(block))
			{
				continue;
			}

			if (block.getY() > player.getLocation().getY() + 10)
			{
				continue;
			}

			if (block.getY() < player.getLocation().getY() - 10)
			{
				continue;
			}

			Manager.GetBlockRestore().add(block, 30, (byte) 0, (int) (getLength() + 5000 * Math.random()));
		}

		// Regen
		Manager.GetCondition().Factory().Regen(GetName(), player, player, getLength() / 1000, 2, false, false, false);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void damagePre(CustomDamageEvent event)
	{
		if (event.IsCancelled())
		{
			return;
		}

		if (event.GetCause() != DamageCause.ENTITY_ATTACK && event.GetCause() != DamageCause.PROJECTILE && event.GetCause() != DamageCause.CUSTOM)
		{
			return;
		}
		
		Player damager = event.GetDamagerPlayer(true);

		if (damager == null)
		{
			return;
		}

		LivingEntity damagee = event.GetDamageeEntity();

		if (damagee == null)
		{
			return;
		}

		if (!isUsingUltimate(damager))
		{
			return;
		}

		_preHealth.put(damagee, damagee.getHealth());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void damagePost(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		Player damager = event.GetDamagerPlayer(true);

		if (damager == null)
		{
			return;
		}

		LivingEntity damagee = event.GetDamageeEntity();

		if (damagee == null)
		{
			return;
		}

		if (!isUsingUltimate(damager))
		{
			return;

		}
		if (!_preHealth.containsKey(damagee))
		{
			return;
		}
		
		double diff = (_preHealth.remove(damagee) - damagee.getHealth()) / 2;

		if (diff <= 0)
		{
			return;
		}
		
		damager.setMaxHealth(Math.min(30, damager.getMaxHealth() + diff));

		UtilParticle.PlayParticle(ParticleType.HEART, damager.getLocation().add(0, 1, 0), 0, 0, 0, 0, 1, ViewDist.LONG, UtilServer.getPlayers());

		UtilParticle.PlayParticle(ParticleType.RED_DUST, damagee.getLocation().add(0, 1, 0), 0.4f, 0.4f, 0.4f, 0, 12, ViewDist.LONG, UtilServer.getPlayers());

		if (event.GetCause() == DamageCause.ENTITY_ATTACK)
		{
			damager.getWorld().playSound(damager.getLocation(), Sound.SPIDER_IDLE, 1.5f, 1f);
		}
	}
	
}
