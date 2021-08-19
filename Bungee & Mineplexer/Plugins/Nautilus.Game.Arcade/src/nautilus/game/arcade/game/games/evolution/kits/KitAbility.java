package nautilus.game.arcade.game.games.evolution.kits;

import mineplex.core.common.util.C;
import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.evolution.kits.perks.PerkCooldownEVO;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class KitAbility extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkCooldownEVO()
			};

	public KitAbility(ArcadeManager manager)
	{
		super(manager, GameKit.EVOLUTION_ABILITY, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
	}
}
