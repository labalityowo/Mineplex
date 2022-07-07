 package mineplex.minecraft.game.core.boss;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.blockrestore.BlockRestoreMap;
import mineplex.core.common.block.BlockData;
import mineplex.core.common.block.schematic.Schematic;
import mineplex.core.common.block.schematic.SchematicRunnable;
import mineplex.core.common.block.schematic.UtilSchematic;
import mineplex.core.common.util.C;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.projectile.ProjectileManager;
import mineplex.core.thereallyoldscoreboardapiweshouldremove.ScoreboardManager;
import mineplex.core.thereallyoldscoreboardapiweshouldremove.elements.ScoreboardElement;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.condition.ConditionManager;
import mineplex.minecraft.game.core.damage.DamageManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public abstract class WorldEvent implements Listener, ScoreboardElement
{
	// 20 Minutes
	private static final int ACTIVE_TIMEOUT = 1200000;
	
	private DamageManager _damageManager;
	private ConditionManager _conditionManager;
	private DisguiseManager _disguiseManager;
	private ProjectileManager _projectileManager;
	
	private String _name;
	private EventState _state;
	private Location _cornerLocation;
	private EventMap _map;
	private Random _random;
	private long _timeStarted = System.currentTimeMillis();
	private long _lastActive;
	private Schematic _schematic;
	
	// Creatures
	private List<EventCreature<?>> _creatures;
	// Block Restore
	private BlockRestoreMap _blocks;
	private boolean _isArcade;
	private double _difficulty = 1;
	
	private double _minX;
	private double _minY;
	private double _minZ;
	
	private double _maxX;
	private double _maxY;
	private double _maxZ;
	
	public WorldEvent(DisguiseManager disguiseManager, ProjectileManager projectileManager, DamageManager damageManager, BlockRestore blockRestore, ConditionManager conditionManager, String name, Location cornerLocation)
	{
		this(disguiseManager, projectileManager, damageManager, blockRestore, conditionManager, name, cornerLocation, null);
	}
	
	public WorldEvent(DisguiseManager disguiseManager, ProjectileManager projectileManager, DamageManager damageManager, BlockRestore blockRestore, ConditionManager conditionManager, String name, Location cornerLocation, String schematicName)
	{
		_disguiseManager = disguiseManager;
		_projectileManager = projectileManager;
		_damageManager = damageManager;
		_conditionManager = conditionManager;
		
		_name = name;
		_state = EventState.PREPARE;
		_cornerLocation = cornerLocation;
		_random = new Random();
		
		_creatures = new ArrayList<>();
		_blocks = blockRestore.createMap();
		_lastActive = System.currentTimeMillis();
		
		if (schematicName != null)
		{
			try
			{
				_schematic = UtilSchematic.loadSchematic(new File(schematicName));
				
				_cornerLocation.subtract(_schematic.getWidth() / 2, 0, _schematic.getLength() / 2);
			}
			catch (IOException e)
			{
				e.printStackTrace();
				setState(EventState.COMPLETE);
			}
		}
	}
	
	public DisguiseManager getDisguiseManager()
	{
		return _disguiseManager;
	}
	
	public ProjectileManager getProjectileManager()
	{
		return _projectileManager;
	}
	
	public void setDifficulty(double difficulty)
	{
		_difficulty = difficulty;
	}
	
	public void setArcadeGame(boolean isArcade)
	{
		_isArcade = isArcade;
	}
	
	public void loadMap()
	{
		loadMap(new Runnable()
		{
			@Override
			public void run()
			{
				System.out.println("Runnable on complete");
			}
		});
	}
	
	public double getDifficulty()
	{
		return _difficulty;
	}
	
	public void loadMap(Runnable runnable)
	{
		if (_schematic == null)
		{
			return;
		}
		
		EventMap map = new EventMap(_schematic, _cornerLocation);
		setMap(map, runnable);
	}
	
	public Schematic getSchematic()
	{
		return _schematic;
	}
	
	public ConditionManager getCondition()
	{
		return _conditionManager;
	}
	
	public final void start()
	{
		customStart();
	}

	@Override
	public List<String> getLines(ScoreboardManager manager, Player player, List<String> out)
	{
		return null;
	}
	
	protected abstract void customStart();
	
	public final void cleanup()
	{
		clearCreatures();
		restoreBlocks();
		customCleanup();
	}
	
	public void customCleanup()
	{
	
	}
	
	public final void cancel()
	{
		cleanup();
		setState(EventState.CANCELLED);
		customCancel();
	}
	
	protected final void triggerEnd()
	{
		setState(EventState.COMPLETE);
	}
	
	protected void customCancel()
	{
	
	}
	
	protected void customTick()
	{
	
	}
	
	protected void setState(EventState state)
	{
		_state = state;
	}
	
	public EventState getState()
	{
		return _state;
	}
	
	public long getTimeRunning()
	{
		return System.currentTimeMillis() - _timeStarted;
	}
	
	public DamageManager getDamageManager()
	{
		return _damageManager;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public void setName(String name)
	{
		_name = name;
	}
	
	protected Random getRandom()
	{
		return _random;
	}
	
	public Location getCenterLocation()
	{
		return _map == null ? _cornerLocation : _map.getCenterLocation();
	}
	
	public List<EventCreature<?>> getCreatures()
	{
		return _creatures;
	}
	
	public void registerCreature(EventCreature<?> creature)
	{
		UtilServer.getServer().getPluginManager().registerEvents(creature, _damageManager.getPlugin());
		_creatures.add(creature);
	}
	
	public void removeCreature(EventCreature<?> creature)
	{
		Bukkit.getPluginManager().callEvent(new EventCreatureDeathEvent(creature));
		HandlerList.unregisterAll(creature);
		_creatures.remove(creature);
	}

	public void announceStart()
	{
		UtilTextMiddle.display(C.cGreen + getName(), UtilWorld.locToStrClean(getCenterLocation()), 10, 100, 40);

		UtilServer.broadcast(F.main("Event", F.elem(getName()) + " has started at coordinates " + F.elem(UtilWorld.locToStrClean(getCenterLocation()))));
	}
	
	public void clearCreatures()
	{
		for (EventCreature<?> creature : _creatures)
		{
			creature.remove(false);
			HandlerList.unregisterAll(creature);
		}
		
		_creatures.clear();
	}
	
	public JavaPlugin getPlugin()
	{
		return _damageManager.getPlugin();
	}
	
	public void restoreBlocks()
	{
		_blocks.restore();
	}
	
	public long getLastActive()
	{
		return _lastActive;
	}
	
	public void updateLastActive()
	{
		_lastActive = System.currentTimeMillis();
	}
	
	public EventMap getEventMap()
	{
		return _map;
	}
	
	public void setMap(EventMap map, final Runnable onComplete)
	{
		_map = map;
		
		SchematicRunnable task = new SchematicRunnable(_damageManager.getPlugin(), map.getSchematic(), _cornerLocation.getBlock(), new Callback<List<BlockData>>()
		{
			@Override
			public void run(List<BlockData> data)
			{
				if (!_isArcade)
				{
					for (BlockData blockData : data)
					{
						_blocks.addBlockData(blockData);
					}
				}
				
				if (onComplete != null)
				{
					onComplete.run();
				}
			}
		});
		
		task.setBlocksPerTick(_isArcade ? 2000000 : 1000);
		task.start();
	}
	
	protected BlockRestoreMap getBlocks()
	{
		return _blocks;
	}
	
	public void setBlock(Block block, Material material)
	{
		setBlock(block, material, (byte) 0);
	}
	
	public void setBlock(Block block, Material material, byte data)
	{
		setBlock(block, material.getId(), data);
	}
	
	public void setBlock(Block block, int id, byte data)
	{
		_blocks.set(block, id, data);
	}
	
	public int getRandomRange(int min, int max)
	{
		return min + _random.nextInt(UtilMath.clamp(max - min, min, max));
	}
	
	@EventHandler
	public void tick(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		customTick();
	}
	
	@EventHandler
	public void endInactive(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}
		
		long diff = System.currentTimeMillis() - getLastActive();
		if (diff > ACTIVE_TIMEOUT)
		{
			setState(EventState.COMPLETE);
		}
	}
	
	@EventHandler
	public void prepareTimeout(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}
		
		if (getState() != EventState.PREPARE)
		{
			return;
		}
		
		// Event was preparing for more than 5 minutes
		if (getTimeRunning() > 1000 * 60 * 5)
		{
			cancel();
		}
	}

	public boolean isInBounds(Location location)
	{
		if (_minX == 0)
		{
			// Calculate bounds
			Set<Block> blocks = _blocks.getChangedBlocks();
			
			for (Block block : blocks)
			{
				if (_minX > block.getX())
				{
					_minX = block.getX();
				}

				if (_minY > block.getY())
				{
					_minY = block.getY();
				}
				
				if (_minZ > block.getZ())
				{
					_minZ = block.getZ();
				}
				
				if (_maxX < block.getX())
				{
					_maxX = block.getX();
				}

				if (_maxY < block.getY())
				{
					_maxY = block.getY();
				}
				
				if (_maxZ < block.getZ())
				{
					_maxZ = block.getZ();
				}
			}
			
			_maxY++;
		}
		
		return UtilAlg.inBoundingBox(location, new Vector(_minX, _minY, _minZ), new Vector(_maxX, _maxY, _maxZ));
	}

}
