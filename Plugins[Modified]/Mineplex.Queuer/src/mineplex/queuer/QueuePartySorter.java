package mineplex.queuer;

import java.util.Comparator;

public class QueuePartySorter implements Comparator<QueueParty>
{
	
    public int compare(QueueParty party1, QueueParty party2) 
    {
    	if (party1.getAverageElo() < party2.getAverageElo())
    		return -1;
    	
    	if (party2.getAverageElo() < party1.getAverageElo())
    		return 1;
    	
    	if (party1.getId() < party2.getId())
    		return -1;

        return 1;
    }
}