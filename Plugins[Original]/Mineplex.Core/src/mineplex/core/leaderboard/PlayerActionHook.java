package mineplex.core.leaderboard;

import org.bukkit.entity.Player;

interface PlayerActionHook
{

	void onPlayerJoin(Player player);

	void onPlayerQuit(Player player);

}
