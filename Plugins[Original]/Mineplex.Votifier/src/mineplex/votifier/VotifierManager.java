package mineplex.votifier;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.bonuses.BonusAmount;
import mineplex.core.bonuses.BonusManager;
import mineplex.core.bonuses.redis.VotifierCommand;
import mineplex.core.common.Pair;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.UUIDFetcher;
import mineplex.core.donation.DonationManager;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.stats.StatsManager;
import mineplex.core.treasure.types.TreasureType;
import mineplex.database.Tables;
import mineplex.database.tables.records.BonusRecord;
import mineplex.serverdata.Region;
import mineplex.serverdata.Utility;
import mineplex.serverdata.commands.ServerCommand;
import mineplex.serverdata.data.PlayerStatus;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.redis.RedisConfig;
import mineplex.serverdata.redis.RedisDataRepository;
import mineplex.serverdata.servers.ServerManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class VotifierManager extends MiniPlugin
{
	private CoreClientManager _clientManager;
	private DonationManager _donationManager;
	private BonusManager _bonusManager;
	private InventoryManager _inventoryManager;
	private StatsManager _statsManager;

	private RedisConfig _usConfig;
	private RedisConfig _euConfig;
	private RedisDataRepository<PlayerStatus> _usPlayerRepo;
	private RedisDataRepository<PlayerStatus> _euPlayerRepo;
	private JedisPool _usWritePool;
	private JedisPool _euWritePool;
	
	private final boolean ClansVotifier;

	public VotifierManager(JavaPlugin plugin, CoreClientManager clientManager, DonationManager donationManager, BonusManager bonusManager, InventoryManager inventoryManager, StatsManager statsManager)
	{
		super("Votifier", plugin);

		_clientManager = clientManager;
		_donationManager = donationManager;
		_bonusManager = bonusManager;
		_inventoryManager = inventoryManager;
		_statsManager = statsManager;

		_usConfig = ServerManager.loadConfig("us-redis.dat");
		_euConfig = ServerManager.loadConfig("eu-redis.dat");

		_usPlayerRepo = new RedisDataRepository<PlayerStatus>(_usConfig.getConnection(true, "DefaultConnection"),
				_usConfig.getConnection(false, "DefaultConnection"), Region.US, PlayerStatus.class, "playerStatus");
		_euPlayerRepo = new RedisDataRepository<PlayerStatus>(_euConfig.getConnection(true, "DefaultConnection"),
				_euConfig.getConnection(false, "DefaultConnection"), Region.EU, PlayerStatus.class, "playerStatus");

		_usWritePool = Utility.generatePool(_usConfig.getConnection(true, "DefaultConnection"));
		_euWritePool = Utility.generatePool(_euConfig.getConnection(true, "DefaultConnection"));
		
		boolean found = false;
		try
		{
			found = new File(new File(".").getCanonicalPath() + File.separator + "ClansVotifier.dat").exists();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		ClansVotifier = found;
	}

	@EventHandler
	public void handleVote(VotifierEvent event)
	{
		final Vote vote = event.getVote();
		final String playerName = vote.getUsername();

		System.out.println("New Vote: " + playerName);

		runAsync(new Runnable()
		{
			@Override
			public void run()
			{
				UUID uuid = UUIDFetcher.getUUIDOf(playerName);
				if (uuid == null)
				{
					System.out.println("Failed to load UUID of " + playerName + " from UUIDFetcher. Trying with database");
					uuid = _clientManager.loadUUIDFromDB(playerName);

					if (uuid == null)
					{
						System.out.println("Failed to load UUID from database. Giving up on " + playerName);
					}
				}

				final PlayerStatus usStatus = _usPlayerRepo.getElement(uuid.toString());
				final PlayerStatus euStatus = _euPlayerRepo.getElement(uuid.toString());

				System.out.println("Loaded " + playerName + " with uuid " + uuid);
				System.out.println("Attempting to award bonus");
				final UUID finalUuid = uuid;
				awardBonus(playerName, finalUuid, new Callback<Integer>()
				{
					@Override
					public void run(final Integer reward)
					{
						runSync(new Runnable()
						{
							@Override
							public void run()
							{
								if (usStatus != null)
								{
									System.out.println("Found " + playerName + " on US " + usStatus.getServer());
									notifyServer(playerName, reward, Region.US, usStatus.getServer());
								}

								if (euStatus != null)
								{
									System.out.println("Found " + playerName + " on EU " + euStatus.getServer());
									notifyServer(playerName, reward, Region.EU, euStatus.getServer());
								}
							}
						});
					}
				});
			}
		});
		System.out.println();
		System.out.println();

//		UUID uuid = _clientManager.loadUUIDFromDB(playerName);
//		if (uuid != null)
//		{
//			System.out.println("Found UUID:" + uuid.toString());
//			if (playerName.equalsIgnoreCase("Phinary"))
//			{
//				System.out.println("award bonus");
//				awardBonus(uuid);
//			}
//		}
//		else
//		{
//			System.out.println("Failed to load UUID for player: " + playerName);
//		}

//		PlayerStatus usStatus = _usPlayerRepo.getElement(playerName);
//		if (usStatus != null)
//		{
//			System.out.println("Found on US Server: " + usStatus.getServer());
//			writePool = _usWritePool;
//			serverName = usStatus.getServer();
//		}
//
//		PlayerStatus euStatus = _euPlayerRepo.getElement(playerName);
//		if (euStatus != null)
//		{
//			System.out.println("Found on EU Server: " + euStatus.getServer());
//			writePool = _euWritePool;
//			serverName = euStatus.getServer();
//		}

		// Currently we just notify all servers, and the server with the player on it can deal with it
//		notifyServer(playerName, true);
	}

	private void notifyServer(String playerName, int reward, Region region, String targetServer)
	{
		JedisPool writePool = region == Region.EU ? _euWritePool : _usWritePool;

		VotifierCommand command = new VotifierCommand(playerName, reward, ClansVotifier, targetServer);
		publishCommand(command, writePool);
	}

	private void awardBonus(final String playerName, final UUID uuid, final Callback<Integer> onComplete)
	{
		DSLContext create = DSL.using(DBPool.getAccount(), SQLDialect.MYSQL);

		Record1<Integer> idRecord = create.select(Tables.accounts.id).from(Tables.accounts).where(Tables.accounts.uuid.eq(uuid.toString())).fetchOne();
		if (idRecord != null)
		{
			final int accountId = idRecord.value1();
			final BonusRecord client = _bonusManager.getRepository().loadRecord(playerName, accountId);
			final int homeServerId = _bonusManager.getRepository().loadClansServerId(accountId);
			if (homeServerId == -1 && ClansVotifier)
			{
				return;
			}
			final BonusAmount amount = ClansVotifier ? _bonusManager.getClansVoteBonusAmount(homeServerId) : _bonusManager.getVoteBonusAmount(client.getVoteStreak());

			_bonusManager.getRepository().attemptVoteBonus(accountId, ClansVotifier, new Callback<Pair<Boolean, Date>>()
			{
				@Override
				public void run(Pair<Boolean, Date> pair)
				{
					if (pair.getLeft())
					{
						// Reward Amount
						final int gems = amount.getTotalGems();
						final int gold = amount.getTotalGold();
						final int shards = amount.getTotalShards();
						final int tickets = amount.getTickets();
						int experience = amount.getTotalExperience();
						int oldChests = amount.getOldChests();
						int ancientChests = amount.getAncientChests();
						int mythicalChests = amount.getMythicalChests();
						int illuminatedChests = amount.getIlluminatedChests();
						int omegaChests = amount.getOmegaChests();

						if (oldChests > 0)
						{
							_inventoryManager.addItemToInventoryForOffline(data ->
							{
								if (data)
								{
									System.out.println("Gave " + oldChests + " old chest(s) to " + playerName);
								}
								else
								{
									System.out.println("Failed to give " + oldChests + " old chest(s) to " + playerName);
								}
							}, accountId, TreasureType.OLD.getItemName(), oldChests);
						}

//						if (ancientChests > 0)
//						{
//							_inventoryManager.addItemToInventoryForOffline(data ->
//							{
//								if (data)
//								{
//									System.out.println("Gave " + ancientChests + " ancient chest(s) to " + playerName);
//								}
//								else
//								{
//									System.out.println("Failed to give " + ancientChests + " ancient chest(s) to " + playerName);
//								}
//							}, accountId, TreasureType.ANCIENT.getItemName(), ancientChests);
//						}
//
//						if (mythicalChests > 0)
//						{
//							_inventoryManager.addItemToInventoryForOffline(data ->
//							{
//								if (data)
//								{
//									System.out.println("Gave " + mythicalChests + " mythical chest(s) to " + playerName);
//								}
//								else
//								{
//									System.out.println("Failed to give " + mythicalChests + " mythical chest(s) to " + playerName);
//								}
//							}, accountId, TreasureType.MYTHICAL.getItemName(), mythicalChests);
//						}
//
//						if (illuminatedChests > 0)
//						{
//							_inventoryManager.addItemToInventoryForOffline(data ->
//							{
//								if (data)
//								{
//									System.out.println("Gave " + illuminatedChests + " illuminated chest(s) to " + playerName);
//								}
//								else
//								{
//									System.out.println("Failed to give " + illuminatedChests + " illuminated chest(s) to " + playerName);
//								}
//							}, accountId, TreasureType.ILLUMINATED.getItemName(), illuminatedChests);
//						}
//
//						if (omegaChests > 0)
//						{
//							_inventoryManager.addItemToInventoryForOffline(data ->
//							{
//								if (data)
//								{
//									System.out.println("Gave " + omegaChests + " omega chest(s) to " + playerName);
//								}
//								else
//								{
//									System.out.println("Failed to give " + omegaChests + " omega chest(s) to " + playerName);
//								}
//							}, accountId, TreasureType.OMEGA.getItemName(), omegaChests);
//						}

						if (gems > 0)
						{
							_donationManager.rewardCurrency(GlobalCurrency.GEM, playerName, uuid, "Votifier", gems, data ->
							{
								if (data)
								{
									System.out.println("Gave " + gems + " gems to " + playerName);
								}
								else
								{
									System.out.println("Failed to give " + gems + " gems to " + playerName);
								}
							});
						}

						if (gold > 0)
						{
							Set<Integer> serverIds = new HashSet<>();
							serverIds.addAll(amount.getGold().getServerIds());
							serverIds.addAll(amount.getBonusGold().getServerIds());
							for (Integer serverId : serverIds)
							{
								int goldCount = amount.getGold().getGoldFor(serverId) + amount.getBonusGold().getGoldFor(serverId);
								_donationManager.getGoldRepository().rewardGold(data ->
								{
									if (data)
									{
										System.out.println("Gave " + goldCount + " gold to " + playerName + " on clans server id " + serverId);
									}
									else
									{
										System.out.println("Failed to give " + goldCount + " gold to " + playerName + " on clans server id " + serverId);
									}
								}, serverId, accountId, goldCount);
							}
						}

						if (shards > 0)
						{
							_donationManager.rewardCurrency(GlobalCurrency.TREASURE_SHARD, playerName, uuid, "Votifier", shards, data ->
							{
								if (data)
								{
									System.out.println("Gave " + shards + " shards to " + playerName);
								}
								else
								{
									System.out.println("Failed to give " + shards + " shards to " + playerName);
								}
							});
						}

						if (experience > 0)
						{
							_statsManager.incrementStat(accountId, "Global.ExpEarned", experience);
							System.out.println("Gave " + experience + " experience to " + playerName);
						}
						
						if (tickets > 0)
						{
							client.setTickets(client.getTickets() + tickets);
						}

						// Check if we need to reset vote streak
						_bonusManager.updateVoteStreak(client);
						client.setVotetime(pair.getRight());

						// Update Streak
						_bonusManager.incrementVoteStreak(client);

						client.store();
						System.out.println("Awarded " + tickets + " carl ticket(s) to " + playerName);
						onComplete.run(ClansVotifier ? amount.getTotalGold() : amount.getTotalGems());
					}
					else
					{
						System.out.println(playerName + " attempted to vote, vote bonus returned false!");
					}
				}
			});
		}
	}

	private void publishCommand(final ServerCommand serverCommand, final JedisPool writePool)
	{
		new Thread(new Runnable()
		{
			public void run()
			{
				try (Jedis jedis = writePool.getResource())
				{
					String commandType = serverCommand.getClass().getSimpleName();
					String serializedCommand = Utility.serialize(serverCommand);
					jedis.publish("commands.server" + ":" + commandType, serializedCommand);
				}
			}
		}).start();
	}
}