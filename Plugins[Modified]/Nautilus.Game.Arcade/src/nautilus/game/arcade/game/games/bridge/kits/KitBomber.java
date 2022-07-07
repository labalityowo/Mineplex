package nautilus.game.arcade.game.games.bridge.kits;

import org.bukkit.entity.Player;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkBomber;

public class KitBomber extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkBomber(25, 2, -1)
			};

	public KitBomber(ArcadeManager manager)
	{
		super(manager, GameKit.BRIDGES_BOMBER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{

	}
}
