package mineplex.game.clans.clans.siege.outpost.build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;

/**
 * Special block for Mk III Outpost
 *
 */
public class OutpostBlockBanner extends OutpostBlock
{
	private DyeColor _baseColor;
	private List<Pattern> _patterns;
	
	public OutpostBlockBanner(Map<String, OutpostBlock> blocks, Location loc, int id, byte data, DyeColor color, Pattern... patterns)
	{
		super(blocks, loc, id, data);
		_baseColor = color;
		_patterns = new ArrayList<>(Arrays.asList(patterns));
	}
	
	@Override
	public void set() 
	{
		super.set();
		
		Banner banner = (Banner) getLocation().getBlock().getState();
		banner.setBaseColor(_baseColor);
		banner.setPatterns(_patterns);
		
		banner.update(true, false);
	}
}
