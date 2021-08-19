package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.itemstack.ItemBuilder;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;

/**
 * A challenge based on arrows and explosions.
 */
public class ChallengeArrowRampage extends Challenge
{
	private static final int LOCKED_INVENTORY_SLOT = 4;
	private static final int MAP_MIN_SIZE = 9;
	private static final int MAP_SPAWN_SHIFT = 2;
	private static final int MAP_HEIGHT = 3;
	private static final int MAP_SPAWN_HEIGHT = MAP_HEIGHT + 1;

	private static final int HEIGHT_LVL0 = 0;
	private static final int HEIGHT_LVL1 = 1;
	private static final int HEIGHT_LVL2 = 2;
	private static final int HEIGHT_LVL3 = 3;
	private static final byte WOOL_DATA_LVL0 = 14;
	private static final byte WOOL_DATA_LVL1 = 4;
	private static final byte WOOL_DATA_LVL2 = 5;
	private static final int WOOL_DATA_RANGE_LVL3 = 16;

	private static final int JUMP_EFFECT_AMPLIFIER = 2;
	private static final int ARROW_EXPLOSION_RADIUS = 3;
	private static final int ARROW_HIT_BLOCK_MAX_DISTANCE = 4;

	public ChallengeArrowRampage(BawkBawkBattles host)
	{
		super(
			host,
			ChallengeType.LastStanding,
			"Arrow Rampage",
			"You are equipped with explosive arrows.",
			"Force others into the void!");

		Settings.setUseMapHeight();
		Settings.setCanCruble();
		Settings.setLockInventory(LOCKED_INVENTORY_SLOT);
	}

	@Override
	public ArrayList<Location> createSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();
		int size = getArenaSize(MAP_MIN_SIZE) - MAP_SPAWN_SHIFT;

		for (int x = -size; x < size; x++)
		{
			for (int z = -(size); z < size; z++)
			{
				if (x % 2 == 0 && z % 2 == 0)
				{
					spawns.add(getCenter().add(x, MAP_SPAWN_HEIGHT, z));
				}
			}
		}

		return spawns;
	}

	@Override
	public void createMap()
	{
		int size = getArenaSize(MAP_MIN_SIZE);

		for (int x = -size; x <= size; x++)
		{
			for (int z = -size; z <= size; z++)
			{
				for (int y = 0; y <= MAP_HEIGHT; y++)
				{
					Block block = getCenter().getBlock().getRelative(x, y, z);
					setBlock(block, Material.WOOL);

					if (y == HEIGHT_LVL0)
					{
						setData(block, WOOL_DATA_LVL0);
					}
					else if (y == HEIGHT_LVL1)
					{
						setData(block, (byte) WOOL_DATA_LVL1);
					}
					else if (y == HEIGHT_LVL2)
					{
						setData(block, (byte) WOOL_DATA_LVL2);
					}
					else if (y == HEIGHT_LVL3)
					{
						setData(block, (byte) UtilMath.r(WOOL_DATA_RANGE_LVL3));
					}

					addBlock(block);
				}
			}
		}
	}

	@Override
	public void onStart()
	{
		ItemStack bow = new ItemBuilder(Material.BOW)
			.setUnbreakable(true)
			.addEnchantment(Enchantment.ARROW_INFINITE, 1)
			.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
			.build();

		setItem(Settings.getLockedSlot(), bow);
		setItem(31, new ItemStack(Material.ARROW)); // Place arrow above bow, not visible in the hotbar.

		addEffect(PotionEffectType.JUMP, JUMP_EFFECT_AMPLIFIER);
	}

	@Override
	public void onEnd()
	{
		remove(EntityType.FALLING_BLOCK);
	}

	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event)
	{
		if (!isChallengeValid())
			return;

		if (!(event.getEntity() instanceof Arrow))
			return;

		Arrow arrow = (Arrow) event.getEntity();

		if (arrow.getShooter() == null || !(arrow.getShooter() instanceof Player))
			return;

		Set<Block> blocks = UtilBlock.getInRadius(arrow.getLocation(), ARROW_EXPLOSION_RADIUS).keySet();
		Iterator<Block> blockIterator = blocks.iterator();

		Block block = getHitBlock(arrow);

		if (!Data.getModifiedBlocks().contains(block))
			return;

		while (blockIterator.hasNext())
		{
			Block toDestroy = blockIterator.next();

			if (toDestroy.isLiquid())
			{
				blockIterator.remove();
			}
			else if (toDestroy.getRelative(BlockFace.UP).isLiquid())
			{
				blockIterator.remove();
			}
		}

		arrow.remove();
		Host.Manager.GetExplosion().BlockExplosion(blocks, arrow.getLocation(), false);
	}

	public Block getHitBlock(Arrow arrow)
	{
		Block hit = null;
		BlockIterator iterator = new BlockIterator(arrow.getLocation().getWorld(), arrow.getLocation().toVector(), arrow.getVelocity().normalize(), 0, ARROW_HIT_BLOCK_MAX_DISTANCE);

		while (iterator.hasNext())
		{
			hit = iterator.next();

			if (!hit.isEmpty())
			{
				break;
			}
		}

		return hit;
	}
}
