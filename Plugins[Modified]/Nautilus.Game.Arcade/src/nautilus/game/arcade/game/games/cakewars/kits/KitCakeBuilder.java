package nautilus.game.arcade.game.games.cakewars.kits;

import org.bukkit.entity.Player;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.cakewars.kits.perk.PerkPassiveWoolGain;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;

public class KitCakeBuilder extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkPassiveWoolGain()
			};

	public KitCakeBuilder(ArcadeManager manager)
	{
		super(manager, GameKit.CAKE_WARS_BUILDER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
	}

}
