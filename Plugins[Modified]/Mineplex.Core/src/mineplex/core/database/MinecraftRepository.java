package mineplex.core.database;

import javax.sql.DataSource;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.bukkit.event.Listener;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import mineplex.core.common.util.UtilServer;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;

/**
 * Do not extend this class unless you are doing MSSQL calls (which you shouldn't be)
 *
 * @deprecated don't use mssql thx
 */
@Deprecated
public abstract class MinecraftRepository extends RepositoryBase implements Listener
{
	private static AtomicReference<MSSQLProvider> PROVIDER = new AtomicReference<>(new BasicMSSQLProvider());

	public static void setMSSQLProvider(MSSQLProvider provider)
	{
		MSSQLProvider oldProvider = PROVIDER.getAndSet(provider);
		oldProvider.deregister();
	}

	public MinecraftRepository(DataSource dataSource)
	{
		super(dataSource);

		UtilServer.RegisterEvents(this);
	}

	protected <T> T handleSyncMSSQLCall(String uri, Object param, Type responseType)
	{
		return PROVIDER.get().handleSyncMSSQLCall(uri, param, responseType);
	}

	protected String handleSyncMSSQLCallStream(String uri, Object param)
	{
		return PROVIDER.get().handleSyncMSSQLCallStream(uri, param);
	}

	protected <T> void handleMSSQLCall(String uri, String error, Object param, Class<T> responseType, Consumer<T> consumer)
	{
		PROVIDER.get().handleMSSQLCall(uri, error, param, responseType, consumer);
	}

	protected <T> void handleMSSQLCall(String uri, Object param, Class<T> responseType, Consumer<T> consumer)
	{
		PROVIDER.get().handleMSSQLCall(uri, param, responseType, consumer);
	}

	protected <T> void handleMSSQLCall(String uri, Object param, Type responseType, Consumer<T> consumer)
	{
		PROVIDER.get().handleMSSQLCall(uri, param, responseType, consumer);
	}

	protected <T> void handleAsyncMSSQLCall(String uri, Object param)
	{
		PROVIDER.get().handleMSSQLCall(uri, param);
	}
}
