package mineplex.game.clans.clans.banners.gui.overview;

import java.util.Arrays;

import mineplex.core.common.util.C;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.banners.BannerPattern;
import mineplex.game.clans.clans.banners.ClanBanner;
import mineplex.game.clans.clans.banners.gui.BannerGUI;
import mineplex.game.clans.clans.banners.gui.creation.BaseColorSelectionGUI;
import mineplex.game.clans.clans.banners.gui.creation.PatternColorSelectionGUI;
import mineplex.game.clans.clans.banners.gui.nonedit.NonEditOverviewGUI;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

/**
 * GUI manager for overviewing a banner's design
 */
public class OverviewGUI extends BannerGUI
{
	private static final int MAX_BANNER_LAYERS = 12;
	private static final int PATTERN_DISPLAY_START_SLOT = 27;
	private final ClanBanner _banner;
	
	public OverviewGUI(Player viewer, ClanBanner banner)
	{
		super(viewer, "Clan Banner", 45);
		_banner = banner;
		
		propagate();
		open();
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public void propagate()
	{
		getItems().put(4, _banner.getBanner());
		ItemStack color = new MaterialData(Material.INK_SACK, _banner.getBaseColor().getDyeData()).toItemStack(1);
		ItemMeta colorMeta = color.getItemMeta();
		colorMeta.setDisplayName(C.cGray);
		color.setItemMeta(colorMeta);
		getItems().put(22, color);
		Integer slot = PATTERN_DISPLAY_START_SLOT;
		for (int i = 0; i < MAX_BANNER_LAYERS; i++)
		{
			ItemStack item = new ItemStack(Material.INK_SACK, 1, (short)8);
			if (i < _banner.getPatterns().size())
			{
				BannerPattern pattern = _banner.getPatterns().get(i);
				if (pattern.getBukkitPattern() != null)
				{
					ItemStack representation = new ItemStack(Material.BANNER);
					BannerMeta im = (BannerMeta) representation.getItemMeta();
					im.setBaseColor(_banner.getBaseColor());
					im.setDisplayName(C.cGray);
					im.setPatterns(Arrays.asList(pattern.getBukkitPattern()));
					representation.setItemMeta(im);
					item = representation;
				}
			}
			getItems().put(slot, item);
			slot++;
		}
		refresh();
	}

	@Override
	public void onClick(Integer slot, ClickType type)
	{
		if (slot == 4)
		{
			ClansManager.getInstance().runSyncLater(() ->
			{
				getViewer().closeInventory();
			}, 1L);
			ClansManager.getInstance().runSyncLater(() ->
			{
				new NonEditOverviewGUI(getViewer(), _banner);
			}, 2L);
		}
		if (slot == 22)
		{
			ClansManager.getInstance().runSyncLater(() ->
			{
				getViewer().closeInventory();
			}, 1L);
			ClansManager.getInstance().runSyncLater(() ->
			{
				new BaseColorSelectionGUI(getViewer(), _banner);
			}, 2L);
		}
		if (slot >= PATTERN_DISPLAY_START_SLOT)
		{
			if (type == ClickType.RIGHT)
			{
				_banner.getPatterns().get(slot - PATTERN_DISPLAY_START_SLOT).setColor(null);
				ClansManager.getInstance().runSyncLater(() ->
				{
					getItems().clear();
					propagate();
					refresh();
				}, 1L);
			}
			else
			{
				ClansManager.getInstance().runSyncLater(() ->
				{
					getViewer().closeInventory();
				}, 1L);
				ClansManager.getInstance().runSyncLater(() ->
				{
					new PatternColorSelectionGUI(getViewer(), _banner, slot - PATTERN_DISPLAY_START_SLOT);
				}, 2L);
			}
		}
	}
}