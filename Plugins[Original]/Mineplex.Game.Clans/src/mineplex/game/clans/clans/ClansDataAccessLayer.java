package mineplex.game.clans.clans;

import java.sql.Timestamp;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import mineplex.core.common.util.Callback;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilWorld;
import mineplex.game.clans.clans.event.ClanCreatedEvent;
import mineplex.game.clans.clans.event.ClanCreationCompleteEvent;
import mineplex.game.clans.clans.event.ClanDeleteEvent;
import mineplex.game.clans.clans.event.ClanJoinEvent;
import mineplex.game.clans.clans.event.ClanLeaveEvent;
import mineplex.game.clans.clans.event.ClanSetHomeEvent;
import mineplex.game.clans.clans.scoreboard.ClansScoreboardManager;
import mineplex.game.clans.clans.tntgenerator.TntGenerator;
import mineplex.game.clans.core.ClaimLocation;
import mineplex.game.clans.core.repository.ClanRepository;
import mineplex.game.clans.core.repository.ClanTerritory;
import mineplex.game.clans.core.repository.tokens.ClanToken;
import mineplex.game.clans.core.war.ClanWarData;

public class ClansDataAccessLayer
{
	private ClansManager _manager;
	private ClanRepository _repository;
	private ClansScoreboardManager _scoreboard;
	
	public ClansDataAccessLayer(ClansManager clans, ClansScoreboardManager scoreboard)
	{
		_manager = clans;
		_scoreboard = scoreboard;
		_repository = new ClanRepository(clans.getPlugin(), clans.getServerName(), true);
	}
	
	public void delete(final ClanInfo clan, final Callback<Boolean> callback)
	{
		runAsync(new Runnable()
		{
			@Override
			public void run()
			{
				final boolean out = _repository.deleteClan(clan.getId());
				
				if (out)
				{
					runSync(new Runnable()
					{
						@Override
						public void run()
						{
							deleteLocally(clan);
							
							if (callback != null) callback.run(out);
						}
					});
				}
				
			}
		});
	}
	
	public void deleteLocally(ClanInfo clan)
	{
		// Territory Unclaim
		for (ClaimLocation cur : clan.getClaimSet())
		{
			_manager.getClaimMap().remove(cur);
		}
		
		_manager.getClanMap().remove(clan.getName());
		
		for (ClansPlayer player : clan.getMembers().values())
		{
			_manager.getClanMemberUuidMap().remove(player.getUuid());
		}
		
		// Clean from Others
		for (ClanInfo cur : _manager.getClanMap().values())
		{
			cur.getAllyMap().remove(clan.getName());
			cur.getRequestMap().remove(clan.getName());
			cur.clearWar(cur.getName());
		}
		
		// Scoreboard
		_scoreboard.refresh(clan);
		
		// Log
		_manager.log("Deleted [" + clan.getName() + "].");
		
		ClanDeleteEvent event = new ClanDeleteEvent(clan);
		Bukkit.getServer().getPluginManager().callEvent(event);
	}
	
	public void createAndJoin(final Player player, final String name, final Callback<ClanInfo> callback)
	{
		create(player.getName(), name, false, new Callback<ClanInfo>()
		{
			@Override
			public void run(final ClanInfo clan)
			{
				if (clan != null)
				{
					runAsync(new Runnable()
					{
						@Override
						public void run()
						{
							final ClanRole role = ClanRole.LEADER;
							_repository.addMember(clan.getId(), player.getUniqueId(), role.toString());
							runSync(new Runnable()
							{
								@Override
								public void run()
								{
									updateJoinCache(clan, player, role);
									
									// Log
									_manager.log("Added [" + player + "] to [" + clan.getName() + "].");
									
									if (callback != null) callback.run(clan);
								}
							});
						}
					});
				}
				else
				{
					if (callback != null) callback.run(null);
				}
			}
		});
	}
	
	public void create(final String creator, final String name, final boolean admin, final Callback<ClanInfo> callback)
	{
		final ClanToken token = new ClanToken();
		token.Name = name;
		token.Description = "No Description";
		token.Home = "";
		token.Admin = admin;
		token.Energy = 4320;
		
		ClanCreatedEvent event = new ClanCreatedEvent(token, Bukkit.getPlayer(creator));
		UtilServer.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled())
		{
			return;
		}
		
		token.Id = event.getId();
		token.Name = event.getName();
		token.Description = creator;
		token.Home = event.getHome();
		token.Admin = event.isAdmin();
		token.Energy = event.getEnergy();
		token.Kills = event.getKills();
		token.Murder = event.getMurders();
		token.Deaths = event.getDeaths();
		token.WarWins = event.getWarWins();
		token.WarLosses = event.getWarLosses();
		token.DateCreated = event.getDateCreated();
		token.LastOnline = event.getLastOnline();
		
		runAsync(new Runnable()
		{
			@Override
			public void run()
			{
				final int clanId = _repository.addClan(token);
				final boolean response = clanId != -1;
				
				runSync(new Runnable()
				{
					@Override
					public void run()
					{
						ClanInfo clanInfo = null;
						
						if (response)
						{
							token.Id = clanId;
							clanInfo = new ClanInfo(_manager, token);
							_manager.getClanMap().put(name, clanInfo);
							_manager.log("[" + clanInfo.getName() + "] with Admin [" + admin + "] created by [" + creator + "].");
						}
						
						if (callback != null) callback.run(clanInfo);

						ClanCreationCompleteEvent event = new ClanCreationCompleteEvent(token, Bukkit.getPlayer(creator));
						UtilServer.getServer().getPluginManager().callEvent(event);
					}
				});
			}
		});
	}
	
	public void join(final ClanInfo clan, final Player player, final ClanRole role, final Callback<Boolean> callback)
	{
		ClanJoinEvent event = new ClanJoinEvent(clan, player);
		
		UtilServer.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled())
		{
			return;
		}
		
		runAsync(new Runnable()
		{
			@Override
			public void run()
			{
				final boolean success = _repository.addMember(clan.getId(), player.getUniqueId(), role.toString());
				
				runSync(new Runnable()
				{
					@Override
					public void run()
					{
						if (success)
						{
							updateJoinCache(clan, player, role);
							
							// Log
							_manager.log("Added [" + player + "] to [" + clan.getName() + "].");
						}
						
						if (callback != null) callback.run(success);
					}
				});
				
			}
		});
	}
	
	private void updateJoinCache(ClanInfo clan, Player player, ClanRole role)
	{
		if (_manager.getClanMemberUuidMap().containsKey(player.getUniqueId())) leave(_manager.getClanUtility().getClanByPlayer(player), player, null);
		
		// Update Clan
		ClansPlayer cp = new ClansPlayer(player.getName(), player.getUniqueId(), role);
		clan.getMembers().put(player.getUniqueId(), cp);
		_manager.getClanMemberUuidMap().put(player.getUniqueId(), clan);
		clan.getInviteeMap().remove(player.getName());
		clan.getInviterMap().remove(player.getName());
		clan.playerOnline(player);
		
		// Scoreboard
		_scoreboard.refresh(player);
	}
	
	public void leave(ClanInfo clan, Player player, Callback<Boolean> callback)
	{
		ClansPlayer clansPlayer = clan.getMembers().get(player.getUniqueId());
		if (clansPlayer != null)
		{
			leave(clan, clansPlayer, callback);
			_scoreboard.refresh(player);
		}
	}
	
	public void leave(final ClanInfo clan, final ClansPlayer clansPlayer, final Callback<Boolean> callback)
	{
		if (clan == null) return;
		
		ClanLeaveEvent event = new ClanLeaveEvent(clan, clansPlayer);
		
		UtilServer.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled())
		{
			return;
		}
		
		runAsync(new Runnable()
		{
			@Override
			public void run()
			{
				final boolean success = _repository.removeMember(clan.getId(), clansPlayer.getPlayerName());
				
				runSync(new Runnable()
				{
					@Override
					public void run()
					{
						if (success)
						{
							// Update Clan
							clan.getMembers().remove(clansPlayer.getUuid());
							_manager.getClanMemberUuidMap().remove(clansPlayer.getUuid());
							clan.playerOffline(clansPlayer.getPlayerName());

							_manager.justLeft(clansPlayer.getUuid(), clan);
							// Log
							_manager.log("Removed [" + clansPlayer.getPlayerName() + "] from [" + clan.getName() + "].");
						}
						
						if (callback != null) callback.run(success);
					}
				});
			}
		});
	}
	
	public void role(final ClanInfo clan, final UUID uuid, final ClanRole role)
	{
		final ClansPlayer clansPlayer = clan.getMembers().get(uuid);
		
		if (clansPlayer != null)
		{
			// Update Clan
			clansPlayer.setRole(role);
			
			// Save
			runAsync(new Runnable()
			{
				@Override
				public void run()
				{
					_repository.updateMember(clan.getId(), clansPlayer.getPlayerName(), role.toString());
				}
			});
			
			// Log
			_manager.log("Removed [" + clansPlayer.getPlayerName() + "] from [" + clan.getName() + "].");
		}
	}
	
	public void invite(ClanInfo clan, String player, String inviter)
	{
		clan.getInviteeMap().put(player, System.currentTimeMillis());
		clan.getInviterMap().put(player, inviter);
		
		// Log
		_manager.log("Invited [" + player + "] to [" + clan.getName() + "] by [" + inviter + "].");
	}
	
	public void requestAlly(ClanInfo clan, ClanInfo target, String player)
	{
		clan.getRequestMap().put(target.getName(), System.currentTimeMillis());
		
		// Log
		_manager.log("Alliance Request to [" + target.getName() + "] from [" + clan.getName() + "] by [" + player + "].");
	}
	
	public void ally(final ClanInfo cA, final ClanInfo cB, String player)
	{
		// Remove Requests
		cA.getRequestMap().remove(cB.getName());
		cB.getRequestMap().remove(cA.getName());
		
		// Update ClansManager
		cA.getAllyMap().put(cB.getName(), false);
		cB.getAllyMap().put(cA.getName(), false);
		
		// Save
		runAsync(new Runnable()
		{
			@Override
			public void run()
			{
				_repository.addClanRelationship(cA.getId(), cB.getId(), false);
				_repository.addClanRelationship(cB.getId(), cA.getId(), false);
			}
		});
		
		// Scoreboard
		_scoreboard.refresh(cA);
		
		// Log
		_manager.log("Added Ally for [" + cB.getName() + "] and [" + cA.getName() + "] by [" + player + "].");
	}
	
	/*
	 * public void enemy(final ClanInfo clan, final ClanInfo otherClan, String
	 * player) { runAsync(new Runnable() {
	 * @Override public void run() { _repository.addEnemy(clan.getId(),
	 * otherClan.getId()); } }); // Memory Timestamp currDate = new
	 * Timestamp(System.currentTimeMillis()); ClanEnemyToken clanEnemyToken =
	 * new ClanEnemyToken(); clanEnemyToken.Initiator = true;
	 * clanEnemyToken.TimeFormed = currDate; clanEnemyToken.Score = 0;
	 * clanEnemyToken.EnemyName = otherClan.getName();
	 * clan.updateEnemy(clanEnemyToken); ClanEnemyToken otherClanEnemyToken =
	 * new ClanEnemyToken(); otherClanEnemyToken.Initiator = false;
	 * otherClanEnemyToken.TimeFormed = currDate; otherClanEnemyToken.Score = 0;
	 * otherClanEnemyToken.EnemyName = clan.getName();
	 * otherClan.updateEnemy(otherClanEnemyToken); //Scoreboard
	 * _scoreboard.refresh(clan); _manager.log("Added Enemy for [" +
	 * clan.getName() + "] and [" + otherClan.getName() + "] by [" + player +
	 * "]."); }
	 */
	
//	public boolean trust(final ClanInfo ownerClan, final ClanInfo otherClan, String player)
//	{
//		if (!ownerClan.getAllyMap().containsKey(otherClan.getName())) return false;
//		
//		final boolean trust = !ownerClan.getAllyMap().get(otherClan.getName());
//		
//		// Memory
//		ownerClan.getAllyMap().put(otherClan.getName(), trust);
//		
//		// Save
//		runAsync(new Runnable()
//		{
//			@Override
//			public void run()
//			{
//				_repository.updateClanRelationship(ownerClan.getId(), otherClan.getId(), trust);
//			}
//		});
//		
//		// Scoreboard
//		_scoreboard.refresh(ownerClan);
//		
//		// Log
//		_manager.log((trust ? "Gave" : "Revoked") + " Trust [" + trust + "] to [" + otherClan.getName() + "] for [" + ownerClan.getName() + "] by [" + player + "].");
//		
//		return trust;
//	}
	
	public void neutral(final ClanInfo cA, final ClanInfo cB, String player, boolean bothClansManager)
	{
		// Update ClansManager
		cA.getAllyMap().remove(cB.getName());
		cB.getAllyMap().remove(cA.getName());
		
		// Save
		runAsync(new Runnable()
		{
			@Override
			public void run()
			{
				_repository.removeClanRelationship(cA.getId(), cB.getId());
				_repository.removeClanRelationship(cB.getId(), cA.getId());
			}
		});
		
		// Scoreboard
		_scoreboard.refresh(cA);
		
		// Log
		_manager.log("Added Neutral between [" + cA.getName() + "] and [" + cB.getName() + "] by [" + player + "].");
	}
	
	public boolean claimAll(final String name, final String player, final boolean safe, final ClaimLocation... chunks)
	{
		if (!_manager.getClanMap().containsKey(name)) return false;
		
		final ClanInfo clan = _manager.getClanMap().get(name);
		
		// Unclaim
		for (ClaimLocation chunk : chunks)
		{
			if (_manager.getClaimMap().containsKey(chunk))
			{
				unclaim(chunk, player, false);
			}
			
			// Memory
			ClanTerritory claim = new ClanTerritory(chunk, name, safe);
			clan.getClaimSet().add(chunk);
			_manager.getClaimMap().put(chunk, claim);
		}
		
		// Save
		runAsync(new Runnable()
		{
			@Override
			public void run()
			{
				_repository.addTerritoryClaims(clan.getId(), safe, chunks);
				
				// Log
				_manager.log("Successfully added [" + chunks.length + "] Claims for [" + name + "] by [" + player + "].");
			}
		});
		
		return true;
	}
	
	@SuppressWarnings("deprecation")
	public boolean claim(String name, final ClaimLocation chunk, String player, final boolean safe)
	{
		if (!_manager.getClanMap().containsKey(name)) return false;
		
		final ClanInfo clan = _manager.getClanMap().get(name);
		
		// Unclaim
		if (_manager.getClaimMap().containsKey(chunk)) unclaim(chunk, player, false);
		
		// Memory
		ClanTerritory claim = new ClanTerritory(chunk, name, safe);
		clan.getClaimSet().add(chunk);
		_manager.getClaimMap().put(chunk, claim);
		
		// Save
		runAsync(() ->
		{
			_repository.addTerritoryClaim(clan.getId(), chunk, safe);
		});
		
		// Visual
		Chunk c = chunk.toChunk();
		if (!clan.isAdmin())
		{
			for (int x = 0; x < 16; x++)
			{
				for (int z = 0; z < 16; z++)
				{
					if (z == 0 || z == 15 || x == 0 || x == 15)
					{
						Block down = UtilBlock.getHighest(c.getWorld(), c.getBlock(x, 0, z).getX(), c.getBlock(x, 0, z).getZ()).getRelative(BlockFace.DOWN);
						if (down.getTypeId() == 1 || down.getTypeId() == 2 || down.getTypeId() == 3 || down.getTypeId() == 12 || down.getTypeId() == 8) _manager.getBlockRestore().add(down, 89, (byte) 0, 180000, true);
					}
				}
			}
		}
		
		// Log
		 _manager.log("Added Claim for [" + name + "] at [" + chunk + "] by ["
		 + player + "].");
		
		return true;
	}
	
	public boolean unclaim(final ClaimLocation chunk, String player, boolean sql)
	{
		ClanTerritory claim = _manager.getClaimMap().remove(chunk);
		
		if (claim == null)
		{
			_manager.log("Unclaiming NULL Chunk Failed.");
			return false;
		}
		
		final ClanInfo clan = _manager.getClanMap().get(claim.Owner);
		
		if (clan == null)
		{
			_manager.log("Unclaiming from NULL Clan Failed.");
			return false;
		}
		
		// Memory
		clan.getClaimSet().remove(chunk);
		_manager.getUnclaimMap().put(chunk, System.currentTimeMillis());
		
		// Save
		runAsync(new Runnable()
		{
			@Override
			public void run()
			{
				_repository.removeTerritoryClaim(clan.getId(), chunk);
			}
		});
		
		// Restore any glowstone blocks
		Chunk c = chunk.toChunk();
		if (!clan.isAdmin())
		{
			for (int x = 0; x < 16; x++)
			{
				for (int z = 0; z < 16; z++)
				{
					if (z == 0 || z == 15 || x == 0 || x == 15)
					{
						for (int y = 0; y < 256; y++)
						{
							Block block = c.getBlock(x, y, z);
							if (block.getType() == Material.GLOWSTONE)
							{
								if (!_manager.getBlockRestore().restore(block)) block.setType(Material.STONE);
							}
						}
					}
				}
			}
		}
		
		// Log
		if (player != null) _manager.log("Removed Claim for [" + clan.getName() + "] at [" + chunk + "] by [" + player + "].");
		else
			_manager.log("Removed Claim for [" + clan.getName() + "] at [" + chunk + "] by [NO ONE?!].");
		
		// Bed Removal
		if (clan.getHome() != null && UtilWorld.chunkToStr(clan.getHome().getChunk()).equals(chunk))
		{
			UtilBlock.deleteBed(clan.getHome());
			clan.setHome(null);
			clan.inform("Clan has lost its Home because of a Territory loss.", null);
		}
		
		return true;
	}
	
	public boolean unclaimSilent(ClaimLocation chunk, boolean sql)
	{
		return unclaim(chunk, null, sql);
	}
	
	public void setHome(final ClanInfo clan, Location loc, String player)
	{
		// Event
		ClanSetHomeEvent event = new ClanSetHomeEvent(clan, Bukkit.getPlayer(player), loc);
		Bukkit.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled())
		{
			return;
		}
		
		// Memory
		clan.setHome(loc);
		
		// Save
		runAsync(new Runnable()
		{
			@Override
			public void run()
			{
				_repository.updateClan(clan.getId(), clan.getName(), clan.getDesc(), UtilWorld.locToStr(clan.getHome()), clan.isAdmin(), clan.getEnergy(), clan.getKills(), clan.getMurder(), clan.getDeaths(), clan.getWarWins(), clan.getWarLosses(), clan.getLastOnline());
			}
		});
		
		// Log
		_manager.log("Set Home for [" + clan.getName() + "] to " + UtilWorld.locToStrClean(loc) + " by [" + player + "].");
	}
	
	public void saveClan(final ClanInfo clan)
	{
		runAsync(new Runnable()
		{
			@Override
			public void run()
			{
				_repository.updateClan(clan.getId(), clan.getName(), clan.getDesc(), UtilWorld.locToStr(clan.getHome()), clan.isAdmin(), clan.getEnergy(), clan.getKills(), clan.getMurder(), clan.getDeaths(), clan.getWarWins(), clan.getWarLosses(), clan.getLastOnline());
			}
		});
	}
	
	public void war(final ClanInfo clanA, final ClanInfo clanB, final int score, final Callback<ClanWarData> warCallback)
	{
		final Timestamp currentTime = new Timestamp(System.currentTimeMillis());
		final ClanWarData war = new ClanWarData(clanA.getName(), clanB.getName(), score, currentTime, currentTime, 0);

		// Memory
		clanA.addWar(war);
		clanB.addWar(war);
		
		runAsync(new Runnable()
		{
			@Override
			public void run()
			{
				_repository.addWar(clanA.getId(), clanB.getId(), score, currentTime);
				
				runSync(new Runnable()
				{
					@Override
					public void run()
					{
						_manager.log("Initiator war for [" + clanA.getName() + "] against [" + clanB.getName() + "].");
						
						if (warCallback != null) warCallback.run(war);
					}
				});
			}
		});
		
	}
	
	public void updateWar(final ClanInfo clanA, final ClanInfo clanB, final ClanWarData war, final Callback<Boolean> callback)
	{
		runAsync(new Runnable()
		{
			@Override
			public void run()
			{
				final boolean ran = _repository.updateWar(clanA.getId(), clanB.getId(), war.getPoints(), war.getLastUpdated());
				runSync(new Runnable()
				{
					@Override
					public void run()
					{
						if (callback != null) callback.run(ran);
					}
				});
			}
		});
	}
	
	/*
	 * public void updateEnemy(ClanInfo clan, ClanInfo otherClan) { assert
	 * clan.getEnemyData() != null && otherClan.getEnemyData() != null; assert
	 * clan.getEnemyData().getEnemyName() == otherClan.getName() &&
	 * otherClan.getEnemyData().getEnemyName() == clan.getName(); ClanInfo
	 * initiator = clan.getEnemyData().isInitiator() ? clan : otherClan;
	 * EnemyData iData = initiator.getEnemyData(); ClanInfo other = clan ==
	 * initiator ? otherClan : clan; EnemyData oData = other.getEnemyData();
	 * _repository.updateEnemy(initiator.getId(), other.getId(),
	 * iData.getClanAScore(), oData.getClanAScore(), iData.getKills(),
	 * oData.getKills()); //Log _manager.log("Updated Enemy Data for [" +
	 * clan.getName() + ", " + otherClan.getName() + "]"); }
	 */
	
	public void updateGenerator(final ClanInfo clanInfo, final Callback<Boolean> callback)
	{
		TntGenerator generator = clanInfo.getGenerator();
		final String creator;
		final int generatorStock;
		
		if (generator != null)
		{
			creator = generator.getBuyer().toString();
			generatorStock = generator.getStock();
		}
		else
		{
			creator = "";
			generatorStock = 0;
		}
		
		runAsync(new Runnable()
		{
			@Override
			public void run()
			{
				final boolean ran = _repository.updateClanGenerator(clanInfo.getId(), creator, generatorStock);
				runSync(new Runnable()
				{
					@Override
					public void run()
					{
						if (callback != null)
						{
							callback.run(ran);
						}
						else
						{
							if (!ran)
							{
								System.out.println("Tnt Gen didn't save!");
							}
						}
					}
				});
			}
		});
	}
	
	public void updateEnergy(ClanInfo clan)
	{
		// Save
		// TODO: query to only update energy?
		_repository.updateClan(clan.getId(), clan.getName(), clan.getDesc(), UtilWorld.locToStr(clan.getHome()), clan.isAdmin(), clan.getEnergy(), clan.getKills(), clan.getMurder(), clan.getDeaths(), clan.getWarWins(), clan.getWarLosses(), clan.getLastOnline());
		
		// Log
		_manager.log("Updated Energy for [" + clan.getName() + "] to " + clan.getEnergy() + ".");
	}
	
	public void safe(ClanTerritory claim, String player)
	{
		// Memory
		claim.Safe = !claim.Safe;
		
		runAsync(new Runnable()
		{
			@Override
			public void run()
			{
				_repository.updateTerritoryClaim(claim.ClaimLocation, claim.Safe);
			}
		});
		
		// Log
		_manager.log("Safe Zone at [" + claim.ClaimLocation.toStoredString() + "] set to [" + claim.Safe + "] by [" + player + "].");
	}
	
	public void retrieveClan(final String clanName, final Callback<ClanToken> callback)
	{
		runAsync(new Runnable()
		{
			@Override
			public void run()
			{
				ClanToken clan = _repository.retrieveClan(clanName);
				runSync(() -> callback.run(clan));
			}
		});
	}
	
	public void clanExists(String clanName, Callback<Boolean> callback)
	{
		runAsync(new Runnable()
		{
			@Override
			public void run()
			{
				_repository.clanExists(clanName, new Callback<Boolean>()
				{
					@Override
					public void run(Boolean data)
					{
						runSync(new Runnable()
						{
							@Override
							public void run()
							{
								if (callback != null) callback.run(data);
							}
						});
					}
				});
			}
		});
	}
	
	public ClanRepository getRepository()
	{
		return _repository;
	}
	
	private void runAsync(Runnable runnable)
	{
		_manager.runAsync(runnable);
	}
	
	private void runSync(Runnable runnable)
	{
		_manager.runSync(runnable);
	}
}
