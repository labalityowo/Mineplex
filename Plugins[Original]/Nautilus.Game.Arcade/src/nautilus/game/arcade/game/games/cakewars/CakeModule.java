package nautilus.game.arcade.game.games.cakewars;

import nautilus.game.arcade.game.modules.Module;

public class CakeModule extends Module
{

	protected final CakeWars _game;

	public CakeModule(CakeWars game)
	{
		_game = game;
	}

	public void register()
	{
		register(_game);
	}

	@Override
	public CakeWars getGame()
	{
		return _game;
	}
}
