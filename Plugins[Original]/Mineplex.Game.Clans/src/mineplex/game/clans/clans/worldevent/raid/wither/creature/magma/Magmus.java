package mineplex.game.clans.clans.worldevent.raid.wither.creature.magma;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.worldevent.api.BossPassive;
import mineplex.game.clans.clans.worldevent.raid.RaidCreature;
import mineplex.game.clans.clans.worldevent.raid.wither.challenge.six.ChallengeSix;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class Magmus extends RaidCreature<MagmaCube>
{
	private ChallengeSix _challenge;
	protected List<BossPassive<Magmus, MagmaCube>> Abilities = new ArrayList<>();
	
	protected boolean TeleportBackASAP = true;
	protected boolean HeatingRoom = false;

	public Magmus(ChallengeSix challenge, Location location)
	{
		super(challenge.getRaid(), location, "Gatekeeper Magmus", true, 2000, 500, true, MagmaCube.class);
		
		_challenge = challenge;
		spawnEntity();
		Abilities.add(new MagmusCataclysm(this));
		Abilities.add(new MagmusSmash(this));
		Abilities.add(new MagmusMeteor(this));
		Abilities.add(new MagmusEat(this));
	}
	
	protected ChallengeSix getChallenge()
	{
		return _challenge;
	}

	@Override
	protected void spawnCustom()
	{
		UtilEnt.vegetate(getEntity());
		getEntity().setSize(17);
	}

	@Override
	public void dieCustom()
	{
		endAbility();
	}

	private void endAbility()
	{
		Abilities.forEach(ability ->
		{
			HandlerList.unregisterAll(ability);
		});
		Abilities.clear();
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
		
		if (TeleportBackASAP)
		{
			getEntity().teleport(getSpawnLocation());
		}
		
		Abilities.forEach(BossPassive::tick);
	}
	
	@EventHandler
	public void onMagmusDamage(CustomDamageEvent event)
	{
		if (event.GetDamagerEntity(false) == null)
		{
			return;
		}
		if (event.GetDamagerEntity(false).getEntityId() == getEntity().getEntityId() && (event.GetCause() != DamageCause.FIRE && event.GetCause() != DamageCause.CUSTOM && event.GetCause() != DamageCause.PROJECTILE))
		{
			event.SetCancelled("Wrong Attack Type Magmus");
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