package mineplex.core.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * A collection of threads for different uses.
 */
public class ThreadPool
{

	// Async Thread
	public static ExecutorService ASYNC = Executors.newCachedThreadPool(
			new ThreadFactoryBuilder().setNameFormat("MiniPlugin Async %1$d").build()
	);

}
