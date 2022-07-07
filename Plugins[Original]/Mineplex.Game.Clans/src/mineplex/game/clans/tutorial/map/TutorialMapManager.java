package mineplex.game.clans.tutorial.map;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.base.Objects;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multisets;

import mineplex.core.MiniPlugin;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.tutorial.TutorialManager;
import mineplex.game.clans.tutorial.tutorials.clans.ClansMainTutorial;

import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.MaterialMapColor;
import net.minecraft.server.v1_8_R3.PersistentCollection;

public class TutorialMapManager extends MiniPlugin
{
	private ClansMainTutorial _tutorial;
	private int _blocksScan = 16;
	private Comparator<Entry<Integer, Integer>> _comparator;
	private int _halfMapSize = 64;
	private int[][] _heightMap;
	private boolean _loadWorld = true;
	private HashMap<Integer, Byte[][]> _map = new HashMap<Integer, Byte[][]>();
	private short _mapId = (short) UtilMath.r(Short.MAX_VALUE);
	private ArrayList<Entry<Integer, Integer>> _scanList = new ArrayList<Entry<Integer, Integer>>();
	private World _world;
	private int _centerX;
	private int _centerZ;
	private int _scale = 1;
	private int _minX;
	private int _minZ;

	public TutorialMapManager(JavaPlugin plugin, ClansMainTutorial tutorial, World world, int minX, int minZ, int maxX, int maxZ)
	{
		super("TutorialMapManager", plugin);

		_centerX = minX + ((maxX - minX) / 2);
		_centerZ = minZ + ((maxZ - minZ) / 2);
		_centerX = (int) (Math.round(_centerX / 16D) * 16);
		_centerZ = (int) (Math.round(_centerZ / 16D) * 16);
		_tutorial = tutorial;
		_minX = minX;
		_minZ = minZ;

		_heightMap = new int[(_halfMapSize * 2) + 16][];

		_comparator = new Comparator<Entry<Integer, Integer>>()
		{

			@Override
			public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2)
			{
				// Render the places outside the map first to speed up visual errors fixing
				int outsideMap = Boolean.compare(o1.getValue() < -_halfMapSize, o2.getValue() < -_halfMapSize);

				if (outsideMap != 0)
				{
					return -outsideMap;
				}

				double dist1 = 0;
				double dist2 = 0;

				for (Player player : UtilServer.getPlayers())
				{
					dist1 += getDistance(o1, player.getLocation().getX(), player.getLocation().getZ());
					dist2 += getDistance(o2, player.getLocation().getX(), player.getLocation().getZ());
				}

				if (dist1 != dist2)
				{
					return Double.compare(dist1, dist2);
				}

				dist1 = getDistance(o1, 0, 0);
				dist2 = getDistance(o2, 0, 0);

				return Double.compare(dist1, dist2);

			}
		};

		for (int x = -_halfMapSize; x < _halfMapSize; x += _blocksScan)
		{
			for (int z = -_halfMapSize - 16; z < _halfMapSize; z += (z < -_halfMapSize ? 16 : _blocksScan))
			{
				_scanList.add(new HashMap.SimpleEntry(x, z));
			}
		}

		for (int s = 1; s <= 2; s++)
		{
			if (s == 2)
			{
				s = getScale();

				if (s == 1)
					break;
			}

			int size = (_halfMapSize * 2) / s;
			Byte[][] bytes = new Byte[size][];

			for (int i = 0; i < size; i++)
			{
				bytes[i] = new Byte[size];
			}

			_map.put(s, bytes);
		}

		for (int i = 0; i < _heightMap.length; i++)
		{
			_heightMap[i] = new int[_heightMap.length];
		}

		_world = world;

		try
		{
			File foundFile = null;

			for (File f : new File("world/data").listFiles())
			{
				if (f.getName().startsWith("map_"))
				{
					foundFile = f;
					break;
				}
			}

			if (foundFile == null)
			{
				PersistentCollection collection = ((CraftWorld) _world).getHandle().worldMaps;
				Field f = collection.getClass().getDeclaredField("d");
				f.setAccessible(true);
				((HashMap) f.get(collection)).put("map", (short) 0);
			}

			MapView view = Bukkit.createMap(_world);
			_mapId = view.getId();
			setupRenderer(view);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		rebuildScan();
	}

	private void setupRenderer(MapView view)
	{
		for (MapRenderer renderer : view.getRenderers())
		{
			view.removeRenderer(renderer);
		}

		view.addRenderer(new TutorialMapRenderer(_tutorial, _minX, _minZ));
	}

	public int getScale()
	{
		return _scale;
	}

	public int getX()
	{
		return _centerX;
	}

	public int getZ()
	{
		return _centerZ;
	}

	@EventHandler
	public void preventMapInItemFrame(PlayerInteractEntityEvent event)
	{
		if (!(event.getRightClicked() instanceof ItemFrame))
			return;

		ItemStack item = event.getPlayer().getItemInHand();

		if (item == null || item.getType() != Material.MAP || item.getDurability() < _mapId || item.getDurability() != _mapId)
			return;

		event.setCancelled(true);
	}

	/**
	 * Get the center of the map.
	 */
	public int calcMapCenter(int zoom, int cord)
	{
		int mapSize = _halfMapSize / zoom; // This is how large the map is in pixels

		int mapCord = cord / zoom; // This is pixels from true center of map, not held map

		int fDiff = mapSize - -mapCord;
		int sDiff = mapSize - mapCord;

		double chunkBlock = cord & 0xF;
		cord -= chunkBlock;
		chunkBlock /= zoom;

		/*if ((fDiff < 64 || sDiff < 64) && (Math.abs(fDiff - sDiff) > 1))
		{
			cord += (fDiff > sDiff ? Math.floor(chunkBlock) : Math.ceil(chunkBlock));
		}
		else*/
		{
			cord += (int) Math.floor(chunkBlock) * zoom;
		}

		while ((fDiff < 64 || sDiff < 64) && (Math.abs(fDiff - sDiff) > 1))
		{
			int change = (fDiff > sDiff ? -zoom : zoom);
			cord += change;

			mapCord = cord / zoom;

			fDiff = mapSize - -mapCord;
			sDiff = mapSize - mapCord;
		}

		return cord;
	}

	private void colorWorldHeight(int zoom, int startingX, int startingZ)
	{
		if (zoom == 0)
			zoom = 1;

		Byte[][] map = _map.get(zoom);

		for (int x = startingX; x < startingX + _blocksScan; x += zoom)
		{
			double d0 = 0;

			// Prevents ugly lines for the first line of Z

			for (int addX = 0; addX < zoom; addX++)
			{
				for (int addZ = 0; addZ < zoom; addZ++)
				{
					int hX = x + addX + _halfMapSize;
					int hZ = (startingZ - zoom) + addZ + _halfMapSize;

					if (hX >= _halfMapSize * 2 || hZ >= _halfMapSize * 2)
					{
						continue;
					}

					d0 += _heightMap[hX + 16][hZ + 16] / (zoom * zoom);
				}
			}

			for (int z = startingZ; z < startingZ + _blocksScan; z += zoom)
			{
				// Water depth colors not included
				double d1 = 0;

				for (int addX = 0; addX < zoom; addX++)
				{
					for (int addZ = 0; addZ < zoom; addZ++)
					{
						int hX = x + addX + _halfMapSize;
						int hZ = z + addZ + _halfMapSize;

						if (hX >= _halfMapSize * 2 || hZ >= _halfMapSize * 2)
						{
							continue;
						}

						d1 += _heightMap[hX + 16][hZ + 16] / (zoom * zoom);
					}
				}

				double d2 = (d1 - d0) * 4.0D / (zoom + 4) + ((x + z & 0x1) - 0.5D) * 0.4D;
				byte b0 = 1;

				d0 = d1;

				if (d2 > 0.6D)
				{
					b0 = 2;
				}
				else if (d2 > 1.2D)
				{
					b0 = 3;
				}
				else if (d2 < -0.6D)
				{
					b0 = 0;
				}

				int origColor = map[(x + _halfMapSize) / zoom][(z + _halfMapSize) / zoom] - 1;

				/*if (color < 4)
				{
					d2 = waterDepth * 0.1D + (k1 + j2 & 0x1) * 0.2D;
					b0 = 1;
					if (d2 < 0.5D)
					{
						b0 = 2;
					}

					if (d2 > 0.9D)
					{
						b0 = 0;
					}
				}*/

				byte color = (byte) (origColor + b0);
				map[(x + _halfMapSize) / zoom][(z + _halfMapSize) / zoom] = color;
			}
		}
	}

	private void drawWorldScale(int zoom, int startingX, int startingZ)
	{
		Byte[][] first = _map.get(1);
		Byte[][] second = _map.get(zoom);

		for (int x = startingX; x < startingX + _blocksScan; x += zoom)
		{
			for (int z = startingZ; z < startingZ + _blocksScan; z += zoom)
			{
				HashMultiset<Byte> hashmultiset = HashMultiset.create();

				for (int addX = 0; addX < zoom; addX++)
				{
					for (int addZ = 0; addZ < zoom; addZ++)
					{
						int pX = x + addX + _halfMapSize;
						int pZ = z + addZ + _halfMapSize;

						if (pX >= first.length || pZ >= first.length)
						{
							continue;
						}

						Byte b = first[pX][pZ];

						if (b == null)
							continue;

						hashmultiset.add(b);
					}
				}

				Byte color = Iterables.getFirst(Multisets.copyHighestCountFirst(hashmultiset), (byte) 0);

				second[(x + _halfMapSize) / zoom][(z + _halfMapSize) / zoom] = color;
			}
		}
	}

	@EventHandler
	public void dropItem(ItemSpawnEvent event)
	{
		ItemStack item = event.getEntity().getItemStack();

		if (item != null && item.getType() == Material.MAP && item.getDurability() == _mapId)
		{
			event.getEntity().remove();
		}
	}

	private double getDistance(double x1, double z1, double x2, double z2)
	{
		x1 = (x1 - x2);
		z1 = (z1 - z2);

		return (x1 * x1) + (z1 * z1);
	}

	private double getDistance(Entry<Integer, Integer> entry, double x1, double z1)
	{
		return getDistance(x1 + _centerX, z1 + _centerZ, entry.getKey() + (_blocksScan / 2),
				entry.getValue() + (_blocksScan / 2));
	}

	public Byte[][] getMap(int scale)
	{
		return _map.get(scale);
	}

	public int getMapSize()
	{
		return _halfMapSize;
	}

	@EventHandler
	public void preventMapMoveInventories(InventoryClickEvent event)
	{
		Inventory inv = event.getClickedInventory();

		if (inv == null)
			return;

		// Yeah, the loop looks a little weird..
		for (ItemStack item : new ItemStack[]
				{
						event.getCurrentItem(),
						event.getCursor()
				})
		{
			if (item == null || item.getType() != Material.MAP || item.getDurability() != _mapId)
				continue;

			if (inv.getHolder() instanceof Player ? !event.isShiftClick() : Objects.equal(event.getCurrentItem(), item))
				continue;

			event.setCancelled(true);

			UtilPlayer.message(event.getWhoClicked(),
					F.main("Inventory", "You cannot move " + F.item("Clans Map") + " between inventories."));
			return;
		}
	}

	private void rebuildScan()
	{
		for (int x = -_halfMapSize; x < _halfMapSize; x += _blocksScan)
		{
			for (int z = -_halfMapSize - 16; z < _halfMapSize; z += (z < -_halfMapSize ? 16 : _blocksScan))
			{
				_scanList.add(new HashMap.SimpleEntry(x, z));
			}
		}

		if (!_loadWorld)
		{
			Iterator<Entry<Integer, Integer>> itel = _scanList.iterator();

			while (itel.hasNext())
			{
				Entry<Integer, Integer> entry = itel.next();
				boolean removeEntry = true;

				for (Player player : UtilServer.getPlayers())
				{
					if (Math.sqrt(getDistance(entry, player.getLocation().getX(), player.getLocation().getZ())) < 200)
					{
						removeEntry = false;
						break;
					}
				}

				if (removeEntry)
				{
					itel.remove();
				}
			}
		}

		Collections.sort(_scanList, _comparator);
	}

	@EventHandler
	public void renderMap(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
			return;

		if (_scanList.isEmpty())
		{
			if (_loadWorld)
			{
				_loadWorld = false;
			}

			if (UtilServer.getPlayers().length == 0)
				return;

			rebuildScan();
		}
		else if (_scanList.size() % 20 == 0)
		{
			Collections.sort(_scanList, _comparator);
		}

		if (_scanList.isEmpty())
			return;

		Entry<Integer, Integer> entry = _scanList.remove(0);

		int startingX = entry.getKey();
		int startingZ = entry.getValue();

		boolean outsideMap = startingZ < -_halfMapSize;

		scanWorldMap(startingX, startingZ, !outsideMap);

		if (outsideMap)
		{
			return;
		}

		for (int s = 1; s <= 2; s++)
		{
			if (s == 2)
			{
				s = getScale();

				if (s == 1)
					break;
			}

			if (s == 13 && _loadWorld)
				continue;

			if (!outsideMap)
			{
				drawWorldScale(s, startingX, startingZ);
			}

			colorWorldHeight(s, startingX, startingZ);
		}

		colorWorldHeight(0, startingX, startingZ);
	}

	public void scanWorldMap(int startingX, int startingZ, boolean setColors)
	{
		Byte[][] map = _map.get(1);

		for (int beginX = startingX; beginX < startingX + _blocksScan; beginX += 16)
		{
			for (int beginZ = startingZ - (startingZ > -_halfMapSize ? 16 : 0); beginZ < startingZ
					+ (setColors ? _blocksScan : 16); beginZ += 16)
			{
				Chunk chunk = _world.getChunkAt((beginX + _centerX) / 16, (beginZ + _centerZ) / 16);
				boolean loaded = false;

				if (!chunk.isLoaded())
				{
					if (_loadWorld)
					{
						loaded = chunk.load();
					}
					else
					{
						continue;
					}
				}

				net.minecraft.server.v1_8_R3.Chunk nmsChunk = ((CraftChunk) chunk).getHandle();

				for (int x = beginX; x < beginX + 16; x++)
				{
					for (int z = beginZ; z < beginZ + 16; z++)
					{
						int color = 0;

						if (!nmsChunk.isEmpty())
						{
							int k3 = x & 0xF;
							int l3 = z & 0xF;

							int l4 = nmsChunk.b(k3, l3) + 1;
							IBlockData iblockdata = Blocks.AIR.getBlockData();

							if (l4 > 1)
							{
								do
								{
									l4--;
									iblockdata = nmsChunk.getBlockData(new BlockPosition(k3, l4, l3));
								}
								while (iblockdata.getBlock().g(iblockdata) == MaterialMapColor.b && (l4 > 0));

								if ((l4 > 0) && (iblockdata.getBlock().getMaterial().isLiquid()))
								{
									int j5 = l4 - 1;
									Block block1;
									do
									{
										block1 = nmsChunk.getType(new BlockPosition(k3, j5--, l3));
									}
									while ((j5 > 0) && (block1.getMaterial().isLiquid()));
								}
							}

							_heightMap[x + _halfMapSize + 16][z + _halfMapSize + 16] = l4;

							if (setColors)
							{
								// color = block.f(i5).M;
								IBlockData data = nmsChunk.getBlockData(new BlockPosition(k3, l4, l3));
								color = data.getBlock().g(data).M;

								color = (byte) ((color * 4) + 1);
							}
						}

						if (setColors && beginZ >= startingZ)
						{
							map[x + _halfMapSize][z + _halfMapSize] = (byte) color;
						}
					}

					if (loaded)
					{
						chunk.unload();
					}
				}
			}
		}
	}

	public void setMap(Player player)
	{
		for (ItemStack item : UtilInv.getItems(player))
		{
			if (item.getType() == Material.MAP && item.getDurability() == _mapId)
			{
				return;
			}
		}

		ItemStack item = new ItemBuilder(Material.MAP, 1, _mapId).setTitle("Clans Map").build();

		int slot = player.getInventory().firstEmpty();

		if (slot >= 0)
		{
			ItemStack mapSlot = player.getInventory().getItem(8);

			if (mapSlot == null || mapSlot.getType() == Material.AIR)
			{
				slot = 8;
			}

			player.getInventory().setItem(slot, item);
		}
	}

}
