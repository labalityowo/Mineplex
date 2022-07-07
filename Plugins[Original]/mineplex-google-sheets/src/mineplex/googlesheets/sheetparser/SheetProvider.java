package mineplex.googlesheets.sheetparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;

import mineplex.googlesheets.SpreadsheetType;

/**
 *	A provider class designed with the functionality to get data from a <a href="https://docs.google.com/spreadsheets">Google Spreadsheet</a>.<br>
 *	Then proceed to return this data within the context of a {@link JSONObject}.
 */
public class SheetProvider
{

	/** Service name. */
	private static final String APPLICATION_NAME = "Mineplex Google Sheets";

	/** Directory to store user credentials for the service. */
	private static final File DATA_STORE_DIR = new File(".." + File.separatorChar + ".." + File.separatorChar + "update" + File.separatorChar + "files");

	/** Global instance of the {@link FileDataStoreFactory}. */
	private static FileDataStoreFactory DATA_STORE_FACTORY;

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	/** Global instance of the HTTP transport. */
	private static HttpTransport HTTP_TRANSPORT;

	/** List of all permissions that the service requires */
	private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);

	private Sheets _service;
	private Credential _credential;

	static
	{
		try
		{
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}

	public SheetProvider()
	{
		try
		{
			_credential = authorize();
			_service = getSheetsService();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Creates an authorized Credential object.
	 * 
	 * @return an authorized Credential object.
	 * @throws IOException If the Credential fails to authorise.
	 */
	private Credential authorize() throws IOException
	{
		// Load client secrets.
		InputStream in = new FileInputStream(DATA_STORE_DIR + File.separator + "client_secret.json");
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline").build();
		return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
	}

	/**
	 * Build and return an authorized Sheets API client service.
	 * 
	 * @return an authorized Sheets API client service
	 */
	private Sheets getSheetsService()
	{
		return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, _credential).setApplicationName(APPLICATION_NAME).build();
	}

	/**
	 * Returns the data comprised inside a Google Spreadsheet in the context of a {@link JSONObject}.
	 *
	 * @param spreadsheet The {@link SpreadsheetType} that you need to get the data from.
	 * @return A {@link JSONObject} containing all the data about a spreadsheet mapped by sheet -> rows -> columns.
	 */
	public JSONObject asJSONObject(SpreadsheetType spreadsheet)
	{
		JSONObject parent = new JSONObject();
		JSONArray array = new JSONArray();
		Map<String, List<List<Object>>> valuesMap = get(spreadsheet);

		if (valuesMap == null)
		{
			return null;
		}

		valuesMap.forEach((sheetName, lists) ->
		{
			List<List<Object>> values = valuesMap.get(sheetName);
			
			JSONObject object = new JSONObject();
			
			object.put("name", sheetName);
			object.put("values", values);
			
			array.put(object);
		});
	
		parent.put("data", array);
		return parent;
	}

	private Map<String, List<List<Object>>> get(SpreadsheetType spreadsheet)
	{
		try
		{
			Spreadsheet googleSpreadsheet = _service.spreadsheets().get(spreadsheet.getId()).execute();
			List<Sheet> sheets = googleSpreadsheet.getSheets();
			Map<String, List<List<Object>>> valuesMap = new HashMap<>(sheets.size());

			for (Sheet sheet : sheets)
			{
				String name = sheet.getProperties().getTitle();

				valuesMap.put(name, get(spreadsheet, name));
			}

			return valuesMap;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	private List<List<Object>> get(SpreadsheetType spreadsheet, String sheetName) throws IOException
	{
		return _service.spreadsheets().values().get(spreadsheet.getId(), sheetName).execute().getValues();
	}

}
