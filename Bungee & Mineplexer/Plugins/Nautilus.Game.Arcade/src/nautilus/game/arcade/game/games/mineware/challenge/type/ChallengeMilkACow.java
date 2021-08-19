package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilMath;
import mineplex.core.disguise.disguises.DisguiseVillager;
import mineplex.core.hologram.Hologram;
import mineplex.core.hologram.Hologram.HologramTarget;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;
import nautilus.game.arcade.game.games.mineware.challenge.NumberTracker;

/**
 * A challenge based on cow milking.
 */
public class ChallengeMilkACow extends Challenge implements NumberTracker
{
	private static final int SCORE_GOAL = 5;
	private static final int LOCKED_INVENTORY_SLOT = 4;
	private static final int MAP_SPAWN_SHIFT = 1;
	private static final int SPAWN_COORDINATE_MULTIPLE = 2;
	private static final int MAP_HEIGHT = 1;

	private static final double COW_NAMES_CHANCE = 0.3;
	private static final int COW_SPAWN_AMOUNT = 7;
	private static final int COW_SPAWN_LOCATION_MULTIPLIER = 2;


	private static final float MILK_DELIVERY_SOUND_VOLUME = 2.0F;
	private static final float MILK_DELIVERY_SOUND_PITCH = 0.0F;

	private static final int SCORE_DISPLAY_HEIGHT_ADD = 2;
	private static final double MILKED_HOLOGRAM_HEIGHT_ADD = 2.3;

	private static final String[] HUMAN_NAMES = new String[] {
		"Tom",
		"Steve",
		"John",
		"Harry",
		"Andrew",
		"Daniel",
		"Josh",
		"Jim"
	};

	private static final String[] COW_NAMES = new String[] {
		"Moosly",
		"Mooington",
		"Mooley",
		"Moose",
		"Mooskee",
		"Chicken",
		"Mooffy",
		"Moozzle",
	};

	private Villager _farmer;
	private Map<Cow, ArrayList<String>> _milkedCows = new HashMap<>();
	private Map<Cow, Hologram> _milkedCowsHolograms = new HashMap<>();
	private Map<Player, Integer> _score = new HashMap<>();
	private boolean _aquaColor;

	public ChallengeMilkACow(BawkBawkBattles host)
	{
		super(
			host,
			ChallengeType.FirstComplete,
			"Milk a Cow",
			"Milk " + SCORE_GOAL + " different cows.",
			"Deliver the milk to the villager!");

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
				if (x % SPAWN_COORDINATE_MULTIPLE == 0 && z % SPAWN_COORDINATE_MULTIPLE == 0)
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

		spawnCowsAndFarmer();
	}

	@Override
	public void onStart()
	{
		for (Player player : getPlayersAlive())
		{
			DisguiseVillager disguise = new DisguiseVillager(player);
			disguise.setBaby();
			Host.getArcadeManager().GetDisguise().disguise(disguise);

			player.getInventory().setItem(Settings.getLockedSlot(), new ItemStack(Material.BUCKET));
			_score.put(player, 0);
		}
	}

	@Override
	public void onEnd()
	{
		for (Cow cow : _milkedCows.keySet())
		{
			Hologram hologram = _milkedCowsHolograms.get(cow);
			hologram.stop();
			cow.remove();
		}

		for (Player player : Host.GetPlayers(false))
		{
			if (Host.getArcadeManager().GetDisguise().getActiveDisguise(player) instanceof DisguiseVillager)
			{
				Host.Manager.GetDisguise().undisguise(player);
			}
		}

		_farmer.remove();
		_farmer = null;
		_milkedCows.clear();
		_milkedCowsHolograms.clear();
		_score.clear();
		_aquaColor = false;
	}

	@EventHandler
	public void onEntityInteract(PlayerInteractEntityEvent event)
	{
		if (!isChallengeValid())
			return;

		Player player = event.getPlayer();

		if (!isPlayerValid(player))
			return;

		if (!(event.getRightClicked() instanceof LivingEntity))
			return;

		ItemStack item = player.getItemInHand();

		if (item == null)
			return;

		LivingEntity entity = (LivingEntity) event.getRightClicked();

		if (item.getType() == Material.BUCKET && _milkedCows.containsKey(entity))
		{
			milkCow(player, entity);
			event.setCancelled(true);
		}
		else if (item.getType() == Material.MILK_BUCKET && entity.equals(_farmer))
		{
			deliverMilk(player);

			int score = incrementScore(player);
			displayScore(entity, player, score);
			checkCompletion(player, score);

			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerItemConsume(PlayerItemConsumeEvent event)
	{
		if (!isChallengeValid())
			return;

		Player player = event.getPlayer();

		if (!isPlayerValid(player))
			return;

		event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		if (!isChallengeValid())
			return;

		Player player = event.getPlayer();
		_score.remove(player);
	}

	private void spawnCowsAndFarmer()
	{
		Host.CreatureAllow = true;

		spawnFarmerJoe();

		String[] chosenNames = HUMAN_NAMES;

		if (Math.random() < COW_NAMES_CHANCE)
		{
			chosenNames = COW_NAMES;
		}

		for (int i = 0; i <= COW_SPAWN_AMOUNT; i++)
		{
			spawnCow(chosenNames, i);
		}

		Host.CreatureAllow = false;
	}

	private void spawnFarmerJoe()
	{
		_farmer = (Villager) getCenter().getWorld().spawnEntity(getCenter().add(0, 1, 0), EntityType.VILLAGER);
		_farmer.setProfession(Profession.FARMER);
		_farmer.setCustomName(C.cGreen + C.Bold + "Farmer Joe");
		_farmer.setCustomNameVisible(true);
	}

	private void spawnCow(String[] chosenNames, int index)
	{
		Location loc = getRandomLocation();

		Cow cow = (Cow) loc.getWorld().spawnEntity(loc, EntityType.COW);
		cow.setCustomName(C.cWhite + chosenNames[index]);
		cow.setCustomNameVisible(true);

		_milkedCows.put(cow, new ArrayList<>());

		Location milkedLocation = cow.getLocation().add(0, MILKED_HOLOGRAM_HEIGHT_ADD, 0).clone();
		_milkedCowsHolograms.put(cow, new Hologram(Host.Manager.getHologramManager(), milkedLocation, C.cRed + "Already Milked"));

		Hologram holo = _milkedCowsHolograms.get(cow);
		holo.setHologramTarget(HologramTarget.WHITELIST);
		holo.setFollowEntity(cow);
		holo.start();
	}

	private Location getRandomLocation()
	{
		return getCenter().add(
			UtilMath.r(((getArenaSize() * COW_SPAWN_LOCATION_MULTIPLIER) - 1) - (getArenaSize() - 1)),
			1,
			UtilMath.r((getArenaSize() * COW_SPAWN_LOCATION_MULTIPLIER) - 1) - (getArenaSize() - 1));
	}

	private void milkCow(Player player, LivingEntity entity)
	{
		ArrayList<String> usernames = _milkedCows.get(entity);

		if (!usernames.contains(player.getName()))
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

					player.setItemInHand(new ItemStack(Material.MILK_BUCKET));
				}
			}.runTaskLater(Host.Manager.getPlugin(), 1L);

			_milkedCows.get(entity).add(player.getName());
			displayMilkedIndicator(player, entity);
		}
	}

	private void displayMilkedIndicator(Player player, Entity entity)
	{
		Hologram indicator = _milkedCowsHolograms.get(entity);

		if (!indicator.containsPlayer(player))
		{
			indicator.addPlayer(player);
		}
	}

	private void deliverMilk(Player player)
	{
		player.setItemInHand(new ItemStack(Material.BUCKET));
		player.playSound(player.getLocation(), Sound.ORB_PICKUP, MILK_DELIVERY_SOUND_VOLUME, MILK_DELIVERY_SOUND_PITCH);
	}

	private int incrementScore(Player player)
	{
		int score = _score.get(player) + 1;
		_score.put(player, score);
		return score;
	}

	private void displayScore(LivingEntity entity, Player player, int score)
	{
		Location displayLoc = entity.getLocation().add(0, SCORE_DISPLAY_HEIGHT_ADD, 0).clone();
		displayCount(player, displayLoc, selectScoreColor(score));
	}

	private String selectScoreColor(int score)
	{
		if (_aquaColor)
		{
			_aquaColor = false;
			return C.cAquaB + score;
		}
		else
		{
			_aquaColor = true;
			return C.cWhiteB + score;
		}
	}

	private void checkCompletion(Player player, int score)
	{
		if (score >= SCORE_GOAL)
		{
			setCompleted(player);
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
