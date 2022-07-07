package mineplex.hub.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.account.CoreClient;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilServer;
import mineplex.core.donation.Donor;
import mineplex.core.scoreboard.MineplexScoreboard;
import mineplex.core.scoreboard.ScoreboardManager;
import mineplex.hub.HubClient;
import mineplex.hub.HubManager;

public class HubScoreboard extends ScoreboardManager
{

	private final HubManager _manager;

	public HubScoreboard(JavaPlugin plugin, HubManager manager)
	{
		super(plugin);

		_manager = manager;
	}

	@Override
	public void setup(MineplexScoreboard scoreboard)
	{
		for (PermissionGroup group : PermissionGroup.values())
		{
			if (!group.canBePrimary())
			{
				continue;
			}
			if (!group.getDisplay(false, false, false, false).isEmpty())
			{
				scoreboard.getHandle().registerNewTeam(group.name()).setPrefix(group.getDisplay(true, true, true, false) + ChatColor.RESET + " ");
			}
			else
			{
				scoreboard.getHandle().registerNewTeam(group.name()).setPrefix("");
			}
		}

		scoreboard.register(HubScoreboardLine.SERVER_TITLE)
				.register(HubScoreboardLine.SERVER_NAME)
				.register(HubScoreboardLine.SERVER_EMPTY_SPACER)
				.register(HubScoreboardLine.GEM_TITLE)
				.register(HubScoreboardLine.GEM_COUNT)
				.register(HubScoreboardLine.GEM_EMPTY_SPACER)
				.register(HubScoreboardLine.SHARDS_TITLE)
				.register(HubScoreboardLine.SHARDS_COUNT)
				.register(HubScoreboardLine.PLAYER_EMPTY_SPACER)
				.register(HubScoreboardLine.RANK_TITLE)
				.register(HubScoreboardLine.RANK_NAME)
				.register(HubScoreboardLine.RANK_EMPTY_SPACER)
				.register(HubScoreboardLine.WEBSITE_TITLE)
				.register(HubScoreboardLine.WEBSITE_VALUE)
				.register(HubScoreboardLine.END_BREAKER)
				.recalculate();

		scoreboard.get(HubScoreboardLine.SERVER_TITLE).write(C.cAquaB + "Server");
		scoreboard.get(HubScoreboardLine.SERVER_NAME).write(UtilServer.getServerName());
		scoreboard.get(HubScoreboardLine.GEM_TITLE).write(C.cGreenB + "Gems");
		scoreboard.get(HubScoreboardLine.SHARDS_TITLE).write(C.cYellowB + "Shards");
		scoreboard.get(HubScoreboardLine.RANK_TITLE).write(C.cGoldB + "Rank");
		scoreboard.get(HubScoreboardLine.WEBSITE_TITLE).write(C.cRedB + "Website");
		scoreboard.get(HubScoreboardLine.WEBSITE_VALUE).write("www.mineplex.com");
		scoreboard.get(HubScoreboardLine.END_BREAKER).write("----------------");
	}

	@Override
	public void draw(MineplexScoreboard scoreboard)
	{
		CoreClient client = _manager.GetClients().Get(scoreboard.getOwner());
		Donor donor = _manager.GetDonation().Get(scoreboard.getOwner());

		scoreboard.setSidebarName(C.cWhite + C.Bold + _manager.Get(scoreboard.getOwner()).GetScoreboardText());
		scoreboard.get(HubScoreboardLine.GEM_COUNT).write(donor.getBalance(GlobalCurrency.GEM));
		scoreboard.get(HubScoreboardLine.SHARDS_COUNT).write(donor.getBalance(GlobalCurrency.TREASURE_SHARD));

		String rankName = getRankName(client.getPrimaryGroup(), _manager.GetDonation().Get(scoreboard.getOwner()));

		PermissionGroup disguisedRank = client.getDisguisedPrimaryGroup();
		String disguisedAs = client.getDisguisedAs();
		if (disguisedRank != null && disguisedAs != null)
		{
			rankName = getRankName(disguisedRank, donor) + " (" + rankName + ")";
		}

		scoreboard.get(HubScoreboardLine.RANK_NAME).write(rankName);
	}

	@Override
	public void handlePlayerJoin(String playerName)
	{
		Player player = Bukkit.getPlayerExact(playerName);

		HubClient hubclient = _manager.Get(player);
		hubclient.setName(playerName);

		PermissionGroup group = _manager.GetClients().Get(player).getRealOrDisguisedPrimaryGroup();

		for (MineplexScoreboard scoreboard : getScoreboards().values())
		{
			scoreboard.getHandle().getTeam(group.name()).addEntry(playerName);
		}

		if (get(player) != null)
		{
			for (Player player1 : Bukkit.getOnlinePlayers())
			{
				group = _manager.GetClients().Get(player1).getRealOrDisguisedPrimaryGroup();
				get(player).getHandle().getTeam(group.name()).addEntry(player1.getName());
			}
		}
	}

	@Override
	public void handlePlayerQuit(String playerName)
	{
		Player player = Bukkit.getPlayerExact(playerName);

		PermissionGroup group = _manager.GetClients().Get(player).getRealOrDisguisedPrimaryGroup();

		for (MineplexScoreboard scoreboard : getScoreboards().values())
		{
			scoreboard.getHandle().getTeam(group.name()).removeEntry(playerName);
		}
	}

	private String getRankName(PermissionGroup group, Donor donor)
	{
		String display = group.getDisplay(false, false, false, false);
		if (display.isEmpty())
		{
			if (donor.ownsUnknownSalesPackage("SuperSmashMobs ULTRA") ||
					donor.ownsUnknownSalesPackage("Survival Games ULTRA") ||
					donor.ownsUnknownSalesPackage("Minigames ULTRA") ||
					donor.ownsUnknownSalesPackage("CastleSiege ULTRA") ||
					donor.ownsUnknownSalesPackage("Champions ULTRA"))
			{
				display = "Single Ultra";
			}
			else
			{
				display = "No Rank";
			}
		}

		return display;
	}

}
