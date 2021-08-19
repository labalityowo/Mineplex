package mineplex.core.tournament;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.database.MinecraftRepository;
import mineplex.core.tournament.data.Tournament;
import mineplex.core.tournament.data.TournamentInviteStatus;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.ResultSetCallable;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnVarChar;

public class TournamentRepository extends RepositoryBase
{
	private static String REGISTER_FOR_TOURNAMENT = "INSERT INTO tournamentTeams(accountId, tournamentId, status) VALUES (?, ?, ?);";
	private static String UNREGISTER_FOR_TOURNAMENT = "DELETE FROM TTI FROM tournamentTeamInvites AS TTI INNER JOIN tournamentTeams AS TT ON TT.id = TTI.teamId WHERE TTI.accountId = ? AND TT.tournamentId = ?;";
	private static String UNREGISTER_TEAM_FOR_TOURNAMENT = "DELETE FROM tournamentTeams WHERE accountId = ? AND tournamentId = ?;";
	private static String RETRIEVE_ALL_TOURNAMENTS = "SELECT * FROM tournaments;";
	
	public TournamentRepository(JavaPlugin plugin)
	{
		super(DBPool.getAccount());
	}

	public int registerForTournament(int accountId, int tournamentId)
	{
		List<Integer> teamId = new ArrayList<>();
		
		executeInsert(REGISTER_FOR_TOURNAMENT, new ResultSetCallable()
			{
				@Override
				public void processResultSet(ResultSet resultSet) throws SQLException
				{
					if (resultSet.next())
					{
						teamId.add(resultSet.getInt(1));
					}
				}
			}, new ColumnInt("accountId", accountId), 
			new ColumnInt("tournamentId", tournamentId),
			new ColumnVarChar("status", 11, TournamentInviteStatus.OWNER.toString()));
		
		return teamId.size() > 0 ? teamId.get(0) : -1;
	}
	
	public boolean unregisterFromTeam(int accountId, int tournamentId, int teamId)
	{
		return executeUpdate(UNREGISTER_FOR_TOURNAMENT, new ColumnInt("accountId", accountId), new ColumnInt("tournamentId", tournamentId), new ColumnInt("teamId", teamId)) > 0;
	}
	
	public boolean unregisterTeamFromTournament(int accountId, int tournamentId)
	{
		return executeUpdate(UNREGISTER_TEAM_FOR_TOURNAMENT, new ColumnInt("accountId", accountId), new ColumnInt("tournamentId", tournamentId)) > 0;
	}
	
	public HashSet<Tournament> getTournaments()
	{
		HashSet<Tournament> tournaments = new HashSet<>();
		
		executeQuery(RETRIEVE_ALL_TOURNAMENTS, new ResultSetCallable()
		{
			@Override
			public void processResultSet(ResultSet resultSet) throws SQLException
			{
				while (resultSet.next())
				{
					Tournament tournament = new Tournament();
					tournament.TournamentId = resultSet.getInt(1);
					tournament.Name = resultSet.getString(2);
					tournament.Date = resultSet.getTimestamp(3).getTime();
					tournament.GameType = resultSet.getString(4);
					
					tournaments.add(tournament);
				}
			}
		});
		
		return tournaments;
	}
}
