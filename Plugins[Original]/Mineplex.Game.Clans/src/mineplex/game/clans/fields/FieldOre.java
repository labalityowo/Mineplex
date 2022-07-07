package mineplex.game.clans.fields;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.fields.commands.FieldOreCommand;
import mineplex.game.clans.fields.repository.FieldOreToken;
import mineplex.game.clans.fields.repository.FieldRepository;

public class FieldOre extends MiniPlugin
{
	public enum Perm implements Permission
	{
		FIELD_ORE_COMMAND,
	}

	private FieldRepository _repository;
	private Set<String> _active = new HashSet<String>();
	
	private List<FieldOreData> _oreInactive = new ArrayList<FieldOreData>();
	private List<FieldOreData> _oreActive = new ArrayList<FieldOreData>();

	private Map<Location, String> _oreLocations = new HashMap<Location, String>();

	private long _oreRegen = 0;
	private long _oreRegenTime = 20000;
	
	private String _serverName;
	
	public FieldOre(JavaPlugin plugin, FieldRepository repository, String serverName) 
	{
		super("Field Ore", plugin);
		
		_repository = repository;
		_serverName = serverName;
		
		load();
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.ADMIN.setPermission(Perm.FIELD_ORE_COMMAND, true, true);
	}
	
	@Override
	public void addCommands()
	{
		addCommand(new FieldOreCommand(this));
	}
	
	public void help(Player caller) 
	{
		UtilPlayer.message(caller, F.main(_moduleName, "Commands List;"));
		UtilPlayer.message(caller, F.help("/fo toggle", "Toggle Tools", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/fo list", "List Ores", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/fo fill", "Set Ores to Ore", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/fo reset", "Reset Ores to Stone", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/fo wipe", "Delete All Ore Fields (Database)", ChatColor.DARK_RED));
	}
	
	@EventHandler
	public void handleInteract(PlayerInteractEvent event)
	{
		if (!_active.contains(event.getPlayer().getName()))
			return;
		
		if (UtilGear.isMat(event.getPlayer().getItemInHand(), Material.DIAMOND))
		{
			if (UtilEvent.isAction(event, ActionType.L))
				addBlock(event.getPlayer(), event);

			else if (UtilEvent.isAction(event, ActionType.R_BLOCK))
				delBlock(event.getPlayer(), event);
		}
	}
	
	public void reset(Player player)
	{
		for (FieldOreData ore : _oreActive)
		{
			ore.SetActive(false);
			_oreInactive.add(ore);
		}

		_oreActive.clear();
		
		UtilPlayer.message(player, F.main(_moduleName, "Field Ore Reset."));
	}
	
	public void fill(Player player)
	{
		while (!_oreInactive.isEmpty())
			_oreInactive.get(UtilMath.r(_oreInactive.size())).StartVein(2 + UtilMath.r(5));

		UtilPlayer.message(player, F.main(_moduleName, "Field Ore Generated."));
	}
	
	public void list(Player player)
	{
		UtilPlayer.message(player, F.main(_moduleName, F.value("Total", ""+_oreLocations.size())));
		UtilPlayer.message(player, F.main(_moduleName, F.value("Active", ""+_oreActive.size())));
		UtilPlayer.message(player, F.main(_moduleName, F.value("Inactive", ""+_oreInactive.size())));
	}
	
	public void wipe(Player player)
	{
		reset(player);
		
		for (Map.Entry<Location, String> entry : _oreLocations.entrySet())
		{
			_repository.deleteFieldOre(entry.getValue(), UtilWorld.locToStr(entry.getKey()));
		}

		_oreInactive.clear();
		_oreLocations.clear();

		UtilPlayer.message(player, F.main(_moduleName, "Field Ore Wiped."));
	}
	
	private void addBlock(Player player, PlayerInteractEvent event) 
	{
		Block block = player.getTargetBlock(((Set<Material>) null), 0);

		if (Get(block.getLocation()) != null)
		{
			UtilPlayer.message(player, F.main(_moduleName, "This is already Field Ore."));
			return;
		}

		//Repo
		FieldOreToken token = new FieldOreToken();
		token.Server = "ALL";
		token.Location = UtilWorld.locToStr(block.getLocation());
		_repository.addFieldOre(token);

		//Memory
		_oreInactive.add(new FieldOreData(this, token.Server, block.getLocation()));

		//Inform
		block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, 57);
		UtilPlayer.message(player, F.main(_moduleName, "Field Ore Added."));

		event.setCancelled(true);
	}

	private void delBlock(Player player, PlayerInteractEvent event) 
	{
		event.setCancelled(true);

		FieldOreData ore = Get(event.getPlayer().getTargetBlock(((Set<Material>) null), 0).getLocation());

		if (ore == null)
		{
			UtilPlayer.message(player, F.main(_moduleName, "This is not Field Ore."));
			return;
		}

		_repository.deleteFieldOre(ore._server, UtilWorld.locToStr(event.getClickedBlock().getLocation()));

		ore.GetLocation().getBlock().setType(Material.STONE);

		ore.Delete();

		_oreActive.remove(ore);
		_oreInactive.remove(ore);
		_oreLocations.remove(ore._loc);

		//Inform
		event.getClickedBlock().getWorld().playEffect(event.getClickedBlock().getLocation(), Effect.STEP_SOUND, 57);
		UtilPlayer.message(player, F.main(_moduleName, "Field Ore Removed."));	
	}

	public FieldOreData Get(Location loc)
	{
		for (FieldOreData ore : _oreInactive)
			if (ore.GetLocation().equals(loc))
				return ore;

		for (FieldOreData ore : _oreActive)
			if (ore.GetLocation().equals(loc))
				return ore;

		return null;
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void Break(BlockBreakEvent event)
	{
		if (!_oreLocations.containsKey(event.getBlock().getLocation()))
			return;

		FieldOreData ore = Get(event.getBlock().getLocation());

		if (ore == null)
			return;

		event.setCancelled(true);

		ore.OreMined(event.getPlayer(), event.getPlayer().getEyeLocation());
	}

	@EventHandler
	private void Regenerate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		if (_oreInactive.isEmpty())
			return;

		if (!UtilTime.elapsed(_oreRegen, UtilField.scale(_oreRegenTime)))
			return;

		_oreRegen = System.currentTimeMillis();

		//Start!
		_oreInactive.get(UtilMath.r(_oreInactive.size())).StartVein(2 + UtilMath.r(5));
	}

	public List<FieldOreData> GetActive() 
	{
		return _oreActive;
	}

	public List<FieldOreData> GetInactive() 
	{
		return _oreInactive;
	}
	
	public Map<Location, String> getLocationMap()
	{
		return _oreLocations;
	}
	
	public void load()
	{
		clean();

		for (FieldOreToken token : _repository.getFieldOres(_serverName))
		{
			Location loc = UtilWorld.strToLoc(token.Location);
			
			loc.getBlock().setType(Material.STONE);
			_oreInactive.add(new FieldOreData(this, token.Server, loc));
			_oreLocations.put(loc, token.Server);
		}
	}

	public void clean()
	{
		reset(null);
		_oreInactive.clear();
		_oreActive.clear();
		_oreLocations.clear();
	}

	public Set<String> getActivePlayers()
	{
		return _active;
	}
}
