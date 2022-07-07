package nautilus.game.arcade.game.games.draw.kits;

import org.bukkit.entity.Player;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;

public class KitArtist extends Kit
{

	public KitArtist(ArcadeManager manager)
	{
		super(manager, GameKit.DRAW_ARTIST);
	}

	@Override
	public void GiveItems(Player player)
	{
	}
}
