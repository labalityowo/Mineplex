package mineplex.game.clans.tutorial;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import mineplex.core.common.block.DataLocationMap;
import mineplex.core.common.block.schematic.Schematic;

public class TutorialRegion
{
	private final Schematic _schematic;
	private final Location _origin;
	private DataLocationMap _locationMap;

	protected TutorialRegion(Schematic schematic, Location origin)
	{
		_origin = origin;
		_schematic = schematic;

		pasteSchematic();
	}

	public Location getOrigin()
	{
		return _origin;
	}

	public DataLocationMap getLocationMap()
	{
		return _locationMap;
	}

	private void pasteSchematic()
	{
		_locationMap = _schematic.paste(getOrigin(), false).getDataLocationMap();
	}

	/**
	 * Clear the schematic area. This shouldn't be needed
	 */
	@Deprecated
	private void clearSchematic()
	{
		for (int x = 0; x < _schematic.getWidth(); x++)
		{
			for (int y = 0; y < _schematic.getHeight(); y++)
			{
				for (int z = 0; z < _schematic.getLength(); z++)
				{
					Block b = _origin.clone().add(x, y, z).getBlock();
					if (b.getType() != Material.AIR)
					{
						b.setTypeIdAndData(0, (byte) 0, false);
					}
				}
			}
		}
	}

	protected void reset()
	{
		long start = System.currentTimeMillis();
		System.out.println("TutorialRegion starting reset...");
		pasteSchematic();
		System.out.println("TutorialRegion finished reset! Took " + (System.currentTimeMillis() - start) + "ms");

	}

	@Override
	public String toString()
	{
		return "TutorialRegion[" + _origin.toString() + "]";
	}
}
