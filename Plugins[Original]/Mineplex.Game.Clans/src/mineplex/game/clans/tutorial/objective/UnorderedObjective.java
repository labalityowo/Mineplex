package mineplex.game.clans.tutorial.objective;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.game.clans.tutorial.Tutorial;
import net.md_5.bungee.api.ChatColor;

public abstract class UnorderedObjective<Plugin extends Tutorial> extends Objective<Plugin, UnorderedObjectiveData>
{
	private List<ObjectiveGoal<?>> _goals;

	public UnorderedObjective(Plugin plugin, JavaPlugin javaPlugin, String name, String description, String extraDescription)
	{
		super(plugin, javaPlugin, name, description, extraDescription);

		_goals = new ArrayList<>();
	}

	public UnorderedObjective(Plugin plugin, JavaPlugin javaPlugin, String name, String description)
	{
		this(plugin, javaPlugin, name, description, null);
	}

	protected void addGoal(ObjectiveGoal<?> goal)
	{
		_goals.add(goal);

		getJavaPlugin().getServer().getPluginManager().registerEvents(goal, getJavaPlugin());
	}

	@Override
	protected UnorderedObjectiveData createDataObject(Player player)
	{
		return new UnorderedObjectiveData(_goals.size());
	}

	@Override
	protected void completeGoal(ObjectiveGoal<?> goal, Player player)
	{
		int index = _goals.indexOf(goal);

		UnorderedObjectiveData data = getData(player);
		data.setComplete(index);

		if (data.isComplete())
		{
			finish(player);
		}
		else
		{
			notifyUpdate(player);
		}
	}

	@Override
	public String getDescription(Player player)
	{
		UnorderedObjectiveData data = getData(player);
		if (data == null) return super.getDescription(player);
		int index = data.getFirstIncompleteIndex();
		return index == -1 ? super.getDescription(player) : _goals.get(index).getDescription(player);
	}

	@Override
	public String getExtraDescription(Player player)
	{
		UnorderedObjectiveData data = getData(player);
		if (data == null) return super.getExtraDescription(player);
		int index = data.getFirstIncompleteIndex();
		return index == -1 ? super.getExtraDescription(player) : _goals.get(index).getExtraDescription(player);
	}

	@Override
	public ObjectiveGoal<?> getLatestGoal(Player player)
	{
		UnorderedObjectiveData data = getData(player);
		if (data == null) return null;
		int index = data.getFirstIncompleteIndex();
		return index == -1 ? null : _goals.get(index);
	}

	@Override
	protected void customStart(Player player)
	{
		_goals.forEach(goal -> goal.start(player));
	}

	@Override
	protected void customLeave(Player player)
	{

	}

	@Override
	protected List<ObjectiveGoal<?>> getGoals()
	{
		return _goals;
	}

	@Override
	public void addScoreboardLines(Player player, List<String> lines)
	{
		if (contains(player))
		{
			UnorderedObjectiveData data = getData(player);
//			lines.add(" " + getName());

			for (int i = 0; i < _goals.size(); i++)
			{
				String prefix;
				if (data.isComplete(i))
					prefix = ChatColor.GREEN.toString();
				else
					prefix = ChatColor.RED.toString();

				lines.add(" " + prefix + _goals.get(i).getName(player));
			}
		}
	}
}