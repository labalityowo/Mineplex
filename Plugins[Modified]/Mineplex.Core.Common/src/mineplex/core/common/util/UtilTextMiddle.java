package mineplex.core.common.util;

import org.bukkit.entity.Player;

import net.minecraft.server.v1_8_R3.ChatMessage;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;

/**
 * Created by Shaun on 9/5/2014.
 */
public class UtilTextMiddle
{
	public static void display(String text, String subtitle, Player... players)
	{
		setSubtitle(subtitle, players);
		
		showTitle(text, players);
	}
	
	public static void display(String text, String subtitle)
	{
		setSubtitle(subtitle, UtilServer.getPlayers());
		
		showTitle(text, UtilServer.getPlayers());
	}
	
	public static void display(String text, String subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks, Player... players)
	{
		if (players.length == 1 && players[0] == null)
		{
			return;
		}
		
		setTimings(fadeInTicks, stayTicks, fadeOutTicks, players);
		
		display(text, subtitle, players);
	}
	
	public static void display(String text, String subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks)
	{
		setTimings(fadeInTicks, stayTicks, fadeOutTicks, UtilServer.getPlayers());
		
		display(text, subtitle, UtilServer.getPlayers());
	}
	
	/**
	 * Show Title text for a player with their current set timings.
	 *
	 * Default timings are 20, 60, 20 (in ticks)
	 */
	private static void showTitle(String text, Player... players)
	{
		if (text == null)
			text = "";
		
		ChatMessage message = new ChatMessage(text);
		PacketPlayOutTitle packet = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, message);
		sendPacket(packet, players);
	}

	/**
	 * Set the current subtitle for a player.
	 *
	 * This stays unless reset or cleared, and doesn't appear unless a title is showing
	 */
	private static void setSubtitle(String text, Player... players)
	{
		if (text == null)
			text = "";
		
		ChatMessage message = new ChatMessage(text);
		PacketPlayOutTitle packet = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, message);
		sendPacket(packet, players);
	}

	/**
	 * Set timings for a player.
	 *
	 * Remember these are in ticks
	 */
	private static void setTimings(int fadeInTicks, int stayTicks, int fadeOutTicks, Player... players)
	{
		PacketPlayOutTitle packet = new PacketPlayOutTitle(fadeInTicks, stayTicks, fadeOutTicks);
		sendPacket(packet, players);
	}

	/**
	 * Clear the title that is currently being displayed, has no affect on timings or subtitle.
	 */
	public static void clear(Player... players)
	{
		PacketPlayOutTitle packet = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.CLEAR, null);
		sendPacket(packet, players);
	}

	/**
	 * Reset subtitle and timings for a player.
	 *
	 * This will set the subtitle to nothing and timings back to default (20, 60, 20)
	 */
	public static void reset(Player... players)
	{
		PacketPlayOutTitle packet = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.RESET, null);
		sendPacket(packet, players);
	}

	private static void sendPacket(Packet packet, Player... players)
	{
		for (Player player : players)
		{
			UtilPlayer.sendPacket(player, packet);
		}
	}

	public static String progress(float exp)
	{
		String out = "";
		
		for (int i=0 ; i<40 ; i++)
		{
			float cur = i * (1f /40f);
			
			if (cur < exp)
				out += C.cGreen + C.Bold + "|";
			else
				out += C.cGray + C.Bold + "|";
		}
		
		return out;
	}

}
