package nautilus.game.arcade.game.games.moba.kit.bob;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.moba.MobaRole;
import nautilus.game.arcade.game.games.moba.kit.HeroKit;
import nautilus.game.arcade.kit.Perk;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class HeroBob extends HeroKit
{

	private static final Perk[] PERKS = {
			new SkillPaint(0),
			new SkillHappyTrees(1),
			new SkillBeatTheDevil(2),
			new SkillBuildPainting(3)
	};

	private static final ItemStack AMMO = new ItemBuilder(Material.SNOW_BALL)
			.setTitle(C.cYellowB + "Titanium Hwhite")
			.build();

	public HeroBob(ArcadeManager manager)
	{
		super(manager, "Bob Ross", PERKS, MobaRole.MAGE, SkinData.BOB_ROSS);

		setAmmo(AMMO, 500);
		setMaxAmmo(8);
		setVisible(false);
	}

}
