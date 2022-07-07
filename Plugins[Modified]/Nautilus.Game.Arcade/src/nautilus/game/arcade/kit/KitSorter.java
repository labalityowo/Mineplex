package nautilus.game.arcade.kit;

import java.util.Comparator;

import mineplex.core.game.kit.KitAvailability;

public class KitSorter implements Comparator<Kit>
{
	@Override
	public int compare(Kit kit1, Kit kit2)
	{
		if (kit1.GetAvailability() == KitAvailability.Free && kit2.GetAvailability() == KitAvailability.Gem)
			return -1;
		else if (kit1.GetAvailability() == KitAvailability.Gem && kit2.GetAvailability() == KitAvailability.Free)
			return 1;
		else if (kit1.GetAvailability() == KitAvailability.Null && kit2.GetAvailability() != KitAvailability.Null)
			return 1;
		else if (kit1.GetAvailability() == KitAvailability.Achievement)
			return 1;
		else if (kit2.GetAvailability() == KitAvailability.Achievement)
			return -1;
		else
			return kit1.GetCost() - kit2.GetCost();
	}
}
