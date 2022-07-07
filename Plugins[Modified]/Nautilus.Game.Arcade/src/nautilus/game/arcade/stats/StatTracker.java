package nautilus.game.arcade.stats;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import nautilus.game.arcade.game.Game;

public class StatTracker<G extends Game> implements Listener
{
	private final G game;

	public StatTracker(G game)
	{
		this.game = game;
	}

	public G getGame()
	{
		return game;
	}

	public boolean canAddStats()
	{
		return getGame().CanAddStats;
	}

	public void addStat(Player player, String stat, int value, boolean limitTo1, boolean global)
	{
		if (canAddStats())
			getGame().AddStat(player, stat, value, limitTo1, global);
	}
}
