package mineplex.game.clans.tutorial.gui;

import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.clans.tutorial.Tutorial;
import mineplex.game.clans.tutorial.TutorialManager;

public class TutorialShop extends ShopBase<TutorialManager>
{
	private final Tutorial _tutorial;

	public TutorialShop(TutorialManager plugin, CoreClientManager clientManager, DonationManager donationManager, Tutorial tutorial)
	{
		super(plugin, clientManager, donationManager, tutorial.getName());

		_tutorial = tutorial;
	}

	@Override
	protected ShopPageBase<TutorialManager, ? extends ShopBase<TutorialManager>> buildPagesFor(Player player)
	{
		return new TutorialSelectPage(getPlugin(), this, getClientManager(), getDonationManager(), player);
	}

	public Tutorial getTutorial()
	{
		return _tutorial;
	}
}
