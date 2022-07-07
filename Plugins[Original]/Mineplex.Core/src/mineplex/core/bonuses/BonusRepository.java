package mineplex.core.bonuses;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.SQLDialect;
import org.jooq.TableField;
import org.jooq.impl.DSL;

import mineplex.core.common.Pair;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.UtilServer;
import mineplex.core.donation.DonationManager;
import mineplex.core.recharge.Recharge;
import mineplex.database.Tables;
import mineplex.database.tables.records.BonusRecord;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.ResultSetCallable;

public class BonusRepository extends RepositoryBase
{
	private static String CREATE_BONUS_TABLE = "CREATE TABLE IF NOT EXISTS bonus (accountId INT NOT NULL AUTO_INCREMENT, dailytime TIMESTAMP NULL DEFAULT NULL, clansdailytime TIMESTAMP NULL DEFAULT NULL, ranktime DATE NULL DEFAULT NULL, votetime DATE NULL DEFAULT NULL, clansvotetime DATE NULL DEFAULT NULL, PRIMARY KEY (accountId), FOREIGN KEY (accountId) REFERENCES accounts(id));";
	private BonusManager _manager;
	private DonationManager _donationManager;

	public BonusRepository(JavaPlugin plugin, BonusManager bonusManager, DonationManager donationManager)
	{
		super(DBPool.getAccount());
		_manager = bonusManager;
		_donationManager = donationManager;
	}

	public BonusRecord loadRecord(String playerName, int accountId)
	{
		DSLContext create = jooq();

		BonusRecord record = create.selectFrom(Tables.bonus).where(Tables.bonus.accountId.eq(accountId)).fetchOne();

		if (record == null)
		{
			// Need to create new record!
			record = create.newRecord(Tables.bonus);
			record.setAccountId(accountId);
			record.setDailyStreak(0);
			record.setMaxDailyStreak(0);
			record.setVoteStreak(0);
			record.setMaxVoteStreak(0);
			record.setTickets(0);
			record.store(); // Todo - is this necessary?
		}
		System.out.println("Loaded record. Daily time: " + record.getDailytime());
		return record;
	}
	
	public int loadClansServerId(int accountId)
	{
		try (Connection connection = getConnection())
		{
			PreparedStatement s = connection.prepareStatement("SELECT clans.serverId FROM accountClan INNER JOIN clans ON clans.id = accountClan.clanId WHERE accountClan.accountId = " + accountId + ";");
			ResultSet rs = s.executeQuery();
			boolean hasRow = rs.next();
			if (hasRow)
			{
				return rs.getInt(1);
			}
			else
			{
				return -1;
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return -1;
		}
	}

	public BonusClientData loadData(final int accountId, ResultSet resultSet) throws SQLException
	{
		BonusClientData clientData = new BonusClientData();
		clientData.setAccountId(accountId);

		boolean foundClient = false;
		while (resultSet.next())
		{
			foundClient = true;
			clientData.setDailyTime(resultSet.getTimestamp(2));
			clientData.setClansDailyTime(resultSet.getTimestamp(3));
			clientData.setRankTime(resultSet.getDate(4));
			clientData.setVoteTime(resultSet.getDate(5));
			clientData.setClansVoteTime(resultSet.getDate(6));
			clientData.setDailyStreak(resultSet.getInt(7));
			clientData.setMaxDailyStreak(resultSet.getInt(8));
			clientData.setVoteStreak(resultSet.getInt(9));
			clientData.setMaxVoteStreak(resultSet.getInt(10));
			clientData.setTickets(resultSet.getInt(11));
		}

		if (!foundClient)
		{
			UtilServer.runAsync(() -> executeInsert("INSERT IGNORE INTO bonus (accountId) VALUES (" + accountId + ")", null));
		}

		return clientData;
	}

	public void getDailyStreakRecord(Callback<StreakRecord> callback)
	{
		getStreakRecord(Tables.bonus.maxDailyStreak, callback);
	}

	public void getVoteStreakRecord(Callback<StreakRecord> callback)
	{
		getStreakRecord(Tables.bonus.maxVoteStreak, callback);
	}

	private void getStreakRecord(final TableField<BonusRecord, Integer> field, final Callback<StreakRecord> callback)
	{
		Bukkit.getScheduler().runTaskAsynchronously(_manager.getPlugin(), new Runnable()
		{
			@Override
			public void run()
			{
				final Record2<String, Integer> record = jooq().select(Tables.accounts.name, field)
						.from(Tables.bonus.join(Tables.accounts).on(Tables.bonus.accountId.eq(Tables.accounts.id)))
						.orderBy(field.desc()).limit(1).fetchOne();

				Bukkit.getScheduler().runTask(_manager.getPlugin(), new Runnable()
				{
					@Override
					public void run()
					{
						if (record.value1() != null && record.value2() != null)
						{
							callback.run(new StreakRecord(record.value1(), record.value2()));
						}
					}
				});
			}
		});
	}

	public void attemptAddTickets(final int accountId, final BonusClientData client, final int tickets, final Callback<Boolean> callback)
	{
		if (client.getTickets() + tickets < 0)
			callback.run(false);

		Bukkit.getScheduler().runTaskAsynchronously(_manager.getPlugin(), new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					DSLContext create = DSL.using(DBPool.getAccount(), SQLDialect.MYSQL);
					create.update(Tables.bonus).set(Tables.bonus.tickets, Tables.bonus.tickets.add(tickets)).
							where(Tables.bonus.accountId.eq(accountId)).execute();
					final int newTickets = create.select(Tables.bonus.tickets).from(Tables.bonus).where(Tables.bonus.accountId.eq(accountId)).fetchOne().value1();

					Bukkit.getScheduler().runTask(_manager.getPlugin(), new Runnable()
					{
						@Override
						public void run()
						{
							client.setTickets(newTickets);
							callback.run(true);
						}
					});
				}
				catch (Exception e)
				{
					e.printStackTrace();
					callback.run(false);
				}
			}
		});
	}

	public void attemptDailyBonus(final Player player, final boolean clans, final Callback<Boolean> result)
	{
		final int accountId = _manager.getClientManager().Get(player).getAccountId();
		final int serverId = _manager.getClansHomeServer(player).getRight();
		final int shards = 0;
		final int gems = 0;
		final int gold = 500;
		/*
		 * if (shards == 0 && gems == 0) { result.accept(false); return; }
		 */
		final JavaPlugin plug = _manager.getPlugin();

		Bukkit.getScheduler().runTaskAsynchronously(plug, new Runnable()
		{
			@Override
			public void run()
			{
				if (clans)
				{
					try (Connection connection = getConnection();
							CallableStatement callableStatement = connection.prepareCall("{call check_clans_daily(?, ?, ?, ?, ?)}"))
						{
							callableStatement.setInt(1, accountId);
							callableStatement.setInt(2, serverId);
							callableStatement.setInt(3, gold);
							callableStatement.registerOutParameter(4, java.sql.Types.BOOLEAN);
							callableStatement.registerOutParameter(5, java.sql.Types.TIMESTAMP);

							callableStatement.executeUpdate();

							final boolean pass = callableStatement.getBoolean(4);

							final Timestamp timeStamp = callableStatement.getTimestamp(5);

							Bukkit.getScheduler().runTask(plug, new Runnable()
							{
								@Override
								public void run()
								{

									if (pass)
									{
										_manager.Get(player).setClansDailyTime(new Timestamp(BonusManager.getSqlTime()));
										result.run(true);
									}
									else
									{
										Recharge.Instance.use(player, "AttemptDailyBonus", 1000 * 10, false, false);
										_manager.Get(player).setClansDailyTime(timeStamp);
										result.run(false);
									}
								}
							});
						}
						catch (Exception e)
						{
							Recharge.Instance.use(player, "AttemptDailyBonus", 1000 * 30, false, false);
							e.printStackTrace();
							result.run(false);
						}
				}
				else
				{
					try (Connection connection = getConnection();
						CallableStatement callableStatement = connection.prepareCall("{call check_daily(?, ?, ?, ?, ?)}"))
					{
						callableStatement.setInt(1, accountId);
						callableStatement.setInt(2, shards);
						callableStatement.setInt(3, gems);
						callableStatement.registerOutParameter(4, java.sql.Types.BOOLEAN);
						callableStatement.registerOutParameter(5, java.sql.Types.TIMESTAMP);

						callableStatement.executeUpdate();

						final boolean pass = callableStatement.getBoolean(4);

						final Timestamp timeStamp = callableStatement.getTimestamp(5);

						Bukkit.getScheduler().runTask(plug, new Runnable()
						{
							@Override
							public void run()
							{

								if (pass)
								{
									_manager.Get(player).setDailyTime(new Timestamp(BonusManager.getSqlTime()));
									result.run(true);
								}
								else
								{
									Recharge.Instance.use(player, "AttemptDailyBonus", 1000 * 10, false, false);
									_manager.Get(player).setDailyTime(timeStamp);
									result.run(false);
								}
							}
						});
					}
					catch (Exception e)
					{
						Recharge.Instance.use(player, "AttemptDailyBonus", 1000 * 30, false, false);
						e.printStackTrace();
						result.run(false);
					}
				}
			}
		});
	}

	@Deprecated
	public void giveTickets(final Player player, final Callback<Boolean> result)
	{
		final int accountId = _manager.getClientManager().Get(player).getAccountId();

		Bukkit.getScheduler().runTaskAsynchronously(_manager.getPlugin(), new Runnable()
		{
			@Override
			public void run()
			{
				final int newTickets = jooq().update(Tables.bonus).set(Tables.bonus.tickets, Tables.bonus.tickets.sub(-1)).
						where(Tables.bonus.accountId.eq(accountId)).returning(Tables.bonus.tickets).fetchOne().getTickets();

				Bukkit.getScheduler().runTask(_manager.getPlugin(), new Runnable()
				{
					@Override
					public void run()
					{


					}
				});

			}
		});
	}

	public void attemptRankBonus(final Player player, final Callback<Boolean> result)
	{
		if (!Recharge.Instance.usable(player, "AttemptRankBonus")) 
		{
			result.run(false);
			return;
		}

		final BonusAmount bonusAmount = _manager.getRankBonusAmount(player);
		final int accountId = _manager.getClientManager().Get(player).getAccountId();

		if (!_manager.getRankBonusAmount(player).isGreaterThanZero()) 
		{
			result.run(false);
			return;
		}

		UtilServer.runAsync(() ->
		{
			try (Connection connection = getConnection();
				CallableStatement callableStatement = connection.prepareCall("{call rankBonus(?, ?, ?, ?, ?, ?, ?, ?)}");
				)
			{
				callableStatement.setInt(1, accountId);
				callableStatement.setInt(2, bonusAmount.getShards());
				callableStatement.setInt(3, bonusAmount.getGems());
				callableStatement.setInt(4, bonusAmount.getMythicalChests());
				callableStatement.setInt(5, bonusAmount.getOmegaChests());
				callableStatement.setInt(6, bonusAmount.getIlluminatedChests());
				callableStatement.registerOutParameter(7, java.sql.Types.BOOLEAN);
				callableStatement.registerOutParameter(8, java.sql.Types.DATE);

				callableStatement.executeUpdate();

				final boolean pass = callableStatement.getBoolean(7);

				final Date date = callableStatement.getDate(8);

				UtilServer.runSync(() -> 
				{
					_manager.Get(player).setRankTime(date);

					if (pass)
					{
						result.run(true);
					}
					else
					{
						Recharge.Instance.use(player, "AttemptRankBonus", 1000 * 10, false, false);
						result.run(false);
					}
				});
			} 
			catch (Exception e) 
			{
				Recharge.Instance.use(player, "AttemptRankBonus", 1000 * 30, false, false);
				e.printStackTrace();
				System.out.println("Error : " + e.getMessage());
				result.run(false);
			}
		});
	}

	public void attemptVoteBonus(final int accountId, final boolean clans, final Callback<Pair<Boolean, Date>> result)
	{
		final int serverId = 0;
		final int shards = 0;
		final int gems = 0;
		final int gold = 1000;

		final JavaPlugin plug = _manager.getPlugin();

		Bukkit.getScheduler().runTaskAsynchronously(plug, new Runnable() {

			@Override
			public void run()
			{
				if (clans)
				{
					try (Connection connection = getConnection();
							CallableStatement callableStatement = connection.prepareCall("{call check_clans_vote(?, ?, ?, ?, ?)}")) {
						callableStatement.setInt(1, accountId);
						callableStatement.setInt(2, serverId);
						callableStatement.setInt(3, gold);
						callableStatement.registerOutParameter(4, Types.BOOLEAN);
						callableStatement.registerOutParameter(5, Types.DATE);

						callableStatement.executeUpdate();

						final boolean pass = callableStatement.getBoolean(4);
						final Date date = callableStatement.getDate(5);

						Bukkit.getScheduler().runTask(plug, new Runnable()
						{
							@Override
							public void run()
							{
								//							_manager.Get(player).setVoteTime(date);
								result.run(Pair.create(pass, date));
							}
						});
					} catch (Exception e) {
						e.printStackTrace();
						result.run(null);
					}
				}
				else
				{
					try (Connection connection = getConnection();
							CallableStatement callableStatement = connection.prepareCall("{call check_vote(?, ?, ?, ?, ?)}")) {
						callableStatement.setInt(1, accountId);
						callableStatement.setInt(2, shards);
						callableStatement.setInt(3, gems);
						callableStatement.registerOutParameter(4, Types.BOOLEAN);
						callableStatement.registerOutParameter(5, Types.DATE);

						callableStatement.executeUpdate();

						final boolean pass = callableStatement.getBoolean(4);
						final Date date = callableStatement.getDate(5);

						Bukkit.getScheduler().runTask(plug, new Runnable() {
							@Override
							public void run()
							{
								//							_manager.Get(player).setVoteTime(date);
								result.run(Pair.create(pass, date));
							}
						});
					} catch (Exception e) {
						e.printStackTrace();
						result.run(null);
					}
				}
			}
		});
	}

	public void getTimeOffset(final Callback<Long> callback)
	{
		final long startTime = System.currentTimeMillis();
		final Plugin plugin = _manager.getPlugin();

		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run()
			{
				executeQuery("SELECT CURRENT_TIMESTAMP", new ResultSetCallable() {
					@Override
					public void processResultSet(ResultSet resultSet) throws SQLException
					{
						resultSet.next();

						long theirTimeUnadjusted = resultSet.getTimestamp(1).getTime();

						long ourCurrentTime = System.currentTimeMillis();

						long latencyOffset = (ourCurrentTime - startTime) / 2;

						long theirTime = theirTimeUnadjusted - latencyOffset;

						final long offSet = theirTime - ourCurrentTime;

						Bukkit.getScheduler().runTask(plugin, new Runnable() {
							@Override
							public void run()
							{
								callback.run(offSet);
							}
						});
					}
				});
			}
		});
	}

	public void getClientData(final int accountId, final Callback<BonusClientData> callback)
	{
		String query = "SELECT * FROM bonus WHERE accountId = '" + accountId + "';";

		executeQuery(query, new ResultSetCallable()
		{
			@Override
			public void processResultSet(ResultSet resultSet) throws SQLException
			{
				BonusClientData clientData = loadData(accountId, resultSet);
				callback.run(clientData);
			}
		});
	}

	public void saveStreak(int accountId, BonusClientData clientData)
	{
		jooq().update(Tables.bonus).set(Tables.bonus.dailyStreak, clientData.getDailyStreak())
				.set(Tables.bonus.maxDailyStreak, clientData.getMaxDailyStreak())
				.set(Tables.bonus.voteStreak, clientData.getVoteStreak())
				.set(Tables.bonus.maxVoteStreak, clientData.getMaxVoteStreak())
				.where(Tables.bonus.accountId.eq(accountId)).execute();
	}
}
