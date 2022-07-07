package mineplex.core.elo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniDbClientPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.itemstack.ItemBuilder;

public class EloManager extends MiniDbClientPlugin<EloClientData>
{
    public enum Perm implements Permission
    {
        TOP_ELO_COMMAND,
    }

    private EloRepository _repository;
    private EloRatingSystem _ratingSystem;
    private NautHashMap<String, EloTeam> _eloTeams = new NautHashMap<>();

    public EloManager(JavaPlugin plugin, CoreClientManager clientManager)
    {
        super("Elo Rating", plugin, clientManager);

        _repository = new EloRepository(plugin);
        _ratingSystem = new EloRatingSystem
          (
            new KFactor(0, 1299, 50),
            new KFactor(1300, 1899, 45),
            new KFactor(1900, 2499, 40),
            new KFactor(2500, 3099, 30),
            new KFactor(3100, 3699, 20),
            new KFactor(3700, 5000, 10)
          );
        
        generatePermissions();
    }
    
    private void generatePermissions()
	{

		PermissionGroup.ADMIN.setPermission(Perm.TOP_ELO_COMMAND, true, true);
	}

    public EloRepository getRepo()
    {
        return _repository;
    }

    @Override
    public void addCommands()
    {
        addCommand(new TopEloCommand(this));
    }

    public int getElo(Player player, int gameType)
    {
        if (!Get(player).Elos.containsKey(gameType))
        {
            return 1000;
        }

        return Get(player).Elos.get(gameType);
    }

    public EloTeam getNewRatings(EloTeam teamA, EloTeam teamB, GameResult result)
    {
        EloTeam newTeam = new EloTeam();

        int newTotal = _ratingSystem.getNewRating(teamA.TotalElo / teamA.getPlayers().size(), teamB.TotalElo / teamB.getPlayers().size(), result) * teamA.getPlayers().size();
        int kTotal = 0;

        for (EloPlayer player : teamA.getPlayers())
        {
            kTotal += _ratingSystem.getKFactor(player.getRating());
        }

        for (EloPlayer player : teamA.getPlayers())
        {
            int newRating = (int) (player.getRating() + (_ratingSystem.getKFactor(player.getRating()) / (double) kTotal) * (newTotal - teamA.TotalElo));
            EloPlayer newPlayer = new EloPlayer(player.getPlayer(), player.getAccountId(), newRating);

            newTeam.addPlayer(newPlayer);
        }

        return newTeam;
    }

    public void saveElo(final Player player, final int accountId, final int gameType, final int oldElo, final int elo)
    {
        runAsync(() -> {
            boolean success = false;

            try
            {
                success = _repository.saveElo(accountId, gameType, oldElo, elo);
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            } finally
            {
                System.out.println("Saving " + accountId + "'s new elo rating of " + elo + " for gameType " + gameType + (success ? " SUCCEEDED." : " FAILED."));
            }

            final boolean finalSuccess = success;

            runSync(() ->
            {
                if (finalSuccess)
                {
                    if (player.isOnline())
                    {
                        Get(player).Elos.put(gameType, elo);
                    }
                }
            });
        });
    }

    @Override
    protected EloClientData addPlayer(UUID uuid)
    {
        return new EloClientData();
    }

    @Override
    public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
    {
        Set(uuid, _repository.loadClientInformation(resultSet));
    }

    @Override
    public String getQuery(int accountId, String uuid, String name)
    {
        return "SELECT gameType, elo FROM eloRating WHERE accountId = '" + accountId + "';";
    }

    public void addTeam(String displayName, EloTeam eloTeam)
    {
        _eloTeams.put(displayName, eloTeam);
    }

    public void setWinningTeam(String displayName)
    {
        _eloTeams.get(displayName).Winner = true;
    }
    
    /**
     * Checks if a player is banned from ranked games async
     * @param accountId The player's account ID
     * @param afterCheck Code to be executed with the fetched ban status
     */
    public void checkRankBannedAsync(int accountId, Callback<Long> afterCheck)
    {
    	getRankBanExpiryAsync(accountId, expiry -> {
    		boolean expired = System.currentTimeMillis() >= expiry;
    		
    		if (expired)
    		{
    			_repository.getStrikeExpiry(accountId, strikeExpiration ->
    			{
    				if (System.currentTimeMillis() >= strikeExpiration)
    				{
    					_repository.resetStrikes(accountId);
    				}
    			});
    		}
    		
    		afterCheck.run(expiry);
    	});
    }
    
    /**
     * Checks if a player is banned from ranked games (Should not be run on main thread)
     * @param accountId The player's account ID
     * @return The ban status of the player
     */
    public boolean checkRankBanned(int accountId)
    {
    	boolean expired = System.currentTimeMillis() >= getRankBanExpiry(accountId);
    	
    	if (expired)
    	{
			_repository.getStrikeExpiry(accountId, strikeExpiration ->
			{
				if (System.currentTimeMillis() >= strikeExpiration)
				{
					_repository.resetStrikes(accountId);
				}
			});
    	}
    	
    	return !expired;
    }
    
    /**
     * Gets a player's remaining ranked game ban duration (Should not be run on main thread)
     * @param accountId The player's account ID
     * @return The ban expiry of the player (0 if not found)
     */
    public long getRankBanExpiry(int accountId)
    {
    	return _repository.getBanExpiry(accountId);
    }
    
    /**
     * Gets a player's remaining ranked game ban duration if applicable async
     * @param accountId The player's account ID
     * @param afterFetch Code to be executed with the fetched ban expiration
     */
    public void getRankBanExpiryAsync(int accountId, Callback<Long> afterFetch)
    {
        _repository.getBanExpiryAsync(accountId, afterFetch);
    }
    
    /**
     * Bans a player from joining ranked games temporarily
     * @param accountId The player's account ID
     */
    public void banFromRanked(int accountId)
    {
        _repository.addRankedBan(accountId);
    }
    
    /**
     * Called when game ends to calculate new Elo and award it
     * @param gameId The game's ID
     */
    public void endMatch(int gameId)
    {
        EloTeam teamWinner = null;
        EloTeam teamLoser = null;

        for (EloTeam team : _eloTeams.values())
        {
            if (team.Winner)
            {
                teamWinner = team;
            }
            else
            {
                teamLoser = team;
            }
        }

        EloTeam teamWinnerNew = getNewRatings(teamWinner, teamLoser, GameResult.Win);
        EloTeam teamLoserNew = getNewRatings(teamLoser, teamWinner, GameResult.Loss);

        // Use teams to calculate Elo
        for (EloPlayer eloPlayer : teamWinnerNew.getPlayers())
        {
            int oldElo = teamWinner.getPlayer(eloPlayer.getPlayer().getUniqueId().toString()).getRating();

            // If this is the first time.
            if (!Get(eloPlayer.getPlayer()).Elos.containsKey(gameId))
            {
                oldElo = eloPlayer.getRating();
            }

            saveElo(eloPlayer.getPlayer(), eloPlayer.getAccountId(), gameId, oldElo, eloPlayer.getRating());
        }

        for (EloPlayer eloPlayer : teamLoserNew.getPlayers())
        {
            int oldElo = teamLoser.getPlayer(eloPlayer.getPlayer().getUniqueId().toString()).getRating();

            // If this is the first time.
            if (!Get(eloPlayer.getPlayer()).Elos.containsKey(gameId))
            {
                oldElo = eloPlayer.getRating();
            }

            saveElo(eloPlayer.getPlayer(), eloPlayer.getAccountId(), gameId, oldElo, eloPlayer.getRating());
        }

        _eloTeams.clear();
    }
    
    /**
     * Manages and stores all Elo divisions and required data for those divisions for proper operation of Elo division display in-game.
     */
    public enum EloDivision
    {
        DIAMOND("Diamond", -1, 3700, Material.DIAMOND_BLOCK),
        EMERALD_3("Emerald 3", 3699, 3500, Material.EMERALD_BLOCK),
        EMERALD_2("Emerald 2", 3499, 3300, Material.EMERALD_BLOCK),
        EMERALD_1("Emerald 1", 3299, 3100, Material.EMERALD_BLOCK),
        LAPIS_3("Lapis 3", 3099, 2900, Material.LAPIS_BLOCK),
        LAPIS_2("Lapis 2", 2899, 2700, Material.LAPIS_BLOCK),
        LAPIS_1("Lapis 1", 2699, 2500, Material.LAPIS_BLOCK),
        GOLD_3("Gold 3", 2499, 2300, Material.GOLD_BLOCK),
        GOLD_2("Gold 2", 2299, 2100, Material.GOLD_BLOCK),
        GOLD_1("Gold 1", 2099, 1900, Material.GOLD_BLOCK),
        IRON_3("Iron 3", 1899, 1700, Material.IRON_BLOCK),
        IRON_2("Iron 2", 1699, 1500, Material.IRON_BLOCK),
        IRON_1("Iron 1", 1499, 1300, Material.IRON_BLOCK),
        COAL_3("Coal 3", 1299, 1100, Material.COAL_BLOCK),
        COAL_2("Coal 2", 1099, 900, Material.COAL_BLOCK),
        COAL_1("Coal 1", 899, -1, Material.COAL_BLOCK);

        private String _disp;
        private int _maxElo, _minElo;
        private Material _visual;

        EloDivision(String display, int maxElo, int minElo, Material visual)
        {
            _disp = display;
            _maxElo = maxElo;
            _minElo = minElo;
            _visual = visual;
        }
        
        /*
         * Method for fetching the proper division for a given Elo value
         */
        public static EloDivision getDivision(int elo)
        {
            for (EloDivision ed : EloDivision.values())
            {
                boolean applies = true;
                if (ed._maxElo != -1)
                {
                    if (elo > ed._maxElo)
                    {
                        applies = false;
                    }
                }
                if (ed._minElo != -1)
                {
                    if (elo < ed._minElo)
                    {
                        applies = false;
                    }
                }

                if (applies)
                {
                    return ed;
                }
            }

            return EloDivision.COAL_2;
        }

        public String getDisplayName()
        {
            return _disp;
        }
        
        /**
         * Method for fetching the ItemStack that represents a player's division and their progress in it
         */
        public ItemStack getVisual(int elo)
        {
            ItemBuilder build = new ItemBuilder(_visual);
            build.setTitle(C.cGreen + _disp);
            int percentage;

            if (_maxElo == -1)
            {
            	percentage = 100;
            }
            else
            {
            	percentage = ((elo - Math.max(0, _minElo)) * 100) / (_maxElo - Math.max(0, _minElo));
            }
            
            String color = C.cYellow;
            if (percentage <= 35)
            {
                color = C.cRed;
            }
            else if (percentage >= 65)
            {
                color = C.cGreen;
            }
            
            build.addLore(C.cGold + "Progress:", color + percentage + "% complete with this Division");
            return build.build();
        }
    }
}