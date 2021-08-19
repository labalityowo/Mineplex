package mineplex.core.common.block.schematic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import com.java.sk89q.jnbt.ByteArrayTag;
import com.java.sk89q.jnbt.CompoundTag;
import com.java.sk89q.jnbt.IntTag;
import com.java.sk89q.jnbt.ListTag;
import com.java.sk89q.jnbt.NBTInputStream;
import com.java.sk89q.jnbt.NBTOutputStream;
import com.java.sk89q.jnbt.NBTUtils;
import com.java.sk89q.jnbt.NamedTag;
import com.java.sk89q.jnbt.ShortTag;
import com.java.sk89q.jnbt.StringTag;
import com.java.sk89q.jnbt.Tag;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagInt;
import net.minecraft.server.v1_8_R3.TileEntity;
import net.minecraft.server.v1_8_R3.WorldServer;

public class UtilSchematic
{
	public static Schematic loadSchematic(File file) throws IOException
	{
		FileInputStream fis = new FileInputStream(file);
		return loadSchematic(fis);
	}

	public static Schematic loadSchematic(byte[] bytes) throws IOException
	{
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		return loadSchematic(bis);
	}

	public static Schematic loadSchematic(InputStream input) throws IOException
	{
		NBTInputStream nbtStream = new NBTInputStream(new GZIPInputStream(input));

		NamedTag rootTag = nbtStream.readNamedTag();
		nbtStream.close();

		if (!rootTag.getName().equals("Schematic"))
			return null;

		CompoundTag schematicTag = (CompoundTag) rootTag.getTag();
		Map<String, Tag> schematic = schematicTag.getValue();

		short width = getChildTag(schematic, "Width", ShortTag.class).getValue();
		short height = getChildTag(schematic, "Height", ShortTag.class).getValue();
		short length = getChildTag(schematic, "Length", ShortTag.class).getValue();


		byte[] blockId = getChildTag(schematic, "Blocks", ByteArrayTag.class).getValue();
		byte[] addId = new byte[0];
		short[] blocks = new short[blockId.length]; // Have to later combine IDs
		byte[] blockData = getChildTag(schematic, "Data", ByteArrayTag.class).getValue();

		Vector weOffset = null;
		if (schematic.containsKey("WEOffsetX") && schematic.containsKey("WEOffsetY") && schematic.containsKey("WEOffsetZ"))
		{
			int x = getChildTag(schematic, "WEOffsetX", IntTag.class).getValue();
			int y = getChildTag(schematic, "WEOffsetY", IntTag.class).getValue();
			int z = getChildTag(schematic, "WEOffsetZ", IntTag.class).getValue();
			weOffset = new Vector(x, y, z);
		}

		// We support 4096 block IDs using the same method as vanilla Minecraft, where
		// the highest 4 bits are stored in a separate byte array.
		if (schematic.containsKey("AddBlocks"))
		{
			addId = getChildTag(schematic, "AddBlocks", ByteArrayTag.class).getValue();
		}

		// Combine the AddBlocks data with the first 8-bit block ID
		for (int index = 0; index < blockId.length; index++)
		{
			if ((index >> 1) >= addId.length)
			{
				blocks[index] = (short) (blockId[index] & 0xFF);
			}
			else
			{
				if ((index & 1) == 0)
				{
					blocks[index] = (short) (((addId[index >> 1] & 0x0F) << 8) + (blockId[index] & 0xFF));
				}
				else
				{
					blocks[index] = (short) (((addId[index >> 1] & 0xF0) << 4) + (blockId[index] & 0xFF));
				}
			}
		}

		// Need to pull out tile entities
		List<Tag> tileEntities = getChildTag(schematic, "TileEntities", ListTag.class).getValue();
		Map<BlockVector, Map<String, Tag>> tileEntitiesMap = new HashMap<>();

		for (Tag tag : tileEntities) 
		{
			if (!(tag instanceof CompoundTag))
			{
				continue;
			}
			CompoundTag t = (CompoundTag) tag;

			int x = 0;
			int y = 0;
			int z = 0;

			Map<String, Tag> values = new HashMap<>();

			for (Map.Entry<String, Tag> entry : t.getValue().entrySet())
			{
				if (entry.getValue() instanceof IntTag)
				{
					if (entry.getKey().equals("x")) 
					{
						x = ((IntTag) entry.getValue()).getValue();
					}
					else if (entry.getKey().equals("y")) 
					{
						y = ((IntTag) entry.getValue()).getValue();
					}
					else if (entry.getKey().equals("z")) 
					{
						z = ((IntTag) entry.getValue()).getValue();
					}
				}

				values.put(entry.getKey(), entry.getValue());
			}

			BlockVector vec = new BlockVector(x, y, z);
			tileEntitiesMap.put(vec, values);
		}

		List<Tag> entityTags = getChildTag(schematic, "Entities", ListTag.class).getValue();

		return new Schematic(width, height, length, blocks, blockData, weOffset, tileEntitiesMap, entityTags);
	}

	/**
	 * @param schematic The schematic you want to turn into bytes
	 * @return Returns a byte array of the schematic which may be used for saving the schematic to DB or file
	 */
	public static byte[] getBytes(Schematic schematic)
	{
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		writeBytes(schematic, output);
		return output.toByteArray();
	}

	/**
	 * @param schematic The scheamtic you want save somewhere
	 * @return Writes out this schematic on byte form
	 */
	public static void writeBytes(Schematic schematic, OutputStream output)
	{
		Map<String, Tag> map = new HashMap<>();

		short width = schematic.getWidth();
		short height = schematic.getHeight();
		short length = schematic.getLength();

		map.put("Width", new ShortTag(width));
		map.put("Height", new ShortTag(height));
		map.put("Length", new ShortTag(length));

		if (schematic.hasWorldEditOffset())
		{
			Vector weOffset = schematic.getWorldEditOffset();
			map.put("WEOffsetX", new IntTag(weOffset.getBlockX()));
			map.put("WEOffsetY", new IntTag(weOffset.getBlockX()));
			map.put("WEOffsetZ", new IntTag(weOffset.getBlockX()));
		}

		map.put("Materials", new StringTag("Alpha"));

		short[] sBlocks = schematic.getBlocks();
		Map<BlockVector, Map<String, Tag>> sTileEntities = schematic.getTileEntities();

		byte[] blocks = new byte[sBlocks.length];
		byte[] addBlocks = null;
		byte[] blockData = schematic.getBlockData();
		List<Tag> tileEntities = new ArrayList<>();

		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				for (int z = 0; z < length; z++)
				{
					int index = y * width * length + z * width + x;
					BlockVector bv = new BlockVector(x, y, z);

					//Save 4096 IDs in an AddBlocks section
					if (sBlocks[index] > 255)
					{
						if (addBlocks == null) // Lazily create section
						{
							addBlocks = new byte[(blocks.length >> 1) + 1];
						}

						addBlocks[index >> 1] = (byte) (((index & 1) == 0) ?
								addBlocks[index >> 1] & 0xF0 | (sBlocks[index] >> 8) & 0xF
								: addBlocks[index >> 1] & 0xF | ((sBlocks[index] >> 8) & 0xF) << 4);  
					}

					blocks[index] = (byte) sBlocks[index];

					if (sTileEntities.get(bv) != null)
					{
						Map<String, Tag> values = new HashMap<>(sTileEntities.get(bv));
						values.put("x", new IntTag(x));
						values.put("y", new IntTag(y));
						values.put("z", new IntTag(z));

						CompoundTag tileEntityTag = new CompoundTag(values);
						tileEntities.add(tileEntityTag);
					}
				}
			}
		}

		map.put("Blocks", new ByteArrayTag(blocks));
		map.put("Data", new ByteArrayTag(blockData));
		map.put("TileEntities", new ListTag(CompoundTag.class, tileEntities));

		if (addBlocks != null)
		{
			map.put("AddBlocks", new ByteArrayTag(addBlocks));
		}

		// ====================================================================
		// Entities
		// ====================================================================

		List<Tag> entities = schematic.getEntities();

		map.put("Entities", new ListTag(CompoundTag.class, entities));

		// ====================================================================
		// Output
		// ====================================================================

		CompoundTag schematicTag = new CompoundTag(map);

		try (NBTOutputStream outputStream = new NBTOutputStream(new GZIPOutputStream(output)))
		{
			outputStream.writeNamedTag("Schematic", schematicTag);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static Schematic createSchematic(Location locA, Location locB)
	{
		return createSchematic(locA, locB, null);
	}

	public static Schematic createSchematic(Location locA, Location locB, Vector worldEditOffset)
	{
		World world = locA.getWorld();

		Vector min = Vector.getMinimum(locA.toVector(), locB.toVector());
		Vector max = Vector.getMaximum(locB.toVector(), locA.toVector());

		short width = (short) (max.getBlockX()-min.getBlockX());
		short height = (short) (max.getBlockY()-min.getBlockY());
		short length = (short) (max.getBlockZ()-min.getBlockZ());

		short[] blocks = new short[width*height*length];
		byte[] blocksData = new byte[blocks.length];

		WorldServer nmsWorld = ((CraftWorld)world).getHandle();

		Map<BlockVector, Map<String, Tag>> tileEntities = new HashMap<>();

		for(int x = min.getBlockX(); x < max.getBlockX(); x++)
		{
			for(int y = min.getBlockY(); y < max.getBlockY(); y++)
			{
				for(int z = min.getBlockZ(); z < max.getBlockZ(); z++)
				{
					int localX = x-min.getBlockX();
					int localY = y-min.getBlockY();
					int localZ = z-min.getBlockZ();

					Block b = world.getBlockAt(x, y, z);

					int index = localY * width * length + localZ * width + localX;

					blocks[index] = (short) b.getTypeId();
					blocksData[index] = b.getData();

					BlockPosition bp = new BlockPosition(x, y, z);

					TileEntity tileEntity = nmsWorld.getTileEntity(bp);
					if(tileEntity == null) continue;

					NBTTagCompound nmsTag = new NBTTagCompound();
					tileEntity.b(nmsTag);

					nmsTag.set("x", new NBTTagInt(localX));
					nmsTag.set("y", new NBTTagInt(localY));
					nmsTag.set("z", new NBTTagInt(localZ));

					CompoundTag tag = NBTUtils.fromNative(nmsTag);

					tileEntities.put(new BlockVector(localX, localY, localZ), tag.getValue());
				}
			}
		}

		List<Tag> entities = new ArrayList<>();
		for (Entity e : world.getEntities())
		{
			if (e instanceof Player) continue;

			if (e.getLocation().toVector().isInAABB(min, max))
			{
				net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity)e).getHandle();

				NBTTagCompound nmsTag = new NBTTagCompound();

				nmsEntity.c(nmsTag);

				Vector diff = e.getLocation().subtract(min).toVector();
				setPosition(nmsTag, diff);

				nmsTag.remove("UUID");
				nmsTag.remove("UUIDMost");
				nmsTag.remove("UUIDLeast");

				CompoundTag tag = NBTUtils.fromNative(nmsTag);
				entities.add(tag);
			}
		}

		return new Schematic(width, height, length, blocks, blocksData, worldEditOffset, tileEntities, entities);
	}

	private static <T extends Tag> T getChildTag(Map<String, Tag> items, String key, Class<T> expected)
	{
		Tag tag = items.get(key);
		return expected.cast(tag);
	}

	public static void setPosition(NBTTagCompound nbtTag, Vector pos)
	{
		nbtTag.set("Pos", NBTUtils.doubleArrayToList(pos.getX(), pos.getY(), pos.getZ()));
	}

	public static void main(String[] args) throws IOException
	{
		File file = new File("test.schematic");
		System.out.println(file.getAbsoluteFile());
		Schematic m = UtilSchematic.loadSchematic(file);
		System.out.println(m);
	}
}