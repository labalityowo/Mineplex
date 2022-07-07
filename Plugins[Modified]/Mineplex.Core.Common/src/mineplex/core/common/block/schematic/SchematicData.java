package mineplex.core.common.block.schematic;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.util.BlockVector;

import mineplex.core.common.block.DataLocationMap;

/**
 * Wrapper class for holding the output from pasting a schematic
 */
public class SchematicData
{
	
	private final List<BlockVector> _blocks;
	private final DataLocationMap _dataMap;
	private final List<Entity> _entities;
	private final List<BlockVector> _tileEntities;
	private final World _world;

	public SchematicData(DataLocationMap dataMap, World world)
	{
		_dataMap = dataMap;
		_blocks = new ArrayList<>();
		_tileEntities = new ArrayList<>();
		_entities = new ArrayList<>();
		_world = world;
	}
	
	/**
	 * @return Returns a list of blocks which has been edited by the schematic
	 */
	public List<BlockVector> getBlocks()
	{
		return new ArrayList<>(_blocks);
	}
	
	/**
	 * @return Returns the DataLocationMap which was utilized while pasting
	 */
	public DataLocationMap getDataLocationMap()
	{
		return _dataMap;
	}
	
	/**
	 * @return Returns a entities which was spawned by the schematic
	 */
	public List<Entity> getEntities()
	{
		return new ArrayList<>(_entities);
	}
	
	/**
	 * @return Returns a list of blocks who are tile entities which have been edited by the schematic. All the blocks in this list is also
	 * inside the {@link #getBlocks()} method
	 */
	public List<BlockVector> getTileEntities()
	{
		return new ArrayList<>(_tileEntities);
	}
	
	public World getWorld()
	{
		return _world;
	}
	
	List<BlockVector> getBlocksRaw()
	{
		return _blocks;
	}
	
	List<BlockVector> getTileEntitiesRaw()
	{
		return _tileEntities;
	}
	
	List<Entity> getEntitiesRaw()
	{
		return _entities;
	}

}
