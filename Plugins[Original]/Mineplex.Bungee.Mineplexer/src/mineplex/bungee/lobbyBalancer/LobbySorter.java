package mineplex.bungee.lobbyBalancer;

import java.util.Comparator;

import mineplex.serverdata.data.MinecraftServer;

public class LobbySorter implements Comparator<MinecraftServer>
{
	@Override
	public int compare(MinecraftServer first, MinecraftServer second)
	{
		if (second.getPlayerCount() == 999)
			return -1;
		
		if (first.getPlayerCount() == 999)
			return 1;
		
		if (first.getPlayerCount() < (first.getMaxPlayerCount() / 2) && second.getPlayerCount() >= (second.getMaxPlayerCount() / 2))
			return -1;

		if (second.getPlayerCount() < (second.getMaxPlayerCount() / 2) && first.getPlayerCount() >= (first.getMaxPlayerCount() / 2))
			return 1;
		
		if (first.getPlayerCount() < (first.getMaxPlayerCount() / 2))
		{
			if (first.getPlayerCount() > second.getPlayerCount())
				return -1;
			
			if (second.getPlayerCount() > first.getPlayerCount())
				return 1;
		}
		else
		{
			if (first.getPlayerCount() < second.getPlayerCount())
				return -1;
			
			if (second.getPlayerCount() < first.getPlayerCount())
				return 1;
		}
		
		if (Integer.parseInt(first.getName().split("-")[1]) < Integer.parseInt(second.getName().split("-")[1]))
			return -1;
		else if (Integer.parseInt(second.getName().split("-")[1]) < Integer.parseInt(first.getName().split("-")[1]))
			return 1;

		return 0;
	}
}
