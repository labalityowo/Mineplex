package mineplex.bungee.status;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.plugin.Plugin;

public class InternetStatus implements Runnable
{
	// Current internet connectivity status
	private static boolean _connected = true;
	public static boolean isConnected() { return _connected; }
	
	private Plugin _plugin;

	public InternetStatus(Plugin plugin)
	{
		_plugin = plugin;
		_plugin.getProxy().getScheduler().schedule(_plugin, this, 1L, 1L, TimeUnit.MINUTES);
		
		System.out.println("Initialized InternetStatus.");
	}

	@Override
	public void run()
	{
		_connected = isOnline();	// Update _connected flag.
	}

	private boolean isOnline()
	{
		return testUrl("www.google.com")
			|| testUrl("www.espn.com")
			|| testUrl("www.bing.com");
	}

	private boolean testUrl(String url)
	{
		boolean reachable = false;
		
		try (Socket socket = new Socket(url, 80))
		{
			reachable = true;
		}
		catch (Exception e)
		{
			// Meh i don't care
		}
		
		return reachable;
	}
}
