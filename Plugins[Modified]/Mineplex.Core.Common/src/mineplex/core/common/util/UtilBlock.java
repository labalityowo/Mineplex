package mineplex.core.common.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.block.Furnace;
import org.bukkit.block.Hopper;
import org.bukkit.block.Jukebox;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.Bed;

import com.mojang.authlib.GameProfile;

import mineplex.core.common.Pair;
import mineplex.core.common.block.MultiBlockUpdaterAgent;
import mineplex.core.common.skin.SkinData;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.MinecraftKey;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.TileEntityFlowerPot;
import net.minecraft.server.v1_8_R3.WorldServer;

public class UtilBlock
{
	
	public static void main(String[] args)
	{
		
		for (Material m : Material.values())
		{
			
			boolean thisSolid = fullSolid(m.getId());
			boolean solid = m.isSolid();
			if (thisSolid != solid)
			{
				StringBuilder sb = new StringBuilder();
				sb.append("Failed: ");
				sb.append(m.name());
				int amount = 40 - sb.length();
				for (int i = 0; i < amount; i++)
				{
					sb.append(" ");
				}
				sb.append(thisSolid);
				System.out.println(sb);
			}
			
		}
		System.out.println("done!");
		
	}
	
	/**
	 * A list of blocks that are usable
	 */
	public static HashSet<Byte> blockUseSet = new HashSet<Byte>();
	/**
	 * A list of blocks that are always solid and can be stood on
	 */
	public static HashSet<Byte> fullSolid = new HashSet<Byte>();
	/**
	 * A list of blocks that are non-solid, but can't be moved through. Eg lily,
	 * fence gate, portal
	 */
	public static HashSet<Byte> blockPassSet = new HashSet<Byte>();
	/**
	 * A list of blocks that offer zero resistance (long grass, torch, flower)
	 */
	public static HashSet<Byte> blockAirFoliageSet = new HashSet<Byte>();
	
	/**
	 * All horizontal diections [north, east, south, west]
	 */
	public static List<BlockFace> horizontals = Arrays.asList(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST);
	
	private static MultiBlockUpdaterAgent _quickChangeRecorder;

	static
	{
		
		blockAirFoliageSet.add((byte) Material.AIR.getId());
		blockAirFoliageSet.add((byte) Material.SAPLING.getId());
		blockAirFoliageSet.add((byte) Material.LONG_GRASS.getId());
		blockAirFoliageSet.add((byte) Material.DOUBLE_PLANT.getId());
		blockAirFoliageSet.add((byte) Material.DEAD_BUSH.getId());
		blockAirFoliageSet.add((byte) Material.YELLOW_FLOWER.getId());
		blockAirFoliageSet.add((byte) Material.RED_ROSE.getId());
		blockAirFoliageSet.add((byte) Material.BROWN_MUSHROOM.getId());
		blockAirFoliageSet.add((byte) Material.RED_MUSHROOM.getId());
		blockAirFoliageSet.add((byte) Material.FIRE.getId());
		blockAirFoliageSet.add((byte) Material.CROPS.getId());
		blockAirFoliageSet.add((byte) Material.PUMPKIN_STEM.getId());
		blockAirFoliageSet.add((byte) Material.MELON_STEM.getId());
		blockAirFoliageSet.add((byte) Material.NETHER_WARTS.getId());
		blockAirFoliageSet.add((byte) Material.TRIPWIRE_HOOK.getId());
		blockAirFoliageSet.add((byte) Material.TRIPWIRE.getId());
		blockAirFoliageSet.add((byte) Material.CARROT.getId());
		blockAirFoliageSet.add((byte) Material.POTATO.getId());
		blockAirFoliageSet.add((byte) Material.DOUBLE_PLANT.getId());
		blockAirFoliageSet.add((byte) Material.STANDING_BANNER.getId());
		blockAirFoliageSet.add((byte) Material.WALL_BANNER.getId());
		
		blockPassSet.add((byte) Material.AIR.getId());
		blockPassSet.add((byte) Material.SAPLING.getId());
		blockPassSet.add((byte) Material.WATER.getId());
		blockPassSet.add((byte) Material.STATIONARY_WATER.getId());
		blockPassSet.add((byte) Material.LAVA.getId());
		blockPassSet.add((byte) Material.STATIONARY_LAVA.getId());
		blockPassSet.add((byte) Material.BED_BLOCK.getId());
		blockPassSet.add((byte) Material.POWERED_RAIL.getId());
		blockPassSet.add((byte) Material.DETECTOR_RAIL.getId());
		blockPassSet.add((byte) Material.WEB.getId());
		blockPassSet.add((byte) Material.LONG_GRASS.getId());
		blockPassSet.add((byte) Material.DEAD_BUSH.getId());
		blockPassSet.add((byte) Material.YELLOW_FLOWER.getId());
		blockPassSet.add((byte) Material.RED_ROSE.getId());
		blockPassSet.add((byte) Material.BROWN_MUSHROOM.getId());
		blockPassSet.add((byte) Material.RED_MUSHROOM.getId());
		blockPassSet.add((byte) Material.TORCH.getId());
		blockPassSet.add((byte) Material.FIRE.getId());
		blockPassSet.add((byte) Material.REDSTONE_WIRE.getId());
		blockPassSet.add((byte) Material.CROPS.getId());
		blockPassSet.add((byte) Material.SIGN_POST.getId());
		blockPassSet.add((byte) Material.WOODEN_DOOR.getId());
		blockPassSet.add((byte) Material.LADDER.getId());
		blockPassSet.add((byte) Material.RAILS.getId());
		blockPassSet.add((byte) Material.WALL_SIGN.getId());
		blockPassSet.add((byte) Material.LEVER.getId());
		blockPassSet.add((byte) Material.STONE_PLATE.getId());
		blockPassSet.add((byte) Material.IRON_DOOR_BLOCK.getId());
		blockPassSet.add((byte) Material.WOOD_PLATE.getId());
		blockPassSet.add((byte) Material.REDSTONE_TORCH_OFF.getId());
		blockPassSet.add((byte) Material.REDSTONE_TORCH_ON.getId());
		blockPassSet.add((byte) Material.STONE_BUTTON.getId());
		blockPassSet.add((byte) Material.SNOW.getId());
		blockPassSet.add((byte) Material.SUGAR_CANE_BLOCK.getId());
		blockPassSet.add((byte) Material.FENCE.getId());
		blockPassSet.add((byte) Material.PORTAL.getId());
		blockPassSet.add((byte) Material.CAKE_BLOCK.getId());
		blockPassSet.add((byte) Material.DIODE_BLOCK_OFF.getId());
		blockPassSet.add((byte) Material.DIODE_BLOCK_ON.getId());
		blockPassSet.add((byte) Material.TRAP_DOOR.getId());
		blockPassSet.add((byte) Material.IRON_FENCE.getId());
		blockPassSet.add((byte) Material.THIN_GLASS.getId());
		blockPassSet.add((byte) Material.PUMPKIN_STEM.getId());
		blockPassSet.add((byte) Material.MELON_STEM.getId());
		blockPassSet.add((byte) Material.VINE.getId());
		blockPassSet.add((byte) Material.FENCE_GATE.getId());
		blockPassSet.add((byte) Material.WATER_LILY.getId());
		blockPassSet.add((byte) Material.NETHER_WARTS.getId());
		blockPassSet.add((byte) Material.ENCHANTMENT_TABLE.getId());
		blockPassSet.add((byte) Material.BREWING_STAND.getId());
		blockPassSet.add((byte) Material.CAULDRON.getId());
		blockPassSet.add((byte) Material.ENDER_PORTAL.getId());
		blockPassSet.add((byte) Material.ENDER_PORTAL_FRAME.getId());
		blockPassSet.add((byte) Material.DAYLIGHT_DETECTOR.getId());
		blockPassSet.add((byte) Material.STAINED_GLASS_PANE.getId());
		blockPassSet.add((byte) Material.IRON_TRAPDOOR.getId());
		blockPassSet.add((byte) Material.DAYLIGHT_DETECTOR_INVERTED.getId());
		blockPassSet.add((byte) Material.BARRIER.getId());
		
		blockPassSet.add((byte) Material.BIRCH_FENCE_GATE.getId());
		blockPassSet.add((byte) Material.JUNGLE_FENCE_GATE.getId());
		blockPassSet.add((byte) Material.DARK_OAK_FENCE_GATE.getId());
		blockPassSet.add((byte) Material.ACACIA_FENCE_GATE.getId());
		blockPassSet.add((byte) Material.SPRUCE_FENCE.getId());
		blockPassSet.add((byte) Material.BIRCH_FENCE.getId());
		blockPassSet.add((byte) Material.JUNGLE_FENCE.getId());
		blockPassSet.add((byte) Material.DARK_OAK_FENCE.getId());
		blockPassSet.add((byte) Material.ACACIA_FENCE.getId());
		
		blockPassSet.add((byte) Material.SPRUCE_DOOR.getId());
		blockPassSet.add((byte) Material.BIRCH_DOOR.getId());
		blockPassSet.add((byte) Material.JUNGLE_DOOR.getId());
		blockPassSet.add((byte) Material.ACACIA_DOOR.getId());
		blockPassSet.add((byte) Material.DARK_OAK_DOOR.getId());
		
		fullSolid.add((byte) Material.STONE.getId());
		fullSolid.add((byte) Material.GRASS.getId());
		fullSolid.add((byte) Material.DIRT.getId());
		fullSolid.add((byte) Material.COBBLESTONE.getId());
		fullSolid.add((byte) Material.WOOD.getId());
		fullSolid.add((byte) Material.BEDROCK.getId());
		fullSolid.add((byte) Material.SAND.getId());
		fullSolid.add((byte) Material.GRAVEL.getId());
		fullSolid.add((byte) Material.GOLD_ORE.getId());
		fullSolid.add((byte) Material.IRON_ORE.getId());
		fullSolid.add((byte) Material.COAL_ORE.getId());
		fullSolid.add((byte) Material.LOG.getId());
		fullSolid.add((byte) Material.LEAVES.getId());
		fullSolid.add((byte) Material.SPONGE.getId());
		fullSolid.add((byte) Material.GLASS.getId());
		fullSolid.add((byte) Material.LAPIS_ORE.getId());
		fullSolid.add((byte) Material.LAPIS_BLOCK.getId());
		fullSolid.add((byte) Material.DISPENSER.getId());
		fullSolid.add((byte) Material.SANDSTONE.getId());
		fullSolid.add((byte) Material.NOTE_BLOCK.getId());
		fullSolid.add((byte) Material.PISTON_STICKY_BASE.getId());
		fullSolid.add((byte) Material.PISTON_BASE.getId());
		fullSolid.add((byte) Material.WOOL.getId());
		fullSolid.add((byte) Material.GOLD_BLOCK.getId());
		fullSolid.add((byte) Material.IRON_BLOCK.getId());
		fullSolid.add((byte) Material.DOUBLE_STEP.getId());
		fullSolid.add((byte) Material.STEP.getId());
		fullSolid.add((byte) Material.BRICK.getId());
		fullSolid.add((byte) Material.TNT.getId());
		fullSolid.add((byte) Material.BOOKSHELF.getId());
		fullSolid.add((byte) Material.MOSSY_COBBLESTONE.getId());
		fullSolid.add((byte) Material.OBSIDIAN.getId());
		fullSolid.add((byte) Material.DIAMOND_ORE.getId());
		fullSolid.add((byte) Material.DIAMOND_BLOCK.getId());
		fullSolid.add((byte) Material.WORKBENCH.getId());
		fullSolid.add((byte) Material.SOIL.getId());
		fullSolid.add((byte) Material.FURNACE.getId());
		fullSolid.add((byte) Material.BURNING_FURNACE.getId());
		fullSolid.add((byte) Material.REDSTONE_ORE.getId());
		fullSolid.add((byte) Material.GLOWING_REDSTONE_ORE.getId());
		fullSolid.add((byte) Material.ICE.getId());
		fullSolid.add((byte) Material.SNOW_BLOCK.getId());
		fullSolid.add((byte) Material.CLAY.getId());
		fullSolid.add((byte) Material.JUKEBOX.getId());
		fullSolid.add((byte) Material.PUMPKIN.getId());
		fullSolid.add((byte) Material.NETHERRACK.getId());
		fullSolid.add((byte) Material.SOUL_SAND.getId());
		fullSolid.add((byte) Material.GLOWSTONE.getId());
		fullSolid.add((byte) Material.JACK_O_LANTERN.getId());
		fullSolid.add((byte) Material.STAINED_GLASS.getId());
		fullSolid.add((byte) Material.MONSTER_EGGS.getId());
		fullSolid.add((byte) Material.SMOOTH_BRICK.getId());
		fullSolid.add((byte) Material.HUGE_MUSHROOM_1.getId());
		fullSolid.add((byte) Material.HUGE_MUSHROOM_2.getId());
		fullSolid.add((byte) Material.MELON_BLOCK.getId());
		fullSolid.add((byte) Material.MYCEL.getId());
		fullSolid.add((byte) Material.NETHER_BRICK.getId());
		fullSolid.add((byte) Material.ENDER_STONE.getId());
		fullSolid.add((byte) Material.REDSTONE_LAMP_OFF.getId());
		fullSolid.add((byte) Material.REDSTONE_LAMP_ON.getId());
		fullSolid.add((byte) Material.WOOD_DOUBLE_STEP.getId());
		fullSolid.add((byte) Material.WOOD_STEP.getId());
		fullSolid.add((byte) Material.EMERALD_ORE.getId());
		fullSolid.add((byte) Material.EMERALD_BLOCK.getId());
		fullSolid.add((byte) Material.COMMAND.getId());
		fullSolid.add((byte) Material.BEACON.getId());
		fullSolid.add((byte) Material.REDSTONE_BLOCK.getId());
		fullSolid.add((byte) Material.QUARTZ_ORE.getId());
		fullSolid.add((byte) Material.QUARTZ_BLOCK.getId());
		fullSolid.add((byte) Material.DROPPER.getId());
		fullSolid.add((byte) Material.STAINED_CLAY.getId());
		fullSolid.add((byte) Material.LEAVES_2.getId());
		fullSolid.add((byte) Material.LOG_2.getId());
		fullSolid.add((byte) Material.PRISMARINE.getId());
		fullSolid.add((byte) Material.SEA_LANTERN.getId());
		fullSolid.add((byte) Material.HAY_BLOCK.getId());
		fullSolid.add((byte) Material.HARD_CLAY.getId());
		fullSolid.add((byte) Material.COAL_BLOCK.getId());
		fullSolid.add((byte) Material.PACKED_ICE.getId());
		fullSolid.add((byte) Material.RED_SANDSTONE.getId());
		fullSolid.add((byte) Material.DOUBLE_STONE_SLAB2.getId());
		
		blockUseSet.add((byte) Material.DISPENSER.getId());
		blockUseSet.add((byte) Material.BED_BLOCK.getId());
		blockUseSet.add((byte) Material.PISTON_BASE.getId());
		blockUseSet.add((byte) Material.BOOKSHELF.getId());
		blockUseSet.add((byte) Material.CHEST.getId());
		blockUseSet.add((byte) Material.WORKBENCH.getId());
		blockUseSet.add((byte) Material.FURNACE.getId());
		blockUseSet.add((byte) Material.BURNING_FURNACE.getId());
		blockUseSet.add((byte) Material.WOODEN_DOOR.getId());
		blockUseSet.add((byte) Material.LEVER.getId());
		blockUseSet.add((byte) Material.IRON_DOOR_BLOCK.getId());
		blockUseSet.add((byte) Material.STONE_BUTTON.getId());
		blockUseSet.add((byte) Material.FENCE.getId());
		blockUseSet.add((byte) Material.DIODE_BLOCK_OFF.getId());
		blockUseSet.add((byte) Material.DIODE_BLOCK_ON.getId());
		blockUseSet.add((byte) Material.TRAP_DOOR.getId());
		blockUseSet.add((byte) Material.FENCE_GATE.getId());
		blockUseSet.add((byte) Material.NETHER_FENCE.getId());
		blockUseSet.add((byte) Material.ENCHANTMENT_TABLE.getId());
		blockUseSet.add((byte) Material.BREWING_STAND.getId());
		blockUseSet.add((byte) Material.ENDER_CHEST.getId());
		blockUseSet.add((byte) Material.ANVIL.getId());
		blockUseSet.add((byte) Material.TRAPPED_CHEST.getId());
		blockUseSet.add((byte) Material.HOPPER.getId());
		blockUseSet.add((byte) Material.DROPPER.getId());
		
		blockUseSet.add((byte) Material.BIRCH_FENCE_GATE.getId());
		blockUseSet.add((byte) Material.JUNGLE_FENCE_GATE.getId());
		blockUseSet.add((byte) Material.DARK_OAK_FENCE_GATE.getId());
		blockUseSet.add((byte) Material.ACACIA_FENCE_GATE.getId());
		blockUseSet.add((byte) Material.SPRUCE_FENCE_GATE.getId());
		blockUseSet.add((byte) Material.BIRCH_FENCE_GATE.getId());
		blockUseSet.add((byte) Material.JUNGLE_FENCE_GATE.getId());
		blockUseSet.add((byte) Material.DARK_OAK_FENCE_GATE.getId());
		blockUseSet.add((byte) Material.ACACIA_FENCE_GATE.getId());
		
		blockUseSet.add((byte) Material.SPRUCE_DOOR.getId());
		blockUseSet.add((byte) Material.BIRCH_DOOR.getId());
		blockUseSet.add((byte) Material.JUNGLE_DOOR.getId());
		blockUseSet.add((byte) Material.ACACIA_DOOR.getId());
		blockUseSet.add((byte) Material.DARK_OAK_DOOR.getId());
	}
	
	public static boolean solid(Block block)
	{
		if (block == null) return false;
		return solid(block.getTypeId());
	}
	
	public static boolean solid(int block)
	{
		return solid((byte) block);
	}
	
	public static boolean solid(byte block)
	{
		return !blockPassSet.contains(block);
	}
	
	public static boolean airFoliage(Block block)
	{
		if (block == null) return false;
		return airFoliage(block.getTypeId());
	}
	
	public static boolean airFoliage(int block)
	{
		return airFoliage((byte) block);
	}
	
	public static boolean airFoliage(byte block)
	{
		return blockAirFoliageSet.contains(block);
	}
	
	public static boolean fullSolid(Block block)
	{
		if (block == null) return false;
		
		return fullSolid(block.getTypeId());
	}
	
	public static boolean fullSolid(int block)
	{
		return fullSolid((byte) block);
	}
	
	public static boolean fullSolid(byte block)
	{
		return fullSolid.contains(block);
	}

	/**
	 * Determines whether a block is a bottom slab.
	 *
	 * @param block The block object.
	 *
	 * @return <code>true</code> if block is a bottom slab.
	 */
	public static boolean bottomSlab(Block block)
	{
		return bottomSlab(block.getType().getId(), block.getData());
	}

	/**
	 * Determines whether a block is a bottom slab.
	 *
	 * @param block The block id
	 * @param data  The block data
	 *
	 * @return <code>true</code> if block is a bottom slab.
	 */
	public static boolean bottomSlab(int block, byte data)
	{
		switch (block)
		{
			case 44:
				if (data >= 0 && data <= 7) return true;
				break;
			case 182:
				if (data == 0) return true;
				break;
			case 126:
				if (data >= 0 && data <= 5) return true;
				break;
		}

		return false;
	}

	public static boolean usable(Block block)
	{
		if (block == null) return false;
		
		return usable(block.getTypeId());
	}
	
	public static boolean usable(int block)
	{
		return usable((byte) block);
	}
	
	public static boolean usable(byte block)
	{
		return blockUseSet.contains(block);
	}

	public static Set<Block> getBlocksInRadius(Location loc, double radius)
	{
		return getInRadius(loc, radius).keySet();
	}

	public static Set<Block> getBlocksInRadius(Location loc, double radius, int maxHeight)
	{
		return getInRadius(loc, radius, maxHeight).keySet();
	}

	public static HashMap<Block, Double> getInRadius(Location loc, double dR)
	{
		return getInRadius(loc, dR, 9999);
	}
	
	public static HashMap<Block, Double> getInRadius(Location loc, double dR, double maxHeight)
	{
		HashMap<Block, Double> blockList = new HashMap<Block, Double>();
		int iR = (int) dR + 1;
		
		for (int x = -iR; x <= iR; x++)
			for (int z = -iR; z <= iR; z++)
				for (int y = -iR; y <= iR; y++)
				{
					if (Math.abs(y) > maxHeight) continue;
					
					Block curBlock = loc.getWorld().getBlockAt((int) (loc.getX() + x), (int) (loc.getY() + y), (int) (loc.getZ() + z));
					
					double offset = UtilMath.offset(loc, curBlock.getLocation().add(0.5, 0.5, 0.5));;
					
					if (offset <= dR) blockList.put(curBlock, 1 - (offset / dR));
				}
				
		return blockList;
	}
	
	public static HashMap<Block, Double> getInRadius(Block block, double dR)
	{
		return getInRadius(block, dR, false);
	}
	
	public static HashMap<Block, Double> getInRadius(Block block, double dR, boolean hollow)
	{
		HashMap<Block, Double> blockList = new HashMap<Block, Double>();
		int iR = (int) dR + 1;
		
		for (int x = -iR; x <= iR; x++)
			for (int z = -iR; z <= iR; z++)
				for (int y = -iR; y <= iR; y++)
				{
					Block curBlock = block.getRelative(x, y, z);
					
					double offset = UtilMath.offset(block.getLocation(), curBlock.getLocation());
					
					if (offset <= dR && !(hollow && offset < dR - 1))
					{
						blockList.put(curBlock, 1 - (offset / dR));
					}
				}
				
		return blockList;
	}
	
	public static ArrayList<Block> getInSquare(Block block, double dR)
	{
		ArrayList<Block> blockList = new ArrayList<Block>();
		int iR = (int) dR + 1;
		
		for (int x = -iR; x <= iR; x++)
			for (int z = -iR; z <= iR; z++)
				for (int y = -iR; y <= iR; y++)
				{
					blockList.add(block.getRelative(x, y, z));
				}
				
		return blockList;
	}
	
	public static boolean isBlock(ItemStack item)
	{
		if (item == null) return false;
		
		return item.getTypeId() > 0 && item.getTypeId() < 256;
	}
	
	public static Block getHighest(World world, int x, int z)
	{
		return getHighest(world, x, z, null);
	}
	
	public static Block getHighest(World world, Location location)
	{
		return getHighest(world, location.getBlockX(), location.getBlockZ());
	}
	
	public static Block getHighest(World world, Block block)
	{
		return getHighest(world, block.getLocation());
	}
	
	public static Block getHighest(World world, int x, int z, HashSet<Material> ignore)
	{
		Block block = world.getHighestBlockAt(x, z);
		
		// Shuffle Down
		while (block.getY() > 0 && (airFoliage(block) || block.getType() == Material.LEAVES || block.getType() == Material.LEAVES_2 || (ignore != null && ignore.contains(block.getType()))))
		{
			block = block.getRelative(BlockFace.DOWN);
		}
		
		return block.getRelative(BlockFace.UP);
	}
	
	/**
	 * 
	 * @param location of explosion
	 * @param strength of explosion
	 * @param damageBlocksEqually - Treat all blocks as durability of dirt
	 * @return
	 */
	public static ArrayList<Block> getExplosionBlocks(Location location, float strength, boolean damageBlocksEqually)
	{
		ArrayList<Block> toExplode = new ArrayList<Block>();
		WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
		
		for (int i = 0; i < 16; i++)
		{
			for (int j = 0; j < 16; j++)
			{
				for (int k = 0; k < 16; k++)
				{
					if ((i == 0) || (i == 16 - 1) || (j == 0) || (j == 16 - 1) || (k == 0) || (k == 16 - 1))
					{
						double d3 = i / (16 - 1.0F) * 2.0F - 1.0F;
						double d4 = j / (16 - 1.0F) * 2.0F - 1.0F;
						double d5 = k / (16 - 1.0F) * 2.0F - 1.0F;
						double d6 = Math.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
						
						d3 /= d6;
						d4 /= d6;
						d5 /= d6;
						float f1 = strength * (0.7F + UtilMath.random.nextFloat() * 0.6F);
						
						double d0 = location.getX();
						double d1 = location.getY();
						double d2 = location.getZ();
						
						for (float f2 = 0.3F; f1 > 0.0F; f1 -= f2 * 0.75F)
						{
							int l = MathHelper.floor(d0);
							int i1 = MathHelper.floor(d1);
							int j1 = MathHelper.floor(d2);
							Block block = location.getWorld().getBlockAt(l, i1, j1);
							
							if (block.getType() != Material.AIR)
							{
								Blocks.DIRT.a((net.minecraft.server.v1_8_R3.Entity) null);
								float f3 = (damageBlocksEqually ? Blocks.DIRT : world.getType(new BlockPosition(block.getX(), block.getY(), block.getZ())).getBlock()).a((net.minecraft.server.v1_8_R3.Entity) null);
								
								f1 -= (f3 + 0.3F) * f2;
							}
							
							if ((f1 > 0.0F) && (i1 < 256) && (i1 >= 0))
							{
								toExplode.add(block);
							}
							
							d0 += d3 * f2;
							d1 += d4 * f2;
							d2 += d5 * f2;
						}
					}
				}
			}
		}
		
		return toExplode;
	}
	
	public static ArrayList<Block> getSurrounding(Block block, boolean diagonals)
	{
		return UtilBlockBase.getSurrounding(block, diagonals);
	}
	
	public static boolean isVisible(Block block)
	{
		for (Block other : UtilBlock.getSurrounding(block, false))
		{
			if (!other.getType().isOccluding())
			{
				return true;
			}
		}
		
		return false;
	}
	
	public static List<Block> getInBoundingBox(World world, AxisAlignedBB box)
	{
		Location l1 = new Location(world, box.a, box.b, box.c);
		Location l2 = new Location(world, box.d, box.e, box.f);
		
		return getInBoundingBox(l1, l2);
	}
	
	public static ArrayList<Block> getInBoundingBox(Location a, Location b)
	{
		return getInBoundingBox(a, b, true);
	}
	
	public static ArrayList<Block> getInBoundingBox(Location a, Location b, boolean ignoreAir) {
		return getInBoundingBox(a, b, ignoreAir, false, false, false);
	}

	public static ArrayList<Block> getInBoundingBox(Location a, Location b, boolean ignoreAir, boolean hollow, boolean wallsOnly, boolean ceilfloorOnly)
	{
		ArrayList<Block> blocks = new ArrayList<Block>();
		
		int xmin = Math.min(a.getBlockX(), b.getBlockX());
		int xmax = Math.max(a.getBlockX(), b.getBlockX());

		int ymin = Math.min(a.getBlockY(), b.getBlockY());
		int ymax = Math.max(a.getBlockY(), b.getBlockY());

		int zmin = Math.min(a.getBlockZ(), b.getBlockZ());
		int zmax = Math.max(a.getBlockZ(), b.getBlockZ());

		for (int x = xmin; x <= xmax; x++)
			for (int y = ymin; y <= ymax; y++)
				for (int z = zmin; z <= zmax; z++)
				{
					if(hollow) 
					{
						if(!(x == xmin || x == xmax || y == ymin || y == ymax || z == zmin || z == zmax)) continue;
					}
					
					if(wallsOnly) 
					{
						if(
								(x != xmin && x != xmax) &&
								(z != zmin && z != zmax)
								)
								{
									continue;
								}
					}

					if(ceilfloorOnly)
					{
						if(y != ymin && y != ymax)
						{
							continue;
						}
					}

					Block block = a.getWorld().getBlockAt(x, y, z);
					
					if (ignoreAir)
					{
						if (block.getType() != Material.AIR) blocks.add(block);
					}
					else
					{
						blocks.add(block);
					}
				}
				
		return blocks;
	}
	
	public static int getStepSoundId(Block block)
	{
		if (block.getTypeId() != 35 && block.getTypeId() != 159 && block.getTypeId() != 160) return block.getTypeId();
		
		switch (block.getData())
		{
			case 0:
				return block.getTypeId();
			case 1:
				return 172;
			case 2:
				return 87;
			case 3:
				return 79;
			case 4:
				return 41;
			case 5:
				return 133;
			case 6:
				return 45;
			case 7:
				return 16;
			case 8:
				return 13;
			case 9:
				return 56;
			case 10:
				return 110;
			case 11:
				return 22;
			case 12:
				return 3;
			case 13:
				return 31;
			case 14:
				return 152;
			case 15:
				return 173;
				
			default:
				return block.getTypeId();
		}
	}
	
	/**
	 * The location specified is the location of the foot block of the bed.
	 * <p>
	 * ex:
	 * <p>
	 * placeBed(location[0,0,0], NORTH);
	 * <p>
	 * will result in two blocks appearing, at location[0,0,0](foot block) and
	 * location[0,0,-1](head block (pillow))
	 * <p>
	 * allowFloating defines whether or not the bed is allowed to float (over
	 * water, lava, air, and anything other blocks that do not have bounds)
	 * <p>
	 * if force is set to true, then the bed will be placed even if there are
	 * blocks obstructing it, on the other hand, if force is false, then the bed
	 * will only be placed if there is air in the location specified.
	 */
	public static boolean placeBed(Location location, BlockFace direction, boolean allowFloating, boolean force)
	{
		if (!horizontals.contains(direction))
		{
			return false;
		}
		
		if (location == null)
		{
			return false;
		}
		
		if (location.getY() <= 0)
		{
			return false;
		}
		
		if (!allowFloating && UtilItem.isBoundless(location.getBlock().getRelative(BlockFace.DOWN).getType()))
		{
			return false;
		}
		
		BlockState head = location.getBlock().getRelative(direction).getState();
		BlockState foot = location.getBlock().getState();
		
		if (!force && (!UtilItem.isBoundless(head.getType()) || !UtilItem.isBoundless(foot.getType())))
		{
			return false;
		}
		
		System.out.println("<-bed-> head & foot are air");
		
		head.setType(Material.BED_BLOCK);
		foot.setType(Material.BED_BLOCK);
		
		Bed bedHead = (Bed) head.getData();
		Bed bedFoot = (Bed) foot.getData();
		
		bedHead.setHeadOfBed(true);
		bedFoot.setHeadOfBed(false);
		
		bedHead.setFacingDirection(direction);
		bedFoot.setFacingDirection(direction);
		
		head.setData(bedHead);
		foot.setData(bedFoot);
		head.update(true, false);
		foot.update(true, false);
		
		System.out.println("<-bed-> alls guude");
		
		return head.getBlock().getType().equals(Material.BED_BLOCK) && foot.getBlock().getType().equals(Material.BED_BLOCK);
	}
	
	public static boolean deleteBed(Location loc)
	{
		if (loc == null)
		{
			return false;
		}
		
		if (loc.getY() <= 0)
		{
			return false;
		}
		
		if (!loc.getBlock().getType().equals(Material.BED_BLOCK))
		{
			return false;
		}
		
		BlockState head = getBedHead(loc.getBlock()).getState();
		BlockState foot = getBedFoot(loc.getBlock()).getState();
		
		head.setType(Material.AIR);
		head.setRawData((byte) 0);
		
		foot.setType(Material.AIR);
		foot.setRawData((byte) 0);
		
		return head.update(true, false) && foot.update(true, false);
	}
	
	private static Block getBedHead(Block bed)
	{
		if (bed == null)
		{
			return null;
		}
		
		if (!bed.getType().equals(Material.BED_BLOCK))
		{
			return null;
		}
		
		if (getBed(bed).isHeadOfBed())
		{
			return bed;
		}
		else
		{
			return bed.getRelative(getBed(bed).getFacing()).getType().equals(Material.BED_BLOCK) ? bed.getRelative(getBed(bed).getFacing()) : null;
		}
	}
	
	private static Block getBedFoot(Block bed)
	{
		if (bed == null)
		{
			return null;
		}
		
		if (!bed.getType().equals(Material.BED_BLOCK))
		{
			return null;
		}
		
		if (!getBed(bed).isHeadOfBed())
		{
			return bed;
		}
		else
		{
			return bed.getRelative(getBed(bed).getFacing().getOppositeFace()).getType().equals(Material.BED_BLOCK) ? bed.getRelative(getBed(bed).getFacing().getOppositeFace()) : null;
		}
	}
	
	private static Bed getBed(Block bed)
	{
		if (bed == null)
		{
			return null;
		}
		
		if (!bed.getType().equals(Material.BED_BLOCK))
		{
			return null;
		}
		
		return (Bed) bed.getState().getData();
	}
	
	public static boolean isValidBed(Location bed)
	{
		if (bed == null)
		{
			return false;
		}
		
		if (bed.getY() <= 0)
		{
			return false;
		}
		
		if (!bed.getBlock().getType().equals(Material.BED_BLOCK))
		{
			return false;
		}
		
		return getBedHead(bed.getBlock()) != null && getBedFoot(bed.getBlock()) != null;
	}
	
	public static HashSet<Block> findConnectedBlocks(Block source, Block block, HashSet<Block> blocks, int limit, double range)
	{
		if (blocks == null)
			blocks = new HashSet<Block>();
		
		//This is incase you recursively check an entire MC world
		if (blocks.size() >= limit)
			return blocks;

		//Mark current node as searched
		blocks.add(block);

		//Search the node
		for (Block neighbour : UtilBlock.getSurrounding(block, false))
		{
			if (neighbour.getType() == Material.AIR)
				continue;
			
			if (UtilMath.offset(source.getLocation(), neighbour.getLocation()) > range)
				continue;
				
			//If neighbour hasn't been searched, recursively search it!
			if (!blocks.contains(neighbour))
				findConnectedBlocks(source, neighbour, blocks, limit, range);
		}
		
		return blocks;
	}
	
	public static ArrayList<ItemStack> blockToInventoryItemStacks(Block block)
	{
		ItemStack itemStack = new ItemStack(block.getType(), 1, block.getData());
		ArrayList<ItemStack> itemStacks = new ArrayList<ItemStack>();
		itemStacks.add(itemStack);
		
		switch (block.getType())
		{
			case SAPLING:
				itemStack.setDurability((short) 0);
				break;
			case WATER:
				itemStack.setType(Material.WATER_BUCKET);
				itemStack.setDurability((short) 0);
				break;
			case STATIONARY_WATER:
				itemStack.setType(Material.WATER_BUCKET);
				itemStack.setDurability((short) 0);
				break;
			case LAVA:
				itemStack.setType(Material.LAVA_BUCKET);
				itemStack.setDurability((short) 0);
				break;
			case STATIONARY_LAVA:
				itemStack.setType(Material.LAVA_BUCKET);
				itemStack.setDurability((short) 0);
				break;
			case LOG:
				itemStack.setDurability((short) (itemStack.getDurability() % 4));
				break;
			case LEAVES:
				itemStack.setDurability((short) (itemStack.getDurability() % 4));
				break;
			case DISPENSER:
				itemStack.setDurability((short) 0);
				
				Dispenser dispenser = (Dispenser) block.getState();
				
				for (ItemStack is : dispenser.getInventory().getContents())
				{
					if (is == null)
						continue;
					
					itemStacks.add(is);
				}
				
				break;
			case BED_BLOCK:
				itemStack.setType(Material.BED);
				itemStack.setDurability((short) 0);
				break;
			case POWERED_RAIL:
				itemStack.setDurability((short) 0);
				break;
			case DETECTOR_RAIL:
				itemStack.setDurability((short) 0);
				break;
			case PISTON_STICKY_BASE:
				itemStack.setDurability((short) 0);
				break;
			case PISTON_BASE:
				itemStack.setDurability((short) 0);
				break;
			case PISTON_EXTENSION:
				itemStack.setType(Material.AIR);
				itemStack.setDurability((short) 0);
				break;
			case PISTON_MOVING_PIECE:
				itemStack.setType(Material.AIR);
				itemStack.setDurability((short) 0);
				break;
			case DOUBLE_STEP:
				itemStack.setType(Material.STEP);
				itemStack.setAmount(2);
				break;
			case STEP:
				itemStack.setDurability((short) (itemStack.getDurability() % 8));
				break;
			case TORCH:
				itemStack.setDurability((short) 0);
				break;
			case FIRE:
				itemStack.setType(Material.FIREBALL);
				itemStack.setDurability((short) 0);
				break;
			case WOOD_STAIRS:
				itemStack.setDurability((short) 0);
				break;
			case CHEST:
				itemStack.setDurability((short) 0);
				
				Chest chest = (Chest) block.getState();
				
				for (ItemStack is : chest.getBlockInventory().getContents())
				{
					if (is == null)
						continue;
					
					itemStacks.add(is);
				}
				
				break;
			case REDSTONE_WIRE:
				itemStack.setType(Material.REDSTONE);
				itemStack.setDurability((short) 0);
				break;
			case CROPS:
				itemStack.setType(Material.SEEDS);
				itemStack.setDurability((short) 0);
				break;
			case SOIL:
				itemStack.setType(Material.DIRT);
				itemStack.setDurability((short) 0);
				break;
			case FURNACE:
				itemStack.setDurability((short) 0);
				
				Furnace furnace = (Furnace) block.getState();
				
				for (ItemStack is : furnace.getInventory().getContents())
				{
					if (is == null)
						continue;
					
					itemStacks.add(is);
				}
				
				break;
			case BURNING_FURNACE:
				itemStack.setType(Material.FURNACE);
				itemStack.setDurability((short) 0);
				
				Furnace burningFurnace = (Furnace) block.getState();
				
				for (ItemStack is : burningFurnace.getInventory().getContents())
				{
					if (is == null)
						continue;
					
					itemStacks.add(is);
				}
				
				break;
			case SIGN_POST:
				itemStack.setType(Material.SIGN);
				itemStack.setDurability((short) 0);
				break;
			case WOODEN_DOOR:
				itemStack.setType(Material.WOOD_DOOR);
				itemStack.setDurability((short) 0);
				break;
			case LADDER:
				itemStack.setDurability((short) 0);
				break;
			case RAILS:
				itemStack.setDurability((short) 0);
				break;
			case COBBLESTONE_STAIRS:
				itemStack.setDurability((short) 0);
				break;
			case WALL_SIGN:
				itemStack.setType(Material.SIGN);
				itemStack.setDurability((short) 0);
				break;
			case LEVER:
				itemStack.setDurability((short) 0);
				break;
			case STONE_PLATE:
				itemStack.setDurability((short) 0);
				break;
			case IRON_DOOR_BLOCK:
				itemStack.setType(Material.IRON_DOOR);
				itemStack.setDurability((short) 0);
				break;
			case WOOD_PLATE:
				itemStack.setDurability((short) 0);
				break;
			case GLOWING_REDSTONE_ORE:
				itemStack.setType(Material.REDSTONE_ORE);
				break;
			case REDSTONE_TORCH_OFF:
				itemStack.setType(Material.REDSTONE_TORCH_ON);
				itemStack.setDurability((short) 0);
				break;
			case REDSTONE_TORCH_ON:
				itemStack.setDurability((short) 0);
				break;
			case STONE_BUTTON:
				itemStack.setDurability((short) 0);
				break;
			case SNOW:
				itemStack.setAmount(1 + itemStack.getDurability());
				itemStack.setDurability((short) 0);
				break;
			case CACTUS:
				itemStack.setDurability((short) 0);
				break;
			case SUGAR_CANE_BLOCK:
				itemStack.setType(Material.SUGAR_CANE);
				itemStack.setDurability((short) 0);
				break;
			case JUKEBOX:
				itemStack.setDurability((short) 0);
				
				Jukebox jukebox = (Jukebox) block.getState();
				
				if (jukebox.getPlaying() != Material.AIR)
					itemStacks.add(new ItemStack(jukebox.getPlaying()));
					
				break;
			case PORTAL:
				itemStack.setType(Material.AIR);
				itemStack.setDurability((short) 0);
				break;
			case CAKE_BLOCK:
				itemStack.setType(Material.CAKE);
				itemStack.setDurability((short) 0);
				break;
			case DIODE_BLOCK_OFF:
				itemStack.setType(Material.DIODE);
				itemStack.setDurability((short) 0);
				break;
			case DIODE_BLOCK_ON:
				itemStack.setType(Material.DIODE);
				itemStack.setDurability((short) 0);
				break;
			case TRAP_DOOR:
				itemStack.setDurability((short) 0);
				break;
			case HUGE_MUSHROOM_1:
				itemStack.setDurability((short) 0);
				break;
			case HUGE_MUSHROOM_2:
				itemStack.setDurability((short) 0);
				break;
			case PUMPKIN_STEM:
				itemStack.setType(Material.PUMPKIN_SEEDS);
				itemStack.setDurability((short) 0);
				break;
			case MELON_STEM:
				itemStack.setType(Material.MELON_SEEDS);
				itemStack.setDurability((short) 0);
				break;
			case VINE:
				itemStack.setDurability((short) 0);
				break;
			case FENCE_GATE:
				itemStack.setDurability((short) 0);
				break;
			case BRICK_STAIRS:
				itemStack.setDurability((short) 0);
				break;
			case SMOOTH_STAIRS:
				itemStack.setDurability((short) 0);
				break;
			case NETHER_BRICK_STAIRS:
				itemStack.setDurability((short) 0);
				break;
			case NETHER_WARTS:
				itemStack.setType(Material.NETHER_STALK);
				itemStack.setDurability((short) 0);
				break;
			case BREWING_STAND:
				itemStack.setType(Material.BREWING_STAND_ITEM);
				itemStack.setDurability((short) 0);
				
				BrewingStand brewingStand = (BrewingStand) block.getState();
				
				for (ItemStack is : brewingStand.getInventory().getContents())
				{
					if (is == null)
						continue;
					
					itemStacks.add(is);
				}
				
				break;
			case CAULDRON:
				itemStack.setType(Material.CAULDRON_ITEM);
				itemStack.setDurability((short) 0);
				
				if (block.getData() != 0)
					itemStacks.add(new ItemStack(Material.WATER_BUCKET));
					
				break;
			case ENDER_PORTAL:
				itemStack.setType(Material.AIR);
				itemStack.setDurability((short) 0);
				break;
			case ENDER_PORTAL_FRAME:
				itemStack.setDurability((short) 0);
				
				if ((block.getData() & 0x4) != 0)
					itemStacks.add(new ItemStack(Material.EYE_OF_ENDER));
					
				break;
			case REDSTONE_LAMP_ON:
				itemStack.setType(Material.REDSTONE_LAMP_OFF);
				break;
			case WOOD_DOUBLE_STEP:
				itemStack.setType(Material.WOOD_STEP);
				itemStack.setAmount(2);
				break;
			case WOOD_STEP:
				itemStack.setDurability((short) (itemStack.getDurability() % 8));
				break;
			case COCOA:
				itemStack.setType(Material.INK_SACK);
				itemStack.setDurability((short) 3);
				break;
			case SANDSTONE_STAIRS:
				itemStack.setDurability((short) 0);
				break;
			case ENDER_CHEST:
				itemStack.setDurability((short) 0);
				break;
			case TRIPWIRE_HOOK:
				itemStack.setDurability((short) 0);
				break;
			case TRIPWIRE:
				itemStack.setType(Material.STRING);
				itemStack.setDurability((short) 0);
				break;
			case SPRUCE_WOOD_STAIRS:
				itemStack.setDurability((short) 0);
				break;
			case BIRCH_WOOD_STAIRS:
				itemStack.setDurability((short) 0);
				break;
			case JUNGLE_WOOD_STAIRS:
				itemStack.setDurability((short) 0);
				break;
			case COMMAND:
				itemStack.setDurability((short) 0);
				break;
			case FLOWER_POT:
				itemStack.setType(Material.FLOWER_POT_ITEM);
				itemStack.setDurability((short) 0);
				
				//The FlowerPot class is outdated and doesn't work so we do some NBT checking
				TileEntityFlowerPot tefp = (TileEntityFlowerPot) ((CraftWorld) block.getWorld()).getHandle().getTileEntity(new BlockPosition(block.getX(), block.getY(), block.getZ()));
				
				NBTTagCompound c = new NBTTagCompound();
				tefp.b(c);
				
				ItemStack blockInPot = new ItemStack(Material.AIR);
				
				if (c.hasKey("Item"))
				{
					MinecraftKey mk = new MinecraftKey(c.getString("Item"));
					blockInPot = CraftItemStack.asNewCraftStack(Item.REGISTRY.get(mk));
				}
				
				if (c.hasKey("Data"))
					blockInPot.setDurability(c.getShort("Data"));
				
				if (blockInPot.getType() != Material.AIR)
					itemStacks.add(blockInPot);
				
				break;
			case CARROT:
				itemStack.setType(Material.CARROT_ITEM);
				itemStack.setDurability((short) 0);
				break;
			case POTATO:
				itemStack.setType(Material.POTATO_ITEM);
				itemStack.setDurability((short) 0);
				break;
			case WOOD_BUTTON:
				itemStack.setDurability((short) 0);
				break;
			case SKULL:
				itemStack.setType(Material.SKULL_ITEM);
				
				Skull skull = (Skull) block.getState();
				itemStack.setDurability((short) skull.getSkullType().ordinal());
				
				if (skull.getSkullType() == SkullType.PLAYER && skull.hasOwner())
				{
					SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
					skullMeta.setOwner(skull.getOwner());
					itemStack.setItemMeta(skullMeta);
				}
					
				break;
			case TRAPPED_CHEST:
				itemStack.setDurability((short) 0);
				
				Chest trappedChest = (Chest) block.getState();
				
				for (ItemStack is : trappedChest.getBlockInventory().getContents())
				{
					if (is == null)
						continue;
					
					itemStacks.add(is);
				}
				
				break;
			case GOLD_PLATE:
				itemStack.setDurability((short) 0);
				break;
			case IRON_PLATE:
				itemStack.setDurability((short) 0);
				break;
			case REDSTONE_COMPARATOR_OFF:
				itemStack.setType(Material.REDSTONE_COMPARATOR);
				itemStack.setDurability((short) 0);
				break;
			case REDSTONE_COMPARATOR_ON:
				itemStack.setType(Material.REDSTONE_COMPARATOR);
				itemStack.setDurability((short) 0);
				break;
			case DAYLIGHT_DETECTOR:
				itemStack.setDurability((short) 0);
				break;
			case HOPPER:
				itemStack.setDurability((short) 0);
				
				Hopper hopper = (Hopper) block.getState();
				
				for (ItemStack is : hopper.getInventory().getContents())
				{
					if (is == null)
						continue;
					
					itemStacks.add(is);
				}
				
				break;
			case QUARTZ_STAIRS:
				itemStack.setDurability((short) 0);
				break;
			case ACTIVATOR_RAIL:
				itemStack.setDurability((short) 0);
				break;
			case DROPPER:
				itemStack.setDurability((short) 0);
				
				Dropper dropper = (Dropper) block.getState();
				
				for (ItemStack is : dropper.getInventory().getContents())
				{
					if (is == null)
						continue;
					
					itemStacks.add(is);
				}
				
				break;
			case LEAVES_2:
				itemStack.setDurability((short) (itemStack.getDurability() % 4));
				break;
			case LOG_2:
				itemStack.setDurability((short) (itemStack.getDurability() % 4));
				break;
			case ACACIA_STAIRS:
				itemStack.setDurability((short) 0);
				break;
			case DARK_OAK_STAIRS:
				itemStack.setDurability((short) 0);
				break;
			case IRON_TRAPDOOR:
				itemStack.setDurability((short) 0);
				break;
			case HAY_BLOCK:
				itemStack.setDurability((short) 0);
				break;
			case STANDING_BANNER:
				itemStack.setType(Material.BANNER);
				
				Banner banner = (Banner) block.getState();
				itemStack.setDurability(banner.getBaseColor().getDyeData());
				
				BannerMeta bannerMeta = (BannerMeta) itemStack.getItemMeta();
				bannerMeta.setBaseColor(bannerMeta.getBaseColor());
				bannerMeta.setPatterns(banner.getPatterns());
				itemStack.setItemMeta(bannerMeta);
				
				break;
			case WALL_BANNER:
				itemStack.setType(Material.BANNER);
				
				Banner wallBanner = (Banner) block.getState();
				itemStack.setDurability(wallBanner.getBaseColor().getDyeData());
				
				BannerMeta wallBannerMeta = (BannerMeta) itemStack.getItemMeta();
				wallBannerMeta.setBaseColor(wallBannerMeta.getBaseColor());
				wallBannerMeta.setPatterns(wallBanner.getPatterns());
				itemStack.setItemMeta(wallBannerMeta);
				
				break;
			case DAYLIGHT_DETECTOR_INVERTED:
				itemStack.setType(Material.DAYLIGHT_DETECTOR);
				itemStack.setDurability((short) 0);
				break;
			case RED_SANDSTONE_STAIRS:
				itemStack.setDurability((short) 0);
				break;
			case DOUBLE_STONE_SLAB2:
				itemStack.setType(Material.STONE_SLAB2);
				itemStack.setAmount(2);
				break;
			case STONE_SLAB2:
				itemStack.setDurability((short) (itemStack.getDurability() % 8));
				break;
			case SPRUCE_FENCE_GATE:
				itemStack.setDurability((short) 0);
				break;
			case BIRCH_FENCE_GATE:
				itemStack.setDurability((short) 0);
				break;
			case JUNGLE_FENCE_GATE:
				itemStack.setDurability((short) 0);
				break;
			case DARK_OAK_FENCE_GATE:
				itemStack.setDurability((short) 0);
				break;
			case ACACIA_FENCE_GATE:
				itemStack.setDurability((short) 0);
				break;
			case SPRUCE_DOOR:
				itemStack.setType(Material.SPRUCE_DOOR_ITEM);
				itemStack.setDurability((short) 0);
				break;
			case BIRCH_DOOR:
				itemStack.setType(Material.BIRCH_DOOR_ITEM);
				itemStack.setDurability((short) 0);
				break;
			case JUNGLE_DOOR:
				itemStack.setType(Material.JUNGLE_DOOR_ITEM);
				itemStack.setDurability((short) 0);
				break;
			case ACACIA_DOOR:
				itemStack.setType(Material.ACACIA_DOOR_ITEM);
				itemStack.setDurability((short) 0);
				break;
			case DARK_OAK_DOOR:	
				itemStack.setType(Material.DARK_OAK_DOOR_ITEM);
				itemStack.setDurability((short) 0);
				break;
			case ANVIL:
				itemStack.setDurability((short) (itemStack.getDurability() / 4));
				break;
			case QUARTZ_BLOCK:
				if (itemStack.getDurability() == 4 || itemStack.getDurability() == 3)
					itemStack.setDurability((short) 2);
				
				break;
		}
		
		return itemStacks;
	}

	public static Location nearestFloor(Location location)
	{
		if (!UtilItem.isBoundless(location.getBlock().getType()))
		{
			return location.clone();
		}
		
		Location gr = location.clone();
		
		while (UtilItem.isBoundless(gr.getBlock().getType()) && gr.getY() > 0)
		{
			gr.subtract(0, 0.5, 0);
		}
		
		return gr.getBlock().getLocation();
	}
	
	public static boolean setSilent(Block block, Material type)
	{
		return setSilent(block, type, (byte) 0);
	}
	
	/**
	 * Sets block data without causing a block update.
	 */
	public static boolean setSilent(Block block, Material type, byte data)
	{
		BlockState state = block.getState();
		
		state.setType(type);
		state.setRawData(data);
		
		return state.update(false, false);
	}

	/**
	 * See {@link #setQuick(World, int, int, int, int, byte)}
	 */
	public static void startQuickRecording()
	{
		if(_quickChangeRecorder != null)
		{
			_quickChangeRecorder.send();
			_quickChangeRecorder.reset();
		}
		else
		{
			_quickChangeRecorder = new MultiBlockUpdaterAgent();
		}
	}

	/**
	 * See {@link #setQuick(World, int, int, int, int, byte)}
	 */
	public static void stopQuickRecording()
	{
		if(_quickChangeRecorder == null) return;
		_quickChangeRecorder.send();
		_quickChangeRecorder.reset();
		_quickChangeRecorder = null;

	}

	/**
	 * This doesn't send the block changes to the client. If you want to change lots of blocks and then send it to the player
	 * then do <code>startQuickRecording()</code> first. Change all the blocks you want. Then to send it do
	 * <code>stopQuickRecording()</code>. This will automatically send all the block changes to all relevant players.
	 */
	public static void setQuick(World world, int x, int y, int z, int type, byte data)
	{
		int cx = x >> 4;
		int cz = z >> 4;
		if (!world.isChunkLoaded(cx, cz))
		{
			world.loadChunk(cx, cz, true);
		}

		WorldServer nmsWorld = ((CraftWorld) world).getHandle();

		net.minecraft.server.v1_8_R3.Chunk chunk = nmsWorld.getChunkAt(x >> 4, z >> 4);
		BlockPosition pos = new BlockPosition(x, y, z);
		IBlockData ibd = net.minecraft.server.v1_8_R3.Block.getById(type).fromLegacyData(data);
		chunk.a(pos, ibd);
		nmsWorld.notify(pos);

//		if(_quickChangeRecorder != null)
//		{
//			_quickChangeRecorder.addBlock(world.getBlockAt(x, y, z));
//		}
	}
	
	/**
	 * @return true if all of the blocks within the specified radius of the specified origin block are boundless ({@link UtilItem#isBoundless}.)
	 */
	public static boolean boundless(Location origin, double radius)
	{
		for (Block block : getInRadius(origin, radius).keySet())
		{
			if (!UtilItem.isBoundless(block.getType()))
			{
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * @return true if there are any non-boundless ({@link UtilItem#isBoundless}) blocks within the specified radius of the specified origin block.
	 */
	public static boolean boundless(Block origin, double radius)
	{
		return boundless(origin.getLocation(), radius);
	}

	/**
	 * Gets the max distance this blocks bounding box extends in the given block face. E.g. stone have a max:min of 1:0 in all direction.
	 * Slabs have 0:1 in horizontal directions, but 0:0.5 or 0.5:1 depending on if it is top or bottom.
	 * @param block The block to test
	 * @param blockFace The direction to test in
	 * @return
	 */
	public static double getSize(Block block, BlockFace blockFace)
	{
		BlockPosition bpos = new BlockPosition(block.getX(), block.getY(), block.getZ());
		net.minecraft.server.v1_8_R3.Block b = ((CraftWorld)block.getWorld()).getHandle().c(bpos);

		switch (blockFace)
		{
			default:
			case WEST:
				return b.B();	//min-x
			case EAST:
				return b.C();	//max-x
			case DOWN:
				return b.D(); 	//min-y
			case UP:
				return b.E(); 	//max-y
			case NORTH:
				return b.F(); 	//min-z
			case SOUTH:
				return b.G(); 	//max-z
		}

	}

	public static boolean water(Material type)
	{
		return type == Material.WATER || type == Material.STATIONARY_WATER;
	}
	
	public static boolean lava(Material type)
	{
		return type == Material.LAVA || type == Material.STATIONARY_LAVA;
	}
	
	public static boolean liquid(Material type)
	{
		return water(type) || lava(type);
	}
	
	public static boolean water(Block block)
	{
		return water(block.getType());
	}
	
	public static boolean lava(Block block)
	{
		return lava(block.getType());
	}
	
	public static boolean liquid(Block block)
	{
		return liquid(block.getType());
	}

	public static Skull blockToSkull(Block block, SkinData skinData) throws Exception
	{
		block.setType(Material.SKULL);
		block.setData((byte) 1);
		if (block.getState() instanceof Skull)
		{
			Skull skull = (Skull) block.getState();
			skull.setSkullType(SkullType.PLAYER);
			Field field = Class.forName("org.bukkit.craftbukkit.v1_8_R3.block.CraftSkull").getDeclaredField("profile");
			field.setAccessible(true);
			GameProfile data = new GameProfile(UUID.randomUUID(), SkinData.getUnusedSkullName());
			data.getProperties().put("textures", skinData.getProperty());
			field.set(skull, data);
			skull.update();
			return skull;
		}
		return null;
	}
	
	/**
	 * Returns a {@link Set} containing all the relevant data regarding beacon construction.
	 * Useful for adding them to block restore.
	 * 
	 * @param surface
	 * 		The Location of the glass coloured block (at surface level). The beacon is placed one block below this.
	 * @param glassData
	 * 		The colour data value of glass that colours the beacon
	 */
	public static Set<Pair<Location, Pair<Material, Byte>>> getBeaconBlocks(Location surface, byte glassData)
	{
		Set<Pair<Location, Pair<Material, Byte>>> blocks = new HashSet<>();
		
		for (int x = -1; x <= 1; x++)
		{
			for (int z = -1; z <= 1; z++)
			{
				blocks.add(Pair.create(surface.clone().add(x, -3, z), Pair.create(Material.IRON_BLOCK, (byte) 0)));

				if (x == 0 && z == 0)
				{
					continue;
				}

				blocks.add(Pair.create(surface.clone().add(x, -1, z), Pair.create(Material.QUARTZ_BLOCK, (byte) 0)));
			}
		}

		blocks.add(Pair.create(surface.clone().add(0, -2, 0), Pair.create(Material.BEACON, (byte) 0)));
		blocks.add(Pair.create(surface.clone().add(0, -1, 0), Pair.create(Material.STAINED_GLASS, glassData)));
		
		return blocks;
	}

	public static BlockFace getFace(float yaw)
	{
		return horizontals.get(Math.round(yaw / 90F) & 0x3);
	}

	public static boolean isFence(Block block)
	{
		return isFence(block.getType());
	}

	public static boolean isFence(Material type)
	{
		switch (type)
		{
			case FENCE:
			case FENCE_GATE:
			case ACACIA_FENCE:
			case BIRCH_FENCE:
			case DARK_OAK_FENCE:
			case IRON_FENCE:
			case JUNGLE_FENCE:
			case NETHER_FENCE:
			case SPRUCE_FENCE:
			case ACACIA_FENCE_GATE:
			case BIRCH_FENCE_GATE:
			case DARK_OAK_FENCE_GATE:
			case JUNGLE_FENCE_GATE:
			case SPRUCE_FENCE_GATE:
				return true;
		}

		return false;
	}

	public static boolean isSlab(Block block)
	{
		return isSlab(block.getType());
	}

	public static boolean isSlab(Material type)
	{
		switch (type)
		{
			case STEP:
			case WOOD_STEP:
			case STONE_SLAB2:
				return true;
		}

		return false;
	}

}
