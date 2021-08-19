package mineplex.serverdata.servers;

import java.util.Comparator;

import mineplex.serverdata.data.DedicatedServer;

public class DedicatedServerSorter implements Comparator<DedicatedServer>
{
	@Override
	public int compare(DedicatedServer first, DedicatedServer second)
	{
		if (second.getAvailableRam() <= 1024) return -1;
		else if (first.getAvailableRam() <= 1024) return 1;
		else if (first.getAvailableRam() > second.getAvailableRam()) return -1;
		else if (second.getAvailableRam() > first.getAvailableRam()) return 1;
		else if (first.getAvailableCpu() > second.getAvailableCpu()) return -1;
		else if (second.getAvailableCpu() > first.getAvailableCpu()) return 1;
		else return 0;
	}
}