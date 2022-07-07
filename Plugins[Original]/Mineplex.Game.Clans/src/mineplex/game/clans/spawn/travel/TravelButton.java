package mineplex.game.clans.spawn.travel;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mineplex.core.common.util.C;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.F;
import mineplex.core.common.util.InventoryUtil;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.item.IButton;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.economy.GoldManager;
import mineplex.game.clans.items.CustomItem;
import mineplex.game.clans.items.GearManager;
import mineplex.game.clans.items.economy.GoldToken;
import mineplex.game.clans.shop.PvpItem;
import mineplex.game.clans.spawn.Spawn;

public class TravelButton implements IButton
{

	private static final String LEFT_CLICK_TRAVEL = C.cYellow + "Left-Click" + C.cWhite + " to Warp";
	
	private TravelPage _page;
	private Location _location;
	private Material _iconMaterial;
	private String _name;
	private String[] _lore;
	private byte _data;
	
	public TravelButton(TravelPage page, Location location, Material material, String name, String[] lore, byte data)
	{
		_data =data;
		_page = page;
		_location = location;
		_iconMaterial = material;
		_name = name;
		_lore = lore;
	}
	
	@Override
	public void onClick(Player player, ClickType clickType)
	{
		if (clickType != ClickType.LEFT)
		{
			return;
		}
		
		if (_location == null)
		{
			return;
		}
		
		if (player.getLocation().distance(_location) <= 64)
		{
			return;
		}
		
		transportPlayer(player);
	}
	
	public ItemStack generateButtonItem()
	{
		Object[] lore =
				{
					C.cWhite + " ",
					LEFT_CLICK_TRAVEL,
					C.cWhite + " ",
					
				};
		
		
		if (_lore != null)
		{
			lore = ArrayUtils.addAll(lore, _lore);
		}
		
		String[] strLore = new String[lore.length];
		
		int index = 0;
		for (Object obj : lore)
		{
			strLore[index] = obj.toString();
			index++;
		}
		
		ShopItem item = new ShopItem(_iconMaterial, (byte)_data, _name, strLore, 0, false, false);
		return item;
	}
	
	private void transportPlayer(Player player)
	{
		player.teleport(_location);
		_page.playAcceptSound(player);
		// TODO: Notify player? Effects here?
	}
}
