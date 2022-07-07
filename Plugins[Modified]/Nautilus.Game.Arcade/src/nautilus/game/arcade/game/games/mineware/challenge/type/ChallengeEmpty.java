package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;

/**
 * This challenge is used to prevent the game from crashing if it's forced to start with only one player.
 * 
 * @deprecated
 */
public class ChallengeEmpty extends Challenge
{
	public ChallengeEmpty(BawkBawkBattles host)
	{
		super(host, ChallengeType.FirstComplete, "Empty", "Not enough players");
		
		Settings.setUseMapHeight();
	}

	@Override
	public ArrayList<Location> createSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<>();
		spawns.add(getCenter().add(0, 1, 0));
		return spawns;
	}

	@Override
	public void createMap()
	{
		Block center = getCenter().getBlock();
		center.setType(Material.BARRIER);
		addBlock(center);
	}
}
