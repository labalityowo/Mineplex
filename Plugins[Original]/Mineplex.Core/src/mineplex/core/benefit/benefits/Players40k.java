package mineplex.core.benefit.benefits;

import mineplex.core.benefit.BenefitManager;
import mineplex.core.benefit.BenefitManagerRepository;
import mineplex.core.common.util.C;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.inventory.InventoryManager;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Players40k extends BenefitBase
{
	private InventoryManager _inventoryManager;
	
	public Players40k(BenefitManager plugin, BenefitManagerRepository repository, InventoryManager inventoryManager)
	{
		super(plugin, "Players40k", repository);
		
		_inventoryManager = inventoryManager;
	}

	@Override
	public void rewardPlayer(final Player player)
	{
		_inventoryManager.addItemToInventory(new Callback<Boolean>()
		{
			public void run(Boolean success)
			{
				if (success)
				{
					UtilPlayer.message(player, C.cGold + C.Strike + "=============================================");
					UtilPlayer.message(player, "");
					UtilPlayer.message(player, "To celebrate hitting 40,000 players online,");
					UtilPlayer.message(player, "everyone receives a prize! You're awesome!");
					UtilPlayer.message(player, "");
					UtilPlayer.message(player, "You received 1 Ancient Chest!");
					UtilPlayer.message(player, "");
					UtilPlayer.message(player, C.cGold + C.Strike + "=============================================");
				}
			}
		}, player, "Ancient Chest", 1);
	}
}
