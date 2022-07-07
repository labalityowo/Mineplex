package mineplex.gemhunters.worldevent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.event.EventHandler;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextTop;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.gemhunters.worldevent.blizzard.BlizzardWorldEvent;
import mineplex.gemhunters.worldevent.command.WorldEventCommand;
import mineplex.gemhunters.worldevent.giant.GiantWorldEvent;
import mineplex.gemhunters.worldevent.gwenmart.GwenMartWorldEvent;
import mineplex.gemhunters.worldevent.ufo.UFOWorldEvent;
import mineplex.gemhunters.worldevent.wither.WitherWorldEvent;

@ReflectivelyCreateMiniPlugin
public class WorldEventModule extends MiniPlugin
{
	public enum Perm implements Permission
	{
		WORLD_EVENT_COMMAND,
		START_WORLD_EVENT_COMMAND,
		STOP_WORLD_EVENT_COMMAND,
	}

	private static final long EVENT_TIMER = TimeUnit.MINUTES.toMillis(30);
	private static final long EVENT_COOLDOWN_TIMER = TimeUnit.MINUTES.toMillis(40);
	private static final long COMPLETE_TIMER = TimeUnit.SECONDS.toMillis(30);

	private final List<WorldEvent> _events;

	private WorldEventModule()
	{
		super("World Event");

		_events = Arrays.asList(
				new GiantWorldEvent(),
				new BlizzardWorldEvent(),
				//new NetherPortalWorldEvent(),
				new WitherWorldEvent(),
				new GwenMartWorldEvent(),
				new UFOWorldEvent()
		);
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.ADMIN.setPermission(Perm.WORLD_EVENT_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.START_WORLD_EVENT_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.STOP_WORLD_EVENT_COMMAND, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new WorldEventCommand(this));
	}

	public void startEvent(WorldEventType eventType)
	{
		WorldEvent event = getEvent(eventType);

		event.setEventState(WorldEventState.WARMUP);
	}

	public void startRandomEvent()
	{
		WorldEventType[] eventTypes = WorldEventType.values();
		Set<WorldEventType> possibleWorldEvents = new HashSet<>();

		for (WorldEventType eventType : eventTypes)
		{
			if (UtilTime.elapsed(eventType.getLast(), EVENT_COOLDOWN_TIMER) || eventType.getPriority() == WorldEventPriority.TRIGGERED)
			{
				continue;
			}

			possibleWorldEvents.add(eventType);
		}

		if (possibleWorldEvents.isEmpty())
		{
			return;
		}

		startEvent(UtilAlg.Random(possibleWorldEvents));
	}

	@EventHandler
	public void checkNextEvent(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		Iterator<WorldEvent> iterator = _events.iterator();

		while (iterator.hasNext())
		{
			WorldEvent worldEvent = iterator.next();

			if (worldEvent.getEventState() == WorldEventState.COMPLETE && UtilTime.elapsed(worldEvent.getCompleteTime(), COMPLETE_TIMER))
			{
				worldEvent.setEventState(null);
			}
			else if (worldEvent.getEventState() == WorldEventState.LIVE && worldEvent.checkToEnd())
			{
				worldEvent.setEventState(WorldEventState.COMPLETE);
			}
		}
	}

	@EventHandler
	public void displayStatus(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !isEventActive())
		{
			return;
		}
		
		WorldEvent worldEvent = getActiveEvents().get(0);
		
		UtilTextTop.displayProgress(C.cRed + worldEvent.getEventType().getName() + C.cYellow +  " -> " + C.cRed + worldEvent.getEventState().getName(), worldEvent.getProgress(), UtilServer.getPlayers());
	}
	
	public WorldEvent getEvent(WorldEventType eventType)
	{
		for (WorldEvent event : _events)
		{
			if (event.getEventType() == eventType)
			{
				return event;
			}
		}

		return null;
	}

	public boolean isEventActive()
	{
		return !getActiveEvents().isEmpty();
	}

	public List<WorldEvent> getActiveEvents()
	{
		List<WorldEvent> events = new ArrayList<>();

		for (WorldEvent event : _events)
		{
			if (event.isInProgress())
			{
				events.add(event);
			}
		}

		return events;
	}

	public long getEventTimer()
	{
		return EVENT_TIMER;
	}
}