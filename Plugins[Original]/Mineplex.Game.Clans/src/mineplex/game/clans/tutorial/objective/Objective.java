package mineplex.game.clans.tutorial.objective;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.common.util.C;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.game.clans.tutorial.Tutorial;
import mineplex.game.clans.tutorial.TutorialRegion;

/**
 * An Objective represents a set of goals that need to be completed to move on to the next Objective in the quest
 */
public abstract class Objective<Plugin extends Tutorial, Data extends ObjectiveData> implements Listener
{
	private Plugin _plugin;
	private JavaPlugin _javaPlugin;
	private String _name;
	private String _description;
	private String _extraDescription;
	private boolean _displayStartMessage;
	private int _startMessageDelay;
	private boolean _displayFinishMessage;
	private int _finishMessageDelay;

	private NautHashMap<UUID, Data> _active;
	private List<ObjectiveListener> _listeners;

	public Objective(Plugin plugin, JavaPlugin javaPlugin, String name, String description, String extraDescription)
	{
		_plugin = plugin;
		_javaPlugin = javaPlugin;
		_name = name;
		_description = description;
		_extraDescription = extraDescription;
		_displayStartMessage = false;
		_displayFinishMessage = false;
		_startMessageDelay = 60;
		_finishMessageDelay = 1;

		_active = new NautHashMap<>();
		_listeners = new LinkedList<>();

		javaPlugin.getServer().getPluginManager().registerEvents(this, javaPlugin);
	}

	public Objective(Plugin plugin, JavaPlugin javaPlugin, String name, String description)
	{
		this(plugin, javaPlugin, name, description, null);
	}

	public Plugin getPlugin()
	{
		return _plugin;
	}

	public JavaPlugin getJavaPlugin()
	{
		return _javaPlugin;
	}

	/**
	 * Get the name of this Objective
	 */
	public String getName(Player player)
	{
		return _name;
	}

	/**
	 * Get the description of this Objective
	 */
	public String getDescription(Player player)
	{
		return _description;
	}

	/**
	 * Get the extra description for this Objective
	 * Extra description should be any additional useful information that isn't required to complete the objective
	 */
	public String getExtraDescription(Player player)
	{
		return _extraDescription;
	}

	/**
	 * Add an ObjectiveListener to this Objective
	 * @param listener
	 */
	public void addListener(ObjectiveListener listener)
	{
		_listeners.add(listener);
	}

	/**
	 * Remove all ObjectiveListeners from this Objective
	 */
	public void clearListeners()
	{
		_listeners.clear();
	}

	protected abstract void customStart(Player player);

	/**
	 * Start this Objective for a player
	 * @param player
	 */
	public void start(Player player)
	{
		System.out.println(String.format("Tutorial> [%s] started objective [%s]", player.getName(), getName(player)));

		Data data = createDataObject(player);
		_active.put(player.getUniqueId(), data);

		_listeners.forEach(listener -> listener.onObjectiveStart(player, this));

		if (_displayStartMessage)
			Bukkit.getServer().getScheduler().runTaskLater(getJavaPlugin(), () -> showStartMessage(player), _startMessageDelay);

		customStart(player);
	}

	/**
	 * Leave this objective for a specific player
	 * This does not count as completing the object
	 * @param player
	 */
	public final void leave(Player player)
	{
		customLeave(player);

		_active.remove(player.getUniqueId());

		getGoals().forEach(goal -> goal.leave(player));
	}

	protected abstract void customLeave(Player player);

	/**
	 * Returns a new Data object for use in the active map
	 * @param player
	 * @return
	 */
	protected abstract Data createDataObject(Player player);

	/**
	 * Called by ObjectiveGoals, used to notify this Objective that a goal has been completed
	 * @param goal
	 * @param player
	 */
	protected abstract void completeGoal(ObjectiveGoal<?> goal, Player player);

	/**
	 * Called when a player is finished the tutorial
	 * @param player
	 * @param region
	 */
	public void clean(Player player, TutorialRegion region)
	{
		List<ObjectiveGoal<?>> goals = getGoals();
		if (goals != null)
			goals.forEach(goal -> goal.clean(player, region));
		
		_active.remove(player.getUniqueId());
	}

	/**
	 * Called when a player starts the tutorial
	 * @param player
	 * @param region
	 */
	public void setup(Player player, TutorialRegion region)
	{
		List<ObjectiveGoal<?>> goals = getGoals();
		if (goals != null)
			goals.forEach(goal -> goal.setup(player, region));
	}

	/**
	 * Returns a list of all the ObjectiveGoals used by this Objective
	 * Can return <code>null</code>
	 */
	protected abstract List<ObjectiveGoal<?>> getGoals();

	protected final void finish(Player player)
	{
		System.out.println(String.format("Tutorial> [%s] finished objective [%s]", player.getName(), getName(player)));

		_active.remove(player.getUniqueId());

		if (_displayFinishMessage)
			Bukkit.getServer().getScheduler().runTaskLater(getJavaPlugin(), () -> showFinishMessage(player), _finishMessageDelay);

		player.playSound(player.getEyeLocation(), Sound.LEVEL_UP, 1f, 1f);

		customFinish(player);

		_listeners.forEach(listener -> listener.onObjectiveFinish(player, this));
	}

	protected final void notifyUpdate(Player player)
	{
		_listeners.forEach(listener -> listener.onObjectivePlayerUpdate(player, this));
	}

	protected abstract void customFinish(Player player);

	public boolean contains(Player player)
	{
		return contains(player.getUniqueId());
	}

	public boolean contains(UUID uuid)
	{
		return _active.containsKey(uuid);
	}

	protected final List<UUID> getActive()
	{
		return new LinkedList<UUID>(_active.keySet());
	}

	protected final Player[] getActivePlayers()
	{
		Set<UUID> uuidSet = _active.keySet();
		Player[] players = new Player[uuidSet.size()];

		int index = 0;
		for (UUID uuid : uuidSet)
		{
			players[index] = UtilPlayer.searchExact(uuid);
			index++;
		}
		return players;
	}

	public Data getData(Player player)
	{
		return _active.get(player.getUniqueId());
	}

	/**
	 * Unregister all listeners associated with this Objective
	 */
	public final void unregisterAll()
	{
		HandlerList.unregisterAll(this);

		List<ObjectiveGoal<?>> goals = getGoals();
		if (goals != null) goals.forEach(HandlerList::unregisterAll);
	}

	public abstract void addScoreboardLines(Player player, List<String> lines);

	private void showStartMessage(Player player)
	{
		UtilTextMiddle.display(C.cAqua + "Next Tutorial Section", getName(player), 20, 60, 20,  player);
	}

	private void showFinishMessage(Player player)
	{
		UtilTextMiddle.display(C.cGreen + "Tutorial Section Completed", getName(player), 20, 60, 20, player);
	}

	public void displayChatMessages(Player player)
	{
		if (getPlugin().getTutorialSession(player) == null)
		{
			return;
		}
		
		for (int i = 0; i < 1; i++)
		{
			UtilPlayer.message(player, "");
		}

		ObjectiveGoal<?> goal = getLatestGoal(player);
		String name = goal == null ? getName(player) : goal.getName(player);
		String extra = getExtraDescription(player);
//		UtilPlayer.message(player, C.cGold + C.Strike + "---------------------------------------------");
		UtilPlayer.message(player, C.cPurpleB + name);
		if (extra != null)
		{
			UtilPlayer.message(player, "");
			UtilPlayer.message(player, C.cGray + " " + extra);
		}
		UtilPlayer.message(player, "");
		UtilPlayer.message(player, C.cGreen + getDescription(player));
//		UtilPlayer.message(player, C.cGold + C.Strike + "---------------------------------------------");
		getPlugin().getTutorialSession(player).setTextSeconds(0);
	}

	public void setDisplayStartMessage(boolean displayStartMessage)
	{
		_displayStartMessage = displayStartMessage;
	}

	public abstract ObjectiveGoal<?> getLatestGoal(Player player);

	public void setDisplayFinishMessage(boolean displayFinishMessage)
	{
		_displayFinishMessage = displayFinishMessage;
	}

	public int getStartMessageDelay()
	{
		return _startMessageDelay;
	}

	public void setStartMessageDelay(int startMessageDelay)
	{
		_startMessageDelay = startMessageDelay;
	}

	public int getFinishMessageDelay()
	{
		return _finishMessageDelay;
	}

	public void setFinishMessageDelay(int finishMessageDelay)
	{
		_finishMessageDelay = finishMessageDelay;
	}
}
