package nautilus.game.arcade.game.games.moba.kit.rowena;

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

public class HeroRowena extends HeroKit
{

	private static final Perk[] PERKS = {
			new SkillBow(0),
			new SkillLightArrows(1),
			new SkillCombatDash(2),
			new SkillBombardment(3)
	};

	private static final ItemStack AMMO = new ItemBuilder(Material.ARROW)
			.setTitle(C.cYellowB + "Hunting Arrow")
			.setUnbreakable(true)
			.build();

	public HeroRowena(ArcadeManager manager)
	{
		super(manager, "Rowena", PERKS, MobaRole.HUNTER, SkinData.ROWENA, 10);

		setAmmo(AMMO, 2000);
		setMaxAmmo(2);
	}
}
