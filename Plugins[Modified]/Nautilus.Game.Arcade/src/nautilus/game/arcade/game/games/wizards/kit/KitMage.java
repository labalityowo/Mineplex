package nautilus.game.arcade.game.games.wizards.kit;

import org.bukkit.entity.Player;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.wizards.Wizards;
import nautilus.game.arcade.kit.Kit;

public class KitMage extends Kit
{

	public KitMage(ArcadeManager manager)
	{
		super(manager, GameKit.WIZARDS_MAGE);
	}

	@Override
	public void GiveItems(Player player)
	{
		((Wizards) this.Manager.GetGame()).setupWizard(player);
	}
}
