package nautilus.game.arcade.game.games.rings;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;

public class Ring
{
	private ArrayList<Block> _core = new ArrayList<Block>();
	private ArrayList<Block> _ring = new ArrayList<Block>();
	private Location _center;

	public Ring(ArrayList<Block> blocks, ArrayList<Block> inside, Location center)
	{
		_core = inside;
		_ring = blocks;
		_center = center;
	}

	public Location getCenter()
	{
		return _center;
	}

	public ArrayList<Block> getRing()
	{
		return _ring;
	}

	public boolean isMoveThroughRing(Location from, Location to)
	{
		from = from.clone();
		to = to.clone();
		from.setX(from.getBlockX() + 0.5);
		from.setY(from.getBlockY() + 0.5);
		from.setZ(from.getBlockZ() + 0.5);
		to.setX(to.getBlockX() + 0.5);
		to.setY(to.getBlockY() + 0.5);
		to.setZ(to.getBlockZ() + 0.5);

		Vector vec = UtilAlg.getTrajectory(from, to).multiply(0.5);
		double dist = UtilMath.offset(from, to);

		while (dist > 0)
		{
			dist -= 0.5;

			Location loc = from.getBlock().getLocation().add(0.5, 0.5, 0.5);

			if (_core.contains(loc.getBlock()))
				return true;

			if (_core.contains(loc.clone().add(loc.getX() == 0 ? 0 : loc.getX() > 0 ? 1 : -1, 0, 0)))
				return true;

			if (_core.contains(loc.clone().add(0, loc.getY() == 0 ? 0 : loc.getY() > 0 ? 1 : -1, 0)))
				return true;

			if (_core.contains(loc.clone().add(0, 0, loc.getZ() == 0 ? 0 : loc.getZ() > 0 ? 1 : -1)))
				return true;

			from.add(vec);
		}

		return false;
	}

}
