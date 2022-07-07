package mineplex.game.clans.shop.energy;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilServer;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.shop.confirmation.ConfirmationPage;
import mineplex.core.shop.item.IButton;
import mineplex.game.clans.clans.ClanEnergyManager;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.event.PreEnergyShopBuyEvent;
import mineplex.game.clans.economy.GoldPurchaseProcessor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class EnergyShopButton implements IButton
{
	private static final ItemStack ICON = new ItemBuilder(Material.REDSTONE).setTitle(ChatColor.RESET + "Clan Energy").build();

	private ClanEnergyManager _energyManager;
	private EnergyPage _page;
	private int _energyToPurchase;
	private ClanInfo _clanInfo;
	private int _cost;

	public EnergyShopButton(ClanEnergyManager energyManager, EnergyPage page, int energyToPurchase, ClanInfo clanInfo, int cost)
	{
		_energyManager = energyManager;
		_clanInfo = clanInfo;
		_energyToPurchase = energyToPurchase;
		_page = page;
		_cost = cost;
	}

	@Override
	public void onClick(final Player player, ClickType clickType)
	{
		if (!Recharge.Instance.use(player, "Attempt Buy Clans Shop Item", 1500, false, false))
		{
			return;
		}
		
		if (UtilServer.CallEvent(new PreEnergyShopBuyEvent(player, _energyToPurchase, _cost)).isCancelled())
		{
			return;
		}

		_page.getShop().openPageForPlayer(player, new ConfirmationPage<>(player, _page, new GoldPurchaseProcessor(player, _cost, _energyManager.getClansManager().getGoldManager(), () ->
		{
			_clanInfo.adjustEnergy(_energyToPurchase);
			_page.refresh();

			_energyManager.runAsync(() -> _energyManager.getClansManager().getClanDataAccess().updateEnergy(_clanInfo));

			// Notify
			_energyManager.getClansManager().messageClan(_clanInfo, F.main("Energy", F.name(player.getName()) + " purchased " + F.elem(_energyToPurchase + " Energy") + " for the clan"));
		}), ICON));
	}

}
