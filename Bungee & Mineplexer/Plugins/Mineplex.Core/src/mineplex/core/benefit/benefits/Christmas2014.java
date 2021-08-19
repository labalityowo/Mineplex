package mineplex.core.benefit.benefits;

import mineplex.core.benefit.BenefitManager;
import mineplex.core.benefit.BenefitManagerRepository;
import mineplex.core.common.util.C;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.inventory.InventoryManager;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Christmas2014 extends BenefitBase
{
	private InventoryManager _inventoryManager;
	
	public Christmas2014(BenefitManager plugin, BenefitManagerRepository repository, InventoryManager inventoryManager)
	{
		super(plugin, "Christmas2014", repository);
		
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
					UtilPlayer.message(player, C.cPurple + C.Strike + "=============================================");
					UtilPlayer.message(player, "");
					UtilPlayer.message(player, C.cRed + "MERRY CHRISTMAS");
					UtilPlayer.message(player, "You received 2 Treasure Keys!");
					UtilPlayer.message(player, "");
					UtilPlayer.message(player, C.cPurple + C.Strike + "=============================================");
				}
			}
		}, player, "Treasure Key", 2);
	}
}
