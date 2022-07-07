package mineplex.game.nano.game.components.player;

import mineplex.game.nano.game.Game;
import mineplex.game.nano.game.GameComponent;

public class GamePlayerComponent extends GameComponent<Game>
{

	private boolean _hunger;
	private boolean _regainHealth = true;
	private boolean _itemMovement;
	private boolean _itemDropPickup;
	private boolean _hideParticles;

	public GamePlayerComponent(Game game)
	{
		super(game);

		game.getManager().getGamePlayerManager().setHook(this);
	}

	@Override
	public void disable()
	{

	}

	public GamePlayerComponent setHunger(boolean hunger)
	{
		_hunger = hunger;
		return this;
	}

	public boolean isHunger()
	{
		return _hunger;
	}

	public GamePlayerComponent setRegainHealth(boolean regainHealth)
	{
		_regainHealth = regainHealth;
		return this;
	}

	public boolean isRegainHealth()
	{
		return _regainHealth;
	}

	public GamePlayerComponent setItemMovement(boolean itemMovement)
	{
		_itemMovement = itemMovement;
		return this;
	}

	public boolean isItemMovement()
	{
		return _itemMovement;
	}

	public GamePlayerComponent setItemDropPickup(boolean itemDropPickup)
	{
		_itemDropPickup = itemDropPickup;
		return this;
	}

	public boolean isItemDropPickup()
	{
		return _itemDropPickup;
	}

	public GamePlayerComponent setHideParticles(boolean hideParticles)
	{
		_hideParticles = hideParticles;
		return this;
	}

	public boolean isHideParticles()
	{
		return _hideParticles;
	}
}
