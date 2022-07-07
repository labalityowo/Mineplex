package mineplex.core.chat.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;

import mineplex.core.chat.ChatChannel;
import mineplex.core.chat.format.ChatFormatComponent;

public class FormatPlayerChatEvent extends PlayerEvent implements Cancellable
{

	private static final HandlerList HANDLER_LIST = new HandlerList();

	public static HandlerList getHandlerList()
	{
		return HANDLER_LIST;
	}

	private final AsyncPlayerChatEvent _parentEvent;
	private final ChatChannel _chatChannel;
	private final List<ChatFormatComponent> _formatComponents;
	private ChatColor _messageColour;
	private boolean _filtered, _cancelled;

	public FormatPlayerChatEvent(AsyncPlayerChatEvent parentEvent, ChatChannel chatChannel, List<ChatFormatComponent> formatComponents)
	{
		super(parentEvent.getPlayer());

		_parentEvent = parentEvent;
		_chatChannel = chatChannel;
		_formatComponents = new ArrayList<>(formatComponents);
		_messageColour = ChatColor.WHITE;
		_filtered = true;
		_cancelled = parentEvent.isCancelled();
	}

	public List<ChatFormatComponent> getFormatComponents()
	{
		return _formatComponents;
	}

	public void setMessage(String message)
	{
		_parentEvent.setMessage(message);
	}

	public String getMessage()
	{
		return _parentEvent.getMessage();
	}

	public Set<Player> getRecipients()
	{
		return _parentEvent.getRecipients();
	}

	public ChatChannel getChatChannel()
	{
		return _chatChannel;
	}

	public void setMessageColour(ChatColor messageColour)
	{
		_messageColour = messageColour;
	}

	public ChatColor getMessageColour()
	{
		return _messageColour;
	}

	public void setFiltered(boolean filtered)
	{
		_filtered = filtered;
	}

	public boolean isFiltered()
	{
		return _filtered;
	}

	@Override
	public void setCancelled(boolean cancelled)
	{
		_cancelled = cancelled;
	}

	@Override
	public boolean isCancelled()
	{
		return _cancelled;
	}

	@Override
	public HandlerList getHandlers()
	{
		return HANDLER_LIST;
	}

}
