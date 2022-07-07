package nautilus.game.arcade.game.games.bossbattles.displays;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEnt;
import nautilus.game.arcade.game.games.bossbattles.BattleBoss;
import nautilus.game.arcade.game.games.bossbattles.BossBattles;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Sheep;

public class SnakeDisplay extends BossDisplay
{
	private Sheep _sheep;

	public SnakeDisplay(BossBattles plugin, BattleBoss boss, Location location)
	{
		super(plugin, boss, location);
	}

	@Override
	public void start()
	{
		_sheep = (Sheep) getLocation().getWorld().spawnEntity(getLocation(),
				EntityType.SHEEP);

		_sheep.teleport(getLocation());

		UtilEnt.vegetate(_sheep);

		addEntity(_sheep);
	}

	@Override
	public String getDisplayName()
	{
		return C.cDGreen + "SNAAAAAAAAAAAAAAAAAKE";
	}

	@Override
	public void spawnHologram()
	{
		super.spawnHologram();

		getHologram().setFollowEntity(_sheep);
	}

	@Override
	public Location getHologramLocation()
	{
		return _sheep.getEyeLocation().add(0, 0.1, 0);
	}

}
