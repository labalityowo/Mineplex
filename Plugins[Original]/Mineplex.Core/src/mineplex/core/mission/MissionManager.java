package mineplex.core.mission;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.MiniDbClientPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.CoreClient;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.achievement.leveling.rewards.LevelReward;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.donation.DonationManager;
import mineplex.core.game.GameDisplay;
import mineplex.core.mission.commands.DebugMissionCommand;
import mineplex.core.mission.commands.SetMissionsCommand;
import mineplex.core.mission.commands.ViewMissionsCommand;
import mineplex.core.mission.ui.MissionShop;
import mineplex.core.newnpc.NPC;
import mineplex.core.newnpc.NewNPCManager;
import mineplex.core.newnpc.SimpleNPC;
import mineplex.core.newnpc.event.NPCInteractEvent;
import mineplex.core.stats.StatsManager;

@ReflectivelyCreateMiniPlugin
public class MissionManager extends MiniDbClientPlugin<MissionClient>
{

	public enum Perm implements Permission
	{
		VIEW_MISSION_COMMAND,
		SET_MISSIONS_COMMAND,
		DEBUG_MISSION_COMMAND,
	}

	public enum BonusPerm implements Permission
	{
		MISSION_BONUS_0_5,
		MISSION_BONUS_1,
		MISSION_BONUS_1_5,
		MISSION_BONUS_2,
		MISSION_BONUS_2_5
	}

	private static final int MAX_DAILY = 5;
	private static final int MAX_WEEKLY = 3;
	private static final int MAX_TOTAL = MAX_DAILY + MAX_WEEKLY;

	private static final String NPC_METADATA = "MISSION_NPC";
	private static final String NPC_NAME = C.cGreenB + "Sam The Slime";

	private final DonationManager _donationManager;
	private final NewNPCManager _npcManager;
	private final StatsManager _statsManager;

	private final List<MissionContext> _missions;
	private final Set<MissionTracker> _missionTrackers;
	private final MissionRepository _repository;
	private final MissionShop _shop;

	private Supplier<Boolean> _canIncrement;
	private boolean _debug;

	private MissionManager()
	{
		super("Mission");

		_donationManager = require(DonationManager.class);
		_npcManager = require(NewNPCManager.class);
		_statsManager = require(StatsManager.class);

		_missions = new ArrayList<>();
		_missionTrackers = new HashSet<>();
		_repository = new MissionRepository();
		_shop = new MissionShop(this);

		MissionPopulator.populateMissions(this);
		populateTrackers();

		generatePermissions();

		runSyncTimer(this::processUnsavedProgress, 20, 100);
	}

	private void generatePermissions()
	{
		PermissionGroup.PLAYER.setPermission(Perm.VIEW_MISSION_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.SET_MISSIONS_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.DEBUG_MISSION_COMMAND, true, true);
		PermissionGroup.ULTRA.setPermission(BonusPerm.MISSION_BONUS_0_5, true, true);
		PermissionGroup.HERO.setPermission(BonusPerm.MISSION_BONUS_1, true, true);
		PermissionGroup.LEGEND.setPermission(BonusPerm.MISSION_BONUS_1_5, true, true);
		PermissionGroup.TITAN.setPermission(BonusPerm.MISSION_BONUS_2, true, true);
		PermissionGroup.ETERNAL.setPermission(BonusPerm.MISSION_BONUS_2_5, true, true);

		if (UtilServer.isTestServer())
		{
			PermissionGroup.QAM.setPermission(Perm.SET_MISSIONS_COMMAND, true, true);
			PermissionGroup.QAM.setPermission(Perm.DEBUG_MISSION_COMMAND, true, true);
		}
	}

	@Override
	public void addCommands()
	{
		addCommand(new ViewMissionsCommand(this));
		addCommand(new SetMissionsCommand(this));
		addCommand(new DebugMissionCommand(this));
	}

	private void populateTrackers()
	{
		registerTrackers();
	}

	void addMission(MissionContext mission)
	{
		_missions.add(mission);
	}

	public void registerTrackers(MissionTracker... trackers)
	{
		for (MissionTracker tracker : trackers)
		{
			registerEvents(tracker);
			_missionTrackers.add(tracker);
		}
	}

	public void clearTrackers(Predicate<MissionTracker> removeIf)
	{
		_missionTrackers.removeIf(tracker ->
		{
			if (removeIf.test(tracker))
			{
				tracker.cleanup();
				UtilServer.Unregister(tracker);
				return true;
			}

			return false;
		});
	}

	@Override
	public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
	{
		MissionClient client = Get(uuid);
		TimeZone utc = TimeZone.getTimeZone("UTC");
		// Why UK? because we want Monday to be the first day of the week
		Locale uk = Locale.UK;
		Calendar now = Calendar.getInstance(utc, uk);
		int nonEventMissions = 0;

		while (resultSet.next())
		{
			int missionId = resultSet.getInt("missionId");

			MissionContext<?> context = getMission(missionId);

			if (context == null)
			{
				continue;
			}

			// Here we attempt to fix a duplication bug caused by players joining US and EU at the same time.
			// How? well we know that players can have a maximum of 8 (non-event) missions total. So in that case
			// when they join a server with more than they should we'll clear everything from their 9th mission
			// onwards. But isn't that going to remove missions with higher ids? No, look down and you'll see that
			// we sort the missions by the time they were started.
			if (!context.isEventMission() && ++nonEventMissions > MAX_TOTAL)
			{
				runAsync(() -> _repository.clearMission(accountId, missionId));
				continue;
			}

			int progress = resultSet.getInt("progress");
			int length = resultSet.getInt("length");
			int x = resultSet.getInt("x");
			int y = resultSet.getInt("y");
			long startTime = resultSet.getLong("startTime");
			byte completeStatus = resultSet.getByte("complete");

			PlayerMission mission = new PlayerMission<>(context, MissionLength.values()[length], x, y, progress, false);

			Calendar start = Calendar.getInstance(utc, uk);
			start.setTimeInMillis(startTime);
			int lengthField = mission.getLength().getCalendarField();

			if (completeStatus == PlayerMission.COMPLETE)
			{
				mission.reward();
			}
			else if (completeStatus == PlayerMission.DISCARDED)
			{
				mission.discard();
			}

			if (lengthField != -1 && now.get(lengthField) != start.get(lengthField) && completeStatus != PlayerMission.ACTIVE)
			{
				runAsync(() -> _repository.clearMission(accountId, missionId));
			}
			else
			{
				client.startMission(mission);
			}
		}
	}

	@Override
	protected MissionClient addPlayer(UUID uuid)
	{
		return new MissionClient();
	}

	@Override
	public String getQuery(int accountId, String uuid, String name)
	{
		return "SELECT * FROM accountMissions WHERE accountId=" + accountId + " ORDER BY startTime;";
	}

	@EventHandler(priority = EventPriority.LOW)
	public void playerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		double rankBonus = getRankBonus(player);

		for (PlayerMission mission : Get(player).getMissions())
		{
			mission.createRewards(rankBonus);
		}

		selectNewMissions(player);
	}

	public void startMission(Player player, PlayerMission mission)
	{
		MissionClient client = Get(player);
		client.startMission(mission);

		_repository.addQueryToQueue(MissionRepository.startMission(ClientManager.getAccountId(player), () -> mission.createRewards(getRankBonus(player)), mission));
	}

	private void completeMission(Player player, PlayerMission<?> mission)
	{
		if (mission.hasRewarded())
		{
			return;
		}

		mission.reward();

		runAsync(() ->
		{
			if (_repository.completeMission(ClientManager.getAccountId(player), mission.getId()))
			{
				runSync(() ->
				{
					player.sendMessage("");
					player.sendMessage("  " + mission.getLength().getChatColour() + C.Bold + mission.getLength().getName() + " Mission Complete! " + C.mElem + C.Bold + mission.getName());
					player.sendMessage("  " + C.cGray + mission.getDescription());
					player.sendMessage("");

					for (LevelReward reward : mission.getRewards())
					{
						reward.claim(player);
						player.sendMessage(" - " + reward.getDescription());
					}

					player.sendMessage("");
					player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, (float) Math.random());

					_statsManager.incrementStat(player, mission.getLength().getStatName(), 1);
				});
			}
		});
	}

	public <T> void incrementProgress(Player player, int amount, MissionTrackerType trackerType, GameDisplay game, T data)
	{
		incrementProgress(player, amount, trackerType, game, data, 0);
	}

	@SuppressWarnings("unchecked")
	public <T> void incrementProgress(Player player, int amount, MissionTrackerType trackerType, GameDisplay game, T data, int y)
	{
		if (_canIncrement != null && !_canIncrement.get() || amount <= 0)
		{
			return;
		}

		MissionClient client = Get(player);

		client.getMissions().forEach(mission ->
		{
			if (mission.getTrackerType() != trackerType || mission.hasRewarded() || mission.isDiscarded() || !mission.canProgress(game) || !mission.validateData(data) || !mission.validateY(y))
			{
				return;
			}

			if (_debug)
			{
				player.sendMessage(F.main(getName(), F.name(mission.getName()) + " incremented " + F.count(amount) + "."));
			}

			int currentProgress = mission.getCurrentProgress() + mission.getUnsavedProgress();
			int requiredProgress = mission.getRequiredProgress();

			for (double requirement = 1; requirement > 0; requirement -= 0.25)
			{
				if (canInform(currentProgress, amount, requiredProgress, requirement))
				{
					if (requirement == 1)
					{
						break;
					}

					inform(player, mission, requirement);
					break;
				}
			}

			mission.incrementProgress(amount);
		});
	}

	private boolean canInform(int currentProgress, int amount, int requiredProgress, double requirement)
	{
		int progressToGet = (int) Math.ceil(requiredProgress * requirement);

		return currentProgress + amount >= progressToGet && currentProgress < progressToGet;
	}

	private void inform(Player player, PlayerMission mission, double percentage)
	{
		player.sendMessage(F.main(getName(), F.name(mission.getName()) + " is " + F.count((int) (percentage * 100) + "%") + " complete."));
	}

	private void processUnsavedProgress()
	{
		for (Player player : UtilServer.getPlayersCollection())
		{
			saveProgress(player);
		}

		runAsync(_repository::bulkProcess);
	}

	public void setMissions(Player player, Map<MissionLength, List<MissionContext<?>>> missions)
	{
		int accountId = ClientManager.getAccountId(player);
		double rankBonus = getRankBonus(player);
		MissionClient client = Get(player);

		player.sendMessage(F.main(getName(), "Clearing old missions..."));
		client.getMissions().forEach(mission ->
		{
			if (mission.getLength() == MissionLength.EVENT)
			{
				return;
			}

			runAsync(() -> _repository.clearMission(accountId, mission.getId()));
		});
		client.getMissions().clear();

		missions.forEach((length, contexts) ->
		{
			contexts.forEach(context ->
			{
				player.sendMessage(F.main(getName(), "Starting " + F.name(context.getName()) + "..."));
				PlayerMission<?> mission = length.createFromContext(context);
				_repository.addQueryToQueue(MissionRepository.startMission(accountId, () ->
				{
					mission.createRewards(rankBonus);
					client.startMission(mission);
				}, mission));
			});
		});
	}

	public void discardMission(Player player, PlayerMission mission)
	{
		if (mission.hasRewarded() || mission.isDiscarded())
		{
			return;
		}

		mission.discard();
		player.sendMessage(F.main(getName(), "Discarded " + F.name(mission.getName()) + ". You will receive a new one " + mission.getLength().getResetWhen() + "."));

		runAsync(() -> _repository.discardMission(ClientManager.getAccountId(player), mission.getId()));

		ItemStack itemStack = new ItemStack(Material.SLIME_BALL);

		_npcManager.getNPCs(NPC_METADATA).forEach(npc ->
		{
			Location location = npc.getEntity().getLocation().add(0, 1, 0);

			if (!location.getWorld().equals(player.getWorld()))
			{
				return;
			}

			location.getWorld().playSound(location, Sound.SLIME_WALK, 1, (float) (0.5 + Math.random() / 2));
			UtilParticle.PlayParticleToAll(ParticleType.SLIME, location, 1, 1, 1, 0, 8, ViewDist.NORMAL);

			for (int i = 0; i < 8; i++)
			{
				Item item = UtilItem.dropItem(itemStack, location, false, false, 40, false);
				item.setVelocity(new Vector(Math.random() - 0.5, 1, Math.random() - 0.5).multiply(0.2));
			}
		});
	}

	private void selectNewMissions(Player player)
	{
		MissionClient client = Get(player);
		int started = 0;

		started += selectNewMissions(player, client, MissionLength.DAY, MAX_DAILY);
		started += selectNewMissions(player, client, MissionLength.WEEK, MAX_WEEKLY);

		for (MissionContext<?> context : getSelectableMissions(true))
		{
			if (client.has(context))
			{
				continue;
			}

			startMission(player, MissionLength.EVENT.createFromContext(context));
			started++;
		}

		if (started > 0)
		{
			player.sendMessage(F.main(getName(), "You started " + F.count(started) + " new mission" + (started == 1 ? "" : "s") + ". Visit " + NPC_NAME + C.mBody + " to view them."));
			runAsync(_repository::bulkProcess);
		}
	}

	private int selectNewMissions(Player player, MissionClient client, MissionLength length, int max)
	{
		int current = client.getMissionsOf(length);

		if (current >= max)
		{
			return 0;
		}

		List<MissionContext> missions = getSelectableMissions(false);
		missions.removeIf(client::has);
		int started = 0;

		for (int i = current; i < max; i++)
		{
			MissionContext<?> context = UtilAlg.Random(missions);

			if (context == null)
			{
				break;
			}

			missions.remove(context);
			startMission(player, length.createFromContext(context));
			started++;
		}

		return started;
	}

	private List<MissionContext> getSelectableMissions(boolean eventsOnly)
	{
		return _missions.stream()
				.filter(mission ->
				{
					if (eventsOnly)
					{
						return mission.isEventMission();
					}
					else
					{
						return !mission.isEventMission();
					}
				})
				.collect(Collectors.toList());
	}

	private void saveProgress(Player player)
	{
		MissionClient client = Get(player);
		Map<PlayerMission, Integer> unsaved = client.saveProgress();

		if (unsaved.isEmpty())
		{
			return;
		}

		int accountId = ClientManager.getAccountId(player);

		unsaved.forEach((mission, progress) ->
		{
			boolean complete = mission.isComplete();

			_repository.addQueryToQueue(MissionRepository.incrementProgress(accountId, () ->
			{
				if (complete)
				{
					runSync(() -> completeMission(player, mission));
				}
			}, progress, mission));
		});
	}

	public MissionContext getMission(int id)
	{
		return _missions.stream()
				.filter(mission -> mission.getId() == id)
				.findFirst()
				.orElse(null);
	}

	private double getRankBonus(Player player)
	{
		CoreClient client = ClientManager.Get(player);
		double bonus = 0;

		for (BonusPerm perm : BonusPerm.values())
		{
			if (client.hasPermission(perm))
			{
				bonus += 0.5;
			}
		}

		return bonus;
	}

	public void createNPC(Location location)
	{
		NPC npc = SimpleNPC.of(location, Slime.class, NPC_METADATA, 3);

		_npcManager.addNPC(npc);

		LivingEntity entity = npc.getEntity();

		entity.setCustomName(C.cGreenB + NPC_NAME);
		entity.setCustomNameVisible(true);
	}

	@EventHandler
	public void npcInteract(NPCInteractEvent event)
	{
		if (event.getNpc().getMetadata().equals(NPC_METADATA))
		{
			Player player = event.getPlayer();

			player.playSound(player.getLocation(), Sound.SLIME_WALK2, 1, 1);
			_shop.attemptShopOpen(player);
		}
	}

	public void setCanIncrement(Supplier<Boolean> canIncrement)
	{
		_canIncrement = canIncrement;
	}

	public boolean toggleDebug()
	{
		_debug = !_debug;

		return _debug;
	}

	public DonationManager getDonationManager()
	{
		return _donationManager;
	}

	public StatsManager getStatsManager()
	{
		return _statsManager;
	}

	public MissionShop getShop()
	{
		return _shop;
	}
}
