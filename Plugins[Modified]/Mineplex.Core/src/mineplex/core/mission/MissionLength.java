package mineplex.core.mission;

import java.util.Calendar;

import org.bukkit.ChatColor;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilText;

public enum MissionLength
{

	DAY("Daily", "tomorrow", UtilText.splitLineToArray(C.cGray + "Here you will find missions that reset every day. Missions only reset if you've completed them or you've discarded them, click on an active mission to discard it.", LineFormat.LORE), ChatColor.WHITE, 0, 1, Calendar.DAY_OF_WEEK),
	WEEK("Weekly", "next week", UtilText.splitLineToArray(C.cGray + "These missions will rotate out every Monday. They are weekly missions that have an increased objective and rewards.", LineFormat.LORE), ChatColor.GREEN, 7, 5, Calendar.WEEK_OF_YEAR),
	EVENT("Event", null, UtilText.splitLineToArray(C.cGray + "These super special missions will only show up every once in a while and come with a super special reward!", LineFormat.LORE), ChatColor.LIGHT_PURPLE, 10, 1, -1);

	private final String _name, _resetWhen;
	private final String[] _resetInfo;
	private final ChatColor _chatColour;
	private final byte _colourData;
	private final int _xScale;
	private final int _calendarField;

	MissionLength(String name, String resetWhen, String[] resetInfo, ChatColor chatColour, int colourData, int xScale, int calendarField)
	{
		_name = name;
		_resetWhen = resetWhen;
		_resetInfo = resetInfo;
		_chatColour = chatColour;
		_colourData = (byte) colourData;
		_xScale = xScale;
		_calendarField = calendarField;
	}

	public <T> PlayerMission<T> createFromContext(MissionContext<T> context)
	{
		return new PlayerMission<>(context, this, context.getRandomX(), context.getRandomY(), 0, true);
	}

	public String getName()
	{
		return _name;
	}

	public String getResetWhen()
	{
		return _resetWhen;
	}

	public String[] getResetInfo()
	{
		return _resetInfo;
	}

	public ChatColor getChatColour()
	{
		return _chatColour;
	}

	public byte getColourData()
	{
		return _colourData;
	}

	public int getxScale()
	{
		return _xScale;
	}

	public int getCalendarField()
	{
		return _calendarField;
	}

	public String getStatName()
	{
		return "Global.Missions." + getName();
	}
}
