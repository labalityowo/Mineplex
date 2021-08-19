package nautilus.game.arcade.game.games.build;

import org.bukkit.ChatColor;

import mineplex.core.common.util.C;

public enum BuildQuality 
{
	NoVotes(C.cGray + "No Votes"),
	
	Mindblowing(C.cAqua + "Mindblowing"),
	Awesome(C.cGreen + "Awesome"),
	Good(C.cYellow + "Good"),
	Satisfactory(C.cGold + "Satisfactory"),
	Failure(C.cRed + "FAILURE");
	
	private String _text;
	
	BuildQuality(String text)
	{
		_text = text;
	}
	
	public String getText()
	{
		return _text;
	}
	
	//1000 points is average, if everyone votes for everyone equally
	public static BuildQuality getFinalQuality(double avgScore)
	{
		if (avgScore < 0)	return NoVotes;
		if (avgScore <= 400)	return Failure;
		if (avgScore <= 800)	return Satisfactory;
		if (avgScore <= 1200)	return Good;
		if (avgScore <= 1600)	return Awesome;
		return Mindblowing;
	}

	public String getColor() 
	{
		return ChatColor.getLastColors(_text);
	}
}