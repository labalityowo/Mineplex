package nautilus.game.arcade.game.modules.compass;

import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.kit.Kit;

import org.bukkit.entity.Entity;

public class CompassEntry
{
	private Entity _entity;
	private String _name;
	private String _displayName;
	private GameTeam _team;
	private Kit _kit;

	public CompassEntry(Entity entity, String name, String displayName, GameTeam team, Kit kit)
	{
		_entity = entity;
		_name = name;
		_displayName = displayName;
		_team = team;
		_kit = kit;
	}

	public Entity getEntity()
	{
		return _entity;
	}

	public GameTeam getTeam()
	{
		return _team;
	}

	public String getDisplayName()
	{
		return _displayName;
	}

	public String getName()
	{
		return _name;
	}

	public Kit getKit()
	{
		return _kit;
	}
}
