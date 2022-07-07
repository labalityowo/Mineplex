package mineplex.game.nano.game.components.stats;

import org.bukkit.entity.Player;

public interface StatsComponent
{

	void addStat(Player player, String stat, int amount, boolean limitTo1, boolean global);

}
