package mineplex.core.thereallyoldscoreboardapiweshouldremove;

import java.util.ArrayList;

import mineplex.core.thereallyoldscoreboardapiweshouldremove.elements.*;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ScoreboardData
{
	private ArrayList<ScoreboardElement> _elements = new ArrayList<ScoreboardElement>();

	public ScoreboardData()
	{

	}

	public ArrayList<String> getLines(ScoreboardManager manager, Player player)
	{
		ArrayList<String> output = new ArrayList<String>();

		for (ScoreboardElement elem : _elements)
			output.addAll(elem.getLines(manager, player, output));

		return output;
	}
	
	public void reset()
	{
		_elements.clear();
	}

	public String prepareLine(String line)
	{
		if (line.length() > 28)
		{
			// Due to the scoreboard using teams, You can use prefix and suffix for a total length of 32.
			// this means that the total length of the string can't extend 32.
			// Reason for the fancy logic is that the beginning of the suffix needs to use colors from line1 else the line is pure
			// white. And line2 can't have its length extend 16..

			String line1 = line.substring(0, 16);
			String color = ChatColor.getLastColors(line1);
			String line2 = line.substring(16);

			int length = 16 - (color + line2).length();

			if (length > 0)
			{
				return line1 + line2.substring(0, line2.length() - length);
			}
		}

		return line;
	}

	public void write(String line)
	{
		line = prepareLine(line);

		_elements.add(new ScoreboardElementText(line));
	}

	public void writeOrdered(String key, String line, int value, boolean prependScore)
	{
		if (prependScore)
			line = value + " " + line;

		line = prepareLine(line);

		for (ScoreboardElement elem : _elements)
		{
			if (elem instanceof ScoreboardElementScores)
			{
				ScoreboardElementScores scores = (ScoreboardElementScores) elem;

				if (scores.IsKey(key))
				{
					scores.AddScore(line, value);
					return;
				}
			}
		}

		_elements.add(new ScoreboardElementScores(key, line, value, true));
	}

	public void writeElement(ScoreboardElement element)
	{
		_elements.add(element);
	}

	public void writeEmpty()
	{
		_elements.add(new ScoreboardElementText(" "));
	}

	public void writePlayerGems()
	{
		_elements.add(new ScoreboardElementGemCount());
	}

	public void writePlayerCoins()
	{
		_elements.add(new ScoreboardElementCoinCount());
	}

	public void writePlayerRank()
	{
		_elements.add(new ScoreboardElementRank());
	}
}
