package mineplex.googlesheets.util;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * A utility to fetch UUIDs based on a player's name.
 * Adapted from UUIDFetcher inside Core.Common.
 */
public class UUIDFetcher
{

	private static final String PROFILE_URL = "https://api.mojang.com/profiles/minecraft";
	private static final JSONParser PARSER = new JSONParser();

	public static String getPlayerUUIDNoDashes(String name) throws Exception
	{
		String uuid = null;
		List<String> nameList = Collections.singletonList(name);

		HttpURLConnection connection = createConnection();
		String body = JSONArray.toJSONString(nameList);
		writeBody(connection, body);
		JSONArray array = (JSONArray) PARSER.parse(new InputStreamReader(connection.getInputStream()));

		for (Object profile : array)
		{
			JSONObject jsonProfile = (JSONObject) profile;
			uuid = (String) jsonProfile.get("id");
		}

		return uuid;
	}

	private static void writeBody(HttpURLConnection connection, String body) throws Exception
	{
		OutputStream stream = connection.getOutputStream();
		stream.write(body.getBytes());
		stream.flush();
		stream.close();
	}

	private static HttpURLConnection createConnection() throws Exception
	{
		URL url = new URL(PROFILE_URL);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setUseCaches(false);
		connection.setDoInput(true);
		connection.setDoOutput(true);
		return connection;
	}
}