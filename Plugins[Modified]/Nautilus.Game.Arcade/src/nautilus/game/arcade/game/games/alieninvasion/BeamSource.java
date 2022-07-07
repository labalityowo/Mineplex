package nautilus.game.arcade.game.games.alieninvasion;

import nautilus.game.arcade.ArcadeManager;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class BeamSource
{

	private final Location _source;
	private final List<Beam> _beams;

	BeamSource(Location source)
	{
		_source = source;
		_beams = new ArrayList<>(5);
	}

	public void addBeam(ArcadeManager manager, int id, Location target)
	{
		_beams.add(new Beam(manager, id, this, target));
	}

	public Location getSource()
	{
		return _source;
	}

	public Beam getFromId(int id)
	{
		for (Beam beam : _beams)
		{
			if (beam.getId() == id)
			{
				return beam;
			}
		}

		return null;
	}
}
