package mineplex.hub;

public class HubClient
{

	private static final int DISPLAY_LENGTH = 16;

	private String ScoreboardString;
	private int ScoreboardIndex ;

	public HubClient(String name)
	{
		setName(name);
	}

	public void setName(String name)
	{
		ScoreboardString = "      Welcome " + name + ", to the Mineplex Network!";
		ScoreboardIndex = 0;
	}

	public String GetScoreboardText()
	{
		if (ScoreboardString.length() <= DISPLAY_LENGTH)
		{
			return ScoreboardString;
		}

		String display = ScoreboardString.substring(ScoreboardIndex, Math.min(ScoreboardIndex + DISPLAY_LENGTH, ScoreboardString.length()));

		if (display.length() < DISPLAY_LENGTH)
		{
			int add = DISPLAY_LENGTH - display.length();
			display += ScoreboardString.substring(0, add);
		}

		ScoreboardIndex = (ScoreboardIndex + 1) % ScoreboardString.length();
		return display;
	}
}
