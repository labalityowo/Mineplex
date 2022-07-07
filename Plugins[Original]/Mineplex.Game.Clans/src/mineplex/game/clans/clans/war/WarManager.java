package mineplex.game.clans.clans.war;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.thereallyoldscoreboardapiweshouldremove.ScoreboardManager;
import mineplex.core.thereallyoldscoreboardapiweshouldremove.elements.ScoreboardElement;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.Clans;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClanTips.TipType;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.ClansUtility;
import mineplex.game.clans.clans.event.ClanDisbandedEvent;
import mineplex.game.clans.clans.event.ClansPlayerDeathEvent;
import mineplex.game.clans.clans.war.command.WarPointsCommand;
import mineplex.game.clans.clans.war.event.WarSiegeEndEvent;
import mineplex.game.clans.clans.war.event.WarSiegeStartEvent;
import mineplex.game.clans.core.war.ClanWarData;

public class WarManager extends MiniPlugin implements ScoreboardElement
{
	public enum Perm implements Permission
	{
		WAR_POINT_COMMAND,
	}

	public static final int WAR_START_POINTS = 0;
	public static final int WAR_FINISH_POINTS = 25;
	public static final long INVADE_LENGTH = 60000L * 30; // 30 Minutes
	public static final long WAR_COOLDOWN = 60000L * 30; // 30 Minutes

	private final ClansManager _clansManager;

	/**
	 * Map of the active war sieges. This is indexed by the clan that is being besieged
	 */
	private Map<String, List<WarSiege>> _besiegedMap;
	private Map<String, List<WarSiege>> _besiegerMap;

	public WarManager(JavaPlugin plugin, ClansManager clansManager)
	{
		super("ClanWar Manager", plugin);
		_clansManager = clansManager;
		_besiegedMap = new HashMap<>();
		_besiegerMap = new HashMap<>();
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.PLAYER.setPermission(Perm.WAR_POINT_COMMAND, true, true);
	}

	public ClansManager getClansManager()
	{
		return _clansManager;
	}

	public List<WarSiege> getSiegesOn(ClanInfo besieged)
	{
		return _besiegedMap.get(besieged.getName());
	}

	public List<WarSiege> getSiegesFor(ClanInfo besieger)
	{
		return _besiegerMap.get(besieger.getName());
	}

	public boolean isBeingBesieged(ClanInfo clanInfo)
	{
		List<WarSiege> sieges = _besiegedMap.get(clanInfo.getName());
		return sieges != null && !sieges.isEmpty();
	}

	public boolean isBesieging(ClanInfo clanInfo)
	{
		List<WarSiege> sieges = _besiegerMap.get(clanInfo.getName());
		return sieges != null && !sieges.isEmpty();
	}
	
	public boolean isBeingBesiegedBy(ClanInfo besieged, ClanInfo besieger)
	{
		List<WarSiege> sieges = _besiegedMap.get(besieged.getName());
		if (sieges != null && !sieges.isEmpty())
		{
			for (WarSiege siege : sieges)
			{
				if (siege.getBesiegingClan().equals(besieger.getName()))
				{
					return true;
				}
			}
		}

		return false;
	}

	@EventHandler
	public void handleDeath(final ClansPlayerDeathEvent event)
	{
		if (!Clans.HARDCORE)
		{
			return;
		}
		ClanInfo deathClan = event.getPlayer().getClan();
		
		if (deathClan == null)
		{
			deathClan = _clansManager.leftRecently(event.getPlayer().getPlayer().getUniqueId(), 60000) == null ? deathClan : _clansManager.leftRecently(event.getPlayer().getPlayer().getUniqueId(), 60000).getLeft();
		}
		
		if (event.getPlayer() != null && deathClan != null)
		{
			if (event.getKiller() != null && event.getKiller().getClan() != null)
			{
				final ClanInfo clan = deathClan;
				final ClanInfo killerClan = event.getKiller().getClan();

				ClanWarData war = clan.getWarData(killerClan);
				if (war != null)
				{
					if (war.isOnCooldown())
					{
						// Ignore!
						return;
					}

					_clansManager.ClanTips.displayTip(TipType.DOMINANCE_RIP, event.getPlayer().getPlayer());
					_clansManager.ClanTips.displayTip(TipType.DOMINANCE_NOOICE, event.getKiller().getPlayer());

					// War already exists
					war.increment(killerClan.getName());
					ClansUtility.ClanRelation rel = _clansManager.getClanUtility().rel(clan, killerClan);
					_clansManager.messageClan(killerClan, F.main("Clans", "Your clan gained 1 War Point against " + rel.getColor(false) +
							clan.getName() + " " + C.Reset + "(" + killerClan.getFormattedWarPoints(clan) + C.Reset + ")"));
					_clansManager.messageClan(clan, F.main("Clans", "Your clan lost 1 War Point against " + rel.getColor(false) +
							killerClan.getName() + " " + C.Reset + "(" + clan.getFormattedWarPoints(killerClan) + C.Reset + ")"));
					checkWarComplete(war);

					ClanInfo clanA = clan.getName().equals(war.getClanA()) ? clan : killerClan;
					ClanInfo clanB = clan.equals(clanA) ? killerClan : clan;
					_clansManager.getClanDataAccess().updateWar(clanA, clanB, war, null);

					_clansManager.getScoreboard().refresh(killerClan);
					_clansManager.getScoreboard().refresh(clan);
				}
				else
				{
					// Need to create war
					_clansManager.getClanDataAccess().war(killerClan, clan, WAR_START_POINTS + 1, new Callback<ClanWarData>()
					{
						@Override
						public void run(ClanWarData data)
						{
							ClansUtility.ClanRelation rel = _clansManager.getClanUtility().rel(clan, killerClan);
							_clansManager.ClanTips.displayTip(TipType.DOMINANCE_RIP, event.getPlayer().getPlayer());
							_clansManager.ClanTips.displayTip(TipType.DOMINANCE_NOOICE, event.getKiller().getPlayer());
							_clansManager.messageClan(killerClan, F.main("Clans", "Your clan gained 1 War Point against " + rel.getColor(false) + clan.getName()));
							_clansManager.messageClan(clan, F.main("Clans", "Your clan lost 1 War Point against " + rel.getColor(false) + killerClan.getName()));

							_clansManager.getScoreboard().refresh(killerClan);
							_clansManager.getScoreboard().refresh(clan);
						}
					});
				}
			}
		}
	}

	private void checkWarComplete(ClanWarData war)
	{
		String besiegerClan = null;
		String besiegedClan = null;

		if (war.getClanAPoints() >= WAR_FINISH_POINTS)
		{
			besiegerClan = war.getClanA();
			besiegedClan = war.getClanB();
		}
		else if (war.getClanBPoints() >= WAR_FINISH_POINTS)
		{
			besiegerClan = war.getClanB();
			besiegedClan = war.getClanA();
		}

		if (besiegedClan != null && besiegerClan != null)
		{
			// Reset War to 0:0
			war.resetPoints();
			war.setCooldown(WAR_COOLDOWN);

			WarSiege siege = new WarSiege(besiegedClan, besiegerClan);
			startSiege(siege);
		}
	}

	private void startSiege(WarSiege siege)
	{
		String besieged = siege.getBesiegedClan();
		String besieger = siege.getBesiegingClan();

		addSiege(besieged, siege, _besiegedMap);
		addSiege(besieger, siege, _besiegerMap);

		WarSiegeStartEvent event = new WarSiegeStartEvent(siege);
		UtilServer.getServer().getPluginManager().callEvent(event);
	}

	private void addSiege(String name, WarSiege siege, Map<String, List<WarSiege>> siegeMap)
	{
		if (siegeMap.containsKey(name))
		{
			siegeMap.get(name).add(siege);
		}
		else
		{
			LinkedList<WarSiege> sieges = new LinkedList<>();
			sieges.add(siege);
			siegeMap.put(name, sieges);
		}
	}

	@EventHandler
	public void clearSieges(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		long currentTime = System.currentTimeMillis();

		Iterator<Map.Entry<String, List<WarSiege>>> iterator = _besiegedMap.entrySet().iterator();
		while (iterator.hasNext())
		{
			List<WarSiege> sieges = iterator.next().getValue();
			Iterator<WarSiege> siegeIterator = sieges.iterator();
			while (siegeIterator.hasNext())
			{
				WarSiege siege = siegeIterator.next();
				if (currentTime >= siege.getEndTime())
				{
					WarSiegeEndEvent endEvent = new WarSiegeEndEvent(siege);
					Bukkit.getServer().getPluginManager().callEvent(endEvent);

					List<WarSiege> besiegerList = _besiegerMap.get(siege.getBesiegingClan());
					if (besiegerList != null)
					{
						besiegerList.remove(siege);
						if (besiegerList.isEmpty())
						{
							_besiegerMap.remove(siege.getBesiegingClan());
						}
					}

					siegeIterator.remove();
				}
			}

			if (sieges.isEmpty())
				iterator.remove();
		}
	}

	@EventHandler
	public void onSiegeStart(WarSiegeStartEvent event)
	{
		Bukkit.broadcastMessage(F.main("War", F.elem(event.getWarSiege().getBesiegingClan()) + " can now besiege " + F.elem(event.getWarSiege().getBesiegedClan())));
	}

	@EventHandler
	public void onSiegeEnd(WarSiegeEndEvent event)
	{
		Bukkit.broadcastMessage(F.main("War", F.elem(event.getWarSiege().getBesiegingClan()) + "'s siege against " + F.elem(event.getWarSiege().getBesiegedClan()) + " has ended."));

	}

	@Override
	public void addCommands()
	{
		addCommand(new WarPointsCommand(this));
	}

	@EventHandler
	public void cancelDisband(ClanDisbandedEvent event)
	{
		ClanInfo clan = event.getClan();
		if (isBeingBesieged(clan) || isBesieging(clan))
		{
			UtilPlayer.message(event.getDisbander(), F.main("Clans", "Clans cannot be disbanded in the middle of a siege"));
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlaceTNT(BlockPlaceEvent event)
	{
		if (event.getBlockPlaced().getType() == Material.TNT)
		{
			event.setCancelled(true);
			event.setBuild(false);
			UtilPlayer.message(event.getPlayer(), F.main("Clans", "TNT cannot be used outside of a siege cannon!"));
		}
	}
	
	@EventHandler
	public void onTNTDispense(BlockDispenseEvent event)
	{
		if (event.getItem().getType() == Material.TNT)
		{
			event.setCancelled(true);
		}
	}

	@Override
	public List<String> getLines(ScoreboardManager manager, Player player, List<String> out)
	{
		List<String> element = new ArrayList<String>();

		ClanInfo clan = _clansManager.getClan(player);

		if (clan != null)
		{
			List<WarSiege> besiegedList = _besiegedMap.get(clan.getName());
			List<WarSiege> besiegerList = _besiegerMap.get(clan.getName());

			if (besiegerList != null && !besiegerList.isEmpty())
			{
				for (WarSiege siege : besiegerList)
				{
					element.add(" ");
					element.add(C.cPurpleB + "Besieging");
					element.add("  " + siege.getBesiegedClan());
					element.add("  " + UtilTime.convertString(Math.max(siege.getTimeLeft(), 0), 1, UtilTime.TimeUnit.FIT));
				}
			}

			if (besiegedList != null && !besiegedList.isEmpty())
			{
				for (WarSiege siege : besiegedList)
				{
					element.add(" ");
					element.add(C.cRedB + "Besieged");
					element.add("  " + siege.getBesiegingClan());
					element.add("  " + UtilTime.convertString(Math.max(siege.getTimeLeft(), 0), 1, UtilTime.TimeUnit.FIT));
				}
			}
		}

		return element;
	}
}