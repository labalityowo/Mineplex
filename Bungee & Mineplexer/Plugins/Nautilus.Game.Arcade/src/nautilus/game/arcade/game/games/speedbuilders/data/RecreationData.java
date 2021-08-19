package nautilus.game.arcade.game.games.speedbuilders.data;

import java.util.ArrayList;
import java.util.List;

import mineplex.core.common.util.C;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilInv;
import mineplex.core.hologram.Hologram;
import nautilus.game.arcade.game.games.speedbuilders.SpeedBuilders;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.BlockStairs;
import net.minecraft.server.v1_8_R3.BlockStairs.EnumStairShape;
import net.minecraft.server.v1_8_R3.IBlockData;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Bed;
import org.bukkit.material.Door;
import org.bukkit.material.Stairs;
import org.bukkit.util.Vector;

public class RecreationData
{

	public SpeedBuilders Game;

	public Player Player;

	public BlockState[][] DefaultGround;

	public Location OriginalBuildLocation;

	public Location CornerA;
	public Location CornerB;

	public Location PlayerSpawn;

	public NautHashMap<Item, Long> DroppedItems = new NautHashMap<Item, Long>();

	public ArrayList<DemolitionData> BlocksForDemolition = new ArrayList<DemolitionData>();

	public ArrayList<Entity> Mobs = new ArrayList<Entity>();

	private Hologram _hologram;

	public RecreationData(SpeedBuilders game, Player player, Location loc, Location playerSpawn)
	{
		Game = game;
		
		DefaultGround = new BlockState[game.BuildSize][game.BuildSize];
		
		Player = player;
		
		OriginalBuildLocation = loc;
		
		CornerA = loc.clone().subtract(game.BuildSizeDiv2, 0, game.BuildSizeDiv2);
		CornerB = loc.clone().add(game.BuildSizeDiv2, game.BuildSizeMin1, game.BuildSizeDiv2);
		
		PlayerSpawn = playerSpawn;
		
		for (int x = 0; x < game.BuildSize; x++)
		{
			for (int z = 0; z < game.BuildSize; z++)
			{
				DefaultGround[x][z] = CornerA.clone().add(x, -1, z).getBlock().getState();
			}
		}

		Vector mid = game.getJudgeSpawn().toVector().subtract(loc.toVector()).multiply(0.4);
		Location hologramLocation = loc.clone().add(mid).add(0, 1, 0);
		Location above = loc.clone().add(0.5, game.BuildSize + 0.5, 0.5);
		_hologram = new Hologram(game.getArcadeManager().getHologramManager(), hologramLocation, C.cYellow + player.getName());
		_hologram.start();
	}

	public boolean inBuildArea(Block block) 
	{
		if (block.getX() < Math.min(CornerA.getBlockX(), CornerB.getBlockX()))
			return false;
		
		if (block.getY() < Math.min(CornerA.getBlockY(), CornerB.getBlockY()))
			return false;
		
		if (block.getZ() < Math.min(CornerA.getBlockZ(), CornerB.getBlockZ()))
			return false;
		
		if (block.getX() > Math.max(CornerA.getBlockX(), CornerB.getBlockX()))
			return false;
		
		if (block.getY() > Math.max(CornerA.getBlockY(), CornerB.getBlockY()))
			return false;
		
		if (block.getZ() > Math.max(CornerA.getBlockZ(), CornerB.getBlockZ()))
			return false;
		
		return true;
	}

	public boolean inBuildArea(Location loc)
	{
		if (loc.getX() < Math.min(CornerA.getBlockX(), CornerB.getBlockX()))
			return false;
		
		if (loc.getY() < Math.min(CornerA.getBlockY(), CornerB.getBlockY()))
			return false;
		
		if (loc.getZ() < Math.min(CornerA.getBlockZ(), CornerB.getBlockZ()))
			return false;
		
		if (loc.getX() > Math.max(CornerA.getBlockX(), CornerB.getBlockX()) + 1)
			return false;
		
		if (loc.getY() > Math.max(CornerA.getBlockY(), CornerB.getBlockY()) + 1)
			return false;
		
		if (loc.getZ() > Math.max(CornerA.getBlockZ(), CornerB.getBlockZ()) + 1)
			return false;
		
		return true;
	}

	public void clearBuildArea(boolean resetGround)
	{
		for (Block block : getBlocks())
		{
			MapUtil.QuickChangeBlockAt(block.getLocation(), Material.AIR);
		}
		
		for (Entity entity : Mobs)
		{
			entity.remove();
		}
		
		Mobs.clear();
		
		if (resetGround)
		{
			for (int x = 0; x < Game.BuildSize; x++)
			{
				for (int z = 0; z < Game.BuildSize; z++)
				{
					MapUtil.QuickChangeBlockAt(CornerA.clone().add(x, -1, z), DefaultGround[x][z].getType(), DefaultGround[x][z].getRawData());
				}
			}
		}
	}

	public void pasteBuildData(BuildData buildData)
	{
		clearBuildArea(true);
		
		for (int x = 0; x < Game.BuildSize; x++)
		{
			for (int z = 0; z < Game.BuildSize; z++)
			{
				MapUtil.QuickChangeBlockAt(CornerA.clone().add(x, -1, z), buildData.Ground[x][z].getType(), buildData.Ground[x][z].getRawData());
			}
		}
		
		for (int x = 0; x < Game.BuildSize; x++)
		{
			for (int y = 0; y < Game.BuildSize; y++)
			{
				for (int z = 0; z < Game.BuildSize; z++)
				{
					MapUtil.QuickChangeBlockAt(CornerA.clone().add(x, y, z), buildData.Build[x][y][z].getType(), buildData.Build[x][y][z].getRawData());
				}
			}
		}
		
		Game.CreatureAllowOverride = true;
		
		for (MobData mobData : buildData.Mobs)
		{
			Location loc = CornerA.clone().add(mobData.DX + 0.5, mobData.DY, mobData.DZ + 0.5);
			
			Entity entity = loc.getWorld().spawnEntity(loc, mobData.EntityType);
			
			UtilEnt.vegetate(entity, true);
			UtilEnt.ghost(entity, true, false);
			
			Mobs.add(entity);
		}
		
		Game.CreatureAllowOverride = false;
	}

	public void breakAndDropItems()
	{
		for (Block block : getBlocks())
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
				UtilInv.insert(Player, itemStack);
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
		}
		
		for (Entity entity : Mobs)
		{
			ItemStack spawnEgg = new ItemStack(Material.MONSTER_EGG, 1, entity.getType().getTypeId());

			UtilInv.insert(Player, spawnEgg);
		}
				
		CornerA.getWorld().playEffect(getMidpoint(), Effect.STEP_SOUND, Material.LOG.getId());
		
		clearBuildArea(false);
	}

	public boolean isEmptyBuild(BuildData buildData)
	{
		for (Block block : getBlocks())
		{
			if (block.getType() != Material.AIR)
				return false;
		}
		
		if (!buildData.Mobs.isEmpty())
			return Mobs.isEmpty();
		
		return true;
	}

	public int calculateScoreFromBuild(BuildData buildData)
	{
		int score = 0; 
		
		for (int x = 0; x < Game.BuildSize; x++)
		{
			for (int y = 0; y < Game.BuildSize; y++)
			{
				for (int z = 0; z < Game.BuildSize; z++)
				{
					Block currentBlock = CornerA.clone().add(x, y, z).getBlock();
					BlockState expectedState = buildData.Build[x][y][z];
					
					if (expectedState.getType() == Material.AIR)
						continue;
					
					if (expectedState.getType() == currentBlock.getType() && expectedState.getRawData() == currentBlock.getData())
					{
						score++;
						continue;
					}
					
					//Ender portal direction fix & 0x4 is a check to see if the frame has an ender eye in it
					if (currentBlock.getType() == Material.ENDER_PORTAL_FRAME && expectedState.getType() == Material.ENDER_PORTAL_FRAME)
					{
						if ((currentBlock.getData() & 0x4) == (expectedState.getRawData() & 0x4))
							score++;
					}
					
					//Sapling growth fix
					if (expectedState.getType() == Material.SAPLING && currentBlock.getType() == Material.SAPLING)
					{	
						if (currentBlock.getData() % 8 == expectedState.getRawData() % 8)
							score++;
					}
					
					//Fix for leaves decay flags
					if ((expectedState.getType() == Material.LEAVES && currentBlock.getType() == Material.LEAVES) || ((expectedState.getType() == Material.LEAVES_2 && currentBlock.getType() == Material.LEAVES_2)))
					{
						if (currentBlock.getData() % 4 == expectedState.getRawData() % 4)
							score++;
					}
					
					//Fix for anvil facing direction
					if (expectedState.getType() == Material.ANVIL && currentBlock.getType() == Material.ANVIL)
					{
						if (currentBlock.getData() / 4 == expectedState.getRawData() / 4 && currentBlock.getData() % 2 == expectedState.getRawData() % 2)
							score++;
					}
					
					//Fix for glowing redstone ore
					if ((expectedState.getType() == Material.REDSTONE_ORE && currentBlock.getType() == Material.GLOWING_REDSTONE_ORE) || (expectedState.getType() == Material.GLOWING_REDSTONE_ORE && currentBlock.getType() == Material.REDSTONE_ORE))
					{
						score++;
					}
					
					//Fix for corner stair shape
					if (currentBlock.getState().getData() instanceof Stairs && expectedState.getData() instanceof Stairs)
					{
						net.minecraft.server.v1_8_R3.Block nmsBlock = CraftMagicNumbers.getBlock(currentBlock);
						
						IBlockData blockData = nmsBlock.getBlockData();
						blockData = nmsBlock.updateState(blockData, ((CraftWorld) currentBlock.getWorld()).getHandle(), new BlockPosition(currentBlock.getX(), currentBlock.getY(), currentBlock.getZ()));
						
						EnumStairShape expectedShape = buildData.StairShapes[x][y][z];
						EnumStairShape currentShape = blockData.get(BlockStairs.SHAPE);
						
						if ((expectedShape == EnumStairShape.INNER_LEFT && currentShape == EnumStairShape.INNER_RIGHT) || (expectedShape == EnumStairShape.INNER_RIGHT && currentShape == EnumStairShape.INNER_LEFT) || (expectedShape == EnumStairShape.OUTER_LEFT && currentShape == EnumStairShape.OUTER_RIGHT) || (expectedShape == EnumStairShape.OUTER_RIGHT && currentShape == EnumStairShape.OUTER_LEFT))
							score++;
					}
				}
			}
		}
		
		for (MobData mobData : buildData.Mobs)
		{
			for (Entity entity : Mobs)
			{
				int dx = (int) (entity.getLocation().getX() - (CornerA.getX() + 0.5));
				int dy = (int) (entity.getLocation().getY() - CornerA.getY());
				int dz = (int) (entity.getLocation().getZ() - (CornerA.getZ() + 0.5));
				
				if (mobData.EntityType == entity.getType() && mobData.DX == dx && mobData.DY == dy && mobData.DZ == dz)
				{
					score++;
					
					break;
				}
			}
		}
		
		return score;
	}

	public Location getMidpoint()
	{
		return UtilAlg.getMidpoint(CornerA, CornerB.clone().add(1, 1, 1));
	}

	public List<Block> getBlocks()
	{
		return UtilBlock.getInBoundingBox(CornerA, CornerB);
	}

	public boolean isQueuedForDemolition(Block block)
	{
		for (DemolitionData demolition : BlocksForDemolition)
		{
			if (demolition.Blocks.containsKey(block))
				return true;
		}
		
		return false;
	}

	public boolean isQueuedForDemolition(Entity entity)
	{
		for (DemolitionData demolition : BlocksForDemolition)
		{
			if (demolition.Mobs.contains(entity))
				return true;
		}
		
		return false;
	}

	public void addToDemolition(Block block)
	{
		if (isQueuedForDemolition(block))
			return;
		
		ArrayList<Block> blocks = new ArrayList<Block>();
		blocks.add(block);
		
		//Add the other part of the block
		if (block.getType() == Material.BED_BLOCK)
		{
			Bed bed = (Bed) block.getState().getData();
			
			if (bed.isHeadOfBed())
				blocks.add(block.getRelative(bed.getFacing().getOppositeFace()));
			else
				blocks.add(block.getRelative(bed.getFacing()));
		}
		else if (block.getType() == Material.WOODEN_DOOR || block.getType() == Material.IRON_DOOR_BLOCK || block.getType() == Material.SPRUCE_DOOR || block.getType() == Material.BIRCH_DOOR || block.getType() == Material.JUNGLE_DOOR || block.getType() == Material.ACACIA_DOOR || block.getType() == Material.DARK_OAK_DOOR)
		{
			Door door = (Door) block.getState().getData();
			
			if (door.isTopHalf())
				blocks.add(block.getRelative(BlockFace.DOWN));
			else
				blocks.add(block.getRelative(BlockFace.UP));
		}
		else if (block.getType() == Material.DOUBLE_PLANT)
		{
			if (block.getData() > 7)
				blocks.add(block.getRelative(BlockFace.DOWN));
			else
				blocks.add(block.getRelative(BlockFace.UP));
		}
		
		BlocksForDemolition.add(new DemolitionData(this, blocks, new ArrayList<Entity>()));
	}

	public void addToDemolition(Entity entity)
	{
		if (isQueuedForDemolition(entity))
			return;
		
		ArrayList<Entity> mobs = new ArrayList<Entity>();
		mobs.add(entity);
		
		BlocksForDemolition.add(new DemolitionData(this, new ArrayList<Block>(), mobs));
	}

	public void removeHologram()
	{
		_hologram.stop();
	}

}
