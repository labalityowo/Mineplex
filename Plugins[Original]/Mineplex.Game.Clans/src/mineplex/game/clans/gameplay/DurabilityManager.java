package mineplex.game.clans.gameplay;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.items.GearManager;

public class DurabilityManager implements Listener
{
	private final Map<Material, Integer> _itemDurabilities = new HashMap<>();
	
	public DurabilityManager()
	{
		_itemDurabilities.put(Material.DIAMOND_HELMET, 900);
		_itemDurabilities.put(Material.DIAMOND_CHESTPLATE, 900);
		_itemDurabilities.put(Material.DIAMOND_LEGGINGS, 900);
		_itemDurabilities.put(Material.DIAMOND_BOOTS, 900);
		_itemDurabilities.put(Material.DIAMOND_SWORD, 900);
		_itemDurabilities.put(Material.DIAMOND_AXE, 900);
		_itemDurabilities.put(Material.IRON_HELMET, 900);
		_itemDurabilities.put(Material.IRON_CHESTPLATE, 900);
		_itemDurabilities.put(Material.IRON_LEGGINGS, 900);
		_itemDurabilities.put(Material.IRON_BOOTS, 900);
		_itemDurabilities.put(Material.IRON_SWORD, 900);
		_itemDurabilities.put(Material.IRON_AXE, 900);
		_itemDurabilities.put(Material.CHAINMAIL_HELMET, 900);
		_itemDurabilities.put(Material.CHAINMAIL_CHESTPLATE, 900);
		_itemDurabilities.put(Material.CHAINMAIL_LEGGINGS, 900);
		_itemDurabilities.put(Material.CHAINMAIL_BOOTS, 900);
		_itemDurabilities.put(Material.GOLD_HELMET, 900);
		_itemDurabilities.put(Material.GOLD_CHESTPLATE, 900);
		_itemDurabilities.put(Material.GOLD_LEGGINGS, 900);
		_itemDurabilities.put(Material.GOLD_BOOTS, 900);
		_itemDurabilities.put(Material.GOLD_SWORD, 900);
		_itemDurabilities.put(Material.GOLD_AXE, 900);
		_itemDurabilities.put(Material.LEATHER_HELMET, 900);
		_itemDurabilities.put(Material.LEATHER_CHESTPLATE, 900);
		_itemDurabilities.put(Material.LEATHER_LEGGINGS, 900);
		_itemDurabilities.put(Material.LEATHER_BOOTS, 900);
	}
	
	private boolean canRepair(ItemStack item)
	{
		int repairs = ItemStackFactory.Instance.GetLoreVar(item, "Repaired", 0);
		boolean canRepair = true;
		
		if (repairs >= 2)
		{
			canRepair = false;
		}
		
		return canRepair;
	}
	
	private int getItemDamage(ItemStack item)
	{
		if (item == null)
		{
			return 0;
		}
		if (_itemDurabilities.containsKey(item.getType()))
		{
			int defaultDurability = _itemDurabilities.get(item.getType());
			return defaultDurability - ItemStackFactory.Instance.GetLoreVar(item, "Durability", 0);
		}
		else
		{
			return item.getDurability();
		}
	}
	
	private int itemDuraToLoreDura(ItemStack item)
	{
		if (item == null || !_itemDurabilities.containsKey(item.getType()) || UtilItem.isUnbreakable(item))
		{
			return -1;
		}
		int currentDura = ItemStackFactory.Instance.GetLoreVar(item, "Durability", -1);
		if (item.getDurability() == 0 && currentDura != -1)
		{
			return -1;
		}
		if (currentDura == -1)
		{
			updateItemDamage(item, 0, true);
			return -2;
		}
		int newDura = (currentDura - item.getDurability());
		if (newDura <= 0)
		{
			return 0;
		}
		else
		{
			updateItemDamage(item, newDura, false);
			item.setDurability((short)0);
		}
		return newDura;
	}
	
	private void updateItemDamage(ItemStack item, int itemDamage, boolean subtractFromDefault)
	{
		if (item == null)
		{
			return;
		}
		if (_itemDurabilities.containsKey(item.getType()))
		{
			int defaultDurability = _itemDurabilities.get(item.getType());
			ItemStackFactory.Instance.SetLoreVar(item, "Durability", String.valueOf(Math.min(defaultDurability, subtractFromDefault ? (defaultDurability - itemDamage) : itemDamage)));
		}
		else
		{
			item.setDurability((short)itemDamage);
		}
	}
	
	@EventHandler
	public void onDamageItem(PlayerItemDamageEvent event)
	{
		if (event.getItem() == null)
		{
			return;
		}
		if (_itemDurabilities.containsKey(event.getItem().getType()))
		{
			int damage = event.getDamage();
			int defaultDurability = _itemDurabilities.get(event.getItem().getType());
			int currentDurability = ItemStackFactory.Instance.GetLoreVar(event.getItem(), "Durability", defaultDurability);
			int newDurability = currentDurability - damage;
			if (newDurability > 0)
			{
				event.setCancelled(true);
				ItemStackFactory.Instance.SetLoreVar(event.getItem(), "Durability", String.valueOf(newDurability));
			}
			else
			{
				event.setDamage(999999);
			}
		}
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() == UpdateType.SEC)
		{
			Bukkit.getOnlinePlayers().forEach(player ->
			{
				boolean change = false;
				for (int i = 0; i < player.getInventory().getArmorContents().length; i++)
				{
					int d = itemDuraToLoreDura(player.getInventory().getArmorContents()[i]);
					if (d == -2)
					{
						change = true;
					}
					else if (d == 0)
					{
						ItemStack[] armor = new ItemStack[4];
						for (int ar = 0; ar < armor.length; ar++)
						{
							if (ar != i)
							{
								armor[ar] = player.getInventory().getArmorContents()[ar];
							}
							else
							{
								armor[ar] = null;
							}
						}
						player.getInventory().setArmorContents(armor);
						change = true;
					}
					else if (d != -1)
					{
						change = true;
					}
				}
				for (int i = 0; i < player.getInventory().getContents().length; i++)
				{
					int d = itemDuraToLoreDura(player.getInventory().getContents()[i]);
					if (d == -2)
					{
						change = true;
					}
					else if (d == 0)
					{
						player.getInventory().setItem(i, null);
						change = true;
					}
					else if (d != -1)
					{
						change = true;
					}
				}
				
				if (change)
				{
					UtilInv.Update(player);
				}
			});
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onRepair(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		
		if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.ANVIL || !UtilEvent.isAction(event, ActionType.R_BLOCK) || player.isSneaking() || player.getItemInHand().getType() == Material.AIR)
		{
			return;
		}
		
		if (UtilMath.offset(player.getLocation(), event.getClickedBlock().getLocation()) > 2)
		{
			UtilPlayer.message(player, F.main("Repair", "You are too far from the " + F.item("Anvil") + "."));
			return;
		}
		
		ItemStack item = player.getItemInHand();
		
		if (getItemDamage(item) <= 0)
		{
			UtilPlayer.message(player, F.main("Repair", "Your " + F.item(item == null ? ChatColor.YELLOW + "Hand" : item.getItemMeta().getDisplayName()) + " does not need repairs."));
			return;
		}
		
		if (!UtilGear.isRepairable(item))
		{
			UtilPlayer.message(player, F.main("Repair", "You cannot repair " + F.item(item.getItemMeta().getDisplayName()) + "."));
			return;
		}
		
		if (GearManager.isCustomItem(item))
		{
			UtilPlayer.message(player, F.main("Repair", "You cannot repair " + F.item(item.getItemMeta().getDisplayName()) + "."));
			return;
		}
		
		int repairs = ItemStackFactory.Instance.GetLoreVar(item, "Repaired", 0);
		boolean canRepair = canRepair(item);
		
		if (!canRepair)
		{
			UtilPlayer.message(player, F.main("Repair", "This item cannot be repaired anymore."));
			return;
		}
		
		String creator = ItemStackFactory.Instance.GetLoreVar(item, "Owner");
		
		if (creator != null)
		{
			if (creator.length() > 2) creator = creator.substring(2, creator.length());
			
			if (!creator.equals(player.getName()))
			{
				UtilPlayer.message(player, F.main("Repair", "You cannot repair " + F.item(item.getItemMeta().getDisplayName()) + " by " + F.name(creator) + "."));
				return;
			}
		}

		if (ClansManager.getInstance().getBlockRestore().contains(event.getClickedBlock()))
		{
			UtilPlayer.message(player, F.main("Repair", "You cannot repair using that anvil."));
			return;
		}
		
		// Repair!
		UtilPlayer.message(player, F.main("Repair", "You repaired " + F.item(item.getItemMeta().getDisplayName()) + "."));
		updateItemDamage(item, 0, true);
		UtilInv.Update(player);
		
		// Break
		if (Math.random() > 0.85)
		{
			byte data = event.getClickedBlock().getData();
			if (data >= 8) // Anvil has already been damaged twice
			{
				player.getWorld().playEffect(event.getClickedBlock().getLocation(), Effect.STEP_SOUND, 145);
				event.getClickedBlock().setType(Material.AIR);
			}
			else
			{
				event.getClickedBlock().setData((byte)(data + 4));
			}
		}
		
		// Record
		ItemStackFactory.Instance.SetLoreVar(item, "Repaired", (repairs + 1) + "");
		if (!canRepair(item))
		{
			ItemMeta meta = item.getItemMeta();
			meta.getLore().add(ChatColor.BLUE + "Unrepairable");
			item.setItemMeta(meta);
		}
		
		// Effect
		player.playSound(player.getLocation(), Sound.ANVIL_USE, 1f, 1f);
	}
}