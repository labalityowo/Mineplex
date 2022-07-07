package nautilus.game.arcade.game.games.castlesiegenew.kits;

import org.bukkit.entity.Player;

import mineplex.core.disguise.disguises.DisguisePigZombie;
import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkLeap;
import nautilus.game.arcade.kit.perks.PerkSpeed;

public class KitUndeadGhoul extends KitCastleSiege
{

	private static final String LEAP = "Ghoul Leap";
	private static final Perk[] PERKS =
			{
					new PerkLeap(LEAP, 1.2, 0.8, 8000),
					new PerkSpeed(0)
			};

	public KitUndeadGhoul(ArcadeManager manager)
	{
		super(manager, GameKit.CASTLE_SIEGE_UNDEAD_GHOUL, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		giveItems(player);
		disguise(player, DisguisePigZombie.class);
	}
}
