package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;
import nautilus.game.arcade.game.games.mineware.challenge.NumberTracker;

/**
 * A challenge based around chickens.
 */
public class ChallengeChickenShooting extends Challenge implements NumberTracker
{
	private static final int LOCKED_INVENTORY_SLOT = 4;
	private static final int MAP_SPAWN_SHIFT = 1;
	private static final int MAP_HEIGHT = 1;

	private static final int SPAWN_COORDINATES_MULTIPLE = 2;
	private static final int INVENTORY_ARROW_SLOT = 31;
	private static final int PLAYER_SIZE_LARGE = 40;
	private static final int PLAYER_SIZE_MID = 20;

	private static final int CHICKEN_SPAWN_SHIFT = 2;
	private static final int CHICKEN_SPAWN_HEIGHT = 15;
	private static final int CHICKEN_AMOUNT_MIN = 10;
	private static final int CHICKEN_AMOUNT_MULTIPLY = 3;
	private static final double CHICKEN_HEATH = 0.1;
	private static final double CHICKEN_BABY_SPAWN_CHANCE = 0.3;

	private static final int SCORE_GOAL = 6;
	private static final int SCORE_COLOR_CHANGE_1 = 1;
	private static final int SCORE_COLOR_CHANGE_2 = 2;
	private static final int SCORE_COLOR_CHANGE_3 = 3;
	private static final int SCORE_COLOR_CHANGE_4 = 4;
	private static final int SCORE_COLOR_CHANGE_5 = 5;

	private List<Chicken> _chickens = new ArrayList<>();
	private List<Location> _chickenSpawns = new ArrayList<>();
	private Map<Player, Integer> _score = new HashMap<>();

	public ChallengeChickenShooting(BawkBawkBattles host)
	{
		super(
			host,
			ChallengeType.FirstComplete,
			"Chicken Shooting",
			"Shoot 6 chickens.");

		Settings.setUseMapHeight();
		Settings.setLockInventory(LOCKED_INVENTORY_SLOT);
	}

	@Override
	public ArrayList<Location> createSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();
		int size = getArenaSize() - MAP_SPAWN_SHIFT;

		for (int x = -(size); x <= size; x++)
		{
			for (int z = -(size); z <= size; z++)
			{
				if (x % SPAWN_COORDINATES_MULTIPLE == 0 && z % SPAWN_COORDINATES_MULTIPLE == 0)
				{
					spawns.add(getCenter().add(x, MAP_HEIGHT, z));
				}
			}
		}

		return spawns;
	}

	@Override
	public void createMap()
	{
		for (int x = -getArenaSize(); x <= getArenaSize(); x++)
		{
			for (int z = -getArenaSize(); z <= getArenaSize(); z++)
			{
				for (int y = 0; y <= MAP_HEIGHT; y++)
				{
					Block block = getCenter().getBlock().getRelative(x, y, z);

					if (y == 0)
					{
						setBlock(block, Material.GRASS);
					}
					else
					{
						if (Math.abs(x) == getArenaSize() || Math.abs(z) == getArenaSize())
						{
							setBlock(block, Material.FENCE);
						}
						else
						{
							generateGrass(block);
						}
					}

					addBlock(block);
				}
			}
		}

		initializeChickenSpawns();
	}

	@Override
	public void onStart()
	{
		Host.DamagePvE = true;
		addBowAndArrow();
	}

	@Override
	public void onEnd()
	{
		Host.DamagePvE = false;

		for (Chicken chicken : _chickens)
		{
			chicken.remove();
		}

		_chickens.clear();
		_chickenSpawns.clear();
		_score.clear();

		remove(EntityType.ARROW);
	}

	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event)
	{
		if (!isChallengeValid())
			return;

		Projectile entity = event.getEntity();

		if (entity instanceof Arrow)
		{
			entity.remove();
		}
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event)
	{
		if (!isChallengeValid())
			return;

		event.getDrops().clear();
		event.setDroppedExp(0);
	}

	@EventHandler
	public void onCustomDamage(CustomDamageEvent event)
	{
		if (!isChallengeValid())
			return;

		if (event.GetProjectile() == null)
		{
			event.SetCancelled("Not projectile damage");
			return;
		}

		Player player = event.GetDamagerPlayer(true);

		if (player == null)
			return;

		if (!isPlayerValid(player))
			return;

		LivingEntity entity = event.GetDamageeEntity();

		if (!_chickens.remove(entity))
		{
			event.SetCancelled("Not a chicken");
			return;
		}

		event.AddMod("Ensure Death", null, entity.getHealth(), false);
		checkCompleted(player, entity);
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (!isChallengeValid())
			return;

		UpdateType speed;
		int alive = getPlayersIn(true).size();

		if (alive >= PLAYER_SIZE_LARGE)
		{
			speed = UpdateType.FASTEST;
		}
		else if (alive >= PLAYER_SIZE_MID)
		{
			speed = UpdateType.FASTER;
		}
		else
		{
			speed = UpdateType.FAST;
		}

		if (event.getType() != speed)
			return;

		removeChickensOnGround();
		spawnChicken();
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		if (!isChallengeValid())
			return;

		_score.remove(event.getPlayer());
	}

	private void initializeChickenSpawns()
	{
		_chickenSpawns = (ArrayList<Location>) circle(getCenter(), getArenaSize() - CHICKEN_SPAWN_SHIFT, 1, false, false, CHICKEN_SPAWN_HEIGHT);
	}

	private void addBowAndArrow()
	{
		ItemStack bow = new ItemBuilder(Material.BOW)
			.setUnbreakable(true)
			.addEnchantment(Enchantment.ARROW_INFINITE, 1)
			.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)
			.build();

		ItemStack arrow = new ItemStack(Material.ARROW);

		for (Player player : getPlayersAlive())
		{
			PlayerInventory inv = player.getInventory();
			inv.setItem(Settings.getLockedSlot(), bow);
			inv.setItem(INVENTORY_ARROW_SLOT, arrow);

			_score.put(player, 0);
		}
	}

	private void checkCompleted(Player player, LivingEntity entity)
	{
		int score = showAndIncrementScore(player, entity);

		if (score == SCORE_GOAL)
		{
			setCompleted(player);
		}
	}

	private int showAndIncrementScore(Player player, LivingEntity entity)
	{
		int score = _score.get(player) + 1;

		Location location = player.getEyeLocation();
		location.add(UtilAlg.getTrajectory(location, entity.getEyeLocation()).multiply(Math.min(7, entity.getLocation().distance(location))));

		displayCount(player, location, selectScoreColor(score));

		_score.put(player, score);
		return score;
	}

	private String selectScoreColor(int score)
	{
		if (score == SCORE_COLOR_CHANGE_1)
		{
			return C.cWhiteB + score;
		}
		else if (score == SCORE_COLOR_CHANGE_2)
		{
			return C.cGrayB + score;
		}
		else if (score == SCORE_COLOR_CHANGE_3)
		{
			return C.cGreenB + score;
		}
		else if (score == SCORE_COLOR_CHANGE_4)
		{
			return C.cYellowB + score;
		}
		else if (score == SCORE_COLOR_CHANGE_5)
		{
			return C.cGoldB + score;
		}
		else
		{
			return C.cRedB + score;
		}
	}

	private void removeChickensOnGround()
	{
		Iterator<Chicken> iterator = _chickens.iterator();

		while (iterator.hasNext())
		{
			Chicken chicken = iterator.next();

			if (chicken.isOnGround() || !chicken.isValid())
			{
				chicken.remove();
				iterator.remove();
			}
		}
	}

	private void spawnChicken()
	{
		if (_chickens.size() < CHICKEN_AMOUNT_MIN + (getPlayersIn(true).size() * CHICKEN_AMOUNT_MULTIPLY))
		{
			Location spawn = UtilMath.randomElement(_chickenSpawns);

			Host.CreatureAllow = true;
			Chicken chicken = (Chicken) spawn.getWorld().spawnEntity(spawn, EntityType.CHICKEN);
			Host.CreatureAllow = false;

			chicken.setMaxHealth(CHICKEN_HEATH);
			chicken.setHealth(CHICKEN_HEATH);
			
			if (Math.random() < CHICKEN_BABY_SPAWN_CHANCE)
			{
				chicken.setBaby();
			}

			_chickens.add(chicken);
		}
	}

	@Override
	public Number getData(Player player)
	{
		return _score.get(player);
	}

	@Override
	public boolean hasData(Player player)
	{
		return _score.containsKey(player);
	}
}
