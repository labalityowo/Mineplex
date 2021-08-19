package mineplex.core.particleeffects;

import org.bukkit.scheduler.BukkitTask;

import mineplex.core.common.util.Callback;
import mineplex.core.common.util.UtilServer;

public abstract class Effect
{

	private int _ticksToRun = 20, _ticks = 0;
	private BukkitTask _task;
	protected long _period = 1;
	protected EffectLocation _effectLocation;
	private EffectLocation _targetLocation;
	private boolean _running = false;
	private Callback<Effect> _callback;

	public Effect(int ticks, EffectLocation effectLocation)
	{
		_ticksToRun = ticks;
		_effectLocation = effectLocation;
	}

	public Effect(int ticks, EffectLocation effectLocation, long delay)
	{
		_ticksToRun = ticks;
		_effectLocation = effectLocation;
		_period = delay;
	}

	public void start()
	{
		onStart();
		_running = true;
		_task = UtilServer.runSyncTimer(() ->
		{
			runEffect();
			update();
		}, 1, _period);
	}

	public void stop()
	{
		_running = false;
		_task.cancel();

		if (_callback != null)
		{
			_callback.run(this);
		}

		onStop();
	}

	public void onStart(){}

	public void onStop(){}

	private void update()
	{
		if (++_ticks == _ticksToRun)
		{
			_task.cancel();
		}
	}

	public boolean isRunning()
	{
		return _running;
	}

	public abstract void runEffect();

	public void setTargetLocation(EffectLocation effectLocation)
	{
		_targetLocation = effectLocation;
	}

	public EffectLocation getTargetLocation()
	{
		return _targetLocation;
	}

	public EffectLocation getEffectLocation()
	{
		return _effectLocation;
	}

	public void setCallback(Callback<Effect> callback)
	{
		_callback = callback;
	}

}
