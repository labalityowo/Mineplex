package nautilus.game.minekart.gp;

import java.util.Comparator;

import nautilus.game.minekart.kart.Kart;

public class ScoreComparator implements Comparator<Kart>
{
	private GP _gp;
	
	public ScoreComparator(GP gp)
	{
		_gp = gp;
	}
	
	@Override
	public int compare(Kart kart1, Kart kart2)
	{
		if (_gp.GetScore(kart1) > _gp.GetScore(kart2))
			return -1;

		if (_gp.GetScore(kart1) == _gp.GetScore(kart2))
			return 0;
		
		return 1;
	}
}
