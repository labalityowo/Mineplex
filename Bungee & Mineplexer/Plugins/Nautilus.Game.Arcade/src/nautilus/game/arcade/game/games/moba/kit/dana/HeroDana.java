package nautilus.game.arcade.game.games.moba.kit.dana;

import mineplex.core.common.skin.SkinData;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.moba.MobaRole;
import nautilus.game.arcade.game.games.moba.kit.HeroKit;
import nautilus.game.arcade.game.games.moba.kit.common.SkillSword;
import nautilus.game.arcade.kit.Perk;

public class HeroDana extends HeroKit
{

	private static final Perk[] PERKS = {
			new SkillSword(0),
			new SkillPulseHeal(1),
			new SkillDanaDash(2),
			new SkillRally(3),
	};

	public HeroDana(ArcadeManager manager)
	{
		super(manager, "Dana", PERKS, MobaRole.WARRIOR, SkinData.DANA);
	}
}
