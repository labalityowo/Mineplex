package nautilus.game.arcade.game.games.minecraftleague.data;

import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.minecraftleague.MinecraftLeague;

import org.bukkit.Location;

public class TeamTower extends TeamTowerBase
{
	public Integer Number;
	
	public TeamTower(MinecraftLeague host, TowerManager manager, GameTeam team, Location spawn, Integer number)
	{
		super(host, manager, team, spawn);
		Number = number;
	}
}