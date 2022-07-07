package mineplex.game.clans.fields;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

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
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.energy.Energy;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.fields.commands.FieldBlockCommand;
import mineplex.game.clans.fields.repository.FieldBlockToken;
import mineplex.game.clans.fields.repository.FieldRepository;
import mineplex.minecraft.game.core.condition.ConditionFactory;
import mineplex.minecraft.game.core.condition.ConditionManager;

public class FieldBlock extends MiniPlugin
{
	public enum Perm implements Permission
	{
		FIELD_BLOCK_COMMAND,
	}

	private ConditionFactory _conditionFactory;
	private Energy _energy;
	private FieldRepository _repository;
	private Map<String, FieldBlockData> _blocks;
	private ClansManager _clansManager;

	private Set<String> _active = new HashSet<>();
	
	//Player Info
	private Map<Player, String> _title = new WeakHashMap<>();
	private Map<Player, Integer> _stock = new WeakHashMap<>();
	private Map<Player, Double> _regen = new WeakHashMap<>();
	private Map<Player, Integer> _emptyId = new WeakHashMap<>();
	private Map<Player, Byte> _emptyData = new WeakHashMap<>();
	private Map<Player, String> _lootString = new WeakHashMap<>();

	private String _serverName;
	
	public FieldBlock(JavaPlugin plugin, ConditionManager condition, ClansManager clansManager,
						Energy energy, FieldRepository repository, String serverName) 
	{
		super("Field Block", plugin);
	
		_conditionFactory = condition.Factory();
		_energy = energy;
		_repository = repository;
		_clansManager = clansManager;
		_blocks = new HashMap<>();
		
		_serverName = serverName;
		
		load();
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.ADMIN.setPermission(Perm.FIELD_BLOCK_COMMAND, true, true);
	}
	
	@Override
	public void addCommands()
	{
		addCommand(new FieldBlockCommand(this));
	}
	
	public void showSettings(Player caller) 
	{
		populateSettings(caller);

		UtilPlayer.message(caller, F.main(getName(), "Field Addition Settings;"));
		UtilPlayer.message(caller, F.desc("Title", _title.get(caller)));
		UtilPlayer.message(caller, F.desc("Stock", _stock.get(caller)+""));
		UtilPlayer.message(caller, F.desc("Regen", _regen.get(caller)+""));
		UtilPlayer.message(caller, F.desc("Empty", _emptyId.get(caller) + ":" + _emptyData.get(caller)));
		UtilPlayer.message(caller, F.desc("Loot", _lootString.get(caller)));
	}

	private void populateSettings(Player caller) 
	{
		if (!_title.containsKey(caller))		_title.put(caller, "Default");
		if (!_stock.containsKey(caller))		_stock.put(caller, 1);
		if (!_regen.containsKey(caller))		_regen.put(caller, 3d);
		if (!_emptyId.containsKey(caller))		_emptyId.put(caller, 1);
		if (!_emptyData.containsKey(caller))	_emptyData.put(caller, (byte)0);
		if (!_lootString.containsKey(caller))	_lootString.put(caller, "263:0:2:1:50");
	}

	public void help(Player caller) 
	{
		UtilPlayer.message(caller, F.main(_moduleName, "Commands List;"));
		UtilPlayer.message(caller, F.help("/fo toggle", "Toggle Tools", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/fb loot <ID:Data:Base:Bonus:Chance,etc>", "Set Loot", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/fb empty <ID:Data>", "Set Empty Block", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/fb stock <#>", "Set Stock Max", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/fb regen <Minutes>", "Set Stock Regen", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/fb wipe", "Delete All Block Fields (Database)", ChatColor.DARK_RED));
	}

	@EventHandler
	public void handleInteract(PlayerInteractEvent event)
	{
		if (!_active.contains(event.getPlayer().getName()))
			return;
		
		if (event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getType().toString().contains("PICKAXE"))
		{
			if (UtilEvent.isAction(event, ActionType.R_BLOCK))
				showBlock(event.getPlayer(), event);
		}

		else if (UtilGear.isMat(event.getPlayer().getItemInHand(), Material.GLOWSTONE_DUST))
		{
			if (UtilEvent.isAction(event, ActionType.L_BLOCK))
				addBlock(event.getPlayer(), event);

			else if (UtilEvent.isAction(event, ActionType.R))
				glowBlocks(event.getPlayer(), event);
		}

		else if (UtilGear.isMat(event.getPlayer().getItemInHand(), Material.REDSTONE))
		{
			if (UtilEvent.isAction(event, ActionType.L_BLOCK))
				delBlock(event.getPlayer(), event);

			else if (UtilEvent.isAction(event, ActionType.R))
				glowBlocks(event.getPlayer(), event);
		}
	}

	private void glowBlocks(Player player, PlayerInteractEvent event) 
	{
		load();

		for (FieldBlockData cur : _blocks.values())
			cur.getBlock().setTypeId(89);

		event.setCancelled(true);
	}

	public void wipe(Player player)
	{
		for (FieldBlockData cur : _blocks.values())
		{
			cur.setEmpty();
			_repository.deleteFieldBlock(cur.getServer(), UtilWorld.locToStr(cur.getLocation()));
		}

		_blocks.clear();

		UtilPlayer.message(player, F.main(_moduleName, "Full Field Wipe Completed."));
	}

	private void delBlock(Player player, PlayerInteractEvent event) 
	{
		String locString = UtilWorld.locToStr(event.getClickedBlock().getLocation());
		FieldBlockData block = _blocks.get(locString);
		if (block != null)
		{
			_repository.deleteFieldBlock(block.getServer(), locString);

			//Inform
			event.getClickedBlock().getWorld().playEffect(event.getClickedBlock().getLocation(), Effect.STEP_SOUND, 42);
			UtilPlayer.message(player, F.main(_moduleName, "Field Block removed."));

			event.setCancelled(true);
		}
	}

	private void addBlock(Player player, PlayerInteractEvent event) 
	{
		populateSettings(player);
		showSettings(player);

		FieldBlockToken token = new FieldBlockToken();

		token.Server = "ALL";
		token.Location = UtilWorld.locToStr(event.getClickedBlock().getLocation());
		token.BlockId = event.getClickedBlock().getTypeId();
		token.BlockData = event.getClickedBlock().getData();
		token.EmptyId = _emptyId.get(player);
		token.EmptyData = _emptyData.get(player);
		token.StockMax = _stock.get(player);
		token.StockRegenTime = _regen.get(player);
		token.Loot = _lootString.get(player);	

		_repository.addFieldBlock(token);

		//Inform
		event.getClickedBlock().getWorld().playEffect(event.getClickedBlock().getLocation(), Effect.STEP_SOUND, 41);
		UtilPlayer.message(player, F.main(_moduleName, "Field Block added."));

		event.setCancelled(true);
	}

	private void showBlock(Player player, PlayerInteractEvent event) 
	{
		FieldBlockData fieldBlock = getFieldBlock(event.getClickedBlock());
		if (fieldBlock == null)		return;

		fieldBlock.showInfo(player);

		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void BlockBreak(BlockBreakEvent event)
	{			
		if (_clansManager.isFields(event.getBlock().getLocation()))
		{
			event.setCancelled(true);	// Cancel all block breaks in fields. Handle custom breaking for FieldBlocks and Ores.
			
			FieldBlockData fieldBlock = getFieldBlock(event.getBlock());
			if (fieldBlock != null)
			{
				fieldBlock.handleMined(event.getPlayer());
			}
		}
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			for (FieldBlockData cur : _blocks.values())
				cur.regen();

		if (event.getType() == UpdateType.FAST)
			for (FieldBlockData cur : _blocks.values())
				cur.check();
	}

	public void load()
	{
		clean();

		for (FieldBlockToken token : _repository.getFieldBlocks(_serverName))
		{
			Location loc = UtilWorld.strToLoc(token.Location);
			_blocks.put(token.Location, new FieldBlockData(this, token.Server, loc, token.BlockId, token.BlockData, token.EmptyId, token.EmptyData, token.StockMax, token.StockRegenTime, token.Loot));
		}
	}

	public void clean()
	{
		for (FieldBlockData cur : _blocks.values())
			cur.clean();

		_blocks.clear();
	}

	public FieldBlockData getFieldBlock(Block block) 
	{
		if (_blocks.containsKey(UtilWorld.locToStr(block.getLocation())))
			return _blocks.get(UtilWorld.locToStr(block.getLocation()));

		return null;
	}

	public Energy getEnergy()
	{
		return _energy;
	}

	public Set<String> getActive()
	{
		return _active;
	}

	public Map<Player, String> getLootString()
	{
		return _lootString;
	}

	public Map<Player, Integer> getEmptyId()
	{
		return _emptyId;
	}

	public Map<Player, Byte> getEmptyData()
	{
		return _emptyData;
	}

	public Map<Player, Double> getRegen()
	{
		return _regen;
	}

	public Map<Player, Integer> getStock()
	{
		return _stock;
	}

	public Map<Player, String> getTitle()
	{
		return _title;
	}

	public ConditionFactory getCondition()
	{
		return _conditionFactory;
	}	
}