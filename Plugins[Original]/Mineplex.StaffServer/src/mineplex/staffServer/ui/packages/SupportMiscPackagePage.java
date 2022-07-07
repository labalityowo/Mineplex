package mineplex.staffServer.ui.packages;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClient;
import mineplex.core.common.util.UtilMath;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.staffServer.customerSupport.CustomerSupport;
import mineplex.staffServer.ui.SupportPage;
import mineplex.staffServer.ui.SupportShop;

public class SupportMiscPackagePage extends SupportPackagePage
{
	public SupportMiscPackagePage(CustomerSupport plugin, SupportShop shop, Player player, CoreClient target, SupportPage previousPage)
	{
		super(plugin, shop, player, target, previousPage, "Packages");

		_packageList.add(getPackagePair(
				new ItemBuilder(Material.WOOL).setData((byte) (UtilMath.r(14) + 1)),
				"Rainbow Bundle",
				"Rainbow Arrows", "Rainbow Death", "Rainbow Leap", "Rainbow Aura"
		));
		_packageList.add(getPackagePair(Material.SNOW_BALL,"Frost Lord"));
		_packageList.add(getPackagePair(Material.RABBIT_FOOT,"Easter Bunny Morph"));
		_packageList.add(getPackagePair(Material.RED_ROSE,"Valentines Gift"));
		_packageList.add(getPackagePair(Material.EMERALD, "Game Amplifier", "Game Booster"));

		_itemPackages.add("Valentines Gift");
		_itemPackages.add("Game Booster");

		addToItemLores();

		buildPage();
	}
}
