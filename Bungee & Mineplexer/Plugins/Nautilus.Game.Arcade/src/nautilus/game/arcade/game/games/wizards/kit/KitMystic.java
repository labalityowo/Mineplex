package nautilus.game.arcade.game.games.wizards.kit;

import org.bukkit.entity.Player;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.wizards.Wizards;
import nautilus.game.arcade.kit.Kit;

public class KitMystic extends Kit
{

	public KitMystic(ArcadeManager manager)
	{
		super(manager, GameKit.WIZARDS_MYSTIC);
	}

	@Override
	public void GiveItems(Player player)
	{
		((Wizards) this.Manager.GetGame()).setupWizard(player);
	}
}
