package nautilus.game.arcade.game.games.draw.tools;


import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilEvent.ActionType;
import nautilus.game.arcade.game.games.draw.BlockInfo;
import nautilus.game.arcade.game.games.draw.Draw;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

public abstract class Tool
{
	protected Draw Host;
	
	protected Player _drawer;
	protected Block _start;
	
	protected Material _material;
	
	protected HashMap<Block, BlockInfo> _past = new HashMap<Block, BlockInfo>();
	protected HashMap<Block, BlockInfo> _new = new HashMap<Block, BlockInfo>();
	
	public Tool(Draw host, Material mat)
	{
		Host = host;
		_material = mat;
	}
	
	public void start(PlayerInteractEvent event)
	{
		if (_start != null)
			return;

		if (!UtilEvent.isAction(event, ActionType.R))
			return;

		Block block = UtilPlayer.getTarget(event.getPlayer(), UtilBlock.blockPassSet, 400);

		if (block == null)
			return;

		if (!Host.getCanvas().contains(block))
			return;
		
		if (!UtilGear.isMat(event.getPlayer().getItemInHand(), _material))
			return;
		
		_drawer = event.getPlayer();
		_start = block;
		
		Host.setLock(false);
	}
	
	public void update()
	{
		if (_start == null || _drawer == null)
			return;
		
		if (!_drawer.isOnline() || !Host.isDrawer(_drawer) || !_drawer.isBlocking())
		{
			_drawer = null;
			_start = null;
			_past.clear();
			return;
		}
		
		_new = new HashMap<Block, BlockInfo>();
		
		//Calculate New
		Block end = UtilPlayer.getTarget(_drawer, UtilBlock.blockPassSet, 400);
		if (end != null && Host.getCanvas().contains(end))
		{
			customDraw(end);
		}
		
		//Remove Old
		for (Block block : _past.keySet())
		{
			if (!_new.containsKey(block))
			{
				block.setType(_past.get(block).getType());
				block.setData(_past.get(block).getData());
			}
		}
		
		_past = _new;
		_new = null;
		
		for (Player other : UtilServer.getPlayers())
			other.playSound(other.getLocation(), Sound.FIZZ, 0.2f, 2f);
	}
	
	public abstract void customDraw(Block end);
}
