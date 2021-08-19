package mineplex.game.nano.game.components;

import mineplex.game.nano.game.GameComponent;

public interface ComponentHook<T extends GameComponent>
{

	void setHook(T hook);

	T getHook();

}
