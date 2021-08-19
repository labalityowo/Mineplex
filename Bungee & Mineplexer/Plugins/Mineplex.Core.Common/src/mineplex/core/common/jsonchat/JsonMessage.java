package mineplex.core.common.jsonchat;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;

public class JsonMessage
{
	protected StringBuilder Builder;

	public JsonMessage(String text)
	{
		this(new StringBuilder(), text);
	}
	
	public JsonMessage(StringBuilder builder, String text)
	{	
		Builder = builder;		
		Builder.append("{\"text\":\"" + text + "\"");
	}
	
	public JsonMessage color(String color)
	{
		Builder.append(", color:" + color);
		return this;
	}
	
	public JsonMessage bold()
	{
		Builder.append(", bold:true");

		return this;
	}

	public JsonMessage italic()
	{
		Builder.append(", italic:true");

		return this;
	}

	public JsonMessage underlined()
	{
		Builder.append(", underlined:true");

		return this;
	}

	public JsonMessage strikethrough()
	{
		Builder.append(", strikethrough:true");

		return this;
	}

	public JsonMessage obfuscated()
	{
		Builder.append(", obfuscated:true");

		return this;
	}
	
	public ChildJsonMessage extra(String text)
	{
		Builder.append(", \"extra\":[");
		return new ChildJsonMessage(this, Builder, text);
	}
	
	public JsonMessage click(String action, String value)
	{
		Builder.append(", \"clickEvent\":{\"action\":\"" + action + "\",\"value\":\"" + value + "\"}");
		
		return this;
	}

	public JsonMessage hover(String action, String value)
	{
		Builder.append(", \"hoverEvent\":{\"action\":\"" + action + "\",\"value\":\"" + value + "\"}");
		
		return this;
	}

	public JsonMessage click(ClickEvent event, String value)
	{
		return click(event.toString(), value);
	}

	public JsonMessage hover(HoverEvent event, String value)
	{
		return hover(event.toString(), value);
	}

	public JsonMessage color(Color color)
	{
		return color(color.toString());
	}
	
	public String toString()
	{
		Builder.append("}");
		
		return Builder.toString();
	}
	
	public void sendToPlayer(Player player)
	{
		((CraftPlayer) player).getHandle().sendMessage(IChatBaseComponent.ChatSerializer.a(toString()));
	}

	/**
	 * Send a message to players using the new 1.8 message types
	 *
	 * @param messageType Message type to send
	 * @param players Players to send to
	 */
	public void send(MessageType messageType, Player... players)
	{
		PacketPlayOutChat chatPacket = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a(toString()));
		chatPacket.b = messageType.getId();

		for (Player player : players)
		{
			UtilPlayer.sendPacket(player, chatPacket);
		}
	}

	public static enum MessageType
	{
		CHAT_BOX((byte) 0), // Inside Chat Box
		SYSTEM_MESSAGE((byte) 1), // Inside Chat Box - This is used for the client to identify difference between chat message and server messages
		ABOVE_HOTBAR((byte) 2); // Shows above hotbar

		private byte _id;

		MessageType(byte id)
		{
			_id = id;
		}

		public byte getId()
		{
			return _id;
		}
	}
}
