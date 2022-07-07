package mineplex.game.clans.clans.banners.gui.creation;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.banners.BannerPattern;
import mineplex.game.clans.clans.banners.ClanBanner;
import mineplex.game.clans.clans.banners.gui.BannerGUI;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

/**
 * GUI manager for selecting a banner pattern's base color
 */
public class PatternColorSelectionGUI extends BannerGUI
{
	private ClanBanner _banner;
	private int _bannerPos;
	
	public PatternColorSelectionGUI(Player viewer, ClanBanner banner, int bannerPosition)
	{
		super(viewer, "Pattern Color", 27);
		_banner = banner;
		_bannerPos = bannerPosition;
		
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
			BannerPattern pattern = _banner.getPatterns().get(_bannerPos);
			pattern.setColor(color);
			
			ClansManager.getInstance().runSyncLater(() ->
			{
				getViewer().closeInventory();
			}, 1L);
			ClansManager.getInstance().runSyncLater(() ->
			{
				new PatternTypeSelectionGUI(getViewer(), _banner, _bannerPos, color);
			}, 2L);
		}
	}
}