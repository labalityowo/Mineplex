package nautilus.game.arcade.game.games.hideseek.kits;

import org.bukkit.entity.Player;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.hideseek.HideSeek;
import nautilus.game.arcade.kit.Kit;

public class KitHiderSwapper extends Kit
{

	public KitHiderSwapper(ArcadeManager manager)
	{
		super(manager, GameKit.HIDE_AND_SEEK_SWAPPER);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().setItem(3, HideSeek.PLAYER_ITEM);
	}
}
