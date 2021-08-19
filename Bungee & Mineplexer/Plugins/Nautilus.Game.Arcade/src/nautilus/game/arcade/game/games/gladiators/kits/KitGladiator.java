package nautilus.game.arcade.game.games.gladiators.kits;

import org.bukkit.entity.Player;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;

public class KitGladiator extends Kit
{

	public KitGladiator(ArcadeManager manager)
	{
		super(manager, GameKit.GLADIATORS_PLAYER);
	}

	@Override
	public void GiveItems(Player player)
	{
	}
}
