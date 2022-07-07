package nautilus.game.arcade.game.games.cakewars.kits;

import org.bukkit.entity.Player;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.cakewars.kits.perk.PerkLifeSteal;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;

public class KitCakeWarrior extends Kit
{

	private static final Perk[] PERKS =
			{
				new PerkLifeSteal(6)
			};

	public KitCakeWarrior(ArcadeManager manager)
	{
		super(manager, GameKit.CAKE_WARS_WARRIOR, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
	}
}
