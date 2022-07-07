package mineplex.hub.modules.mavericks;

import mineplex.core.gadget.types.GadgetType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import mineplex.core.MiniPlugin;
import mineplex.core.common.util.F;
import mineplex.core.cosmetic.CosmeticManager;
import mineplex.core.gadget.event.GadgetEnableEvent;
import mineplex.core.npc.event.NpcDamageByEntityEvent;
import mineplex.core.npc.event.NpcInteractEntityEvent;
import mineplex.core.recharge.Recharge;
import mineplex.hub.HubManager;

/**
 * A small teleportation manager to manage the portal from the hub to the mavericks world and back.
 */
public class MavericksPortalManager extends MiniPlugin
{
	private Box _portalMavericksHub;
	
	private Location _destHub;
	private Location _destMavericks;
	
	private CosmeticManager _cosmeticManager;

	public MavericksPortalManager(JavaPlugin plugin, HubManager hubManager, MavericksWorldManager worldManager, CosmeticManager cosmeticManager)
	{
		super("Mavericks Teleporter", plugin);
		
		_cosmeticManager = cosmeticManager;
		
		_destMavericks = worldManager.getSpawn();
		
		_portalMavericksHub = new Box(worldManager.getWorld().getName(), new Vector(3, 20, 316), new Vector(-1, 25, 317));
		_destHub = hubManager.GetSpawn();
	}
	
	@EventHandler
	public void onEnable(GadgetEnableEvent event)
	{
		if (event.getPlayer().getWorld().equals(_destMavericks.getWorld()) && event.getGadget().getGadgetType() != GadgetType.COSTUME)
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onUseNPC(NpcInteractEntityEvent event)
	{
		if (ChatColor.stripColor(event.getNpc().getName()).contains("Mavericks Lobby"))
		{
			useMavsNpc(event.getPlayer());
		}
	}
	
	@EventHandler
	public void onUseNPC(NpcDamageByEntityEvent event)
	{
		if (!(event.getDamager() instanceof Player))
		{
			return;
		}
		Player player = (Player) event.getDamager();
		
		if (ChatColor.stripColor(event.getNpc().getName()).contains("Mavericks Lobby")  && Recharge.Instance.use(player, "Go to Mavs Lobby", 1000, false, false))
		{
			useMavsNpc(player);
		}
	}
	
	@EventHandler
	public void onEnter(EntityPortalEnterEvent event)
	{
		if (!(event.getEntity() instanceof Player))
		{
			return;
		}
		
		Player p = (Player) event.getEntity();
		Box box = isInside(p);
		
		if (box == null)
		{
			return;
		}
		
		_cosmeticManager.getPetManager().disableAll(p);
		_cosmeticManager.getGadgetManager().disableAll(p);
		
		if (box == _portalMavericksHub)
		{
			p.teleport(_destHub);
			p.sendMessage(F.main("Teleporter", "Teleported to " + F.item("Hub") + " area."));
		}
		
		p.playSound(p.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
	}
	
	private void useMavsNpc(Player player)
	{
		_cosmeticManager.getPetManager().disableAll(player);
		_cosmeticManager.getGadgetManager().disableAll(player);
		player.teleport(_destMavericks);
		player.sendMessage(F.main("Teleporter", "Teleported to " + F.item("Mavericks") + " area."));
		player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
	}
	
	private Box isInside(Player player)
	{
		if (_portalMavericksHub.isInside(player.getLocation()))
		{
			return _portalMavericksHub;
		}
		return null;
	}
	
	/**
	 * A small AABB box util class.
	 */
	private static class Box
	{
		private Vector _min;
		private Vector _max;
		
		private String _world;
		
		public Box(String world, Vector a, Vector b)
		{
			_world = world;
			_min = Vector.getMinimum(a, b);
			_max = Vector.getMaximum(a, b);
		}
		
		public boolean isInside(Vector v)
		{
			return v.isInAABB(_min, _max);
		}
		
		public boolean isInside(Location loc)
		{
			return loc.getWorld().getName().equals(_world) && isInside(loc.toVector());
		}
	}
}