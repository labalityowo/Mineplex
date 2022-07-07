package mineplex.game.clans.tutorial.objective;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.game.clans.tutorial.Tutorial;
import net.md_5.bungee.api.ChatColor;

public abstract class OrderedObjective<Plugin extends Tutorial> extends Objective<Plugin, OrderedObjectiveData>
{
	private List<ObjectiveGoal<?>> _goals;

	public OrderedObjective(Plugin plugin, JavaPlugin javaPlugin, String name, String description, String extraDescription)
	{
		super(plugin, javaPlugin, name, description, extraDescription);

		_goals = new ArrayList<>();
	}

	public OrderedObjective(Plugin plugin, JavaPlugin javaPlugin, String name, String description)
	{
		this(plugin, javaPlugin, name, description, null);
	}

	protected void addGoal(ObjectiveGoal<?> goal)
	{
		_goals.add(goal);

		getJavaPlugin().getServer().getPluginManager().registerEvents(goal, getJavaPlugin());
	}

	@Override
	protected OrderedObjectiveData createDataObject(Player player)
	{
		return new OrderedObjectiveData();
	}

	@Override
	protected void completeGoal(ObjectiveGoal<?> goal, Player player)
	{
		int index = _goals.indexOf(goal);

		OrderedObjectiveData data = getData(player);
		assert index == data.getIndex();

		if (data == null || data.getIndex() + 1 >= _goals.size())
		{
			finish(player);
		}
		else
		{
			setGoal(player, data.getIndex() + 1);
			notifyUpdate(player);
		}
	}

	@Override
	public String getDescription(Player player)
	{
		OrderedObjectiveData data = getData(player);
		int index = data == null ? 0 : data.getIndex();
		return _goals.get(index).getDescription(player);
	}

	@Override
	public String getExtraDescription(Player player)
	{
		OrderedObjectiveData data = getData(player);
		int index = data == null ? 0 : data.getIndex();
		return _goals.get(index).getExtraDescription(player);
	}

	@Override
	public ObjectiveGoal<?> getLatestGoal(Player player)
	{
		OrderedObjectiveData data = getData(player);
		int index = data == null ? 0 : data.getIndex();
		return _goals.get(index);
	}

	@Override
	protected void customStart(Player player)
	{
		setGoal(player, 0);
	}

	@Override
	protected void customLeave(Player player)
	{

	}

	private void setGoal(Player player, int index)
	{
		OrderedObjectiveData data = getData(player); // Should never be null!
		ObjectiveGoal<?> nextGoal = _goals.get(index);
		data.setIndex(index);
		nextGoal.start(player);
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
			OrderedObjectiveData data = getData(player);
//			lines.add(" " + getName());

			for (int i = 0; i < _goals.size(); i++)
			{
				String prefix;

				if (i > data.getIndex())
					prefix = ChatColor.RED.toString();
				else if (i == data.getIndex())
					prefix = ">" + ChatColor.YELLOW.toString();
				else
					prefix = ChatColor.GREEN.toString();

				lines.add(" " + prefix + _goals.get(i).getName(player));
			}
		}
	}
}