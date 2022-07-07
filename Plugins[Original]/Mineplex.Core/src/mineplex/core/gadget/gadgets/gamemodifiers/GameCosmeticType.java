package mineplex.core.gadget.gadgets.gamemodifiers;

import mineplex.core.gadget.GadgetManager;
import mineplex.core.game.GameDisplay;

import java.util.ArrayList;
import java.util.List;

public abstract class GameCosmeticType
{

	private final GadgetManager _manager;
	private final String _name;
	private final GameDisplay _game;
	private final List<GameCosmeticCategory> _categories;

	public GameCosmeticType(GadgetManager manager, GameDisplay game)
	{
		_manager = manager;
		_name = game.getName();
		_game = game;
		_categories = new ArrayList<>();

		addCategories();

		manager.getGameCosmeticManager().addCosmeticType(this, _categories);
	}

	public abstract void addCategories();

	public void addCategory(GameCosmeticCategory category)
	{
		_categories.add(category);
	}

	public final GadgetManager getManager()
	{
		return _manager;
	}

	public String getName()
	{
		return _name;
	}

	public GameDisplay getGame()
	{
		return _game;
	}

	public List<GameCosmeticCategory> getCategories()
	{
		return _categories;
	}
}
