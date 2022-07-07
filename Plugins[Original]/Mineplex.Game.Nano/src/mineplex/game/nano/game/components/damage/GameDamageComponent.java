package mineplex.game.nano.game.components.damage;

import mineplex.game.nano.game.Game;
import mineplex.game.nano.game.GameComponent;

public class GameDamageComponent extends GameComponent<Game>
{

	private boolean _damage = true, _pvp = true, _fall = true, _self = true, _teamSelf = false;

	public GameDamageComponent(Game game)
	{
		super(game);

		game.getManager().getGameDamageManager().setHook(this);
	}

	@Override
	public void disable()
	{

	}

	public GameDamageComponent setDamage(boolean damage)
	{
		_damage = damage;
		return this;
	}

	public boolean isDamageEnabled()
	{
		return _damage;
	}

	public GameDamageComponent setPvp(boolean pvp)
	{
		_pvp = pvp;
		return this;
	}

	public boolean isPvpEnabled()
	{
		return _pvp;
	}

	public GameDamageComponent setFall(boolean fall)
	{
		_fall = fall;
		return this;
	}

	public boolean isFallEnabled()
	{
		return _fall;
	}

	public GameDamageComponent setSelf(boolean self)
	{
		_self = self;
		return this;
	}

	public boolean isSelfEnabled()
	{
		return _self;
	}

	public GameDamageComponent setTeamSelf(boolean teamSelf)
	{
		_teamSelf = teamSelf;
		return this;
	}

	public boolean isTeamSelfEnabled()
	{
		return _teamSelf;
	}
}
