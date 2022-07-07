package mineplex.gemhunters.quest.types;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.gemhunters.loot.event.PlayerChestOpenEvent;
import mineplex.gemhunters.quest.Quest;

public class SpecificChestOpenerQuest extends Quest
{

	private final String _colour;
	private final int _amount;
	
	public SpecificChestOpenerQuest(int id, String name, String description, int startCost, int completeReward, String colour, int amount)
	{
		super(id, name, description, startCost, completeReward);
		
		_colour = colour;
		_amount = amount;
	}
	
	@EventHandler
	public void chestOpen(PlayerChestOpenEvent event)
	{
		Player player = event.getPlayer();

		if (!isActive(player) || !event.getProperties().getDataKey().equals(_colour))
		{
			return;
		}
		
		if (getAndIncrement(player, 1) >= _amount)
		{
			onReward(player);
		}
	}

}
