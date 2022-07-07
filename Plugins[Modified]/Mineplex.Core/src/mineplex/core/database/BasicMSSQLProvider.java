package mineplex.core.database;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.scheduler.BukkitTask;

import mineplex.core.common.Constants;
import mineplex.core.common.util.UtilServer;
import mineplex.core.server.remotecall.JsonWebCall;
import mineplex.core.thread.ThreadPool;
import mineplex.core.updater.UpdateType;
import mineplex.core.utils.UtilScheduler;
import mineplex.serverdata.database.DatabaseRunnable;

@Deprecated
public class BasicMSSQLProvider implements MSSQLProvider
{
	private final String _webAddress = Constants.WEB_ADDRESS;

	// Queue for failed processes
	private final Object QUEUE_LOCK = new Object();
	private Set<DatabaseRunnable> _failedQueue = new HashSet<>();

	private final BukkitTask _task;
	private volatile boolean _shutdown = false;

	public BasicMSSQLProvider()
	{
		_task = UtilScheduler.runEvery(UpdateType.MIN_01, this::processDatabaseQueue);
	}

	public <T> T handleSyncMSSQLCall(String uri, Object param, Type responseType)
	{
		return new JsonWebCall(_webAddress + uri).Execute(responseType, param);
	}

	public String handleSyncMSSQLCallStream(String uri, Object param)
	{
		return new JsonWebCall(_webAddress + uri).ExecuteReturnStream(param);
	}

	public <T> void handleMSSQLCall(String uri, String error, Object param, Class<T> responseType, Consumer<T> consumer)
	{
		handleDatabaseCall(new DatabaseRunnable(() ->
		{
			new JsonWebCall(_webAddress + uri).Execute(responseType, consumer::accept, param);
		}, error));
	}

	public <T> void handleMSSQLCall(String uri, Object param, Class<T> responseType, Consumer<T> consumer)
	{
		handleDatabaseCall(new DatabaseRunnable(() ->
		{
			new JsonWebCall(_webAddress + uri).Execute(responseType, consumer::accept, param);
		}, "Handling MSSQL Call " + uri));
	}

	public <T> void handleMSSQLCall(String uri, Object param, Type responseType, Consumer<T> consumer)
	{
		handleDatabaseCall(new DatabaseRunnable(() ->
		{
			T t = new JsonWebCall(_webAddress + uri).Execute(responseType, param);
			consumer.accept(t);
		}, "Handling MSSQL Call " + uri));
	}

	public <T> void handleMSSQLCall(String uri, Object param)
	{
		handleDatabaseCall(new DatabaseRunnable(() ->
		{
			new JsonWebCall(_webAddress + uri).Execute(param);
		}, "Handling MSSQL Call " + uri));
	}

	@Override
	public void deregister()
	{
		_shutdown = true;
	}

	private void handleDatabaseCall(DatabaseRunnable databaseRunnable)
	{
		ThreadPool.ASYNC.submit(() ->
		{
			try
			{
				databaseRunnable.run();
			}
			catch (Exception exception)
			{
				processFailedDatabaseCall(databaseRunnable, exception);
			}
		});
	}

	private void processFailedDatabaseCall(DatabaseRunnable databaseRunnable, Exception exception)
	{
		System.err.println(databaseRunnable.getErrorMessage());
		exception.printStackTrace();

		if (databaseRunnable.getFailedCounts() < 4)
		{
			databaseRunnable.incrementFailCount();

			synchronized (QUEUE_LOCK)
			{
				_failedQueue.add(databaseRunnable);
			}
		}
	}

	private void processDatabaseQueue()
	{
		Set<DatabaseRunnable> clone;

		synchronized (QUEUE_LOCK)
		{
			clone = new HashSet<>(_failedQueue);
			_failedQueue.clear();
		}

		clone.forEach(this::handleDatabaseCall);

		if (_shutdown && _failedQueue.isEmpty())
		{
			_task.cancel();
		}
	}
}
