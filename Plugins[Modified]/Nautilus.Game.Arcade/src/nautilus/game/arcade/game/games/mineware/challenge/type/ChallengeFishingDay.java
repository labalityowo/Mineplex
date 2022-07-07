package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fish;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;

/**
 * A challenge based on fishing.
 * 
 * @deprecated
 */
public class ChallengeFishingDay extends Challenge
{
	private int _startingLureLevel = 8;
	private Map<Player, Boolean> _fishing = new HashMap<>();
	private Map<TNTPrimed, Player> _explosives = new HashMap<>();

	public ChallengeFishingDay(BawkBawkBattles host)
	{
		super(
			host,
			ChallengeType.FirstComplete,
			"Fishing Day",
			"Be the first to catch 5 fish.",
			"Watch out for TNT if you miss one!");

		Settings.setUseMapHeight();
		Settings.setLockInventory(0);
	}

	@Override
	public ArrayList<Location> createSpawns()
	{
		int size = getArenaSize();
		ArrayList<Location> spawns = new ArrayList<Location>();

		for (int x = -(size); x <= size; x++)
		{
			for (int z = -(size); z <= size; z++)
			{
				double absX = Math.abs(x);
				double absZ = Math.abs(z);
				int platform = size - 2;

				if ((absX == platform || absZ == platform) && !(absX > platform || absZ > platform))
				{
					spawns.add(getCenter().add(x, 6, z));
				}
			}
		}

		return spawns;
	}

	@Override
	public void createMap()
	{
		int size = getArenaSize();

		for (int x = -size; x <= size; x++)
		{
			for (int z = -size; z <= size; z++)
			{
				for (int y = 0; y < 8; y++)
				{
					Block block = getCenter().getBlock().getRelative(x, y, z);

					double absX = Math.abs(x);
					double absZ = Math.abs(z);

					// Bottom Layer

					if (y == 0)
					{
						setBlock(block, Material.BEDROCK);
					}

					// Ground Layer

					else if (y == 1)
					{
						setBlock(block, Material.SAND);
					}
					else
					{
						if (y < 6)
						{
							// Fishing Platform

							if ((absX >= size - 3 && absX <= size) || (absZ >= size - 3 && absZ <= size))
							{
								double chance = Math.random() * 100;

								if (chance < 25)
								{
									setBlock(block, Material.GRAVEL);
								}
								else
								{
									if (y == 5)
									{
										setBlock(block, Material.GRASS);

										Block above = block.getRelative(BlockFace.UP);
										generateGrass(above);

										addBlock(above);
									}
									else
									{
										setBlock(block, Material.DIRT);
									}
								}
							}

							// Water Container

							else if (absX <= size - 4 || absZ <= size - 4)
							{
								setBlock(block, Material.WATER);
							}
						}

						// Border Walls

						else if (y > 4 && (absX == size || absZ == size))
						{
							setBlock(block, Material.FENCE);
						}
					}

					addBlock(block);
				}
			}
		}
	}

	@Override
	public void onStart()
	{
		Host.DamageEvP = false;
		Host.ItemPickup = true;
		Host.WorldWaterDamage = 1;

		ItemStack fishingRod = new ItemBuilder(Material.FISHING_ROD)
			.addEnchantment(Enchantment.LURE, _startingLureLevel)
			.setUnbreakable(true)
			.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
			.build();

		setItem(Settings.getLockedSlot(), fishingRod);
	}

	@Override
	public void onEnd()
	{
		Host.DamageEvP = false;
		Host.ItemPickup = false;
		Host.InventoryClick = false;
		Host.WorldWaterDamage = 0;

		remove(EntityType.PRIMED_TNT);
		_fishing.clear();
		_explosives.clear();
	}

	@EventHandler
	public void onPlayerFish(PlayerFishEvent event)
	{
		if (!isChallengeValid())
			return;

		Player fisher = event.getPlayer();

		if (!isPlayerValid(fisher))
			return;

		Fish hook = event.getHook();

		if (!_fishing.containsKey(fisher))
		{
			_fishing.put(fisher, false);
		}
		else
		{
			if (!_fishing.get(fisher))
			{
				_fishing.put(fisher, true);
			}
		}

		if (event.getCaught() != null)
		{
			Entity entity = event.getCaught();

			ItemStack item = new ItemStack(Material.RAW_FISH, 1, (byte) UtilMath.r(3));
			fisher.getInventory().addItem(item);

			UtilTextBottom.display("You caught a " + C.cGreen + getFishType(item.getData().getData()) + C.cWhite + "!",
				fisher);
			fisher.playSound(fisher.getLocation(), Sound.ORB_PICKUP, 1.0F, 1.0F);

			waterSplashEffect(fisher.getLocation().add(0, 0.5, 0), true, true);

			event.setExpToDrop(0);
			entity.remove();

			_fishing.put(fisher, false);

			checkForWinner(fisher);
		}
		else
		{
			if (isFishingHookEmpty(fisher, hook))
			{
				createExplosion(fisher, hook.getLocation());
			}
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
	{
		if (!isChallengeValid())
			return;

		if (event.getEntity() instanceof Player && event.getDamager() instanceof FishHook)
			event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event)
	{
		if (!isChallengeValid())
			return;

		if (!isPlayerValid(event.getPlayer()))
			return;

		if (Math.random() * 100 < 20)
		{
			Player player = event.getPlayer();
			Location from = event.getFrom();
			Location to = event.getTo();

			double fromX = from.getX();
			double fromZ = from.getZ();
			double toX = to.getX();
			double toZ = to.getZ();

			if (fromX != toX || fromZ != toZ)
			{
				waterSplashEffect(player.getLocation().add(0, 0.1, 0), false, false);
			}
		}
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event)
	{
		if (!isChallengeValid())
			return;

		Location dropsite = event.getLocation();
		World world = dropsite.getWorld();

		world.playSound(dropsite, Sound.ZOMBIE_WOODBREAK, 0.5F, 1.0F);
		world.playSound(dropsite, Sound.EXPLODE, 1.0F, 1.0F);
		UtilParticle.PlayParticle(ParticleType.CLOUD, dropsite, 0.6F, 0.6F, 0.6F, 0.0F, 50, ViewDist.NORMAL, UtilServer.getPlayers());
		UtilParticle.PlayParticle(ParticleType.SMOKE, dropsite, 0.3F, 0.3F, 0.3F, 0.5F, 50, ViewDist.NORMAL, UtilServer.getPlayers());

		Player target = _explosives.get(event.getEntity());

		if (UtilMath.offset(dropsite, target.getLocation()) <= 6.0)
		{
			setLost(target);
		}

		event.setCancelled(true);
	}

	private String getFishType(byte data)
	{
		if (data == 1)
		{
			return "Salmon";
		}
		else if (data == 2)
		{
			return "Clownfish";
		}
		else if (data == 3)
		{
			return "Pufferfish";
		}
		else
		{
			return "Fish";
		}
	}

	private void checkForWinner(Player fisher)
	{
		ArrayList<ItemStack> contents = UtilInv.getItems(fisher);
		int caughtFish = 0;

		for (ItemStack item : contents)
		{
			if (item.getType() == Material.RAW_FISH)
			{
				caughtFish = caughtFish + item.getAmount();
			}
		}

		if (caughtFish == 5)
		{
			setCompleted(fisher);
		}
	}

	private boolean isFishingHookEmpty(Player fisher, Fish hook)
	{
		// Check if the player is retracting the hook.
		// Once hook is retracted, the entity is valid but not on ground.

		Location droppedHook = hook.getLocation();
		Block below = droppedHook.getBlock().getRelative(BlockFace.DOWN);

		return _fishing.get(fisher) && hook.isValid() && !hook.isOnGround() && below.getType() == Material.STATIONARY_WATER;
	}

	private void createExplosion(Player target, Location dropsite)
	{
		if (Recharge.Instance.use(target, "TNT Spawn", 700, false, false))
		{
			World world = dropsite.getWorld();

			target.playSound(dropsite, Sound.ZOMBIE_WOODBREAK, 0.3F, 1.3F);

			TNTPrimed explosive = world.spawn(dropsite, TNTPrimed.class);
			explosive.setFuseTicks(40);
			explosive.setYield(0.0F);

			UtilAction.velocity(explosive, UtilAlg.getTrajectory(dropsite, target.getLocation()), 1.2D, false, 0.0D,
				0.3D, 0.6D, false);

			_explosives.put(explosive, target);
		}
	}

	private void waterSplashEffect(Location location, boolean randomAmount, boolean sound)
	{
		int amount = 5;

		if (randomAmount)
			amount += UtilMath.r(10);

		UtilParticle.PlayParticle(ParticleType.WATER_WAKE, location, 0.2F, 0.1F, 0.2F, 0.0F, amount, ViewDist.NORMAL, UtilServer.getPlayers());

		if (sound)
			location.getWorld().playSound(location, Sound.WATER, 0.3F, 1.0F);
	}
}
