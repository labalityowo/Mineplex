package mineplex.core.progression.gui.buttons;

import java.util.UUID;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.F;
import mineplex.core.menu.Menu;
import mineplex.core.progression.KitProgressionManager;
import mineplex.core.progression.ProgressiveKit;
import mineplex.core.progression.data.KitUpgradeProcessor;
import mineplex.core.progression.data.KitUpgradeShop;
import mineplex.core.shop.confirmation.ConfirmationPage;

/**
 * @author Timothy Andis (TadahTech) on 4/7/2016.
 */
public class KitUpgradeButton extends KitButton
{

	private KitProgressionManager _plugin;
	private int _upgradeLevel;

	public KitUpgradeButton(KitProgressionManager plugin, ProgressiveKit kit, ItemStack itemStack, int upgradeLevel)
	{
		super(kit, itemStack);
		_plugin = plugin;
		_upgradeLevel = upgradeLevel;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		UUID uuid = player.getUniqueId();

		if (!getKit().canPurchaseUpgrade(uuid, _upgradeLevel) || getKit().ownsUpgrade(uuid, _upgradeLevel))
		{
			player.playSound(player.getLocation(), Sound.ITEM_BREAK, 10.0F, 1.0F);
			player.sendMessage(F.main("Kit Progression", "You cannot purchase this upgrade!"));
			return;
		}
		
		KitUpgradeShop shop = new KitUpgradeShop(_plugin, _plugin.getClientManager(), _plugin.getDonationManager());
		KitUpgradeProcessor processor = new KitUpgradeProcessor(_plugin, player, getKit(), _upgradeLevel);
		ConfirmationPage<KitProgressionManager, KitUpgradeShop> page = new ConfirmationPage<KitProgressionManager, KitUpgradeShop>(player, _plugin, shop, _plugin.getClientManager(), _plugin.getDonationManager(), processor, getItemStack());
		
		shop.openPageForPlayer(player, page);

		Menu.remove(uuid);

		//player.closeInventory();
	}
}
