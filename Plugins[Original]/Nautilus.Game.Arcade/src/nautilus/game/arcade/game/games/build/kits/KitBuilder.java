package nautilus.game.arcade.game.games.build.kits;

import org.bukkit.entity.Player;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;

public class KitBuilder extends Kit
{

	public KitBuilder(ArcadeManager manager)
	{
		super(manager, GameKit.BUILD_BUILDER);
	}
	
	@Override
	public void GiveItems(Player player)
	{
	}

}