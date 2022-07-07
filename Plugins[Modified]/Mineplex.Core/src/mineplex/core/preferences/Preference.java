package mineplex.core.preferences;

import java.util.List;
import java.util.Map;

import org.bukkit.Material;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import mineplex.core.account.permissions.Permission;

/**
 * All ID's are assigned by enum order.
 * <p>
 * Key:
 * Default value: The value this is set at if the user has not toggled it
 * Category: Determines which sub menu, along with permissions to view, this preference falls to
 * Icon: The material representation in the GUI
 * Name: The string shown to players in the menu and for toggling
 * Lore: A description of the preference
 */
public enum Preference implements Permission
{
	HUB_GAMES(true, PreferenceCategory.USER, Material.FIREBALL, "Hub Games"),
	SHOW_PLAYERS(true, PreferenceCategory.USER, Material.EYE_OF_ENDER, "Hub Player Visibility"),
	SHOW_CHAT(true, PreferenceCategory.USER, Material.PAPER, "Player Chat"),
	PRIVATE_MESSAGING(true, PreferenceCategory.USER, Material.MAP, "Private Messaging"),

	// TIM WHY DID YOU MAKE THE ID ORDINAL YOU DHAJDHJGHADJHGFAJHFSJDHAJDHSJHJA
	PARTY_REQUESTS(true, PreferenceCategory.SOCIAL, Material.SKULL_ITEM, "Party Requests"),

	INVISIBILITY(false, PreferenceCategory.EXCLUSIVE, Material.NETHER_STAR, "Hub Invisibility & Flight"),
	FORCE_FIELD(false, PreferenceCategory.EXCLUSIVE, Material.SLIME_BALL, "Hub Forcefield"),
	GLOBAL_GWEN_REPORTS(true, PreferenceCategory.EXCLUSIVE, Material.PAPER, "Global GWEN Reports"),
	SHOW_USER_REPORTS(false, PreferenceCategory.EXCLUSIVE, Material.BOOK, "User Reports"),
	IGNORE_VELOCITY(false, PreferenceCategory.EXCLUSIVE, Material.SADDLE, "Hub Ignore Velocity"),

	PENDING_FRIEND_REQUESTS(true, PreferenceCategory.SOCIAL, Material.RED_ROSE, "Show Pending Friend Requests"),
	FRIENDS_DISPLAY_INVENTORY_UI(true, PreferenceCategory.SOCIAL, Material.CHEST, "Display Friend GUI"),

	CLAN_TIPS(true, PreferenceCategory.MISC, Material.IRON_SWORD, "Show Clan Tips"),
	HUB_MUSIC(true, PreferenceCategory.MISC, Material.NOTE_BLOCK, "Hub Music"),

	AUTO_JOIN_NEXT_GAME(true, PreferenceCategory.GAME_PLAY, Material.DIAMOND_SWORD, "Auto Join Next Game", "Feel like playing again?", "Enable this, and when you're out", "a 15 second timer will start", "when it ends, it'll send you", "to another game!"),
	DISABLE_WARNING(true, PreferenceCategory.GAME_PLAY, Material.BARRIER, "Disable Automatic Warning", "Know what you're doing?", "Disable this to not receive", "a message warning you about Auto-Join"),
	COUNTDOWN_ON_CLICK(false, PreferenceCategory.GAME_PLAY, Material.WATCH, "Countdown to Join", "See that fancy text when you're out?", "If you click it, and this is enabled", "a 15 second time will countdown", "until you are sent to a new game"),

	COMMUNITY_INVITES(true, PreferenceCategory.SOCIAL, Material.BOOK, "Show Community Invites"),

	PARTY_DISPLAY_INVENTORY_UI(true, PreferenceCategory.SOCIAL, Material.CHEST, "Display Parties GUI"),

	RANDOM_MESSAGES(true, PreferenceCategory.USER, Material.COMMAND, "Send Random Messages", "Got nothing to say? We got you covered!"),
	
	INFORM_MUTED(false, PreferenceCategory.USER, Material.BARRIER, "Inform When Muted", "Inform people who message you if you are muted!"),

	GAME_TIPS(true, PreferenceCategory.GAME_PLAY, Material.BOOK, "Show Game Tips", "Enabling this will show tips", "about the game in the lobby", "and during the game."),

	UNLOCK_KITS(true, PreferenceCategory.EXCLUSIVE, Material.IRON_DOOR, "Unlock All Kits", "Enabling this will allow you", "to have access to every kit ", "in every game for free!"),
	AUTO_QUEUE(true, PreferenceCategory.USER, Material.EMERALD, "Teleport to Game Area", "Enabling this will teleport you to the", "game area instead of opening the server", "selector when choosing a game."),

	FRIEND_MESSAGES_ONLY(true, PreferenceCategory.SOCIAL, Material.SKULL_ITEM, "Friend Messages Only",
			"Enabling this will only allow", "friends to send you private", "messages.",
			"", "If you have private messages", "disabled, this preference", "has no effect."),

	BYPASS_CHAT_FILTER(false, PreferenceCategory.EXCLUSIVE, Material.GLASS, "Bypass Chat Filter", "Enabling this will allow you", "to bypass the chat filter everywhere.", "", "Proceed with caution."),

	COLOR_SUFFIXES(true, PreferenceCategory.USER, Material.WOOL, "Color Chat Suffixes", "Enabling this will color your", "chat suffixes like  ¯\\_(ツ)_/¯", "based on your rank."),

	;

	private static final Map<Integer, Preference> PREFERENCE_MAP = Maps.newHashMap();
	private static final Map<PreferenceCategory, List<Preference>> PREFERENCES_BY_CATEGORY = Maps.newHashMap();

	static
	{
		int id = 0;
		for (Preference preference : values())
		{
			preference._id = ++id;
			PREFERENCE_MAP.put(preference._id, preference);
			List<Preference> preferences = PREFERENCES_BY_CATEGORY.getOrDefault(preference._category, Lists.newArrayList());
			preferences.add(preference);
			PREFERENCES_BY_CATEGORY.put(preference._category, preferences);
		}

	}

	private int _id;
	private final boolean _default;
	private final PreferenceCategory _category;
	private final Material _icon;
	private final String _name;
	private final String[] _lore;

	Preference(boolean defaultSetting, PreferenceCategory category, Material icon, String name, String... lore)
	{
		_default = defaultSetting;
		_category = category;
		_icon = icon;
		_name = name;
		_lore = lore;
	}

	public String getName()
	{
		return _name;
	}

	public Material getIcon()
	{
		return _icon;
	}

	public int getId()
	{
		return _id;
	}

	public boolean getDefaultValue()
	{
		return _default;
	}

	public String[] getLore()
	{
		return _lore;
	}
	
	public PreferenceCategory getCategory()
	{
		return _category;
	}

	public static List<Preference> getByCategory(PreferenceCategory category)
	{
		return PREFERENCES_BY_CATEGORY.get(category);
	}

	public static Preference get(int id)
	{
		return PREFERENCE_MAP.get(id);
	}
}