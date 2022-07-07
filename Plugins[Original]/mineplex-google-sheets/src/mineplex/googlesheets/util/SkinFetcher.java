package mineplex.googlesheets.util;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class SkinFetcher
{

	private static final String SKIN_URL = "https://sessionserver.mojang.com/session/minecraft/profile/UUID?unsigned=false";

	public static String[] getSkinData(String uuid) throws Exception
	{
		String[] skinData = new String[2];
		JSONObject object = UtilJSON.getFromURL(SKIN_URL.replaceFirst("UUID", uuid));
		JSONArray properties = (JSONArray) object.get("properties");

		JSONObject innerObject = (JSONObject) properties.get(0);

		skinData[1] = (String) innerObject.get("signature");
		skinData[0] = (String) innerObject.get("value");

		return skinData;
	}

}
