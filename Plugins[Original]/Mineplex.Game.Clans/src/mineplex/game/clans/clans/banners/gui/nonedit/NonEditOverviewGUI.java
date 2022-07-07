package mineplex.game.clans.clans.banners.gui.nonedit;

import java.util.Arrays;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.game.clans.clans.ClanRole;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.HelmetPacketManager;
import mineplex.game.clans.clans.banners.ClanBanner;
import mineplex.game.clans.clans.banners.gui.BannerGUI;
import mineplex.game.clans.clans.banners.gui.overview.OverviewGUI;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * GUI manager for viewing a banner usage overview
 */
public class NonEditOverviewGUI extends BannerGUI
{
	private ClanBanner _banner;
	
	public NonEditOverviewGUI(Player viewer, ClanBanner banner)
	{
		super(viewer, "Clan Banner", 9);
		_banner = banner;
		
		propagate();
		open();
	}
	
	@Override
	public void propagate()
	{
		ItemStack banner = _banner.getBanner().clone();
		ItemMeta im = banner.getItemMeta();
		im.setDisplayName(C.cGreen + "Clan Banner");
		im.setLore(Arrays.asList(new String[] {F.elem("Left Click") + " to toggle wearing your banner", F.elem("Right Click") + " to place down your banner"}));
		banner.setItemMeta(im);
		getItems().put(4, banner);
		
		if (_banner.getClan().getMembers().get(getViewer().getUniqueId()).getRole() == ClanRole.LEADER && ClansManager.getInstance().getBannerManager().getBannerUnlockLevel(getViewer()) >= 2)
		{
			getItems().put(8, new ItemBuilder(Material.ANVIL).setTitle(C.cGold + "Edit Your Banner").build());
		}
		refresh();
	}

	@Override
	public void onClick(Integer slot, ClickType type)
	{
		if (slot == 4)
		{
			if (type == ClickType.LEFT)
			{
				if (UtilEnt.GetMetadata(getViewer(), "HelmetPacket.Banner") != null)
				{
					UtilEnt.removeMetadata(getViewer(), "HelmetPacket.Banner");
					HelmetPacketManager.getInstance().refreshToAll(getViewer(), null);
					UtilPlayer.message(getViewer(), F.main("Clan Banners", "You have removed your banner!"));
				}
				else
				{
					ItemStack banner = _banner.getBanner();
					UtilEnt.SetMetadata(getViewer(), "HelmetPacket.Banner", banner);
					HelmetPacketManager.getInstance().refreshToAll(getViewer(), banner);
					UtilPlayer.message(getViewer(), F.main("Clan Banners", "You have donned your banner!"));
				}
			}
			else if (type == ClickType.RIGHT)
			{
				_banner.place(getViewer());
			}
		}
		if (slot == 8)
		{
			ClansManager.getInstance().runSyncLater(() ->
			{
				getViewer().closeInventory();
			}, 1L);
			ClansManager.getInstance().runSyncLater(() ->
			{
				new OverviewGUI(getViewer(), _banner);
			}, 2L);
		}
	}
}