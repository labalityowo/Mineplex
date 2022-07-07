package mineplex.game.clans.clans.worldevent.raid.wither.creature.corpse;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilMath;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.worldevent.raid.RaidChallenge;
import mineplex.game.clans.clans.worldevent.raid.RaidCreature;
import mineplex.game.clans.clans.worldevent.raid.wither.WitherRaid;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class ReanimatedCorpse extends RaidCreature<Zombie>
{
	private static final Material[] SWORDS = {Material.WOOD_SWORD, Material.STONE_SWORD, Material.GOLD_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD};
	private RaidChallenge<WitherRaid> _challenge;
	private CorpsePassive _passive;

	public ReanimatedCorpse(RaidChallenge<WitherRaid> challenge, Location location)
	{
		super(challenge.getRaid(), location, "Reanimated Corpse", true, 15, 100, true, Zombie.class);
		
		_challenge = challenge;
		spawnEntity();
		_passive = new CorpsePassive(this);
	}

	@Override
	protected void spawnCustom()
	{
		getEntity().getEquipment().setItemInHand(new ItemStack(UtilMath.randomElement(SWORDS)));
		getEntity().getEquipment().setItemInHandDropChance(0f);
	}

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