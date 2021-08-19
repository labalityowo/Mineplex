package nautilus.game.arcade.game.games.minecraftleague.data;

import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.minecraftleague.MinecraftLeague;

import org.bukkit.Location;

public class TeamCrystal extends TeamTowerBase
{
	public TeamCrystal(MinecraftLeague host, TowerManager manager, GameTeam team, Location spawn)
	{
		super(host, manager, team, spawn);
	}
}