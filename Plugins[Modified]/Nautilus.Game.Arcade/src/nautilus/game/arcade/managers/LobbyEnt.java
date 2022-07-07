package nautilus.game.arcade.managers;

import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.kit.Kit;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class LobbyEnt 
{

	private final GameTeam _team;
	private final Entity _ent;
	private final Location _loc;

	public LobbyEnt(Entity ent, Location loc, GameTeam team)
	{
		_ent = ent;
		_loc = loc;
		_team = team;
	}

	public GameTeam GetTeam()
	{
		return _team;
	}
	
	public Entity GetEnt()
	{
		return _ent;
	}
	
	public Location GetLocation()
	{
		return _loc;
	}
}
