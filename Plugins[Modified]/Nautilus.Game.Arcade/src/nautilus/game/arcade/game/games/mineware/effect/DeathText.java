package nautilus.game.arcade.game.games.mineware.effect;

import mineplex.core.common.util.UtilMath;

/**
 * This class provides random phrases that are shown when a death effect is triggered.
 */
public class DeathText
{
	private String[] _text = {
		"Nom nom nom!",
		"Bawk Bawk!",
		"Delicious Meal!",
		"Nom nom, bawk!",
		"Yummy, delicious!"
	};

	public String getRandom()
	{
		return UtilMath.randomElement(_text);
	}
}
