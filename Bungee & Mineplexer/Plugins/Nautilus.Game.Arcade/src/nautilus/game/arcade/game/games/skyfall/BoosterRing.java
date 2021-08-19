package nautilus.game.arcade.game.games.skyfall;

import java.util.ArrayList;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.omg.DynamicAny._DynUnionStub;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.hologram.Hologram;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.Game.GameState;

/**
 * The BoosterRing is compatible with any game, so it may be used in any other game then Skyfall. <br/>
 * This Object represents a single ring on the map which can boost players in their current direction.
 *
 * @author xXVevzZXx
 */
public class BoosterRing extends Crumbleable implements Listener
{
	private static int MAX_RING_BOUNDS = 18;
	private static int SEARCH_OUTER_RING_RANGE = 12;
	
	private Game _host;
	
	private BlockFace[] _blockFaces = new BlockFace[]{BlockFace.DOWN, BlockFace.UP, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH};

	private Location _location;
	
	private ArrayList<Location> _area;
	private ArrayList<Location> _ring;
	
	private float _boostStrength;
	
	private boolean _disabled;
	
	private Location _ringMiddle;
	
	private ItemStack _material;
	
	private long _disabledSince;
	private long _disabledFor;
	
	private long _disableDelay;
	private long _disableDelayStarted;
	
	private CooldownData _delayedData;
	
	private Hologram _hologram;
	private boolean _timer;
	
	private LinkedList<Location> _sortedBorder;
	
	private double _blocksToFill;
	
	private BlockFace[] _faces = new BlockFace[2];
	
	/**
	 * Standard Constructor for BoosterRing. <br/>
	 * This Constructor will initialize the ring and collect all blocks that are needed. <br/>
	 * A BoosterRing is created by setting the location param to a Location which is <br/>
	 * Inside the ring and only connects to the rings blocks at one BlockFace at a time.
	 * 
	 * @param host is your game
	 * @param location Location inside the ring specified above
	 * @param boostStrength strenght of the boost
	 */
	public BoosterRing(Game host, Location location, float boostStrength)
	{
		_host = host;
		_location = location;
		_boostStrength = boostStrength;
		_disabledSince = System.currentTimeMillis();
		
		_disableDelayStarted = 0;
		
		System.out.println("Registering Ring");
		
		setupRing();	
		outerBlocks();
		sortBlocks();
		
		System.out.println("Ring size: " + _area.size());
		Bukkit.getPluginManager().registerEvents(this, UtilServer.getPlugin());
		
		_hologram = new Hologram(host.getArcadeManager().getHologramManager(), _ringMiddle, "");
		_hologram.setViewDistance(300);
		
		init();
	}
	
	@EventHandler
	public void deregister(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.Dead
				|| event.GetState() == GameState.End)
		{
			HandlerList.unregisterAll(this);
		}
	}
	
	public void outerBlocks()
	{
		_ring = new ArrayList<>();
		Location[] locs = new Location[4];
		BlockFace[] toCheck = new BlockFace[] {_faces[0], _faces[1], getOpposite(_faces[0]), getOpposite(_faces[1])};
		
		int e = 0;
		for (BlockFace face : toCheck)
		{
			int i = 0;
			Block block = _ringMiddle.getBlock().getRelative(face);
			while (i <= SEARCH_OUTER_RING_RANGE && block.getType() == Material.AIR)
			{
				block = block.getRelative(face);
				i++;
			}
			locs[e] = block.getLocation();
			e++;
		}
		Location a = new Location(locs[0].getWorld(), Math.min(Math.min(locs[0].getBlockX(), locs[1].getBlockX()), Math.min(locs[2].getBlockX(), locs[3].getBlockX())),
				Math.min(Math.min(locs[0].getBlockY(), locs[1].getBlockY()), Math.min(locs[2].getBlockY(), locs[3].getBlockY())),
				Math.min(Math.min(locs[0].getBlockZ(), locs[1].getBlockZ()), Math.min(locs[2].getBlockZ(), locs[3].getBlockZ())));
		
		Location b = new Location(locs[0].getWorld(), Math.max(Math.max(locs[0].getBlockX(), locs[1].getBlockX()), Math.max(locs[2].getBlockX(), locs[3].getBlockX())),
				Math.max(Math.max(locs[0].getBlockY(), locs[1].getBlockY()), Math.max(locs[2].getBlockY(), locs[3].getBlockY())),
				Math.max(Math.max(locs[0].getBlockZ(), locs[1].getBlockZ()), Math.max(locs[2].getBlockZ(), locs[3].getBlockZ())));
		
		for (Block boxblock : UtilBlock.getInBoundingBox(a, b, true))
		{
			if (boxblock.getType() == _material.getType() && boxblock.getData() == _material.getData().getData())
				_ring.add(boxblock.getLocation());
		}
	}
	
	public void sortBlocks()
	{
		BlockFace[] direction = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST, BlockFace.UP, BlockFace.DOWN};
		
		BlockFace[][] diagonal = new BlockFace[][]{
			{BlockFace.NORTH, BlockFace.EAST}, {BlockFace.NORTH, BlockFace.WEST},
			{BlockFace.SOUTH, BlockFace.EAST}, {BlockFace.SOUTH, BlockFace.WEST},
			{BlockFace.UP, BlockFace.EAST}, {BlockFace.UP, BlockFace.WEST}, {BlockFace.UP, BlockFace.NORTH}, {BlockFace.UP, BlockFace.SOUTH},
			{BlockFace.DOWN, BlockFace.EAST}, {BlockFace.DOWN, BlockFace.WEST}, {BlockFace.DOWN, BlockFace.NORTH}, {BlockFace.DOWN, BlockFace.SOUTH}};
		
		ArrayList<Location> clone = (ArrayList<Location>) _ring.clone();
		LinkedList<Location> locs = new LinkedList<>();
		
		Location starter = clone.get(0);
		Block block = starter.getBlock();
		locs.add(block.getLocation());
		
		int e = 0;
		while (e < clone.size())
		{
			e++;
			boolean cont = false;
			for (BlockFace face : direction)
			{
				if (containsLoc(locs, block.getRelative(face).getLocation()))
					continue;
				
				if (block.getRelative(face).getType() == _material.getType()
						&& block.getRelative(face).getData() == _material.getData().getData())
				{
					block = block.getRelative(face);
					locs.add(block.getLocation());				
					cont = true;
					break;
				}
			}
			if (cont)
				continue;
			
			for (BlockFace[] first : diagonal)
			{
				Block tempBlock = block.getLocation().getBlock();
				for (BlockFace face : first)
				{
					tempBlock = tempBlock.getRelative(face);
				}
				
				if (containsLoc(locs, tempBlock.getLocation()))
					continue;
				
				if (tempBlock.getType() == _material.getType()
						&& tempBlock.getData() == _material.getData().getData())
				{
					block = tempBlock;
					locs.add(block.getLocation());
					break;
				}
			}
		}
		_sortedBorder = locs;
	}
	
	public void setupRing()
	{
		_area = new ArrayList<>();
		
		ArrayList<Location> firstLine = new ArrayList<>();
		
		BlockFace facing = null;
		for (BlockFace face : _blockFaces)
		{
			if (_location.getBlock().getRelative(face).getType() == Material.AIR)
				continue;
			
			facing = getOpposite(face);
			break;
		}
		firstLine.add(_location.clone());
		_faces[0] = facing;
		
		Block block = _location.getBlock().getRelative(facing);
		while (block.getType() == Material.AIR)
		{
			firstLine.add(block.getLocation());
			block = block.getRelative(facing);
		}
		
		_ringMiddle = firstLine.get(firstLine.size()/2);
		
		BlockFace otherFace = null;
		for (BlockFace face : _blockFaces)
		{
			if (face == getOpposite(facing) || face == facing)
				continue;

			Block middle = _ringMiddle.getBlock();
			int i = 0;
			while (middle.getType() == Material.AIR && i < MAX_RING_BOUNDS)
			{
				middle = middle.getRelative(face);
				i++;
			}
			
			if (i < MAX_RING_BOUNDS)
			{
				otherFace = face;
				break;
			}
		}
		_faces[1] = otherFace;

		BlockFace opposite = getOpposite(otherFace);
		
		int i = 1;
		for (Location loc : firstLine)
		{
			_area.add(loc.clone());
			
			Block firstBlock = loc.getBlock().getRelative(otherFace);
			while (firstBlock.getType() == Material.AIR)
			{
				_area.add(firstBlock.getLocation().clone());
				firstBlock = firstBlock.getRelative(otherFace);
			}
			if (i == 1)
				_material = new ItemStack(firstBlock.getType(), 1, firstBlock.getData(), firstBlock.getData());
			
			Block secondBlock = loc.getBlock().getRelative(opposite);
			while (secondBlock.getType() == Material.AIR)
			{
				_area.add(secondBlock.getLocation().clone());
				secondBlock = secondBlock.getRelative(opposite);
			}
			i++;
		}
		
	}
	
	private BlockFace getOpposite(BlockFace face)
	{
		if (face == BlockFace.UP) return BlockFace.DOWN;
		if (face == BlockFace.DOWN) return BlockFace.UP;
		if (face == BlockFace.NORTH) return BlockFace.SOUTH;
		if (face == BlockFace.EAST) return BlockFace.WEST;
		if (face == BlockFace.SOUTH) return BlockFace.NORTH;
		if (face == BlockFace.WEST) return BlockFace.EAST;
		
		return BlockFace.SELF;
	}
	
	@EventHandler
	public void boostPlayer(UpdateEvent event)
	{
		if (_disabled)
			return;
		
		if (event.getType() != UpdateType.TICK)
			return;
		
		for (Player player : UtilServer.getPlayers())
		{
			if (!UtilPlayer.isGliding(player))
				continue;
			
			for (Location loc : _area)
			{
				if (player.getWorld() == loc.getWorld() && UtilMath.offset(player.getLocation(), loc) < 2)
				{
					applyBoost(player);
					break;
				}
			}
		}
	}
	
	public void applyBoost(Player player)
	{
		PlayerBoostRingEvent event = UtilServer.CallEvent(new PlayerBoostRingEvent(player, _boostStrength, this));
		
		if (event.isCancelled())
			return;
		
		Vector vec = player.getEyeLocation().getDirection();
		UtilAction.velocity(player, vec.multiply(event.getStrength()));
		
		if (_host.IsAlive(player))
			UtilFirework.playFirework(player.getEyeLocation(), Type.BALL_LARGE, Color.BLUE, true, false);
	}
	
	@EventHandler
	public void ringBlockEffect(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;
		
		if (!_timer)
			return;
		
		if (!_disabled)
			return;
		
		if (isCrumbling())
			return;
		
		double blocks = (double) _ring.size() / (double)(_disabledFor/1000);
		_blocksToFill += blocks;
		
		for (int i = 0; i < _blocksToFill; i++)
		{
			if (i >= _sortedBorder.size())
				return;
				
			Block block = _sortedBorder.get(i).getBlock();
			
			if (block.getType() == _material.getType() && block.getData() == _material.getData().getData())
				continue;
			
			block.setTypeIdAndData(_material.getType().getId(), _material.getData().getData(), true);
		}
	}
	
	@EventHandler
	public void enableRing(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (!_disabled)
		{
			_hologram.stop();
			return;
		}
		
		if (_timer)
		{		
			if (!_hologram.isInUse())
				_hologram.start();
			
			_hologram.setText(UtilTime.MakeStr(_disabledSince + _disabledFor - System.currentTimeMillis()));
		}
		else
		{
			_hologram.stop();
		}
		
		if (UtilTime.elapsed(_disabledSince, _disabledFor))
			enable();
	}
	
	@EventHandler
	public void disableUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
			return;
		
		if (_delayedData == null)
			return;
		
		if (UtilTime.elapsed(_disableDelayStarted, _disableDelay))
		{
			disable(_delayedData.getTime(), _delayedData.getMaterial(), _delayedData.getData(), _delayedData.showTimer());
			_delayedData = null;
		}
	}
	
	public void disableLater(long delay, long time, Material mat, byte data, boolean showTimer)
	{
		if (_delayedData != null)
			return;
		
		_delayedData = new CooldownData(time, mat, data, showTimer);
		_disableDelayStarted = System.currentTimeMillis();
		_disableDelay = delay;
	}
	
	public void disableLater(long delay, long time, boolean showTimer)
	{
		disableLater(delay, time, Material.STAINED_CLAY, (byte) 14, showTimer);
	}
	
	public void disableLater(long delay)
	{
		disableLater(delay, Long.MAX_VALUE, Material.STAINED_CLAY, (byte) 14, false);
	}
	
	public void disable()
	{
		disable(Long.MAX_VALUE, false);
	}
	
	public void disable(long time, Material mat, byte data, boolean showTimer)
	{
		disable(time, showTimer);
		
		if (isCrumbledAway())
			return;
		
		UtilBlock.startQuickRecording();
		for (Location loc : _ring)
		{
			UtilBlock.setQuick(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), mat.getId(), data);
		}
		UtilBlock.stopQuickRecording();
	}
	
	public void disable(long time, boolean showTimer)
	{
		_timer = showTimer;
		_disabledSince = System.currentTimeMillis();
		_disabledFor = time;
		_disabled = true;
		_blocksToFill = 0;
	}
	
	public void enable()
	{
		_disabledFor = 0;
		_disabled = false;
		
		if (isCrumbledAway())
			return;
		
		UtilBlock.startQuickRecording();
		for (Location loc : _ring)
		{
			UtilBlock.setQuick(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), _material.getType().getId(), _material.getData().getData());
		}
		UtilBlock.stopQuickRecording();
	}
	
	public void setBoostStrength(float strenght)
	{
		_boostStrength = strenght;
	}
	
	public float getBoostStrength()
	{
		return _boostStrength;
	}
	
	public boolean isDisabled()
	{
		return _disabled;
	}
	
	public Location getMiddle()
	{
		return _ringMiddle;
	}
	
	public int getSize()
	{
		return SEARCH_OUTER_RING_RANGE;
	}
	
	public boolean containsLoc(LinkedList<Location> locations, Location location)
	{
		for (Location loc : locations)
		{
			if (loc.getBlockX() == location.getBlockX()
					&& loc.getBlockY() == location.getBlockY()
					&& loc.getBlockZ() == location.getBlockZ())
			{
				return true;
			}
		}
		return false;
	}
	
	public Material getType()
	{
		return _material.getType();
	}
	
	@Override
	public void crumbledAway()
	{
		if (!isDisabled())
			disable();
	}

	@Override
	public ArrayList<Location> getBlocks()
	{
		return _ring;
	}
	
	private class CooldownData
	{
		private long _time;
		private Material _mat;
		private byte _data;
		private boolean _showTimer;
		
		public CooldownData(long time, Material mat, byte data, boolean showTimer)
		{
			_time = time;
			_mat = mat;
			_data = data;
			_showTimer = showTimer;
		}
		
		public long getTime()
		{
			return _time;
		}
		
		public Material getMaterial()
		{
			return _mat;
		}
		
		public byte getData()
		{
			return _data;
		}
		
		public boolean showTimer()
		{
			return _showTimer;
		}
	}
	
}
