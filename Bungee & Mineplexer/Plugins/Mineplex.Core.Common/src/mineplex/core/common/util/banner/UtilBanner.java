package mineplex.core.common.util.banner;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

public class UtilBanner
{

	public static ItemStack createBanner(DyeColor baseColor, Pattern... patterns)
	{
		ItemStack banner = new ItemStack(Material.BANNER);
		BannerMeta bannerMeta = (BannerMeta) banner.getItemMeta();
		bannerMeta.setBaseColor(baseColor);
		for (Pattern pattern : patterns)
		{
			bannerMeta.addPattern(pattern);
		}
		banner.setItemMeta(bannerMeta);
		return banner;
	}

}
