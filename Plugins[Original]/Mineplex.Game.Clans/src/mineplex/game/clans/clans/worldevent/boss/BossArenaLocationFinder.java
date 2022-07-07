package mineplex.game.clans.clans.worldevent.boss;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import mineplex.core.common.Pair;

public class BossArenaLocationFinder
{
	private World _world;
	
	public BossArenaLocationFinder(World world)
	{
		_world = world;
	}
	
	public Location getIronWizardCenter()
	{
		return new Location(_world, 1057, 63, -77);
	}
	
	public Pair<List<Vector>, List<Vector>> getIronWizardPads()
	{
		List<Vector> in = new ArrayList<>();
		List<Vector> out = new ArrayList<>();
		
		in.add(new Vector(1006, 62, -77));
		in.add(new Vector(1057, 62, -26));
		in.add(new Vector(1108, 62, -77));
		in.add(new Vector(1057, 62, -128));
		
		out.add(new Vector(1035, 63, -77));
		out.add(new Vector(1057, 63, -99));
		out.add(new Vector(1079, 63, -77));
		out.add(new Vector(1057, 63, -55));
		
		return Pair.create(in, out);
	}
	
	public Location getSkeletonKingCenter()
	{
		return new Location(_world, -1043, 58, 159);
	}
	
	public Pair<List<Vector>, List<Vector>> getSkeletonKingPads()
	{
		List<Vector> in = new ArrayList<>();
		List<Vector> out = new ArrayList<>();
		
		in.add(new Vector(-1094, 57, 159));
		in.add(new Vector(-1043, 57, 210));
		in.add(new Vector(-992, 57, 159));
		in.add(new Vector(-1043, 57, 108));
		
		out.add(new Vector(-1021, 58, 159));
		out.add(new Vector(-1043, 58, 137));
		out.add(new Vector(-1065, 58, 159));
		out.add(new Vector(-1043, 58, 181));
		
		return Pair.create(in, out);
	}
}