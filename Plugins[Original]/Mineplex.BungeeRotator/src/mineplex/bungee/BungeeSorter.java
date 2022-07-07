package mineplex.bungee;

import java.util.Comparator;

import mineplex.serverdata.data.BungeeServer;

public class BungeeSorter implements Comparator<BungeeServer>
{	
	public int compare(BungeeServer a, BungeeServer b)
	{
		if (a.getPlayerCount() < b.getPlayerCount())
			return -1;
		
		if (b.getPlayerCount() < a.getPlayerCount())
			return 1;

		return 0;
	}
}