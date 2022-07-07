package nautilus.game.arcade.game.games.moba.kit.anath;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.moba.MobaRole;
import nautilus.game.arcade.game.games.moba.kit.HeroKit;
import nautilus.game.arcade.kit.Perk;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class HeroAnath extends HeroKit
{

	private static final Perk[] PERKS = {
			new SkillFireProjectile(0),
			new SkillBurnBeam(1),
			new SkillFlameDash(2),
			new SkillMeteor(3)
	};

	private static final ItemStack AMMO = new ItemBuilder(Material.BLAZE_POWDER)
			.setTitle(C.cYellowB + "Embers")
			.build();

	public HeroAnath(ArcadeManager manager)
	{
		super(manager, "Anath", PERKS, MobaRole.MAGE, SkinData.ANATH);

		setAmmo(AMMO, 1000);
		setMaxAmmo(4);
	}
}
