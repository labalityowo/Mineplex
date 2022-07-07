package mineplex.game.clans.clans.pvptimer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.recharge.Recharge;
import mineplex.core.slack.SlackAPI;
import mineplex.core.slack.SlackMessage;
import mineplex.core.slack.SlackTeam;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.ClansUtility.ClanRelation;
import mineplex.game.clans.core.repository.ClanTerritory;
import mineplex.minecraft.game.classcombat.Skill.event.SkillTriggerEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

@ReflectivelyCreateMiniPlugin
public class PvPTimerManager extends MiniPlugin
{
	public enum Perm implements Permission
	{
		PVP_ENABLE_SELF,
		PVP_ENABLE_OTHER,
	}
	
	private final File _directory;
	private final Map<Player, Long> _timers = new WeakHashMap<>();
	
	public PvPTimerManager()
	{
		super("PvP Timer");
		
		_directory = new File("pvptimers");
		if (!_directory.exists())
		{
			_directory.mkdir();
		}
		if (!_directory.isDirectory())
		{
			FileUtils.deleteQuietly(_directory);
			_directory.mkdir();
		}
		
		addCommand(new CommandBase<PvPTimerManager>(this, Perm.PVP_ENABLE_SELF, "pvp")
		{
			public void Execute(Player caller, String[] args)
			{
				if (!hasTimer(caller))
				{
					UtilPlayer.message(caller, F.main(getName(), "You do not have a PvP Timer!"));
					return;
				}
				
				disableTimer(caller, true, true);
			}
		});
		addCommand(new CommandBase<PvPTimerManager>(this, Perm.PVP_ENABLE_OTHER, "enablepvp")
		{
			public void Execute(Player caller, String[] args)
			{
				if (args.length < 1)
				{
					UtilPlayer.message(caller, F.main(getName(), "Usage: /enablepvp <Player>"));
					return;
				}
				Player target = Bukkit.getPlayer(args[0]);
				if (target == null)
				{
					UtilPlayer.message(caller, F.main(getName(), F.elem(args[0]) + " was not found!"));
					return;
				}
				
				if (hasTimer(target))
				{
					disableTimer(target, true, true);
					UtilPlayer.message(caller, F.main(getName(), F.elem(target.getName()) + " no longer has a PvP Timer!"));
					if (!UtilServer.isTestServer())
					{
						SlackAPI.getInstance().sendMessage(SlackTeam.DEVELOPER, "#clans-commandspy",
								new SlackMessage("Clans Command Logger", "crossed_swords", caller.getName() + " has removed " + target.getName() + "'s PvP Timer on " + UtilServer.getServerName() + "."),
								true);
					}
				}
				else
				{
					UtilPlayer.message(caller, F.main(getName(), F.elem(target.getName()) + " does not have a PvP Timer!"));
				}
			}
		});
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.PLAYER.setPermission(Perm.PVP_ENABLE_SELF, true, true);
		PermissionGroup.CMOD.setPermission(Perm.PVP_ENABLE_OTHER, false, true);
		PermissionGroup.ADMIN.setPermission(Perm.PVP_ENABLE_OTHER, true, true);
	}
	
	private String getPvPTimerFile(String fileName) throws IOException
	{
		return _directory.getCanonicalFile() + File.separator + fileName;
	}
	
	private boolean addPvPTimer(Player player)
	{
		try
		{
			File file = new File(getPvPTimerFile(player.getUniqueId().toString() + ".pvp"));
			boolean add = !FileUtils.directoryContains(_directory, file);
			
			if (add)
			{
				long start = System.currentTimeMillis();
				FileUtils.write(file, String.valueOf(start), Charset.defaultCharset(), false);
				_timers.put(player, start);
			}
			else
			{
				String read = FileUtils.readFileToString(file, Charset.defaultCharset());
				long parsed = Long.parseLong(read);
				if (parsed != -1 && !UtilTime.elapsed(parsed, 60 * 60 * 1000))
				{
					_timers.put(player, parsed);
				}
			}
			
			return add;
		}
		catch (IOException | NumberFormatException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	private void disableTimer(Player player, boolean removeFromMap, boolean inform)
	{
		try
		{
			File file = new File(getPvPTimerFile(player.getUniqueId().toString() + ".pvp"));
			if (file.exists())
			{
				FileUtils.write(file, String.valueOf(-1L), Charset.defaultCharset(), false);
				if (removeFromMap)
				{
					_timers.remove(player);
				}
				if (inform)
				{
					UtilPlayer.message(player, F.main(getName(), "Your PvP Protection has been removed!"));
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean hasTimer(Player player)
	{
		if (_timers.containsKey(player))
		{
			if (UtilTime.elapsed(_timers.get(player), 60 * 60 * 1000))
			{
				_timers.remove(player);
			}
			else
			{
				return true;
			}
		}
		
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean handleMining(Player player, Block block, boolean playSound, ItemStack overrideDrop, boolean setToAir)
	{
		if (!hasTimer(player))
		{
			return false;
		}
		block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getTypeId());
		Function<Collection<ItemStack>, ItemStack[]> converter = (collection) ->
		{
			ItemStack[] array = new ItemStack[collection.size()];
			int i = 0;
			for (ItemStack item : collection)
			{
				array[i++] = item;
			}
			return array;
		};
		BiConsumer<Integer, ItemStack> dropper = (integer, item) ->
		{
			block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.3, 0.5), item);
		};
		Collection<ItemStack> drops = overrideDrop != null ? Arrays.asList(overrideDrop) : block.getDrops(player.getItemInHand());
		player.getInventory().addItem(converter.apply(drops)).forEach(dropper);
		if (setToAir)
		{
			block.setType(Material.AIR);
		}
		
		return true;
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		if (addPvPTimer(event.getPlayer()))
		{
			runSyncLater(() -> UtilPlayer.message(event.getPlayer(), F.main(getName(), "You now have 1 hour of PvP Protection!")), 40L);
		}
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}
		
		Iterator<Entry<Player, Long>> timers = _timers.entrySet().iterator();
		while (timers.hasNext())
		{
			Entry<Player, Long> timer = timers.next();
			if (UtilTime.elapsed(timer.getValue(), 60 * 60 * 1000))
			{
				UtilPlayer.message(timer.getKey(), F.main(getName(), "Your PvP Protection has expired!"));
				disableTimer(timer.getKey(), false, false);
				timers.remove();
			}
		}
	}
	
	@EventHandler
	public void onSkill(SkillTriggerEvent event)
	{
		if (hasTimer(event.GetPlayer()))
		{
			UtilPlayer.message(event.GetPlayer(), F.main("Clans", "You cannot use skills whilst protected from PvP. Run " + F.elem("/pvp") + " to enable PvP!"));
			event.SetCancelled(true);
		}
	}
	
	@EventHandler
	public void onShoot(EntityShootBowEvent event)
	{
		if (event.getEntity() instanceof Player)
		{
			Player player = (Player) event.getEntity();
			
			if (hasTimer(player))
			{
				UtilPlayer.message(player, F.main("Clans", "You cannot shoot whilst protected from PvP. Run " + F.elem("/pvp") + " to enable PvP!"));
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onDamage(CustomDamageEvent event)
	{
		if (event.GetDamageePlayer() != null)
		{
			if (hasTimer(event.GetDamageePlayer()) && event.GetDamagerPlayer(true) != null)
			{
				UtilPlayer.message(event.GetDamagerPlayer(true), F.main(getName(), "You cannot harm " + F.elem(event.GetDamageePlayer().getName()) + "!"));
				event.SetCancelled("PvP Timer");
			}
			if (event.GetDamagerPlayer(true) != null)
			{
				if (hasTimer(event.GetDamagerPlayer(true)))
				{
					UtilPlayer.message(event.GetDamagerPlayer(true), F.main(getName(), "You cannot harm " + F.elem(event.GetDamageePlayer().getName()) + " whilst protected from PvP. Run " + F.elem("/pvp") + " to enable PvP!"));
					event.SetCancelled("PvP Timer");
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPickup(PlayerPickupItemEvent event)
	{
		if (hasTimer(event.getPlayer()))
		{
			event.setCancelled(true);
			if (Recharge.Instance.use(event.getPlayer(), "PvP Timer Inform NoPickup", 5000, false, false))
			{
				UtilPlayer.message(event.getPlayer(), F.main(getName(), "You cannot pick up items whilst protected from PvP. Run " + F.elem("/pvp") + " to enable PvP!"));
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event)
	{
		if (handleMining(event.getPlayer(), event.getBlock(), true, null, true))
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEnter(PlayerMoveEvent event)
	{
		if (!hasTimer(event.getPlayer()) || UtilWorld.areChunksEqual(event.getFrom(), event.getTo()) || ClansManager.getInstance().getClanUtility().isAdmin(event.getTo()))
		{
			return;
		}
		ClanTerritory claimTo = ClansManager.getInstance().getClanUtility().getClaim(event.getTo());
		ClanTerritory claimFrom = ClansManager.getInstance().getClanUtility().getClaim(event.getFrom());
		
		if (claimTo != null  && ClansManager.getInstance().getClanUtility().getAccess(event.getPlayer(), event.getTo()) != ClanRelation.SELF)
		{
			if (claimFrom == null || !claimFrom.Owner.equals(claimTo.Owner))
			{
				UtilPlayer.message(event.getPlayer(), F.main(getName(), "You cannot enter claimed land whilst protected from PvP. Run " + F.elem("/pvp") + " to enable PvP!"));
				Vector trajectory = UtilAlg.getTrajectory(event.getTo(), event.getFrom()).multiply(4);
				event.getPlayer().teleport(event.getFrom().clone().add(trajectory));
				UtilAction.velocity(event.getPlayer(), trajectory, 1.5, true, 0.8, 0, 1.0, true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onTeleportInto(PlayerTeleportEvent event)
	{
		if (!hasTimer(event.getPlayer()) || UtilWorld.areChunksEqual(event.getFrom(), event.getTo()) || ClansManager.getInstance().getClanUtility().isAdmin(event.getTo()))
		{
			return;
		}
		ClanTerritory claimTo = ClansManager.getInstance().getClanUtility().getClaim(event.getTo());
		ClanTerritory claimFrom = ClansManager.getInstance().getClanUtility().getClaim(event.getFrom());
		
		if (claimTo != null && ClansManager.getInstance().getClanUtility().getAccess(event.getPlayer(), event.getTo()) != ClanRelation.SELF)
		{
			if (claimFrom == null || !claimFrom.Owner.equals(claimTo.Owner))
			{
				UtilPlayer.message(event.getPlayer(), F.main(getName(), "You cannot enter claimed land whilst protected from PvP. Run " + F.elem("/pvp") + " to enable PvP!"));
				event.setCancelled(true);
			}
		}
	}
}