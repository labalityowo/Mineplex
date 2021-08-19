package nautilus.game.arcade;

import mineplex.core.common.MinecraftVersion;
import mineplex.core.common.Pair;
import nautilus.game.arcade.game.Game;

public class GameMode
{

	private final Class<? extends Game> _gameMode;
	private final String _name;
	private final Pair<MinecraftVersion, String>[] _resourcePackUrls;
	private final boolean _enforceResourcePack;

	public GameMode(Class<? extends Game> gameMode, String name)
	{
		this(gameMode, name, null, false);
	}

	public GameMode(Class<? extends Game> gameMode, String name, Pair<MinecraftVersion, String>[] resourcePackUrls, boolean enforceResourcePack)
	{
		_gameMode = gameMode;
		_name = name;
		_resourcePackUrls = resourcePackUrls;
		_enforceResourcePack = enforceResourcePack;
	}

	public Class<? extends Game> getGameClass()
	{
		return _gameMode;
	}

	public String getName()
	{
		return _name;
	}

	public Pair<MinecraftVersion, String>[] getResPackURLs()
	{
		return _resourcePackUrls;
	}

	public boolean enforceResourcePack()
	{
		return _enforceResourcePack;
	}

}
