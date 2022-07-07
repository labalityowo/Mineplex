package mineplex.game.nano;

import mineplex.core.MiniPlugin;

public class GameManager extends MiniPlugin
{

	protected final NanoManager _manager;

	protected GameManager(String name)
	{
		super(name);

		_manager = require(NanoManager.class);
	}

}
