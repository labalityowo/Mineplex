package nautilus.game.arcade.game.games.moba.kit.hattori;

import mineplex.core.common.skin.SkinData;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.moba.MobaRole;
import nautilus.game.arcade.game.games.moba.kit.HeroKit;
import nautilus.game.arcade.game.games.moba.kit.common.SkillSword;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkDoubleJump;

public class HeroHattori extends HeroKit
{

	private static final Perk[] PERKS = {
			new PerkDoubleJump("Double Jump", 1, 1, true, 3000, true),
			new SkillSword(0),
			new SkillSnowball(1),
			new SkillNinjaDash(2),
			new SkillNinjaBlade(3)
	};

	public HeroHattori(ArcadeManager manager)
	{
		super(manager, "Hattori", PERKS, MobaRole.ASSASSIN, SkinData.HATTORI);
	}
}
