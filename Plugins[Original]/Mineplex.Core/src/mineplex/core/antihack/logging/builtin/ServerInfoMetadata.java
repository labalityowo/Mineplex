package mineplex.core.antihack.logging.builtin;

import java.util.UUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import mineplex.core.antihack.logging.AnticheatMetadata;
import mineplex.core.common.util.UtilServer;

public class ServerInfoMetadata extends AnticheatMetadata
{
	private static final String KEY_SERVER_NAME = "server-name";
	private static final String KEY_SERVER_REGION = "server-region";
	private static final String KEY_SERVER_GROUP = "server-group";

	@Override
	public String getId()
	{
		return "server-info";
	}

	@Override
	public JsonElement build(UUID player)
	{
		JsonObject info = new JsonObject();
		info.addProperty(KEY_SERVER_NAME, UtilServer.getServerName());
		info.addProperty(KEY_SERVER_REGION, UtilServer.getRegion().name());
		info.addProperty(KEY_SERVER_GROUP, UtilServer.getGroup());
		return info;
	}

	@Override
	public void remove(UUID player)
	{

	}
}