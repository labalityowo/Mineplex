package nautilus.game.arcade.game.games.survivalgames.kit;

import org.bukkit.entity.Player;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkMammoth;
import nautilus.game.arcade.kit.perks.PerkSeismicSlamHG;

public class KitBrawler extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkMammoth(),
					new PerkSeismicSlamHG()
			};

	public KitBrawler(ArcadeManager manager)
	{
		super(manager, GameKit.SG_BRAWLER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{

	}
}
