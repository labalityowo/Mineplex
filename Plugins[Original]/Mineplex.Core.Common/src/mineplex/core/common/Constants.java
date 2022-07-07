package mineplex.core.common;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.util.UUIDTypeAdapter;

public class Constants
{
	public static final String WEB_ADDRESS = "http://accounts.mineplex.com/";
	public static final String WEB_CONFIG_KEY = "webServer";

	public static Gson GSON;

	static
	{
		GsonBuilder builder = new GsonBuilder()
				.registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer())
				.registerTypeAdapter(UUID.class, new UUIDTypeAdapter());

		builder.registerTypeAdapter(GameProfile.class, new GameProfileSerializer());

		GSON = builder.create();
	}

	private static class GameProfileSerializer implements JsonSerializer<GameProfile>, JsonDeserializer<GameProfile>
	{
		public GameProfile deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException
		{
			if (!(json instanceof JsonObject))
				return new GameProfile(null, null);

			JsonObject object = (JsonObject) json;
			UUID id = object.has("id") ? (UUID) context.deserialize(object.get("id"), UUID.class) : null;
			String name = object.has("name") ? object.getAsJsonPrimitive("name").getAsString() : null;
			GameProfile profile = new GameProfile(id, name);

			if (object.has("properties"))
				profile.getProperties().putAll(context.deserialize(object.get("properties"), PropertyMap.class));

			return profile;
		}

		public JsonElement serialize(GameProfile profile, Type type, JsonSerializationContext context)
		{
			JsonObject result = new JsonObject();

			if (profile.getId() != null)
				result.add("id", context.serialize(profile.getId()));

			if (profile.getName() != null)
				result.addProperty("name", profile.getName());

			if (!profile.getProperties().isEmpty())
				result.add("properties", context.serialize(profile.getProperties()));

			return result;
		}
	}
}
