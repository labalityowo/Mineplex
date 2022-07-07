package mineplex.googlesheets.sheetparser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONObject;

import mineplex.googlesheets.SpreadsheetType;

public class SheetProviderImpl
{

	private static final int SLEEP_TIME = 1000;
	private static final String DATA_STORE_DIR = ".." + File.separatorChar + ".." + File.separatorChar + "update" + File.separatorChar + "files";

	public SheetProviderImpl()
	{
		String sheetToRead = System.getProperty("sheet");
		SpreadsheetType[] types;

		if (sheetToRead == null)
		{
			types = SpreadsheetType.values();
		}
		else
		{
			types = new SpreadsheetType[]{SpreadsheetType.valueOf(sheetToRead)};
		}

		System.out.println("Loading Sheet Provider");
		SheetProvider provider = new SheetProvider();
		System.out.println("Loaded Sheet Provider");

		for (SpreadsheetType type : types)
		{
			System.out.println("Sleeping...");
			try
			{
				Thread.sleep(SLEEP_TIME);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			System.out.println("Getting data for " + type.name() + " (" + type.getId() + ")");

			JSONObject object = provider.asJSONObject(type);

			System.out.println("Done");
			System.out.println("Saving to file...");

			File dir = new File(DATA_STORE_DIR);
			File file = new File(dir + File.separator + type.name() + ".json");

			if (!dir.exists())
			{
				dir.mkdirs();
			}

			try
			{
				System.out.println("Deleting");
				file.delete();
				System.out.println("Creating a new file");
				file.createNewFile();

				FileWriter writer = new FileWriter(file);

				System.out.println("Writing");
				writer.write(object.toString());

				System.out.println("Closing...");
				writer.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
