package nautilus.game.arcade.game.games.moba.kit.devon;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.moba.MobaRole;
import nautilus.game.arcade.game.games.moba.kit.HeroKit;
import nautilus.game.arcade.game.games.moba.kit.common.SkillBow;
import nautilus.game.arcade.kit.Perk;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class HeroDevon extends HeroKit
{

	private static final Perk[] PERKS = {
			new SkillBow(0),
			new SkillTNTArrows(1),
			new SkillBoost(2),
			new SkillInfinity(3)
	};

	private static final ItemStack AMMO = new ItemBuilder(Material.ARROW)
			.setTitle(C.cYellowB + "Hunting Arrow")
			.setUnbreakable(true)
			.build();

	public HeroDevon(ArcadeManager manager)
	{
		super(manager, "Devon", PERKS, MobaRole.HUNTER, SkinData.DEVON);

		setAmmo(AMMO, 3000);
		setMaxAmmo(3);
	}
}
