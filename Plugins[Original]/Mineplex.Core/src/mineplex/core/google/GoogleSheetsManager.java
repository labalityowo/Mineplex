package mineplex.core.google;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;

@ReflectivelyCreateMiniPlugin
public class GoogleSheetsManager extends MiniPlugin
{

	private static final File DATA_STORE_DIR = new File(".." + File.separatorChar + ".." + File.separatorChar + "update" + File.separatorChar + "files");

	private GoogleSheetsManager()
	{
		super("Google Sheets");
	}

	public Map<String, List<List<String>>> getSheetData(String name)
	{
		return getSheetData(new File(DATA_STORE_DIR + File.separator + name + ".json"));
	}

	public Map<String, List<List<String>>> getSheetData(File file)
	{
		if (!file.exists())
		{
			return null;
		}

		Map<String, List<List<String>>> valuesMap = new HashMap<>();

		try
		{
			JsonParser parser = new JsonParser();
			JsonElement data = parser.parse(new FileReader(file));
			JsonArray parent = data.getAsJsonObject().getAsJsonArray("data");
			
			for (int i = 0; i < parent.size(); i++)
			{
				JsonObject sheet = parent.get(i).getAsJsonObject();
				String name = sheet.get("name").getAsString();
				JsonArray values = sheet.getAsJsonArray("values");
				List<List<String>> valuesList = new ArrayList<>(values.size());
				
				for (int j = 0; j < values.size(); j++)
				{
					List<String> list = new ArrayList<>();

					for (JsonElement jsonElement : values.get(j).getAsJsonArray())
					{
						String value = jsonElement.getAsString();
						list.add(value);
					}
					
					valuesList.add(list);
				}

				valuesMap.put(name, valuesList);
			}
		}
		catch (FileNotFoundException e)
		{
		}

		return valuesMap;
	}
}
