package mineplex.core.account.permissions;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.ChatColor;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.MutableGraph;

import mineplex.core.common.util.F;

public enum PermissionGroup
{
	//PLAYER
	PLAYER("player", "", "", ChatColor.WHITE, -1, true),
	ULTRA("ultra", "Ultra", "A first step into the stories of the mist. \nOnly those brave enough may enter. \n\nThe first purchasable rank at Mineplex.com/shop", ChatColor.AQUA, 12, true, PermissionGroup.PLAYER),
	HERO("hero", "Hero", "There are many stories of a \nvaliant Hero who was brave enough to \ntame the most fearsome dragon in the land. \n\nThe second purchasable rank at Mineplex.com/shop", ChatColor.LIGHT_PURPLE, 13, true, PermissionGroup.ULTRA),
	LEGEND("legend", "Legend", "Years they have told stories of this rank, \nonly for the legends to be true. \n\nThe third purchasable rank at Mineplex.com/shop", ChatColor.GREEN, 14, true, PermissionGroup.HERO),
	TITAN("titan", "Titan", "Ancient myths spoke of a gigantic being \nwith immense power... \n\nThe fourth purchasable rank at Mineplex.com/shop", ChatColor.RED, 15, true, PermissionGroup.LEGEND),
	ETERNAL("eternal", "Eternal", "Fantastic and magical, no one \nexcept the time lords truly understand \nthe power of this rank.\n\nThe fifth purchasable rank at Mineplex.com/shop", ChatColor.DARK_AQUA, 18, true, PermissionGroup.TITAN),

	//CONTENT
	CONTENT("content", "", "", ChatColor.WHITE, -1, false, PermissionGroup.ETERNAL),
	TWITCH("twitch", "Twitch", "A Twitch streamer who often features \nMineplex in their streams.", ChatColor.DARK_PURPLE, 21, true, PermissionGroup.CONTENT),
	YT("yt", "YT", "A YouTuber who creates content for \nor related to Mineplex. \n\nThey have fewer subscribers than full YouTubers.", ChatColor.DARK_PURPLE, 20, true, PermissionGroup.CONTENT),
	YOUTUBE("youtube", "YouTube", "A YouTuber who creates content for \nor related to Mineplex.", ChatColor.RED, 22, true, PermissionGroup.CONTENT),

	BUILDER("builder", "Builder", "These creative staff members help \nbuild maps for your favorite games!", ChatColor.BLUE, 26, true, PermissionGroup.ETERNAL),
	MAPPER("mapper", "Mapper", "These senior staff members work closely with \nthe development and design teams to build new \nmaps for new and old content!", ChatColor.BLUE, 100, true, PermissionGroup.BUILDER),
	MAPLEAD("maplead", "MapLead", "Map Leaders are leaders of the Mineplex Build Team. \nThey oversee the creation of new maps and manage Builders.", ChatColor.BLUE, 25, true, PermissionGroup.MAPPER),
	TRAINEE("trainee", "Trainee", "Trainees are moderators-in-training. \nTheir duties include enforcing the rules and \nproviding help to anyone with questions or concerns. \n\nFor assistance, contact them using " + F.elem("/a <message>") + ".", ChatColor.GOLD, 24, true, PermissionGroup.MAPLEAD),
	MOD("mod", "Mod", "Moderators enforce rules and provide help to \nanyone with questions or concerns. \n\nFor assistance, contact them using " + F.elem("/a <message>") + ".", ChatColor.GOLD, 32, true, PermissionGroup.TRAINEE),
	SRMOD("srmod", "Sr.Mod", "Senior Moderators are members of a special \nSenior Moderator team where they have to fulfill specific tasks. \nJust like Moderators, you can always ask them for help. \n\nFor assistance, contact them using " + F.elem("/a <message>") + ".", ChatColor.GOLD, 44, true, PermissionGroup.MOD),
	SUPPORT("support", "Support", "Support agents handle tickets and \nprovide customer service.", ChatColor.BLUE, 47, true, PermissionGroup.SRMOD),
	ADMIN("admin", "Admin", "An Administrator’s role is to manage \ntheir respective Senior Moderator team \nand all moderators within it.", ChatColor.DARK_RED, 10, true, PermissionGroup.SUPPORT, PermissionGroup.CONTENT),
	DEV("dev", "Dev", "Developers work behind the scenes to \ncreate new games and features, and fix bugs to \ngive the best experience.", ChatColor.DARK_RED, 5, true, PermissionGroup.ADMIN),
	LT("lt", "Leader", "Leaders manage the operation of their respective team \nor projects. They usually operate on affairs within \nthe staff, development, or management team.", ChatColor.DARK_RED, 11, true, PermissionGroup.DEV),
	OWNER("owner", "Owner", "Owners are the core managers of Mineplex. \nEach owner manages a different aspect of the \nserver and ensures its efficient operation.", ChatColor.DARK_RED, 55, true, PermissionGroup.LT),

	//SUB-GROUPS
	QAT("qat", "", "Members of the Quality Assurance Testing team.", ChatColor.WHITE, -1, false),
	QA("qa", "", "Members of the Quality Assurance team.", ChatColor.WHITE, 50, false, PermissionGroup.QAT),
	QAM("qam", "", "Managers of the Quality Assurance team.", ChatColor.WHITE, 50, false, PermissionGroup.QA),
	CMOD("cmod", "", "Members of the Clans Management team.", ChatColor.WHITE, 45, false),
	MA("ma", "", "Members of the Mentoring Assistance team.", ChatColor.WHITE, -1, false),
	STM("stm", "", "Members of the Staff Management team.", ChatColor.WHITE, 115, false, PermissionGroup.MA),
	EVENTMOD("eventmod", "", "Members of the Event Management team.", ChatColor.WHITE, 109, false),
	CMA("cma", "", "Members of the Clans Management Assistance team.", ChatColor.WHITE, -1, false),
	RC("rc", "", "Members of the Rules Committee team.", ChatColor.WHITE, 35, false),
	FN("fn", "", "Members of the Forum Ninja team", ChatColor.WHITE, 48, false)

	;

	static
	{
		MutableGraph<PermissionGroup> builder = GraphBuilder.directed().build();

		// Add each group as a node, and add edges between parent (inherited) and child nodes
		Stream.of(PermissionGroup.values()).peek(builder::addNode).forEach(group ->
				group._parentGroups.forEach(parent -> builder.putEdge(parent, group)));

		_groupHierarchy = ImmutableGraph.copyOf(builder);
	}

	// We want a graph so we can walk the hierarchy downward and recalculate permissions when needed
	private static final ImmutableGraph<PermissionGroup> _groupHierarchy;
	private static final Object PERMS_LOCK = new Object();

	private final String _id, _display, _description;
	private final ChatColor _color;
	private final int _forumId;
	private final boolean _canBePrimary;
	private final Set<PermissionGroup> _parentGroups;

	private Map<Permission, Boolean> _specificPerms = new IdentityHashMap<>();
	private Map<Permission, Boolean> _inheritablePerms = new IdentityHashMap<>();

	private Map<Permission, Boolean> _bakedPerms = ImmutableMap.of();

	PermissionGroup(String identifier, String display, String description, ChatColor color, int forumId, boolean canBePrimary, PermissionGroup... parentGroups)
	{
		_id = Objects.requireNonNull(identifier, "Group identifier cannot be null").toLowerCase();
		_display = Objects.requireNonNull(display, "Group display cannot be null");
		_description = Objects.requireNonNull(description, "Group description cannot be null");
		_color = Objects.requireNonNull(color, "Group color cannot be null");
		_forumId = forumId;
		_canBePrimary = canBePrimary;
		_parentGroups = ImmutableSet.copyOf(Arrays.asList(parentGroups));
	}

	// Note the constraints on T: this ensures we'll have reference equality on permissions, so we can put them in `IdentityHashMap`s
	public <T extends Enum<T> & Permission> void setPermission(T permission, boolean inheritable, boolean value)
	{
		synchronized(PERMS_LOCK)
		{
			(inheritable ? _inheritablePerms : _specificPerms).put(permission, value); // Add new permission under the correct category
			(inheritable ? _specificPerms : _inheritablePerms).remove(permission); // Remove permission from the other category, if present

			bakePermissions();
		}
	}

	public void revokePermission(Permission permission)
	{
		synchronized(PERMS_LOCK)
		{
			_specificPerms.remove(permission);
			_inheritablePerms.remove(permission);

			bakePermissions();
		}
	}

	public boolean hasPermission(Permission permission)
	{
		synchronized(PERMS_LOCK)
		{
			return _bakedPerms.getOrDefault(permission, false);
		}
	}

	public boolean inheritsFrom(PermissionGroup group)
	{
		for (PermissionGroup parent : _parentGroups)
		{
			if (parent == group || parent.inheritsFrom(group))
			{
				return true;
			}
		}

		return this == group;
	}

	private void bakePermissions()
	{
		_bakedPerms = calculateInheritable();
		_bakedPerms.putAll(_specificPerms);
		_groupHierarchy.successors(this).forEach(PermissionGroup::bakePermissions);
	}

	// Calculate inheritable permissions from parent nodes and this node
	private Map<Permission, Boolean> calculateInheritable()
	{
		Map<Permission, Boolean> inheritable = _groupHierarchy.predecessors(this).stream() // For each predecessor,
				.map(PermissionGroup::calculateInheritable)                                      // calculate their inheritable permissions
				.flatMap(perms -> perms.entrySet().stream())                                     // and merge with logical OR
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Boolean::logicalOr, IdentityHashMap::new));

		inheritable.putAll(_inheritablePerms); // Add our own inheritable permissions
		return inheritable;
	}

	public String getDisplay(boolean color, boolean uppercase, boolean bold, boolean defaultIdentifier)
	{
		StringBuilder builder = new StringBuilder();
		
		if (uppercase)
		{
			builder.append((_display.isEmpty() && defaultIdentifier) ? _id.toUpperCase() :_display.toUpperCase());
		}
		else
		{
			builder.append((_display.isEmpty() && defaultIdentifier) ? (_id.substring(0, 1).toUpperCase() + _id.substring(1)) :_display);
		}
		
		if (bold)
		{
			builder.insert(0, ChatColor.BOLD);
		}
		if (color)
		{
			builder.insert(0, _color);
		}
		
		return builder.toString();
	}
	
	public String getDescription()
	{
		return _description;
	}
	
	public int getForumId()
	{
		return _forumId;
	}
	
	public ChatColor getColor()
	{
		return _color;
	}
	
	public boolean canBePrimary()
	{
		return _canBePrimary;
	}
	
	public static Optional<PermissionGroup> getGroup(String name)
	{
		return Stream.of(values()).filter(group -> group.name().equalsIgnoreCase(name)).findFirst();
	}
}