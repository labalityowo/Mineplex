package mineplex.game.clans.items;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.game.clans.items.legendaries.LegendaryItem;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

/**
 * PlayerGear caches and manages a players set of {@link CustomItem}s that they
 * currently wield.
 */
public class PlayerGear
{
	private Player _owner;
	
	private CustomItem _weapon, _helmet, _chestplate, _leggings, _boots;
	private Map<Integer, CustomItem> _inventory = new HashMap<>();
	private int _lastHeldSlot = 0;

	public PlayerGear(Player owner)
	{
		_owner = owner;
		_lastHeldSlot = owner.getInventory().getHeldItemSlot();
	}
	
	/**
	 * Tick & update internal logic for the PlayerGear and required custom items
	 * that are equipped.
	 */
	public void update()
	{
		if (_owner.isOnline())
		{
			CustomItem item = getWeapon();
			
			if (item != null && item instanceof LegendaryItem)
			{
				LegendaryItem legendary = (LegendaryItem) item;
				legendary.preUpdate(getPlayer());
				legendary.update(getPlayer());

				if (legendary.OriginalOwner == null)
				{
					legendary.OriginalOwner = getPlayer().getUniqueId().toString();
				}
			}
		}
	}
	
	/**
	 * Refresh the cache of gear due to a potential change
	 */
	public void updateCache(boolean inventoryChanged)
	{
		if (inventoryChanged)
		{
			forEachGear(true, item -> item._lastUser = null);
			_inventory.clear();
			for (int i = 0; i < getPlayer().getInventory().getSize(); i++)
			{
				_inventory.put(i, GearManager.parseItem(getPlayer().getInventory().getItem(i)));
			}
		}
		if (_weapon != null && _weapon instanceof LegendaryItem && _lastHeldSlot != getPlayer().getInventory().getHeldItemSlot())
		{
			((LegendaryItem)_weapon).onUnequip(getPlayer());
		}
		_lastHeldSlot = getPlayer().getInventory().getHeldItemSlot();
		_weapon = _inventory.get(_lastHeldSlot);
		_helmet = GearManager.parseItem(getPlayer().getInventory().getHelmet());
		_chestplate = GearManager.parseItem(getPlayer().getInventory().getChestplate());
		_leggings = GearManager.parseItem(getPlayer().getInventory().getLeggings());
		_boots = GearManager.parseItem(getPlayer().getInventory().getBoots());
		if (inventoryChanged)
		{
			forEachGear(true, item -> item._lastUser = getPlayer());
		}
	}
	
	/**
	 * @return the {@link Player} that owns this gear set.
	 */
	public Player getPlayer()
	{
		return _owner;
	}
	
	public String getPlayerName()
	{
		return getPlayer().getName();
	}
	
	/**
	 * Trigger interact events for the set of equipped {@link CustomItem}s in
	 * gear set.
	 * 
	 * @param event - the triggering interact event
	 */
	public void onInteract(PlayerInteractEvent event)
	{
		forEachGear(false, item -> item.onInteract(event));
	}

	
	/**
	 * Trigger on-attack events for the set of equipped {@link CustomItem}s in
	 * gear set.
	 * 
	 * @param event - the triggering on-attack event
	 */
	public void onAttack(CustomDamageEvent event)
	{
		forEachGear(false, item -> item.onAttack(event));
	}
	
	/**
	 * Trigger attacked events for the set of equipped {@link CustomItem}s in
	 * gear set.
	 * 
	 * @param event - the triggering attacked event
	 */
	public void onAttacked(CustomDamageEvent event)
	{
		forEachGear(false, item -> item.onAttacked(event));
	}

	private void forEachGear(boolean fullInventory, Consumer<CustomItem> itemConsumer)
	{
		if (fullInventory)
		{
			_inventory.values().stream().filter(Objects::nonNull).forEach(itemConsumer);
		}
		else
		{
			CustomItem weapon = getWeapon();
			if (weapon != null)
			{
				itemConsumer.accept(weapon);
			}
		}
		CustomItem helmet = getHelmet();
		if (helmet != null)
		{
			itemConsumer.accept(helmet);
		}
		CustomItem chestplate = getChestplate();
		if (chestplate != null)
		{
			itemConsumer.accept(chestplate);
		}
		CustomItem leggings = getLeggings();
		if (leggings != null)
		{
			itemConsumer.accept(leggings);
		}
		CustomItem boots = getBoots();
		if (boots != null)
		{
			itemConsumer.accept(boots);
		}
	}

	public CustomItem getWeapon()
	{
		return _weapon;
	}
	
	public CustomItem getHelmet()
	{		
		return _helmet;
	}
	
	public CustomItem getChestplate()
	{		
		return _chestplate;
	}
	
	public CustomItem getLeggings()
	{		
		return _leggings;
	}
	
	public CustomItem getBoots()
	{
		return _boots;
	}

	/**
	 * Perform cleanup if necessary. If cleanup was performed, return true. Otherwise return false
	 */
	public boolean cleanup()
	{
		return !getPlayer().isOnline();
	}
}