package mineplex.core.communities.data;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import mineplex.core.common.util.UtilPlayer;
import mineplex.core.game.GameDisplay;

public class Community implements ICommunity
{
	private final int _id;
	private String _name;
	private String _description;
	private Map<UUID, CommunityMemberInfo> _members = new ConcurrentHashMap<>();
	private Map<UUID, CommunityJoinRequestInfo> _joinRequests = new ConcurrentHashMap<>();
	private ChatColor[] _chatFormat;
	private long _chatDelay;
	private GameDisplay _favoriteGame;
	private PrivacySetting _privacy;
	private boolean _showInBrowser;

	public Community(int id, String name)
	{
		_id = id;
		_name = name;
		_description = "No Description Set";
		_chatFormat = new ChatColor[] { ChatColor.BLUE, ChatColor.RED, ChatColor.GREEN };
		_chatDelay = 1000;
		_favoriteGame = GameDisplay.ChampionsCTF;
		_privacy = PrivacySetting.RECRUITING;
	}
	
	public Integer getId()
	{
		return _id;
	}

	@Override
	public Integer getMemberCount()
	{
		return _members.size();
	}

	public String getName()
	{
		return _name;
	}
	
	public void setName(String name)
	{
		_name = name;
	}
	
	public String getDescription()
	{
		return _description;
	}
	
	public void setDescription(String description)
	{
		_description = description;
	}
	
	public Map<UUID, CommunityMemberInfo> getMembers()
	{
		return _members;
	}
	
	public Map<UUID, CommunityJoinRequestInfo> getJoinRequests()
	{
		return _joinRequests;
	}
	
	public ChatColor[] getChatFormatting()
	{
		return _chatFormat;
	}
	
	public void setChatFormatting(ChatColor[] colors)
	{
		_chatFormat = colors;
	}
	
	public Long getChatDelay()
	{
		return _chatDelay;
	}
	
	public void setChatDelay(Long delay)
	{
		_chatDelay = delay;
	}
	
	public GameDisplay getFavoriteGame()
	{
		return _favoriteGame;
	}
	
	public void setFavoriteGame(GameDisplay game)
	{
		_favoriteGame = game;
	}
	
	public PrivacySetting getPrivacySetting()
	{
		return _privacy;
	}
	
	public void setPrivacySetting(PrivacySetting privacy)
	{
		_privacy = privacy;
	}

	public boolean isBrowserFlagSet()
	{
		return _showInBrowser;
	}

	/**
	 * We don't actually care if they should be shown in the browser, just that the flag has been set
	 */
	public void setBrowserFlag()
	{
		_showInBrowser = true;
	}

	public void sendChat(String chat)
	{
		getMembers().values().stream().filter(info -> info.ReadingChat).forEach(member -> UtilPlayer.message(Bukkit.getPlayer(member.UUID), chat));
	}
	
	public void message(String message)
	{
		getMembers().values().forEach(member -> UtilPlayer.message(Bukkit.getPlayer(member.UUID), message));
	}
	
	public void message(String message, CommunityRole minimumRole)
	{
		getMembers().values().stream().filter(member -> member.Role.ordinal() <= minimumRole.ordinal()).forEach(member -> UtilPlayer.message(Bukkit.getPlayer(member.UUID), message));
	}

	public BrowserCommunity toBrowser()
	{
		return new BrowserCommunity(getId(), getName())
				.setPrivacySetting(getPrivacySetting())
				.setFavoriteGame(getFavoriteGame())
				.setDescription(getDescription())
				.setMembers(getMemberCount());
	}

	public enum PrivacySetting
	{
		OPEN("Open to Join"),
		RECRUITING("Accepting Join Requests"),
		PRIVATE("Closed")
		;
		
		private String _display;
		
		PrivacySetting(String display)
		{
			_display = display;
		}
		
		public String getDisplayText()
		{
			return _display;
		}
		
		public static PrivacySetting parsePrivacy(String privacy)
		{
			for (PrivacySetting setting : PrivacySetting.values())
			{
				if (setting.toString().equalsIgnoreCase(privacy))
				{
					return setting;
				}
			}
			
			return PrivacySetting.RECRUITING;
		}

		@Override
		public String toString()
		{
			return _display;
		}
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Community community = (Community) o;
		return _id == community._id;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(_id);
	}

	@Override
	public String toString()
	{
		return "Community{" +
		       "_id=" + _id +
		       ", _name='" + _name + '\'' +
		       ", _description='" + _description + '\'' +
		       ", _favoriteGame=" + _favoriteGame +
		       ", _privacy=" + _privacy +
		       '}';
	}
}