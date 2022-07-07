package mineplex.game.clans.clans;

import java.util.Comparator;

public class ClansPlayerComparator implements Comparator<ClansPlayer>
{
	@Override
	public int compare(ClansPlayer o1, ClansPlayer o2)
	{
		if (o1 == null || o2 == null)
			return 0;

		if (o1.isOnline() != o2.isOnline())
		{
			return o1.isOnline() ? -1 : 1;
		}

		if (o1.getRole() != o2.getRole())
		{
			return o2.getRole().ordinal() - o1.getRole().ordinal();
		}

		return o1.getPlayerName().compareTo(o2.getPlayerName());
	}
}
