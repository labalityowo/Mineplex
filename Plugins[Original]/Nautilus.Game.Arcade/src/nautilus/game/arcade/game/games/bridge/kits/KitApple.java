package nautilus.game.arcade.game.games.bridge.kits;

import org.bukkit.entity.Player;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkApple;

public class KitApple extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkApple(7000)
			};

	public KitApple(ArcadeManager manager)
	{
		super(manager, GameKit.BRIDGES_APPLER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{

	}
}