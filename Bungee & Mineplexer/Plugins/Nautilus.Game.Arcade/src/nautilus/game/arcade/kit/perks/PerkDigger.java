package nautilus.game.arcade.kit.perks;

import mineplex.core.common.util.C;
import nautilus.game.arcade.kit.Perk;

import org.bukkit.entity.Player;

public class PerkDigger extends Perk
{
	public PerkDigger() 
	{
		super("Digger", new String[] 
				{ 
				C.cGray + "Permanent Fast Digging II",
				});
	}
		
	@Override
	public void Apply(Player player)
	{		
		addEffects(player);
	}

	private void addEffects(Player player)
	{
		Manager.GetCondition().Factory().DigFast(GetName(), player, null, Integer.MAX_VALUE, 1, false, false, true);
	}
}
