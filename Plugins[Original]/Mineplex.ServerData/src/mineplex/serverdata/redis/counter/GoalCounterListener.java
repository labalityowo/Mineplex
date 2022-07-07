package mineplex.serverdata.redis.counter;

/**
 * @author Shaun Bennett
 */
public interface GoalCounterListener
{
	public void onMilestone(GoalCounter counter, int milestone);
}
