package mineplex.game.clans.clans.banners;

import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;

/**
 * Data class for clans banner patterns
 */
public class BannerPattern
{
	private int _layer;
	private DyeColor _color;
	private PatternType _type;
	
	public BannerPattern(int layer, DyeColor color, PatternType type)
	{
		_layer = layer;
		_color = color;
		_type = type;
	}
	
	public BannerPattern(int layer)
	{
		_layer = layer;
		_color = null;
		_type = null;
	}
	
	/**
	 * Gets the layer this pattern occupies on the clan's banner
	 * @return The layer this pattern occupies on the clan's banner
	 */
	public int getLayer()
	{
		return _layer;
	}
	
	/**
	 * Gets the Bukkit version of this banner pattern
	 * @return The Bukkit version of this banner pattern, or null if this is not a fully configured pattern
	 */
	public Pattern getBukkitPattern()
	{
		if (_color == null || _type == null)
		{
			return null;
		}
		
		return new Pattern(_color, _type);
	}
	
	/**
	 * Gets the form this banner pattern will take in the database
	 * @return The form this banner pattern will take in the database
	 */
	public String getDatabaseForm()
	{
		if (_color == null || _type == null)
		{
			return "Blank";
		}
		
		return _color.toString() + "," + _type.toString();
	}
	
	/**
	 * Sets the color of this banner pattern
	 * @param color The color to set this pattern to
	 */
	public void setColor(DyeColor color)
	{
		_color = color;
	}
	
	/**
	 * Sets the design of this banner pattern
	 * @param color The design to set this pattern to
	 */
	public void setPatternType(PatternType type)
	{
		_type = type;
	}
}