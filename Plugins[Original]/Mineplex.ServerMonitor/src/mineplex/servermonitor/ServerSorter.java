package mineplex.servermonitor;

import java.util.Comparator;

import mineplex.serverdata.data.MinecraftServer;

public class ServerSorter implements Comparator<MinecraftServer>
{
	@Override
	public int compare(MinecraftServer first, MinecraftServer second)
	{
		String[] args1 = first.getName().split("-");
		String[] args2 = second.getName().split("-");
		if (Integer.parseInt(args1[args1.length - 1]) < Integer.parseInt(args2[args2.length - 1]))
			return -1;
		else if (Integer.parseInt(args2[args2.length - 1]) < Integer.parseInt(args1[args1.length - 1]))
			return 1;

		return 0;
	}
}
