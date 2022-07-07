package nautilus.game.arcade.game;

public class GemData 
{
	public double Gems;
	public int Amount;
	
	public GemData(double gems, boolean amount)
	{
		Gems = gems;
		
		if (amount)
			Amount = 1;
	}
	
	public void AddGems(double gems)
	{
		Gems += gems;
		
		if (Amount > 0)
			Amount++;
	}
}
