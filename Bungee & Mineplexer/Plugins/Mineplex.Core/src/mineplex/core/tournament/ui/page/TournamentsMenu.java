package mineplex.core.tournament.ui.page;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.donation.DonationManager;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.shop.item.IButton;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.core.tournament.TournamentManager;
import mineplex.core.tournament.data.Tournament;
import mineplex.core.tournament.ui.TournamentShop;
import net.md_5.bungee.api.ChatColor;

public class TournamentsMenu extends ShopPageBase<TournamentManager, TournamentShop>
{
	private static Calendar _calendar = Calendar.getInstance();
	
	private static SimpleDateFormat _tournamentStart = new SimpleDateFormat("h:mma z");
	private static SimpleDateFormat _tournamentCountdown = new SimpleDateFormat("h:mm:ss");
	
	public TournamentsMenu(TournamentManager plugin, TournamentShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player)
	{
		super(plugin, shop, clientManager, donationManager, "Tournament Calender", player, 45);
		
		buildPage();
	}

	@Override
	protected void buildPage()
	{
		NautHashMap<Integer, Tournament> tournamentDateMap = new NautHashMap<>();
		
		for (Tournament tournament : getPlugin().getTournaments())
		{
			_calendar.setTime(new Date(tournament.Date));
			
			tournamentDateMap.put(_calendar.get(Calendar.DAY_OF_MONTH), tournament);
		}
		
		int currentDayOfMonth = _calendar.get(Calendar.DAY_OF_MONTH);
		int maxDaysThisMonth = _calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		
		_calendar.set(Calendar.DAY_OF_MONTH, 1);
		int startDayOfMonth = _calendar.get(Calendar.DAY_OF_WEEK);		
		int slot = startDayOfMonth;
		
		for (int i=1; i <= maxDaysThisMonth; i++)
		{
			if ((slot + 1) % 9 == 0)
				slot += 2;
			
			if (tournamentDateMap.containsKey(i))
			{
				final Tournament tournament = tournamentDateMap.get(i);
				boolean registered = getPlugin().Get(getPlayer()).Tournaments.containsKey(tournament.TournamentId);
				
				addButton(slot, new ItemBuilder(Material.PAPER, i).setTitle(ChatColor.RESET + "" + ChatColor.BOLD + ChatColor.GOLD + tournament.Name).setLore
				(
					ChatColor.GRAY + tournament.GameType, " ", 
					ChatColor.WHITE + "Take part in a super competitive", 
					ChatColor.WHITE + "tournament between all kinds of", 
					ChatColor.WHITE + "people in the Mineplex community!",
					registered ? ChatColor.GREEN + "" + ChatColor.BOLD + "You are registered for this tournament!" : " ",
					ChatColor.GRAY + "Time: " + ChatColor.YELLOW + _tournamentStart.format(new Date(tournament.Date)),
					ChatColor.GRAY + "Countdown: " + ChatColor.LIGHT_PURPLE + _tournamentCountdown.format(new Date(tournament.Date - System.currentTimeMillis()))
				).build(), !registered ? new IButton() 
				{
					@Override
					public void onClick(Player player, ClickType clickType)
					{
						getPlugin().registerForTournament(player, tournament, new Runnable()
						{
							@Override
							public void run()
							{
								buildPage();
							}
						});
					}
				} : null);
			}
			else
			{
				/*
				if (i == currentDayOfMonth)
					addItem(slot, new ItemBuilder(Material.STAINED_GLASS_PANE, i, (byte)5).setTitle("TODAY.").build());
				else
				*/
				addItem(slot, new ItemBuilder(Material.STAINED_GLASS_PANE, i, (byte)14).setTitle(ChatColor.RESET + "" + ChatColor.BOLD + ChatColor.RED + "No Events").setLore
				(
					ChatColor.GRAY + "Sorry, there are no events",
					ChatColor.GRAY + "on this particular date."
				).build());
			}
			
			slot++;
		}
	}
}
