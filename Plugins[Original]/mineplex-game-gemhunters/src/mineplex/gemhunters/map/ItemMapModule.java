package mineplex.gemhunters.map;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.ChunkProviderServer;
import net.minecraft.server.v1_8_R3.ChunkRegionLoader;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.MaterialMapColor;
import net.minecraft.server.v1_8_R3.PersistentCollection;
import net.minecraft.server.v1_8_R3.WorldServer;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.util.LongHash;
import org.bukkit.craftbukkit.v1_8_R3.util.LongObjectHashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multisets;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.portal.events.ServerTransferEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.gemhunters.death.event.PlayerCustomRespawnEvent;
import mineplex.gemhunters.map.command.MapCommand;

/**
 * <b>All item map code was adapted from Clans.</b><br>
 */
@ReflectivelyCreateMiniPlugin
public class ItemMapModule extends MiniPlugin
{
	public enum Perm implements Permission
	{
		MAP_COMMAND,
	}

	// Every BLOCK_SCAN_INTERVAL we add as a new region to scan
	private static final int BLOCK_SCAN_INTERVAL = 16 * 3;
	// 1536 is the width of the entire world from one borderland to the other
	private static final int HALF_WORLD_SIZE = 768;
	// This slot is where the Clans Map will go by default
	private static final int CLANS_MAP_SLOT = 8;

	private static final String[] ZOOM_INFO;

	static
	{
		ZOOM_INFO = new String[4];
		for (int zoomLevel = 0; zoomLevel <= 3; zoomLevel++)
		{
			StringBuilder progressBar = new StringBuilder(C.cBlue);

			boolean colorChange = false;
			for (int i = 2; i >= 0; i--)
			{
				if (!colorChange && i < zoomLevel)
				{
					progressBar.append(C.cGray);
					colorChange = true;
				}
				char c;
				switch (i)
				{
					case 0:
						c = '█';
						break;
					case 1:
						c = '▆';
						break;
					default:
						c = '▄';
						break;
				}
				for (int a = 0; a < 4; a++)
				{
					progressBar.append(c);
				}

				if (i > 0)
				{
					progressBar.append(" ");
				}
			}
			ZOOM_INFO[zoomLevel] = progressBar.toString();
		}
	}

	private Comparator<Entry<Integer, Integer>> _comparator;
	private int[][] _heightMap = new int[(HALF_WORLD_SIZE * 2) + 16][];
	private HashMap<Integer, Byte[][]> _map = new HashMap<Integer, Byte[][]>();
	private short _mapId = -1;
	private HashMap<String, MapInfo> _mapInfo = new HashMap<String, MapInfo>();
	private HashMap<Integer, Integer> _scale = new HashMap<Integer, Integer>();
	// Use LinkedList because operations are either add(Entry) which is O(1) and remove(0) which is O(1) on LinkedList but O(n) on ArrayList
	private LinkedList<Entry<Integer, Integer>> _scanList = new LinkedList<Entry<Integer, Integer>>();
	private World _world;
	private WorldServer _nmsWorld;
	private ChunkProviderServer _chunkProviderServer;
	private ChunkRegionLoader _chunkRegionLoader;

	private ItemMapModule()
	{
		super("Map");

		_comparator = (o1, o2) ->
		{
			// Render the places outside the map first to speed up visual errors fixing
			int outsideMap = Boolean.compare(o1.getValue() < -HALF_WORLD_SIZE, o2.getValue() < -HALF_WORLD_SIZE);

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

		};

		_scale.put(0, 1);
		// _scale.put(1, 2);
		_scale.put(1, 4);
		_scale.put(2, 8);
		_scale.put(3, 13);
		// _scale.put(5, 16);

		for (Entry<Integer, Integer> entry : _scale.entrySet())
		{
			int size = (HALF_WORLD_SIZE * 2) / entry.getValue();
			Byte[][] bytes = new Byte[size][];

			for (int i = 0; i < size; i++)
			{
				bytes[i] = new Byte[size];
			}

			_map.put(entry.getKey(), bytes);
		}

		for (int i = 0; i < _heightMap.length; i++)
		{
			_heightMap[i] = new int[_heightMap.length];
		}

		_world = Bukkit.getWorld("world");

		try
		{
			Field chunkLoader = ChunkProviderServer.class.getDeclaredField("chunkLoader");
			chunkLoader.setAccessible(true);
			_nmsWorld = ((CraftWorld) _world).getHandle();
			_chunkProviderServer = _nmsWorld.chunkProviderServer;
			_chunkRegionLoader = (ChunkRegionLoader) chunkLoader.get(_chunkProviderServer);
			if (_chunkRegionLoader == null)
			{
				throw new RuntimeException("Did not expect null chunkLoader");
			}
		}
		catch (ReflectiveOperationException e)
		{
			throw new RuntimeException("Could not reflectively access ChunkRegionLoader", e);
		}

		try
		{
			File file = new File("world/gem_hunters_map_id");
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

			if (file.exists())
			{
				BufferedReader br = new BufferedReader(new FileReader(file));
				_mapId = Short.parseShort(br.readLine());
				br.close();

				if (foundFile == null)
				{
					_mapId = -1;
					file.delete();
				}
				else
				{
					for (int i = _mapId; i <= _mapId + 100; i++)
					{
						File file1 = new File("world/data/map_" + i + ".dat");

						if (!file1.exists())
						{
							FileUtils.copyFile(foundFile, file1);
						}

						setupRenderer(Bukkit.getMap((short) i));
					}
				}
			}

			if (_mapId < 0)
			{
				MapView view = Bukkit.createMap(_world);
				_mapId = view.getId();
				setupRenderer(view);

				for (int i = 0; i < 100; i++)
				{
					setupRenderer(Bukkit.createMap(_world));// Ensures the following 100 maps are unused
				}

				file.createNewFile();

				PrintWriter writer = new PrintWriter(file, "UTF-8");
				writer.print(_mapId);
				writer.close();
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		rebuildScan();
		initialScan();
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.PLAYER.setPermission(Perm.MAP_COMMAND, true, true);
	}

	private void initialScan()
	{
		System.out.println("Beginning initial scan. There are " + _scanList.size() + " regions to scan");

		// How many regions before logging an update (Currently set to every 20%)
		int logPer = _scanList.size() / 5;

		while (!_scanList.isEmpty())
		{
			Entry<Integer, Integer> entry = _scanList.remove(0);
			if (_scanList.size() % logPer == 0)
			{
				System.out.println("Running initial render... " + _scanList.size() + " sections to go");
			}

			int startingX = entry.getKey();
			int startingZ = entry.getValue();

			boolean outsideMap = startingZ < -HALF_WORLD_SIZE;

			scanWorldMap(startingX, startingZ, !outsideMap, true);

			if (outsideMap)
			{
				continue;
			}

			for (int scale = 1; scale < _scale.size(); scale++)
			{
				if (scale == 3)
					continue;

				drawWorldScale(scale, startingX, startingZ);
				colorWorldHeight(scale, startingX, startingZ);
			}

			colorWorldHeight(0, startingX, startingZ);
		}

		for (int x = -HALF_WORLD_SIZE; x < HALF_WORLD_SIZE; x += BLOCK_SCAN_INTERVAL)
		{
			for (int z = -HALF_WORLD_SIZE; z < HALF_WORLD_SIZE; z += BLOCK_SCAN_INTERVAL)
			{
				drawWorldScale(3, x, z);
				colorWorldHeight(3, x, z);
			}
		}

		System.out.println("Finished first map scan and render");
	}

	private void setupRenderer(MapView view)
	{
		for (MapRenderer renderer : view.getRenderers())
		{
			view.removeRenderer(renderer);
		}

		view.addRenderer(new ItemMapRenderer());
	}
	
	/**
	 * Get the center of the map.
	 */
	public int calcMapCenter(int zoom, int cord)
	{
		int mapSize = HALF_WORLD_SIZE / zoom; // This is how large the map is in pixels

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

	private void colorWorldHeight(int scale, int startingX, int startingZ)
	{
		Byte[][] map = _map.get(scale);
		int zoom = getZoom(scale);

		for (int x = startingX; x < startingX + BLOCK_SCAN_INTERVAL; x += zoom)
		{
			double d0 = 0;

			// Prevents ugly lines for the first line of Z

			for (int addX = 0; addX < zoom; addX++)
			{
				for (int addZ = 0; addZ < zoom; addZ++)
				{
					int hX = x + addX + HALF_WORLD_SIZE;
					int hZ = (startingZ - zoom) + addZ + HALF_WORLD_SIZE;

					if (hX >= HALF_WORLD_SIZE * 2 || hZ >= HALF_WORLD_SIZE * 2)
					{
						continue;
					}

					d0 += _heightMap[hX + 16][hZ + 16] / (zoom * zoom);
				}
			}

			for (int z = startingZ; z < startingZ + BLOCK_SCAN_INTERVAL; z += zoom)
			{
				// Water depth colors not included
				double d1 = 0;

				for (int addX = 0; addX < zoom; addX++)
				{
					for (int addZ = 0; addZ < zoom; addZ++)
					{
						int hX = x + addX + HALF_WORLD_SIZE;
						int hZ = z + addZ + HALF_WORLD_SIZE;

						if (hX >= HALF_WORLD_SIZE * 2 || hZ >= HALF_WORLD_SIZE * 2)
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

				int origColor = map[(x + HALF_WORLD_SIZE) / zoom][(z + HALF_WORLD_SIZE) / zoom] - 1;

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
				if((color <= -113 || color >= 0) && color <= 127)
				{
					map[(x + HALF_WORLD_SIZE) / zoom][(z + HALF_WORLD_SIZE) / zoom] = color;
				}
				else
				{
//					System.out.println(String.format("Tried to set color to %s in colorWorldHeight scale: %s, sx: %s, sz: %s, x: %s, z: %s, zoom: %s",
//							color, scale, startingX, startingZ, x, z, zoom));
				}
			}
		}
	}

	private void drawWorldScale(int scale, int startingX, int startingZ)
	{
		Byte[][] first = _map.get(0);
		Byte[][] second = _map.get(scale);
		int zoom = getZoom(scale);

		for (int x = startingX; x < startingX + BLOCK_SCAN_INTERVAL; x += zoom)
		{
			for (int z = startingZ; z < startingZ + BLOCK_SCAN_INTERVAL; z += zoom)
			{
				HashMultiset<Byte> hashmultiset = HashMultiset.create();

				for (int addX = 0; addX < zoom; addX++)
				{
					for (int addZ = 0; addZ < zoom; addZ++)
					{
						int pX = x + addX + HALF_WORLD_SIZE;
						int pZ = z + addZ + HALF_WORLD_SIZE;

						if (pX >= first.length || pZ >= first.length)
						{
							continue;
						}

						Byte b = first[pX][pZ];

						hashmultiset.add(b);
					}
				}

				Byte color;
				try
				{
					color = Iterables.getFirst(Multisets.copyHighestCountFirst(hashmultiset), (byte) 0);
				}
				catch (Exception e)
				{
					color = (byte) 0;
				}
				second[(x + HALF_WORLD_SIZE) / zoom][(z + HALF_WORLD_SIZE) / zoom] = color;
			}
		}
	}

	@EventHandler
	public void dropItem(ItemSpawnEvent event)
	{
		if (isItemClansMap(event.getEntity().getItemStack()))
			event.getEntity().remove();
	}

	public void removeMap(Player player)
	{
		for (int slot = 0; slot < player.getInventory().getSize(); slot++)
		{
			if (isItemClansMap(player.getInventory().getItem(slot)))
				player.getInventory().setItem(slot, null);
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
		return getDistance(x1, z1, entry.getKey() + (BLOCK_SCAN_INTERVAL / 2), entry.getValue() + (BLOCK_SCAN_INTERVAL / 2));
	}

	public Byte[][] getMap(int scale)
	{
		return _map.get(scale);
	}

	public MapInfo getMap(Player player)
	{
		return _mapInfo.get(player.getName());
	}

	public int getMapSize()
	{
		return HALF_WORLD_SIZE;
	}

	public int getZoom(int scale)
	{
		return _scale.get(scale);
	}

	//fixme So what appears to happen is that after you die, if your map is is the same then the map is frozen
	@EventHandler
	public void onDeath(PlayerDeathEvent event)
	{
		MapInfo info = getMap(event.getEntity());

		info.setMap(Math.min(_mapId + 100, info.getMap() + 1));
	}

	@EventHandler
	public void onHotbarMove(PlayerItemHeldEvent event)
	{
		Player player = event.getPlayer();

		if (!isItemClansMap(player.getInventory().getItem(event.getNewSlot())))
			return;

		showZoom(player, getMap(player));
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event)
	{
		if (event.getAction() == Action.PHYSICAL)
			return;

		if (!isItemClansMap(event.getItem()))
			return;

		event.setCancelled(true);

		Player player = event.getPlayer();

		MapInfo info = getMap(player);

		boolean zoomIn = UtilEvent.isAction(event, ActionType.L);

		if (!_scale.containsKey(info.getScale() + (zoomIn ? -1 : 1)))
		{
			return;
		}

		if (!info.canZoom())
		{
			long remainingTime = (info.getZoomCooldown() + 2500) - System.currentTimeMillis();

			UtilPlayer.message(
					player,
					F.main("Recharge",
							"You cannot use " + F.skill("Map Zoom") + " for "
									+ F.time(UtilTime.convertString((remainingTime), 1, TimeUnit.FIT)) + "."));
			return;
		}

		info.addZoom();

		if (zoomIn)
		{
			int newScale = info.getScale() - 1;
			Location loc = player.getLocation();

			int zoom = getZoom(newScale);

			info.setInfo(newScale, calcMapCenter(zoom, loc.getBlockX()), calcMapCenter(zoom, loc.getBlockZ()));
		}
		else
		{
			int newScale = info.getScale() + 1;
			Location loc = player.getLocation();

			int zoom = getZoom(newScale);

			info.setInfo(newScale, calcMapCenter(zoom, loc.getBlockX()), calcMapCenter(zoom, loc.getBlockZ()));
		}

		showZoom(player, info);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerJoin(PlayerJoinEvent event)
	{
		handleGive(event.getPlayer());
	}

	@EventHandler
	public void respawn(PlayerCustomRespawnEvent event)
	{
		handleGive(event.getPlayer());
	}

	public void handleGive(Player player)
	{
		MapInfo info = new MapInfo(_mapId);
		Location loc = player.getLocation();

		int zoom = getZoom(1);

		info.setInfo(1, calcMapCenter(zoom, loc.getBlockX()), calcMapCenter(zoom, loc.getBlockZ()));
		_mapInfo.put(player.getName(), info);
		setMap(player);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		_mapInfo.remove(event.getPlayer().getName());
	}

	private void rebuildScan()
	{
		for (int x = -HALF_WORLD_SIZE; x < HALF_WORLD_SIZE; x += BLOCK_SCAN_INTERVAL)
		{
			for (int z = -HALF_WORLD_SIZE - 16; z < HALF_WORLD_SIZE; z += (z < -HALF_WORLD_SIZE ? 16 : BLOCK_SCAN_INTERVAL))
			{
				_scanList.add(new HashMap.SimpleEntry<>(x, z));
			}
		}

		Collections.sort(_scanList, _comparator);
	}

	@EventHandler
	public void recenterMap(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		for (Player player : Bukkit.getOnlinePlayers())
		{
			MapInfo info = getMap(player);
			
			if (info == null || info.getScale() >= 3)
			{
				continue;
			}

			Location l = player.getLocation();
			int zoom = getZoom(info.getScale());

			double mapX = (l.getX() - info.getX()) / zoom;
			double mapZ = (l.getZ() - info.getZ()) / zoom;

			if (Math.abs(mapX) > 22 || Math.abs(mapZ) > 22)
			{
				int newX = calcMapCenter(zoom, l.getBlockX());
				int newZ = calcMapCenter(zoom, l.getBlockZ());

				if (Math.abs(mapX) > 22 ? newX != info.getX() : newZ != info.getZ())
				{
					info.setInfo(newX, newZ);
				}
			}
		}
	}

	@EventHandler
	public void renderMap(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		if (_scanList.isEmpty() && UtilServer.getPlayers().length > 0)
		{
			rebuildScan();
		}

		if (_scanList.size() % 20 == 0)
		{
			Collections.sort(_scanList, _comparator);
		}

		if (_scanList.isEmpty())
		{
			return;
		}

		Entry<Integer, Integer> entry = _scanList.remove(0);

		int startingX = entry.getKey();
		int startingZ = entry.getValue();

		boolean outsideMap = startingZ < -HALF_WORLD_SIZE;

		scanWorldMap(startingX, startingZ, !outsideMap, false);

		if (outsideMap)
			return;

		for (int scale = 1; scale < _scale.size(); scale++)
		{
			drawWorldScale(scale, startingX, startingZ);
			colorWorldHeight(scale, startingX, startingZ);
		}

		colorWorldHeight(0, startingX, startingZ);
	}


	// Let's not create hundreds of thousands of BlockPositions
	// Single thread = should be thread safe
	private BlockPosition.MutableBlockPosition _blockPosition = new BlockPosition.MutableBlockPosition();

	// Maps the cached chunks which were loaded from disk to save IO operations
	private LongObjectHashMap<Chunk> _chunkCache = new LongObjectHashMap<>();

	/*
	 * Remove the cached chunks when the real chunks are loaded in
	 */
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void LoadChunk(ChunkLoadEvent event)
	{
		_chunkCache.remove(LongHash.toLong(event.getChunk().getX(), event.getChunk().getZ()));
	}

	/*
	 * Given a particular coordinate, this method will scan up to BLOCK_SCAN_INTERVAL and record the color of ever 16th block
	 * If a chunk has not been loaded, the following steps will be taken:
	 * 	* Attempt to load the chunk from disk.
	 * 	* If the chunk could not be loaded, generate it froms scratch
	 * Otherwise, the loaded chunk will be used
	 */
	public void scanWorldMap(int startingX, int startingZ, boolean setColors, boolean isFirstScan)
	{
		Byte[][] map = _map.get(0);
		for (int beginX = startingX; beginX < startingX + BLOCK_SCAN_INTERVAL; beginX += 16)
		{
			for (int beginZ = startingZ - (startingZ > -HALF_WORLD_SIZE ? 16 : 0); beginZ < startingZ
					+ (setColors ? BLOCK_SCAN_INTERVAL : 16); beginZ += 16)
			{
				int chunkX = beginX / 16;
				int chunkZ = beginZ / 16;
				net.minecraft.server.v1_8_R3.Chunk nmsChunk = _chunkProviderServer.getChunkIfLoaded(chunkX, chunkZ);
				if (nmsChunk == null)
				{
					long key = LongHash.toLong(chunkX, chunkZ);
					nmsChunk = _chunkCache.get(key);
					if (nmsChunk == null)
					{
						if (!isFirstScan)
						{
							continue;
						}
						try
						{
							Object[] data = _chunkRegionLoader.loadChunk(_nmsWorld, chunkX, chunkZ);
							if (data == null)
							{
								// Something is wrong with the chunk
								System.out.println("Chunk is not generated or missing level/block data. Regenerating (" + chunkX + "," + chunkZ + ")");
								nmsChunk = ((CraftChunk) _world.getChunkAt(chunkX, chunkZ)).getHandle();
							}
							else
							{
								nmsChunk = (net.minecraft.server.v1_8_R3.Chunk) data[0];
							}
						}
						catch (IOException e)
						{
							throw new RuntimeException("Chunk is corrupt or not readable!", e);
						}
						_chunkCache.put(key, nmsChunk);
					}
				}

				if (!nmsChunk.isEmpty())
				{
					for (int x = beginX; x < beginX + 16; x++)
					{
						for (int z = beginZ; z < beginZ + 16; z++)
						{
							int color = 0;

							int k3 = x & 0xF;
							int l3 = z & 0xF;

							int l4 = nmsChunk.b(k3, l3) + 1;
							IBlockData iblockdata = Blocks.AIR.getBlockData();

							if (l4 > 1)
							{
								do
								{
									l4--;
									_blockPosition.c(k3, l4, l3);
									iblockdata = nmsChunk.getBlockData(_blockPosition);
								}
								while (iblockdata.getBlock().g(iblockdata) == MaterialMapColor.b && (l4 > 0));

								if ((l4 > 0) && (iblockdata.getBlock().getMaterial().isLiquid()))
								{
									int j5 = l4 - 1;
									Block block1;
									do
									{
										_blockPosition.c(k3, j5--, l3);
										block1 = nmsChunk.getType(_blockPosition);
									}
									while ((j5 > 0) && (block1.getMaterial().isLiquid()));
								}
							}

							_heightMap[x + HALF_WORLD_SIZE + 16][z + HALF_WORLD_SIZE + 16] = l4;

							if (setColors)
							{
								//color = block.f(i5).M;
								_blockPosition.c(k3, l4, l3);
								IBlockData data = nmsChunk.getBlockData(_blockPosition);
								color = data.getBlock().g(data).M;

								color = (byte) ((color * 4) + 1);
							}

							if (setColors && beginZ >= startingZ)
							{
								map[x + HALF_WORLD_SIZE][z + HALF_WORLD_SIZE] = (byte) color;
							}
						}
					}
				}
			}
		}
	}

	public void setMap(Player player)
	{
		for (ItemStack item : UtilInv.getItems(player))
		{
			if (isItemClansMap(item))
			{
				return;
			}
		}

		ItemStack item = new ItemBuilder(Material.MAP, 1, (short) getMap(player).getMap()).setTitle(C.cGreen + "World Map").build();

		int slot = CLANS_MAP_SLOT;

		ItemStack mapSlot = player.getInventory().getItem(slot);
		if (mapSlot != null && mapSlot.getType() != Material.AIR)
		{
			slot = player.getInventory().firstEmpty();
		}

		if (slot >= 0)
		{
			player.getInventory().setItem(slot, item);
		}
	}

	/*
	 * Displays the action bar to a player given their zoom level. Implementation may change
	 */
	private void showZoom(Player player, MapInfo info)
	{
		UtilTextBottom.display(ZOOM_INFO[info.getScale()], player);
	}

	/*
	 * Check whether an {@link ItemStack} is also a Clans Map
	 *
	 * @param itemStack The {@link ItemStack} to check
	 * @returns Whether the {@link ItemStack} is also a Clans Map
	 */
	private boolean isItemClansMap(ItemStack itemStack)
	{
		return UtilItem.matchesMaterial(itemStack, Material.MAP)
				&& itemStack.getDurability() >= _mapId
				&& itemStack.getDurability() <= _mapId + 100;
	}
	
	@Override
	public void addCommands()
	{
		addCommand(new MapCommand(this));
	}
}
