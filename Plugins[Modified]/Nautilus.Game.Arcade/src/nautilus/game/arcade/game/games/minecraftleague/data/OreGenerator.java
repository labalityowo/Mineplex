package nautilus.game.arcade.game.games.minecraftleague.data;

import java.util.ArrayList;
import java.util.List;

import mineplex.core.common.util.UtilMath;

import org.bukkit.Location;
import org.bukkit.Material;

public class OreGenerator
{
	private List<Location> _choices = new ArrayList<Location>();
	
	public void generateOre(Material oreType, List<Location> possible, int amount)
	{
		_choices.clear();
		for (Location loc : possible)
		{
			if (loc.getBlock().getType() == Material.WORKBENCH || loc.getBlock().getType() == Material.CHEST || loc.getBlock().getType() == Material.TRAPPED_CHEST || loc.getBlock().getType() == Material.FURNACE || loc.getBlock().getType() == Material.BURNING_FURNACE)
				continue;
			loc.getBlock().setType(Material.STONE);
			_choices.add(loc);
		}
		for (int i = 0; i < (amount + 1); i++)
		{
			if (_choices.size() == 0)
				continue;
			Location selected = _choices.remove(UtilMath.random.nextInt(_choices.size()));
			selected.getBlock().setType(oreType);
		}
	}
}
