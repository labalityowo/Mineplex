package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;

import mineplex.core.disguise.disguises.DisguiseVillager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.disguise.disguises.DisguiseCat;
import mineplex.core.disguise.disguises.DisguiseWolf;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeTeam;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;
import nautilus.game.arcade.game.games.mineware.challenge.TeamChallenge;

/**
 * A team based challenge with players disguised as dogs and cats.
 * 
 * @deprecated
 */
public class ChallengeDogsVersusCats extends TeamChallenge
{
	private static final int CHALLENGE_DURATION = 75000;
	private static final int LOCKED_INVENTORY_SLOT = 4;
	private static final int TEAM_SEPERATOR_MULTIPLE = 2;

	private static final int MAP_SPAWN_SHIFT = 1;
	private static final int MAP_HEIGHT = 1;
	private static final int SPAWN_COORDINATE_MULTIPLE = 2;

	private static final double PARTICLE_LOCATION_ADD_X = 0.5;
	private static final double PARTICLE_LOCATION_ADD_Y = 1;
	private static final double PARTICLE_LOCATION_ADD_Z = 0.5;
	private static final float PARTICLE_OFFSET = 0.5F;
	private static final int PARTICLE_COUNT = 5;

	private static final int SCORE_PER_PLAYER = 50;

	private int _catScore = 0;
	private int _dogScore = 0;

	public ChallengeDogsVersusCats(BawkBawkBattles host)
	{
		super(
			host,
			ChallengeType.FirstComplete,
			"Dogs",
			"Cats",
			"Dogs Vs Cats",
			"If you are a dog, bark.",
			"If you are a cat, meow.",
			"First team to the end number wins!");

		Settings.setUseMapHeight();
		Settings.setTeamBased();
		Settings.setDuration(CHALLENGE_DURATION);
		Settings.setLockInventory(LOCKED_INVENTORY_SLOT);
	}

	@Override
	public ArrayList<Location> createSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();
		int size = getArenaSize() - MAP_SPAWN_SHIFT;

		for (int x = -size; x <= size; x++)
		{
			for (int z = -size; z <= size; z++)
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
						else if (y == 1)
						{
							generateGrass(block);
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
		int looped = 0;

		for (Player player : getPlayersAlive())
		{
			player.setGameMode(GameMode.ADVENTURE);

			if (looped % TEAM_SEPERATOR_MULTIPLE == 0)
			{
				addToDogTeam(player);
			}
			else
			{
				addToCatTeam(player);
			}

			looped++;
		}
	}

	@Override
	public void onEnd()
	{
		_catScore = 0;
		_dogScore = 0;

		for (Player player : Host.GetPlayers(false))
		{
			if (Host.getArcadeManager().GetDisguise().getActiveDisguise(player) instanceof DisguiseCat ||
					Host.getArcadeManager().GetDisguise().getActiveDisguise(player) instanceof DisguiseWolf)
			{
				Host.Manager.GetDisguise().undisguise(player);
			}
		}
	}

	@EventHandler
	public void onScoreUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		if (!isChallengeValid())
			return;

		int maxDogScore = calculateScore(getFirstTeam());
		int maxCatScore = calculateScore(getSecondTeam());

		displayScore(maxDogScore, maxCatScore);
		checkScore(maxDogScore, maxCatScore);
	}

	@EventHandler
	public void onMiddleTextUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		if (!isChallengeValid())
			return;

		showTipsForDogs();
		showTipsForCats();
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if (!isChallengeValid())
			return;

		Player player = event.getPlayer();

		if (!isPlayerValid(player))
			return;

		if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)
		{
			ItemStack item = player.getItemInHand();

			if (item.getType() == Material.BONE || item.getType() == Material.STRING)
			{
				Location loc = player.getLocation();

				if (getFirstTeam().isMember(player))
				{
					incrementDogScore();
					player.playSound(loc, Sound.WOLF_BARK, 1.0F, 1.0F);

				}
				else
				{
					incrementCatScore();
					player.playSound(loc, Sound.CAT_MEOW, 1.0F, 1.0F);
				}

				UtilParticle.PlayParticle(
					ParticleType.NOTE,
					loc.add(PARTICLE_LOCATION_ADD_X, PARTICLE_LOCATION_ADD_Y, PARTICLE_LOCATION_ADD_Z),
					PARTICLE_OFFSET,
					PARTICLE_OFFSET,
					PARTICLE_OFFSET,
					0.0F,
					PARTICLE_COUNT,
					ViewDist.LONG,
					UtilServer.getPlayers());
			}
		}
	}

	private void addToDogTeam(Player player)
	{
		getFirstTeam().add(player);

		DisguiseWolf dog = new DisguiseWolf(player);
		Host.getArcadeManager().GetDisguise().disguise(dog);

		player.getInventory().setItem(Settings.getLockedSlot(), ItemStackFactory.Instance.CreateStack(Material.BONE, (byte) 0, 1, C.Reset + C.Bold + "Bark"));
		UtilPlayer.message(player, F.main("Game", "You are a " + C.cGreen + "Dog" + C.mBody + ", bark!"));
	}

	private void addToCatTeam(Player player)
	{
		getSecondTeam().add(player);

		DisguiseCat Dcat = new DisguiseCat(player);
		Host.getArcadeManager().GetDisguise().disguise(Dcat);

		player.getInventory().setItem(Settings.getLockedSlot(), ItemStackFactory.Instance.CreateStack(Material.STRING, (byte) 0, 1, C.Reset + C.Bold + "Meow"));
		UtilPlayer.message(player, F.main("Game", "You are a " + C.cGreen + "Cat" + C.mBody + ", meow!"));
	}

	private void displayScore(int maxDogScore, int maxCatScore)
	{
		for (Player player : getPlayersAlive())
		{
			UtilTextMiddle.display(null, C.cBlueB + "Dogs: " + C.cWhite + _dogScore + "/" + maxDogScore + C.cGray + "  -  " + C.cRedB + "Cats: " + C.cWhite + _catScore + "/" + maxCatScore, player);
		}
	}

	private void checkScore(int maxDogScore, int maxCatScore)
	{
		if (_dogScore >= maxDogScore)
		{
			for (Player player : getFirstTeam().getPlayers())
			{
				setCompleted(player);
			}
		}
		else if (_catScore >= maxCatScore)
		{
			for (Player player : getSecondTeam().getPlayers())
			{
				setCompleted(player);
			}
		}
	}

	private void showTipsForDogs()
	{
		for (Player player : getFirstTeam().getPlayers())
		{
			if (isPlayerValid(player))
			{
				UtilTextBottom.display(C.Bold + "Left-Click to Woof!", player);
			}
		}
	}

	private void showTipsForCats()
	{
		for (Player player : getSecondTeam().getPlayers())
		{
			if (isPlayerValid(player))
			{
				UtilTextBottom.display(C.Bold + "Left-Click to Meow!", player);
			}
		}
	}

	private void incrementDogScore()
	{
		int maxScore = calculateScore(getFirstTeam());

		if (_dogScore < maxScore)
		{
			_dogScore++;
		}
	}

	private void incrementCatScore()
	{
		int maxScore = calculateScore(getSecondTeam());

		if (_catScore < maxScore)
		{
			_catScore++;
		}
	}

	@SuppressWarnings("unused")
	private int calculateScore(ChallengeTeam team)
	{
		int amountOnTeam = 0;

		for (Player player : team.getPlayers())
		{
			amountOnTeam++;
		}

		return amountOnTeam * SCORE_PER_PLAYER;
	}
}
