package mineplex.game.clans.tutorial.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.clans.tutorial.Tutorial;
import mineplex.game.clans.tutorial.TutorialManager;
import mineplex.game.clans.tutorial.gui.button.DeclineButton;
import mineplex.game.clans.tutorial.gui.button.StartButton;

public class TutorialSelectPage extends ShopPageBase<TutorialManager, TutorialShop>
{
	public TutorialSelectPage(TutorialManager plugin, TutorialShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player)
	{
		super(plugin, shop, clientManager, donationManager, shop.getTutorial().getName(), player, 45);

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		Tutorial tutorial = getShop().getTutorial();

		String name = tutorial.getName();
		Material material = tutorial.getGuiMaterial();
		byte data = tutorial.getGuiData();

		ShopItem infoItem = new ShopItem(material, data, name, new String[0], 0, false, false);
		addItem(13, infoItem);

		ShopItem startItem = new ShopItem(Material.EMERALD_BLOCK, "Start " + tutorial.getName(), new String[0], 0, false, false);
		addButton(27 + 6, startItem, new StartButton(tutorial));

		ShopItem declineButton = new ShopItem(Material.REDSTONE_BLOCK, "Cancel", new String[0], 0, false, false);
		addButton(27 + 2, declineButton, new DeclineButton());
	}

}
