package mineplex.core.gadget.mission;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.core.gadget.event.ItemGadgetUseEvent;
import mineplex.core.mission.MissionManager;
import mineplex.core.mission.MissionTracker;
import mineplex.core.mission.MissionTrackerType;

public class GadgetUseTracker extends MissionTracker
{

	public GadgetUseTracker(MissionManager manager)
	{
		super(manager, MissionTrackerType.LOBBY_GADGET_USE);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void gadgetUse(ItemGadgetUseEvent event)
	{
		_manager.incrementProgress(event.getPlayer(), 1, _trackerType, null, event.getGadget().getClass());
	}
}
