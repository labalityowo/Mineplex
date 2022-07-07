package mineplex.bungee;

import mineplex.bungee.lobbyBalancer.LobbyBalancer;
import mineplex.bungee.motd.MotdManager;
import mineplex.bungee.playerCount.PlayerCount;
import mineplex.bungee.playerStats.PlayerStats;
import mineplex.bungee.playerTracker.PlayerTracker;
import net.md_5.bungee.api.plugin.Plugin;

public class Mineplexer extends Plugin
{
	@Override
	public void onEnable() {
		new MotdManager(this);
		new LobbyBalancer(this);
		new PlayerCount(this);
		new FileUpdater(this);
		new PlayerStats(this);
		new PlayerTracker(this);
	}
}
