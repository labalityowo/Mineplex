package mineplex.core.tournament;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniDbClientPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.F;
import mineplex.core.donation.DonationManager;
import mineplex.core.recharge.Recharge;
import mineplex.core.tournament.data.ClientTournamentData;
import mineplex.core.tournament.data.Tournament;
import mineplex.core.tournament.data.TournamentInviteStatus;
import mineplex.core.tournament.data.TournamentParticipant;
import mineplex.core.tournament.data.TournamentTeam;
import mineplex.core.tournament.ui.TournamentShop;

public class TournamentManager extends MiniDbClientPlugin<ClientTournamentData>
{
	public enum Perm implements Permission
	{
		DEBUG_SHOP_COMMAND,
	}

	private TournamentRepository _repository;
	private TournamentShop _shop;
	private HashSet<Tournament> _tournaments = new HashSet<>();
	
	public TournamentManager(JavaPlugin plugin, CoreClientManager clientManager, DonationManager donationManager)
	{
		super("Tournament Manager", plugin, clientManager);
		
		_repository = new TournamentRepository(plugin);
		_shop = new TournamentShop(this, clientManager, donationManager);
		addCommand(new DebugShopCommand(this));
		_tournaments = _repository.getTournaments();
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.PLAYER.setPermission(Perm.DEBUG_SHOP_COMMAND, true, true);
	}
	
	@Override
	public String getQuery(int accountId, String uuid, String name)
	{
		return "SELECT TT.id, TT.tournamentId, accounts.id, accounts.uuid, accounts.name, TT.status FROM tournamentTeams AS TT INNER JOIN accounts ON accounts.id = TT.accountId WHERE TT.accountId = " + accountId + ";";
	}

	@Override
	public void processLoginResultSet(String playerName, UUID uid, int accountId, ResultSet resultSet) throws SQLException
	{
		ClientTournamentData clientData = Get(uid);
		
		while (resultSet.next())
		{
			int teamId = resultSet.getInt(1);
			int tournamentId = resultSet.getInt(2);
			int id = resultSet.getInt(3);
			String uuid = resultSet.getString(4);
			String name = resultSet.getString(5);
			String status = resultSet.getString(6);
			
			if (!clientData.Tournaments.containsKey(tournamentId))
			{
				clientData.Tournaments.put(tournamentId, new Tournament());
			}
			
			Tournament tournament = clientData.Tournaments.get(tournamentId);
			
			if (!tournament.Teams.containsKey(teamId))
			{
				tournament.Teams.put(teamId, new TournamentTeam());
			}
			
			TournamentTeam team = tournament.Teams.get(teamId);
			TournamentParticipant participant = new TournamentParticipant();
			participant.Name = name;
			participant.Uuid = UUID.fromString(uuid);
			participant.Status = Enum.valueOf(TournamentInviteStatus.class, status);
			
			team.Members.put(id, participant);
		}
	}

	@Override
	protected ClientTournamentData addPlayer(UUID uuid)
	{
		return new ClientTournamentData();
	}

	public void openShop(Player player)
	{
		_shop.attemptShopOpen(player);
	}
	
	public HashSet<Tournament> getTournaments()
	{
		return _tournaments;
	}

	public void registerForTournament(Player player, Tournament tournament, Runnable runnable)
	{
		if (!Recharge.Instance.use(player, "Tournament Registration", 1000, true, false))
			return;
		
		runAsync(new Runnable()
		{
			@Override
			public void run()
			{
				int teamId = _repository.registerForTournament(getClientManager().getAccountId(player), tournament.TournamentId);
				
				runSync(new Runnable()
				{
					@Override
					public void run()
					{
						if (teamId != -1)
						{
							player.sendMessage(F.main(getName(), "You have successfully registered for " + tournament.Name + " tournament!"));
							
							TournamentTeam team = new TournamentTeam();
							TournamentParticipant participant = new TournamentParticipant();
							participant.Name = player.getName();
							participant.Uuid = player.getUniqueId();
							participant.Status = TournamentInviteStatus.OWNER;
							
							team.Members.put(getClientManager().getAccountId(player), participant);
							tournament.Teams.put(teamId, team);
						}
						else
						{
							player.sendMessage(F.main(getName(), "There was an error registering you for " + tournament.Name + " tournament.  Please try again later."));
						}
						
						runnable.run();
					}
				});
			}
		});
	}
}