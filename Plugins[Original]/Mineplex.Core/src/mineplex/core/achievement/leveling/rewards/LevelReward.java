package mineplex.core.achievement.leveling.rewards;

import mineplex.core.Managers;
import mineplex.core.donation.DonationManager;
import mineplex.core.inventory.InventoryManager;
import org.bukkit.entity.Player;

public interface LevelReward
{

	DonationManager DONATION = Managers.require(DonationManager.class);
	InventoryManager INVENTORY = Managers.require(InventoryManager.class);

	void claim(Player player);

	String getDescription();

}
