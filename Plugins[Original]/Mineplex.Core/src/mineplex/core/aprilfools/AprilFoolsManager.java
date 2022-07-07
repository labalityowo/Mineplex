package mineplex.core.aprilfools;

import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.google.common.collect.ImmutableMap;

import mineplex.core.MiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.aprilfools.command.PirateSongCommand;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.twofactor.TwoFactorAuth;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class AprilFoolsManager extends MiniPlugin
{
	/**
	 * Manually can be toggled to enable or disable the April Fools activities.
	 */
	private static final boolean ENABLED = false;

	private static final Map<String, String> CHAT_REPLACEMENTS = ImmutableMap.<String, String>builder()
			.put("you", "ye")
			.put("yes", "aye")
			.put("hello", "ahoy")
			.put("hey", "ahoy")
			.put("hi", "ahoy")
			.put("friend", "matey")
			.put("friends", "mateys")
			.put("chest", "booty")
			.put("chests", "booty")
			.put("treasure", "booty")
			.put("shards", "riches")
			.put("sword", "cutlass")
			.put("my", "me")
			.put("gold", "dubloon")
			.put("dog", "seadog")
			.put("die", "walk the plank")
			.put("kill", "keelhaul")
			.put("boat", "ship")
			.put("drink", "grog")
			.put("water", "grog")
			.put("dirt", "flith")
			.put("flag", "jolly roger")
			.put("am", "be")
			.put("are", "be")
			.put("your", "yer")
			.put("girl", "lass")
			.put("woman", "lass")
			.put("hell", "davy jones' locker")
			.put("nether", "davy jones' locker")
			.put("of", "o'")
			.put("reward", "bounty")
			.put("prize", "bounty")
			.put("shoot", "fire in the hole")
			.put("clumsy", "landlubber")
			.put("clean", "swab")
			.put("look", "avast ye")
			.put("omg", "shiver my timbers")
			.put("wood", "timber")
			.put("trash", "poop deck")
			.put("noob", "shark bait")
			.put("hack", "scurvy")
			.put("hacks", "scurvy")
			.put("hax", "scurvy")
			.put("haks", "scurvy")
			.put("hacker", "scurvy")
			.put("owner", "captain")
			.put("leader", "captain")
			.put("lt", "captain")
			.put("dev", "firstmate")
			.put("developer", "firstmate")
			.put("admin", "firstmate")
			.build();

	public enum Perm implements Permission
	{
		PIRATE_SONG_COMMAND,
	}

	private static AprilFoolsManager _instance;
	private final AprilFoolsRepository _repository;
	private final TwoFactorAuth _twoFA;

	private boolean _enabled;
	private boolean _enabledTitle;

	public AprilFoolsManager()
	{
		super("April Fools");

		_instance = this;
		_repository = new AprilFoolsRepository();
		_twoFA = require(TwoFactorAuth.class);
		setEnabled(true);
		_enabledTitle = UtilServer.isHubServer() || UtilServer.isTestServer();
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.ADMIN.setPermission(Perm.PIRATE_SONG_COMMAND, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new PirateSongCommand(this));
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void chat(AsyncPlayerChatEvent event)
	{
		if (!_enabled || _twoFA.isAuthenticating(event.getPlayer()))
		{
			return;
		}

		String message = event.getMessage();
		String pirateMessage = "";

		for (String word : message.split(" "))
		{
			String wordLower = word.toLowerCase();
			wordLower = wordLower.replaceAll("[^a-zA-Z0-9]", "");

			if (CHAT_REPLACEMENTS.containsKey(wordLower))
			{
				String pirateWord = CHAT_REPLACEMENTS.get(wordLower);
				char[] chars = pirateWord.toCharArray();

				if (Character.isUpperCase(word.charAt(0)))
				{
					chars[0] = Character.toUpperCase(chars[0]);
				}

				pirateWord = String.valueOf(chars);
				pirateMessage += pirateWord + " ";
			}
			else
			{
				if (wordLower.length() > 1 && wordLower.charAt(wordLower.length() - 1) == 'g')
				{
					int g = word.lastIndexOf('g');
					char[] chars = word.toCharArray();

					chars[g] = '\'';

					word = String.valueOf(chars);
				}

				pirateMessage += word + " ";
			}
		}

		double random = Math.random();

		if (random > 0.85)
		{
			pirateMessage += "matey!";
		}
		else if (random > 0.7)
		{
			String ar = "ar";

			for (int i = 0; i < 10; i++)
			{
				if (Math.random() > 0.75)
				{
					break;
				}

				ar += "r";
			}

			pirateMessage += "a" + ar + "gh";
		}
		else if (random > 0.55)
		{
			pirateMessage += "scallywag";
		}

		event.setMessage(pirateMessage.trim());
	}

	@EventHandler
	public void updateRandomMessage(UpdateEvent event)
	{
		if (!_enabled || !_enabledTitle || event.getType() != UpdateType.SLOW)
		{
			return;
		}

		String message = null;
		double random = Math.random();

		if (random > 0.99)
		{
			message = "Aye Aye Captain!";
		}
		else if (random > 0.98)
		{
			message = "Arggggggh";
		}
		else if (random > 0.97)
		{
			message = "Mateyy!";
		}
		else if (random > 0.96)
		{
			message = "Shiver me timbers!";
		}

		if (message == null)
		{
			return;
		}

		UtilTextMiddle.display("", message, 10, 40, 10);
	}

	public AprilFoolsRepository getRepository()
	{
		return _repository;
	}

	public void setEnabled(boolean enabled)
	{
		_enabled = ENABLED && enabled;
	}

	public boolean isEnabled()
	{
		return _enabled;
	}

	public static AprilFoolsManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new AprilFoolsManager();
		}

		return _instance;
	}
}