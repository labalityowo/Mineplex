package nautilus.game.arcade.game.games.survivalgames.kit;

import org.bukkit.entity.Player;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkWolfPet;

public class KitBeastmaster extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkWolfPet(30, 1, false, true)
			};

	public KitBeastmaster(ArcadeManager manager)
	{
		super(manager, GameKit.SG_BEASTMASTER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{

	}
}
