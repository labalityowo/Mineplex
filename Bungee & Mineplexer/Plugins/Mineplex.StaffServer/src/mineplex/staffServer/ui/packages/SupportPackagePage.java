package mineplex.staffServer.ui.packages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mineplex.core.account.CoreClient;
import mineplex.core.common.Pair;
import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.shop.page.MultiPageManager;
import mineplex.staffServer.customerSupport.CustomerSupport;
import mineplex.staffServer.ui.SupportPage;
import mineplex.staffServer.ui.SupportShop;

public class SupportPackagePage extends SupportPage
{
	private MultiPageManager<Pair<ItemStack, List<String>>> _multiPageManager;

	// Add items to this list for them to display on the page.
	protected List<Pair<ItemStack, List<String>>> _packageList;

	// Add package names to this list if they should be given
	// as items when the above package is clicked.
	protected List<String> _itemPackages;

	protected Map<String, Integer> _receivedPackages;

	public SupportPackagePage(CustomerSupport plugin, SupportShop shop, Player player, CoreClient target, SupportPage previousPage, String name)
	{
		super(plugin, shop, player, target, previousPage, name);

		_multiPageManager = new MultiPageManager<>(this, this::getPackageList, this::addPackage);
		_packageList = new LinkedList<>();
		_itemPackages = new ArrayList<>();
	}

	protected Pair<ItemStack, List<String>> getPackagePair(ItemBuilder builder, String name, String... packages)
	{
		builder.setTitle(C.cGreenB + name)
				.addLore(C.mBody + "Click to give the")
				.addLore(C.mBody + "following items:");

		for (String packageName : packages)
		{
			builder.addLore("  " + C.cYellow + packageName);
		}

		return Pair.create(builder.build(), Arrays.asList(packages));
	}

	protected Pair<ItemStack, List<String>> getPackagePair(Material mat, String name, String... packages)
	{
		return getPackagePair(new ItemBuilder(mat), name, packages);
	}

	protected Pair<ItemStack, List<String>> getPackagePair(Material mat, String name)
	{
		return getPackagePair(mat, name, name);
	}

	protected Pair<ItemStack, List<String>> getPackagePair(ItemBuilder builder, String name)
	{
		return getPackagePair(builder, name, name);
	}

	private void addPackage(Pair<ItemStack, List<String>> item, int slot)
	{
		// Only 1 item, and it's an item type
		if (item.getRight().size() == 1 && _itemPackages.contains(item.getRight().get(0)))
		{
			addButton(slot, item.getLeft(), (p, c) ->
			{
				getShop().openPageForPlayer(getPlayer(),
						new SupportGivePackagePage(
								getPlugin(),
								getShop(),
								getPlayer(),
								_target,
								this,
								new ItemBuilder(item.getLeft().getType()).setData(item.getLeft().getData().getData()).setTitle(item.getLeft().getItemMeta().getDisplayName()),
								item.getRight().get(0)
						));
			});
		}
		else
		{
			addButton(slot, item.getLeft(), (p, c) ->
			{
				for (String packageName : item.getRight())
				{
					giveUnknownSalesPackage(packageName, _itemPackages.contains(packageName));
				}
			});
		}
	}

	private List<Pair<ItemStack, List<String>>> getPackageList()
	{
		return _packageList;
	}

	/**
	 * Call this method once all items
	 * have been added to the _packageList
	 * map so that they can be processed.
	 */
	protected void addToItemLores()
	{
		List<String> allPackages = new ArrayList<>();
		_packageList.forEach(pair -> allPackages.addAll(pair.getRight()));
		_receivedPackages = getPackageOwnership(allPackages);

		_packageList.forEach(pair ->
		{
			// Only process item packages, i.e. ones that
			// can be received multiple times
			List<String> packages = pair.getRight().stream().filter(p -> _itemPackages.contains(p)).collect(Collectors.toList());

			if (packages.size() == 0)
			{
				return;
			}

			ItemStack item = pair.getLeft();

			List<String> newLore = new ArrayList<>();

			newLore.addAll(Arrays.asList(
					C.mBody + "This player has",
					C.mBody + "received:"
			));

			for (String packageName : packages)
			{
				newLore.add("  " + C.cYellow + _receivedPackages.computeIfAbsent(packageName, k -> 0) + " " + C.cWhite + packageName);
			}

			newLore.add("");

			ItemMeta meta = item.getItemMeta();

			newLore.addAll(meta.getLore());

			meta.setLore(newLore);

			item.setItemMeta(meta);
		});
	}

	@Override
	protected void buildPage()
	{
		super.buildPage();

		_multiPageManager.buildPage();
	}
}
