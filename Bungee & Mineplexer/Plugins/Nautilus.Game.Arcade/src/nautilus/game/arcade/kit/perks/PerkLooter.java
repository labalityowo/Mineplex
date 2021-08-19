package nautilus.game.arcade.kit.perks;

import mineplex.core.common.util.C;
import nautilus.game.arcade.kit.Perk;

public class PerkLooter extends Perk
{
	public PerkLooter() 
	{
		super("Looter", new String[] 
				{ 
				C.cGray + "You find extra loot in chests.",
				});
	}
}
