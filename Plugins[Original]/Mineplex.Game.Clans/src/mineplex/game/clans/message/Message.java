package mineplex.game.clans.message;

import org.bukkit.entity.Player;

import mineplex.core.common.util.UtilTextMiddle;

public class Message
{
	private int _ticksBetweenMessage;
	private int _ticks;

	private String _title;
	private String _description;

	public Message(String title, String description, int ticksBetweenMessage)
	{
		_title = title;
		_description = description;
		_ticksBetweenMessage = ticksBetweenMessage;
		_ticks = 0;
	}

	protected void send(Player player)
	{
		UtilTextMiddle.display(_title, _description, player);
	}

	public int getTicks()
	{
		return _ticks;
	}

	public int getTicksBetweenMessage()
	{
		return _ticksBetweenMessage;
	}

	public void increment()
	{
		_ticks++;
	}

	public boolean shouldSend()
	{
		return _ticks % _ticksBetweenMessage == 0;
	}
}
