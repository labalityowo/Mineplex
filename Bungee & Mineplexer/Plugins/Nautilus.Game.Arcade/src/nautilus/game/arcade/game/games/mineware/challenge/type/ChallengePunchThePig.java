package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;

/**
 * A challenge based on killing pigs to increase the score.
 */
public class ChallengePunchThePig extends Challenge
{
	private int _goal = 5;
	private Set<Pig> _pigs = new HashSet<>();
	private Map<Player, Integer> _score = new HashMap<>();

	public ChallengePunchThePig(BawkBawkBattles host)
	{
		super(
			host,
			ChallengeType.FirstComplete,
			"Punch the Pig",
			"Punch 5 pigs.");

		Settings.setUseMapHeight();
	}

	@Override
	public ArrayList<Location> createSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();
		int size = getArenaSize() - 1;

		for (int x = -(size); x < size; x++)
		{
			for (int z = -(size); z < size; z++)
			{
				if (x % 2 == 0 && z % 2 == 0)
				{
					spawns.add(getCenter().add(x, 1, z));
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
				for (int y = 0; y <= 1; y++)
				{
					Block block = getCenter().getBlock().getRelative(x, y, z);

					if (y == 0)
					{
						if (UtilMath.random.nextDouble() * 100 < 20)
						{
							setBlock(block, Material.DIRT);

							if (UtilMath.random.nextBoolean())
							{
								setData(block, (byte) 1);
							}
						}
						else
						{
							setBlock(block, Material.GRASS);
						}
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
	}

	@Override
	public void onStart()
	{
		Host.DamagePvE = true;

		spawnPigs();
		initializeScores();

	}

	@Override
	public void onEnd()
	{
		Host.DamagePvE = false;

		for (Pig pigs : _pigs)
		{
			if (!pigs.isDead())
			{
				pigs.remove();
			}
		}

		_pigs.clear();
		_score.clear();
	}

	@EventHandler
	public void onCustomDamage(CustomDamageEvent event)
	{
		if (!isChallengeValid())
			return;

		if (event.GetDamageeEntity() == null)
			return;

		if (event.GetDamagerPlayer(false) == null)
			return;

		if (event.GetDamageeEntity() instanceof Pig)
		{
			Player player = event.GetDamagerPlayer(false);
			Pig pig = (Pig) event.GetDamageeEntity();

			if (Data.isCompleted(player) || !isPlayerValid(player))
			{
				event.SetCancelled("Player already completed");
				return;
			}

			if (_pigs.contains(pig))
			{
				killPig(player, pig);
			}
		}
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event)
	{
		if (!isChallengeValid())
			return;

		if (event.getEntity() instanceof Pig)
			event.getDrops().clear();
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		if (!isChallengeValid())
			return;

		_score.remove(event.getPlayer());
	}

	private void spawnPigs()
	{
		for (int i = 0; i <= Math.round(getPlayersAlive().size() * 5); i++)
		{
			Location spawn = getRandomPigSpawn();

			Host.CreatureAllowOverride = true;
			Pig pig = (Pig) spawn.getWorld().spawnEntity(spawn, EntityType.PIG);
			Host.CreatureAllowOverride = false;

			_pigs.add(pig);
		}
	}

	private void initializeScores()
	{
		for (Player player : getPlayersAlive())
		{
			_score.put(player, 0);
		}
	}

	private void killPig(Player player, Pig pig)
	{
		Location loc = pig.getLocation().add(0, 1, 0);
		_score.put(player, _score.get(player) + 1);
		int score = _score.get(player);

		if (score < _goal)
		{
			displayCount(player, loc, C.cWhiteB + score);
		}
		else
		{
			setCompleted(player, true);
			displayCount(player, loc, C.cGreenB + "Completed!");
		}

		UtilParticle.PlayParticle(ParticleType.CLOUD, loc.subtract(0, 0.5, 0), 0.3F, 0.3F, 0.3F, 0.0F, 20, ViewDist.NORMAL, UtilServer.getPlayers());

		pig.remove();
		_pigs.remove(pig);

		if (_pigs.isEmpty())
		{
			end();
		}
	}

	private Location getRandomPigSpawn()
	{
		return getCenter().add(UtilMath.r((getArenaSize() * 2) - 1) - (getArenaSize() - 1), 1, UtilMath.r((getArenaSize() * 2) - 1) - (getArenaSize() - 1));
	}
}
