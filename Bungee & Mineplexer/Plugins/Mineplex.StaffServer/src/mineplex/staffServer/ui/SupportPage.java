package mineplex.staffServer.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClient;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilSkull;
import mineplex.core.common.util.UtilText;
import mineplex.core.donation.Donor;
import mineplex.core.donation.repository.token.TransactionToken;
import mineplex.core.inventory.ClientInventory;
import mineplex.core.inventory.ClientItem;
import mineplex.core.server.util.TransactionResponse;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.staffServer.customerSupport.CustomerSupport;

public class SupportPage extends ShopPageBase<CustomerSupport, SupportShop>
{
	protected CoreClient _target;
	protected Donor _donor;
	protected SupportPage _previousPage;

	public SupportPage(CustomerSupport plugin, SupportShop shop, Player player, CoreClient target, SupportPage previousPage, String name)
	{
		super(plugin, shop, plugin.getClientManager(), plugin.getDonationManager(), name + " - " + target.getName(), player);

		_target = target;
		_donor = getPlugin().getDonationManager().Get(_target.getUniqueId());
		_previousPage = previousPage;
	}

	public SupportPage(CustomerSupport plugin, SupportShop shop, Player player, CoreClient target, SupportPage previousPage)
	{
		this(plugin, shop, player, target, previousPage, "Support");
	}

	protected int getSlotIndex(int rowIndex, int columnIndex)
	{
		return (rowIndex * 9) + columnIndex;
	}

	protected Map<String, Integer> getPackageOwnership(List<String> validNames, Predicate<TransactionToken> allowed)
	{
		Map<String, Integer> ownership = new HashMap<>();

		for (TransactionToken transaction : _donor.getTransactions())
		{
			if (!allowed.test(transaction))
			{
				continue;
			}

			if (validNames.contains(transaction.SalesPackageName))
			{
				ownership.putIfAbsent(transaction.SalesPackageName, 0);
				ownership.compute(transaction.SalesPackageName, (treasureType, count) -> count + 1);
				continue;
			}

			// Otherwise, try to see if everything but the last arg fits...
			String[] split = transaction.SalesPackageName.split(" ");

			String newName = Arrays.asList(split).subList(0, split.length - 1).stream().collect(Collectors.joining(" "));

			if (validNames.contains(newName))
			{
				int toAdd;
				try
				{
					toAdd = Integer.parseInt(split[split.length - 1]);
				}
				catch (NumberFormatException ex)
				{
					// nope
					continue;
				}

				ownership.putIfAbsent(newName, 0);
				ownership.compute(newName, (treasureType, count) -> count + toAdd);
			}
		}

		return ownership;
	}

	public Map<String, Integer> getPackageOwnership(List<String> names)
	{
		return getPackageOwnership(names, (transactionToken -> transactionToken.Coins == 0 && transactionToken.Gems == 0));
	}

	private List<String> getBasicPlayerInfo()
	{
		List<String> info = new ArrayList<>();

		info.add("");
		info.add(C.mBody + "Rank: " + _target.getPrimaryGroup().getDisplay(true, true, true, true));
		info.addAll(UtilText.splitLine(C.mBody + "Subranks: " + C.cYellow + "(" + _target.getAdditionalGroups().size() + ") "
				+ _target.getAdditionalGroups()
				.stream()
				.map(g -> g.getDisplay(false, false, false, true).toLowerCase())
				.collect(Collectors.joining(", ")), LineFormat.LORE));
		info.add("");
		info.add(C.mBody + "Gems: " + C.cGreen + _donor.getBalance(GlobalCurrency.GEM));
		info.add(C.mBody + "Shards: " + C.cAqua + _donor.getBalance(GlobalCurrency.TREASURE_SHARD));

		return info;
	}

	private void buildPlayerSkull()
	{
		addItem(4, UtilSkull.getPlayerHead(_target.getName(), C.cGreenB + _target.getName(), getBasicPlayerInfo()));
	}

	protected void playSuccess()
	{
		_player.playSound(_player.getLocation(), Sound.LEVEL_UP, 1F, 1F);
	}

	protected void playFail()
	{
		_player.playSound(_player.getLocation(), Sound.ITEM_BREAK, 1F, 1F);
	}

	protected void goBack()
	{
		_previousPage.refresh();
		getShop().openPageForPlayer(getPlayer(), _previousPage);
	}

	private void buildBackButton()
	{
		if (_previousPage == null)
		{
			return;
		}

		addButton(0, new ShopItem(Material.BED, "Go Back", new String[0], 1, false, true), (p, c)-> goBack());
	}

	protected void message(String message)
	{
		_player.sendMessage(F.main(getPlugin().getName(), message));
	}

	private void giveSuccess(int amount, String itemName)
	{
		message("Gave " + C.cYellow + amount + " " + itemName + C.mBody + " to " + C.cYellow + _target.getName());
	}

	private void giveFailed(int amount, String itemName)
	{
		message("Unable to give " + C.cYellow + amount + " " + itemName + C.mBody + " to " + C.cYellow + _target.getName() + C.mBody + ", please try again later.");
	}

	private void addItemsToClient(int amount, String itemName)
	{
		ClientInventory clientInventory = getPlugin().getInventoryManager().Get(_target.getUniqueId());

		clientInventory.addItem(new ClientItem(getPlugin().getInventoryManager().getItem(itemName), amount));
	}

	protected void giveUnknownSalesPackage(int amount, String itemName, boolean isItem, Consumer<Boolean>callback)
	{
		getPlugin().getDonationManager().purchaseUnknownSalesPackage(_target, (amount == 1 ? itemName : itemName + " " + amount), GlobalCurrency.GEM, 0, false, response ->
		{
			if (response == TransactionResponse.Failed || response == TransactionResponse.InsufficientFunds)
			{
				giveFailed(amount, itemName);
				callback.accept(false);
			}
			else if (response == TransactionResponse.Success)
			{
				if (isItem)
				{
					getPlugin().getInventoryManager().addItemToInventoryForOffline(success ->
					{
						if (success)
						{
							giveSuccess(amount, itemName);
							addItemsToClient(amount, itemName);
							callback.accept(true);
						}
						else
						{
							giveFailed(amount, itemName);
							callback.accept(false);
						}
					}, _target.getAccountId(), itemName, amount);
				}
				else
				{
					giveSuccess(amount, itemName);
					addItemsToClient(amount, itemName);
					callback.accept(true);
				}
			}
			else if (response == TransactionResponse.AlreadyOwns)
			{
				message(C.cYellow + _target.getName() + C.mBody + " already owns " + C.cYellow + itemName);
				callback.accept(true);
			}
		});
	}

	protected void giveUnknownSalesPackage(String itemName, boolean isItem, Consumer<Boolean> callback)
	{
		giveUnknownSalesPackage(1, itemName, isItem, callback);
	}

	protected void giveUnknownSalesPackage(String itemName, boolean isItem)
	{
		giveUnknownSalesPackage(itemName, isItem, success -> {});
	}

	protected void giveUnknownSalesPackage(String itemName)
	{
		giveUnknownSalesPackage(itemName, false);
	}

	protected boolean ownsSalesPackage(String packageName)
	{
		return _donor.ownsUnknownSalesPackage(packageName);
	}

	protected int getOwnedCount(String packageName)
	{
		return getPlugin().getInventoryManager().Get(_target.getUniqueId()).getItemCount(packageName);
	}

	@Override
	protected void buildPage()
	{
		buildPlayerSkull();
		buildBackButton();
	}
}
