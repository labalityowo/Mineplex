package nautilus.game.arcade.game.games.snowfight.kits;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkDoubleJump;
import nautilus.game.arcade.kit.perks.PerkFallDamage;
import nautilus.game.arcade.kit.perks.PerkSpeed;

public class KitSportsman extends KitSnowFight
{

	private static final Perk[] PERKS =
			{
					new PerkSpeed(0),
					new PerkFallDamage(-2),
					new PerkDoubleJump("Snow Jump", 1, 0.8, true, 6000, true),
			};

	public KitSportsman(ArcadeManager manager)
	{
		super(manager, GameKit.SNOW_FIGHT_SPORTSMAN, PERKS);
	}

}
