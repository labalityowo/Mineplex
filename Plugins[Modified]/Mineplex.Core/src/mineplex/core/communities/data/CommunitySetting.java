package mineplex.core.communities.data;

import org.bukkit.ChatColor;

import mineplex.core.common.Pair;
import mineplex.core.common.util.Callback;
import mineplex.core.communities.data.Community.PrivacySetting;
import mineplex.core.game.GameDisplay;

public enum CommunitySetting
{
	CHAT_NAME_COLOR(1, pair ->
		{
			String value = pair.getLeft();
			Community community = pair.getRight();
			
			ChatColor color = ChatColor.valueOf(value);
			if (color == null || !color.isColor())
			{
				return;
			}
			community.setChatFormatting(new ChatColor[] {color, community.getChatFormatting()[1], community.getChatFormatting()[2]});
		}
	),
	CHAT_PLAYER_COLOR(2, pair ->
		{
			String value = pair.getLeft();
			Community community = pair.getRight();
			
			ChatColor color = ChatColor.valueOf(value);
			if (color == null || !color.isColor())
			{
				return;
			}
			community.setChatFormatting(new ChatColor[] {community.getChatFormatting()[0], color, community.getChatFormatting()[2]});
		}
	),
	CHAT_MESSAGE_COLOR(3, pair ->
		{
			String value = pair.getLeft();
			Community community = pair.getRight();
			
			ChatColor color = ChatColor.valueOf(value);
			if (color == null || !color.isColor())
			{
				return;
			}
			community.setChatFormatting(new ChatColor[] {community.getChatFormatting()[0], community.getChatFormatting()[1], color});
		}
	),
	CHAT_DELAY(4, pair ->
		{
			String value = pair.getLeft();
			Community community = pair.getRight();
			
			try
			{
				Long delay = Long.parseLong(value);
				community.setChatDelay(delay);
			}
			catch (Exception e)
			{
				return;
			}
		}
	),
	FAVORITE_GAME(5, pair ->
		{
			String value = pair.getLeft();
			Community community = pair.getRight();
			
			GameDisplay display = GameDisplay.matchName(value);
			community.setFavoriteGame(display);
		}
	),
	PRIVACY(6, pair ->
		{
			String value = pair.getLeft();
			Community community = pair.getRight();
			
			PrivacySetting setting = PrivacySetting.parsePrivacy(value);
			community.setPrivacySetting(setting);
		}
	),
	DESCRIPTION(7, pair ->
	{
		String value = pair.getLeft();
		Community community = pair.getRight();
		
		community.setDescription(value);
	}),
	SHOW_IN_BROWSER(8, pair ->
	{
		pair.getRight().setBrowserFlag();
	}),
	;
	
	private int _id;
	private Callback<Pair<String, Community>> _parser;
	
	private CommunitySetting(int id, Callback<Pair<String, Community>> parseValue)
	{
		_id = id;
		_parser = parseValue;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public void parseValueInto(String value, Community community)
	{
		_parser.run(Pair.create(value, community));
	}
	
	public static CommunitySetting getSetting(Integer id)
	{
		for (CommunitySetting setting : CommunitySetting.values())
		{
			if (setting.getId() == id)
			{
				return setting;
			}
		}
		
		return null;
	}
}