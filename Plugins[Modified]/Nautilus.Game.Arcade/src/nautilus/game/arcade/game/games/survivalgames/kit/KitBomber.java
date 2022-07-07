package nautilus.game.arcade.game.games.survivalgames.kit;

import org.bukkit.entity.Player;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkBomberHG;
import nautilus.game.arcade.kit.perks.PerkTNTArrow;

public class KitBomber extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkBomberHG(30, 2),
					new PerkTNTArrow()
			};

	public KitBomber(ArcadeManager manager)
	{
		super(manager, GameKit.SG_BOMBER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{

	}
}
