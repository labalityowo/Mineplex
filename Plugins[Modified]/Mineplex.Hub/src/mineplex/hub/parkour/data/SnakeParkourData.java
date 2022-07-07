package mineplex.hub.parkour.data;

import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.hub.parkour.ParkourData;
import mineplex.hub.parkour.ParkourManager;

public class SnakeParkourData extends ParkourData
{

	private final ParkourManager _manager;
	private final List<Snake> _snakes;

	public SnakeParkourData(ParkourManager manager, List<Snake> snakes)
	{
		super(manager, "Snake", new String[]
				{
						"You have to be sssssssslipery",
						"to sssssneak past this parkour",
						"with no mistakes"
				}, ParkourManager.DIFFICULTY_MEDIUM);

		_manager = manager;
		_snakes = snakes;
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER || _manager.getActivePlayers(this).isEmpty())
		{
			return;
		}

		_snakes.forEach(Snake::update);
	}
}
