package mineplex.game.clans.clans.banners;

import java.util.LinkedList;
import java.util.List;

import mineplex.core.common.util.C;
import mineplex.game.clans.clans.ClanInfo;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import com.google.common.collect.Lists;

/**
 * Data class for clan banners
 */
public class ClanBanner
{
	private BannerManager _manager;
	private ClanInfo _clan;
	private DyeColor _baseColor;
	private LinkedList<BannerPattern> _patterns;
	
	public ClanBanner(BannerManager manager, ClanInfo clan, DyeColor baseColor, LinkedList<BannerPattern> patterns)
	{
		_manager = manager;
		_clan = clan;
		_baseColor = baseColor;
		_patterns = patterns;
	}
	
	/**
	 * Gets the clan that owns this banner
	 * @return The clan that owns this banner
	 */
	public ClanInfo getClan()
	{
		return _clan;
	}
	
	/**
	 * Gets the base color for this banner
	 * @return The base color for this banner
	 */
	public DyeColor getBaseColor()
	{
		return _baseColor;
	}
	
	/**
	 * Gets this banner's patterns
	 * @return This banner's patterns
	 */
	public LinkedList<BannerPattern> getPatterns()
	{
		return _patterns;
	}
	
	/**
	 * Gets the ItemStack representation of this banner
	 * @return The ItemStack representation of this banner
	 */
	public ItemStack getBanner()
	{
		ItemStack banner = new ItemStack(Material.BANNER);
		BannerMeta im = (BannerMeta) banner.getItemMeta();
		
		im.setDisplayName(C.cGray + _clan.getName() + "'s Banner");
		im.setBaseColor(_baseColor);
		List<Pattern> patterns = Lists.newArrayList();
		for (BannerPattern bp : _patterns)
		{
			if (bp.getBukkitPattern() != null)
			{
				patterns.add(bp.getBukkitPattern());
			}
		}
		im.setPatterns(patterns);
		banner.setItemMeta(im);
		
		return banner;
	}
	
	/**
	 * Sets the base color of this banner
	 * @param color The color to set
	 */
	public void setBaseColor(DyeColor color)
	{
		_baseColor = color;
	}
	
	/**
	 * Saves this banner to the database
	 */
	public void save()
	{
		_manager.saveBanner(this);
	}
	
	/**
	 * Places this banner on the ground
	 * @param player The player to place the banner for
	 */
	public void place(Player player)
	{
		_manager.placeBanner(player, this);
	}
}