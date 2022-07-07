package mineplex.core.punish;

import java.util.Comparator;
import java.util.Map.Entry;

public class PunishmentSorter implements Comparator<Entry<Category, Punishment>>
{
	public int compare(Entry<Category, Punishment> a, Entry<Category, Punishment> b) 
	{
		if (a.getValue().GetTime() > b.getValue().GetTime())
			return -1;

		if (a.getValue().GetTime() == b.getValue().GetTime())
			return 0;
		
		return 1;
	}
}