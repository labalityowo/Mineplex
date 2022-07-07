package nautilus.game.arcade.game.games.moba.kit.ivy;

import mineplex.core.common.skin.SkinData;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.moba.MobaRole;
import nautilus.game.arcade.game.games.moba.kit.HeroKit;
import nautilus.game.arcade.game.games.moba.kit.common.SkillSword;
import nautilus.game.arcade.kit.Perk;

public class HeroIvy extends HeroKit
{

	private static final Perk[] PERKS = {
			new SkillSword(0),
			new SkillHook(1),
			new SkillFloralLeap(2),
			new SkillBoxingRing(3)
	};

	public HeroIvy(ArcadeManager manager)
	{
		super(manager, "Ivy", PERKS, MobaRole.WARRIOR, SkinData.IVY, 20);
	}
}
