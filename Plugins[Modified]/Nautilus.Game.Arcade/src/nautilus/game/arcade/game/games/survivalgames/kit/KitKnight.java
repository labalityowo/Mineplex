package nautilus.game.arcade.game.games.survivalgames.kit;

import org.bukkit.entity.Player;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkHiltSmash;
import nautilus.game.arcade.kit.perks.PerkIronSkin;

public class KitKnight extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkIronSkin(0.5),
					new PerkHiltSmash()
			};

	public KitKnight(ArcadeManager manager)
	{
		super(manager, GameKit.SG_KNIGHT, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{

	}
}
