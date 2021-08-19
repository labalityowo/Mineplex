package nautilus.game.arcade.game.games.hideseek.kits;

import org.bukkit.entity.Player;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.hideseek.HideSeek;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkShockingStrike;

public class KitHiderShocker extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkShockingStrike()
			};

	public KitHiderShocker(ArcadeManager manager)
	{
		super(manager, GameKit.HIDE_AND_SEEK_SHOCKER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().setItem(3, HideSeek.PLAYER_ITEM);
	}
}
