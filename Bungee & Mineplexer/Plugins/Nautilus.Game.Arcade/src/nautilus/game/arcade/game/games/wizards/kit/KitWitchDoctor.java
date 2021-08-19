package nautilus.game.arcade.game.games.wizards.kit;

import org.bukkit.entity.Player;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.wizards.Wizards;
import nautilus.game.arcade.kit.Kit;

public class KitWitchDoctor extends Kit
{

	public KitWitchDoctor(ArcadeManager manager)
	{
		super(manager, GameKit.WIZARDS_WITCH_DOCTOR);
	}

	@Override
	public void GiveItems(Player player)
	{
		((Wizards) this.Manager.GetGame()).setupWizard(player);
	}
}
