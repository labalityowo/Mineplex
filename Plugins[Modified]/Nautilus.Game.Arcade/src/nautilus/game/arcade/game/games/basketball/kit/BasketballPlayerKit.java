package nautilus.game.arcade.game.games.basketball.kit;

import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkDoubleJump;

/**
 * Kit for Basketball Game
 */
public class BasketballPlayerKit extends Kit
{

	private static final Perk[] PERKS =
	{
		new PerkDoubleJump("Hops", new String[] {C.cYellow + "Tap Jump Twice" + C.cGray + " to use " + C.cGreen + "Hops"}, 1.2, 1.2, true, 8000, true)
	};

	public BasketballPlayerKit(ArcadeManager manager)
	{
		super(manager, GameKit.BASKET_BALL_PLAYER, PERKS);
	}
	
	@Override
	public void GiveItems(Player player)
	{

	}
}