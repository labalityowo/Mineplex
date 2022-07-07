package mineplex.core.achievement;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClient;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.achievement.command.StatsCommand;
import mineplex.core.achievement.ui.AchievementShop;
import mineplex.core.donation.DonationManager;
import mineplex.core.elo.EloManager;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.gadget.types.LevelPrefixGadget;
import mineplex.core.incognito.IncognitoManager;
import mineplex.core.stats.PlayerStats;
import mineplex.core.stats.StatsManager;
import mineplex.core.stats.event.StatChangeEvent;

public class AchievementManager extends MiniPlugin
{
	public enum Perm implements Permission
	{
		FAKE_LEVEL_50,
		FAKE_LEVEL_30,
		FAKE_LEVEL_15,
		FAKE_LEVEL_5,
		SEE_FULL_STATS,
		STATS_COMMAND,
	}

	private final CoreClientManager _clientManager;
	private final IncognitoManager _incognitoManager;
	private final StatsManager _statsManager;
	private final EloManager _eloManager;
	private GadgetManager _gadgetManager;

	private final AchievementShop _shop;

	private final Map<String, Map<Achievement, AchievementLog>> _log = new HashMap<>();

	public AchievementManager(StatsManager statsManager, CoreClientManager clientManager, DonationManager donationManager, IncognitoManager incognitoManager, EloManager eloManager)
	{
		super("Achievement Manager", statsManager.getPlugin());

		_incognitoManager = incognitoManager;
		_statsManager = statsManager;
		_eloManager = eloManager;
		_clientManager = clientManager;
		_shop = new AchievementShop(this, _statsManager, clientManager, donationManager, "Achievement");

		generatePermissions();
	}

	private void generatePermissions()
	{
		PermissionGroup.MOD.setPermission(Perm.SEE_FULL_STATS, true, true);
		PermissionGroup.MOD.setPermission(Perm.FAKE_LEVEL_5, true, true);
		PermissionGroup.SRMOD.setPermission(Perm.FAKE_LEVEL_15, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.FAKE_LEVEL_30, true, true);
		PermissionGroup.LT.setPermission(Perm.FAKE_LEVEL_50, true, true);
		PermissionGroup.PLAYER.setPermission(Perm.STATS_COMMAND, true, true);
	}

	public AchievementData get(Player player, Achievement type)
	{
		return get(player.getUniqueId(), type);
	}

	public AchievementData get(UUID playerUUID, Achievement type)
	{
		return get(_statsManager.Get(playerUUID), type);
	}

	public AchievementData get(PlayerStats stats, Achievement type)
	{
		int exp = 0;

		for (String stat : type.getStats())
		{
			exp += stats.getStat(stat);
		}

		return type.getLevelData(exp);
	}

	// AchievementManager and GadgetManager depend on each other (which is a nightmare)
	// In theory once everything can be created reflectively this can go
	public void setGadgetManager(GadgetManager gadgetManager)
	{
		_gadgetManager = gadgetManager;
	}

	public EloManager getEloManager()
	{
		return _eloManager;
	}

	@EventHandler
	public void informLevelUp(StatChangeEvent event)
	{
		Player player = event.getPlayer();

		for (Achievement type : Achievement.values())
		{
			for (String stat : type.getStats())
			{
				if (stat.equalsIgnoreCase(event.getStatName()))
				{
					_log.computeIfAbsent(player.getName(), k -> new HashMap<>());

					//Record that achievement has leveled up
					if (type.getLevelData(event.getValueAfter()).getLevel() > type.getLevelData(event.getValueBefore()).getLevel())
					{
						//Add new
						if (!_log.get(player.getName()).containsKey(type))
						{
							_log.get(player.getName()).put(type, new AchievementLog(event.getValueAfter() - event.getValueBefore(), true));
						}
						//Edit previous
						else
						{
							AchievementLog log = _log.get(player.getName()).get(type);
							log.Amount += event.getValueAfter() - event.getValueBefore();
							log.LevelUp = true;
						}

					}
					//Record that there has been changes in this Achievement
					else if (!_log.get(player.getName()).containsKey(type))
					{
						//Add new
						if (!_log.get(player.getName()).containsKey(type))
						{
							_log.get(player.getName()).put(type, new AchievementLog(event.getValueAfter() - event.getValueBefore(), false));
						}
						//Edit previous
						else
						{
							AchievementLog log = _log.get(player.getName()).get(type);
							log.Amount += event.getValueAfter() - event.getValueBefore();
						}
					}
				}
			}
		}
	}

	@Override
	public void addCommands()
	{
		addCommand(new StatsCommand(this));
	}

	public void openShop(Player player)
	{
		openShop(player, player);
	}

	public void openShop(Player player, Player target)
	{
		openShop(player, target.getName(), _statsManager.Get(target));
	}

	public void openShop(Player player, String targetName, PlayerStats targetStats)
	{
		_shop.attemptShopOpen(player, targetName, targetStats);
	}

	@EventHandler
	public void fixPressE(PlayerJoinEvent event)
	{
		event.getPlayer().awardAchievement(org.bukkit.Achievement.OPEN_INVENTORY);
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_log.remove(event.getPlayer().getName());
	}

	public void clearLog(Player player)
	{
		_log.remove(player.getName());
	}

	public Map<Achievement, AchievementLog> getLog(Player player)
	{
		return _log.get(player.getName());
	}

	public boolean hasCategory(Player player, Achievement[] required)
	{
		if (required == null || required.length == 0)
		{
			return false;
		}

		for (Achievement cur : required)
		{
			if (get(player, cur).getLevel() < cur.getMaxLevel())
			{
				return false;
			}
		}

		return true;
	}

	public int getMineplexLevelNumber(Player sender)
	{
		return getMineplexLevelNumber(sender, true);
	}

	public int getMineplexLevelNumber(Player sender, boolean fakeLevels)
	{
		CoreClient client = _clientManager.Get(sender);
		int level = get(sender, Achievement.GLOBAL_MINEPLEX_LEVEL).getLevel();
		
		if (fakeLevels)
		{
			if (client.hasPermission(Perm.FAKE_LEVEL_50))
			{
				level = Math.max(level, 50 + get(sender, Achievement.GLOBAL_GEM_HUNTER).getLevel());
			}
			else if (client.hasPermission(Perm.FAKE_LEVEL_30))
			{
				level = Math.max(level, 30 + get(sender, Achievement.GLOBAL_GEM_HUNTER).getLevel());
			}
			else if (client.hasPermission(Perm.FAKE_LEVEL_15))
			{
				level = Math.max(level, 15);
			}
			else if (client.hasPermission(Perm.FAKE_LEVEL_5))
			{
				level = Math.max(level, 5);
			}
		}

		return level;
	}

	public String getMineplexLevel(Player sender)
	{
		Gadget gadget = _gadgetManager == null ? null : _gadgetManager.getActive(sender, GadgetType.LEVEL_PREFIX);
		String prefix;
		int level = getMineplexLevelNumber(sender);

		if (gadget != null)
		{
			prefix = ((LevelPrefixGadget) gadget).getPrefixType().getChatColor().toString() + level;
		}
		else
		{
			prefix = Achievement.getExperienceString(level);
		}

		return prefix;
	}

	public CoreClientManager getClientManager()
	{
		return _clientManager;
	}

	public IncognitoManager getIncognito()
	{
		return _incognitoManager;
	}

	public StatsManager getStatsManager()
	{
		return _statsManager;
	}
}