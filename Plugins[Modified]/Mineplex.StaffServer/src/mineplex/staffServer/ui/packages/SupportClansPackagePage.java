package mineplex.staffServer.ui.packages;

import java.util.Map;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClient;
import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.staffServer.customerSupport.CustomerSupport;
import mineplex.staffServer.ui.SupportPage;
import mineplex.staffServer.ui.SupportShop;

public class SupportClansPackagePage extends SupportPackagePage
{
	private Map<String, Integer> _receivedClansPackages;

	public SupportClansPackagePage(CustomerSupport plugin, SupportShop shop, Player player, CoreClient target, SupportPage previousPage)
	{
		super(plugin, shop, player, target, previousPage, "Clans");

		// Black banner, for some reason
		addClanBanner(DyeColor.ORANGE, "Editor");
		addClanBanner(DyeColor.RED, "Usage");

		addClansBossToken(new ItemBuilder(Material.BONE), "Skeleton");
		addClansBossToken(new ItemBuilder(Material.BLAZE_ROD), "Wizard");

		// These are backwards colors on purpose, for some reason it works.
		addClansBox(new ItemBuilder(Material.INK_SACK).setData(DyeColor.CYAN.getDyeData()), "Dye");
		addClansBox(new ItemBuilder(Material.INK_SACK).setData(DyeColor.YELLOW.getDyeData()), "Gilded Dye");
		addClansBox(new ItemBuilder(Material.IRON_PICKAXE), "Builder");

		addClansSupplyDrop(new ItemBuilder(Material.CHEST), false);
		addClansSupplyDrop(new ItemBuilder(Material.ENDER_CHEST), true);

		addRuneAmplifier(20);
		addRuneAmplifier(60);

		addToItemLores();

		buildPage();
	}

	private void addClanBanner(DyeColor color, String unlock)
	{
		String packageName = "Clan Banner " + unlock;

		_packageList.add(getPackagePair(
				new ItemBuilder(Material.BANNER)
						.setData(color.getData())
						.addLore(C.mBody + "Is Active: " + (ownsSalesPackage(packageName) ? C.cGreen + "Yes" : C.cRed + "No"))
						.addLore(""),
				"Banner " + unlock,
				packageName
		));
	}

	private void addClansBox(ItemBuilder builder, String box)
	{
		String name = "Clans " + box + " Box";
		_packageList.add(getPackagePair(builder, box + " Box", name));
		_itemPackages.add(name);
	}

	private void addClansBossToken(ItemBuilder builder, String boss)
	{
		String packageName = "Clans Boss Token " + boss;
		_packageList.add(getPackagePair(builder, boss + " Boss Token", packageName));
		_itemPackages.add(packageName);
	}

	private void addClansSupplyDrop(ItemBuilder builder, boolean gilded)
	{
		String packageName = "Clans " + (gilded ? "Gilded " : "") + "Supply Drop";
		_packageList.add(getPackagePair(builder, (gilded ? "Gilded " : "") + "Supply Drop", packageName));
		_itemPackages.add(packageName);
	}

	private void addRuneAmplifier(int minutes)
	{
		String packageName = "Rune Amplifier " + minutes;
		// In case we ever add a >64min amplifier
		_packageList.add(getPackagePair(new ItemBuilder(Material.NETHER_STAR).setAmount(Math.min(minutes, 64)), minutes + " Minute Rune Amplifier", packageName));
		_itemPackages.add(packageName);
	}
}
