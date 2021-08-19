package nautilus.game.arcade.game.games.mineware.challenge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.UtilMath;
import mineplex.core.projectile.ProjectileUser;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;

/**
 * <p>
 * This class contains the base structure of a team challenge.
 * All challenges should trigger any functionality inside
 * {@link #createSpawns()}, {@link #createMap()}, {@link #onStart()} and {@link #onEnd()}.
 * </p>
 * 
 * Additionally, {@link #onTimerFinish()} and {@link #onCollide(LivingEntity, Block, ProjectileUser)} can be overrided.
 */
public abstract class TeamChallenge extends Challenge
{
	private static final int TEAM_EVEN_NUMBER = 2;
	private static final int TEAM_SEPERATOR = 2;

	private ChallengeTeam _firstTeam;
	private ChallengeTeam _secondTeam;
	private boolean _teamSpawn;
	private byte _firstTeamSpawnBlockData;
	private byte _secondTeamSpawnBlockData;

	public TeamChallenge(BawkBawkBattles host, ChallengeType type, String firstTeam, String secondTeam, boolean teamSpawn, byte firstTeamSpawnBlockData, byte secondTeamSpawnBlockData, String name, String... description)
	{
		super(host, type, name, description);

		_firstTeam = new ChallengeTeam(firstTeam);
		_secondTeam = new ChallengeTeam(secondTeam);
		_teamSpawn = teamSpawn;
		_firstTeamSpawnBlockData = firstTeamSpawnBlockData;
		_secondTeamSpawnBlockData = secondTeamSpawnBlockData;
	}

	public TeamChallenge(BawkBawkBattles host, ChallengeType type, String firstTeam, String secondTeam, String name, String... description)
	{
		this(host, type, firstTeam, secondTeam, false, (byte) 0, (byte) 0, name, description);
	}

	/*
	 * Called automatically only when _teamSpawn is true.
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void spawn(boolean firstRun)
	{
		if (_teamSpawn)
		{
			autoSelectTeams();

			List<Player> firstTeam = _firstTeam.getPlayers();
			List<Player> secondTeam = _secondTeam.getPlayers();

			int firstTeamIndex = 0;
			int secondTeamIndex = 0;

			int players = Host.getPlayersWithRemainingLives();
			int teleported = 0;

			List<Location> randomSpawns = Data.getDefinedSpawns();
			Collections.shuffle(randomSpawns);

			while (teleported < players)
			{
				for (Location spawn : randomSpawns)
				{
					Block block = spawn.getBlock().getRelative(BlockFace.DOWN);

					if (block.getData() == _firstTeamSpawnBlockData && firstTeamIndex < firstTeam.size())
					{
						Player player = firstTeam.get(firstTeamIndex);

						if (firstRun)
						{
							teleportWithDelay(player, spawn, teleported);
						}
						else
						{
							player.teleport(spawn);
						}

						firstTeamIndex++;
						teleported++;
					}
					else if (block.getData() == _secondTeamSpawnBlockData && secondTeamIndex < secondTeam.size())
					{
						Player player = secondTeam.get(secondTeamIndex);

						if (firstRun)
						{
							teleportWithDelay(player, spawn, teleported);
						}
						else
						{
							player.teleport(spawn);
						}

						secondTeamIndex++;
						teleported++;
					}
				}
			}
		}
		else
		{
			super.spawn(firstRun);
		}
	}

	private void teleportWithDelay(Player player, final Location spawn, int teleported)
	{
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				player.teleport(spawn);
			}
		}.runTaskLater(Host.Manager.getPlugin(), teleported);
	}

	/*
	 * Called automatically only when _teamSpawn is true.
	 */
	protected void autoSelectTeams()
	{
		ArrayList<Player> players = Host.GetPlayers(true);
		Collections.shuffle(players);
		int size = 0;

		if (players.size() % TEAM_EVEN_NUMBER == 0)
		{
			for (Player player : players)
			{
				if (size < players.size() / TEAM_SEPERATOR)
				{
					_firstTeam.add(player);
				}
				else
				{
					_secondTeam.add(player);
				}

				size++;
			}
		}
		else
		{
			for (int i = 0; i < players.size(); i++)
			{
				Player player = players.get(i);

				if (i == players.size() - 1)
				{
					if (UtilMath.random.nextBoolean())
					{
						_firstTeam.add(player);
					}
					else
					{
						_secondTeam.add(player);
					}
				}
				else
				{
					if (size < (int) players.size() / TEAM_SEPERATOR)
					{
						_firstTeam.add(player);
					}
					else
					{
						_secondTeam.add(player);
					}
				}

				size++;
			}
		}
	}

	@Override
	public void end()
	{
		_firstTeam.reset();
		_secondTeam.reset();
		super.end();
	}

	@Override
	public boolean canFinish()
	{
		int firstTeamAlive = 0;

		for (Player firstTeamPlayer : _firstTeam.getPlayers())
		{
			if (isPlayerValid(firstTeamPlayer))
			{
				firstTeamAlive++;
			}
		}

		int secondTeamAlive = 0;

		for (Player secondTeamPlayer : _secondTeam.getPlayers())
		{
			if (isPlayerValid(secondTeamPlayer))
			{
				secondTeamAlive++;
			}
		}

		boolean firstTeamDominant = firstTeamAlive > 0 && secondTeamAlive == 0;
		boolean secondTeamDominant = secondTeamAlive > 0 && firstTeamAlive == 0;

		if (firstTeamDominant)
		{
			for (Player firstTeamPlayer : _firstTeam.getPlayers())
			{
				setCompleted(firstTeamPlayer);
			}

			return true;
		}
		else if (secondTeamDominant)
		{
			for (Player secondTeamPlayer : _secondTeam.getPlayers())
			{
				setCompleted(secondTeamPlayer);
			}

			return true;
		}

		return super.canFinish();
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		if (!isChallengeValid())
			return;

		Player player = event.getPlayer();

		ChallengeTeam team = getTeam(player);

		if (team != null)
		{
			team.remove(player);
		}
	}

	public ChallengeTeam getTeam(Player player)
	{
		if (_firstTeam.isMember(player))
		{
			return _firstTeam;
		}
		else
		{
			return _secondTeam;
		}
	}

	public boolean areOnSameTeam(Player player1, Player player2)
	{
		return (_firstTeam.isMember(player1) && _firstTeam.isMember(player2)) || (_secondTeam.isMember(player1) && _secondTeam.isMember(player2));
	}

	public ChallengeTeam getFirstTeam()
	{
		return _firstTeam;
	}

	public ChallengeTeam getSecondTeam()
	{
		return _secondTeam;
	}

	public boolean shouldTeamSpawn()
	{
		return _teamSpawn;
	}
}
