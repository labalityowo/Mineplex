package mineplex.game.clans.tutorial.objective;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.common.util.C;
import mineplex.game.clans.tutorial.Tutorial;

public abstract class SingleObjective<Plugin extends Tutorial> extends Objective<Plugin, ObjectiveData>
{
	private final ObjectiveData _nullData;

	public SingleObjective(Plugin plugin, JavaPlugin javaPlugin, String name, String description, String extraDescription)
	{
		super(plugin, javaPlugin, name, description, extraDescription);

		_nullData = new ObjectiveData();
	}

	public SingleObjective(Plugin plugin, JavaPlugin javaPlugin, String name, String description)
	{
		this(plugin, javaPlugin, name, description, null);
	}

	@Override
	protected ObjectiveData createDataObject(Player player)
	{
		return _nullData;
	}

	@Override
	protected void completeGoal(ObjectiveGoal<?> goal, Player player)
	{
		// Do Nothing
	}

	@Override
	public ObjectiveGoal<?> getLatestGoal(Player player)
	{
		return null;
	}

	@Override
	protected List<ObjectiveGoal<?>> getGoals()
	{
		return null;
	}

	@Override
	public void addScoreboardLines(Player player, List<String> lines)
	{
		/*
		if (contains(player))
		{
			lines.add(" " + C.cRed + getName(player));
		}
		*/
	}
}