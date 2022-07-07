package mineplex.core.delayedtask;

import org.bukkit.Location;

import mineplex.core.common.util.Callback;

public class Task
{
	private DelayedTaskClient _client;
	
	private Callback<DelayedTaskClient> _end;
	private Callback<DelayedTaskClient> _tick;
	private Callback<DelayedTaskClient> _cancel;
	
	private String _name;
	
	private long _startTime;
	private long _endTime;
	
	private Location _startPos;
	private boolean _allowMovement;
	
	public Task(DelayedTaskClient client, String task, Callback<DelayedTaskClient> end, Callback<DelayedTaskClient> tick, Callback<DelayedTaskClient> cancel,  long taskLength, boolean allowMovement)
	{
		_client = client;
		
		_name = task;
		_end = end;
		_tick = tick;
		_cancel = cancel;
		
		_startPos = client.getPlayer().getLocation();
		
		_allowMovement = allowMovement;
		
		_startTime = System.currentTimeMillis();
		_endTime = _startTime + taskLength;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public Callback<DelayedTaskClient> getEnd()
	{
		return _end;
	}
	
	public Callback<DelayedTaskClient> getTick()
	{
		return _tick;
	}
	
	public long getEndTime()
	{
		return _endTime;
	}
	
	public long getStartTime()
	{
		return _startTime;
	}

	public void tick()
	{
		_tick.run(_client);
		
		if (!_allowMovement && _startPos.distance(_client.getPlayer().getLocation()) > 0.3)
		{
			if (_cancel != null)
			{
				_cancel.run(_client);
			}
			
			_client.cleanup(_name);
			return;
		}
		
		if (System.currentTimeMillis() >= _endTime)
		{
			if (_end != null)
			{
				_end.run(_client);
			}
			
			_client.cleanup(_name);
		}
	}
}
