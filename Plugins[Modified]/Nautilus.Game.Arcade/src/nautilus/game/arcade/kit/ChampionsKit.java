package nautilus.game.arcade.kit;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;

/**
 * Champions kit class to ensure backwards compatibility with the new KitProgression System
 */
public abstract class ChampionsKit extends Kit
{
	public ChampionsKit(ArcadeManager manager, GameKit gameKit, Perk... perks)
	{
		super(manager, gameKit, perks);
	}
}
