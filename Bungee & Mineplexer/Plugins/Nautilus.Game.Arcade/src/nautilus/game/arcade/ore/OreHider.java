package nautilus.game.arcade.ore;

import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.explosion.ExplosionEvent;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;

public class OreHider 
{
	private NautHashMap<Location, Material> _hidden = new NautHashMap<Location, Material>();
	private boolean _visible = false;

	public void AddOre(Location loc, Material type) 
	{
		boolean visible = false;

		for (Block block : UtilBlock.getSurrounding(loc.getBlock(), false))
		{
			if (!block.getType().isOccluding()) 
			{
				visible = true;
				break;
			}
		}

		if (visible)
		{
			MapUtil.QuickChangeBlockAt(loc, type);
		}
		else
		{			
			_hidden.put(loc.getBlock().getLocation(), type);
		}
	}

	public void BlockBreak(BlockBreakEvent event)
	{
		for (Block block : UtilBlock.getSurrounding(event.getBlock(), false))
		{
			if (_hidden.containsKey(block.getLocation()))
			{
				MapUtil.QuickChangeBlockAt(block.getLocation(), _hidden.remove(block.getLocation()));
			}
		}
	}

	public void Explosion(ExplosionEvent event) 
	{
		for (Block cur : event.GetBlocks())
		{
			for (Block block : UtilBlock.getSurrounding(cur, false))
			{
				if (_hidden.containsKey(block.getLocation()))
				{
					block.setType(_hidden.remove(block.getLocation()));
				}
			}
		}
	}

	public void ToggleVisibility()
	{
		if (!_visible)
		{
			for (Location loc : _hidden.keySet())
			{
				loc.getBlock().setType(_hidden.get(loc));
			}
		}
		else
		{
			for (Location loc : _hidden.keySet())
			{
				loc.getBlock().setType(Material.STONE);
			}
		}

		_visible = !_visible;
	}

	public NautHashMap<Location, Material> GetHiddenOre() 
	{
		return _hidden;
	}
}
