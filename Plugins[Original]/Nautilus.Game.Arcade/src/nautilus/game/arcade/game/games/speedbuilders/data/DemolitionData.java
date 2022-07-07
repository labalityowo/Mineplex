package nautilus.game.arcade.game.games.speedbuilders.data;

import java.util.ArrayList;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Bed;
import org.bukkit.material.Door;

import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilTime;
import mineplex.core.hologram.Hologram;

public class DemolitionData
{

	public RecreationData Parent;

	public NautHashMap<Block, BlockState> Blocks;
	public ArrayList<Entity> Mobs;

	public long Start;

	private Hologram _hologram;

	private boolean _flickerAir = true;
	private long _lastFlicker = System.currentTimeMillis();

	public DemolitionData(RecreationData parent, ArrayList<Block> blocks, ArrayList<Entity> mobs)
	{
		Parent = parent;
		
		Blocks = new NautHashMap<Block, BlockState>();
		Mobs = mobs;
		
		for (Block block : blocks)
		{
			Blocks.put(block, block.getState());
		}
		
		Start = System.currentTimeMillis();
		
		spawnHologram();
	}

	public void spawnHologram()
	{
		if (Parent.Game.InstaBreak)
			return;
		
		Location loc = Parent.getMidpoint();
		
		if (!Blocks.isEmpty())
			loc = Blocks.keySet().iterator().next().getLocation().add(0.5, 0.5, 0.5);
		else if (!Mobs.isEmpty())
			loc = UtilAlg.Random(Mobs).getLocation().add(0, 1, 0);
		
		_hologram = new Hologram(Parent.Game.Manager.getHologramManager(), loc, "3");
		
		_hologram.start();
	}

	public void despawnHologram()
	{
		if (_hologram == null)
			return;
		
		_hologram.stop();
		
		_hologram = null;
	}

	public void update()
	{
		if (Parent.Game.InstaBreak)
		{
			breakBlocks();
			
			return;
		}
		
		if (_hologram == null)
			spawnHologram();
		
		int secondsLeft = (int) Math.ceil((3000 - (System.currentTimeMillis() - Start)) / 1000.0D);
		
		if (secondsLeft < 0)
			secondsLeft = 0;
			
		_hologram.setText("" + secondsLeft);
		
		if (UtilTime.elapsed(_lastFlicker, 500))
		{
			_lastFlicker = System.currentTimeMillis();
			
			for (Block block : Blocks.keySet())
			{
				if (_flickerAir)
					MapUtil.QuickChangeBlockAt(block.getLocation(), Material.AIR);
				else
					Blocks.get(block).update(true, false);
			}
			
			for (Entity entity : Mobs)
			{
				if (_flickerAir)
					UtilEnt.ghost(entity, true, true);
				else
					UtilEnt.ghost(entity, true, false);
			}
			
			_flickerAir = !_flickerAir;
		}
		
		if (secondsLeft == 0)
			breakBlocks();
	}

	public void cancelBreak()
	{
		despawnHologram();

		for (Block block : Blocks.keySet())
		{
			Blocks.get(block).update(true, false);
		}
		
		for (Entity entity : Mobs)
		{
			UtilEnt.ghost(entity, true, false);
		}

		Parent.BlocksForDemolition.remove(this);
	}

	public void breakBlocks()
	{
		despawnHologram();
		
		//Effect will play for all blocks even two-parted ones
		for (Block block : Blocks.keySet())
		{
			Blocks.get(block).update(true, false);
			
			block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getTypeId());
		}
		
		for (Block block : Blocks.keySet())
		{
			if (block.getType() == Material.AIR)
				continue;
			
			//Ignore top double plant blocks
			if (block.getType() == Material.DOUBLE_PLANT)
			{
				if (block.getData() > 7)
					continue;
			}
			
			for (ItemStack itemStack : UtilBlock.blockToInventoryItemStacks(block))
			{
				UtilInv.insert(Parent.Player, itemStack);
			}
			
			//Destroy the other part
			if (block.getType() == Material.BED_BLOCK)
			{
				Bed bed = (Bed) block.getState().getData();
				
				if (bed.isHeadOfBed())
					MapUtil.QuickChangeBlockAt(block.getRelative(bed.getFacing().getOppositeFace()).getLocation(), Material.AIR);
				else
					MapUtil.QuickChangeBlockAt(block.getRelative(bed.getFacing()).getLocation(), Material.AIR);
			}
			else if (block.getType() == Material.WOODEN_DOOR || block.getType() == Material.IRON_DOOR_BLOCK || block.getType() == Material.SPRUCE_DOOR || block.getType() == Material.BIRCH_DOOR || block.getType() == Material.JUNGLE_DOOR || block.getType() == Material.ACACIA_DOOR || block.getType() == Material.DARK_OAK_DOOR)
			{
				Door door = (Door) block.getState().getData();
				
				if (door.isTopHalf())
					MapUtil.QuickChangeBlockAt(block.getRelative(BlockFace.DOWN).getLocation(), Material.AIR);
				else
					MapUtil.QuickChangeBlockAt(block.getRelative(BlockFace.UP).getLocation(), Material.AIR);
			}
			else if (block.getType() == Material.DOUBLE_PLANT)
			{
				//The top block does not carry the correct data
				if (block.getData() <= 7)
					MapUtil.QuickChangeBlockAt(block.getRelative(BlockFace.UP).getLocation(), Material.AIR);
				else
					MapUtil.QuickChangeBlockAt(block.getLocation(), Material.AIR);
			}
			
			MapUtil.QuickChangeBlockAt(block.getLocation(), Material.AIR);
		}
		
		for (Entity entity : Mobs)
		{
			ItemStack spawnEgg = new ItemStack(Material.MONSTER_EGG, 1, entity.getType().getTypeId());
			
			UtilInv.insert(Parent.Player, spawnEgg);
			
			entity.remove();
			
			Parent.Mobs.remove(entity);
		}
		
		Parent.BlocksForDemolition.remove(this);
		
		Parent.Game.checkPerfectBuild(Parent.Player);
	}

}
