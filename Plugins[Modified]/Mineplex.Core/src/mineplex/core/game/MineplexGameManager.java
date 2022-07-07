package mineplex.core.game;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import net.minecraft.server.v1_8_R3.DataWatcher.WatchableObject;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;

import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.core.MiniClientPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.CoreClient;
import mineplex.core.account.CoreClientManager;
import mineplex.core.achievement.Achievement;
import mineplex.core.achievement.AchievementManager;
import mineplex.core.common.MinecraftVersion;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.donation.DonationManager;
import mineplex.core.donation.Donor;
import mineplex.core.event.CustomTagEvent;
import mineplex.core.game.kit.GameKit;
import mineplex.core.game.kit.KitAvailability;
import mineplex.core.game.kit.KitOperations;
import mineplex.core.game.kit.KitRepository;
import mineplex.core.game.kit.LegacyKit;
import mineplex.core.game.kit.PlayerKitData;
import mineplex.core.game.kit.event.KitNPCInteractEvent;
import mineplex.core.game.kit.event.KitSelectEvent;
import mineplex.core.game.kit.ui.KitMainPage;
import mineplex.core.game.kit.ui.KitShop;
import mineplex.core.game.kit.upgrade.KitStat;
import mineplex.core.game.kit.upgrade.KitStatLog;
import mineplex.core.game.kit.upgrade.LinearUpgradeTree;
import mineplex.core.newnpc.NPC;
import mineplex.core.newnpc.NewNPCManager;
import mineplex.core.newnpc.event.NPCInteractEvent;
import mineplex.core.packethandler.IPacketHandler;
import mineplex.core.packethandler.PacketHandler;
import mineplex.core.packethandler.PacketInfo;
import mineplex.core.preferences.Preference;
import mineplex.core.preferences.PreferencesManager;
import mineplex.core.recharge.Recharge;
import mineplex.core.stats.event.PlayerStatsLoadedEvent;

@ReflectivelyCreateMiniPlugin
public class MineplexGameManager extends MiniClientPlugin<Map<GameKit, PlayerKitData>> implements IPacketHandler
{

	private final AchievementManager _achievementManager;
	private final CoreClientManager _clientManager;
	private final DonationManager _donationManager;
	private final NewNPCManager _npcManager;
	private final PreferencesManager _preferencesManager;

	private final List<GameKit> _kits;
	private final Map<NPC, GameKit> _kitNPCs;
	private final Map<Player, KitStatLog> _kitStatLog;

	private final KitRepository _repository;

	private final KitShop _shop;

	private MineplexGameManager()
	{
		super("Game");

		_achievementManager = require(AchievementManager.class);
		_clientManager = require(CoreClientManager.class);
		_donationManager = require(DonationManager.class);
		_npcManager = require(NewNPCManager.class);
		_preferencesManager = require(PreferencesManager.class);

		require(PacketHandler.class).addPacketHandler(this, PacketPlayOutEntityMetadata.class);

		_kits = Arrays.asList(GameKit.values());
		_kitNPCs = new HashMap<>(_kits.size());
		_kitStatLog = new HashMap<>();
		_repository = new KitRepository(this);
		_shop = new KitShop(this);
	}

	@Override
	protected Map<GameKit, PlayerKitData> addPlayer(UUID uuid)
	{
		return new HashMap<>();
	}

	public void unlock(Player player, GameKit kit)
	{
		runAsync(() ->
		{
			boolean result = new KitOperations(_repository, _clientManager.getAccountId(player))
					.unlockKit(kit.getId(), true)
					.execute();

			if (result)
			{
				Get(player).put(kit, new PlayerKitData(false));
				setActiveKit(player, kit);

				if (kit.getAvailability() != KitAvailability.Free)
				{
					player.sendMessage(F.main(_moduleName, "Unlocked " + F.name(kit.getFormattedName()) + "."));
				}
			}
			else
			{
				player.sendMessage(F.main(_moduleName, "There was an error processing your request for " + F.name(kit.getDisplayName()) + "."));
			}
		});
	}

	public boolean isUnlocked(Player player, GameKit kit)
	{
		return kit.getAvailability() == KitAvailability.Free || _preferencesManager.get(player).isActive(Preference.UNLOCK_KITS) && _clientManager.Get(player).hasPermission(Preference.UNLOCK_KITS) || ownsKit(player, kit);
	}

	public boolean ownsKit(Player player, GameKit kit)
	{
		return getPlayerKitData(player, kit).isPresent() || _donationManager.Get(player).ownsUnknownSalesPackage(kit.getDisplay().getKitGameName() + " " + kit.getDisplayName());
	}

	public boolean canUnlock(Player player, GameKit kit)
	{
		switch (kit.getAvailability())
		{
			case Gem:
				return _donationManager.Get(player).getBalance(GlobalCurrency.GEM) >= kit.getCost();
			case Achievement:
				Achievement[] achievements = kit.getAchievements();

				for (Achievement achievement : achievements)
				{
					if (_achievementManager.get(player, achievement).getLevel() < achievement.getMaxLevel())
					{
						return false;
					}
				}

				return true;
		}

		return kit.getAvailability() == KitAvailability.Free;
	}

	public boolean isActive(Player player, GameKit kit)
	{
		Optional<PlayerKitData> optional = getPlayerKitData(player, kit);

		return optional.isPresent() && optional.get().isActive();
	}

	public void setActiveKit(Player player, GameKit kit)
	{
		KitSelectEvent selectEvent = new KitSelectEvent(player, kit);
		UtilServer.CallEvent(selectEvent);

		if (selectEvent.isCancelled())
		{
			return;
		}

		runAsync(() ->
		{
			List<Integer> idsToDisable = _kits
					.stream()
					.filter(otherKit -> !otherKit.equals(kit) && otherKit.getDisplay().equals(kit.getDisplay()))
					.mapToInt(GameKit::getId)
					.boxed()
					.collect(Collectors.toList());

			boolean result = new KitOperations(_repository, _clientManager.getAccountId(player))
					.setActiveKit(kit.getId(), idsToDisable)
					.execute();

			if (result)
			{
				for (int id : idsToDisable)
				{
					getKitFrom(id).ifPresent(otherKit -> getPlayerKitData(player, otherKit).ifPresent(kitData -> kitData.setActive(false)));
				}

				getPlayerKitData(player, kit).ifPresent(kitData -> kitData.setActive(true));
				player.sendMessage(F.main(_moduleName, "Set " + F.name(kit.getFormattedName()) + " as your selected kit."));
				player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);
			}
			else
			{
				player.sendMessage(F.main(_moduleName, "There was an error processing your request."));
			}
		});
	}

	public Optional<GameKit> getActiveKit(Player player, GameDisplay display)
	{
		for (GameKit kit : _kits)
		{
			if (!kit.getDisplay().equals(display))
			{
				continue;
			}

			Optional<PlayerKitData> optional = getPlayerKitData(player, kit);

			if (optional.isPresent() && optional.get().isActive())
			{
				return Optional.of(kit);
			}
		}

		return Optional.empty();
	}

	public int getKitStat(Player player, GameKit kit, KitStat stat)
	{
		Optional<PlayerKitData> optional = getPlayerKitData(player, kit);
		return optional.isPresent() ? optional.get().getStats().getOrDefault(stat, 0) : 0;
	}

	public void incrementKitStat(Player player, GameKit kit, KitStat stat, int value)
	{
		if (value <= 0)
		{
			return;
		}

		runAsync(() ->
		{
			boolean result = new KitOperations(_repository, _clientManager.getAccountId(player))
					.incrementStat(kit.getId(), stat.getId(), value)
					.execute();

			if (result)
			{
				getPlayerKitData(player, kit).ifPresent(kitData -> kitData.getStats().put(stat, kitData.getStats().getOrDefault(stat, 0) + value));
			}
		});
	}

	public void setKitStat(Player player, GameKit kit, KitStat stat, int value)
	{
		if (value < 0)
		{
			return;
		}

		runAsync(() ->
		{
			boolean result = new KitOperations(_repository, _clientManager.getAccountId(player))
					.setStat(kit.getId(), stat.getId(), value)
					.execute();

			if (result)
			{
				getPlayerKitData(player, kit).ifPresent(kitData -> kitData.getStats().put(stat, value));
			}
		});
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void interactNPC(NPCInteractEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		NPC npc = event.getNpc();

		getKitFrom(npc).ifPresent(kit ->
		{
			Player player = event.getPlayer();

			if (UtilPlayer.isSpectator(player) || !Recharge.Instance.use(player, "Kit NPC Interact", 1000, false, false))
			{
				return;
			}

			KitNPCInteractEvent interactEvent = new KitNPCInteractEvent(player, npc, kit);
			UtilServer.CallEvent(interactEvent);

			if (interactEvent.isCancelled() || kit.isChampionsKit())
			{
				return;
			}

			if (event.isLeftClick() && !isActive(player, kit) && ownsKit(player, kit))
			{
				setActiveKit(player, kit);
			}
			else
			{
				openKitUI(player, kit);
			}
		});
	}

	public void openKitUI(Player player, GameKit kit)
	{
		_shop.openPageForPlayer(player, new KitMainPage(this, player, kit));
	}

	public void addKitNPC(NPC npc, GameKit kit)
	{
		_kitNPCs.put(npc, kit);
	}

	public void clearKitNPCs()
	{
		_kitNPCs.keySet().forEach(_npcManager::deleteNPC);
		_kitNPCs.clear();
	}

	private String getKitEntityName(Player player, GameKit kit)
	{
		CoreClient client = _clientManager.Get(player);
		Donor donor = _donationManager.Get(player);

		String entityName = kit.getFormattedName();

		if (!player.isOnline() || client == null || donor == null || isUnlocked(player, kit))
		{
			if (isActive(player, kit))
			{
				entityName += " ★";
			}

			return entityName;
		}

		switch (kit.getAvailability())
		{
			case Gem:
				entityName = kit.getAvailability().getColour() + entityName + C.Reset + " (" + GlobalCurrency.GEM.getString(kit.getCost()) + C.Reset + ")";
				break;
			case Achievement:
				entityName = kit.getAvailability().getColour() + entityName + C.Reset + " (" + C.cPurple + "Achievement Kit" + C.Reset + ")";
				break;
		}

		return entityName;
	}

	@Override
	public void handle(PacketInfo packetInfo)
	{
		// Only need to handle this for 1.9+
		if (!UtilPlayer.getVersion(packetInfo.getPlayer()).atOrAbove(MinecraftVersion.Version1_9))
		{
			return;
		}

		PacketPlayOutEntityMetadata packet = (PacketPlayOutEntityMetadata) packetInfo.getPacket();

		_kitNPCs.forEach((npc, kit) ->
		{
			LivingEntity entity = npc.getEntity();

			if (entity.getEntityId() == packet.a)
			{
				for (WatchableObject watchableObject : packet.b)
				{
					if (watchableObject.getIndex().equals(net.minecraft.server.v1_8_R3.Entity.META_CUSTOMNAME))
					{
						String customName = getKitEntityName(packetInfo.getPlayer(), kit);

						watchableObject.a(customName, customName);
						return;
					}
				}
			}
		});
	}

	@EventHandler
	public void customEntityName(CustomTagEvent event)
	{
		_kitNPCs.forEach((npc, kit) ->
		{
			LivingEntity entity = npc.getEntity();

			if (entity.getEntityId() == event.getEntityId())
			{
				String customName = getKitEntityName(event.getPlayer(), kit);

				if (customName != null)
				{
					event.setCustomName(customName);
				}
			}
		});
	}

	public Optional<PlayerKitData> getPlayerKitData(Player player, GameKit kit)
	{
		return Optional.ofNullable(Get(player).get(kit));
	}

	public Optional<GameKit> getKitFrom(int id)
	{
		return _kits
				.stream()
				.filter(kit -> kit.getId() == id)
				.findFirst();
	}

	private Optional<GameKit> getKitFrom(String legacyName)
	{
		return _kits
				.stream()
				.filter(kit ->
				{
					Optional<String> optional = kit.getLegacyName();
					return optional.isPresent() && optional.get().equals(legacyName);
				})
				.findFirst();
	}

	public Optional<GameKit> getKitFrom(NPC npc)
	{
		return Optional.ofNullable(_kitNPCs.get(npc));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerStatsLoad(PlayerStatsLoadedEvent event)
	{
		Player player = event.getPlayer();
		Map<GameKit, PlayerKitData> dataMap = Get(player);

		if (!dataMap.isEmpty())
		{
			return;
		}

		runAsync(() ->
		{
			if (!dataMap.isEmpty() || !player.isOnline())
			{
				return;
			}

			int accountId = _clientManager.getAccountId(player);

			if (accountId == -1)
			{
				return;
			}

			KitOperations operations = new KitOperations(_repository, accountId);

			player.sendMessage(F.main(_moduleName, "Converting your " + F.name("Kits") + " to our shiny new system. This will take a few seconds..."));

			List<LegacyKit> legacyKits = _repository.getLegacyKits(player.getUniqueId());

			if (legacyKits == null)
			{
				player.sendMessage(F.main(_moduleName, "Ruh roh scoob looks like we couldn't load your old kits."));
				return;
			}

			legacyKits.forEach(legacyKit ->
			{
				Optional<GameKit> optional = getKitFrom(legacyKit.getId());

				if (!optional.isPresent())
				{
					return;
				}

				GameKit kit = optional.get();

				if (kit.getAvailability() == KitAvailability.Gem && !ownsKit(player, kit) || kit.getAvailability() == KitAvailability.Achievement && !canUnlock(player, kit))
				{
					return;
				}

				int xp = LinearUpgradeTree.getTotalXpForLevel(legacyKit.getKitLevel()) + legacyKit.getKitXP(), upgradeLevel = legacyKit.getKitUpgradeLevel();

				operations.unlockKit(kit.getId(), false);
				operations.setStat(kit.getId(), KitStat.XP.getId(), xp);

				PlayerKitData kitData = new PlayerKitData(false);
				dataMap.put(kit, kitData);
				kitData.getStats().put(KitStat.XP, xp);

				if (upgradeLevel > 0)
				{
					operations.setStat(kit.getId(), KitStat.UPGRADE_LEVEL.getId(), upgradeLevel);
					kitData.getStats().put(KitStat.UPGRADE_LEVEL, upgradeLevel);
				}
			});

			// Gonna keep this commented out for now just incase something terrible happens
//			if (_repository.deleteLegacyKits(player.getUniqueId()))
//			{
			operations.execute();

			player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
			player.sendMessage(F.main(_moduleName, "We're all finished. All your " + F.name("Kits") + " were successfully transferred over! You may need to rejoin/switch servers in order for all of your kit levels and upgrades to apply!"));
//			}
//			else
//			{
//				player.sendMessage(F.main(_moduleName, "Ruh roh scoob looks like we couldn't delete your old kits."));
//			}
		}, 10);
	}

	public AchievementManager getAchievementManager()
	{
		return _achievementManager;
	}

	public CoreClientManager getClientManager()
	{
		return _clientManager;
	}

	public DonationManager getDonationManager()
	{
		return _donationManager;
	}

	public NewNPCManager getNpcManager()
	{
		return _npcManager;
	}

	public List<GameKit> getKits()
	{
		return _kits;
	}

	public Map<Player, KitStatLog> getKitStatLog()
	{
		return _kitStatLog;
	}

	public KitShop getShop()
	{
		return _shop;
	}
}
