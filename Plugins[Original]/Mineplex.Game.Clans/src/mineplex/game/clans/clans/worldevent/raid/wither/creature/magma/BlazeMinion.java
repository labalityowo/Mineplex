package mineplex.game.clans.clans.worldevent.raid.wither.creature.magma;

import org.bukkit.Location;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.worldevent.raid.RaidChallenge;
import mineplex.game.clans.clans.worldevent.raid.RaidCreature;
import mineplex.game.clans.clans.worldevent.raid.wither.WitherRaid;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class BlazeMinion extends RaidCreature<Blaze>
{
	private RaidChallenge<WitherRaid> _challenge;
	
	public BlazeMinion(RaidChallenge<WitherRaid> challenge, Location spawnLocation)
	{
		super(challenge.getRaid(), spawnLocation, "Blaze Minion", true, 20, 500, true, Blaze.class);
		
		_challenge = challenge;
		spawnEntity();
	}

	@Override
	protected void spawnCustom() {}

	@Override
	public void dieCustom() {}
	
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