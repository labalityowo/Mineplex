package nautilus.game.arcade.game.modules.worldmap;

import java.io.File;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.MaterialMapColor;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multisets;

import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.modules.Module;
import nautilus.game.arcade.world.WorldData;

public class WorldMapModule extends Module
{

	public enum Perm implements Permission
	{
		MAP_COMMAND
	}

	private static final int BLOCK_SCAN_INTERVAL = 16;

	private int _halfMapSize;
	private final int[][] _heightMap;
	private boolean _loadWorld = true;
	private Map<Integer, Byte[][]> _map = new HashMap<>();
	private short _mapId = (short) UtilMath.r(Short.MAX_VALUE);
	private final List<Entry<Integer, Integer>> _scanList = new ArrayList<>();
	private World _world;
	private int _centerX, _centerZ, _scale;

	private final WorldMapRenderer _renderer;

	public WorldMapModule(WorldData worldData, WorldMapRenderer renderer)
	{
		this(worldData.World, worldData.MinX, worldData.MinZ, worldData.MaxX, worldData.MaxZ, renderer);
	}

	public WorldMapModule(World world, int minX, int minZ, int maxX, int maxZ, WorldMapRenderer renderer)
	{
		_centerX = minX + ((maxX - minX) / 2);
		_centerZ = minZ + ((maxZ - minZ) / 2);
		_centerX = (int) (Math.round(_centerX / 16D) * 16);
		_centerZ = (int) (Math.round(_centerZ / 16D) * 16);
		_renderer = renderer;
		renderer.setManager(this);

		_halfMapSize = (int) (Math.ceil(Math.max((maxX - minX) / 2D, (maxZ - minZ) / 2D) / 16D) * 16);

		List<Entry<Integer, Integer>> list = new ArrayList<>();

		for (int scale = 1; scale <= 16; scale++)
		{
			int s = _halfMapSize;

			if ((s / scale) > 127)
			{
				continue;
			}

			while (s < 10000 && (s % 16 != 0 || s % scale != 0))
			{
				s += 16;
			}

			if (s < 10000)
			{
				list.add(new SimpleEntry<>(scale, s));
			}
		}

		if (list.isEmpty())
		{
			_scale = 16;
			_halfMapSize = 127 * 8;
		}
		else
		{
			list.sort(Comparator.comparingInt(Entry::getValue));

			_scale = list.get(0).getKey();
			_halfMapSize = list.get(0).getValue();
		}

		_heightMap = new int[(_halfMapSize * 2) + 16][];

		for (int x = -_halfMapSize; x < _halfMapSize; x += BLOCK_SCAN_INTERVAL)
		{
			for (int z = -_halfMapSize - 16; z < _halfMapSize; z += (z < -_halfMapSize ? 16 : BLOCK_SCAN_INTERVAL))
			{
				_scanList.add(new SimpleEntry<>(x, z));
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
			File dataFolder = new File("world/data");

			if (dataFolder.exists())
			{
				for (File file : dataFolder.listFiles())
				{
					if (file.getName().startsWith("map_"))
					{
						file.delete();
					}
				}
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

	@Override
	protected void setup()
	{
		getGame().registerDebugCommand("map", Perm.MAP_COMMAND, PermissionGroup.PLAYER, (player, args) ->
		{
			if (UtilPlayer.isSpectator(player))
			{
				return;
			}

			Inventory inventory = player.getInventory();
			inventory.remove(Material.MAP);
			inventory.addItem(getMapItem());
			player.sendMessage(F.main("Game", "Here, have a " + F.name("Map") + "."));
		});
	}

	private void setupRenderer(MapView view)
	{
		for (MapRenderer renderer : view.getRenderers())
		{
			view.removeRenderer(renderer);
		}

		view.addRenderer(_renderer);
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

	private void colorWorldHeight(int zoom, int startingX, int startingZ)
	{
		zoom = Math.max(zoom, 1);

		Byte[][] map = _map.get(zoom);

		for (int x = startingX; x < startingX + BLOCK_SCAN_INTERVAL; x += zoom)
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

			for (int z = startingZ; z < startingZ + BLOCK_SCAN_INTERVAL; z += zoom)
			{
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
				byte color = (byte) (origColor + b0);
				map[(x + _halfMapSize) / zoom][(z + _halfMapSize) / zoom] = color;
			}
		}
	}

	private void drawWorldScale(int zoom, int startingX, int startingZ)
	{
		Byte[][] first = _map.get(1);
		Byte[][] second = _map.get(zoom);

		for (int x = startingX; x < startingX + BLOCK_SCAN_INTERVAL; x += zoom)
		{
			for (int z = startingZ; z < startingZ + BLOCK_SCAN_INTERVAL; z += zoom)
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
						{
							continue;
						}

						hashmultiset.add(b);
					}
				}

				Byte color = Iterables.getFirst(Multisets.copyHighestCountFirst(hashmultiset), (byte) 0);

				second[(x + _halfMapSize) / zoom][(z + _halfMapSize) / zoom] = color;
			}
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
		return getDistance(x1 + _centerX, z1 + _centerZ, entry.getKey() + (BLOCK_SCAN_INTERVAL / 2), entry.getValue() + (BLOCK_SCAN_INTERVAL / 2));
	}

	Byte[][] getMap(int scale)
	{
		return _map.get(scale);
	}

	private void rebuildScan()
	{
		for (int x = -_halfMapSize; x < _halfMapSize; x += BLOCK_SCAN_INTERVAL)
		{
			for (int z = -_halfMapSize - 16; z < _halfMapSize; z += (z < -_halfMapSize ? 16 : BLOCK_SCAN_INTERVAL))
			{
				_scanList.add(new SimpleEntry<>(x, z));
			}
		}

		if (!_loadWorld)
		{
			_scanList.removeIf(entry ->
			{
				for (Player player : UtilServer.getPlayers())
				{
					Location location = player.getLocation();

					if (getDistance(entry, location.getX(), location.getZ()) < 6400)
					{
						return false;
					}
				}

				return true;
			});
		}
	}

	@EventHandler
	public void renderMap(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		_renderer.renderTick();

		if (_scanList.isEmpty())
		{
			if (_loadWorld)
			{
				_loadWorld = false;
				System.out.println("Finished rendering the map.");
			}

			if (UtilServer.getPlayersCollection().size() == 0)
			{
				return;
			}

			rebuildScan();
			return;
		}

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
				{
					break;
				}
			}

			if (s == 13 && _loadWorld)
			{
				continue;
			}

			drawWorldScale(s, startingX, startingZ);
			colorWorldHeight(s, startingX, startingZ);
		}

		colorWorldHeight(0, startingX, startingZ);
	}

	public void scanWorldMap(int startingX, int startingZ, boolean setColors)
	{
		Byte[][] map = _map.get(1);

		for (int beginX = startingX; beginX < startingX + BLOCK_SCAN_INTERVAL; beginX += 16)
		{
			for (int beginZ = startingZ - (startingZ > -_halfMapSize ? 16 : 0); beginZ < startingZ + (setColors ? BLOCK_SCAN_INTERVAL : 16); beginZ += 16)
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
							IBlockData iblockdata;

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

	@EventHandler
	public void inventoryClick(InventoryClickEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		UtilInv.DisallowMovementOf(event, null, Material.MAP, (byte) _mapId, false);

		if (event.isCancelled())
		{
			event.getWhoClicked().sendMessage(F.main("Game", "You cannot move this map from your inventory."));
		}
	}

	public ItemStack getMapItem()
	{
		return new ItemBuilder(Material.MAP, 1, _mapId)
				.setTitle(C.cGreenB + "World Map")
				.build();
	}
}