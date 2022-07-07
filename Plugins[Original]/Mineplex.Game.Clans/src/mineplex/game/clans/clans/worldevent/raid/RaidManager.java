package mineplex.game.clans.clans.worldevent.raid;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import mineplex.core.MiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.projectile.ProjectileManager;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.utils.UtilScheduler;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.worldevent.api.EventState;
import mineplex.game.clans.clans.worldevent.raid.command.StartRaidCommand;
import mineplex.minecraft.game.core.condition.ConditionManager;
import mineplex.minecraft.game.core.damage.DamageManager;

public class RaidManager extends MiniPlugin
{
	public enum Perm implements Permission
	{
		START_RAID_COMMAND,
	}

	private static final int MAX_PARTICIPANTS = 10;
	
	private Set<RaidWorldEvent> _raids = new HashSet<>();
	private List<RaidAltar> _altars = new ArrayList<>();
	
	public RaidManager(JavaPlugin plugin)
	{
		super("Raid Manager", plugin);
		
		addCommand(new StartRaidCommand(this));
		
		{
			List<ItemStack> items = new ArrayList<>();
			items.add(new ItemStack(Material.BONE, 20));
			items.add(new ItemBuilder(Material.IRON_INGOT).setAmount(2).setTitle(C.cDRedB + "Old Silver Token").setLore(C.cRed + "This token pulses with an evil aura.").setGlow(true).build());
			_altars.add(new RaidAltar(new Location(Bukkit.getWorld("world"), 361, 57, -990).getBlock(), RaidType.CHARLES_WITHERTON, items));
		}
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.ADMIN.setPermission(Perm.START_RAID_COMMAND, true, true);
	}
	
	@Override
	public void disable()
	{
		_raids.forEach(raid -> raid.stop(true));
		_raids.clear();
	}
	
	private boolean isIneligible(Player player)
	{
		if (ClansManager.getInstance().hasTimer(player))
		{
			if (Recharge.Instance.use(player, "PvP Timer Inform NoRaid", 5000, false, false))
			{
				UtilPlayer.message(player, F.main(getName(), "You cannot enter a Raid whilst protected from PvP. Run " + F.elem("/pvp") + " to enable PvP!"));
			}
			return true;
		}
		
		return false;
	}
	
	public DisguiseManager getDisguiseManager()
	{
		return ClansManager.getInstance().getDisguiseManager();
	}
	
	public ProjectileManager getProjectileManager()
	{
		return ClansManager.getInstance().getProjectile();
	}
	
	public DamageManager getDamageManager()
	{
		return ClansManager.getInstance().getDamageManager();
	}
	
	public BlockRestore getBlockRestore()
	{
		return ClansManager.getInstance().getBlockRestore();
	}
	
	public ConditionManager getConditionManager()
	{
		return ClansManager.getInstance().getCondition();
	}
	
	public int getActiveRaids()
	{
		return _raids.size();
	}
	
	public boolean isInRaid(Location loc)
	{
		for (RaidWorldEvent event : _raids)
		{
			if (event.WorldData.World.equals(loc.getWorld()))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public boolean startRaid(Player player, RaidType type)
	{
		if (_raids.size() >= 5)
		{
			UtilPlayer.message(player, F.main(type.getRaidName() + " Raid", "There are currently too many ongoing raids to start a new one!"));
			return false;
		}
		Set<Player> inside = UtilPlayer.getInRadius(player.getLocation(), 4).keySet();
		inside.removeIf(this::isIneligible);
		if (inside.size() > MAX_PARTICIPANTS)
		{
			UtilPlayer.message(player, F.main(type.getRaidName() + " Raid", "You cannot start a raid with more than " + MAX_PARTICIPANTS + " participants!"));
			return false;
		}
		inside.forEach(in -> UtilPlayer.message(in, F.main(type.getRaidName() + " Raid", "Summoning ancient power...")));
		createNewRaid(type, raid -> inside.forEach(in ->
		{
			raid.addPlayer(in);
			in.getWorld().strikeLightningEffect(in.getLocation());
		}));
		return true;
	}
	
	public void createNewRaid(RaidType type, Consumer<RaidWorldEvent> raidConsumer)
	{
		final WorldData data = new WorldData(type.getRaidName());
		BukkitTask task = UtilScheduler.runEvery(UpdateType.TICK, () ->
		{
			if (data.Loaded)
			{
				try
				{
					RaidWorldEvent event = type.getClazz().getConstructor(WorldData.class, RaidManager.class).newInstance(data, this);
					raidConsumer.accept(event);
					event.start();
					_raids.add(event);
				}
				catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
				{
					UtilServer.runSyncLater(data::uninitialize, 120);
					e.printStackTrace();
				}
				while (data.LoadChecker == null) {}
				data.LoadChecker.cancel();
			}
		});
		data.LoadChecker = task;
	}
	
	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() == UpdateType.TICK)
		{
			UtilServer.getPlayersCollection()
				.stream()
				.filter(player -> isInRaid(player.getLocation()))
				.forEach(player -> ClansManager.getInstance().getItemMapManager().removeMap(player));
			return;
		}
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}
		
		_raids.removeIf(e -> e.getState() == EventState.STOPPED);
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event)
	{
		if (!event.hasBlock())
		{
			return;
		}
		
		for (RaidAltar altar : _altars)
		{
			if (altar.handleInteract(event.getPlayer(), event.getClickedBlock()))
			{
				event.setCancelled(true);
				return;
			}
		}
	}
}