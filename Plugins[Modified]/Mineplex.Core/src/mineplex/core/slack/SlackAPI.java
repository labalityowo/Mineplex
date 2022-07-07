package mineplex.core.slack;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

import com.google.gson.JsonObject;

import org.bukkit.Bukkit;

import mineplex.core.thread.ThreadPool;

/**
 * An API for sending and handling Slack messages.
 */
public class SlackAPI
{
	// Default emoji.
	public static final String DEFAULT_ICON = ":mineplex:";

	// Singular instance.
	private static final SlackAPI _instance = new SlackAPI();

	// Don't allow instantiation elsewhere.
	private SlackAPI() {}

	/**
	 * <p>Sends a message to a Slack channel</p>
	 * <p>Will be run asynchronously if called from the main thread, else it will execute immediately on the current thread</p>
	 *
	 * @param team The team which contains the target channel.
	 * @param channel The target channel for the message.
	 * @param message The message to be displayed.
	 * @param customTitle Whether or not to use a custom title for the message.
	 *                    If <code>false</code> the default team title is used.
	 */
	public void sendMessage(SlackTeam team, String channel, SlackMessage message, boolean customTitle)
	{
		Runnable send = () ->
		{
			// Set message title.
			if (!customTitle)
			{
				message.setUsername(team.getTitle());
				message.setIcon(DEFAULT_ICON);
			}

			// Set message channel.
			JsonObject msg = message.toJson();
			msg.addProperty("channel", channel);

			// Run the call.
			runWebCall(team, msg);
		};

		if (Bukkit.isPrimaryThread())
		{
			ThreadPool.ASYNC.execute(send);
		}
		else
		{
			send.run();
		}
	}

	/**
	 * Runs a web call to a specified Slack incoming-hook.
	 *
	 * @param team The team to run the call on.
	 * @param call The call to be run.
	 */
	private String runWebCall(SlackTeam team, JsonObject call)
	{
		HttpsURLConnection connection = null;
		try
		{
			// Create connection.
			URL url = new URL(team.getURL());
			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setConnectTimeout(5000);
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			// Setup payload.
			String payload = "payload=" + URLEncoder.encode(call.toString(), "UTF-8");

			// Send request.
			DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
			dos.writeBytes(payload);
			dos.flush();
			dos.close();

			int responseCode = connection.getResponseCode();

			// Receive response.
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			String response = "";
			while ((line = rd.readLine()) != null)
			{
				response += line + "\n";
			}

			rd.close();
			return response.toString();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (connection != null)
			{
				// Terminate connection.
				connection.disconnect();
			}
		}

		return "500 Error";
	}

	/**
	 * Gets the singular instance of the Slack API.
	 *
	 * @return The {@link SlackAPI} instance.
	 */
	public static SlackAPI getInstance()
	{
		return _instance;
	}
}
