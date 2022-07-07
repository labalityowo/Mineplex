package mineplex.core.common.block.schematic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityTypes;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagInt;
import net.minecraft.server.v1_8_R3.TileEntity;
import net.minecraft.server.v1_8_R3.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import com.java.sk89q.jnbt.CompoundTag;
import com.java.sk89q.jnbt.DoubleTag;
import com.java.sk89q.jnbt.NBTUtils;
import com.java.sk89q.jnbt.Tag;

import mineplex.core.common.block.DataLocationMap;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilBlock;

public class Schematic
{
	private final short _width;
	private final short _height;
	private final short _length;
	private final short[] _blocks;
	private final byte[] _blockData;
	private final Vector _weOffset;
	private final Map<BlockVector, Map<String, Tag>> _tileEntities;
	private final List<Tag> _entities;

	public Schematic(short width, short height, short length, short[] blocks, byte[] blockData, Vector worldEditOffset, Map<BlockVector, Map<String, Tag>> tileEntities, List<Tag> entities)
	{
		_width = width;
		_height = height;
		_length = length;
		_blocks = blocks;
		_blockData = blockData;
		_weOffset = worldEditOffset;
		_tileEntities = tileEntities;
		_entities = entities;
	}

	public Schematic(short width, short height, short length, short[] blocks, byte[] blockData, Vector worldEditOffset, Map<BlockVector, Map<String, Tag>> tileEntities)
	{
		this(width, height, length, blocks, blockData, worldEditOffset, tileEntities, new ArrayList<>());
	}

	public Schematic(short width, short height, short length, short[] blocks, byte[] blockData, Vector worldEditOffset)
	{
		this(width, height, length, blocks, blockData, worldEditOffset, new HashMap<>());
	}

	public Schematic(short width, short height, short length, short[] blocks, byte[] blockData)
	{
		this(width, height, length, blocks, blockData, null);
	}

	public Schematic(Schematic schematic)
	{
		this(schematic.getWidth(), schematic.getHeight(), schematic.getLength(), schematic.getBlocks(), schematic.getBlockData(), schematic.getWorldEditOffset(), schematic.getTileEntities(), schematic.getEntities());
	}

	public SchematicData paste(Location originLocation)
	{
		return paste(originLocation, false);
	}

	public SchematicData paste(Location originLocation, boolean ignoreAir)
	{
		return paste(originLocation, ignoreAir, false);
	}

	public SchematicData paste(Location originLocation, boolean ignoreAir, boolean worldEditOffset)
	{
		return paste(originLocation, ignoreAir, worldEditOffset, true);
	}

	public SchematicData paste(Location originLocation, boolean ignoreAir, boolean worldEditOffset, boolean quickSet)
	{
		if (worldEditOffset && hasWorldEditOffset())
		{
			originLocation = originLocation.clone().add(_weOffset);
		}
		DataLocationMap locationMap = new DataLocationMap();

		SchematicData output = new SchematicData(locationMap, originLocation.getWorld());

		int startX = originLocation.getBlockX();
		int startY = originLocation.getBlockY();
		int startZ = originLocation.getBlockZ();

		UtilBlock.startQuickRecording();

		WorldServer nmsWorld = ((CraftWorld) originLocation.getWorld()).getHandle();

		for (int x = 0; x < _width; x++)
		{
			for (int y = 0; y < _height; y++)
			{
				for (int z = 0; z < _length; z++)
				{
					int index = getIndex(x, y, z);
					// some blocks were giving me negative id's in the schematic (like stairs)
					// not sure why but the math.abs is my simple fix
					int materialId = Math.abs(_blocks[index]);

					if (ignoreAir && materialId == 0) // Air
					{
						continue;
					}
					else if (materialId == 147) // Gold Plate
					{
						// Check for data wool at location below the gold plate
						if (addDataWool(locationMap, true, originLocation, x, y - 1, z))
							continue;
					}
					else if (materialId == 148) // Iron Plate
					{
						// Check for data wool at location below the gold plate
						if (addDataWool(locationMap, false, originLocation, x, y - 1, z))
							continue;
					}
					else if (materialId == Material.SPONGE.getId())
					{
						if (addSpongeLocation(locationMap, originLocation, x, y + 1, z))
							continue;
					}
					else if (materialId == 35)
					{
						// Check if this is a dataloc so we can skip setting the block
						int aboveIndex = getIndex(x, y + 1, z);
						if (hasIndex(aboveIndex))
						{
							if (Math.abs(_blocks[aboveIndex]) == Material.GOLD_PLATE.getId() || Math.abs(_blocks[aboveIndex]) == Material.IRON_PLATE.getId())
								continue;
						}
						int belowIndex = getIndex(x, y - 1, z);
						if (hasIndex(belowIndex))
						{
							if (Math.abs(_blocks[belowIndex]) == Material.SPONGE.getId())
								continue;
						}
					}

					if (quickSet)
					{
						UtilBlock.setQuick(originLocation.getWorld(), startX + x, startY + y, startZ + z, materialId, _blockData[index]);
					}
					else
					{
						originLocation.getWorld().getBlockAt(startX + x, startY + y, startZ + z).setTypeIdAndData(materialId, _blockData[index], false);
					}

					BlockVector bv = new BlockVector(x, y, z);

					output.getBlocksRaw().add(bv);

					Map<String, Tag> map = _tileEntities.get(bv);

					if (map != null)
					{
						TileEntity te = nmsWorld.getTileEntity(MapUtil.getBlockPos(bv.add(originLocation.toVector())));
						if (te == null) continue;
						CompoundTag weTag = new CompoundTag(map);
						NBTTagCompound tag = NBTUtils.toNative(weTag);

						tag.set("x", new NBTTagInt(tag.getInt("x") + startX));
						tag.set("y", new NBTTagInt(tag.getInt("y") + startY));
						tag.set("z", new NBTTagInt(tag.getInt("z") + startZ));

						te.a(tag);

						output.getTileEntitiesRaw().add(bv);
					}
				}
			}
		}

		UtilBlock.stopQuickRecording();

		for (Tag tag : _entities)
		{
			if (tag instanceof CompoundTag)
			{
				CompoundTag ctag = (CompoundTag) tag;
				NBTTagCompound nmsTag = NBTUtils.toNative(ctag);

				List<DoubleTag> list = ctag.getList("Pos", DoubleTag.class);

				Vector pos = new Vector(list.get(0).getValue(), list.get(1).getValue(), list.get(2).getValue());

				pos.add(originLocation.toVector());

				UtilSchematic.setPosition(nmsTag, pos);

				list = NBTUtils.fromNative(nmsTag).getList("Pos", DoubleTag.class);

				Entity nmsEntity = EntityTypes.a(nmsTag, nmsWorld);
				nmsWorld.addEntity(nmsEntity, SpawnReason.CUSTOM);

				if (nmsEntity == null) continue;

				output.getEntitiesRaw().add(nmsEntity.getBukkitEntity());
			}
		}

		return output;
	}

	/**
	 * Checks the schematic location for x, y, z and adds the a Location to the DataLocationMap if it is a wool block
	 *
	 * @return true if a location was added to the DataLocationMap
	 */
	private boolean addDataWool(DataLocationMap map, boolean gold, Location origin, int x, int y, int z)
	{
		int index = getIndex(x, y, z);
		if (hasIndex(index))
		{
			int materialId = Math.abs(_blocks[index]);
			if (materialId == 35) // WOOL
			{
				byte data = _blockData[index];
				DyeColor color = DyeColor.getByWoolData(data);
				if (color != null)
				{
					if (gold)
					{
						map.addGoldLocation(color, origin.clone().add(x, y, z));
					}
					else
					{
						map.addIronLocation(color, origin.clone().add(x, y, z));
					}
					return true;
				}
			}
		}
		return false;
	}

	private boolean addSpongeLocation(DataLocationMap map, Location origin, int x, int y, int z)
	{
		int index = getIndex(x, y, z);
		if (hasIndex(index))
		{
			int materialId = Math.abs(_blocks[index]);
			if (materialId == 35) // WOOL
			{
				byte data = _blockData[index];
				DyeColor color = DyeColor.getByWoolData(data);
				if (color != null)
				{
					map.addSpongeLocation(color, origin.clone().add(x, y - 1, z));
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Rotates the schematic 180 degrees.
	 */
	public Schematic rotate180()
	{
		// Swap blocks around
		int area = _length * _width;
		Bukkit.broadcastMessage("length=" + _length + " width=" + _width + " height=" + _height);

		for (int height = 0; height < _height; height++)
		{
			int startIndex = height * area;
			int endIndex = (int) (startIndex + area / 2D);
			Bukkit.broadcastMessage("startIndex=" + startIndex + " endIndex=" + endIndex);

			for (int lower = startIndex; lower <= endIndex; lower++)
			{
				int upper = endIndex - lower;

				short temp = _blocks[lower];
				byte tempData = _blockData[lower];

				_blocks[lower] = _blocks[upper];
				_blocks[upper] = temp;

				_blockData[lower] = _blockData[upper];
				_blockData[upper] = tempData;
			}
		}

		// Inverse Tile Entities BlockVectors in the X and Z axis
		_tileEntities.keySet().forEach(blockVector ->
		{
			blockVector.setX(-blockVector.getX());
			blockVector.setZ(-blockVector.getZ());
		});

		return this;
	}

	public boolean hasWorldEditOffset()
	{
		return _weOffset != null;
	}

	public Vector getWorldEditOffset()
	{
		if (!hasWorldEditOffset()) return null;

		return _weOffset.clone();
	}

	public int getSize()
	{
		return _blocks.length;
	}

	public int getIndex(int x, int y, int z)
	{
		return y * _width * _length + z * _width + x;
	}

	public boolean hasIndex(int index)
	{
		return index < _blocks.length && index >= 0;
	}

	public Short getBlock(int x, int y, int z)
	{
		if (getIndex(x, y, z) >= _blocks.length)
		{
			return null;
		}
		if (getIndex(x, y, z) < 0)
		{
			return null;
		}
		return _blocks[getIndex(x, y, z)];
	}

	public Byte getData(int x, int y, int z)
	{
		if (getIndex(x, y, z) >= _blocks.length)
		{
			return null;
		}
		if (getIndex(x, y, z) < 0)
		{
			return null;
		}
		return _blockData[getIndex(x, y, z)];
	}

	public short getWidth()
	{
		return _width;
	}

	public short getHeight()
	{
		return _height;
	}

	public short getLength()
	{
		return _length;
	}

	public short[] getBlocks()
	{
		return _blocks;
	}

	public byte[] getBlockData()
	{
		return _blockData;
	}

	public List<Tag> getEntities()
	{
		return _entities;
	}

	public Map<BlockVector, Map<String, Tag>> getTileEntities()
	{
		return _tileEntities;
	}

	@Override
	public String toString()
	{
		return String.format("Schematic [width: %d, length: %d, height: %d, blockLength: %d, blockDataLength: %d]", _width, _length, _height, _blocks.length, _blockData.length);
	}
}
