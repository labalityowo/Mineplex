package nautilus.game.arcade.game.games.survivalgames.kit;

import org.bukkit.entity.Player;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkHorsePet;

public class KitHorseman extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkHorsePet()
			};

	public KitHorseman(ArcadeManager manager)
	{
		super(manager, GameKit.SG_HORSEMAN, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{

	}
}
