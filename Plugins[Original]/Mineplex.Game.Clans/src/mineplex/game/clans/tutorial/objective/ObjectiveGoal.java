package mineplex.game.clans.tutorial.objective;

import java.util.*;

import mineplex.core.common.util.UtilFirework;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import org.bukkit.*;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.tutorial.TutorialRegion;

public abstract class ObjectiveGoal <T extends Objective<?, ?>> implements Listener
{
	private T _objective;

	private HashSet<UUID> _active;
	private HashSet<UUID> _removeList;
	private String _name;
	private String _description;
	private String _extraDescription;
	private boolean _displayStartMessage;
	private int _startMessageDelay;
	private boolean _displayFinishMessage;
	private int _finishMessageDelay;
	private DyeColor _fireworkLocations;

	public ObjectiveGoal(T objective, String name, String description)
	{
		this(objective, name, description, null, null);
	}

	public ObjectiveGoal(T objective, String name, String description, String extraDescription, DyeColor fireworkLocs)
	{
		_objective = objective;

		_active = new HashSet<>();
		_removeList = new HashSet<>();
		_name = name;
		_description = description;
		_extraDescription = extraDescription;
		_displayStartMessage = true;
		_startMessageDelay = 1;//40;
		_displayFinishMessage = false;
		_finishMessageDelay = 1;
		_fireworkLocations = fireworkLocs;
	}

	public String getName(Player player)
	{
		return _name;
	}
	
	public String getDescription(Player player)
	{
		return _description;
	}

	public String getExtraDescription(Player player)
	{
		return _extraDescription;
	}
	
	public Set<UUID> getActivePlayers()
	{
		return _active;
	}
	
	public boolean contains(Player player)
	{
		if (player == null || !player.isOnline())
		{
			return false;
		}
		
		return _active.contains(player.getUniqueId());
	}

	public final void start(Player player)
	{
		System.out.println(String.format("Tutorial> [%s] started objective goal [%s]", player.getName(), getName(player)));

		_active.add(player.getUniqueId());

		if (_displayStartMessage)
		{
			Bukkit.getServer().getScheduler().runTaskLater(_objective.getJavaPlugin(), () -> displayStartMessage(player), _startMessageDelay);
		}

		customStart(player);
	}

	protected abstract void customStart(Player player);

	protected abstract void customFinish(Player player);

	protected void customLeave(Player player) { }

	protected void leave(Player player)
	{
		if (_active.contains(player.getUniqueId()))
		{
			System.out.println(String.format("Tutorial> [%s] left objective goal [%s]", player.getName(), getName(player)));

			_removeList.add(player.getUniqueId());
		}

		customLeave(player);
	}

	protected void finish(Player player)
	{
		if (_active.contains(player.getUniqueId()))
		{
			System.out.println(String.format("Tutorial> [%s] finished objective goal [%s]", player.getName(), getName(player)));

			if (getObjective().getPlugin().getTutorialSession(player) != null)
				getObjective().getPlugin().getTutorialSession(player).setTextSeconds(0);

			_removeList.add(player.getUniqueId());

			if (_displayFinishMessage)
			{
				Bukkit.getServer().getScheduler().runTaskLater(_objective.getJavaPlugin(), () -> displayFinishMessage(player), _finishMessageDelay);
			}

			player.playSound(player.getEyeLocation(), Sound.ORB_PICKUP, 1f, 1f);

			customFinish(player);

			_objective.completeGoal(this, player);
		}
	}

	/**
	 * Called when a player starts the tutorial with this goal
	 */
	protected void setup(Player player, TutorialRegion region)
	{

	}

	/**
	 * Called when a player finishes the tutorial with this goal
	 */
	protected void clean(Player player, TutorialRegion region)
	{
		_removeList.add(player.getUniqueId());
	}

	public T getObjective()
	{
		return _objective;
	}

	protected void displayFinishMessage(Player player)
	{
		UtilTextMiddle.display(C.cGreen + "Completed Objective", getName(player), player);
	}

	protected void displayStartMessage(Player player)
	{
		if (player == null || !player.isOnline())
		{
			return;
		}

		UtilTextMiddle.display(C.cYellow + "New Objective", getName(player), player);

		_objective.displayChatMessages(player);
	}

	public void setDisplayStartMessage(boolean displayStartMessage)
	{
		_displayStartMessage = displayStartMessage;
	}

	public void setDisplayFinishMessage(boolean displayFinishMessage)
	{
		_displayFinishMessage = displayFinishMessage;
	}

	public void setStartMessageDelay(int startMessageDelay)
	{
		_startMessageDelay = startMessageDelay;
	}

	public void setFinishMessageDelay(int finishMessageDelay)
	{
		_finishMessageDelay = finishMessageDelay;
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (!event.getType().equals(UpdateType.SEC_05)) return;
		if (_fireworkLocations == null) return;

		for (UUID id : getActivePlayers())
		{
			if (Bukkit.getPlayer(id) == null) continue;
			List<Location> locations = getObjective().getPlugin().getRegion(Bukkit.getPlayer(id)).getLocationMap().getSpongeLocations(_fireworkLocations);
			if (locations == null) continue;
			for (Location loc : locations)
			{
				UtilFirework.playFirework(loc, FireworkEffect.Type.BURST, Color.AQUA, true, true);
			}
		}
	}

	@EventHandler
	public void activeCleaner(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		for (UUID uuid : _removeList)
		{
			_active.remove(uuid);
		}
		
		_removeList.clear();
	}
}
