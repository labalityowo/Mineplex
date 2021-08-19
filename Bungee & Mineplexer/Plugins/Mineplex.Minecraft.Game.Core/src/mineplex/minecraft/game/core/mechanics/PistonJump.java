package mineplex.minecraft.game.core.mechanics;

import java.util.HashMap;
import java.util.HashSet;

import mineplex.core.MiniPlugin;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilTime;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.material.PistonBaseMaterial;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class PistonJump extends MiniPlugin
{
	private HashMap<Block, Long> _pistonExtend = new HashMap<Block, Long>();

	public PistonJump(JavaPlugin plugin) 
	{
		super("Piston Jump", plugin);
	}
	
	@EventHandler
	public void PistonLaunch(PlayerMoveEvent event)
	{	
		Block below = event.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN);

		if (below.getTypeId() != 33)
			return;

		if (below.getData() != 1)
			return;

		if (below.getRelative(BlockFace.UP).getType() != Material.AIR)
			return;

		if (_pistonExtend.containsKey(below))
			return;

		for (Player player : below.getWorld().getPlayers())
		{
			if (!below.equals(player.getLocation().getBlock().getRelative(BlockFace.DOWN)))
				continue;
			
			//Vector
			Vector vec =  new Vector(0,1.2,0);

			UtilAction.velocity(player, vec);
		}

		final Block block = below;
		_plugin.getServer().getScheduler().scheduleSyncDelayedTask(_plugin, new Runnable()
		{
			public void run()
			{
				//Extend
				BlockState state = block.getState();
				PistonBaseMaterial pbm = (PistonBaseMaterial)state.getData();
				pbm.setPowered(true);
				state.setData(pbm);
				state.update(true);

				block.getRelative(BlockFace.UP).setTypeIdAndData(34, (byte)1, false);

				_pistonExtend.put(block, System.currentTimeMillis());
				//Effect
				
				block.getWorld().playSound(block.getLocation(), Sound.PISTON_EXTEND, 1f, 1f);	
			}
		}, 10);
	}


	@EventHandler
	public void PistonExtendUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		HashSet<Block> retract = new HashSet<Block>();

		for (Block cur : _pistonExtend.keySet())
		{
			if (UtilTime.elapsed(_pistonExtend.get(cur), 600))
				retract.add(cur);
		}

		for (Block cur : retract)
		{
			_pistonExtend.remove(cur);

			//Retract
			if (cur.getTypeId() == 33)
			{
				//Extend
				BlockState state = cur.getState();
				PistonBaseMaterial pbm = (PistonBaseMaterial)state.getData();
				pbm.setPowered(false);
				state.setData(pbm);
				state.update(true);
			}

			if (cur.getRelative(BlockFace.UP).getTypeId() == 34)
				cur.getRelative(BlockFace.UP).setTypeIdAndData(0, (byte)0, true);

			//Effect
			cur.getWorld().playSound(cur.getLocation(), Sound.PISTON_RETRACT, 1f, 1f);
		}
	}
}
