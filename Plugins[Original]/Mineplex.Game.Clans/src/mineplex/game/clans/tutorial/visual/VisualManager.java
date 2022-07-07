package mineplex.game.clans.tutorial.visual;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;

public class VisualManager extends MiniPlugin
{
	public VisualManager(JavaPlugin plugin)
	{
		super("Visual", plugin);
	}

	public void setTitleMessage(Player player, String message, String desc, int timer, boolean displayNow)
	{

	}

	public void displayTitleMessage(Player player, String message, String desc)
	{

	}

	public void playFinish(Player player)
	{
		player.playSound(player.getLocation(), Sound.LEVEL_UP, 1f, 1f);
	}

	public void clear(Player player)
	{

	}
}
