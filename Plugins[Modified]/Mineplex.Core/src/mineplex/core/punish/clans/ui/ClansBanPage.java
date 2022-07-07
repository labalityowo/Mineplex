package mineplex.core.punish.clans.ui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.Managers;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.donation.DonationManager;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.punish.clans.ClansBan;
import mineplex.core.punish.clans.ClansBanClient;
import mineplex.core.punish.clans.ClansBanManager;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.core.slack.SlackAPI;
import mineplex.core.slack.SlackMessage;
import mineplex.core.slack.SlackTeam;

public class ClansBanPage extends ShopPageBase<ClansBanManager, ClansBanShop>
{
	private long _time;
	private boolean _permanent;

	private String _victimName;
	private ClansBanClient _victimClient;

	private String _reason;
	
	public ClansBanPage(final ClansBanManager banManager, final ClansBanShop shop, final String name, final Player player, String victimName, ClansBanClient client, String reason)
	{
		super(banManager, shop, banManager.getClientManager(), Managers.get(DonationManager.class), name, player);

		_reason = reason;

		_victimName = victimName;
		_victimClient = client;

		buildPage();
	}
	
	protected void buildPage()
	{
		_time = Math.max(0, _time);
		
		int slot = 27;
		
		// Middle of first row
		addButton(4, new ItemBuilder(Material.SKULL_ITEM)
							.setData((short) 3)
							.setPlayerHead(_victimName)
							.setTitle(C.cDGreenB + _victimName)
							.addLore(" ")
							.addLore(C.cYellow + _reason).build(), (player, click) -> {});
		
		addTimeAdjuster((9 * 1 + 2), -(1000l * 60l * 60l));
		addTimeAdjuster((9 * 1 + 1), -(1000l * 60l * 60l * 24l));
		addTimeAdjuster((9 * 1 + 0), -(1000l * 60l * 60l * 24l * 30l));
		addTimeAdjuster((9 * 1 + 6), (1000l * 60l * 60l));
		addTimeAdjuster((9 * 1 + 7), (1000l * 60l * 60l * 24l));
		addTimeAdjuster((9 * 1 + 8), (1000l * 60l * 60l * 24l * 30l));
		
		addButton((9 * 1) + 4,
					new ItemBuilder(Material.RECORD_5)
						.setTitle(C.cRedB + "Ban Player")
						.setLore(
							" ",
							C.cGray + "Player: " + F.elem(_victimName),
							C.cGray + "Reason: " + F.elem(_reason),
							C.cGray + "Time: " + F.elem(_permanent ? "Permanent" : UtilTime.MakeStr(_time)),
							"",
							C.cRed + C.Italics + "Left-Click to BAN PLAYER",
							C.cGray + C.Italics + "Right-Click to toggle permanent ban setting"
						).build(),
		(player, click) ->
		{
			if (click == ClickType.RIGHT)
			{
				_permanent = !_permanent;
				refresh();
			}
			else
			{
				performBan();
			}
		});
		
		for (ClansBan ban : _victimClient._bans)
		{
			ItemStack item;
			if (ban.isPermanent())
			{
				item = new ItemBuilder(ban.isActive() ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK)
							.setTitle(ban.isActive() ? C.cGreenB + "Active" : C.cRedB + "Inactive")
							.addLore(" ")
							.addLore(C.cGray + "Date Banned: " + C.cYellow + UtilTime.date(ban.getBanTime().getTime()))
							.addLore(C.cGray + "Admin: " + C.cYellow + ban.getAdmin())
							.addLore(C.cGray + "Permanent: " + C.cYellow + (ban.isPermanent() ? "Yes" : "No"))
							.addLore(C.cGray + "Reason: " + C.cYellow + ban.getReason(), 16)
							.addLore(C.cGray + "Removed: " + C.cYellow + (ban.isRemoved() ? "Yes" : "No"))
							.addLore(ban.getRemoveAdmin() != null ? C.cGray + "Removed By: " + C.cYellow + ban.getRemoveAdmin() : null)
							.addLore(ban.getRemoveReason() != null ? C.cGray + "Remove Reason: " + C.cYellow + ban.getRemoveReason() : null)
							.addLore(ban.isActive() ? " " : null)
							.addLore(ban.isActive() ? C.cDAqua + "Left-Click to Remove Ban" : null)
							.setGlow(ban.isActive())
						.build();
			}
			else
			{
				item = new ItemBuilder(ban.isActive() ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK)
							.setTitle(ban.isActive() ? C.cGreenB + "Active" : C.cRedB + "Inactive")
							.addLore(" ")
							.addLore(C.cGray + "Date Banned: " + C.cYellow + UtilTime.date(ban.getBanTime().getTime()))
							.addLore(C.cGray + "Admin: " + C.cYellow + ban.getAdmin())
							.addLore(C.cGray + "Time Left: " + C.cYellow + (ban.isActive() ? ban.getBanTimeFormatted(false) : "None"))
							.addLore(C.cGray + "Permanent: " + C.cYellow + (ban.isPermanent() ? "Yes" : "No"))
							.addLore(C.cGray + "Reason: " + C.cYellow + ban.getReason(), 16)
							.addLore(C.cGray + "Removed: " + C.cYellow + (ban.isRemoved() ? "Yes" : "No"))
							.addLore(ban.getRemoveAdmin() != null ? C.cGray + "Removed By: " + C.cYellow + ban.getRemoveAdmin() : null)
							.addLore(ban.getRemoveReason() != null ? C.cGray + "Remove Reason: " + C.cYellow + ban.getRemoveReason() : null)
							.addLore(ban.isActive() ? " " : null)
							.addLore(ban.isActive() ? C.cDAqua + "Left-Click to Remove Ban" : null)
							.setGlow(ban.isActive())
						.build();
			}
			
			addButton(slot++, item, (player, click) ->
			{
				if (ban.isActive())
				{
					getPlugin().unban(_victimClient, ban, getPlayer().getName(), _reason, () ->
					{
						playAcceptSound(player);
						if (!UtilServer.isTestServer())
						{
							SlackAPI.getInstance().sendMessage(SlackTeam.DEVELOPER, "#clans-commandspy",
									new SlackMessage("Clans Command Logger", "crossed_swords", getPlayer().getName() + " has removed a blacklist from " + _victimName + " for '" + _reason + "'."),
									true);
						}
						refresh();
					});
				}
			});
		}
	}
	
	private void performBan()
	{
		if (_time == 0 && !_permanent)
		{
			playDenySound(getPlayer());
			return;
		}
		getPlugin().ban(_victimClient, _victimName, getPlayer().getName(), _permanent ? -1 : _time, _reason, getPlayer(), ban ->
		{
			if (ban.isPresent())
			{
				playAcceptSound(getPlayer());
				if (!UtilServer.isTestServer())
				{
					SlackAPI.getInstance().sendMessage(SlackTeam.DEVELOPER, "#clans-commandspy",
							new SlackMessage("Clans Command Logger", "crossed_swords", getPlayer().getName() + " has blacklisted " + _victimName + " for '" + _reason + "'. Duration: " + UtilTime.MakeStr(_permanent ? -1 : _time)),
							true);
				}
				refresh();
			}
			else
			{
				playDenySound(getPlayer());
			}
		});
	}

	private void addTimeAdjuster(int index, long time)
	{
		addButton(index, new ItemBuilder(Material.PAPER).setTitle(C.cRed + (time < 0 ? "-" : "") + UtilTime.MakeStr(Math.abs(time))).build(),
			(player, click) ->
			{
				_time += time;
				refresh();
			});
	}
}