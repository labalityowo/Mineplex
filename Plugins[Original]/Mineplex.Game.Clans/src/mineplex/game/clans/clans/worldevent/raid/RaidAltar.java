package mineplex.game.clans.clans.worldevent.raid;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.clans.ClansManager;

public class RaidAltar
{
	private Block _altarBlock;
	private RaidType _type;
	private List<ItemStack> _requiredItems;
	
	public RaidAltar(Block altarBlock, RaidType type, List<ItemStack> requiredItems)
	{
		_altarBlock = altarBlock;
		_type = type;
		_requiredItems = requiredItems;
	}
	
	@SuppressWarnings("deprecation")
	private boolean has(Player player, Material type, String itemName, List<String> lore, Byte data, Integer amount)
	{
		int count = 0;
		for (ItemStack item : player.getInventory().getContents())
		{
			boolean rejected = false;
			if (item == null || item.getType() == Material.AIR)
			{
				continue;
			}
			if (type != null && item.getType() != type)
			{
				rejected = true;
			}
			if (itemName != null && (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName() || !item.getItemMeta().getDisplayName().equals(itemName)))
			{
				rejected = true;
			}
			if (lore != null)
			{
				if (!item.hasItemMeta() || !item.getItemMeta().hasLore())
				{
					rejected = true;
				}
				else
				{
					List<String> itemLore = item.getItemMeta().getLore();
					if (itemLore.size() != lore.size())
					{
						rejected = true;
					}
					else
					{
						for (int i = 0; i < lore.size(); i++)
						{
							if (!itemLore.get(i).equals(lore.get(i)))
							{
								rejected = true;
							}
						}
					}
				}
			}
			if (data != null)
			{
				if (item.getData().getData() != data)
				{
					rejected = true;
				}
			}
			
			if (!rejected)
			{
				count += item.getAmount();
			}
		}
		
		if (amount != null)
		{
			return count >= amount;
		}
		else
		{
			return count > 0;
		}
	}
	
	@SuppressWarnings("deprecation")
	private void remove(Player player, Material type, String itemName, List<String> lore, Byte data, int amount)
	{
		int removed = 0;
		for (int i = 0; i < player.getInventory().getContents().length; i++)
		{
			ItemStack item = player.getInventory().getContents()[i];
			if (removed >= amount)
			{
				return;
			}
			boolean rejected = false;
			if (item == null || item.getType() == Material.AIR)
			{
				continue;
			}
			if (type != null && item.getType() != type)
			{
				rejected = true;
			}
			if (itemName != null && (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName() || !item.getItemMeta().getDisplayName().equals(itemName)))
			{
				rejected = true;
			}
			if (lore != null)
			{
				if (!item.hasItemMeta() || !item.getItemMeta().hasLore())
				{
					rejected = true;
				}
				else
				{
					List<String> itemLore = item.getItemMeta().getLore();
					if (itemLore.size() != lore.size())
					{
						rejected = true;
					}
					else
					{
						for (int index = 0; index < lore.size(); index++)
						{
							if (!itemLore.get(index).equals(lore.get(index)))
							{
								rejected = true;
							}
						}
					}
				}
			}
			if (data != null)
			{
				if (item.getData().getData() != data)
				{
					rejected = true;
				}
			}
			
			if (!rejected)
			{
				if (item.getAmount() > (amount - removed))
				{
					removed += (amount - removed);
					item.setAmount(item.getAmount() - removed);
				}
				else
				{
					removed += item.getAmount();
					player.getInventory().setItem(i, null);
				}
			}
		}
		
		player.updateInventory();
	}
	
	@SuppressWarnings("deprecation")
	public boolean handleInteract(Player player, Block block)
	{
		if (_altarBlock.equals(block))
		{
			for (ItemStack required : _requiredItems)
			{
				String displayName = ((required.hasItemMeta() && required.getItemMeta().hasDisplayName()) ? required.getItemMeta().getDisplayName() : null);
				List<String> lore = ((required.hasItemMeta() && required.getItemMeta().hasLore()) ? required.getItemMeta().getLore() : null);
				if (!has(player, required.getType(), displayName, lore, required.getData().getData(), required.getAmount()))
				{
					UtilPlayer.message(player, F.main(_type.getRaidName() + " Raid", "You do not have the required summoning items for this raid!"));
					return true;
				}
			}
			if (ClansManager.getInstance().getWorldEvent().getRaidManager().startRaid(player, _type))
			{
				for (ItemStack required : _requiredItems)
				{
					String displayName = ((required.hasItemMeta() && required.getItemMeta().hasDisplayName()) ? required.getItemMeta().getDisplayName() : null);
					List<String> lore = ((required.hasItemMeta() && required.getItemMeta().hasLore()) ? required.getItemMeta().getLore() : null);
					remove(player, required.getType(), displayName, lore, required.getData().getData(), required.getAmount());
				}
			}
			return true;
		}
		
		return false;
	}
}