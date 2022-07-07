package mineplex.gemhunters.shop;

import mineplex.gemhunters.loot.LootItem;

public class TradeableItem
{

	private final LootItem _item;
	private final int _cost;

	public TradeableItem(LootItem item, int cost)
	{
		_item = item;
		_cost = cost;
	}

	public LootItem getLootItem()
	{
		return _item;
	}

	public int getCost()
	{
		return _cost;
	}

}
