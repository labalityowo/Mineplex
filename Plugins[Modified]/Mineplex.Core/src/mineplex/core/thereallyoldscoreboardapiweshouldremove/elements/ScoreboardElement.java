package mineplex.core.thereallyoldscoreboardapiweshouldremove.elements;

import java.util.List;

import org.bukkit.entity.Player;

import mineplex.core.thereallyoldscoreboardapiweshouldremove.ScoreboardManager;

public interface ScoreboardElement
{
	public List<String> getLines(ScoreboardManager manager, Player player, List<String> out);
}
