package mineplex.game.clans.clans.worldevent.raid.wither.creature.mage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilMath;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.worldevent.api.BossPassive;
import mineplex.game.clans.clans.worldevent.raid.RaidChallenge;
import mineplex.game.clans.clans.worldevent.raid.RaidCreature;
import mineplex.game.clans.clans.worldevent.raid.wither.WitherRaid;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class UndeadMage extends RaidCreature<Skeleton>
{
	private RaidChallenge<WitherRaid> _challenge;
	private List<BossPassive<UndeadMage, Skeleton>> _abilities;
	private boolean _invuln;
	
	public UndeadMage(RaidChallenge<WitherRaid> challenge, Location location, boolean invulnerable, List<Location> spawnLocations, List<Location> blinkLocations)
	{
		super(challenge.getRaid(), location, "Undead Mage", true, 500, 1500, true, Skeleton.class);
		
		_challenge = challenge;
		_invuln = invulnerable;
		spawnEntity();
		_abilities = new ArrayList<>();
		_abilities.add(new MageSummon(this, spawnLocations));
		_abilities.add(new MageBlink(this, blinkLocations));
		_abilities.add(new MageBolt(this));
		_abilities.add(new MageBoneExplode(this));
	}

	@Override
	protected void spawnCustom()
	{
		//UtilEnt.vegetate(getEntity());
		getEntity().setSkeletonType(SkeletonType.WITHER);
		getEntity().getEquipment().setItemInHand(new ItemStack(Material.RECORD_6)); //Meridian Scepter
		getEntity().getEquipment().setItemInHandDropChance(0f);
		getEntity().getEquipment().setHelmet(new ItemBuilder(Material.GOLD_HELMET).setUnbreakable(true).build());
		getEntity().getEquipment().setChestplate(new ItemBuilder(Material.GOLD_CHESTPLATE).setUnbreakable(true).build());
		getEntity().getEquipment().setLeggings(new ItemBuilder(Material.GOLD_LEGGINGS).setUnbreakable(true).build());
		getEntity().getEquipment().setBoots(new ItemBuilder(Material.GOLD_BOOTS).setUnbreakable(true).build());
		getEntity().getEquipment().setHelmetDropChance(0f);
		getEntity().getEquipment().setChestplateDropChance(0f);
		getEntity().getEquipment().setLeggingsDropChance(0f);
		getEntity().getEquipment().setBootsDropChance(0f);
	}
	
	public RaidChallenge<WitherRaid> getChallenge()
	{
		return _challenge;
	}

	@Override
	public void dieCustom()
	{
		endAbility();
	}

	private void endAbility()
	{
		for (BossPassive<UndeadMage, Skeleton> ability : _abilities)
		{
			HandlerList.unregisterAll(ability);
		}
		_abilities.clear();
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
		
		_abilities.forEach(BossPassive::tick);
	}

	protected List<Player> getPlayers(Map<Player, Double> map, double maxDist)
	{
		return getPlayers(map, 0, maxDist);
	}

	protected List<Player> getPlayers(final Map<Player, Double> map, double minDist, double maxDist)
	{
		List<Player> list = new ArrayList<>();

		for (Player p : map.keySet())
		{
			if (!_challenge.getRaid().getPlayers().contains(p))
			{
				continue;
			}
			if (map.get(p) >= minDist && map.get(p) <= maxDist)
			{
				list.add(p);
			}
		}

		Collections.sort(list, (o1, o2) ->
		{
			return Double.compare(map.get(o2), map.get(o1));
		});

		return list;
	}
	
	@EventHandler
	public void onSkeletonDamage(CustomDamageEvent event)
	{
		if (event.GetDamageeEntity().getEntityId() == getEntity().getEntityId())
		{
			event.SetKnockback(false);
			if (_invuln)
			{
				event.SetCancelled("Challenge Invulnerability");
			}
		}
	}

	@EventHandler
	public void noFallDamage(CustomDamageEvent event)
	{
		if (getEntity() == null)
		{
			return;
		}

		if (event.GetDamageeEntity().getEntityId() != getEntity().getEntityId())
		{
			return;
		}

		DamageCause cause = event.GetCause();

		if (cause == DamageCause.FALL)
		{
			event.SetCancelled("Boss Invulnerability");
		}
	}
	
	@EventHandler
	public void buffDamage(CustomDamageEvent event)
	{
		if (event.GetDamagerEntity(false) == null)
		{
			return;
		}
		if (event.GetDamagerEntity(false).getEntityId() == getEntity().getEntityId() && event.GetCause() == DamageCause.ENTITY_ATTACK)
		{
			event.AddMod("Mage Attack", 7 - event.GetDamage());
		}
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void ally(CustomDamageEvent event)
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
	public void handleDeath(Location location)
	{
		location.getWorld().dropItem(location.clone().add(0, 1, 0), new ItemStack(Material.EMERALD, UtilMath.rRange(10, 20)));
		location.getWorld().dropItem(location.clone().add(0, 1, 0), new ItemStack(UtilMath.randomElement(new Material[] {Material.GOLD_HELMET, Material.GOLD_CHESTPLATE, Material.GOLD_LEGGINGS, Material.GOLD_BOOTS})));
	}
}