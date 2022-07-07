package mineplex.core.explosion;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import mineplex.core.MiniPlugin;
import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class Explosion extends MiniPlugin
{
	private boolean _regenerateGround;
	private long _regenerateTime;
	private boolean _temporaryDebris = true;
	private boolean _enableDebris = false;
	private boolean _tntSpread;
	private boolean _liquidDamage = true;
	private HashSet<FallingBlock> _explosionBlocks = new HashSet<FallingBlock>();

	private BlockRestore _blockRestore;

	public Explosion(JavaPlugin plugin, BlockRestore blockRestore) 
	{
		super("Block Restore", plugin);

		_blockRestore = blockRestore;

		resetConfiguration();
	}

	@EventHandler
	public void ExplosionPrime(ExplosionPrimeEvent event)
	{
		if (event.getRadius() >= 5)
			return;

		if (_liquidDamage)
			for (Block block : UtilBlock.getInRadius(event.getEntity().getLocation(), (double) event.getRadius()).keySet())
				if (block.isLiquid())
					block.setTypeId(0);
	}

	@EventHandler
	public void ExplosionEntity(EntityExplodeEvent event)
	{
		if (event.isCancelled())
			return;

		try
		{
			if (event.getEntityType() == EntityType.CREEPER)
				event.blockList().clear();

			if (event.getEntityType() == EntityType.WITHER_SKULL)
				event.blockList().clear();
		}
		catch(Exception e)
		{
			//Nothing
		}

		if (event.blockList().isEmpty())
			return;

		// This metadata is used to identify the owner of the explosion for use in other plugins
		Player owner = null;
		Entity entity = event.getEntity();
		if (entity.hasMetadata("owner"))
		{
			FixedMetadataValue ownerData = (FixedMetadataValue) entity.getMetadata("owner").get(0);
			UUID ownerUUID = (UUID) ownerData.value();

			owner = UtilPlayer.searchExact(ownerUUID);
		}

		//Event for Block awareness
		ExplosionEvent explodeEvent = new ExplosionEvent(event.blockList(), owner);
		_plugin.getServer().getPluginManager().callEvent(explodeEvent);

		event.setYield(0f);

		//Save
		final HashMap<Block, Entry<Integer, Byte>> blocks = new HashMap<Block, Entry<Integer, Byte>>();

		for (Block cur : event.blockList())
		{
			if (cur.getTypeId() == 0 || cur.isLiquid())
				continue;

			if (cur.getType() == Material.CHEST ||
					cur.getType() == Material.IRON_ORE ||
					cur.getType() == Material.COAL_ORE ||
					cur.getType() == Material.GOLD_ORE ||
					cur.getType() == Material.DIAMOND_ORE)
			{
				cur.breakNaturally();
				continue;
			}

			blocks.put(cur, new AbstractMap.SimpleEntry<Integer, Byte>(cur.getTypeId(), cur.getData()));

			if (!_regenerateGround)
			{
				if (cur.getTypeId() != 98 || (cur.getData() != 0 && cur.getData() != 3))
					cur.setTypeId(0);
			}

			else
			{
				int heightDiff = cur.getLocation().getBlockY() - event.getEntity().getLocation().getBlockY();
				_blockRestore.add(cur, 0, (byte) 0, (long) (_regenerateTime + (heightDiff * 3000) + Math.random() * 2900));
			}

		}

		event.blockList().clear();

		//DELAY

		final Entity fEnt = event.getEntity();
		final Location fLoc = event.getLocation();
		_plugin.getServer().getScheduler().runTaskLater(_plugin, new Runnable()
		{
			public void run()
			{
				//Launch
				for (Block cur : blocks.keySet())
				{
					if (blocks.get(cur).getKey() == 98)
						if (blocks.get(cur).getValue() == 0 || blocks.get(cur).getValue() == 3)
							continue;

					//TNT
					if (_tntSpread && blocks.get(cur).getKey() == 46)
					{
						TNTPrimed ent  = cur.getWorld().spawn(cur.getLocation().add(0.5, 0.5, 0.5), TNTPrimed.class);
						Vector vec =  UtilAlg.getTrajectory(fEnt, ent);
						if (vec.getY() < 0)			vec.setY(vec.getY() * -1);

						UtilAction.velocity(ent, vec, 1, false, 0, 0.6, 10, false);

						ent.setFuseTicks(10);
					}
					//Other
					else
					{
						//XXX ANTILAG
						double chance = 0.85  + (double)_explosionBlocks.size()/(double)500;
						if (Math.random() > Math.min(0.975, chance))
						{
							FallingBlock fall = cur.getWorld().spawnFallingBlock(cur.getLocation().add(0.5, 0.5, 0.5), blocks.get(cur).getKey(), blocks.get(cur).getValue());
							fall.setDropItem(false);

							Vector vec =  UtilAlg.getTrajectory(fEnt, fall);
							if (vec.getY() < 0)			vec.setY(vec.getY() * -1);

							UtilAction.velocity(fall, vec,	0.5 + 0.25*Math.random(), false, 0, 0.4 + 0.20*Math.random(), 10, false);

							_explosionBlocks.add(fall); 
						}	
					}	
				}

				//Items 
				/**
				for (Item item : event.getEntity().getWorld().getEntitiesByClass(Item.class))
					if (UtilMath.offset(item, event.getEntity()) < 5)
					{
						Vector vec =  UtilAlg.getTrajectory(event.getEntity(), item);
						if (vec.getY() < 0)			vec.setY(vec.getY() * -1);

						UtilAction.velocity(item, vec,	1, false, 0, 0.6, 10, false);
					}
				 **/

				//Crack
				for (Block cur : UtilBlock.getInRadius(fLoc, 4d).keySet())
					if (cur.getTypeId() == 98)
						if (cur.getData() == 0 || cur.getData() == 3)
							cur.setTypeIdAndData(98, (byte)2, true);
			}
		}, 1);	
	}

	@EventHandler
	public void ExplosionChangeBlock(EntityChangeBlockEvent event)
	{
		if (_explosionBlocks.contains(event.getEntity()))
			event.setCancelled(true);
	}

	@EventHandler
	public void ExplosionBlockUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		//Clean Archived Games
		Iterator<FallingBlock> fallingIterator = _explosionBlocks.iterator();

		while (fallingIterator.hasNext())
		{	
			FallingBlock cur = fallingIterator.next();

			if (cur.isDead() || !cur.isValid() || cur.getTicksLived() > 400 || !cur.getWorld().isChunkLoaded(cur.getLocation().getBlockX() >> 4, cur.getLocation().getBlockZ() >> 4))
			{
				fallingIterator.remove();

				//Expire
				if (cur.getTicksLived() > 100 || !cur.getWorld().isChunkLoaded(cur.getLocation().getBlockX() >> 4, cur.getLocation().getBlockZ() >> 4))
				{
					cur.remove();
					return;
				}

				Block block = cur.getLocation().getBlock();
				//block.setTypeIdAndData(0, (byte)0, true);

				//Block Replace
				if (_enableDebris)
				{
					if (_temporaryDebris)
					{
						_blockRestore.add(block, cur.getBlockId(), cur.getBlockData(), 10000);
					}
					else
					{
						block.setTypeIdAndData(cur.getBlockId(), cur.getBlockData(), true);
					}
				}
				else
				{
					cur.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, cur.getBlockId());
				}

				cur.remove();
			}	
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void ExplosionBlocks(EntityExplodeEvent event)
	{
		if (event.getEntity() == null)
			event.blockList().clear();	
	}

	public void SetRegenerate(boolean regenerate)
	{
		_regenerateGround = regenerate;
	}

	public void setRegenerateTime(long time)
	{
		_regenerateTime = time;
	}

	public void SetDebris(boolean value)
	{
		_enableDebris = value;
	}
	
	public void SetLiquidDamage(boolean value)
	{
		_liquidDamage = value;
	}
	
	public void SetTNTSpread(boolean value)
	{
		_tntSpread = value;
	}

	public void SetTemporaryDebris(boolean value)
	{
		_temporaryDebris = value;
	}

	public void resetConfiguration()
	{
		_regenerateGround = false;
		_regenerateTime = TimeUnit.SECONDS.toMillis(20);
		_tntSpread = true;
		_temporaryDebris = true;
	}

	public HashSet<FallingBlock> GetExplosionBlocks() 
	{
		return _explosionBlocks;
	}

	public void BlockExplosion(Collection<Block> blockSet, Location mid, boolean onlyAbove)
	{
		BlockExplosion(blockSet, mid, onlyAbove, true);
	}

	public void BlockExplosion(Collection<Block> blockSet, Location mid, boolean onlyAbove, boolean removeBlock)
	{
		if (blockSet.isEmpty())
			return;

		int lowestY = Integer.MAX_VALUE;

		for (Block block : blockSet)
		{
			int y = block.getLocation().getBlockY();

			if (y < lowestY)
			{
				lowestY = y;
			}
		}

		//Save
		final HashMap<Block, Entry<Integer, Byte>> blocks = new HashMap<>();

		for (Block cur : blockSet)
		{
			if (cur.getTypeId() == 0 || onlyAbove && cur.getY() < mid.getY())
				continue;
			
			blocks.put(cur, new AbstractMap.SimpleEntry<>(cur.getTypeId(), cur.getData()));

			if (removeBlock)
			{
				if (_regenerateGround)
				{
					_blockRestore.add(cur, 0, (byte) 0, (long) (_regenerateTime + ((cur.getLocation().getBlockY() - lowestY) * 3000) + (Math.random() * 1500)));
				}
				else
				{
					cur.setType(Material.AIR);
				}
			}
		}

		//DELAY
		final Location fLoc = mid;
		_plugin.getServer().getScheduler().runTaskLater(_plugin, new Runnable()
		{
			public void run()
			{
				//Launch
				for (Block cur : blocks.keySet())
				{
					if (blocks.get(cur).getKey() == 98)
						if (blocks.get(cur).getValue() == 0 || blocks.get(cur).getValue() == 3)
							continue;

					double chance = 0.2  + (double)_explosionBlocks.size()/(double)80;
					if (Math.random() > Math.min(0.98, chance))
					{
						FallingBlock fall = cur.getWorld().spawnFallingBlock(cur.getLocation().add(0.5, 0.5, 0.5), blocks.get(cur).getKey(), blocks.get(cur).getValue());
						fall.setDropItem(false);

						Vector vec =  UtilAlg.getTrajectory(fLoc, fall.getLocation());
						if (vec.getY() < 0)			vec.setY(vec.getY() * -1);

						UtilAction.velocity(fall, vec,	0.5 + 0.25*Math.random(), false, 0, 0.4 + 0.20*Math.random(), 10, false);

						_explosionBlocks.add(fall);
					}
				}
			}
		}, 1);	
	}

	private void addToRegenerate(Block block)
	{

	}

	public void setEnabled(boolean var)
	{
		if (var)
		{
			registerSelf();
		}
		else
		{
			deregisterSelf();
		}
	}
}
