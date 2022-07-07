package nautilus.game.arcade.game.games.moba.modes;

import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.boss.pumpkin.PumpkinBoss;
import org.bukkit.Bukkit;

public class MobaHeroesValleyMap extends MobaMap
{

	public MobaHeroesValleyMap(Moba host)
	{
		super(host);

		new PumpkinBoss(host, host.WorldData.GetDataLocs("BLACK").get(0))
				.registerBoss();
	}

}
