package nautilus.game.arcade.game.games.minecraftleague.kit;

import org.bukkit.entity.Player;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.minecraftleague.MinecraftLeague;
import nautilus.game.arcade.kit.Kit;

public class KitPlayer extends Kit
{

	public KitPlayer(ArcadeManager manager)
	{
		super(manager, GameKit.MC_LEAGUE_PLAYER);
	}

	@Override
	public void GiveItems(Player player)
	{
		MinecraftLeague game = (MinecraftLeague)Manager.GetGame();
		
		if (game != null)
		{
			game.restoreGear(player);
			game.MapManager.setMap(player);
		}
	}
}
