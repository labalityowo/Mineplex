package mineplex.staffServer.ui;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import mineplex.core.account.CoreClient;
import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.shop.confirmation.ConfirmationCallback;
import mineplex.core.shop.confirmation.ConfirmationPage;
import mineplex.core.shop.confirmation.ConfirmationProcessor;
import mineplex.core.shop.item.ShopItem;
import mineplex.staffServer.customerSupport.CustomerSupport;
import mineplex.staffServer.ui.chest.SupportChestPage;
import mineplex.staffServer.ui.currency.SupportCurrencyPage;
import mineplex.staffServer.ui.packages.SupportClansPackagePage;
import mineplex.staffServer.ui.packages.SupportMiscPackagePage;
import mineplex.staffServer.ui.pet.SupportPetPage;
import mineplex.staffServer.ui.ppc.SupportPowerplayPage;
import mineplex.staffServer.ui.rank.SupportRankBonusPage;
import mineplex.staffServer.ui.rank.SupportRankListPage;

public class SupportHomePage extends SupportPage
{
	public SupportHomePage(CustomerSupport plugin, SupportShop shop, Player player, CoreClient target)
	{
		super(plugin, shop, player, target, null);

		buildPage();
	}

	private void buildUnavailableButton(int slot)
	{
		addItem(slot, new ShopItem(Material.BARRIER, C.cRedB + "Unavailable", new String[]
		{
				C.cRed + "Sorry, but this button is",
				C.cRed + "not currently available.",
				C.cGray + " ",
				C.cGray + "Please contact a developer",
				C.cGray + "to get it fixed."
		}, 1, true, true));
	}

	private void buildPageButton(int slot, ShopItem shopItem, Class<? extends SupportPage> clazz)
	{
		Constructor pageConstructor;
		try
		{
			pageConstructor = clazz.getConstructor(CustomerSupport.class, SupportShop.class, Player.class, CoreClient.class, SupportPage.class);
		}
		catch (NoSuchMethodException e)
		{
			System.out.println("Could not get page constructor:");
			e.printStackTrace();
			buildUnavailableButton(slot);
			return;
		}

		addButton(slot, shopItem, (p, c) ->
		{
			try
			{
				getShop().openPageForPlayer(getPlayer(), (SupportPage) pageConstructor.newInstance(getPlugin(), getShop(), getPlayer(), _target, this));
			}
			catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
			{
				System.out.println("Could not instantiate page:");
				e.printStackTrace();
				buildUnavailableButton(slot);
			}
		});
	}

	private void buildPageButton(int slot, ShopItem item, Class<? extends SupportPage> clazz, Supplier<Boolean> isValid)
	{
		if (isValid.get())
		{
			buildPageButton(slot, item, clazz);
		}
		else
		{
			buildUnavailableButton(slot);
		}
	}

	@Override
	protected void buildPage()
	{
		super.buildPage();

		buildPageButton(getSlotIndex(1, 2), new ShopItem(Material.CHEST, "Chests", new String[]
		{
				C.mBody + "Click to view or add",
				C.mBody + "chests for " + C.cYellow + _target.getName()
		}, 1, false, true), SupportChestPage.class);

		buildPageButton(getSlotIndex(1, 4), new ShopItem(Material.EMERALD, "Currency", new String[]
		{
				C.mBody + "Click to view Gem or",
				C.mBody + "Shard packages for " + C.cYellow + _target.getName()
		}, 1, false, true), SupportCurrencyPage.class);

		buildPageButton(getSlotIndex(1, 6), new ShopItem(Material.GOLD_INGOT, "PowerPlay Club", new String[]
		{
				C.mBody + "Click to view PowerPlay",
				C.mBody + "Club info for " + C.cYellow + _target.getName()
		}, 1, false, true), SupportPowerplayPage.class, () -> getShop().getPowerPlayData().get(_target.getAccountId()) != null);

		buildPageButton(getSlotIndex(3, 2), new ShopItem(Material.ENCHANTED_BOOK, "Rank Bonus", new String[]
		{
				C.mBody + "Click to view rank",
				C.mBody + "bonus log for " + C.cYellow + _target.getName()
		}, 1, false, true), SupportRankBonusPage.class, () -> getShop().getBonusLog().get(_target.getAccountId()) != null);

		buildPageButton(getSlotIndex(3, 4), new ShopItem(Material.BOOK_AND_QUILL, "Rank Utilities", new String[]
		{
				C.mBody + "Click to view ranks",
				C.mBody + "and monthly bonus",
				C.mBody + "items for " + C.cYellow + _target.getName()
		}, 1, false, true), SupportRankListPage.class);

		buildPageButton(getSlotIndex(3, 6), new ShopItem(Material.BONE, "Pets", new String[]
		{
				C.mBody + "Click to view pets",
				C.mBody + "for " + C.cYellow + _target.getName()
		}, 1, false, true), SupportPetPage.class);

		buildPageButton(getSlotIndex(5, 2), new ShopItem(Material.HAY_BLOCK, "Misc Packages", new String[]
		{
				C.mBody + "Click to view misc",
				C.mBody + "packages, such as",
				C.mBody + "Frost Lord and",
				C.mBody + "Rainbow bundles."
		}, 1, false, true), SupportMiscPackagePage.class);

		buildPageButton(getSlotIndex(5, 4), new ShopItem(Material.IRON_SWORD, "Clans Packages", new String[]
		{
				C.mBody + "Click to view Clans",
				C.mBody + "packages, such as",
				C.mBody + "Boss Tokens and",
				C.mBody + "Supply Drops."
		}, 1, false, true), SupportClansPackagePage.class);

		addButton(getSlotIndex(5, 6),
				new ItemBuilder(Material.WOOD_DOOR)
					.setTitle(C.cGreenB + "Apply Kits")
					.addLore(C.mBody + "Click to apply all")
					.addLore(C.mBody + "kits to " + C.cYellow + _target.getName())
					.build(),
				(p, c) ->
						getShop().openPageForPlayer(getPlayer(), new ConfirmationPage<>(
								getPlayer(),
								this,
								new ConfirmationProcessor()
								{
									@Override
									public void init(Inventory inventory)
									{

									}

									@Override
									public void process(ConfirmationCallback callback)
									{
										getPlugin().getDonationManager().applyKits(_target.getName());
										message("You gave all kits to " + C.cYellow + _target.getName());
										playSuccess();
									}
								},
								new ItemBuilder(Material.WOOD_DOOR)
										.setTitle(C.cGreenB + "Confirm Apply All Kits")
										.build()
						))
		);
	}
}
