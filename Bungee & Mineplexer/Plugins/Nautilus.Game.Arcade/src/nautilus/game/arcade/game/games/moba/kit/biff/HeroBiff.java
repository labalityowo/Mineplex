package nautilus.game.arcade.game.games.moba.kit.biff;

import mineplex.core.common.skin.SkinData;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.moba.MobaRole;
import nautilus.game.arcade.game.games.moba.kit.HeroKit;
import nautilus.game.arcade.game.games.moba.kit.common.SkillSword;
import nautilus.game.arcade.kit.Perk;

public class HeroBiff extends HeroKit
{

	private static final Perk[] PERKS = {
			new SkillSword(0),
			new SkillLeash(1),
			new SkillBiffDash(2),
			new SkillWarHorse(3)
	};

	public HeroBiff(ArcadeManager manager)
	{
		super(manager, "Biff", PERKS, MobaRole.WARRIOR, SkinData.BIFF, 10);
	}
}
