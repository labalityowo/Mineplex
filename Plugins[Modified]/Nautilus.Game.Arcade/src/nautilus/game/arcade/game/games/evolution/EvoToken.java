package nautilus.game.arcade.game.games.evolution;

import nautilus.game.arcade.kit.Kit;

import org.bukkit.entity.Player;

public class EvoToken implements Comparable<EvoToken>
{
	/**
	 * @author Mysticate
	 */
	
	public final Player Player;
		
	public final Kit SupplementKit;
	
	public int Level = 0;
	public int LifeKills = 0;
	
	public EvoToken(Player player, Kit kit)
	{
		Player = player;
		SupplementKit = kit;
	}

	@Override
	public int compareTo(EvoToken o)
	{
        if (Level == o.Level)
        {
            return 0;
        }
        if (Level > o.Level)
        {
            return -1;
        }
        return 1;
    }
}
