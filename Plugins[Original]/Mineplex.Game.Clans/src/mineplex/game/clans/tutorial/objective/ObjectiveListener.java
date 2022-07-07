package mineplex.game.clans.tutorial.objective;

import org.bukkit.entity.Player;

public interface ObjectiveListener
{
	/**
	 * Called when a player starts an objective
	 */
	public void onObjectiveStart(Player player, Objective<?, ?> objective);

	/**
	 * Called when a player progresses in an objective
	 * For example, in an OrderedObjective this will be called when the player
	 * moves to the next ObjectiveGoal
	 */
	public void onObjectivePlayerUpdate(Player player, Objective<?, ?> objective);

	/**
	 * Called when a player finishes an objective
	 */
	public void onObjectiveFinish(Player player, Objective<?, ?> objective);
}
