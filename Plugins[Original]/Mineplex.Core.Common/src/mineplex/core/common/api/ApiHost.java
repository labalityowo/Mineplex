package mineplex.core.common.api;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.YamlConfiguration;

public class ApiHost
{
	private static final String API_HOST_FILE = "api-config.dat";
	private static final Object LOCK = new Object();

	private static volatile boolean LOADED = false;

	private static final Map<String, ApiHost> API_HOST_MAP = new HashMap<>();

	public static ApiHost getAPIHost(String identifier)
	{
		if (!LOADED)
		{
			synchronized (LOCK)
			{
				if (!LOADED)
				{
					try
					{
						File configFile = new File(API_HOST_FILE);
						YamlConfiguration configuration = YamlConfiguration.loadConfiguration(configFile);

						for (String key : configuration.getKeys(false))
						{
							String ip = configuration.getConfigurationSection(key).getString("ip");
							// Use parseInt to catch non-ints instead of a 0
							int port = Integer.parseInt(configuration.getConfigurationSection(key).getString("port"));
							if (ip == null)
							{
								throw new NullPointerException();
							}

							API_HOST_MAP.put(key, new ApiHost(ip, port));
						}
					}
					catch (Throwable t)
					{
						t.printStackTrace();
					}
					finally
					{
						LOADED = true;
					}
				}
			}
		}

		return API_HOST_MAP.get(identifier);
	}

	public static ApiHost getAmplifierService()
	{
		return getAPIHost("AMPLIFIERS");
	}

	public static ApiHost getAntispamService()
	{
		return getAPIHost("ANTISPAM");
	}

	public static ApiHost getEnderchestService()
	{
		return getAPIHost("ENDERCHEST");
	}

	public static ApiHost getBanner()
	{
		return getAPIHost("BANNER");
	}

	private String _host;
	private int _port;

	private ApiHost(String host, int port)
	{
		_host = host;
		_port = port;
	}

	public String getHost()
	{
		return _host;
	}

	public int getPort()
	{
		return _port;
	}
}
