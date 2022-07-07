package nautilus.game.arcade.game.games.evolution.kits;

import org.bukkit.entity.Player;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.evolution.kits.perks.PerkEvolveSpeedEVO;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;

public class KitEvolveSpeed extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkEvolveSpeedEVO()
			};

	public KitEvolveSpeed(ArcadeManager manager)
	{
		super(manager, GameKit.EVOLUTION_EVOLVE_SPEED, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
	}
}
