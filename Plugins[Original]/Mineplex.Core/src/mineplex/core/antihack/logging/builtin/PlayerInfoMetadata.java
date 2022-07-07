package mineplex.core.antihack.logging.builtin;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import mineplex.core.account.CoreClientManager;
import mineplex.core.antihack.logging.AnticheatMetadata;

import static mineplex.core.Managers.require;

public class PlayerInfoMetadata extends AnticheatMetadata
{
	private static final String KEY_UUID = "uuid";
	private static final String KEY_ACCOUNT_ID = "accountid";
	private static final String KEY_NAME = "name";

	private final CoreClientManager _clientManager = require(CoreClientManager.class);

	@Override
	public String getId()
	{
		return "player-info";
	}

	@Override
	public JsonElement build(UUID player)
	{
		JsonObject object = new JsonObject();
		object.addProperty(KEY_UUID, player.toString());

		Player bPlayer = Bukkit.getPlayer(player);
		if (bPlayer != null)
		{
			object.addProperty(KEY_NAME, bPlayer.getName());
			object.addProperty(KEY_ACCOUNT_ID, _clientManager.getAccountId(bPlayer));
		}
		
		return object;
	}

	@Override
	public void remove(UUID player)
	{

	}
}