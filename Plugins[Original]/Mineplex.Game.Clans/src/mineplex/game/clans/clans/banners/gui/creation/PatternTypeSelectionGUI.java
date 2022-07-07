package mineplex.game.clans.clans.banners.gui.creation;

import java.util.LinkedList;

import mineplex.core.common.util.C;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.banners.BannerPattern;
import mineplex.game.clans.clans.banners.ClanBanner;
import mineplex.game.clans.clans.banners.gui.BannerGUI;
import mineplex.game.clans.clans.banners.gui.overview.OverviewGUI;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

/**
 * GUI manager for selecting a banner pattern's design
 */
public class PatternTypeSelectionGUI extends BannerGUI
{
	private ClanBanner _banner;
	private int _bannerPos;
	private DyeColor _color;
	
	public PatternTypeSelectionGUI(Player viewer, ClanBanner banner, int bannerPosition, DyeColor patternColor)
	{
		super(viewer, "Pattern Color", 45);
		_banner = banner;
		_bannerPos = bannerPosition;
		_color = patternColor;
		
		propagate();
		open();
	}
	
	@Override
	public void propagate()
	{
		for (int i = 0; i < PatternType.values().length; i++)
		{
			ItemStack item = new ItemStack(Material.BANNER);
			BannerMeta im = (BannerMeta) item.getItemMeta();
			im.setBaseColor(_banner.getBaseColor());
			LinkedList<Pattern> patterns = new LinkedList<>();
			for (int patternId = 0; patternId < _bannerPos; patternId++)
			{
				BannerPattern show = _banner.getPatterns().get(patternId);
				if (show.getBukkitPattern() != null)
				{
					patterns.add(show.getBukkitPattern());
				}
			}
			patterns.add(new Pattern(_color, PatternType.values()[i]));
			im.setPatterns(patterns);
			im.setDisplayName(C.cGray);
			item.setItemMeta(im);
			getItems().put(i, item);
		}
		refresh();
	}

	@Override
	public void onClick(Integer slot, ClickType ctype)
	{
		if (getItems().get(slot).getType() == Material.BANNER)
		{
			PatternType type = ((BannerMeta)getItems().get(slot).getItemMeta()).getPatterns().get(Math.min(_bannerPos, ((BannerMeta)getItems().get(slot).getItemMeta()).getPatterns().size() - 1)).getPattern();
			BannerPattern pattern = _banner.getPatterns().get(_bannerPos);
			pattern.setPatternType(type);
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