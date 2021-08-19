package mineplex.core.bonuses;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.EntityCreeper;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import mineplex.core.MiniClientPlugin;
import mineplex.core.account.CoreClient;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.ILoginProcessor;
import mineplex.core.account.event.ClientUnloadEvent;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.achievement.leveling.LevelingManager;
import mineplex.core.bonuses.animations.AnimationCarl;
import mineplex.core.bonuses.commands.AnimationCommand;
import mineplex.core.bonuses.commands.GuiCommand;
import mineplex.core.bonuses.commands.PowerPlayCommand;
import mineplex.core.bonuses.commands.TicketCommand;
import mineplex.core.bonuses.event.CarlSpinnerEvent;
import mineplex.core.bonuses.gui.BonusGui;
import mineplex.core.bonuses.gui.SpinGui;
import mineplex.core.bonuses.gui.buttons.PowerPlayClubButton;
import mineplex.core.bonuses.redis.VoteHandler;
import mineplex.core.bonuses.redis.VotifierCommand;
import mineplex.core.common.MinecraftVersion;
import mineplex.core.common.Pair;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.donation.DonationManager;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.hologram.Hologram;
import mineplex.core.hologram.HologramManager;
import mineplex.core.inventory.ClientItem;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.npc.Npc;
import mineplex.core.npc.NpcManager;
import mineplex.core.pet.PetManager;
import mineplex.core.poll.PollManager;
import mineplex.core.powerplayclub.PowerPlayClubRepository;
import mineplex.core.recharge.Recharge;
import mineplex.core.stats.StatsManager;
import mineplex.core.thank.ThankManager;
import mineplex.core.treasure.reward.TreasureRewardManager;
import mineplex.core.treasure.types.TreasureType;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.youtube.YoutubeManager;
import mineplex.database.Tables;
import mineplex.database.tables.records.BonusRecord;
import mineplex.serverdata.commands.ServerCommandManager;
import mineplex.serverdata.database.DBPool;

public class BonusManager extends MiniClientPlugin<BonusClientData> implements ILoginProcessor
{
	private static final TimeZone TIMEZONE = TimeZone.getTimeZone("UTC");

	private static long timeOffSet = 0;

	public enum Perm implements Permission
	{
		ETERNAL_BONUS,
		TITAN_BONUS,
		LEGEND_BONUS,
		HERO_BONUS,
		ULTRA_BONUS,
		MONTHLY_BONUS,
		ALLOW_COMMAND,
		ANIMATION_COMMAND,
		GUI_COMMAND,
		POWER_PLAY_COMMAND,
		TICKET_COMMAND,
	}

	private ArrayList<Object> _pendingExplosions = new ArrayList<>();
	private ArrayList<Player> _pendingExplosionsPlayers = new ArrayList<>();
	private final Map<UUID, Pair<String, Integer>> _homeServerMap = new ConcurrentHashMap<>();
	private Map<String, Boolean> _showCarl = new HashMap<>();
	private long _explode;
	private boolean _canVote;
	private boolean _animationRunning;

	public final boolean ClansBonus;

	public static long getSqlTime()
	{
		return getSqlTime(System.currentTimeMillis());
	}

	public static long getSqlTime(long currentTime)
	{
		return currentTime + timeOffSet;
	}

	public static long getLocalTime()
	{
		return System.currentTimeMillis();
	}

	public static long getLocalTime(long sqlTime)
	{
		return sqlTime - timeOffSet;
	}

	public void updateOffSet()
	{
		_repository.getTimeOffset(data -> timeOffSet = data);
	}

	private BonusRepository _repository;
	private CoreClientManager _clientManager;
	private InventoryManager _inventoryManager;
	private DonationManager _donationManager;
	private PollManager _pollManager;
	private NpcManager _npcManager;
	private HologramManager _hologramManager;
	private TreasureRewardManager _rewardManager;
	private StatsManager _statsManager;
	private YoutubeManager _youtubeManager;
	private PowerPlayClubRepository _powerPlayClubRepository;
	private ThankManager _thankManager;
	private LevelingManager _levelingManager;
	public boolean _enabled;
	private Npc _carlNpc;
	private Location _carlLocation;
	private AnimationCarl _animation;
	private int _visualTick;

	private List<String> _voteList;

	private String _creeperName;

	/**
	 * THIS SHOULD ONLY BE USED FOR VOTIFIER!
	 */
	public BonusManager(JavaPlugin plugin, CoreClientManager clientManager, DonationManager donationManager)
	{
		super("Bonus", plugin);
		_enabled = false;

		_repository = new BonusRepository(plugin, this, donationManager);
		_clientManager = clientManager;
		_donationManager = donationManager;
		_powerPlayClubRepository = new PowerPlayClubRepository(plugin, clientManager, donationManager);

		System.out.print("VOTIFIER: ");
		System.out.print("DONATION MANAGER - > " + _donationManager.toString());

		ClansBonus = _plugin.getClass().getSimpleName().equalsIgnoreCase("ClansHub");

		_voteList = new ArrayList<>();
		if (ClansBonus)
		{
			_voteList.add("http://cvote1.mineplex.com");
			_voteList.add("http://cvote2.mineplex.com");
			_voteList.add("http://cvote3.mineplex.com");
		}
		else
		{
			_voteList.add("http://vote1.mineplex.com");
			_voteList.add("http://vote2.mineplex.com");
		}

		_creeperName = "Carl";

		updateOffSet();

		generatePermissions();
	}

	public BonusManager(JavaPlugin plugin, Location carlLocation, CoreClientManager clientManager, DonationManager donationManager, PollManager pollManager, NpcManager npcManager, HologramManager hologramManager, StatsManager statsManager, InventoryManager inventoryManager, PetManager petManager, YoutubeManager youtubeManager, GadgetManager gadgetManager, ThankManager thankManager, String creeperName)
	{
		super("Bonus", plugin);
		_repository = new BonusRepository(plugin, this, donationManager);
		_clientManager = clientManager;
		_donationManager = donationManager;
		_npcManager = npcManager;
		_hologramManager = hologramManager;
		_inventoryManager = inventoryManager;
		_thankManager = thankManager;
		_levelingManager = require(LevelingManager.class);
		_creeperName = creeperName;

		if (gadgetManager == null)
		{
			System.out.print("GM NULL");
		}

		_rewardManager = require(TreasureRewardManager.class);

		_pollManager = pollManager;
		_statsManager = statsManager;
		_youtubeManager = youtubeManager;

		_powerPlayClubRepository = new PowerPlayClubRepository(plugin, _clientManager, _donationManager);

		ClansBonus = _plugin.getClass().getSimpleName().equalsIgnoreCase("ClansHub");

		_voteList = new ArrayList<>();
		if (ClansBonus)
		{
			_voteList.add("http://cvote1.mineplex.com");
			_voteList.add("http://cvote2.mineplex.com");
			_voteList.add("http://cvote3.mineplex.com");
		}
		else
		{
			_voteList.add("http://vote1.mineplex.com");
			_voteList.add("http://vote2.mineplex.com");
		}
		_canVote = true;

		if (npcManager != null)
		{
			_carlNpc = _npcManager.getNpcByName(_creeperName + " the Creeper");
			if (_carlNpc == null)
			{
				_enabled = false;
			}
			else
			{
				if (carlLocation != null)
				{
					_carlNpc.setLocation(carlLocation);
				}
				_enabled = true;
//				_animation = new AnimationCarl(_carlNpc.getEntity());
//				_animation.setRunning(false);
			}
		}
		else
		{
			_enabled = false;
		}

		clientManager.addStoredProcedureLoginProcessor(this);

		ServerCommandManager.getInstance().registerCommandType("VotifierCommand", VotifierCommand.class, new VoteHandler(this));

		updateOffSet();

		if (ClansBonus)
		{
			clientManager.addStoredProcedureLoginProcessor(new ILoginProcessor()
			{
				@Override
				public String getName()
				{
					return "clans-server-id-loader";
				}

				@Override
				public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
				{
					boolean hasRow = resultSet.next();
					if (hasRow)
					{
						_homeServerMap.put(uuid, Pair.create(resultSet.getString(1), resultSet.getInt(2)));
					}
				}

				@Override
				public String getQuery(int accountId, String uuid, String name)
				{
					return "SELECT cs.serverName, cs.id FROM accountClan INNER JOIN clans ON clans.id = accountClan.clanId INNER JOIN clanServer AS cs ON clans.serverId = cs.id WHERE accountClan.accountId = " + accountId + ";";
				}
			});
		}

		generatePermissions();
	}

	private void generatePermissions()
	{
		PermissionGroup.ULTRA.setPermission(Perm.MONTHLY_BONUS, true, true);
		PermissionGroup.ULTRA.setPermission(Perm.ULTRA_BONUS, true, true);
		PermissionGroup.HERO.setPermission(Perm.HERO_BONUS, true, true);
		PermissionGroup.LEGEND.setPermission(Perm.LEGEND_BONUS, true, true);
		PermissionGroup.TITAN.setPermission(Perm.TITAN_BONUS, true, true);
		PermissionGroup.ETERNAL.setPermission(Perm.ETERNAL_BONUS, true, true);

		//pm.setPermission(pm.getGroup("mod"), Perm.ALLOW_COMMAND, true, true);
		PermissionGroup.DEV.setPermission(Perm.ANIMATION_COMMAND, true, true);
		PermissionGroup.DEV.setPermission(Perm.GUI_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.POWER_PLAY_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.TICKET_COMMAND, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new GuiCommand(this));
		addCommand(new AnimationCommand(this));
		addCommand(new TicketCommand(this));
		addCommand(new PowerPlayCommand(this));
	}

	// Just keeping things up-to-date
	@EventHandler
	public void onSlow(UpdateEvent event)
	{
		if (event.getType() != UpdateType.MIN_16)
			return;
		updateOffSet();
	}

	public String getCreeperName()
	{
		return _creeperName;
	}

	public Pair<String, Integer> getClansHomeServer(Player player)
	{
		if (ClansBonus)
		{
			return _homeServerMap.getOrDefault(player.getUniqueId(), Pair.create("No Server", -1));
		}
		else
		{
			return Pair.create("No Server", -1);
		}
	}

	public void handleVote(final Player player, final int rewardReceived, final boolean clans)
	{
		final int accountId = _clientManager.getAccountId(player);

		runAsync(() -> _repository.getClientData(accountId, data -> runSync(() ->
		{
			BonusClientData oldData = Get(player);
			if (oldData != null)
			{
				data.setHologram(oldData.getHologram());
			}
			Set(player, data);

			if (clans)
			{
				_statsManager.incrementStat(player, "Global.ClansDailyVote", 1);
				addPendingExplosion(player, player.getName());
				UtilPlayer.message(player, F.main(_creeperName, "Thanks for voting for Mineplex Clans!"));
				UtilPlayer.message(player, F.main(_creeperName, "You received " + F.elem(rewardReceived + " Gold") + " on your home server and " + F.elem("1 Spinner Ticket") + "!"));
			}
			else
			{
				_statsManager.incrementStat(player, "Global.DailyVote", 1);
				addPendingExplosion(player, player.getName());
				UtilPlayer.message(player, F.main(_creeperName, "Thanks for voting for Mineplex!"));
				UtilPlayer.message(player, F.main(_creeperName, "You received " + F.elem(rewardReceived + " Gems") + " and " + F.elem("1 Spinner Ticket") + "!"));
			}
		})));
	}

	@EventHandler
	public void fireCreeper(UpdateEvent event)
	{
		if(event.getType() != UpdateType.SLOW)
			return;

		if(_pendingExplosions.isEmpty())
			return;

		if (_animationRunning)
			return;

		if (!_canVote)
			return;

		if (!_enabled)
			return;

		_animationRunning = true;
		_explode = System.currentTimeMillis();
		//_animation.setTicks(0);
		_canVote = false;
	}

	@EventHandler
	public void creeperAnimation(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		//_animation.itemClean();

		if (_canVote)
			return;

		if (!_enabled)
			return;

		if (!_animationRunning)
			return;

		Entity creeper = _carlNpc.getEntity();

		double elapsed = (System.currentTimeMillis() - _explode)/1000d;

		//Not Detonated
		if (elapsed < 1)
		{
			//Sound
			creeper.getWorld().playSound(creeper.getLocation(), Sound.CREEPER_HISS, (float)(0.5 + elapsed), (float)(0.5 + elapsed));

			IncreaseSize(creeper);
			return;
		}

//		if(!_animation.isRunning())
//		{
//			//Effect
//			UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION, creeper.getLocation(), 0, 0, 0, 0, 1, ViewDist.MAX, UtilServer.getPlayers());
//			creeper.getWorld().playSound(creeper.getLocation(), Sound.EXPLODE, 2f, 1f);
//			_animation.setType(_pendingExplosions.get(0));
//			_animation.setPlayer(_pendingExplosionsPlayers.get(0));
//			_animation.setRunning(true);
//		}
//
//		if(!_animation.isDone())
//			return;

		_animationRunning = false;
		DecreaseSize(creeper);
		_pendingExplosions.remove(0);
		_pendingExplosionsPlayers.remove(0);
		_canVote = true;
	}

//	@EventHandler
//	public void updateAnimation(UpdateEvent event)
//	{
//		if(event.getType() != UpdateType.TICK)
//			return;
//
//		if (!_enabled)
//			return;
//
//		if(!_animation.isRunning())
//			return;
//
//		_animation.run();
//	}

	public void DecreaseSize(Entity player)
	{
		if (!_enabled)
			return;

		((CraftEntity)_carlNpc.getEntity()).getHandle().getDataWatcher().watch(16, (byte) -1, EntityCreeper.META_FUSE_STATE, -1);
	}

	public void IncreaseSize(Entity player)
	{
		if (!_enabled)
			return;

		((CraftEntity)_carlNpc.getEntity()).getHandle().getDataWatcher().watch(16, (byte) 1, EntityCreeper.META_FUSE_STATE, 1);
	}

	// DAILY BONUS

	public static final long TIME_BETWEEN_BONUSES = 1000 * 60 * 60 * 20;
	public static final long DAILY_STREAK_RESET_TIME = 1000 * 60 * 60 * 12;
	public static final long VOTE_STREAK_RESET_TIME = 1000 * 60 * 60 * 24;

	public void attemptDailyBonus(final Player player, final BonusAmount amount, final boolean clans, final Callback<Boolean> result)
	{
		if (timeTillDailyBonus(player) > 0)
			result.run(false);

		getRepository().attemptDailyBonus(player, ClansBonus, r ->
		{
			if (r)
			{
				if (ClansBonus)
				{
					_statsManager.incrementStat(player, "Global.ClansDailyReward", 1);
				}
				else
				{
					incrementDailyStreak(player);
					_statsManager.incrementStat(player, "Global.DailyReward", 1);
				}
				awardBonus(player, amount);
				updateCreeperVisual(player, true, C.cAqua);
				UtilPlayer.message(player, F.main(_creeperName, "Come back tomorrow for more!"));
			}

			result.run(r);
		});
	}

	public long timeTillDailyBonus(Player player)
	{
		return nextDailyBonus(player) - getLocalTime();
	}

	// This calculates the the next daily bonus, IT HAS TO MATCH THE MYSQL STORED FUNCTION.
	public long nextDailyBonus(Player player)
	{
		Timestamp timestamp = ClansBonus ? Get(player).getClansDailyTime() : Get(player).getDailyTime();

		if (timestamp == null)
			return 0;

		long lastBonus = timestamp.getTime();

		return getLocalTime(lastBonus + TIME_BETWEEN_BONUSES);
	}

	// RANK BONUS
	public void attemptRankBonus(final Player player, final Callback<Boolean> result)
	{
		if (timeTillRankBonus(player) > 0)
			result.run(false);

		getRepository().attemptRankBonus(player, success ->
		{
			if (success)
			{
				awardBonus(player, getRankBonusAmount(player));
				updateCreeperVisual(player, true, C.cAqua);
				UtilPlayer.message(player, F.main(_creeperName, "Come back next month for more!"));
			}

			result.run(success);
		});
	}

	public void attemptCarlSpin(final Player player)
	{
		final BonusClientData clientData = Get(player);

		if (Recharge.Instance.use(player, "Carl Spin", 5000, false, false) && clientData.getTickets() > 0)
		{
			CarlSpinnerEvent event = new CarlSpinnerEvent(player);
			Bukkit.getServer().getPluginManager().callEvent(event);

			if (!event.isCancelled())
			{
				final int accountId = _clientManager.Get(player).getAccountId();

				_repository.attemptAddTickets(accountId, clientData, -1, data ->
				{
					if (data)
					{
						new SpinGui(_plugin, player, _rewardManager, BonusManager.this).openInventory();
					}
					else
					{
						UtilPlayer.message(player, F.main(_creeperName, "There was an error processing your request. Try again later"));
					}
				});
			}
		}
	}

	public long timeTillRankBonus(Player player)
	{
		return nextRankBonus(player) - getLocalTime();
	}

	// This calculates the the next rank bonus, IT HAS TO MATCH THE MYSQL STORED FUNCTION.
	public long nextRankBonus(Player player)
	{
		Date date = Get(player).getRankTime();

		if (date == null)
			return 0;

		long lastBonus = date.getTime();

		return getNextRankBonusTime(getLocalTime(lastBonus));
	}

	public void updateDailyStreak(Player player)
	{
		BonusClientData client = Get(player);
		if (client.getDailyStreak() > 0 && client.getDailyTime() != null)
		{
			long lastBonus = getLocalTime(client.getDailyTime().getTime());
			long timeLeft = getStreakTimeRemaining(lastBonus, BonusManager.DAILY_STREAK_RESET_TIME);

			if (timeLeft < 0)
			{
				client.setDailyStreak(0);
			}
		}
	}

	public void updateVoteStreak(BonusRecord client)
	{
		if (client.getVoteStreak() > 0 && client.getVotetime() != null)
		{
			long lastBonus = getLocalTime(client.getVotetime().getTime());
			long timeLeft = getStreakTimeRemaining(lastBonus, BonusManager.VOTE_STREAK_RESET_TIME);

			if (timeLeft < 0)
			{
				client.setVoteStreak(0);
			}
		}
	}

	public void incrementDailyStreak(Player player)
	{
		BonusClientData data = Get(player);

		data.setDailyStreak(data.getDailyStreak() + 1);

		if (data.getDailyStreak() > data.getMaxDailyStreak())
			data.setMaxDailyStreak(data.getDailyStreak());
	}

	public void incrementVoteStreak(BonusRecord client)
	{
		client.setVoteStreak(client.getVoteStreak() + 1);

		if (client.getVoteStreak() > client.getMaxVoteStreak())
		{
			client.setMaxVoteStreak(client.getVoteStreak());
		}
	}

	public boolean continueStreak(long localLastBonus, long extendTime)
	{
		long maxTime = localLastBonus + TIME_BETWEEN_BONUSES + extendTime;
		return System.currentTimeMillis() < maxTime;
	}

	public long getStreakTimeRemaining(long localLastBonus, long extendTime)
	{
		long maxTime = localLastBonus + TIME_BETWEEN_BONUSES + extendTime;
		return maxTime - System.currentTimeMillis();
	}

	public static long getNextRankBonusTime(long time)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(TIMEZONE);
		calendar.setTimeInMillis(time);

		calendar.add(Calendar.MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));

		return calendar.getTimeInMillis();
	}

	public int getDailyMultiplier(Player player)
	{
		BonusClientData client = Get(player);
		int streak = client.getDailyStreak();

		// Increase the multiplier by 5 for each day
		// in the player's streak, until they reach
		// 200 (aka 40 days)
		int multiplier = Math.min(200, 5 * streak);

		// Once they reach 40 days (the 200 cap from
		// above), begin adding 1 per day instead of 5.
		if (streak >= 40)
		{
			multiplier += (1 * (streak - 40));
		}

		return multiplier;
	}

	public int getVoteMultiplier(int streak)
	{
		int multiplier = Math.min(100, 5 * streak);

		if (streak >= 20)
		{
			multiplier += (1 * (streak - 40));
		}

		return multiplier;
	}

	public BonusAmount getDailyBonusAmount(Player player)
	{
		double mult = getDailyMultiplier(player) / 100.0;

		BonusAmount amount = new BonusAmount();
		int shards = 200;
		int gems = 200;
		int experience = 350;

		amount.setShards(shards);
		amount.setGems(gems);
		amount.setExperience(experience);
		amount.setBonusShards((int) (mult * shards));
		amount.setBonusGems((int) (mult * gems));
		amount.setBonusExperience((int) (mult * experience));

		return amount;
	}

	public BonusAmount getClansDailyBonusAmount(Player player)
	{
		BonusAmount amount = new BonusAmount();

		int serverId = getClansHomeServer(player).getRight();

		if (serverId != -1)
		{
			amount.setGold(serverId, 500);
		}

		return amount;
	}

	public BonusAmount getVoteBonusAmount(Player player)
	{
		return getVoteBonusAmount(Get(player).getVoteStreak());
	}

	public BonusAmount getVoteBonusAmount(int voteStreak)
	{
		double mult = getVoteMultiplier(voteStreak) / 100.0;

		BonusAmount amount = new BonusAmount();
		amount.setTickets(1);
		amount.setGems(400);
		amount.setBonusGems((int) (mult * 400));

		return amount;
	}

	public BonusAmount getClansVoteBonusAmount(Player player)
	{
		return getClansVoteBonusAmount(getClansHomeServer(player).getRight());
	}

	public BonusAmount getClansVoteBonusAmount(int serverId)
	{
		BonusAmount amount = new BonusAmount();

		if (serverId != -1)
		{
			amount.setTickets(1);
			amount.setGold(serverId, 1000);
		}

		return amount;
	}

	public BonusAmount getRankBonusAmount(Player player)
	{
		BonusAmount data = new BonusAmount();

		if (_clientManager.Get(player).hasPermission(Perm.ETERNAL_BONUS))
		{
			data.setIlluminatedChests(2);
			data.setMythicalChests(2);
			data.setOmegaChests(1);
		}
		else if (_clientManager.Get(player).hasPermission(Perm.TITAN_BONUS))
		{
			data.setMythicalChests(5);
		}
		else if (_clientManager.Get(player).hasPermission(Perm.LEGEND_BONUS))
		{
			data.setMythicalChests(3);
		}
		else if (_clientManager.Get(player).hasPermission(Perm.HERO_BONUS))
		{
			data.setMythicalChests(2);
		}
		else if (_clientManager.Get(player).hasPermission(Perm.ULTRA_BONUS))
		{
			data.setMythicalChests(1);
		}

		return data;
	}


	//VOTE
	public long timeTillVoteBonus(Player player)
	{
		return nextVoteTime(player) - getLocalTime();
	}

	// This calculates the the next vote bonus, IT HAS TO MATCH THE MYSQL STORED FUNCTION.
	public long nextVoteTime(Player player)
	{
		Date date = ClansBonus ? Get(player).getClansVoteTime() : Get(player).getVoteTime();
		if (date == null)
		{
			return 0;
		}
		long lastBonus = date.getTime();

		return getNextVoteTime(getLocalTime(lastBonus));
	}

	public void awardBonus(final Player player, BonusAmount amount)
	{
		final BonusClientData bonusClient = Get(player);
		CoreClient coreClient = _clientManager.Get(player);

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
			UtilPlayer.message(player, F.main(_creeperName, "Rewarded " + F.elem(oldChests + " Old Chests")));
			_inventoryManager.Get(player).addItem(new ClientItem(_inventoryManager.getItem(TreasureType.OLD.getItemName()), oldChests));
		}

		if (ancientChests > 0)
		{
			UtilPlayer.message(player, F.main(_creeperName, "Rewarded " + F.elem(ancientChests + " Ancient Chests")));
			_inventoryManager.Get(player).addItem(new ClientItem(_inventoryManager.getItem(TreasureType.ANCIENT.getItemName()), ancientChests));
		}

		if (mythicalChests > 0)
		{
			UtilPlayer.message(player, F.main(_creeperName, "Rewarded " + F.elem(mythicalChests + " Mythical Chests")));
			_inventoryManager.Get(player).addItem(new ClientItem(_inventoryManager.getItem(TreasureType.MYTHICAL.getItemName()), mythicalChests));
		}

		if (illuminatedChests > 0)
		{
			UtilPlayer.message(player, F.main(_creeperName, "Rewarded " + F.elem(illuminatedChests + " Illuminated Chests")));
			_inventoryManager.Get(player).addItem(new ClientItem(_inventoryManager.getItem(TreasureType.ILLUMINATED.getItemName()), illuminatedChests));
		}

		if (omegaChests > 0)
		{
			UtilPlayer.message(player, F.main(_creeperName, "Rewarded " + F.elem(omegaChests + " Omega Chests")));
			_inventoryManager.Get(player).addItem(new ClientItem(_inventoryManager.getItem(TreasureType.OMEGA.getItemName()), omegaChests));
		}

		if (gems > 0)
		{
			UtilPlayer.message(player, F.main(_creeperName, "Rewarded " + F.elem(gems + " Gems")));
			_donationManager.rewardCurrencyUntilSuccess(GlobalCurrency.GEM, player, "Earned", gems);
		}

		if (gold > 0)
		{
			UtilPlayer.message(player, F.main(_creeperName, "Rewarded " + F.elem(gold + " Gold")));
			Set<Integer> serverIds = new HashSet<>();
			serverIds.addAll(amount.getGold().getServerIds());
			serverIds.addAll(amount.getBonusGold().getServerIds());
			for (Integer serverId : serverIds)
			{
				_donationManager.getGoldRepository().rewardGold(null, serverId, coreClient.getAccountId(), amount.getGold().getGoldFor(serverId) + amount.getBonusGold().getGoldFor(serverId));
			}
		}

		if (shards > 0)
		{
			UtilPlayer.message(player, F.main(_creeperName, "Rewarded " + F.elem(shards + " Treasure Shards")));
			_donationManager.rewardCurrencyUntilSuccess(GlobalCurrency.TREASURE_SHARD, player, "Earned", shards);
		}

		if (tickets > 0)
		{
			UtilPlayer.message(player, F.main(_creeperName, "Rewarded " + F.elem(tickets + " Carl Spin Ticket")));
			final int accountId = coreClient.getAccountId();
			runAsync(() ->
			{
				try
				{
					final int newTickets = DSL.using(DBPool.getAccount(), SQLDialect.MYSQL).update(Tables.bonus).set(Tables.bonus.tickets, Tables.bonus.tickets.add(tickets)).
							where(Tables.bonus.accountId.eq(accountId)).returning(Tables.bonus.tickets).fetchOne().getTickets();
					runSync(() -> bonusClient.setTickets(newTickets));
				}
				catch (Exception e)
				{
					System.out.println("Failed to award ticket to player: " + player);
					e.printStackTrace();
				}
			});
		}

		if (experience > 0)
		{
			_statsManager.incrementStat(player, "Global.ExpEarned", experience);
			UtilPlayer.message(player, F.main(_creeperName, "Rewarded " + F.elem(experience + " Experience")));
		}
	}

	public static long getNextVoteTime(long time)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(TIMEZONE);
		calendar.setTimeInMillis(time);

		calendar.add(Calendar.DAY_OF_YEAR, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		return calendar.getTimeInMillis();
	}

	public boolean canVote(Player player)
	{
		long nextVoteTime = nextVoteTime(player);
		return System.currentTimeMillis() >= nextVoteTime;
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void openGui(PlayerInteractAtEntityEvent event)
	{
		if (!_enabled)
			return;

		attemptOpenCarlGUI(event.getPlayer(), event.getRightClicked());
	}

	@EventHandler
	public void openGui(EntityDamageByEntityEvent event)
	{
		if (!_enabled)
			return;

		if (event.getDamager() instanceof Player)
		{
			attemptOpenCarlGUI((Player) event.getDamager(), event.getEntity());
		}
	}

	private void attemptOpenCarlGUI(Player player, Entity entity)
	{
		if (_carlNpc == null || _carlNpc.getEntity() == null)
		{
			System.err.println("Carl is missing! (carlNpc=" + _carlNpc + ")");
			return;
		}

		if (entity.equals(_carlNpc.getEntity()))
		{
			updateDailyStreak(player);
			new BonusGui(_plugin, player, this, _rewardManager, _youtubeManager, _thankManager).openInventory();
		}
	}

	public boolean canDaily(Player player)
	{
		long nextDailyTime = nextDailyBonus(player);
		return System.currentTimeMillis() >= nextDailyTime;
	}

	public boolean canRank(Player player)
	{
		long nextRankTime = nextRankBonus(player);
		return System.currentTimeMillis() >= nextRankTime;
	}

	@EventHandler
	public void join(final PlayerJoinEvent event)
	{
		runSyncLater(() ->
		{
			if (event.getPlayer().isOnline())
				updateCreeperVisual(event.getPlayer(), true, C.cAqua);
		}, 10);
	}

	public void updateCreeperVisual(Player player, boolean updateDataWatcher, String rewardPrefix)
	{
		if (!_enabled)
			return;

		BonusClientData client = Get(player);

		int availableRewards = 0;

		if (canVote(player)) availableRewards++;
		if (_youtubeManager.canYoutube(player)) availableRewards++;
		if (canRank(player) && _clientManager.Get(player).hasPermission(Perm.MONTHLY_BONUS) && isPastAugust()) availableRewards++;
		if (canDaily(player)) availableRewards++;
		if (getPollManager().getNextPoll(_pollManager.Get(player), _clientManager.Get(player).getPrimaryGroup()) != null) availableRewards++;
		if (_thankManager.Get(player).getThankToClaim() > 0) availableRewards++;
		if (PowerPlayClubButton.isAvailable(player, _powerPlayClubRepository)) availableRewards++;
		availableRewards += _levelingManager.getUnclaimedLevels(player);

		Hologram hologram;

		if (client.getHologram() == null)
		{
			double yAdd = 2.3;
			if (!UtilPlayer.getVersion(player).atOrAbove(MinecraftVersion.Version1_9))
			{
				yAdd = 2.45;
			}
			hologram = new Hologram(_hologramManager, _carlNpc.getLocation().clone().add(0, yAdd, 0), "");
			hologram.setHologramTarget(Hologram.HologramTarget.WHITELIST);
			hologram.addPlayer(player);
			client.setHologram(hologram);
			hologram.start();
		}
		else
		{
			hologram = client.getHologram();
		}

		if (availableRewards > 0)
		{
			// Hologram
			String text = rewardPrefix + availableRewards + " Reward" + (availableRewards > 1 ? "s" : "") + " to Claim";
			hologram.setText(text);

			if (updateDataWatcher)
			{
				// Charged
				DataWatcher watcher = new DataWatcher(null);
				watcher.a(0, (byte) 0, EntityCreeper.META_ENTITYDATA, (byte) 0);
				watcher.a(1, (short) 300, EntityCreeper.META_AIR, 0);
				watcher.a(17, (byte) 1, EntityCreeper.META_POWERED, true);
				PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(_carlNpc.getEntity().getEntityId(), watcher, true);

				UtilPlayer.sendPacket(player, packet);
			}
		}
		else
		{
			String text = C.cGray + "No Rewards";
			hologram.setText(text);

			if (updateDataWatcher)
			{
				// Charged
				DataWatcher watcher = new DataWatcher(null);
				watcher.a(17, (byte) 0, EntityCreeper.META_POWERED, false);
				PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(_carlNpc.getEntity().getEntityId(), watcher, true);

				UtilPlayer.sendPacket(player, packet);
			}
		}
	}

	@EventHandler
	public void updateCreeper(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER || !_enabled)
			return;

		for (Player player : UtilServer.getPlayers())
		{
			String prefix = _visualTick % 2 == 0 ? C.cAqua : C.cDAqua;
			updateCreeperVisual(player, false, prefix);
		}

		_visualTick++;
	}

	@Override
	protected BonusClientData addPlayer(UUID uuid)
	{
		return new BonusClientData();
	}

	public BonusRepository getRepository()
	{
		return _repository;
	}

	public CoreClientManager getClientManager()
	{
		return _clientManager;
	}

	@Override
	@EventHandler
	public void UnloadPlayer(final ClientUnloadEvent event)
	{
		final BonusClientData clientData = Get(event.getUniqueId());

		if (clientData.getHologram() != null)
		{
			clientData.getHologram().stop();
		}

		// Save streaks
		runAsync(() -> _repository.saveStreak(clientData.getAccountId(), clientData));

		super.UnloadPlayer(event);
	}

	public void addPendingExplosion(Player player, Object obj)
	{
		_pendingExplosions.add(obj);
		_pendingExplosionsPlayers.add(player);
	}

	public PollManager getPollManager()
	{
		return _pollManager;
	}

	public YoutubeManager getYoutubeManager()
	{
		return _youtubeManager;
	}

	@EventHandler
	public void Join(final PlayerJoinEvent event)
	{
		runSyncLater(() -> _showCarl.put(event.getPlayer().getName(), true), 200);
	}

	@EventHandler
	public void Quit(PlayerQuitEvent event)
	{
		_showCarl.remove(event.getPlayer().getName());
		// Removes from allow command map
		UtilPlayer.removeAllowedCommands(event.getPlayer());
	}

	@EventHandler
	public void carlUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		for (Player player : UtilServer.getPlayers())
		{
			if (Recharge.Instance.use(player, "Carl Inform", 240000, false, false))
			{
				if (_pollManager.hasPoll(player) || canVote(player) || _youtubeManager.canYoutube(player) || (canRank(player) && _clientManager.Get(player).hasPermission(Perm.MONTHLY_BONUS) && isPastAugust()) || canDaily(player) || PowerPlayClubButton.isAvailable(player, _powerPlayClubRepository))
				{
					if (_showCarl.containsKey(player.getName()))
					{
						if (_plugin.getClass().getSimpleName().equalsIgnoreCase("Hub") || _plugin.getClass().getSimpleName().equalsIgnoreCase("ClansHub"))
						{
							UtilPlayer.message(player, C.cDGreen + C.Bold + _creeperName + " the Creeper>" + C.cGreen + " Hey " + player.getName().replace("s", "sss") + "! I have sssome amazing rewardsss for you! Come sssee me!");
						}
					}
				}
			}
		}
	}

	public String getVoteLink()
	{
		long sqlTime = getSqlTime();
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(sqlTime);
		int date = calendar.get(Calendar.DAY_OF_YEAR);
		int index = date % _voteList.size();
		return _voteList.get(index);
	}

	/**
	 * Used for disabling rank rewards during first month of release
	 * @return
	 */
	public boolean isPastAugust()
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(TIMEZONE);
		calendar.setTimeInMillis(getSqlTime());

		if (calendar.get(Calendar.YEAR) == 2015 && calendar.get(Calendar.MONTH) == Calendar.AUGUST)
			return false;

		return true;
	}

	@Override
	public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
	{
		Set(uuid, _repository.loadData(accountId, resultSet));
	}

	@Override
	public String getQuery(int accountId, String uuid, String name)
	{
		return "SELECT * FROM bonus WHERE accountId = '" + accountId + "';";
	}

	public ThankManager getThankManager()
	{
		return _thankManager;
	}

	public DonationManager getDonationManager()
	{
		return _donationManager;
	}

	public PowerPlayClubRepository getPowerPlayClubRepository()
	{
		return _powerPlayClubRepository;
	}

	public InventoryManager getInventoryManager()
	{
		return _inventoryManager;
	}

	public LevelingManager getLevelingManager()
	{
		return _levelingManager;
	}

	public Location getCarlLocation()
	{
		return _carlLocation;
	}

	public void setCarlLocation(Location carlLocation)
	{
		_carlLocation = carlLocation;
	}

	public Npc getCarl()
	{
		return _carlNpc;
	}
}