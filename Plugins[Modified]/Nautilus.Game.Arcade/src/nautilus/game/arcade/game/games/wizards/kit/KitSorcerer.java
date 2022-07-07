package nautilus.game.arcade.game.games.wizards.kit;

import org.bukkit.entity.Player;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.wizards.Wizards;
import nautilus.game.arcade.kit.Kit;

public class KitSorcerer extends Kit
{

	public KitSorcerer(ArcadeManager manager)
	{
		super(manager, GameKit.WIZARDS_SORCERER);
	}

	@Override
	public void GiveItems(Player player)
	{
		((Wizards) this.Manager.GetGame()).setupWizard(player);
	}
}
