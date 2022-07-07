package nautilus.game.arcade.game.games.mineware.kit;

import org.bukkit.entity.Player;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;

/**
 * The default bawk bawk battles game kit.
 */
public class KitBawksFood extends Kit
{

	public KitBawksFood(ArcadeManager manager)
	{
		super(manager, GameKit.BAWK_BAWK_PLAYER);
	}

	@Override
	public void GiveItems(Player player)
	{

	}
}
