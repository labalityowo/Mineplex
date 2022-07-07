package nautilus.game.arcade.game.games.bossbattles.displays;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEnt;
import nautilus.game.arcade.game.games.bossbattles.BattleBoss;
import nautilus.game.arcade.game.games.bossbattles.BossBattles;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;

public class IronWizardDisplay extends BossDisplay
{
	private IronGolem _golem;

	public IronWizardDisplay(BossBattles plugin, BattleBoss boss,
			Location location)
	{
		super(plugin, boss, location);
	}

	@Override
	public void start()
	{
		_golem = (IronGolem) getLocation().getWorld().spawnEntity(getLocation(),
				EntityType.IRON_GOLEM);
		_golem.teleport(getLocation());
		UtilEnt.vegetate(_golem);

		addEntity(_golem);
	}

	@Override
	public String getDisplayName()
	{
		return C.cGray + "Iron Wizard";
	}

	@Override
	public Location getHologramLocation()
	{
		return _golem.getEyeLocation().add(0, 0.3, 0);
	}

	@Override
	public void spawnHologram()
	{
		super.spawnHologram();

		getHologram().setFollowEntity(_golem);
	}

}
