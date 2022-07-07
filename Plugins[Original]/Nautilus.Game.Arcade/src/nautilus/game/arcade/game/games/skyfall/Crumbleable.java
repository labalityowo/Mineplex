package nautilus.game.arcade.game.games.skyfall;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.PacketPlayOutMapChunk;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.entity.Player;

import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;

/**
 * Crumbleable is a Superclass to create decayable/crumleable Objects like Sky Islands
 *
 * @author xXVevzZXx
 */
public abstract class Crumbleable
{
	private static final long CHUNK_CRUMBLE_DELAY = 1000;
	
	private boolean _crumble;
	
	private ArrayList<Location> _initBlocks;
	private ArrayList<Location> _realBlocks;
	private Map<Chunk, BitSet> _chunksToUpdate;
	
	private boolean _onlyTop;
	private int _height;
	
	private long _lastChunk;
	
	public Crumbleable()
	{
		this(false, 0);
	}
	
	public Crumbleable(boolean onlyTop, int height)
	{
		_onlyTop = onlyTop;
		_height = height;
		
		_realBlocks = new ArrayList<>();
		_chunksToUpdate = new HashMap<>();
		
		_lastChunk = System.currentTimeMillis();
	}
	
	/**
	 * Must be called when getBlocks is set
	 */
	public void init()
	{
		if (_onlyTop)
		{
			ArrayList<Location> flatMap = new ArrayList<>();
			ArrayList<Location> locs = getBlocks();
			int y = Integer.MIN_VALUE;
			for (Location loc : locs)
			{
				if (loc.getBlockY() > y)
					y = loc.getBlockY();
			}
			
			for (Location loc : locs)
			{
				if (loc.getBlockY() == y)
					flatMap.add(loc.clone());
			}
			
			for (Location loc : flatMap)
			{
				Block block = loc.getBlock();
				
				int i = 0;
				
				while (i <= _height)
				{
					if (block.getType() != Material.AIR && block.getRelative(BlockFace.UP).getType() == Material.AIR)
						_realBlocks.add(block.getLocation());
						
					block = block.getRelative(BlockFace.DOWN);
					i++;
				}
			}
			_initBlocks = (ArrayList<Location>) _realBlocks.clone();
		}
		else
		{
			_realBlocks = (ArrayList<Location>) getBlocks().clone();
			_initBlocks = (ArrayList<Location>) getBlocks().clone();
		}
		
	}

	/**
	 * @see #crumble(int, Material...)
	 */
	public boolean crumble(int blocks)
	{
		return crumble(blocks, Material.AIR);
	}
	
	/**
	 * Lets the island crumble with the provided rate. <br/>
	 * <br/>
	 * Will call {@link #crumbledAway()} in subclasses if there are no blocks left to crumble.
	 * <br/>
	 * 
	 * @param blocks blocks to crumble per call
	 * @param replacements blocks which will replace old blocks
	 * 
	 * @return true if island is comepletely crumbled away
	 */
	public boolean crumble(int blocks, Material... replacements)
	{
		_crumble = true;
			
		crumblePercentage();
		
		if (_realBlocks.isEmpty())
		{
			crumbledAway();
			if (!_chunksToUpdate.isEmpty())
			{
				if (UtilTime.elapsed(_lastChunk, CHUNK_CRUMBLE_DELAY))
				{
					Chunk chunk = _chunksToUpdate.keySet().iterator().next();
					BitSet bitSet = _chunksToUpdate.remove(chunk);

					int mask = 0;

					for (int i = 0; i < bitSet.length(); i++)
					{
						if (bitSet.get(i))
						{
							mask |= 1 << i;
						}
					}

					for (Player player : UtilServer.getPlayers())
					{
						int protocol = UtilPlayer.getProtocol(player);
						UtilPlayer.sendPacket(player, new PacketPlayOutMapChunk(protocol, chunk, false, mask));
					}

					_lastChunk = System.currentTimeMillis();
				}
			}
			return true;
		}
			
		for (int i = 0; i < blocks; i++)
		{
			Material material = replacements[UtilMath.r(replacements.length)];
			if (_realBlocks.isEmpty())
			{
				crumbledAway();
				return true;
			}
			
			Location toRemove =	_realBlocks.remove(UtilMath.r(_realBlocks.size()));
			if (toRemove.getBlock().getType() == Material.CHEST && _realBlocks.size() > 25)
			{
				_realBlocks.add(toRemove);
				continue;
			}
			
			if (toRemove.getBlock().getType() == Material.AIR
					|| toRemove.getBlock().getType() == Material.WATER
					|| toRemove.getBlock().getType() == Material.STATIONARY_WATER
					|| toRemove.getBlock().getType() == Material.LAVA
					|| toRemove.getBlock().getType() == Material.STATIONARY_LAVA)
				continue;
			
			byte id = 0;
			
			if (toRemove.getBlock().getType() == Material.STAINED_GLASS ||
					toRemove.getBlock().getType() == Material.GLASS)
			{
				material = Material.STAINED_GLASS;
				id = DyeColor.BLACK.getData();
			}
			
			MapUtil.ChunkBlockChange(toRemove, material.getId(), id, false);
			_chunksToUpdate.computeIfAbsent(((CraftChunk) toRemove.getChunk()).getHandle(), key -> new BitSet()).set(((int) toRemove.getY()) >> 4);
		}
		
		return false;
	}
	
	public boolean isCrumbling()
	{
		return _crumble;
	}
	
	public boolean isCrumbledAway()
	{
		return _realBlocks.isEmpty();
	}
	
	/**
	 * @return the percentage of the crumbeled blocks.
	 */
	public double crumblePercentage()
	{	
		try 
		{
			return (_realBlocks.size()/(_initBlocks.size()));
		}
		catch (Exception e)
		{
			return 1;
		}
	}
	
	public ArrayList<Location> getRealBlocks()
	{
		return _realBlocks;
	}
	
	/**
	 * Overrideable method which is called by 
	 * {@link #crumble(int, Material...)} or {@link #crumble(int)} 
	 * when there are no more blocks left to crumble.
	 */
	public void crumbledAway() {}
	
	public abstract ArrayList<Location> getBlocks();
	
}
