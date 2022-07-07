package mineplex.core.tournament.ui.page;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.account.CoreClientManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.shop.item.IButton;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.core.tournament.TournamentManager;
import mineplex.core.tournament.ui.TournamentShop;

public class MainMenu extends ShopPageBase<TournamentManager, TournamentShop>
{
	private static IButton _friendsButton = null;
	private static IButton _tournamentsButton = null;
	
	public MainMenu(TournamentManager plugin, TournamentShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player)
	{
		super(plugin, shop, clientManager, donationManager, "Tournament Menu", player, 9);
		
		if (_friendsButton == null)
		{
			_friendsButton = new IButton() 
			{
				@Override
				public void onClick(Player player, ClickType clickType)
				{
					shop.openPageForPlayer(player, new SoloTournamentMenu(plugin, shop, clientManager, donationManager, player));
				} 
			};
		}
		
		if (_tournamentsButton == null)
		{
			_tournamentsButton = new IButton() 
			{
				@Override
				public void onClick(Player player, ClickType clickType)
				{
					shop.openPageForPlayer(player, new TournamentsMenu(plugin, shop, clientManager, donationManager, player));
				} 
			};
		}
		
		buildPage();
	}

	@Override
	protected void buildPage()
	{
		addButton(0, new ItemBuilder(Material.SKULL_ITEM, 1, (short) 3).setTitle("Friends").build(), _friendsButton);
		addButton(1, new ItemBuilder(Material.CHEST, 1).setTitle("Tournaments").build(), _tournamentsButton);
	}
}
