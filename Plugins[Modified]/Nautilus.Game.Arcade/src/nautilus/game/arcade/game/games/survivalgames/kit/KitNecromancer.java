package nautilus.game.arcade.game.games.survivalgames.kit;

import org.bukkit.entity.Player;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkSkeletons;

import mineplex.core.common.util.C;

public class KitNecromancer extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkSkeletons(true)
			};

	public KitNecromancer(ArcadeManager manager)
	{
		super(manager, GameKit.SG_NECROMANCER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{

	}
}
