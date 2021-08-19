package mineplex.core.common.animation;

import java.util.Collection;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

/**
 * Self sufficient animator to animate task with steps using local vector logic
 */

public abstract class Animator
{
	private final Plugin _plugin;

//	private TreeSet<AnimationPoint> _points = new TreeSet<>((a, b) -> Integer.compare(a.getTick(), b.getTick()));
	private Set<AnimationPoint> _points = new HashSet<>();
	private PriorityQueue<AnimationPoint> _queue = new PriorityQueue<>((a, b) -> Integer.compare(a.getTick(), b.getTick()));
	
	private AnimationPoint _prev;
	private AnimationPoint _next;
	private Location _baseLoc;
	private int _tick = -1;
	
	private boolean _repeat = false;

	private BukkitTask _task;

	public Animator(Plugin plugin)
	{
		_plugin = plugin;
	}
	
	public void addPoints(Collection<AnimationPoint> points)
	{
		for(AnimationPoint p : points) _points.add(p);
	}
	
	public void addPoint(AnimationPoint point) {
		_points.add(point);
	}
	
	public void removePoint(AnimationPoint point) {
		_points.remove(point);
	}
	
	/**
	 * @return Returns a cloned list of the animator points for this instance.
	 */
	public Set<AnimationPoint> getSet() {
		Set<AnimationPoint> set = new HashSet<>();
		set.addAll(_points);
		return set;
	}
	
	/**
	 * @return Returns the actual list of animator points used by this instance. As this is not a copy, editing this list will apply
	 * changes to the current instance.
	 */
	public Set<AnimationPoint> getSetRaw() {
		return _points;
	}

	/**
	 * Start the animation at the given location. If the animator is already running then this call will be silently ignored.
	 * @param loc Location the animation will start relative too. The vector poitns will be added to relative to this location.
	 */
	public void start(Location loc)
	{
		if(isRunning()) return;
		
		_queue.clear();
		_queue.addAll(_points);
		
		if(_queue.isEmpty()) return;
		
		_baseLoc = loc.clone();
		_next = _queue.peek();
		_prev = new AnimationPoint(0, _next.getMove().clone(), _next.getDirection().clone());
		
		_task = new BukkitRunnable() 
		{
			public void run() 
			{
				_tick++;
				
				if(_next.getTick() < _tick)
				{
					_queue.remove();
					_prev = _next;
					_next = _queue.peek();
				}
				
				if(_queue.isEmpty())
				{
					if(_repeat)
					{
						Location clone = _baseLoc.clone();
						stop();
						start(clone);
					}
					else
					{
						finish(_baseLoc);
						stop();
					}
					return;
				}
				
				Location prev = _baseLoc.clone().add(_prev.getMove());
				Location next = _baseLoc.clone().add(_next.getMove());
				prev.setDirection(_prev.getDirection());
				
				double diff = ((double)_tick-_prev.getTick())/(_next.getTick()-_prev.getTick());
				if(!Double.isFinite(diff)) diff = 0;
				prev.add(next.clone().subtract(prev).toVector().multiply(diff));
				
				Vector dirDiff = _next.getDirection().subtract(prev.getDirection());
				dirDiff.multiply(diff);
				prev.setDirection(prev.getDirection().add(dirDiff));
				
				tick(prev);
			}
		}.runTaskTimer(_plugin, 0, 1);
	}

	public void start(Entity entity)
	{
		if(isRunning()) return;

		_queue.clear();
		_queue.addAll(_points);

		if(_queue.isEmpty()) return;

		_baseLoc = entity.getLocation().clone();
		_next = _queue.peek();
		_prev = new AnimationPoint(0, _next.getMove().clone(), _next.getDirection().clone());

		_task = new BukkitRunnable()
		{
			public void run()
			{
				_tick++;

				if(_next.getTick() < _tick)
				{
					_queue.remove();
					_prev = _next;
					_next = _queue.peek();
				}

				if(_queue.isEmpty())
				{
					if(_repeat)
					{
						Location clone = _baseLoc.clone();
						stop();
						start(clone);
					}
					else
					{
						finish(_baseLoc);
						stop();
					}
					return;
				}

				Location prev = _baseLoc.clone().add(_prev.getMove());
				Location next = _baseLoc.clone().add(_next.getMove());
				prev.setDirection(_prev.getDirection());

				double diff = ((double)_tick-_prev.getTick())/(_next.getTick()-_prev.getTick());
				if(!Double.isFinite(diff)) diff = 0;
				prev.add(next.clone().subtract(prev).toVector().multiply(diff));

				Vector dirDiff = _next.getDirection().subtract(prev.getDirection());
				dirDiff.multiply(diff);
				prev.setDirection(prev.getDirection().add(dirDiff));

				tick(prev);
			}
		}.runTaskTimer(_plugin, 0, 1);
	}

	public boolean isRunning()
	{
		return _task != null;
	}

	public void stop()
	{
		if(!isRunning()) return;
		_task.cancel();
		_task = null;
		_tick = -1;
		_baseLoc = null;
	}
	
	/**
	 * @return Returns true if the animation should repeat.
	 * @see #setRepeat(boolean)
	 */
	public boolean isRepeat()
	{
		return _repeat;
	}
	
	/**
	 * If the last animation point does not make the animation end up at the exact same location as the start
	 * then it might lead to unexpected results as it will re-start the animation from the end of the animation.
	 */
	public void setRepeat(boolean repeat)
	{
		_repeat = repeat;
	}
	
	protected abstract void tick(Location loc); 
	

	protected abstract void finish(Location loc);

}
