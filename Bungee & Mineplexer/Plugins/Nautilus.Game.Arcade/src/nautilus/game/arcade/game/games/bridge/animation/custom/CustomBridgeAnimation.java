package nautilus.game.arcade.game.games.bridge.animation.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;

import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import nautilus.game.arcade.game.games.bridge.Bridge;
import nautilus.game.arcade.game.games.bridge.animation.BridgeAnimation;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public abstract class CustomBridgeAnimation extends BridgeAnimation
{

	private static final String BLOCK_IDS_KEY = "BLOCK_IDS";
	private static final String RATE_KEY = "RATE";
	private static final String PARTICLE_KEY = "PARTICLE";
	private static final String SOUND_KEY = "SOUND";
	private static final String REGION_KEY = "BRIDGE";
	
	protected final BlockRestore _restore;
	
	private final String _typeKey;
	protected final Map<Location, Double> _bridgeBlocks;
	
	// Configuration
	protected List<Integer> _blockIds;
	protected int _rate;
	protected ParticleType _particle;

	// Sound
	protected Sound _sound;
	protected float _pitch;
	
	protected double _maxDistance;
	
	public CustomBridgeAnimation(Bridge bridge, String typeKey)
	{
		super(bridge);

		_restore = bridge.getArcadeManager().GetBlockRestore();
		
		_typeKey = typeKey;
		_bridgeBlocks = new HashMap<>(AVERAGE_BRIDGE_BLOCKS);
		
		// Defaults
		// Wood, Logs, Fences
		_blockIds = Arrays.asList(5, 17, 85);
		_rate = 1;
		_particle = ParticleType.BLOCK_DUST;
	}

	@Override
	public void onParse()
	{
		List<Integer> blockIds = new ArrayList<>();

		for (String key : _worldData.GetAllCustomLocs().keySet())
		{
			String[] split = key.split(" ");
			String subKey = split[0];

			if (split.length < 2)
			{
				continue;
			}

			switch (subKey)
			{

			case BLOCK_IDS_KEY:
				// Set the block ids that the animation will use

				for (int i = 1; i < split.length; i++)
				{
					try
					{
						blockIds.add(Integer.parseInt(split[i]));
					}
					catch (NumberFormatException e)
					{
						continue;
					}
				}

				break;

			case RATE_KEY:
				// Set the rate at which the animation will run at

				try
				{
					_rate = Integer.parseInt(split[1]);
				}
				catch (NumberFormatException e)
				{
					continue;
				}

				break;

			case PARTICLE_KEY:
				// Set which type of particle will be displayed when a block
				// spawns

				try
				{
					_particle = ParticleType.valueOf(split[1]);
				}
				catch (IllegalArgumentException e)
				{
					continue;
				}

				break;

			case SOUND_KEY:
				// Set the sound and pitch that will be played when a block
				// spawns

				if (split.length < 3)
				{
					continue;
				}

				try
				{
					_sound = Sound.valueOf(split[1]);
				}
				catch (IllegalArgumentException e)
				{
					continue;
				}

				try
				{
					_pitch = Float.parseFloat(split[2]);
				}
				catch (NumberFormatException e)
				{
					continue;
				}

				break;

			default:
				break;
			}
		}
		
		// Set configuration values
		_blockIds = blockIds;
		
		// Save all blocks in a big map.
		for (String key : _worldData.GetAllCustomLocs().keySet())
		{
			if (!key.startsWith(REGION_KEY))
			{
				continue;
			}

			List<Location> locations = _worldData.GetCustomLocs(key);

			if (locations.size() < 2)
			{
				continue;
			}

			for (Location location : locations)
			{
				location.getChunk().load();
			}

			for (Block block : UtilBlock.getInBoundingBox(locations.get(0), locations.get(1)))
			{
				if (!_blockIds.contains(block.getTypeId()) || _bridgeBlocks.containsKey(block))
				{
					continue;
				}

				double dist = UtilMath.offset2d(block.getLocation(), _bridge.GetSpectatorLocation());

				if (dist > _maxDistance)
				{
					_maxDistance = dist;
				}

				int toSet = Material.AIR.getId();

				for (Block surround : getNextBlocks(block))
				{
					if (surround.getType() == Material.WATER || surround.getType() == Material.STATIONARY_WATER)
					{
						toSet = Material.STATIONARY_WATER.getId();
						break;
					}
				}

				_restore.add(block, toSet, (byte) 0, Integer.MAX_VALUE);
				_bridgeBlocks.put(block.getLocation(), dist);
			}
		}
	}

	public void onBlockSet(Block block)
	{
		World world = _worldData.World;
		Location location = block.getLocation().add(0.5, 0.5, 0.5);

		if (_particle != null)
		{
			if (_particle == ParticleType.BLOCK_DUST)
			{
				world.playEffect(location, Effect.STEP_SOUND, block.getType(), block.getData());
			}
			else
			{
				UtilParticle.PlayParticleToAll(_particle, block.getLocation(), 0.5F, 0.5F, 0.5F, 0.5F, 5, ViewDist.NORMAL);
			}
		}
		
		if (_sound != null)
		{
			world.playSound(location, _sound, 1, _pitch);
		}
	}
	
	public final String getTypeKey()
	{
		return _typeKey;
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		
		builder.append("Type: " + _typeKey).append("\n");
		builder.append("Bridge Blocks: " +  _bridgeBlocks.size()).append("\n");
		builder.append("Block Ids: " + _blockIds).append("\n");
		builder.append("Rate: " + _rate).append("\n");
		builder.append("Particle: " + (_particle == null ? "Null" : _particle.getFriendlyName())).append("\n");
		builder.append("Sound: " + (_sound == null ? "Null" : _sound.toString() + " Pitch: " + _pitch)).append("\n");
		builder.append("Max Distance: " + _maxDistance).append("\n");

		return builder.toString();
	}

	private List<Block> getNextBlocks(Block block)
	{
		List<Block> blocks = new ArrayList<>(4);

		blocks.add(block.getRelative(BlockFace.NORTH));
		blocks.add(block.getRelative(BlockFace.EAST));
		blocks.add(block.getRelative(BlockFace.SOUTH));
		blocks.add(block.getRelative(BlockFace.WEST));

		return blocks;
	}

}
