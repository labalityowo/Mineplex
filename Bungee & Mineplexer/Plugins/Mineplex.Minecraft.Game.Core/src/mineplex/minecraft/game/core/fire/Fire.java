package mineplex.minecraft.game.core.fire;

import java.util.HashMap;
import java.util.HashSet;

import mineplex.core.MiniPlugin;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import mineplex.minecraft.game.core.condition.ConditionManager;
import mineplex.minecraft.game.core.damage.DamageManager;

import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

public class Fire extends MiniPlugin
{
	private ConditionManager _conditionManager;
	private DamageManager _damageManager;
	private HashMap<Item, FireData> _fire = new HashMap<Item, FireData>();
	
	public Fire(JavaPlugin plugin, ConditionManager conditionManager, DamageManager damageManager) 
	{
		super("Fire", plugin);
		
		_conditionManager = conditionManager;
		_damageManager = damageManager;
	}

	public void Add(Item item, LivingEntity owner, double expireTime, double delayTime, double burnTime, double damage, String skillName, boolean hitSelf)
	{
		_fire.put(item, new FireData(owner, expireTime, delayTime, burnTime, damage, skillName, hitSelf));
		item.setPickupDelay(0);
	}

	@EventHandler
	public void IgniteCollide(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK) 
			return;

		HashMap<Item, LivingEntity> collided = new HashMap<Item, LivingEntity>();

		for (Item fire : _fire.keySet())
		{
			if (!_fire.get(fire).IsPrimed())
				continue;

			if (fire.getLocation().getBlock().isLiquid())
			{
				collided.put(fire, null);
				continue;
			}

			for (LivingEntity ent : fire.getWorld().getEntitiesByClass(LivingEntity.class))
			{
				if (UtilPlayer.isSpectator(ent) || ent.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE))
				{
					continue;
				}

				if (ent.getLocation().getBlock().getTypeId() == 8 || ent.getLocation().getBlock().getTypeId() == 9)
					continue;

				if (!_fire.get(fire).canHitOwner() && ent.equals(_fire.get(fire).GetOwner()))
					continue; 

				if (_conditionManager.HasCondition(ent, ConditionType.FIRE_ITEM_IMMUNITY, null))
				{
					continue; 
				}

				if (!UtilEnt.hitBox(fire.getLocation(), ent, 1.5, null))
					continue;

				collided.put(fire, ent);
			}
		}

		for (Item fire : collided.keySet())
		{	
			FireData fireData = _fire.remove(fire);
			fire.remove();
			Ignite(collided.get(fire), fireData);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void IgnitePickup(PlayerPickupItemEvent event)
	{
		Player player = event.getPlayer();
		Item fire = event.getItem();

		if (!_fire.containsKey(fire))
			return;
		
		event.setCancelled(true);

		if (!_fire.get(fire).canHitOwner() && _fire.get(fire).GetOwner().equals(player))
			return;

		if (UtilPlayer.isSpectator(player))
			return;

		if (player.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE))
			return;

		if (player.getLocation().getBlock().getTypeId() == 8 || player.getLocation().getBlock().getTypeId() == 9)
			return;

		if (!_fire.get(fire).IsPrimed())
			return;

		if (_conditionManager.HasCondition(player, ConditionType.FIRE_ITEM_IMMUNITY, null))
		{
			return;
		}

		if (!UtilEnt.hitBox(fire.getLocation(), player, 1.5, null))
			return;

		//Remove
		FireData fireData = _fire.remove(fire);
		fire.remove();
		Ignite(player, fireData);
	}
	
	@EventHandler
	public void HopperPickup(InventoryPickupItemEvent event)
	{
		if (_fire.containsKey(event.getItem()))
			event.setCancelled(true);
	}

	public void Ignite(LivingEntity ent, FireData fireData)
	{
		if (ent == null)
			return;

		_conditionManager.Factory().Ignite(fireData.GetName(), ent, fireData.GetOwner(), fireData.GetBurnTime(), true, true);

		//Damage
		if (fireData.GetDamage() > 0)
		{
			//Inferno Negate
			if (fireData.GetDamage() == 1)
				if (ent instanceof Player)
					if (!Recharge.Instance.use((Player)ent, "Fire Damage", 150, false, false))
					{
						ent.playEffect(EntityEffect.HURT);
						return;
					}
						

			//Damage Event
			_damageManager.NewDamageEvent(ent, fireData.GetOwner(), null, 
					DamageCause.CUSTOM, fireData.GetDamage(), false, true, false,
					UtilEnt.getName(fireData.GetOwner()), fireData.GetName());
		}
	}



	@EventHandler
	public void Expire(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK) 
			return;

		HashSet<Item> expire = new HashSet<Item>();

		for (Item cur : _fire.keySet())
		{
			if (!cur.isValid() || _fire.get(cur).Expired())
				expire.add(cur);
		}
		for (Item cur : expire)
		{
			_fire.remove(cur);
			cur.remove();
		}	
	}	
	
	public void Remove(LivingEntity owner, String cause)
	{
		HashSet<Item> remove = new HashSet<Item>();
		
		for (Item cur : _fire.keySet())
			if (owner == null || _fire.get(cur).GetOwner().equals(owner))
				if (cause == null || _fire.get(cur).GetName().equals(cause))
					remove.add(cur);
		
		for (Item cur : remove)
		{
			_fire.remove(cur);
			cur.remove();
		}		
	}
	
	public void RemoveNear(Location loc, double range)
	{
		HashSet<Item> remove = new HashSet<Item>();
		
		for (Item cur : _fire.keySet())
			if (UtilMath.offset(loc, cur.getLocation()) < range)
				remove.add(cur);
		
		for (Item cur : remove)
		{
			_fire.remove(cur);
			
			cur.getWorld().playEffect(cur.getLocation(), Effect.EXTINGUISH, 0);
			
			cur.remove();
		}		
	}
}
