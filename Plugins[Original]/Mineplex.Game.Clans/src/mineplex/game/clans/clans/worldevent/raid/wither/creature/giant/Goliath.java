package mineplex.game.clans.clans.worldevent.raid.wither.creature.giant;

import org.bukkit.Location;
import org.bukkit.entity.Giant;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.worldevent.raid.RaidChallenge;
import mineplex.game.clans.clans.worldevent.raid.RaidCreature;
import mineplex.game.clans.clans.worldevent.raid.wither.WitherRaid;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class Goliath extends RaidCreature<Giant>
{
	private RaidChallenge<WitherRaid> _challenge;
	private GoliathPassive _passive;

	public Goliath(RaidChallenge<WitherRaid> challenge, Location location)
	{
		super(challenge.getRaid(), location, "Goliath", true, 5000, 500, true, Giant.class);
		
		_challenge = challenge;
		spawnEntity();
		_passive = new GoliathPassive(this);
	}

	@Override
	protected void spawnCustom() {}

	@Override
	public void dieCustom()
	{
		endAbility();
	}

	private void endAbility()
	{
		HandlerList.unregisterAll(_passive);
		_passive = null;
	}
	
	@EventHandler
	public void onTick(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		if (_challenge.isComplete())
		{
			remove();
			return;
		}
		
		_passive.tick();
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void allyDamage(CustomDamageEvent event)
	{
		if (event.GetDamageeEntity().getEntityId() == getEntity().getEntityId())
		{
			LivingEntity damager = event.GetDamagerEntity(event.GetCause() == DamageCause.PROJECTILE);
			if (damager != null && !(damager instanceof Player))
			{
				event.SetCancelled("Allied Damage");
			}
		}
	}

	@Override
	public void handleDeath(Location location) {}
}