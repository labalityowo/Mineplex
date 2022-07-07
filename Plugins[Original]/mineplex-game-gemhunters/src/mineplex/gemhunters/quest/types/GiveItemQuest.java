package mineplex.gemhunters.quest.types;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilInv;
import mineplex.gemhunters.quest.Quest;

public class GiveItemQuest extends Quest
{

	private final ItemStack _required;
	private final int _amount;

	public GiveItemQuest(int id, String name, String description, int startCost, int completeReward, ItemStack required, int amount)
	{
		super(id, name, "Bring the " + F.name("Quest Master ") + description, startCost, completeReward);

		_required = required;
		_amount = amount;
	}

	@Override
	public float getProgress(Player player)
	{
		return (float) get(player) / (float) _amount;
	}

	@Override
	public int getGoal()
	{
		return _amount;
	}

	@EventHandler
	public void entityClick(PlayerInteractEntityEvent event)
	{
		if (!_quest.isQuestNPC(event.getRightClicked()))
		{
			return;
		}

		Player player = event.getPlayer();
		
		if (!isActive(player))
		{
			return;
		}
		
		ItemStack itemStack = player.getItemInHand();
		
		if (itemStack == null || !itemStack.isSimilar(_required))
		{
			return;
		}
				
		int amount = get(player);
		
		while (itemStack != null && amount < _amount)
		{
			itemStack = UtilInv.decrement(itemStack);
			amount++;
			getAndIncrement(player, 1);
		}
		
		player.setItemInHand(itemStack);
		player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1.1F);
		event.setCancelled(true);
		
		if (amount >= _amount)
		{
			onReward(player);
			return;
		}
	}

}
