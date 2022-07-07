package mineplex.core.chat;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.CoreClient;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.chat.command.BroadcastCommand;
import mineplex.core.chat.command.ChatSlowCommand;
import mineplex.core.chat.command.HelpCommand;
import mineplex.core.chat.command.ListEmotesCommand;
import mineplex.core.chat.command.SilenceCommand;
import mineplex.core.chat.event.FormatPlayerChatEvent;
import mineplex.core.chat.format.ChatFormatComponent;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.UtilTime;
import mineplex.core.incognito.IncognitoManager;
import mineplex.core.preferences.Preference;
import mineplex.core.preferences.PreferencesManager;
import mineplex.core.preferences.UserPreferences;
import mineplex.core.recharge.Recharge;

@ReflectivelyCreateMiniPlugin
public class Chat extends MiniPlugin
{
	public enum Perm implements Permission
	{
		ALLOW_CAPS,
		ALLOW_COLOUR,
		BYPASS_COOLDOWN,
		BYPASS_SLOW,
		BYPASS_SILENCE,
		BYPASS_SIGNS,
		BYPASS_CHAT_FILTER,
		ALLOW_HACKUSATE,
		ALLOW_SIMILAR,
		SILENCE_COMMAND,
		SLOW_CHAT_COMMAND,
		BROADCAST_COMMAND,
		HELP_COMMAND,
		WHITE_CHAT,

		CHAT_EXTRA_COMMAND,
		MOD_CHAT_EXTRA_COMMAND,
		ADMIN_CHAT_EXTRA_COMMAND
	}

	private static final String STRIP_UNICODE_REGEX = "[^\\x00-\\x7F]";
	private static final String STRIP_HACKUSATION_REGEX = "[^A-Za-z ]";
	private static final List<String> HACKUSATIONS = Arrays.asList("hack", "hax", "hacker", "hacking", "cheat", "cheater", "cheating", "forcefield", "flyhack", "flyhacking", "autoclick", "aimbot");
	private static final int MAX_CAPS_PER_MSG = 30;
	public static final Map<String, String> EMOTES = new HashMap<>();

	static
	{
		EMOTES.put("ヽ༼ຈل͜ຈ༽ﾉ", ":donger:");
		EMOTES.put("¯\\_(ツ)_/¯", ":shrug:");
		EMOTES.put("༼ つ ◕_◕ ༽つ", ":ameno:");
		EMOTES.put("(╯°□°）╯︵ ┻━┻", ":tableflip:");
		EMOTES.put("┬─┬ノ(ಠ_ಠノ)", ":tablesit:");
		EMOTES.put("(౮⦦ʖ౮)", ":lenny:");
		EMOTES.put("ಠ_ಠ", ":disapproval:");
		EMOTES.put("(☞ﾟヮﾟ)☞", ":same:");
		EMOTES.put("ლ(ಥ Д ಥ )ლ", ":why:");
		EMOTES.put("(≖_≖)", ":squint:");
	}

	// Cleanspeak
	private static final String FILTER_URL = "https://chat.mineplex.com:8003/content/item/moderate";
	private static final String APP_ID = "34018d65-466d-4a91-8e92-29ca49f022c4";
	private static final String API_KEY = "oUywMpwZcIzZO5AWnfDx";

	private final IncognitoManager _incognitoManager;
	private final CoreClientManager _clientManager;
	private final PreferencesManager _preferencesManager;

	private int _chatSlowCooldown = 0;
	private long _silenceLength;
	private BukkitTask _silenceTask;

	private Map<UUID, MessageData> _playerLastMessage = new HashMap<>();
	private final List<ChatFormatComponent> _formatComponents = new ArrayList<>();

	private Chat()
	{
		super("Chat");

		_incognitoManager = require(IncognitoManager.class);
		_clientManager = require(CoreClientManager.class);
		_preferencesManager = require(PreferencesManager.class);

		new SpamHandler();

		try
		{
			trustCert();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		generatePermissions();
	}

	private void generatePermissions()
	{
		PermissionGroup.ADMIN.setPermission(Perm.ALLOW_CAPS, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.ALLOW_COLOUR, true, true);
		PermissionGroup.MOD.setPermission(Perm.BYPASS_COOLDOWN, true, true);
		PermissionGroup.TRAINEE.setPermission(Perm.BYPASS_SLOW, true, true);
		PermissionGroup.MOD.setPermission(Perm.BYPASS_SILENCE, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.BYPASS_SIGNS, true, true);
		PermissionGroup.TRAINEE.setPermission(Perm.ALLOW_HACKUSATE, true, true);
		PermissionGroup.MOD.setPermission(Perm.ALLOW_SIMILAR, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.SILENCE_COMMAND, true, true);
		PermissionGroup.SRMOD.setPermission(Perm.SLOW_CHAT_COMMAND, true, true);
		PermissionGroup.MOD.setPermission(Perm.BROADCAST_COMMAND, true, true);
		PermissionGroup.PLAYER.setPermission(Perm.HELP_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.BYPASS_CHAT_FILTER, true, true);

		PermissionGroup.TITAN.setPermission(Perm.CHAT_EXTRA_COMMAND, true, true);
		PermissionGroup.MOD.setPermission(Perm.MOD_CHAT_EXTRA_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.ADMIN_CHAT_EXTRA_COMMAND, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new SilenceCommand(this));
		addCommand(new BroadcastCommand(this));
		addCommand(new ChatSlowCommand(this));
		addCommand(new HelpCommand(this));
		addCommand(new ListEmotesCommand(this));
	}

	public void setFormatComponents(ChatFormatComponent... components)
	{
		_formatComponents.clear();
		_formatComponents.addAll(Arrays.asList(components));
	}

	public CoreClientManager getClientManager()
	{
		return _clientManager;
	}

	public PreferencesManager getPreferencesManager()
	{
		return _preferencesManager;
	}

	public void setChatSlow(int seconds, boolean inform)
	{
		seconds = Math.max(seconds, 0);

		_chatSlowCooldown = seconds;

		if (!inform)
		{
			return;
		}

		if (seconds == 0)
		{
			UtilServer.broadcast(F.main(getName(), "Chat slow has been " + C.cRed + "disabled" + C.mBody + "."));
		}
		else
		{
			UtilServer.broadcast(F.main(getName(), "Chat slow has been " + C.cGreen + "enabled" + C.mBody + " with a cooldown of " + F.time(seconds + " seconds") + "."));
		}
	}

	public long getChatSilence()
	{
		return _silenceLength;
	}

	public void setChatSilence(long duration, boolean inform)
	{
		// Cancel existing silence which has a pending re-enable
		if (_silenceTask != null)
		{
			_silenceTask.cancel();
			_silenceTask = null;
		}

		long durationTicks = (duration / 1000) * 20L;

		if (duration > 0)
		{
			// If duration is positive, convert it to milliseconds and store the end time
			_silenceLength = duration + System.currentTimeMillis();

			// Only set the task to disable the silence if it is a positive number of ticks
			_silenceTask = UtilServer.runSyncLater(this::endChatSilence, durationTicks);
		}
		else
		{
			// If duration is 0/neg, store it directly
			_silenceLength = duration;
		}

		// Return if the player is not to be informed of this silencing
		if (!inform)
		{
			return;
		}

		// Permanent silence if negative
		if (duration < 0)
		{
			UtilServer.broadcast(F.main("Chat", "Chat has been silenced for " + F.time("Permanent") + "."));
		}
		// Disabled if 0
		else if (duration == 0)
		{
			UtilServer.broadcast(F.main("Chat", "Chat is no longer silenced."));
		}
		// Otherwise it's just some amount of time...
		else
		{
			// Divide duration by 20 before turning it into a time string so it's in seconds instead of ticks
			UtilServer.broadcast(F.main("Chat", "Chat has been silenced for " + F.time(UtilTime.MakeStr(duration, 1)) + "."));
		}
	}

	public void endChatSilence()
	{
		// Silence is not enabled
		if (_silenceLength == 0)
		{
			return;
		}

		setChatSilence(0, true);
	}

	public boolean isSilenced(Player player, boolean inform)
	{
		if (_silenceLength == 0)
		{
			return false;
		}

		if (_clientManager.Get(player).hasPermission(Perm.BYPASS_SILENCE))
		{
			return false;
		}

		if (inform)
		{
			if (_silenceLength == -1)
			{
				player.sendMessage(F.main(getName(), "Chat is silenced permanently."));
			}
			else
			{
				player.sendMessage(F.main(getName(), "Chat is silenced for " + F.time(UtilTime.MakeStr(_silenceLength - System.currentTimeMillis(), 1)) + "."));
			}
		}

		return true;
	}

	public boolean isSilenced(Player player)
	{
		return isSilenced(player, true);
	}

	public static String replaceUnicode(String original)
	{
		return original.replaceAll(STRIP_UNICODE_REGEX, "");
	}

	private boolean containsHackusation(String message)
	{
		String[] parts = message.toLowerCase().replaceAll(STRIP_HACKUSATION_REGEX, "").split(" ");

		for (String part : parts)
		{
			if (HACKUSATIONS.contains(part))
			{
				return true;
			}
		}

		return false;
	}

	private JSONObject buildMessageObject(String message)
	{
		JSONObject content = new JSONObject();

		content.put("content", message);
		content.put("type", "text");

		return content;
	}

	private JSONArray buildPartsArray(String... messages)
	{
		JSONArray parts = new JSONArray();

		for (String message : messages)
		{
			parts.add(buildMessageObject(message));
		}

		return parts;
	}

	private JSONObject buildRequestObject(Player player, String... messages)
	{
		JSONObject content = new JSONObject();

		content.put("applicationId", APP_ID);
		content.put("createInstant", System.currentTimeMillis());
		content.put("parts", buildPartsArray(messages));
		content.put("senderDisplayName", player.getPlayerListName());
		content.put("senderId", player.getUniqueId().toString());

		JSONObject message = new JSONObject();
		message.put("content", content);

		return message;
	}

	private String makeCleanspeakRequest(JSONObject message)
	{
		StringBuffer response = null;

		HttpsURLConnection connection = null;
		DataOutputStream outputStream = null;
		BufferedReader bufferedReader = null;
		InputStreamReader inputStream = null;

		try
		{
			URL url = new URL(FILTER_URL);

			connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.addRequestProperty("Authentication", API_KEY);

			String jsonString = message.toString();

			// Send post request
			connection.setDoOutput(true);
			outputStream = new DataOutputStream(connection.getOutputStream());
			outputStream.writeBytes(jsonString);
			outputStream.flush();
			outputStream.close();

			inputStream = new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8"));
			bufferedReader = new BufferedReader(inputStream);
			String inputLine;
			response = new StringBuffer();

			while ((inputLine = bufferedReader.readLine()) != null)
			{
				response.append(inputLine);
			}

			bufferedReader.close();
		}
		catch (Exception exception)
		{
			System.out.println("Error getting response from CleanSpeak : " + exception.getMessage());
		}
		finally
		{
			if (connection != null)
			{
				connection.disconnect();
			}

			if (outputStream != null)
			{
				try
				{
					outputStream.flush();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}

				try
				{
					outputStream.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}

			if (bufferedReader != null)
			{
				try
				{
					bufferedReader.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}

			if (inputStream != null)
			{
				try
				{
					inputStream.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}

		if (response != null)
		{
			return response.toString();
		}

		return null;
	}

	public static void trustCert() throws Exception
	{
		TrustManager[] trustAllCerts = new TrustManager[]
				{
						new X509TrustManager()
						{
							public java.security.cert.X509Certificate[] getAcceptedIssuers()
							{
								return null;
							}

							public void checkClientTrusted(X509Certificate[] certs, String authType)
							{
							}

							public void checkServerTrusted(X509Certificate[] certs, String authType)
							{
							}

						}
				};

		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = (hostname, session) -> true;

		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	}

	public List<String> getReplacements(JSONObject responseJson, String... originalMessages)
	{
		// Again, no content means no good
		if (!responseJson.containsKey("content"))
		{
			return Arrays.stream(originalMessages).collect(Collectors.toList());
		}

		List<String> replacements = new ArrayList<>();

		JSONObject content = (JSONObject) responseJson.get("content");

		// No parts means the message is clean, so it's all good
		if (!content.containsKey("parts"))
		{
			return Arrays.stream(originalMessages).collect(Collectors.toList());
		}

		// {"parts":[{"replacement":"the string to replace the entire part with"}]}
		JSONArray parts = (JSONArray) content.get("parts");

		// Iterate over all the original messages...
		// If the original message is empty, just add a blank and go to the next one
		// If it's not, add its part and increment the part counter.
		// This means that each non-space element moves the part counter forward one,
		// So spaces are properly perserved in order
		int partIndex = 0;
		for (String originalMessage : originalMessages)
		{
			if (originalMessage.trim().isEmpty())
			{
				replacements.add("");
				continue;
			}

			Object obj = parts.get(partIndex);

			// Increment index regardless of further checks
			partIndex++;

			JSONObject jsonObject = (JSONObject) obj;

			if (!jsonObject.containsKey("replacement"))
			{
				replacements.add(originalMessage);
				continue;
			}

			replacements.add(jsonObject.get("replacement").toString());
		}

		return replacements;
	}

	public List<String> filterMessages(Player player, boolean useBackup, String... messages)
	{
		if (_clientManager.Get(player).hasPermission(Perm.BYPASS_CHAT_FILTER)
				&& _preferencesManager.get(player).isActive(Preference.BYPASS_CHAT_FILTER))
		{
			return Arrays.stream(messages).collect(Collectors.toList());
		}

		// Trim and unicode-sanitize all of the inputs
		messages = Arrays.stream(messages).map(m -> replaceUnicode(m).trim()).toArray(String[]::new);

		// If all are empty anyways just return the original messages
		if (Arrays.stream(messages).allMatch(s -> s.trim().isEmpty()))
		{
			return Arrays.stream(messages).collect(Collectors.toList());
		}

		// Get the request body's json object
		// Remove empties in the request because getReplacements handles that
		JSONObject requestJson = buildRequestObject(player, Arrays.stream(messages).filter(s -> !s.isEmpty()).toArray(String[]::new));

		// Get cleanspeak's response
		String response = makeCleanspeakRequest(requestJson);

		// Some kind of error, apparently
		if (response == null)
		{
			System.out.println("[ERROR] Unable to filter chat message...thanks a lot CleanSpeak.");

			if (useBackup)
			{
				return BackupFilter.filterMessages(messages);
			}

			return Arrays.stream(messages).collect(Collectors.toList());
		}

		// Parse response body
		JSONObject responseJson = (JSONObject) JSONValue.parse(response);

		// If there is no content then we can't do anything with it
		if (!responseJson.containsKey("content"))
		{
			return Arrays.stream(messages).collect(Collectors.toList());
		}

		// Get replacements with original messages to preserve blank spaces
		return getReplacements(responseJson, messages);
	}

	public List<String> filterMessages(Player player, String... messages)
	{
		return filterMessages(player, false, messages);
	}

	public String filterMessage(Player player, boolean useBackup, String message)
	{
		return filterMessages(player, useBackup, message).get(0);
	}

	public String filterMessage(Player player, String message)
	{
		return filterMessages(player, message).get(0);
	}

	/**
	 * Check whether the given player is allowed to send a given message in chat.
	 * This method will also inform the player of why they are not allowed to, if inform is true.
	 * It will check last sent message, but will not handle updating it.
	 *
	 * @param sender  - The player who is sending the message
	 * @param message - The message the player is attempting to send
	 * @param inform  - Whether the player should be informed if they cannot chat
	 * @return - Whether this player can chat
	 */
	public boolean canChat(Player sender, String message, boolean inform)
	{
		CoreClient client = _clientManager.Get(sender);

		if (_incognitoManager != null && _incognitoManager.Get(sender).Status && !UtilServer.isTestServer())
		{
			if (inform)
			{
				sender.sendMessage(C.cYellow + "You can not chat while incognito.");
			}
			return false;
		}

		if (isSilenced(sender, inform))
		{
			return false;
		}

		if (!client.hasPermission(Perm.BYPASS_COOLDOWN)
				&& !Recharge.Instance.use(sender, "Chat Message", 400, false, false))
		{
			if (inform)
			{
				sender.sendMessage(F.main(getName(), "You are sending messages too fast."));
			}
			return false;
		}

		if (_playerLastMessage.containsKey(sender.getUniqueId()))
		{
			MessageData lastMessage = _playerLastMessage.get(sender.getUniqueId());

			long chatSlowLengthMillis = _chatSlowCooldown * 1000L;
			long timeSinceLastMillis = System.currentTimeMillis() - lastMessage.getTimeSent();

			if (timeSinceLastMillis < chatSlowLengthMillis && !client.hasPermission(Perm.BYPASS_SLOW))
			{
				sender.sendMessage(F.main("Chat", "Chat slow enabled. Please wait " + F.time(UtilTime.convertString(chatSlowLengthMillis - timeSinceLastMillis, 1, UtilTime.TimeUnit.FIT))));
				return false;
			}

			if (!client.hasPermission(Perm.ALLOW_SIMILAR)
					&& UtilText.isStringSimilar(message, lastMessage.getMessage(), 0.8f))
			{
				sender.sendMessage(F.main("Chat", "This message is too similar to your previous message."));
				return false;
			}
		}


		if (!client.hasPermission(Perm.ALLOW_HACKUSATE)
				&& containsHackusation(message))
		{
			if (inform)
			{
				sender.sendMessage(F.main("Chat",
						"Accusing players of cheating in-game is against the rules. "
								+ "If you think someone is cheating, please gather evidence and report it at "
								+ F.link("www.mineplex.com/reports")));
			}
			return false;
		}

		return true;
	}

	public boolean canChat(Player sender, String message)
	{
		return canChat(sender, message, true);
	}

	public String decapsify(String original)
	{
		int capsCount = 0;

		for (char c : original.toCharArray())
		{
			if (Character.isUpperCase(c))
			{
				capsCount++;
			}

			if (capsCount > MAX_CAPS_PER_MSG)
			{
				return original.toLowerCase();
			}
		}

		return original;
	}

	public String decapsifyIfNecessary(Player sender, String original)
	{
		if (!_clientManager.Get(sender).hasPermission(Chat.Perm.ALLOW_CAPS))
		{
			return decapsify(original);
		}

		return original;
	}

	public void updateLastMessage(Player sender, String message)
	{
		_playerLastMessage.put(sender.getUniqueId(), new MessageData(message));
	}

	@EventHandler
	public void preventMe(PlayerCommandPreprocessEvent event)
	{
		if (event.getPlayer().isOp())
		{
			return;
		}

		String lowerCase = event.getMessage().toLowerCase();

		if (lowerCase.startsWith("/me ") || lowerCase.startsWith("/bukkit") || lowerCase.startsWith("/minecraft"))
		{
			event.getPlayer().sendMessage(F.main(getName(), "Nope, not allowed!"));
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void lagTest(PlayerCommandPreprocessEvent event)
	{
		if (event.getMessage().equals("/lag") || event.getMessage().equals("/ping"))
		{
			event.getPlayer().sendMessage(F.main(getName(), "PONG!"));
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void handleChat(AsyncPlayerChatEvent event)
	{
		if (!event.isAsynchronous())
		{
			event.setCancelled(true);
			return;
		}

		Player sender = event.getPlayer();
		String message = event.getMessage();

		CoreClient client = _clientManager.Get(sender);
		UserPreferences preferences = _preferencesManager.get(sender);
		ChatChannel channel = ChatChannel.PUBLIC;

		for (ChatChannel other : ChatChannel.values())
		{
			if (other.getPrefix() != null && message.startsWith(other.getPrefix()))
			{
				channel = other;
				message = message.substring(1);
				break;
			}
		}

		if (channel.isModerated() && !canChat(sender, message))
		{
			event.setCancelled(true);
			return;
		}

		event.setMessage(message);

		if (event.isCancelled() || message.isEmpty())
		{
			event.setCancelled(true);
			return;
		}

		FormatPlayerChatEvent customEvent = new FormatPlayerChatEvent(event, channel, _formatComponents);
		UtilServer.CallEvent(customEvent);

		if (customEvent.isCancelled() || customEvent.getRecipients().isEmpty())
		{
			event.setCancelled(true);
			return;
		}

		message = customEvent.getMessage();

		if (channel.isModerated())
		{
			event.getRecipients().removeIf(recipient -> !_preferencesManager.get(recipient).isActive(Preference.SHOW_CHAT));
		}

		if (message.isEmpty())
		{
			event.setCancelled(true);
			return;
		}

		if (client.hasPermission(Perm.CHAT_EXTRA_COMMAND))
		{
			ChatColor colour = null;

			if (preferences.isActive(Preference.COLOR_SUFFIXES))
			{
				colour = client.getRealOrDisguisedPrimaryGroup().getColor();
			}

			for (Entry<String, String> entry : EMOTES.entrySet())
			{
				message = message.replace(entry.getValue(), (colour == null ? "" : colour) + entry.getKey() + customEvent.getMessageColour());
			}
		}

		updateLastMessage(sender, message);
		event.setCancelled(true);

		if (client.hasPermission(Perm.ALLOW_COLOUR))
		{
			message = ChatColor.translateAlternateColorCodes('&', message);
		}

		final TextComponent formatted = new TextComponent("");
		List<ChatFormatComponent> components = customEvent.getFormatComponents();

		for (ChatFormatComponent component : components)
		{
			BaseComponent text = component.getText(sender);

			if (text == null || text.toPlainText().isEmpty())
			{
				continue;
			}

			formatted.addExtra(text);
			formatted.addExtra(" ");
		}

		TextComponent messageContent = new TextComponent(message);
		messageContent.setColor(customEvent.getMessageColour());
		formatted.addExtra(messageContent);

		event.getRecipients().forEach(player -> player.spigot().sendMessage(formatted));
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void filterMessage(FormatPlayerChatEvent event)
	{
		if (event.isFiltered())
		{
			String message = event.getMessage();

			message = decapsifyIfNecessary(event.getPlayer(), message);
			message = filterMessage(event.getPlayer(), true, message);

			event.setMessage(message);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onSignChange(SignChangeEvent event)
	{
		if (_clientManager.Get(event.getPlayer()).hasPermission(Perm.BYPASS_SIGNS))
		{
			return;
		}

		// Prevent silenced players from using signs
		if (isSilenced(event.getPlayer()))
		{
			event.setCancelled(true);
			return;
		}

		runAsync(() ->
		{
			List<String> lines = filterMessages(event.getPlayer(), event.getLines());

			runSync(() ->
			{
				Sign sign = (Sign) event.getBlock().getState();
				for (int i = 0; i < lines.size(); i++)
				{
					sign.setLine(i, lines.get(i));
				}
				sign.update();
			});
		});
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_playerLastMessage.remove(event.getPlayer().getUniqueId());
	}
}