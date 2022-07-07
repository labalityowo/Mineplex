package nautilus.game.arcade.game.games.moba.buff;

import mineplex.core.common.util.UtilServer;
import nautilus.game.arcade.game.games.moba.Moba;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;

public abstract class Buff<T extends LivingEntity> implements Listener
{

	protected final Moba _host;
	protected final BuffManager _manager;
	protected final T _entity;
	protected final long _duration;

	private long _start;

	public Buff(Moba host, T entity, long duration)
	{
		_host = host;
		_manager = host.getBuffManager();
		_entity = entity;
		_duration = duration;
	}

	public abstract void onApply();

	public abstract void onExpire();

	final void apply()
	{
		_start = System.currentTimeMillis();
		UtilServer.RegisterEvents(this);
		onApply();
	}

	final void expire()
	{
		UtilServer.Unregister(this);
		onExpire();
	}

	public final T getEntity()
	{
		return _entity;
	}

	public final long getDuration()
	{
		return _duration;
	}

	public final long getStart()
	{
		return _start;
	}
}
