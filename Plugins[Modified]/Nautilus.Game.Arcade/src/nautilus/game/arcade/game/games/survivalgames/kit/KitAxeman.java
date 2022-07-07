package nautilus.game.arcade.game.games.survivalgames.kit;

import org.bukkit.entity.Player;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkAxeThrower;
import nautilus.game.arcade.kit.perks.PerkAxeman;

public class KitAxeman extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkAxeman(),
					new PerkAxeThrower()
			};

	public KitAxeman(ArcadeManager manager)
	{
		super(manager, GameKit.SG_AXEMAN, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{

	}
}
