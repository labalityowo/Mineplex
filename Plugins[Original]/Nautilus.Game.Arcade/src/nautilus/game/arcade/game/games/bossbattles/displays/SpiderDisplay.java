package nautilus.game.arcade.game.games.bossbattles.displays;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEnt;
import nautilus.game.arcade.game.games.bossbattles.BattleBoss;
import nautilus.game.arcade.game.games.bossbattles.BossBattles;

public class SpiderDisplay extends BossDisplay
{

	public SpiderDisplay(BossBattles plugin, BattleBoss boss, Location location)
	{
		super(plugin, boss, location);
	}

	@Override
	public String getDisplayName()
	{
		return C.cDBlue + "Brood Mother";
	}

	@Override
	public void start()
	{
		Entity entity = getLocation().getWorld().spawnEntity(getLocation(),
				EntityType.SPIDER);

		UtilEnt.vegetate(entity);
		this.addEntity(entity);
	}

	@Override
	public Location getHologramLocation()
	{
		return getLocation().clone().add(0, 2, 0);
	}

}
