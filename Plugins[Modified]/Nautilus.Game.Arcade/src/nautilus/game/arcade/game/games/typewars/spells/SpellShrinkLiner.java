package nautilus.game.arcade.game.games.typewars.spells;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.common.util.UtilParticle.ParticleType;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.typewars.Spell;

public class SpellShrinkLiner extends Spell
{
	public SpellShrinkLiner(ArcadeManager manager)
	{
		super(manager, "Shrinking Line", 7, Material.BLAZE_ROD, 2000L, 10, 0, false);
	}

	@Override
	public ParticleType trail()
	{
		return ParticleType.FLAME;
	}

	@Override
	public boolean execute(final Player player, Location location)
	{
		final List<Location> line = getLine(player, location);
		for(Location loc : line)
		{
			getTypeWars().getLineShorten().get(getManager().GetGame().GetTeam(player)).add(loc);
		}
		
		getManager().runSyncLater(new Runnable()
		{
			@Override
			public void run()
			{
				for(Location loc : line)
				{
					getTypeWars().getLineShorten().get(getManager().GetGame().GetTeam(player)).remove(loc);
				}
			}
		}, 180);
		return true;
	}
	
	private List<Location> getLine(Player player, Location location)
	{
		List<Location> line = new ArrayList<>();
		List<Location> spawns =  getTypeWars().getMinionSpawns().get(getManager().GetGame().GetTeam(player));
		for(Location loc : spawns)
		{
			if(loc.getBlockX() == location.getBlockX() || loc.getBlockX() == location.getBlockX() - 1  || loc.getBlockX() == location.getBlockX() + 1)
			{
				for(Location locs : getTypeWars().getMinionSpawns().get(getManager().GetGame().GetTeam(player)))
				{
					Location newLoc = locs.clone();
					newLoc.setZ(location.getBlockZ());
					line.add(newLoc);
					Location pos1 = newLoc.clone().add(1, 0, 0);
					Location pos2 = newLoc.clone().add(-1, 0, 0);
					boolean addLoc1 = true;
					boolean addLoc2 = true;
					for(Location otherLoc : line)
					{
						if(otherLoc.equals(pos1))
							addLoc1 = false;
					}
					for(Location otherLoc : line)
					{
						if(otherLoc.equals(pos2))
							addLoc2 = false;
					}
					if(addLoc1)
						line.add(pos1);
					
					if(addLoc2)
						line.add(pos2);
				}
				break;
			}
			if(loc.getBlockZ() == location.getBlockZ() || loc.getBlockZ() == location.getBlockZ() - 1  || loc.getBlockZ() == location.getBlockZ() + 1)
			{
				for(Location locs : getTypeWars().getMinionSpawns().get(getManager().GetGame().GetTeam(player)))
				{
					Location newLoc = locs.clone();
					newLoc.setX(location.getBlockX());
					line.add(newLoc);
					Location pos1 = newLoc.clone().add(0, 0, 1);
					Location pos2 = newLoc.clone().add(0, 0, -1);
					boolean addLoc1 = true;
					boolean addLoc2 = true;
					for(Location otherLoc : line)
					{
						if(otherLoc.equals(pos1))
							addLoc1 = false;
					}
					for(Location otherLoc : line)
					{
						if(otherLoc.equals(pos2))
							addLoc2 = false;
					}
					if(addLoc1)
						line.add(pos1);
					
					if(addLoc2)
						line.add(pos2);
				}
				break;
			}
		}
		for(Location loc : spawns)
		{
			Iterator<Location> locIterator = line.iterator();
			while(locIterator.hasNext())
			{
				Location locs = locIterator.next();
				if(locs.equals(loc))
				{
					locIterator.remove();
				}
				if(locs.getBlock().getType() != Material.AIR)
					locIterator.remove();
				
			}
		}
		return line;	
	}

}
