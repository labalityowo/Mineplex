package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.recharge.Recharge;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;

/**
 * A challenge based on click speed.
 */
public class ChallengeFastFood extends Challenge
{
	private static final int MAP_SPAWN_SHIFT = 3;
	private static final int MAP_HEIGHT = 1;
	private static final int INVENTORY_HOTBAR_SIZE = 8;
	private static final int RAW_FISH_DATA_RANGE = 3;

	private static final int RANDOM_FOOD_AMOUNT = 5;
	private static final int FOOD_THROW_COOLDOWN = 100;
	private static final double FOOD_THROW_EYE_LOCATION_HEIGHT_SUBTRACT = 0.5;
	private static final double FOOD_THROW_CREATE_DIRT_CHANCE = 0.3;

	private static final int LONG_GRASS_DATA_RANGE = 2;
	private static final int RED_ROSE_DATA_RANGE = 8;

	private static final Material[] FOOD = {
		Material.APPLE,
		Material.BREAD,
		Material.GRILLED_PORK,
		Material.COOKED_BEEF,
		Material.RAW_FISH,
		Material.COOKED_FISH,
		Material.CAKE,
		Material.COOKIE,
		Material.MELON,
		Material.COOKED_CHICKEN,
		Material.CARROT_ITEM,
		Material.BAKED_POTATO,
		Material.PUMPKIN_PIE };

	private static final Material[] FLOWERS = { Material.LONG_GRASS, Material.YELLOW_FLOWER, Material.RED_ROSE };

	private int _itemSeperator = 0;

	public ChallengeFastFood(BawkBawkBattles host)
	{
		super(
			host,
			ChallengeType.FirstComplete,
			"Fast Food",
			"Your inventory is full of food.",
			"Punch to throw it on the ground.");

		Settings.setUseMapHeight();
	}

	@Override
	public ArrayList<Location> createSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();
		int size = getArenaSize() - MAP_SPAWN_SHIFT;

		for (Location location : circle(getCenter(), size, 1, true, false, 0))
		{
			spawns.add(location.add(0, MAP_HEIGHT, 0));
		}

		return spawns;
	}

	@Override
	public void createMap()
	{
		for (Location location : circle(getCenter(), getArenaSize(), 1, false, false, 0))
		{
			Block block = location.getBlock();
			setBlock(block, Material.GRASS);
			addBlock(block);
		}
	}

	@Override
	public void onStart()
	{
		itemParticleTask();

		for (Player player : getPlayersAlive())
		{
			for (int i = 0; i <= INVENTORY_HOTBAR_SIZE; i++)
			{
				player.getInventory().setItem(i, getRandomFood());
			}
		}
	}

	@Override
	public void onEnd()
	{
		_itemSeperator = 0;

		remove(EntityType.DROPPED_ITEM);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if (!isChallengeValid())
			return;

		Player player = event.getPlayer();

		if (!isPlayerValid(player))
			return;

		if (UtilEvent.isAction(event, ActionType.L))
		{
			if (event.getItem() != null)
			{
				ItemStack item = event.getItem();
				throwItemInGround(player, item);
			}
			else
			{
				changeItemSlot(player);
			}
		}
	}

	private void itemParticleTask()
	{
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if (!isChallengeValid())
				{
					cancel();
					return;
				}

				for (Entity entity : Host.WorldData.World.getEntities())
				{
					if (entity instanceof Item)
					{
						Item item = (Item) entity;

						if (!item.isValid() || item.isDead() || item.isOnGround() || item.getItemStack().getType() == Material.INK_SACK)
							continue;

						UtilParticle.PlayParticle(ParticleType.INSTANT_SPELL, item.getLocation(), 0, 0, 0, 0, 1, ViewDist.NORMAL, UtilServer.getPlayers());
					}
				}
			}
		}.runTaskTimer(Host.getArcadeManager().getPlugin(), 0L, 1L);
	}

	private ItemStack getRandomFood()
	{
		Material foodMaterial = UtilMath.randomElement(FOOD);
		byte data = 0;

		if (foodMaterial == Material.RAW_FISH)
		{
			data = (byte) (UtilMath.r(RAW_FISH_DATA_RANGE) + 1);
		}
		else if (foodMaterial == Material.COOKED_FISH)
		{
			data = (byte) UtilMath.r(1);
		}

		ItemStack itemStack = new ItemStack(foodMaterial, RANDOM_FOOD_AMOUNT, (byte) data);
		return itemStack;
	}

	private void changeItemSlot(Player player)
	{
		for (int i = 0; i <= INVENTORY_HOTBAR_SIZE; i++)
		{
			if (player.getInventory().getItem(i) != null)
			{
				ItemStack newItemSelection = player.getInventory().getItem(i);

				if (newItemSelection.getType() != Material.AIR)
				{
					player.getInventory().setHeldItemSlot(i);
					return;
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void throwItemInGround(Player player, ItemStack item)
	{
		if (!Recharge.Instance.use(player, "Food Throw", FOOD_THROW_COOLDOWN, false, false))
			return;

		player.getWorld().playSound(player.getLocation(), Sound.EAT, 0.5F, 1.1F);
		UtilInv.remove(player, item.getType(), item.getData().getData(), 1);

		_itemSeperator++;
		ItemStack toThrow = ItemStackFactory.Instance.CreateStack(item.getType(), item.getData().getData(), 1, Integer.toString(_itemSeperator));

		double randomMultiply = UtilMath.random.nextDouble();

		Item thrownItem = player.getWorld().dropItem(player.getEyeLocation().subtract(0, FOOD_THROW_EYE_LOCATION_HEIGHT_SUBTRACT, 0), toThrow);
		thrownItem.setVelocity(player.getLocation().getDirection().normalize().multiply(randomMultiply));

		growGrassTask(thrownItem);
		checkForWinner(player);
	}

	private void growGrassTask(final Item item)
	{
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if (!isChallengeValid() || !item.isValid() || item.isDead())
				{
					cancel();
					return;
				}

				if (item.isOnGround())
				{
					if (Math.random() < FOOD_THROW_CREATE_DIRT_CHANCE)
					{
						Location drop = item.getLocation();
						Block block = drop.getBlock();
						Block below = block.getRelative(BlockFace.DOWN);

						if (UtilMath.random.nextBoolean())
						{
							if (!below.isEmpty())
							{
								setBlock(below, Material.DIRT);

								if (UtilMath.random.nextBoolean())
								{
									setData(below, (byte) 1);
								}
							}
						}

						if (block.isEmpty() && !below.isEmpty())
						{
							Material flower = UtilMath.randomElement(FLOWERS);
							setBlock(block, flower);

							if (flower == Material.LONG_GRASS)
							{
								setData(block, (byte) (UtilMath.r(LONG_GRASS_DATA_RANGE) + 1));
							}
							else if (flower == Material.RED_ROSE)
							{
								setData(block, (byte) UtilMath.r(RED_ROSE_DATA_RANGE));
							}

							blockBreakEffect(block, false);
							addBlock(block);
							item.remove();
						}
					}

					cancel();
				}
			}
		}.runTaskTimer(Host.getArcadeManager().getPlugin(), 0L, 1L);
	}

	private void checkForWinner(Player player)
	{
		ArrayList<ItemStack> items = UtilInv.getItems(player);

		if (items.size() == 0)
			setCompleted(player);
	}
}
