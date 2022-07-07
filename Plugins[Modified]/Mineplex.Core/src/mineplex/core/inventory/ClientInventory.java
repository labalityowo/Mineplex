package mineplex.core.inventory;

import mineplex.core.common.util.NautHashMap;

public class ClientInventory
{
	public NautHashMap<String, ClientItem> Items = new NautHashMap<String, ClientItem>();

	public void addItem(ClientItem item)
	{
		if (!Items.containsKey(item.Item.Name))
			Items.put(item.Item.Name, new ClientItem(item.Item, 0));
		
		Items.get(item.Item.Name).Count += item.Count;
	}
	
	public void removeItem(ClientItem item)
	{
		if (!Items.containsKey(item.Item.Name))
			return;
		
		Items.get(item.Item.Name).Count -= item.Count;
		
		if (Items.get(item.Item.Name).Count == 0)
			Items.remove(item.Item.Name);
	}

	public ClientItem getClientItem(String name) {
		return Items.containsKey(name) ? Items.get(name) : null;
	}
	
	public int getItemCount(String name)
	{
		return Items.containsKey(name) ? Items.get(name).Count : 0;
	}
}
