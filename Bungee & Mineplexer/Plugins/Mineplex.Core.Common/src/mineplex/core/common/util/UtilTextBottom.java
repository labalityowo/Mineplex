package mineplex.core.common.util;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.core.common.jsonchat.JsonMessage;
import mineplex.core.common.jsonchat.JsonMessage.MessageType;

public class UtilTextBottom
{
	public static void display(String text, Player... players)
	{
		JsonMessage msg = new JsonMessage(text);
		
		//1.8
		msg.send(MessageType.ABOVE_HOTBAR, players);
	}
	
	public static void displayProgress(double amount, Player... players)
	{
		displayProgress(null, amount, null, players);
	}
	
	public static void displayProgress(String prefix, double amount, Player... players)
	{
		displayProgress(prefix, amount, null, players);
	}
	
	public static void displayProgress(String prefix, double amount, String suffix, Player... players)
	{
		displayProgress(prefix, amount, suffix, false, players);
	}
	
	public static void displayProgress(String prefix, double amount, String suffix, boolean progressDirectionSwap, Player... players)
	{
		if (progressDirectionSwap)
			amount = 1 - amount;
		
		//Generate Bar
		int bars = 24;
		String progressBar = C.cGreen + "";
		boolean colorChange = false;
		for (int i=0 ; i<bars ; i++)
		{
			if (!colorChange && (float)i/(float)bars >= amount)
			{
				progressBar += C.cRed;
				colorChange = true;
			}
			
			progressBar += "▌";
		}

		display((prefix == null ? "" : prefix + ChatColor.RESET + " ") + progressBar + (suffix == null ? "" : ChatColor.RESET + " " + suffix), players);
	}

	/**
	 * Displays a bottom text progress bar with a specific number of bars
	 * @param prefix The prefix of the progress bar
	 * @param suffix The suffix of the progress bar
	 * @param bars The number of bars to display
	 * @param filledBars The number of filled bars
	 * @param players The players who will receive the progress bar
	 */
	public static void displayProgress(String prefix, String suffix, int bars, int filledBars, Player... players)
	{
		String progressBar = C.cGreen + "";
		for (int i = 0; i < bars; i++)
		{
			if (i > filledBars - 1)
			{
				progressBar += C.cRed;
			}
			progressBar += "▌";
		}

		display((prefix == null ? "" : prefix + ChatColor.RESET + " ") + progressBar + (suffix == null ? "" : ChatColor.RESET + " " + suffix), players);
	}

}
