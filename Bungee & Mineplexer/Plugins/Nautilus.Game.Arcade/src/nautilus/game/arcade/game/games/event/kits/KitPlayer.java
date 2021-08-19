package nautilus.game.arcade.game.games.event.kits;

import org.bukkit.entity.Player;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.event.EventGame;
import nautilus.game.arcade.kit.Kit;

public class KitPlayer extends Kit
{

	public KitPlayer(ArcadeManager manager)
	{
		super(manager, GameKit.NULL_PLAYER);
	}
	
	@Override
	public void GiveItems(Player player) 
	{
		((EventGame)Manager.GetGame()).giveItems(player);
	}
}
