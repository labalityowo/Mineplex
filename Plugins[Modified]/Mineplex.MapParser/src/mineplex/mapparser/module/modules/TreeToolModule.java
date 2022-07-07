package mineplex.mapparser.module.modules;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilGear;
import mineplex.mapparser.BlockData;
import mineplex.mapparser.MapParser;
import mineplex.mapparser.module.Module;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 *
 */
public class TreeToolModule extends Module
{

	private Map<UUID, List<Set<BlockData>>> _treeHistory = Maps.newHashMap();

	public TreeToolModule(MapParser plugin)
	{
		super("TreeTool", plugin);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void treeRemover(PlayerInteractEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		//Permission
		if (!getPlugin().getData(event.getPlayer().getWorld().getName()).HasAccess(event.getPlayer()))
		{
			return;
		}

		Player player = event.getPlayer();

		if (!UtilGear.isMat(player.getItemInHand(), Material.NETHER_STAR))
		{
			return;
		}

		event.setCancelled(true);

		//Remove
		if (event.getAction() == Action.LEFT_CLICK_BLOCK)
		{
			if (event.getClickedBlock().getType() != Material.LOG)
			{
				player.sendMessage(C.cRed + C.Bold + "TreeTool: " + ChatColor.RESET + "Left-Click on Log");
				return;
			}

			Set<Block> toRemove = getPlugin().searchLog(Sets.newHashSet(), event.getClickedBlock());

			if (toRemove.isEmpty())
			{
				player.sendMessage(C.cRed + C.Bold + "TreeTool: " + ChatColor.RESET + "Left-Click on Log");
				return;
			}

			Set<BlockData> history = Sets.newHashSet();

			for (Block block : toRemove)
			{
				history.add(new BlockData(block));

				block.setType(Material.AIR);
			}

			if (!_treeHistory.containsKey(player.getUniqueId()))
			{
				_treeHistory.put(player.getUniqueId(), Lists.newArrayList());
			}

			_treeHistory.get(player.getUniqueId()).add(0, history);

			player.sendMessage(C.cRed + C.Bold + "TreeTool: " + ChatColor.RESET + "Tree Removed");

			while (_treeHistory.get(player.getUniqueId()).size() > 10)
			{
				_treeHistory.get(player.getUniqueId()).remove(10);
			}

		} else if (UtilEvent.isAction(event, ActionType.R))
		{
			if (!_treeHistory.containsKey(player.getUniqueId()) || _treeHistory.get(player.getUniqueId()).isEmpty())
			{
				player.sendMessage(C.cGreen + C.Bold + "TreeTool: " + ChatColor.RESET + "No Tree History");
				return;
			}

			Set<BlockData> datas = _treeHistory.get(player.getUniqueId()).remove(0);

			datas.forEach(BlockData::restore);

			player.sendMessage(C.cGreen + C.Bold + "TreeTool: " + ChatColor.RESET + "Tree Restored");
		}
	}

}
