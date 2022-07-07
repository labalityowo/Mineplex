package mineplex.core.boosters;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.authlib.properties.PropertyMap;
import mineplex.core.common.api.ApiEndpoint;
import mineplex.core.common.api.ApiFieldNamingStrategy;
import mineplex.core.common.api.ApiHost;
import mineplex.core.common.api.ApiResponse;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Boosters interaction is handled through a web API. All data is represented as JSON and then parsed using gson.
 *
 * @author Shaun Bennett
 */
public class BoosterRepository extends ApiEndpoint
{
	public BoosterRepository()
	{
		super(ApiHost.getAmplifierService(), "/booster", new GsonBuilder().setFieldNamingStrategy(new ApiFieldNamingStrategy())
//				.registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer())
				.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").create());
	}

	public Map<String, List<Booster>> getBoosters()
	{
		return getWebCall().get("/", new TypeToken<Map<String, List<Booster>>>(){}.getType());
	}

	public List<Booster> getBoosters(String serverGroup)
	{
		return Arrays.asList(getWebCall().get("/" + serverGroup, Booster[].class));
	}

	public BoosterApiResponse addBooster(String serverGroup, String playerName, UUID uuid, int accountId, int duration)
	{
		JsonObject body = new JsonObject();
		body.addProperty("playerName", playerName);
		body.addProperty("uuid", uuid.toString());
		body.addProperty("accountId", accountId);
		body.addProperty("duration", duration);
//		body.add("propertyMap", getGson().toJsonTree(propertyMap));

		return getWebCall().post("/" + serverGroup, BoosterApiResponse.class, body);
	}
}
