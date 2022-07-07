package mineplex.game.clans.clans.worldevent.raid.wither.creature.mage;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.worldevent.raid.RaidChallenge;
import mineplex.game.clans.clans.worldevent.raid.RaidCreature;
import mineplex.game.clans.clans.worldevent.raid.wither.WitherRaid;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class UndeadKnight extends RaidCreature<Skeleton>
{
	private RaidChallenge<WitherRaid> _challenge;
	private KnightPassive _passive;

	public UndeadKnight(RaidChallenge<WitherRaid> challenge, Location location)
	{
		super(challenge.getRaid(), location, "Undead Knight", true, 25, 100, true, Skeleton.class);
		
		_challenge = challenge;
		spawnEntity();
		_passive = new KnightPassive(this);
	}

	@Override
	protected void spawnCustom()
	{
		getEntity().getEquipment().setItemInHand(new ItemStack(Material.STONE_SWORD));
		getEntity().getEquipment().setItemInHandDropChance(0f);
		getEntity().getEquipment().setHelmet(new ItemBuilder(Material.CHAINMAIL_HELMET).setUnbreakable(true).build());
		getEntity().getEquipment().setHelmetDropChance(0f);
		getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000000, 0));
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