package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import mineplex.core.common.util.UtilMath;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;

/**
 * A challenge based on knocking players off a platform.
 */
public class ChallengeSmashOff extends Challenge
{
	private static final int PLATFORM_SIZE = 4;
	private static final double SPAWN_CENTER = 0.5;
	private static final int PLATFORM_COLOR_RANGE = 16;
	private static final int PLATFORM_ADD_Z = 2;
	private static final int PLATFORM_COLOR_LIMIT = 14;

	public ChallengeSmashOff(BawkBawkBattles host)
	{
		super(
			host,
			ChallengeType.LastStanding,
			"Smash Off",
			"Knock other players off their platform.");

		Settings.setUseMapHeight();
		Settings.setCanCruble();
	}

	@Override
	public ArrayList<Location> createSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();
		int amount = (int) Math.ceil(Math.sqrt(Host.getPlayersWithRemainingLives()));

		for (int pX = 0; pX < amount; pX++)
		{
			for (int pZ = 0; pZ < amount; pZ++)
			{
				spawns.add(getCenter().add((pX * PLATFORM_SIZE) + SPAWN_CENTER, 1, (pZ * PLATFORM_SIZE) + SPAWN_CENTER));
			}
		}

		return spawns;
	}

	@Override
	public void createMap()
	{
		int amount = (int) Math.ceil(Math.sqrt(Host.getPlayersWithRemainingLives()));
		int a = UtilMath.r(PLATFORM_COLOR_RANGE);

		for (int pX = 0; pX < amount; pX++)
		{
			for (int pZ = 0; pZ < amount; pZ++)
			{
				for (int x = pX * PLATFORM_SIZE; x < (pX * PLATFORM_SIZE) + PLATFORM_ADD_Z; x++)
				{
					for (int z = pZ * PLATFORM_SIZE; z < (pZ * PLATFORM_SIZE) + PLATFORM_ADD_Z; z++)
					{
						Block block = getCenter().getBlock().getRelative(x, 0, z);
						setBlock(block, Material.STAINED_CLAY, (byte) a);
						addBlock(block);
					}
				}

				if (++a >= PLATFORM_COLOR_LIMIT)
				{
					a = 0;
				}
			}
		}
	}

	@Override
	public void onStart()
	{
		Host.DamagePvP = true;
	}

	@Override
	public void onEnd()
	{
		Host.DamagePvP = false;
	}
}
