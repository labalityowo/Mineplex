package nautilus.game.arcade.game.games.uhc;

import org.bukkit.entity.Player;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;

public class KitUHC extends Kit
{

	KitUHC(ArcadeManager manager)
	{
		super(manager, GameKit.UHC_PLAYER);
	}

	@Override
	public void GiveItems(Player player) 
	{
	}
}
