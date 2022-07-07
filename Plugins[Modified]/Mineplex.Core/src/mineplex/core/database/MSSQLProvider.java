package mineplex.core.database;

import java.lang.reflect.Type;
import java.util.function.Consumer;

@Deprecated
public interface MSSQLProvider
{
	<T> T handleSyncMSSQLCall(String uri, Object param, Type responseType);

	String handleSyncMSSQLCallStream(String uri, Object param);

	<T> void handleMSSQLCall(String uri, String error, Object param, Class<T> responseType, Consumer<T> consumer);

	<T> void handleMSSQLCall(String uri, Object param, Class<T> responseType, Consumer<T> consumer);

	<T> void handleMSSQLCall(String uri, Object param, Type responseType, Consumer<T> consumer);

	<T> void handleMSSQLCall(String uri, Object param);

	void deregister();
}
