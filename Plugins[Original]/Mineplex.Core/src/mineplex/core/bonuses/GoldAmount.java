package mineplex.core.bonuses;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GoldAmount
{
	private Map<Integer, Integer> _goldAmounts;
	
	public GoldAmount()
	{
		_goldAmounts = new HashMap<>();
	}
	
	public Collection<Integer> getServerIds()
	{
		return _goldAmounts.keySet();
	}
	
	public Integer getGoldFor(Integer serverId)
	{
		return _goldAmounts.getOrDefault(serverId, 0);
	}
	
	public Integer getTotalGold()
	{
		Integer gold = 0;
		for (Integer g : _goldAmounts.values())
		{
			gold += g;
		}
		
		return gold;
	}
	
	public void setGoldFor(Integer serverId, Integer gold)
	{
		_goldAmounts.put(serverId, gold);
	}
	
	public void addGold(Integer serverId, Integer gold)
	{
		_goldAmounts.put(serverId, getGoldFor(serverId) + gold);
	}
	
	public void clearGoldFor(Integer serverId)
	{
		_goldAmounts.remove(serverId);
	}
}