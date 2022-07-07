package nautilus.game.arcade.game.games.minestrike.kits;

import org.bukkit.entity.Player;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;

public class KitPlayer extends Kit 
{

	public KitPlayer(ArcadeManager manager)
	{
		super(manager, GameKit.MINESTRIKE_PLAYER);
 
	}

	@Override
	public void GiveItems(Player player)
	{
		
	}

}
