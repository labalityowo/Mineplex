package mineplex.core.thereallyoldscoreboardapiweshouldremove.elements;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import mineplex.core.thereallyoldscoreboardapiweshouldremove.ScoreboardManager;

public class ScoreboardElementText implements ScoreboardElement
{
	private String _line;
	
	public ScoreboardElementText(String line)
	{
		_line = line;
	}
	
	@Override
	public List<String> getLines(ScoreboardManager manager, Player player, List<String> out)
	{
		List<String> orderedScores = new ArrayList<String>();
		
		orderedScores.add(_line);
		
		return orderedScores;
	}
	
}
