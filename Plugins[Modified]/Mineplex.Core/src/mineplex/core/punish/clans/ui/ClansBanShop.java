package mineplex.core.punish.clans.ui;

import org.bukkit.entity.Player;

import mineplex.core.Managers;
import mineplex.core.donation.DonationManager;
import mineplex.core.punish.clans.ClansBanClient;
import mineplex.core.punish.clans.ClansBanManager;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;

public class ClansBanShop extends ShopBase<ClansBanManager>
{
	private final String _clientName;
	private final ClansBanClient _client;
	private final String _reason;

	public ClansBanShop(final ClansBanManager plugin, String victimName, ClansBanClient client, String reason)
	{
		super(plugin, plugin.getClientManager(), Managers.get(DonationManager.class), "Clans Punish");
		_clientName = victimName;
		_client = client;
		_reason = reason;
	}
	
	@Override
	protected ShopPageBase<ClansBanManager, ? extends ShopBase<ClansBanManager>> buildPagesFor(final Player player)
	{
		return new ClansBanPage(getPlugin(), this, "Clans Punish", player, _clientName, _client, _reason);
	}	
}