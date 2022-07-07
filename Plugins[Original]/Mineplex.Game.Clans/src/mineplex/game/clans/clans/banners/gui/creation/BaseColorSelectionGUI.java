package mineplex.game.clans.clans.banners.gui.creation;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.banners.ClanBanner;
import mineplex.game.clans.clans.banners.gui.BannerGUI;
import mineplex.game.clans.clans.banners.gui.overview.OverviewGUI;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

/**
 * GUI manager for selecting a banner's base color
 */
public class BaseColorSelectionGUI extends BannerGUI
{
	private ClanBanner _banner;
	
	public BaseColorSelectionGUI(Player viewer, ClanBanner banner)
	{
		super(viewer, "Background Color", 27);
		_banner = banner;
		
		propagate();
		open();
	}
	
	@Override
	public void propagate()
	{
		Integer slot = 1;
		for (short data = 0; data <= 15; data++)
		{
			getItems().put(slot, new ItemBuilder(Material.INK_SACK).setData(data).setTitle(C.cGray).build());
			if ((slot + 1) == 8)
			{
				slot = 10;
			}
			else if ((slot + 1) == 17)
			{
				slot = 21;
			}
			else if ((slot + 1) == 22)
			{
				slot = 23;
			}
			else
			{
				slot++;
			}
		}
		refresh();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(Integer slot, ClickType type)
	{
		if (getItems().get(slot).getType() == Material.INK_SACK)
		{
			DyeColor color = DyeColor.getByDyeData(getItems().get(slot).getData().getData());
			_banner.setBaseColor(color);
			_banner.save();
			
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