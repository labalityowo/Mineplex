package nautilus.game.arcade.game.games.moba.kit.larissa;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.moba.MobaRole;
import nautilus.game.arcade.game.games.moba.kit.HeroKit;
import nautilus.game.arcade.kit.Perk;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class HeroLarissa extends HeroKit
{

	private static final Perk[] PERKS = {
			new SkillAquaCannon(0),
			new SkillAOEHeal(1),
			new SkillWaterDash(2),
			new SkillStormHeal(3)
	};

	private static final ItemStack AMMO = new ItemBuilder(Material.INK_SACK, (byte) 4)
			.setTitle(C.cYellowB + "Water Stone")
			.setUnbreakable(true)
			.build();

	public HeroLarissa(ArcadeManager manager)
	{
		super(manager, "Larissa", PERKS, MobaRole.MAGE, SkinData.LARISSA, 10);

		setAmmo(AMMO, 3000);
		setMaxAmmo(6);
	}

}
