package nautilus.game.arcade.game.games.wizards;

import mineplex.core.account.CoreClientManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;
import org.bukkit.entity.Player;

public class WizardSpellMenuShop extends ShopBase<WizardSpellMenu>
{
	private Wizards _wizards;

	public WizardSpellMenuShop(WizardSpellMenu plugin, CoreClientManager clientManager, DonationManager donationManager,
			Wizards wizard)
	{
		super(plugin, clientManager, donationManager, "Kit Evolve Menu");
		_wizards = wizard;
	}

	@Override
	protected ShopPageBase<WizardSpellMenu, ? extends ShopBase<WizardSpellMenu>> buildPagesFor(Player player)
	{
		return new SpellMenuPage(getPlugin(), this, getClientManager(), getDonationManager(), player, _wizards);
	}

	public void update()
	{
		getPlayerPageMap().values().forEach(ShopPageBase::refresh);
	}
}
