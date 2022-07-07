package mineplex.gemhunters.supplydrop;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.common.Pair;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.gemhunters.loot.LootModule;
import mineplex.gemhunters.supplydrop.command.SupplyDropCommand;
import mineplex.gemhunters.world.WorldDataModule;

@ReflectivelyCreateMiniPlugin
public class SupplyDropModule extends MiniPlugin
{
	public enum Perm implements Permission
	{
		SUPPLY_DROP_COMMAND,
		START_SUPPLY_DROP_COMMAND,
		STOP_SUPPLY_DROP_COMMAND,
	}

	private static final long SEQUENCE_TIMER = TimeUnit.MINUTES.toMillis(20);

	private static final String CHEST_COLOUR = "RED";
	private static final String LOCATION_DATA = "SUPPLY_DROP";
	
	private final BlockRestore _blockRestore;
	private final LootModule _loot;
	private final WorldDataModule _worldData;

	private final Set<Block> _beaconBlocks;

	private String[] _locationKeys;
	private SupplyDrop _current;

	private long _lastSupplyDrop;

	private SupplyDropModule()
	{
		super("Supply Drop");

		_blockRestore = require(BlockRestore.class);
		_loot = require(LootModule.class);
		_worldData = require(WorldDataModule.class);

		_beaconBlocks = new HashSet<>();

		_lastSupplyDrop = System.currentTimeMillis();
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.ADMIN.setPermission(Perm.SUPPLY_DROP_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.START_SUPPLY_DROP_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.STOP_SUPPLY_DROP_COMMAND, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new SupplyDropCommand(this));
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}
		
		if (isActive())
		{
			if (_current.advancePath())
			{
				stopSequence();
			}
			else if (UtilMath.offset2d(_current.getBladeLocation(), _current.getChestLocation()) < 1)
			{
				spawnLootChest();
			}
		}
		else if (UtilTime.elapsed(_lastSupplyDrop, SEQUENCE_TIMER))
		{
			startSequence();
		}
	}

	@EventHandler
	public void itemSpawn(ItemSpawnEvent event)
	{
		// The Helicopter has a door. This stops it dropping items when it
		// moves.
		if (event.getEntity().getItemStack().getType() == Material.IRON_DOOR)
		{
			event.setCancelled(true);
		}
	}

	public void startSequence(String locationKey)
	{
		Location spawn = _worldData.getCustomLocation(LOCATION_DATA + " " + locationKey + " Start").get(0);
		Location destination = _worldData.getCustomLocation(LOCATION_DATA + " " + locationKey + " Chest").get(0);
		Location despawn = _worldData.getCustomLocation(LOCATION_DATA + " " + locationKey + " End").get(0);

		// Construct a beacon
		for (Pair<Location, Pair<Material, Byte>> pair : UtilBlock.getBeaconBlocks(destination, (byte) 0))
		{
			// Look it's like a maze
			_beaconBlocks.add(pair.getLeft().getBlock());
			_blockRestore.add(pair.getLeft().getBlock(), pair.getRight().getLeft().getId(), pair.getRight().getRight(), Long.MAX_VALUE);
		}

		// Inform the masses
		UtilTextMiddle.display(C.cYellow + locationKey, C.cGray + "A Supply Drop is spawning!", 10, 40, 10);
		UtilServer.broadcast(F.main(_moduleName, "A Supply Drop is spawning at " + F.elem(locationKey) + " - " + C.cYellow + UtilWorld.locToStrClean(destination)));

		_lastSupplyDrop = System.currentTimeMillis();
		_current = new SupplyDrop(locationKey, spawn, destination, despawn);
	}

	public void startSequence()
	{
		startSequence(getLocationKeys()[UtilMath.r(getLocationKeys().length)]);
	}

	public void stopSequence()
	{
		// Remove beacon (only needed incase the command was executed)
		for (Block block : _beaconBlocks)
		{
			_blockRestore.restore(block);
		}

		_beaconBlocks.clear();
		_current.stop();
		_current = null;
	}

	public void spawnLootChest()
	{
		Location chest = _current.getBladeLocation().clone().subtract(0, 10, 0);

		runSyncTimer(new BukkitRunnable()
		{

			Block chestBlock = chest.getBlock();

			@Override
			public void run()
			{
				chestBlock.setType(Material.AIR);
				chestBlock = chestBlock.getRelative(BlockFace.DOWN);
				chestBlock.setType(Material.CHEST);

				if (chestBlock.getRelative(BlockFace.DOWN).getType() != Material.AIR)
				{
					// Add location that the chest will appear at into the spawned
					// chests list so that LootModule can populate it with loot.
					_loot.addSpawnedChest(chestBlock.getLocation(), CHEST_COLOUR);

					// Remove beacon
					for (Block beacon : _beaconBlocks)
					{
						_blockRestore.restore(beacon);
					}

					_beaconBlocks.clear();
					cancel();
				}

				UtilFirework.playFirework(chestBlock.getLocation().add(0.5, 1, 0.5), FireworkEffect.Type.BALL, Color.YELLOW, true, false);
			}
		}, 0, 5);
	}

	public boolean isActive()
	{
		return _current != null;
	}
	
	public SupplyDrop getActive()
	{
		return _current;
	}
	
	public long getLastSupplyDrop()
	{
		return _lastSupplyDrop;
	}
	
	public long getSequenceTimer()
	{
		return SEQUENCE_TIMER;
	}

	public String[] getLocationKeys()
	{
		if (_locationKeys == null)
		{
			List<String> supplyDropKeys = new ArrayList<>();

			for (String key : _worldData.getAllCustomLocations().keySet())
			{
				if (key.startsWith(LOCATION_DATA))
				{
					String[] split = key.split(" ");
					String nameKey = "";

					for (int i = 1; i < split.length - 1; i++)
					{
						nameKey += split[i] + " ";
					}

					nameKey = nameKey.trim();

					if (!supplyDropKeys.contains(nameKey))
					{
						supplyDropKeys.add(nameKey);
					}
				}
			}

			_locationKeys = supplyDropKeys.toArray(new String[0]);
		}

		return _locationKeys;
	}
}