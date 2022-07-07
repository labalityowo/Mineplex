package mineplex.gemhunters.quest.types;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.gemhunters.quest.Quest;

public class CraftingQuest extends Quest
{

	private final Material _material;
	private final int _amount;
	
	public CraftingQuest(int id, String name, String description, int startCost, int completeReward, Material material, int amount)
	{
		super(id, name, description, startCost, completeReward);
		
		_material = material;
		_amount = amount;
	}
	
	@EventHandler
	public void craft(CraftItemEvent event)
	{
		ItemStack result = event.getRecipe().getResult();
		
		if (result.getType() == _material)
		{
			if (getAndIncrement((Player) event.getWhoClicked(), result.getAmount()) >= _amount)
			{
				onReward((Player) event.getWhoClicked());
			}
		}
	}

}
