package nautilus.game.arcade.game.games.minestrike.items;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.itemstack.ItemStackFactory;
import nautilus.game.arcade.game.games.minestrike.GunModule;
import nautilus.game.arcade.game.games.minestrike.items.grenades.Grenade;
import nautilus.game.arcade.game.games.minestrike.items.guns.Gun;

public abstract class StrikeItem
{
	private StrikeItemType _type;
	private String _name;
	private String[] _desc;
	private int _cost;
	private int _gemCost; 
	private Material _skinMaterial;
	private byte _skinData;
	
	private String _ownerName;

	private ItemStack _stack = null;
	
	private int _identifier;

	public StrikeItem(StrikeItemType type, String name, String[] desc, int cost, int gemCost, Material skin)
	{
		_type = type;
		_name = name;
		_desc = desc;
		_cost = cost;
		_gemCost = gemCost;
		_skinMaterial = skin;

		//Make Stack
		_stack = new ItemStack(skin);

		_identifier = UtilMath.r(9000) + 1000;
		
		addID();
		
		fixStackName();
	}

	public StrikeItemType getType()
	{
		return _type;
	}

	public String getName()
	{
		return _name;
	}

	public String[] getDesc()
	{
		return _desc;
	}

	public int getCost()
	{
		return _cost;
	}

	public int getGemCost()
	{
		return _gemCost;
	}

	public Material getSkinMaterial()
	{
		return _skinMaterial;
	}
	
	public byte getSkinData()
	{
		return _skinData;
	}
	
	public void addID()
	{	
		ItemMeta meta = _stack.getItemMeta();
		meta.setLore(Arrays.asList(ChatColor.RED + "" + ChatColor.BOLD + "Identifier: " + _identifier));
		_stack.setItemMeta(meta);
	}
	
	public void setSkin(Material skinMaterial, byte skinData)
	{
		_skinMaterial = skinMaterial;
		_skinData = skinData;
		
		_stack = new ItemStack(skinMaterial, 1, (short) 0, skinData);
		addID();
	}

	public String getOwnerName()
	{
		return _ownerName;
	}
	
	public void setOwnerName(String ownerName)
	{
		_ownerName = ownerName;
	}
	
	public void drop(GunModule game, Player player, boolean natural, boolean onlyDeregisterAndRemove)
	{
		_stack.setAmount(1);
		
		Entity ent;

		if (natural)
		{
			ent = player.getWorld().dropItemNaturally(player.getEyeLocation(), _stack);
		}
		else
		{
			ent = player.getWorld().dropItem(player.getEyeLocation(), _stack);
			UtilAction.velocity(ent, player.getLocation().getDirection(), 0.4, false, 0, 0.1, 1, false);
		}
		
		if (this instanceof Gun)
		{
			//Fixes it so it stops reloading when dropped. Allows other players to reload when it's dropped if its ammo is 0.
			((Gun) this).forceCancelReload(player);
			
			game.deregisterGun((Gun)this);
			
			if (!onlyDeregisterAndRemove)
				game.registerDroppedGun(ent, (Gun)this);
		}
			
		else if (this instanceof Grenade)
		{
			game.deregisterGrenade((Grenade)this);
			
			if (!onlyDeregisterAndRemove)
				game.registerDroppedGrenade(ent, (Grenade)this);
		}
		
		if (onlyDeregisterAndRemove)
			ent.remove();
	}

	public ItemStack getStack()
	{
		return _stack;
	}
	
	public void setStack(ItemStack stack)
	{
		_stack = stack;
	}

	public boolean isHolding(Player player)
	{
		return UtilGear.isMat(player.getItemInHand(), _skinMaterial);
	}
	
	public boolean isStack(ItemStack stack)
	{
		if (stack.hasItemMeta())
		{
			if (stack.getItemMeta().hasLore())
			{
				if (getStack().hasItemMeta())
				{
					if (getStack().getItemMeta().hasLore())
					{
						if (UtilGear.isMat(stack, _skinMaterial))
							return stack.getItemMeta().getLore().get(0).equalsIgnoreCase(getStack().getItemMeta().getLore().get(0));
						else
							return false;
					}
				}
			}
		}
		return UtilGear.isMat(stack, _skinMaterial);
	}

	public void giveToPlayer(Player player, int slot, boolean setOwnerName)
	{
		if (setOwnerName)
			_ownerName = player.getName();
		
		fixStackName();

		player.getInventory().setItem(slot, getStack());

		UtilPlayer.message(player, F.main("Game", "You equipped " + getName() + "."));

		player.getWorld().playSound(player.getLocation(), Sound.ITEM_PICKUP, 1.5f, 1f);
	}

	public void fixStackName()
	{
		ItemMeta meta = _stack.getItemMeta();
		meta.setDisplayName(ChatColor.RESET + (_ownerName == null ? "" : _ownerName + "'s ") + C.Bold + getName() + ChatColor.RESET);
		_stack.setItemMeta(meta);
	}

	public abstract boolean pickup(GunModule game, Player player);

	public ItemStack getShopItem(int money, boolean alreadyHas)
	{
		ArrayList<String> lore = new ArrayList<String>();

		for (String cur : _desc)
			lore.add(C.cWhite + cur);

		//Custom (Gun Stats)
		if (getShopItemCustom().length > 0)
		{
			lore.add(" ");
			for (String cur : getShopItemCustom())
				lore.add(C.cWhite + cur);
		}
		
		if (alreadyHas)
		{
			lore.add(" ");
			lore.add(C.cYellow + C.Bold + "You already have this!");
		}
		else
		{
			lore.add(" ");
			lore.add(C.cYellow + C.Bold + "Cost: " + ChatColor.RESET + "$" + _cost);
			lore.add(" ");
			lore.add(C.cYellow + C.Bold + "Money: " + ChatColor.RESET + "$" + money);
			lore.add(" ");
			lore.add((money >= _cost) ? C.cGreen + C.Bold + "Click to Purchase" : C.cRed + C.Bold + "Not enough Money");

		}	

		String[] loreArray = new String[lore.size()];
		loreArray = lore.toArray(loreArray);
		
		String name = getShopItemType() + " " + C.cGreen + getName();

		ItemStack item = ItemStackFactory.Instance.CreateStack(_skinMaterial, (byte)0, 1, name, loreArray);
		
		if (alreadyHas)
			UtilInv.addDullEnchantment(item);
		
		return item;
	}

	public String[] getShopItemCustom()
	{
		return new String[] {};
	}
	
	public String getShopItemType()
	{
		return "";
	}
}
