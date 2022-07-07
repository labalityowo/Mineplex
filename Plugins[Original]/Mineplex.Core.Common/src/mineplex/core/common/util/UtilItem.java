package mineplex.core.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagLong;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mineplex.core.common.structs.ItemContainer;

public class UtilItem
{
	private static Map<Material, EnumSet<ItemCategory>> _materials = new HashMap<>();
	
	static
	{
		// Blocks
		
		_materials.put(Material.AIR, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.BOUNDLESS));
		_materials.put(Material.STONE, EnumSet.of(ItemCategory.BLOCK));
		_materials.put(Material.GRASS, EnumSet.of(ItemCategory.BLOCK));
		_materials.put(Material.DIRT, EnumSet.of(ItemCategory.BLOCK));
		_materials.put(Material.COBBLESTONE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.STONE));
		_materials.put(Material.WOOD, EnumSet.of(ItemCategory.BLOCK, ItemCategory.WOOD));
		_materials.put(Material.SAPLING, EnumSet.of(ItemCategory.BLOCK));
		_materials.put(Material.BEDROCK, EnumSet.of(ItemCategory.BLOCK));
		_materials.put(Material.WATER, EnumSet.of(ItemCategory.BLOCK, ItemCategory.LIQUID, ItemCategory.TRANSLUCENT));
		_materials.put(Material.STATIONARY_WATER, EnumSet.of(ItemCategory.BLOCK, ItemCategory.LIQUID, ItemCategory.TRANSLUCENT));
		_materials.put(Material.LAVA, EnumSet.of(ItemCategory.LIQUID, ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.LIGHT_EMITTING));
		_materials.put(Material.STATIONARY_LAVA, EnumSet.of(ItemCategory.BLOCK, ItemCategory.LIQUID, ItemCategory.TRANSLUCENT, ItemCategory.LIGHT_EMITTING));
		_materials.put(Material.SAND, EnumSet.of(ItemCategory.BLOCK, ItemCategory.PHYSICS));
		_materials.put(Material.GRAVEL, EnumSet.of(ItemCategory.BLOCK, ItemCategory.PHYSICS));
		_materials.put(Material.GOLD_ORE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.ORE, ItemCategory.GOLD));
		_materials.put(Material.IRON_ORE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.ORE, ItemCategory.IRON));
		_materials.put(Material.IRON_ORE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.ORE, ItemCategory.IRON));
		_materials.put(Material.COAL_ORE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.ORE, ItemCategory.IRON));
		_materials.put(Material.LOG, EnumSet.of(ItemCategory.BLOCK, ItemCategory.WOOD, ItemCategory.LOG));
		_materials.put(Material.LEAVES, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.LEAVES));
		_materials.put(Material.SPONGE, EnumSet.of(ItemCategory.BLOCK));
		_materials.put(Material.GLASS, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.GLASS));
		_materials.put(Material.LAPIS_ORE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.ORE));
		_materials.put(Material.LAPIS_BLOCK, EnumSet.of(ItemCategory.BLOCK, ItemCategory.COMPACT_BLOCK));
		_materials.put(Material.DISPENSER, EnumSet.of(ItemCategory.BLOCK, ItemCategory.GUI));
		_materials.put(Material.SANDSTONE, EnumSet.of(ItemCategory.BLOCK));
		_materials.put(Material.NOTE_BLOCK, EnumSet.of(ItemCategory.BLOCK));
		_materials.put(Material.BED_BLOCK, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.POWERED_RAIL, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.DETECTOR_RAIL, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.PISTON_STICKY_BASE, EnumSet.of(ItemCategory.BLOCK));
		_materials.put(Material.WEB, EnumSet.of(ItemCategory.BLOCK, ItemCategory.MOVEMENT_MODIFYING, ItemCategory.TRANSLUCENT));
		_materials.put(Material.LONG_GRASS, EnumSet.of(ItemCategory.BLOCK, ItemCategory.BOUNDLESS, ItemCategory.TRANSLUCENT));
		_materials.put(Material.DEAD_BUSH, EnumSet.of(ItemCategory.BLOCK, ItemCategory.BOUNDLESS, ItemCategory.TRANSLUCENT));
		_materials.put(Material.PISTON_BASE, EnumSet.of(ItemCategory.BLOCK));
		_materials.put(Material.PISTON_EXTENSION, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.WOOL, EnumSet.of(ItemCategory.BLOCK));
		_materials.put(Material.PISTON_MOVING_PIECE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.BOUNDLESS, ItemCategory.TRANSLUCENT));
		_materials.put(Material.YELLOW_FLOWER, EnumSet.of(ItemCategory.BLOCK, ItemCategory.BOUNDLESS, ItemCategory.TRANSLUCENT));
		_materials.put(Material.RED_ROSE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.BOUNDLESS, ItemCategory.TRANSLUCENT));
		_materials.put(Material.BROWN_MUSHROOM, EnumSet.of(ItemCategory.BLOCK, ItemCategory.BOUNDLESS, ItemCategory.TRANSLUCENT));
		_materials.put(Material.RED_MUSHROOM, EnumSet.of(ItemCategory.BLOCK, ItemCategory.BOUNDLESS, ItemCategory.TRANSLUCENT));
		_materials.put(Material.GOLD_BLOCK, EnumSet.of(ItemCategory.BLOCK, ItemCategory.COMPACT_BLOCK));
		_materials.put(Material.IRON_BLOCK, EnumSet.of(ItemCategory.BLOCK, ItemCategory.COMPACT_BLOCK));
		_materials.put(Material.DOUBLE_STEP, EnumSet.of(ItemCategory.BLOCK));
		_materials.put(Material.STEP, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.BRICK, EnumSet.of(ItemCategory.BLOCK));
		_materials.put(Material.TNT, EnumSet.of(ItemCategory.BLOCK));
		_materials.put(Material.BOOKSHELF, EnumSet.of(ItemCategory.BLOCK));
		_materials.put(Material.MOSSY_COBBLESTONE, EnumSet.of(ItemCategory.BLOCK));
		_materials.put(Material.OBSIDIAN, EnumSet.of(ItemCategory.BLOCK));
		_materials.put(Material.TORCH, EnumSet.of(ItemCategory.BLOCK, ItemCategory.LIGHT_EMITTING, ItemCategory.BOUNDLESS));
		_materials.put(Material.FIRE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.LIGHT_EMITTING, ItemCategory.TRANSLUCENT, ItemCategory.BOUNDLESS));
		_materials.put(Material.MOB_SPAWNER, EnumSet.of(ItemCategory.BLOCK, ItemCategory.LIGHT_EMITTING, ItemCategory.TRANSLUCENT));
		_materials.put(Material.WOOD_STAIRS, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.CHEST, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.GUI));
		_materials.put(Material.REDSTONE_WIRE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.BOUNDLESS, ItemCategory.REDSTONE));
		_materials.put(Material.DIAMOND_ORE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.ORE, ItemCategory.DIAMOND));
		_materials.put(Material.DIAMOND_BLOCK, EnumSet.of(ItemCategory.BLOCK, ItemCategory.COMPACT_BLOCK, ItemCategory.DIAMOND));
		_materials.put(Material.WORKBENCH, EnumSet.of(ItemCategory.BLOCK, ItemCategory.GUI));
		_materials.put(Material.CROPS, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.BOUNDLESS));
		_materials.put(Material.SOIL, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.FURNACE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.GUI));
		_materials.put(Material.BURNING_FURNACE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.LIGHT_EMITTING, ItemCategory.GUI));
		_materials.put(Material.SIGN_POST, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.BOUNDLESS));
		_materials.put(Material.WOODEN_DOOR, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.DOOR));
		_materials.put(Material.LADDER, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.CLIMBABLE));
		_materials.put(Material.RAILS, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.BOUNDLESS));
		_materials.put(Material.COBBLESTONE_STAIRS, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.WALL_SIGN, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.BOUNDLESS));
		_materials.put(Material.LEVER, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.BOUNDLESS));
		_materials.put(Material.STONE_PLATE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.BOUNDLESS));
		_materials.put(Material.IRON_DOOR_BLOCK, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.DOOR));
		_materials.put(Material.WOOD_PLATE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.BOUNDLESS));
		_materials.put(Material.REDSTONE_ORE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.ORE, ItemCategory.REDSTONE));
		_materials.put(Material.GLOWING_REDSTONE_ORE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.ORE, ItemCategory.REDSTONE));
		_materials.put(Material.REDSTONE_TORCH_OFF, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.REDSTONE, ItemCategory.BOUNDLESS));
		_materials.put(Material.REDSTONE_TORCH_ON, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.REDSTONE, ItemCategory.BOUNDLESS, ItemCategory.LIGHT_EMITTING));
		_materials.put(Material.STONE_BUTTON, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.BOUNDLESS));
		_materials.put(Material.SNOW, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.ICE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.MOVEMENT_MODIFYING));
		_materials.put(Material.SNOW_BLOCK, EnumSet.of(ItemCategory.BLOCK));
		_materials.put(Material.CACTUS, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.CLAY, EnumSet.of(ItemCategory.BLOCK));
		_materials.put(Material.SUGAR_CANE_BLOCK, EnumSet.of(ItemCategory.BLOCK, ItemCategory.BOUNDLESS, ItemCategory.TRANSLUCENT));
		_materials.put(Material.JUKEBOX, EnumSet.of(ItemCategory.BLOCK));
		_materials.put(Material.FENCE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.PUMPKIN, EnumSet.of(ItemCategory.BLOCK));
		_materials.put(Material.NETHERRACK, EnumSet.of(ItemCategory.BLOCK, ItemCategory.NETHER));
		_materials.put(Material.SOUL_SAND, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.NETHER));
		_materials.put(Material.GLOWSTONE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.LIGHT_EMITTING, ItemCategory.NETHER));
		_materials.put(Material.PORTAL, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.LIGHT_EMITTING, ItemCategory.BOUNDLESS));
		_materials.put(Material.JACK_O_LANTERN, EnumSet.of(ItemCategory.BLOCK, ItemCategory.LIGHT_EMITTING));
		_materials.put(Material.CAKE_BLOCK, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.DIODE_BLOCK_OFF, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.REDSTONE));
		_materials.put(Material.DIODE_BLOCK_ON, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.REDSTONE));
		_materials.put(Material.STAINED_GLASS, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.GLASS));
		_materials.put(Material.TRAP_DOOR, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.DOOR));
		_materials.put(Material.MONSTER_EGGS, EnumSet.of(ItemCategory.BLOCK));
		_materials.put(Material.SMOOTH_BRICK, EnumSet.of(ItemCategory.BLOCK));
		_materials.put(Material.HUGE_MUSHROOM_1, EnumSet.of(ItemCategory.BLOCK));
		_materials.put(Material.HUGE_MUSHROOM_2, EnumSet.of(ItemCategory.BLOCK));
		_materials.put(Material.IRON_FENCE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.THIN_GLASS, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.GLASS));
		_materials.put(Material.MELON_BLOCK, EnumSet.of(ItemCategory.BLOCK));
		_materials.put(Material.PUMPKIN_STEM, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.BOUNDLESS));
		_materials.put(Material.MELON_STEM, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.BOUNDLESS));
		_materials.put(Material.VINE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.BOUNDLESS, ItemCategory.CLIMBABLE));
		_materials.put(Material.FENCE_GATE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.BRICK_STAIRS, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.SMOOTH_STAIRS, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.MYCEL, EnumSet.of(ItemCategory.BLOCK));
		_materials.put(Material.WATER_LILY, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.NETHER_BRICK, EnumSet.of(ItemCategory.BLOCK, ItemCategory.NETHER));
		_materials.put(Material.NETHER_FENCE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.NETHER));
		_materials.put(Material.NETHER_BRICK_STAIRS, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.NETHER));
		_materials.put(Material.NETHER_WARTS, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.BOUNDLESS, ItemCategory.NETHER));
		_materials.put(Material.ENCHANTMENT_TABLE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.BREWING_STAND, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.CAULDRON, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.ENDER_PORTAL, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.BOUNDLESS, ItemCategory.LIGHT_EMITTING));
		_materials.put(Material.ENDER_PORTAL_FRAME, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.ENDER_STONE, EnumSet.of(ItemCategory.BLOCK));
		_materials.put(Material.DRAGON_EGG, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.REDSTONE_LAMP_OFF, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.REDSTONE));
		_materials.put(Material.REDSTONE_LAMP_ON, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.REDSTONE, ItemCategory.LIGHT_EMITTING));
		_materials.put(Material.WOOD_DOUBLE_STEP, EnumSet.of(ItemCategory.BLOCK));
		_materials.put(Material.WOOD_STEP, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.COCOA, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.SANDSTONE_STAIRS, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.EMERALD_ORE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.ORE));
		_materials.put(Material.ENDER_CHEST, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.TRIPWIRE_HOOK, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.BOUNDLESS, ItemCategory.REDSTONE));
		_materials.put(Material.TRIPWIRE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.BOUNDLESS));
		_materials.put(Material.EMERALD_BLOCK, EnumSet.of(ItemCategory.BLOCK, ItemCategory.COMPACT_BLOCK));
		_materials.put(Material.SPRUCE_WOOD_STAIRS, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.BIRCH_WOOD_STAIRS, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.JUNGLE_WOOD_STAIRS, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.COMMAND, EnumSet.of(ItemCategory.BLOCK, ItemCategory.GUI));
		_materials.put(Material.BEACON, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.LIGHT_EMITTING));
		_materials.put(Material.COBBLE_WALL, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.FLOWER_POT, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.CARROT, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.BOUNDLESS));
		_materials.put(Material.POTATO, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.BOUNDLESS));
		_materials.put(Material.WOOD_BUTTON, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.BOUNDLESS));
		_materials.put(Material.SKULL, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.ANVIL, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.GUI));
		_materials.put(Material.TRAPPED_CHEST, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.GUI));
		_materials.put(Material.GOLD_PLATE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.BOUNDLESS));
		_materials.put(Material.IRON_PLATE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.BOUNDLESS));
		_materials.put(Material.REDSTONE_COMPARATOR_OFF, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.REDSTONE));
		_materials.put(Material.REDSTONE_COMPARATOR_ON, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.REDSTONE));
		_materials.put(Material.DAYLIGHT_DETECTOR, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.REDSTONE_BLOCK, EnumSet.of(ItemCategory.BLOCK, ItemCategory.REDSTONE));
		_materials.put(Material.QUARTZ_ORE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.ORE, ItemCategory.NETHER));
		_materials.put(Material.HOPPER, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.GUI));
		_materials.put(Material.QUARTZ_BLOCK, EnumSet.of(ItemCategory.BLOCK, ItemCategory.COMPACT_BLOCK, ItemCategory.NETHER));
		_materials.put(Material.QUARTZ_STAIRS, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.NETHER));
		_materials.put(Material.ACTIVATOR_RAIL, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.BOUNDLESS));
		_materials.put(Material.DROPPER, EnumSet.of(ItemCategory.BLOCK, ItemCategory.GUI));
		_materials.put(Material.STAINED_CLAY, EnumSet.of(ItemCategory.BLOCK));
		_materials.put(Material.STAINED_GLASS_PANE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.GLASS));
		_materials.put(Material.LEAVES_2, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.LEAVES));
		_materials.put(Material.LOG_2, EnumSet.of(ItemCategory.BLOCK, ItemCategory.WOOD, ItemCategory.LOG));
		_materials.put(Material.ACACIA_STAIRS, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.DARK_OAK_STAIRS, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.HAY_BLOCK, EnumSet.of(ItemCategory.BLOCK));
		_materials.put(Material.CARPET, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.HARD_CLAY, EnumSet.of(ItemCategory.BLOCK));
		_materials.put(Material.COAL_BLOCK, EnumSet.of(ItemCategory.BLOCK, ItemCategory.COMPACT_BLOCK));
		_materials.put(Material.PACKED_ICE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.MOVEMENT_MODIFYING));
		_materials.put(Material.DOUBLE_PLANT, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.BOUNDLESS));
		
		_materials.put(Material.SLIME_BLOCK, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.BARRIER, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.IRON_TRAPDOOR, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.DOOR));
		_materials.put(Material.PRISMARINE, EnumSet.of(ItemCategory.BLOCK));
		_materials.put(Material.SEA_LANTERN, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.LIGHT_EMITTING));
		_materials.put(Material.STANDING_BANNER, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.WALL_BANNER, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.DAYLIGHT_DETECTOR_INVERTED, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT));
		_materials.put(Material.RED_SANDSTONE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.STONE));
		_materials.put(Material.RED_SANDSTONE_STAIRS, EnumSet.of(ItemCategory.BLOCK, ItemCategory.STONE, ItemCategory.TRANSLUCENT));
		_materials.put(Material.DOUBLE_STONE_SLAB2, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.STONE));
		_materials.put(Material.STONE_SLAB2, EnumSet.of(ItemCategory.BLOCK, ItemCategory.STONE, ItemCategory.TRANSLUCENT));
		_materials.put(Material.SPRUCE_FENCE_GATE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.WOOD));
		_materials.put(Material.BIRCH_FENCE_GATE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.WOOD));
		_materials.put(Material.JUNGLE_FENCE_GATE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.WOOD));
		_materials.put(Material.DARK_OAK_FENCE_GATE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.WOOD));
		_materials.put(Material.ACACIA_FENCE_GATE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.WOOD));
		_materials.put(Material.SPRUCE_FENCE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.WOOD));
		_materials.put(Material.BIRCH_FENCE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.WOOD));
		_materials.put(Material.JUNGLE_FENCE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.WOOD));
		_materials.put(Material.SPRUCE_DOOR, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.WOOD, ItemCategory.DOOR));
		_materials.put(Material.BIRCH_DOOR, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.WOOD, ItemCategory.DOOR));
		_materials.put(Material.JUNGLE_DOOR, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.WOOD, ItemCategory.DOOR));
		_materials.put(Material.ACACIA_DOOR, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.WOOD, ItemCategory.DOOR));
		_materials.put(Material.DARK_OAK_DOOR, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.WOOD, ItemCategory.DOOR));
		
		
		_materials.put(Material.DARK_OAK_FENCE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.WOOD));
		_materials.put(Material.ACACIA_FENCE, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.WOOD));
		_materials.put(Material.DARK_OAK_DOOR_ITEM, EnumSet.of(ItemCategory.BLOCK, ItemCategory.TRANSLUCENT, ItemCategory.WOOD, ItemCategory.DOOR));
		
		// Items
		
		_materials.put(Material.PRISMARINE_SHARD, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.PRISMARINE_CRYSTALS, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.RABBIT, EnumSet.of(ItemCategory.ITEM, ItemCategory.RAW_FOOD, ItemCategory.EDIBLE));
		_materials.put(Material.COOKED_RABBIT, EnumSet.of(ItemCategory.ITEM, ItemCategory.EDIBLE));
		_materials.put(Material.RABBIT_STEW, EnumSet.of(ItemCategory.ITEM, ItemCategory.EDIBLE));
		_materials.put(Material.RABBIT_FOOT, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.RABBIT_HIDE, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.ARMOR_STAND, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.MUTTON, EnumSet.of(ItemCategory.ITEM, ItemCategory.RAW_FOOD, ItemCategory.EDIBLE));
		_materials.put(Material.COOKED_MUTTON, EnumSet.of(ItemCategory.ITEM, ItemCategory.EDIBLE));
		_materials.put(Material.BANNER, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.SPRUCE_DOOR_ITEM, EnumSet.of(ItemCategory.ITEM, ItemCategory.WOOD, ItemCategory.DOOR));
		_materials.put(Material.BIRCH_DOOR_ITEM, EnumSet.of(ItemCategory.ITEM, ItemCategory.WOOD, ItemCategory.DOOR));
		_materials.put(Material.JUNGLE_DOOR_ITEM, EnumSet.of(ItemCategory.ITEM, ItemCategory.WOOD, ItemCategory.DOOR));
		_materials.put(Material.ACACIA_DOOR_ITEM, EnumSet.of(ItemCategory.ITEM, ItemCategory.WOOD, ItemCategory.DOOR));
		
		
		_materials.put(Material.IRON_SPADE, EnumSet.of(ItemCategory.ITEM, ItemCategory.IRON, ItemCategory.TOOL, ItemCategory.SHOVEL));
		_materials.put(Material.IRON_PICKAXE, EnumSet.of(ItemCategory.ITEM, ItemCategory.IRON, ItemCategory.TOOL, ItemCategory.PICKAXE));
		_materials.put(Material.IRON_AXE, EnumSet.of(ItemCategory.AXE, ItemCategory.ITEM, ItemCategory.IRON, ItemCategory.WEAPON, ItemCategory.TOOL));
		_materials.put(Material.FLINT_AND_STEEL, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.APPLE, EnumSet.of(ItemCategory.ITEM, ItemCategory.EDIBLE));
		_materials.put(Material.BOW, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.ARROW, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.COAL, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.DIAMOND, EnumSet.of(ItemCategory.ITEM, ItemCategory.DIAMOND));
		_materials.put(Material.IRON_INGOT, EnumSet.of(ItemCategory.ITEM, ItemCategory.IRON));
		_materials.put(Material.GOLD_INGOT, EnumSet.of(ItemCategory.ITEM, ItemCategory.GOLD));
		_materials.put(Material.IRON_SWORD, EnumSet.of(ItemCategory.SWORD, ItemCategory.ITEM, ItemCategory.IRON, ItemCategory.WEAPON, ItemCategory.TOOL));
		_materials.put(Material.WOOD_SWORD, EnumSet.of(ItemCategory.SWORD, ItemCategory.ITEM, ItemCategory.WOOD, ItemCategory.WEAPON, ItemCategory.TOOL));
		_materials.put(Material.WOOD_SPADE, EnumSet.of(ItemCategory.ITEM, ItemCategory.WOOD, ItemCategory.TOOL, ItemCategory.SHOVEL));
		_materials.put(Material.WOOD_PICKAXE, EnumSet.of(ItemCategory.ITEM, ItemCategory.WOOD, ItemCategory.TOOL, ItemCategory.PICKAXE));
		_materials.put(Material.WOOD_AXE, EnumSet.of(ItemCategory.AXE, ItemCategory.ITEM, ItemCategory.WOOD, ItemCategory.WEAPON, ItemCategory.TOOL));
		_materials.put(Material.STONE_SWORD, EnumSet.of(ItemCategory.SWORD, ItemCategory.ITEM, ItemCategory.STONE, ItemCategory.WEAPON, ItemCategory.TOOL));
		_materials.put(Material.STONE_SPADE, EnumSet.of(ItemCategory.ITEM, ItemCategory.STONE, ItemCategory.TOOL, ItemCategory.SHOVEL));
		_materials.put(Material.STONE_PICKAXE, EnumSet.of(ItemCategory.ITEM, ItemCategory.STONE, ItemCategory.TOOL, ItemCategory.PICKAXE));
		_materials.put(Material.STONE_AXE, EnumSet.of(ItemCategory.AXE, ItemCategory.ITEM, ItemCategory.STONE, ItemCategory.WEAPON, ItemCategory.TOOL));
		_materials.put(Material.DIAMOND_SWORD, EnumSet.of(ItemCategory.SWORD, ItemCategory.ITEM, ItemCategory.DIAMOND, ItemCategory.WEAPON, ItemCategory.TOOL));
		_materials.put(Material.DIAMOND_SPADE, EnumSet.of(ItemCategory.ITEM, ItemCategory.DIAMOND, ItemCategory.TOOL, ItemCategory.SHOVEL));
		_materials.put(Material.DIAMOND_PICKAXE, EnumSet.of(ItemCategory.ITEM, ItemCategory.DIAMOND, ItemCategory.TOOL, ItemCategory.PICKAXE));
		_materials.put(Material.DIAMOND_AXE, EnumSet.of(ItemCategory.AXE, ItemCategory.ITEM, ItemCategory.DIAMOND, ItemCategory.TOOL, ItemCategory.WEAPON));
		_materials.put(Material.STICK, EnumSet.of(ItemCategory.ITEM, ItemCategory.WOOD));
		_materials.put(Material.BOWL, EnumSet.of(ItemCategory.ITEM, ItemCategory.WOOD));
		_materials.put(Material.MUSHROOM_SOUP, EnumSet.of(ItemCategory.ITEM, ItemCategory.EDIBLE));
		_materials.put(Material.GOLD_SWORD, EnumSet.of(ItemCategory.SWORD, ItemCategory.ITEM, ItemCategory.GOLD, ItemCategory.WEAPON, ItemCategory.TOOL));
		_materials.put(Material.GOLD_SPADE, EnumSet.of(ItemCategory.ITEM, ItemCategory.GOLD, ItemCategory.TOOL, ItemCategory.SHOVEL));
		_materials.put(Material.GOLD_PICKAXE, EnumSet.of(ItemCategory.ITEM, ItemCategory.GOLD, ItemCategory.TOOL, ItemCategory.PICKAXE));
		_materials.put(Material.GOLD_AXE, EnumSet.of(ItemCategory.AXE, ItemCategory.ITEM, ItemCategory.GOLD, ItemCategory.WEAPON, ItemCategory.TOOL));
		_materials.put(Material.STRING, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.FEATHER, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.SULPHUR, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.WOOD_HOE, EnumSet.of(ItemCategory.ITEM, ItemCategory.TOOL, ItemCategory.WOOD, ItemCategory.HOE));
		_materials.put(Material.STONE_HOE, EnumSet.of(ItemCategory.ITEM, ItemCategory.TOOL, ItemCategory.STONE, ItemCategory.HOE));
		_materials.put(Material.IRON_HOE, EnumSet.of(ItemCategory.ITEM, ItemCategory.TOOL, ItemCategory.IRON, ItemCategory.HOE));
		_materials.put(Material.DIAMOND_HOE, EnumSet.of(ItemCategory.ITEM, ItemCategory.TOOL, ItemCategory.DIAMOND, ItemCategory.HOE));
		_materials.put(Material.GOLD_HOE, EnumSet.of(ItemCategory.ITEM, ItemCategory.TOOL, ItemCategory.GOLD, ItemCategory.HOE));
		_materials.put(Material.SEEDS, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.WHEAT, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.BREAD, EnumSet.of(ItemCategory.ITEM, ItemCategory.EDIBLE));
		_materials.put(Material.LEATHER_HELMET, EnumSet.of(ItemCategory.ITEM, ItemCategory.LEATHER, ItemCategory.ARMOR_HELMET, ItemCategory.ARMOR));
		_materials.put(Material.LEATHER_CHESTPLATE, EnumSet.of(ItemCategory.ITEM, ItemCategory.LEATHER, ItemCategory.ARMOR_CHESTPLATE, ItemCategory.ARMOR));
		_materials.put(Material.LEATHER_LEGGINGS, EnumSet.of(ItemCategory.ITEM, ItemCategory.LEATHER, ItemCategory.ARMOR_LEGGINGS, ItemCategory.ARMOR));
		_materials.put(Material.LEATHER_BOOTS, EnumSet.of(ItemCategory.ITEM, ItemCategory.LEATHER, ItemCategory.ARMOR_BOOTS, ItemCategory.ARMOR));
		_materials.put(Material.CHAINMAIL_HELMET, EnumSet.of(ItemCategory.ITEM, ItemCategory.CHAINMAIL, ItemCategory.ARMOR_HELMET, ItemCategory.ARMOR));
		_materials.put(Material.CHAINMAIL_CHESTPLATE, EnumSet.of(ItemCategory.ITEM, ItemCategory.CHAINMAIL, ItemCategory.ARMOR_CHESTPLATE, ItemCategory.ARMOR));
		_materials.put(Material.CHAINMAIL_LEGGINGS, EnumSet.of(ItemCategory.ITEM, ItemCategory.CHAINMAIL, ItemCategory.ARMOR_LEGGINGS, ItemCategory.ARMOR));
		_materials.put(Material.CHAINMAIL_BOOTS, EnumSet.of(ItemCategory.ITEM, ItemCategory.CHAINMAIL, ItemCategory.ARMOR_BOOTS, ItemCategory.ARMOR));
		_materials.put(Material.IRON_HELMET, EnumSet.of(ItemCategory.ITEM, ItemCategory.IRON, ItemCategory.ARMOR_HELMET, ItemCategory.ARMOR));
		_materials.put(Material.IRON_CHESTPLATE, EnumSet.of(ItemCategory.ITEM, ItemCategory.IRON, ItemCategory.ARMOR_CHESTPLATE, ItemCategory.ARMOR));
		_materials.put(Material.IRON_LEGGINGS, EnumSet.of(ItemCategory.ITEM, ItemCategory.IRON, ItemCategory.ARMOR_LEGGINGS, ItemCategory.ARMOR));
		_materials.put(Material.IRON_BOOTS, EnumSet.of(ItemCategory.ITEM, ItemCategory.IRON, ItemCategory.ARMOR_BOOTS, ItemCategory.ARMOR));
		_materials.put(Material.DIAMOND_HELMET, EnumSet.of(ItemCategory.ITEM, ItemCategory.DIAMOND, ItemCategory.ARMOR_HELMET, ItemCategory.ARMOR));
		_materials.put(Material.DIAMOND_CHESTPLATE, EnumSet.of(ItemCategory.ITEM, ItemCategory.DIAMOND, ItemCategory.ARMOR_CHESTPLATE, ItemCategory.ARMOR));
		_materials.put(Material.DIAMOND_LEGGINGS, EnumSet.of(ItemCategory.ITEM, ItemCategory.DIAMOND, ItemCategory.ARMOR_LEGGINGS, ItemCategory.ARMOR));
		_materials.put(Material.DIAMOND_BOOTS, EnumSet.of(ItemCategory.ITEM, ItemCategory.DIAMOND, ItemCategory.ARMOR_BOOTS, ItemCategory.ARMOR));
		_materials.put(Material.GOLD_HELMET, EnumSet.of(ItemCategory.ITEM, ItemCategory.GOLD, ItemCategory.ARMOR_HELMET, ItemCategory.ARMOR));
		_materials.put(Material.GOLD_CHESTPLATE, EnumSet.of(ItemCategory.ITEM, ItemCategory.GOLD, ItemCategory.ARMOR_CHESTPLATE, ItemCategory.ARMOR));
		_materials.put(Material.GOLD_LEGGINGS, EnumSet.of(ItemCategory.ITEM, ItemCategory.GOLD, ItemCategory.ARMOR_LEGGINGS, ItemCategory.ARMOR));
		_materials.put(Material.GOLD_BOOTS, EnumSet.of(ItemCategory.ITEM, ItemCategory.GOLD, ItemCategory.ARMOR_BOOTS, ItemCategory.ARMOR));
		_materials.put(Material.FLINT, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.PORK, EnumSet.of(ItemCategory.ITEM, ItemCategory.EDIBLE, ItemCategory.RAW_FOOD));
		_materials.put(Material.GRILLED_PORK, EnumSet.of(ItemCategory.ITEM, ItemCategory.EDIBLE));
		_materials.put(Material.PAINTING, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.GOLDEN_APPLE, EnumSet.of(ItemCategory.ITEM, ItemCategory.EDIBLE));
		_materials.put(Material.SIGN, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.WOOD_DOOR, EnumSet.of(ItemCategory.ITEM, ItemCategory.ITEM_BLOCK, ItemCategory.DOOR));
		_materials.put(Material.BUCKET, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.WATER_BUCKET, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.LAVA_BUCKET, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.MINECART, EnumSet.of(ItemCategory.ITEM, ItemCategory.VEHICLE));
		_materials.put(Material.SADDLE, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.IRON_DOOR, EnumSet.of(ItemCategory.ITEM, ItemCategory.ITEM_BLOCK, ItemCategory.DOOR));
		_materials.put(Material.REDSTONE, EnumSet.of(ItemCategory.ITEM, ItemCategory.REDSTONE));
		_materials.put(Material.SNOW_BALL, EnumSet.of(ItemCategory.ITEM, ItemCategory.THROWABLE));
		_materials.put(Material.BOAT, EnumSet.of(ItemCategory.ITEM, ItemCategory.VEHICLE));
		_materials.put(Material.LEATHER, EnumSet.of(ItemCategory.ITEM, ItemCategory.LEATHER));
		_materials.put(Material.MILK_BUCKET, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.CLAY_BRICK, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.CLAY_BALL, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.SUGAR_CANE, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.PAPER, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.BOOK, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.SLIME_BALL, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.STORAGE_MINECART, EnumSet.of(ItemCategory.ITEM, ItemCategory.VEHICLE));
		_materials.put(Material.POWERED_MINECART, EnumSet.of(ItemCategory.ITEM, ItemCategory.VEHICLE));
		_materials.put(Material.EGG, EnumSet.of(ItemCategory.ITEM, ItemCategory.THROWABLE));
		_materials.put(Material.COMPASS, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.FISHING_ROD, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.WATCH, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.GLOWSTONE_DUST, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.RAW_FISH, EnumSet.of(ItemCategory.ITEM, ItemCategory.EDIBLE, ItemCategory.RAW_FOOD));
		_materials.put(Material.COOKED_FISH, EnumSet.of(ItemCategory.ITEM, ItemCategory.EDIBLE));
		_materials.put(Material.INK_SACK, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.BONE, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.SUGAR, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.CAKE, EnumSet.of(ItemCategory.ITEM, ItemCategory.ITEM_BLOCK));
		_materials.put(Material.BED, EnumSet.of(ItemCategory.ITEM, ItemCategory.ITEM_BLOCK));
		_materials.put(Material.DIODE, EnumSet.of(ItemCategory.ITEM, ItemCategory.REDSTONE, ItemCategory.ITEM_BLOCK));
		_materials.put(Material.COOKIE, EnumSet.of(ItemCategory.ITEM, ItemCategory.EDIBLE));
		_materials.put(Material.MAP, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.SHEARS, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.MELON, EnumSet.of(ItemCategory.ITEM, ItemCategory.EDIBLE));
		_materials.put(Material.PUMPKIN_SEEDS, EnumSet.of(ItemCategory.ITEM, ItemCategory.ITEM_BLOCK));
		_materials.put(Material.MELON_SEEDS, EnumSet.of(ItemCategory.ITEM, ItemCategory.ITEM_BLOCK));
		_materials.put(Material.RAW_BEEF, EnumSet.of(ItemCategory.ITEM, ItemCategory.EDIBLE, ItemCategory.RAW_FOOD));
		_materials.put(Material.COOKED_BEEF, EnumSet.of(ItemCategory.ITEM, ItemCategory.EDIBLE));
		_materials.put(Material.RAW_CHICKEN, EnumSet.of(ItemCategory.ITEM, ItemCategory.EDIBLE, ItemCategory.RAW_FOOD));
		_materials.put(Material.COOKED_CHICKEN, EnumSet.of(ItemCategory.ITEM, ItemCategory.EDIBLE));
		_materials.put(Material.ROTTEN_FLESH, EnumSet.of(ItemCategory.ITEM, ItemCategory.EDIBLE));
		_materials.put(Material.ENDER_PEARL, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.BLAZE_ROD, EnumSet.of(ItemCategory.ITEM, ItemCategory.NETHER));
		_materials.put(Material.GHAST_TEAR, EnumSet.of(ItemCategory.ITEM, ItemCategory.NETHER));
		_materials.put(Material.GOLD_NUGGET, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.NETHER_STALK, EnumSet.of(ItemCategory.ITEM, ItemCategory.NETHER));
		_materials.put(Material.POTION, EnumSet.of(ItemCategory.ITEM, ItemCategory.POTABLE));
		_materials.put(Material.GLASS_BOTTLE, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.SPIDER_EYE, EnumSet.of(ItemCategory.ITEM, ItemCategory.EDIBLE));
		_materials.put(Material.FERMENTED_SPIDER_EYE, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.BLAZE_POWDER, EnumSet.of(ItemCategory.ITEM, ItemCategory.NETHER));
		_materials.put(Material.MAGMA_CREAM, EnumSet.of(ItemCategory.ITEM, ItemCategory.NETHER));
		_materials.put(Material.BREWING_STAND_ITEM, EnumSet.of(ItemCategory.ITEM, ItemCategory.ITEM_BLOCK));
		_materials.put(Material.CAULDRON_ITEM, EnumSet.of(ItemCategory.ITEM, ItemCategory.ITEM_BLOCK));
		_materials.put(Material.EYE_OF_ENDER, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.SPECKLED_MELON, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.MONSTER_EGG, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.EXP_BOTTLE, EnumSet.of(ItemCategory.ITEM, ItemCategory.THROWABLE));
		_materials.put(Material.FIREBALL, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.BOOK_AND_QUILL, EnumSet.of(ItemCategory.ITEM, ItemCategory.GUI));
		_materials.put(Material.WRITTEN_BOOK, EnumSet.of(ItemCategory.ITEM, ItemCategory.GUI));
		_materials.put(Material.EMERALD, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.ITEM_FRAME, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.FLOWER_POT_ITEM, EnumSet.of(ItemCategory.ITEM, ItemCategory.ITEM_BLOCK));
		_materials.put(Material.CARROT_ITEM, EnumSet.of(ItemCategory.ITEM, ItemCategory.EDIBLE));
		_materials.put(Material.POTATO_ITEM, EnumSet.of(ItemCategory.ITEM, ItemCategory.EDIBLE, ItemCategory.RAW_FOOD));
		_materials.put(Material.BAKED_POTATO, EnumSet.of(ItemCategory.ITEM, ItemCategory.EDIBLE));
		_materials.put(Material.POISONOUS_POTATO, EnumSet.of(ItemCategory.ITEM, ItemCategory.EDIBLE));
		_materials.put(Material.EMPTY_MAP, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.GOLDEN_CARROT, EnumSet.of(ItemCategory.ITEM, ItemCategory.EDIBLE));
		_materials.put(Material.SKULL_ITEM, EnumSet.of(ItemCategory.ITEM, ItemCategory.ITEM_BLOCK));
		_materials.put(Material.CARROT_STICK, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.NETHER_STAR, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.PUMPKIN_PIE, EnumSet.of(ItemCategory.ITEM, ItemCategory.EDIBLE));
		_materials.put(Material.FIREWORK, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.FIREWORK_CHARGE, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.ENCHANTED_BOOK, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.REDSTONE_COMPARATOR, EnumSet.of(ItemCategory.ITEM, ItemCategory.REDSTONE, ItemCategory.ITEM_BLOCK));
		_materials.put(Material.NETHER_BRICK_ITEM, EnumSet.of(ItemCategory.ITEM, ItemCategory.NETHER));
		_materials.put(Material.QUARTZ, EnumSet.of(ItemCategory.ITEM, ItemCategory.NETHER));
		_materials.put(Material.EXPLOSIVE_MINECART, EnumSet.of(ItemCategory.ITEM, ItemCategory.VEHICLE));
		_materials.put(Material.HOPPER_MINECART, EnumSet.of(ItemCategory.ITEM, ItemCategory.VEHICLE));
		_materials.put(Material.IRON_BARDING, EnumSet.of(ItemCategory.ITEM, ItemCategory.IRON));
		_materials.put(Material.GOLD_BARDING, EnumSet.of(ItemCategory.ITEM, ItemCategory.GOLD));
		_materials.put(Material.DIAMOND_BARDING, EnumSet.of(ItemCategory.ITEM, ItemCategory.DIAMOND));
		_materials.put(Material.LEASH, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.NAME_TAG, EnumSet.of(ItemCategory.ITEM));
		_materials.put(Material.COMMAND_MINECART, EnumSet.of(ItemCategory.ITEM, ItemCategory.VEHICLE));
		_materials.put(Material.GOLD_RECORD, EnumSet.of(ItemCategory.ITEM, ItemCategory.MUSIC_DISC));
		_materials.put(Material.GREEN_RECORD, EnumSet.of(ItemCategory.ITEM, ItemCategory.MUSIC_DISC));
		_materials.put(Material.RECORD_3, EnumSet.of(ItemCategory.ITEM, ItemCategory.MUSIC_DISC));
		_materials.put(Material.RECORD_4, EnumSet.of(ItemCategory.ITEM, ItemCategory.MUSIC_DISC));
		_materials.put(Material.RECORD_5, EnumSet.of(ItemCategory.ITEM, ItemCategory.MUSIC_DISC));
		_materials.put(Material.RECORD_6, EnumSet.of(ItemCategory.ITEM, ItemCategory.MUSIC_DISC));
		_materials.put(Material.RECORD_7, EnumSet.of(ItemCategory.ITEM, ItemCategory.MUSIC_DISC));
		_materials.put(Material.RECORD_8, EnumSet.of(ItemCategory.ITEM, ItemCategory.MUSIC_DISC));
		_materials.put(Material.RECORD_9, EnumSet.of(ItemCategory.ITEM, ItemCategory.MUSIC_DISC));
		_materials.put(Material.RECORD_10, EnumSet.of(ItemCategory.ITEM, ItemCategory.MUSIC_DISC));
		_materials.put(Material.RECORD_11, EnumSet.of(ItemCategory.ITEM, ItemCategory.MUSIC_DISC));
		_materials.put(Material.RECORD_12, EnumSet.of(ItemCategory.ITEM, ItemCategory.MUSIC_DISC));
	}
	
	public static LinkedList<ItemContainer> matchItem(Player caller, String items, boolean inform)
	{
		LinkedList<ItemContainer> matchList = new LinkedList<ItemContainer>();
		
		String failList = "";
		
		// Mass Search
		for (String cur : items.split(","))
		{
			ItemContainer match = searchItem(caller, cur, inform);
			
			if (match != null)
				matchList.add(match);
				
			else
				failList += cur + " ";
		}
		
		if (inform && failList.length() > 0)
		{
			failList = failList.substring(0, failList.length() - 1);
			UtilPlayer.message(caller, F.main("Item(s) Search", "" + C.mBody + " Invalid [" + C.mElem + failList + C.mBody + "]."));
		}
		
		return matchList;
	}
	
	public static ItemContainer searchItem(Player caller, String args, boolean inform)
	{
		LinkedList<ItemContainer> matchList = new LinkedList<ItemContainer>();
		
		for (Material cur : Material.values())
		{
			String[] arg = args.split(":");
			
			// Get Selected Name
			String name = null;
			if (arg.length > 2) name = arg[2].replaceAll("_", " ");
			
			// By Name
			if (cur.toString().equalsIgnoreCase(args)) return new ItemContainer(cur, (byte) 0, name);
			
			if (cur.toString().toLowerCase().contains(args.toLowerCase())) matchList.add(new ItemContainer(cur, (byte) 0, name));
			
			// By ID:Data:Name
			
			// ID
			int id = 0;
			try
			{
				if (arg.length > 0) id = Integer.parseInt(arg[0]);
			}
			catch (Exception e)
			{
				continue;
			}
			
			if (id != cur.getId()) continue;
			
			// Data
			byte data = 0;
			try
			{
				if (arg.length > 1) data = Byte.parseByte(arg[1]);
			}
			catch (Exception e)
			{
				continue;
			}
			
			return new ItemContainer(cur, data, name);
		}
		
		// No / Non-Unique
		if (matchList.size() != 1)
		{
			if (!inform) return null;
			
			// Inform
			UtilPlayer.message(caller, F.main("Item Search", "" + C.mCount + matchList.size() + C.mBody + " matches for [" + C.mElem + args + C.mBody + "]."));
			
			if (matchList.size() > 0)
			{
				String matchString = "";
				for (ItemContainer cur : matchList)
					matchString += F.elem(cur.Type.toString()) + ", ";
					
				if (matchString.length() > 1) matchString = matchString.substring(0, matchString.length() - 2);
				
				UtilPlayer.message(caller, F.main("Item Search", "" + C.mBody + "Matches [" + C.mElem + matchString + C.mBody + "]."));
			}
			
			return null;
		}
		
		return matchList.get(0);
	}
	
	public static String itemToStr(ItemStack item)
	{
		String data = "0";
		if (item.getData() != null) data = item.getData().getData() + "";
		
		return item.getType() + ":" + item.getAmount() + ":" + item.getDurability() + ":" + data;
	}
	
	/**
	 * @param item - the item to be checked for material type
	 * @param material - the material to check if it matches
	 * @return true, if {@code item} is non-null and its material type matches
	 *         {@code material}.
	 */
	public static boolean matchesMaterial(ItemStack item, Material material)
	{
		return item != null && item.getType() == material;
	}
	
	public static boolean isFood(ItemStack item)
	{
		return isEdible(item);
	}
	
	public static boolean isFood(Material material)
	{
		return isEdible(material);
	}
	
	public static boolean isSword(ItemStack stack)
	{
		return isSword(stack == null ? null : stack.getType());
	}
	
	public static boolean isSword(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.SWORD));
	}
	
	public static boolean isEdible(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.EDIBLE));
	}
	
	public static boolean isEdible(ItemStack stack)
	{
		return isEdible(stack == null ? null : stack.getType());
	}
	
	public static boolean isPotable(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.POTABLE));
	}
	
	public static boolean isPotable(ItemStack stack)
	{
		return isPotable(stack == null ? null : stack.getType());
	}
	
	public static boolean isAxe(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.AXE));
	}
	
	public static boolean isAxe(ItemStack stack)
	{
		return isAxe(stack == null ? null : stack.getType());
	}
	
	public static boolean isWeapon(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.WEAPON));
	}
	
	public static boolean isWeapon(ItemStack stack)
	{
		return isWeapon(stack == null ? null : stack.getType());
	}
	
	public static boolean isHelmet(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.ARMOR_HELMET));
	}
	
	public static boolean isHelmet(ItemStack stack)
	{
		return isHelmet(stack == null ? null : stack.getType());
	}
	
	public static boolean isChestplate(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.ARMOR_CHESTPLATE));
	}
	
	public static boolean isChestplate(ItemStack stack)
	{
		return isChestplate(stack == null ? null : stack.getType());
	}
	
	public static boolean isLeggings(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.ARMOR_LEGGINGS));
	}
	
	public static boolean isLeggings(ItemStack stack)
	{
		return isLeggings(stack == null ? null : stack.getType());
	}
	
	public static boolean isBoots(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.ARMOR_BOOTS));
	}
	
	public static boolean isBoots(ItemStack stack)
	{
		return isBoots(stack == null ? null : stack.getType());
	}
	
	public static boolean isBlock(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.BLOCK));
	}
	
	public static boolean isBlock(ItemStack stack)
	{
		return isBlock(stack == null ? null : stack.getType());
	}
	
	public static boolean isLiquid(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.LIQUID));
	}
	
	public static boolean isLiquid(ItemStack stack)
	{
		return isLiquid(stack == null ? null : stack.getType());
	}
	
	public static boolean isBoundless(Material material)
	{
		return material == null ? false : contains(material, ItemCategory.BOUNDLESS);
	}
	
	public static boolean isBoundless(ItemStack stack)
	{
		return isBoundless(stack == null ? null : stack.getType());
	}
	
	public static boolean isTranslucent(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.TRANSLUCENT));
	}
	
	public static boolean isTranslucent(ItemStack stack)
	{
		return isTranslucent(stack == null ? null : stack.getType());
	}
	
	public static boolean isOre(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.ORE));
	}
	
	public static boolean isOre(ItemStack stack)
	{
		return isOre(stack == null ? null : stack.getType());
	}
	
	public static boolean isCompactBlock(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.COMPACT_BLOCK));
	}
	
	public static boolean isCompactBlock(ItemStack stack)
	{
		return isCompactBlock(stack == null ? null : stack.getType());
	}
	
	public static boolean isGlassProduct(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.GLASS));
	}
	
	public static boolean isGlassProduct(ItemStack stack)
	{
		return isGlassProduct(stack == null ? null : stack.getType());
	}
	
	public static boolean doesModifyMovement(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.MOVEMENT_MODIFYING));
	}
	
	public static boolean doesModifyMovement(ItemStack stack)
	{
		return doesModifyMovement(stack == null ? null : stack.getType());
	}
	
	public static boolean doesEmitLight(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.LIGHT_EMITTING));
	}
	
	public static boolean doesEmitLight(ItemStack stack)
	{
		return doesEmitLight(stack == null ? null : stack.getType());
	}
	
	public static boolean isRedstoneComponent(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.REDSTONE));
	}
	
	public static boolean isRedstoneComponent(ItemStack stack)
	{
		return isRedstoneComponent(stack == null ? null : stack.getType());
	}
	
	public static boolean doesHaveGUI(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.GUI));
	}
	
	public static boolean doesHaveGUI(ItemStack stack)
	{
		return doesHaveGUI(stack == null ? null : stack.getType());
	}
	
	public static boolean isClimbable(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.CLIMBABLE));
	}
	
	public static boolean isClimbable(ItemStack stack)
	{
		return isClimbable(stack == null ? null : stack.getType());
	}
	
	public static boolean isLeatherProduct(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.LEATHER));
	}
	
	public static boolean isLeatherProduct(ItemStack stack)
	{
		return isLeatherProduct(stack == null ? null : stack.getType());
	}
	
	public static boolean isGoldProduct(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.GOLD));
	}
	
	public static boolean isGoldProduct(ItemStack stack)
	{
		return isGoldProduct(stack == null ? null : stack.getType());
	}
	
	public static boolean isIronProduct(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.IRON));
	}
	
	public static boolean isIronProduct(ItemStack stack)
	{
		return isIronProduct(stack == null ? null : stack.getType());
	}
	
	public static boolean isDiamondProduct(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.DIAMOND));
	}
	
	public static boolean isDiamondProduct(ItemStack stack)
	{
		return isDiamondProduct(stack == null ? null : stack.getType());
	}
	
	public static boolean isStoneProduct(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.STONE));
	}
	
	public static boolean isStoneProduct(ItemStack stack)
	{
		return isStoneProduct(stack == null ? null : stack.getType());
	}
	
	public static boolean isWoodProduct(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.WOOD));
	}
	
	public static boolean isWoodProduct(ItemStack stack)
	{
		return isWoodProduct(stack == null ? null : stack.getType());
	}
	
	public static boolean isChainmailProduct(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.CHAINMAIL));
	}
	
	public static boolean isChainmailProduct(ItemStack stack)
	{
		return isChainmailProduct(stack == null ? null : stack.getType());
	}
	
	public static boolean isThrowable(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.THROWABLE));
	}
	
	public static boolean isThrowable(ItemStack stack)
	{
		return isThrowable(stack == null ? null : stack.getType());
	}
	
	public static boolean isVehicle(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.VEHICLE));
	}
	
	public static boolean isVehicle(ItemStack stack)
	{
		return isVehicle(stack == null ? null : stack.getType());
	}
	
	public static boolean isItem(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.ITEM));
	}
	
	public static boolean isItem(ItemStack stack)
	{
		return isItem(stack == null ? null : stack.getType());
	}
	
	public static boolean isLog(Material material)
	{
		return material == null ? false : (_materials.get(material).contains(ItemCategory.LOG));
	}
	
	public static boolean isLog(ItemStack stack)
	{
		return isLog(stack == null ? null : stack.getType());
	}
	
	public static boolean isLeaf(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.LEAVES));
	}
	
	public static boolean isLeaf(ItemStack stack)
	{
		return isLeaf(stack == null ? null : stack.getType());
	}
	
	public static boolean isDoor(Material type)
	{
		return type == null ? false : (contains(type, ItemCategory.DOOR));
	}
	
	public static boolean isDoor(ItemStack stack)
	{
		return isDoor(stack == null ? null : stack.getType());
	}
	
	public static boolean isTool(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.TOOL));
	}
	
	public static boolean isTool(ItemStack stack)
	{
		return isTool(stack == null ? null : stack.getType());
	}
	
	public static boolean isAffectedByPhysics(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.PHYSICS));
	}
	
	public static boolean isAffectedByPhysics(ItemStack stack)
	{
		return isAffectedByPhysics(stack == null ? null : stack.getType());
	}
	
	public static boolean isFromNether(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.NETHER));
	}
	
	public static boolean isFromNether(ItemStack stack)
	{
		return isFromNether(stack == null ? null : stack.getType());
	}
	
	public static boolean isFoodRaw(Material material)
	{
		return material == null ? false : (_materials.get(material).contains(ItemCategory.RAW_FOOD));
	}
	
	public static boolean isFoodRaw(ItemStack stack)
	{
		return isFoodRaw(stack == null ? null : stack.getType());
	}
	
	public static boolean isMusicDisc(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.MUSIC_DISC));
	}
	
	public static boolean isMusicDisc(ItemStack stack)
	{
		return isMusicDisc(stack == null ? null : stack.getType());
	}
	
	public static boolean isSpade(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.SHOVEL));
	}
	
	public static boolean isSpade(ItemStack stack)
	{
		return isSpade(stack == null ? null : stack.getType());
	}
	
	public static boolean isPickaxe(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.PICKAXE));
	}
	
	public static boolean isPickaxe(ItemStack stack)
	{
		return isPickaxe(stack == null ? null : stack.getType());
	}
	
	public static boolean isHoe(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.HOE));
	}
	
	public static boolean isHoe(ItemStack stack)
	{
		return isHoe(stack == null ? null : stack.getType());
	}
	
	public static boolean isItemBlock(Material material)
	{
		return material == null ? false : (_materials.get(material).contains(ItemCategory.ITEM_BLOCK));
	}
	
	public static boolean isItemBlock(ItemStack stack)
	{
		return isItemBlock(stack == null ? null : stack.getType());
	}
	
	public static boolean isArmor(Material material)
	{
		return material == null ? false : (contains(material, ItemCategory.ARMOR));
	}
	
	public static boolean isArmor(ItemStack stack)
	{
		return isArmor(stack == null ? null : stack.getType());
	}

	public static boolean is(ItemStack stack, ItemCategory category)
	{
		return stack == null ? false : contains(stack.getType(), category);
	}

	public static boolean is(Block block, ItemCategory category)
	{
		return block == null ? false : contains(block.getType(), category);
	}

	private static boolean contains(Material material, ItemCategory category)
	{
		EnumSet<ItemCategory> set = _materials.get(material);
		return set == null ? false : set.contains(category);
	}
	
	public static List<Material> listIn(ItemCategory... attr)
	{
		List<ItemCategory> attributes = new ArrayList<>(attr.length);
		Collections.addAll(attributes, attr);
		
		List<Material> list = new ArrayList<>();
		for (Entry<Material, EnumSet<ItemCategory>> mat : _materials.entrySet())
		{
			if (mat.getValue().containsAll(attributes))
			{
				list.add(mat.getKey());
			}
		}
		
		attributes.clear();
		attributes = null;
		
		return list;
	}
	
	public enum ItemCategory
	{
		EDIBLE,
		POTABLE,
		ORE,
		GLASS,
		COMPACT_BLOCK,
		GUI,
		LIGHT_EMITTING,
		REDSTONE,
		TRANSLUCENT,
		BOUNDLESS,
		LIQUID,
		MOVEMENT_MODIFYING,
		CLIMBABLE,
		THROWABLE,
		PHYSICS,
		NETHER,
		RAW_FOOD,
		MUSIC_DISC,
		VEHICLE,
		TOOL,
		GOLD,
		IRON,
		DIAMOND,
		STONE,
		WOOD,
		LEATHER,
		CHAINMAIL,
		ARMOR,
		ARMOR_HELMET,
		ARMOR_CHESTPLATE,
		ARMOR_LEGGINGS,
		ARMOR_BOOTS,
		WEAPON,
		SWORD,
		AXE,
		SHOVEL,
		PICKAXE,
		HOE,
		ITEM,
		BLOCK,
		ITEM_BLOCK,
		LOG,
		LEAVES,
		DOOR
	}
	
	public enum ArmorMaterial
	{
		DIAMOND,
		IRON,
		GOLD,
		LEATHER,
		CHAINMAIL;
		
		public static ArmorMaterial of(Material armor)
		{
			if (!isArmor(armor))
			{
				return null;
			}
			
			if (isDiamondProduct(armor))
			{
				return DIAMOND;
			}
			
			if (isIronProduct(armor))
			{
				return IRON;
			}
			
			if (isGoldProduct(armor))
			{
				return GOLD;
			}
			
			if (isLeatherProduct(armor))
			{
				return LEATHER;
			}
			
			if (isChainmailProduct(armor))
			{
				return CHAINMAIL;
			}
			
			return null;
		}
		
		public ItemCategory asCategory()
		{
			switch (this)
			{
				case DIAMOND:
					return ItemCategory.DIAMOND;
				case IRON:
					return ItemCategory.IRON;
				case GOLD:
					return ItemCategory.GOLD;
				case LEATHER:
					return ItemCategory.LEATHER;
				case CHAINMAIL:
					return ItemCategory.CHAINMAIL;
				default:
					return null;
			}
		}
	}

	public static boolean isIndexed(Material material)
	{
		return _materials.containsKey(material);
	}

	public static ItemStack makeUnbreakable(ItemStack i)
	{
		ItemMeta im = i.getItemMeta();
		im.spigot().setUnbreakable(true);
		i.setItemMeta(im);
		return i;
	}
	
	public static boolean isUnbreakable(ItemStack i)
	{
		if (i == null)
		{
			return false;
		}
		
		return i.getItemMeta().spigot().isUnbreakable();
	}
	
	
	/**
	 * @param item The item stack to use as source for this Item entity
	 * @param loc Location of where to spawn the Item entity
	 * @param dropNaturally If false then no velocity is applied. If true then it drops with random velocity like from when blocks break.
	 * @param allowPickup If false then it will disable pickup of this item.
	 * @param ticksToLive Ticks before this item should be removed from the ground. (default 6000 ticks = 5min, -1 to never remove it)
	 * @param allowMerge If false then the item will not merge with any other items.
	 * @return The entity spawned
	 */
	
	public static Item dropItem(ItemStack item, Location loc, boolean dropNaturally, boolean allowPickup, int ticksToLive, boolean allowMerge)
	{
		Item ent;

		if (dropNaturally)
		{
			ent = loc.getWorld().dropItemNaturally(loc, item);
		}
		else
		{
			ent = loc.getWorld().dropItem(loc, item);
		}

		if(!allowPickup)
		{
			ent.setPickupDelay(32767);
		}
		
		ent.setTicksLived(32768);
		
		UtilEnt.SetMetadata(ent, "UtilItemSpawning", true);
		
		if(ticksToLive != -1)
		{
			UtilServer.runSyncLater(ent::remove, ticksToLive);
		}
		
		if(!allowMerge)
		{
			net.minecraft.server.v1_8_R3.ItemStack stack = CraftItemStack.asNMSCopy(ent.getItemStack());
			NBTTagCompound tag = stack.getTag();
			if(!stack.hasTag())
			{
				stack.setTag(new NBTTagCompound());
				tag = stack.getTag();
			}
			tag.set("Pickup_" + UtilMath.r(Integer.MAX_VALUE), new NBTTagLong(UtilMath.random.nextLong()));
			ent.setItemStack(CraftItemStack.asBukkitCopy(stack));
		}
		return ent;
	}

	public static ItemStack getVersionSpecificItem(Player player, UtilPlayer.PlayerVersion requiredVersion, ItemStack base)
	{
		if (UtilPlayer.getPlayerVersion(player).compare(requiredVersion))
		{
			return base;
		}
		else
		{
			ItemStack barrier = new ItemStack(Material.BARRIER);
			ItemMeta meta = barrier.getItemMeta();
			meta.setDisplayName(base.getItemMeta().getDisplayName());
			List<String> lore = new ArrayList<>();
			lore.addAll(base.getItemMeta().getLore());
			lore.add(" ");
			lore.add(C.cRedB + "REQUIRES VERSION " + requiredVersion.getFriendlyName() + "+!");
			meta.setLore(lore);
			barrier.setItemMeta(meta);
			return barrier;
		}
	}

	public static double getAttackDamage(Material type)
	{
		return ItemDamage.get(type);
	}
	
	enum ItemDamage
	{
		IRON_SHOVEL(Material.IRON_SPADE, 3),
		IRON_PICKAXE(Material.IRON_PICKAXE, 4),
		IRON_AXE(Material.IRON_AXE, 5),
		WOODEN_SHOVEL(Material.WOOD_SPADE, 1),
		WOODEN_PICKAXE(Material.WOOD_PICKAXE, 2),
		WOODEN_AXE(Material.WOOD_AXE, 3),
		STONE_SHOVEL(Material.STONE_SPADE, 2),
		STONE_PICKAXE(Material.STONE_PICKAXE, 3),
		STONE_AXE(Material.STONE_AXE, 4),
		DIAMOND_SHOVEL(Material.DIAMOND_SPADE, 4),
		DIAMOND_PICKAXE(Material.DIAMOND_PICKAXE, 5),
		DIAMOND_AXE(Material.DIAMOND_AXE, 6),
		GOLD_SHOVEL(Material.GOLD_SPADE, 1),
		GOLD_PICKAXE(Material.GOLD_PICKAXE, 2),
		GOLD_AXE(Material.GOLD_AXE, 3),
		IRON_SWORD(Material.IRON_SWORD, 6),
		WOODEN_SWORD(Material.WOOD_SWORD, 4),
		STONE_SWORD(Material.STONE_SWORD, 5),
		DIAMOND_SWORD(Material.DIAMOND_SWORD, 7),
		GOLDEN_SWORD(Material.GOLD_SWORD, 4);
		
		private double _damage;
		private Material _type;
		
		ItemDamage(Material type, double damage)
		{
			_type = type;
			_damage = damage;
		}
		
		public static double get(Material type)
		{
			for (ItemDamage item : values())
			{
				if (item._type.equals(type))
				{
					return item._damage;
				}
			}
			
			return 1;
		}
		
	}

	public static boolean doesDisplayNameContain(ItemStack item, String name)
	{
		if (item == null)
			return false;
		if (item.getItemMeta() == null)
			return false;
		if (item.getItemMeta().getDisplayName() == null)
			return false;
		return item.getItemMeta().getDisplayName().contains(name);
	}

	public static String getDisplayName(ItemStack itemStack)
	{
		if (itemStack == null)
			return null;

		if (itemStack.getItemMeta() == null)
			return null;

		if (itemStack.getItemMeta().getDisplayName() == null)
			return null;

		return itemStack.getItemMeta().getDisplayName();
	}

	public static boolean isSimilar(ItemStack a, ItemStack b, ItemAttribute... attributes)
	{
		for (ItemAttribute attr : attributes)
		{
			if (!attr.isEqual(a, b))
			{
				return false;
			}
		}
		return true;
	}

	public enum ItemAttribute
	{
		MATERIAL
				{
					@Override
					public boolean isEqual(ItemStack a, ItemStack b)
					{
						return a == null ? b == null : b != null && a.getType() == b.getType();
					}
				},
		DATA
				{
					@Override
					public boolean isEqual(ItemStack a, ItemStack b)
					{
						return a == null ? b == null : b != null && a.getData().getData() == b.getData().getData();
					}
				},
		AMOUNT
				{
					@Override
					public boolean isEqual(ItemStack a, ItemStack b)
					{
						return a == null ? b == null : b != null && a.getAmount() == b.getAmount();
					}
				},
		NAME
				{
					@Override
					public boolean isEqual(ItemStack a, ItemStack b)
					{
						if (a == null)
						{
							return b == null;
						}
						else
						{
							if (b == null)
							{
								return false;
							}
						}
						ItemMeta ma = a.getItemMeta();
						ItemMeta mb = b.getItemMeta();
						if (ma == null)
						{
							return mb == null;
						}
						else
						{
							if (mb == null)
							{
								return false;
							}
						}

						return Objects.equals(ma.getDisplayName(), mb.getDisplayName());
					}
				},
		LORE
				{
					@Override
					public boolean isEqual(ItemStack a, ItemStack b)
					{
						return a == null ? b == null : b != null && Objects.equals(a.getItemMeta().getLore(), b.getItemMeta().getLore());
					}
				};

		public abstract boolean isEqual(ItemStack a, ItemStack b);
	}
}
