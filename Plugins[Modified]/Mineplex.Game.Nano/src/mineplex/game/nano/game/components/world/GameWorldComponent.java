package mineplex.game.nano.game.components.world;

import mineplex.game.nano.game.Game;
import mineplex.game.nano.game.GameComponent;

public class GameWorldComponent extends GameComponent<Game>
{

	// Entities
	private boolean _creatureAllow, _creatureAllowOverride;

	// Blocks
	private boolean _blockBreak, _blockPlace, _blockInteract;

	// World
	private boolean _allowWeather;
	private boolean _worldBoundaryKill = true;

	public GameWorldComponent(Game game)
	{
		super(game);

		game.getManager().getGameWorldManager().setHook(this);
	}

	@Override
	public void disable()
	{

	}

	public GameWorldComponent setCreatureAllow(boolean creatureAllow)
	{
		_creatureAllow = creatureAllow;
		return this;
	}

	public boolean isCreatureAllow()
	{
		return _creatureAllow;
	}

	public GameWorldComponent setCreatureAllowOverride(boolean creatureAllowOverride)
	{
		_creatureAllowOverride = creatureAllowOverride;
		return this;
	}

	public boolean isCreatureAllowOverride()
	{
		return _creatureAllowOverride;
	}

	public GameWorldComponent setBlockBreak(boolean blockBreak)
	{
		_blockBreak = blockBreak;
		return this;
	}

	public boolean isBlockBreak()
	{
		return _blockBreak;
	}

	public GameWorldComponent setBlockPlace(boolean blockPlace)
	{
		_blockPlace = blockPlace;
		return this;
	}

	public boolean isBlockPlace()
	{
		return _blockPlace;
	}

	public GameWorldComponent setAllowWeather(boolean allowWeather)
	{
		_allowWeather = allowWeather;
		return this;
	}

	public GameWorldComponent setBlockInteract(boolean blockInteract)
	{
		_blockInteract = blockInteract;
		return this;
	}

	public boolean isBlockInteract()
	{
		return _blockInteract;
	}

	public boolean isAllowWeather()
	{
		return _allowWeather;
	}

	public GameWorldComponent setWorldBoundaryKill(boolean worldBoundaryKill)
	{
		_worldBoundaryKill = worldBoundaryKill;
		return this;
	}

	public boolean isWorldBoundaryKill()
	{
		return _worldBoundaryKill;
	}
}
