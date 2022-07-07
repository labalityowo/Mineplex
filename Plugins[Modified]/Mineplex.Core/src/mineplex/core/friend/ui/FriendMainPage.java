package mineplex.core.friend.ui;

import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.donation.DonationManager;
import mineplex.core.friend.FriendManager;
import mineplex.core.friend.FriendStatusType;
import mineplex.core.friend.FriendVisibility;
import mineplex.core.friend.data.FriendStatus;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.portal.Intent;
import mineplex.core.recharge.Recharge;
import mineplex.core.shop.page.MultiPageManager;
import mineplex.core.shop.page.ShopPageBase;

public class FriendMainPage extends ShopPageBase<FriendManager, FriendShop>
{

	enum FriendPageType
	{
		MAIN(status -> status.Status == FriendStatusType.Accepted, new ItemBuilder(Material.SKULL_ITEM, (byte) 3)
				.setTitle(C.cGreenB + "Friends")
				.build()),
		REQUESTS(status -> status.Status == FriendStatusType.Pending || status.Status == FriendStatusType.Sent, new ItemBuilder(Material.RED_ROSE)
				.setTitle(C.cYellowB + "Friend Requests")
				.build()),
		DELETE(status -> status.Status == FriendStatusType.Accepted && !status.Favourite, new ItemBuilder(Material.TNT)
				.setTitle(C.cRedB + "Delete Friends")
				.build());

		private final Predicate<FriendStatus> Filter;
		private final ItemStack DisplayItem;

		FriendPageType(Predicate<FriendStatus> filter, ItemStack displayItem)
		{
			Filter = filter;
			DisplayItem = displayItem;
		}
	}

	private final MultiPageManager<FriendStatus> _pageManager;
	private FriendPageType _pageType;

	FriendMainPage(FriendManager plugin, FriendShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player)
	{
		super(plugin, shop, clientManager, donationManager, "Friends", player);

		_pageManager = new MultiPageManager<>(this, () -> getPlugin().Get(player).stream()
				.filter(_pageType.Filter)
				.sorted(FriendManager.getFriendSorter())
				.collect(Collectors.toList()), this::buildItem);
		_pageType = FriendPageType.MAIN;

		buildPage();
	}


	@Override
	protected void buildPage()
	{
		int slot = 0;

		for (FriendPageType pageType : FriendPageType.values())
		{
			ItemBuilder builder = new ItemBuilder(pageType.DisplayItem);

			if (pageType.equals(_pageType))
			{
				builder.setGlow(true);
			}

			addButton(slot, builder.build(), (player, clickType) ->
			{
				if (pageType.equals(_pageType))
				{
					playDenySound(player);
				}
				else
				{
					playAcceptSound(player);
					_pageType = pageType;
					_pageManager.setPage(0);
				}
			});

			slot += 2;
		}

		addButton(slot, new ItemBuilder(Material.BOOK_AND_QUILL)
				.setTitle(C.cAquaB + "Add Friends")
				.build(), (player, clickType) ->
		{
			new FriendAddPage(getPlugin(), player)
					.openInventory();
		});

		slot += 2;

		addButton(slot, new ItemBuilder(Material.SIGN)
				.setTitle(C.cDAquaB + "Toggle GUI")
				.addLore("", "Click to display your friends in", "chat instead of this chest.")
				.build(), (player, clickType) ->
		{
			getPlugin().showFriends(player, true);
			player.closeInventory();
		});

		slot = 47;

		FriendVisibility playerVisibility = getPlugin().getVisibility(getPlayer());

		for (FriendVisibility visibility : FriendVisibility.values())
		{
			addButton(slot, new ItemBuilder(visibility.getItemStack())
					.setGlow(visibility == playerVisibility)
					.build(), (player, clickType) ->
			{
				getPlugin().setVisibility(player, visibility);
				player.closeInventory();
			});

			slot += 2;
		}

		_pageManager.buildPage();
	}

	private void buildItem(FriendStatus status, int slot)
	{
		switch (_pageType)
		{
			case MAIN:
				buildMainItem(status, slot);
				break;
			case REQUESTS:
				buildRequestItem(status, slot);
				break;
			case DELETE:
				buildDeleteItem(status, slot);
				break;
		}
	}

	private void buildMainItem(FriendStatus status, int slot)
	{
		boolean online = status.isOnline();
		boolean canJoin = online && status.Visibility == FriendVisibility.SHOWN && !status.ServerName.equals(UtilServer.getServerName()) && getPlugin().canJoin(status.ServerName, getPlayer());

		ItemBuilder builder = new ItemBuilder(Material.SKULL_ITEM, (byte) (online ? 3 : 0))
				.setPlayerHead(status.Name)
				.setTitle(getColour(status) + status.Name)
				.addLore("");

		String statusString;

		if (online)
		{
			if (status.Visibility == FriendVisibility.PRESENCE)
			{
				statusString = C.cYellow + status.Visibility.getName();
			}
			else
			{
				statusString = C.cGreen + status.ServerName;
			}
		}
		else
		{
			statusString = C.cRed + "Offline";
		}

		builder.addLore("Where: " + statusString, "");

		if (online)
		{
			if (canJoin)
			{
				builder.addLore(C.cGreen + "Click to join their server.", "");
			}
		}
		else
		{
			builder.addLore("Last seen " + UtilTime.MakeStr(status.LastSeenOnline), "");
		}

		builder.addLore(
				"Shift-Click to " + (status.Favourite ? "remove them from" : "add them to"),
				"your favorite friends."
		);

		addButton(slot, builder.build(), (player, clickType) ->
		{
			if (!Recharge.Instance.use(player, "Friends Interact", 500, false, false))
			{
				playDenySound(player);
				return;
			}

			if (clickType.isShiftClick())
			{
				playAcceptSound(player);
				getPlugin().toggleFavourite(player, status.Name, () ->
				{
					status.Favourite = !status.Favourite;
					buildItem(status, slot);
				});
			}
			else
			{
				if (canJoin)
				{
					playAcceptSound(player);
					getPlugin().getPortal().sendPlayerToServer(player, status.ServerName, Intent.PLAYER_REQUEST);
				}
				else
				{
					playDenySound(player);
				}
			}
		});
	}

	private void buildRequestItem(FriendStatus status, int slot)
	{
		boolean sent = status.Status == FriendStatusType.Sent;
		ItemBuilder builder = new ItemBuilder(sent ? Material.ENDER_PEARL : Material.PAPER)
				.setTitle(C.cGray + "Request" + (sent ? C.cGray + " to " + C.cPurple : C.cGray + " from " + C.cAqua) + status.Name)
				.addLore("");

		if (sent)
		{
			builder.addLore("Shift-Click to cancel.");
		}
		else
		{
			builder.addLore(
					"Click to accept.",
					"Shift-Click to deny."
			);
		}

		addButton(slot, builder.build(), (player, clickType) ->
		{
			if (!Recharge.Instance.use(player, "Friends Interact", 500, false, false))
			{
				playDenySound(player);
				return;
			}

			setItem(slot, null);

			if (clickType.isShiftClick())
			{
				playRemoveSound(player);
				getPlugin().removeFriend(player, status.Name);
			}
			else
			{
				playAcceptSound(player);
				getPlugin().addFriend(player, status.Name);
			}
		});
	}

	private void buildDeleteItem(FriendStatus status, int slot)
	{
		boolean online = status.isOnline();

		ItemBuilder builder = new ItemBuilder(Material.SKULL_ITEM, (byte) (online ? 3 : 0))
				.setPlayerHead(status.Name)
				.setTitle((online ? C.cGreen : C.cRed) + status.Name)
				.addLore("Shift-Click to remove " + status.Name, "from your friends list.");

		addButton(slot, builder.build(), (player, clickType) ->
		{
			if (!clickType.isShiftClick() || !Recharge.Instance.use(player, "Friends Interact", 500, false, false))
			{
				playDenySound(player);
				return;
			}

			playRemoveSound(player);
			setItem(slot, null);
			getPlugin().removeFriend(player, status.Name);
		});
	}

	private String getColour(FriendStatus status)
	{
		String colour;

		if (status.Favourite)
		{
			colour = C.cYellow + "★ ";
		}
		else if (status.isOnline())
		{
			colour = C.cGreen;
		}
		else
		{
			colour = C.cRed;
		}

		return colour;
	}
}
