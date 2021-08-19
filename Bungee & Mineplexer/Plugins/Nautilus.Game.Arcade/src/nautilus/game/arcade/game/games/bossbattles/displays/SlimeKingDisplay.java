package nautilus.game.arcade.game.games.bossbattles.displays;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEnt;
import nautilus.game.arcade.game.games.bossbattles.BattleBoss;
import nautilus.game.arcade.game.games.bossbattles.BossBattles;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Slime;

public class SlimeKingDisplay extends BossDisplay
{
	private Slime _slime;

	public SlimeKingDisplay(BossBattles plugin, BattleBoss boss,
			Location location)
	{
		super(plugin, boss, location);
	}

	@Override
	public void start()
	{
		_slime = (Slime) getLocation().getWorld().spawnEntity(getLocation(),
				EntityType.SLIME);
		_slime.setSize(4);

		_slime.teleport(getLocation());

		UtilEnt.vegetate(_slime);

		addEntity(_slime);
	}

	@Override
	public String getDisplayName()
	{
		return C.cDGreen + "Slime King";
	}

	@Override
	public void spawnHologram()
	{
		super.spawnHologram();

		getHologram().setFollowEntity(_slime);
	}

	@Override
	public Location getHologramLocation()
	{
		return _slime.getEyeLocation().add(0, 0.1, 0);
	}

}
