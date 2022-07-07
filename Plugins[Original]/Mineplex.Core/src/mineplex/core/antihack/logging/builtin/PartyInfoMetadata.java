package mineplex.core.antihack.logging.builtin;

import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import mineplex.core.antihack.logging.AnticheatMetadata;
import mineplex.core.party.Party;
import mineplex.core.party.PartyManager;

import static mineplex.core.Managers.require;

public class PartyInfoMetadata extends AnticheatMetadata
{
	private static final String KEY_OWNER = "owner";
	private static final String KEY_MEMBERS = "members";

	@Override
	public String getId()
	{
		return "party-info";
	}

	@Override
	public JsonElement build(UUID player)
	{
		PartyManager pm = require(PartyManager.class);
		if (pm == null)
		{
			return JsonNull.INSTANCE;
		}
		Party party = pm.getPartyByPlayer(player);
		if (party != null)
		{
			JsonObject partyData = new JsonObject();
			partyData.addProperty(KEY_OWNER, party.getOwnerName());

			JsonArray members = new JsonArray();
			party.getMembers().forEach(m -> members.add(new JsonPrimitive(m.getName())));

			partyData.add(KEY_MEMBERS, members);

			return partyData;
		}
		else
		{
			return JsonNull.INSTANCE;
		}
	}

	@Override
	public void remove(UUID player)
	{

	}
}