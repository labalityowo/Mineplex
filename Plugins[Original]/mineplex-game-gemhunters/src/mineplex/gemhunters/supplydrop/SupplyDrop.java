package mineplex.gemhunters.supplydrop;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import mineplex.core.common.util.*;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;

import mineplex.core.common.block.schematic.Schematic;
import mineplex.core.common.block.schematic.SchematicData;
import mineplex.core.common.block.schematic.UtilSchematic;

/**
 * Represents an instance of a Supply Drop. <br>
 * A supply drop consists of a helicopter flying through the map from a one
 * location to another. Upon reaching it's destination it will drop a loot chest
 * which players can then fight over. <br>
 * The helicopter will then fly away towards a despawning location. <br>
 * <br>
 * The helicopter will be made up of a collection of blocks that are moved along
 * a linear path. The look of this helicopter is saved in the map and is stored
 * in internal memory on startup. <br>
 * <br>
 * The blades of the helicopter rotate, this is done within this class. <br>
 * <br>
 * {@link SupplyDropModule} handles when and where these supply drops will
 * spawn.
 */
public class SupplyDrop
{

	private static final String SCHEMATIC_PATH = ".." + File.separator + ".." + File.separator + "update" + File.separator + "files" + File.separator + "Helicopter.schematic";
	private static final int BLADE_LENGTH = 7;

	private final String _name;
	private Location _destination;
	private Location _despawn;
	private Location _current;
	private Location _blade;

	private final Set<Block> _lastHelicopter;
	private final Set<Block> _bladeBlocks;
	private Schematic _schematic;
	private boolean _diagonal;

	public SupplyDrop(String name, Location spawn, Location destination, Location despawn)
	{
		_name = name;
		_destination = destination.clone();
		_despawn = despawn.clone();
		_current = spawn.clone().add(-2, 0, 0);
		_lastHelicopter = new HashSet<>(100);
		_bladeBlocks = new HashSet<>(20);

		try
		{
			_schematic = UtilSchematic.loadSchematic(new File(SCHEMATIC_PATH));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public boolean advancePath()
	{
		boolean done = moveHelicopter();

		if (!done)
		{
			rotateBlades();
		}

		_current.add(0, 0, 1);

		return done;
	}

	public boolean moveHelicopter()
	{
		_current.getChunk().load();
		
		for (Block block : _lastHelicopter)
		{
			block.setType(Material.AIR);
		}

		_lastHelicopter.clear();

		if (_blade != null)
		{
			if (UtilMath.offset2dSquared(_blade, _despawn) < 4)
			{
				for (Block block : _bladeBlocks)
				{
					block.setType(Material.AIR);
				}

				return true;
			}
		}

		SchematicData data = _schematic.paste(_current, true);

		_blade = data.getDataLocationMap().getIronLocations(DyeColor.RED).get(0);

		for (BlockVector vector : data.getBlocks())
		{
			Location location = _current.add(vector);

			_lastHelicopter.add(location.getBlock());

			_current.subtract(vector);
		}

		return false;
	}

	public void rotateBlades()
	{
		_diagonal = !_diagonal;

		for (Block block : _bladeBlocks)
		{
			block.setType(Material.AIR);
		}

		_bladeBlocks.clear();

		if (_diagonal)
		{
			for (int x = -1; x <= 1; x += 2)
			{
				for (int z = -1; z <= 1; z += 2)
				{
					for (Location location : UtilShapes.getLinesLimitedPoints(_blade, _blade.clone().add(x * BLADE_LENGTH, 0, z * BLADE_LENGTH), BLADE_LENGTH))
					{
						Block block = location.getBlock();

						_bladeBlocks.add(block);
						block.setType(Material.STEP);
					}
				}
			}
		}
		else
		{
			for (int x = -1; x <= 1; x += 2)
			{
				for (Location location : UtilShapes.getLinesLimitedPoints(_blade, _blade.clone().add(x * BLADE_LENGTH, 0, 0), BLADE_LENGTH))
				{
					Block block = location.getBlock();

					_bladeBlocks.add(block);
					block.setType(Material.STEP);
				}
			}

			for (int z = -1; z <= 1; z += 2)
			{
				for (Location location : UtilShapes.getLinesLimitedPoints(_blade, _blade.clone().add(0, 0, z * BLADE_LENGTH), BLADE_LENGTH))
				{
					Block block = location.getBlock();

					_bladeBlocks.add(block);
					block.setType(Material.STEP);
				}
			}
		}
	}
	
	public void stop()
	{
		for (Block block : _bladeBlocks)
		{
			block.setType(Material.AIR);
		}
		
		for (Block block : _lastHelicopter)
		{
			block.setType(Material.AIR);
		}
	}
	
	public final String getName()
	{
		return _name;
	}
	
	public final Location getCurrentLocation()
	{
		return _current;
	}
	
	public final Location getChestLocation()
	{
		return _destination;
	}

	public final Location getBladeLocation() { return _blade; }
}
