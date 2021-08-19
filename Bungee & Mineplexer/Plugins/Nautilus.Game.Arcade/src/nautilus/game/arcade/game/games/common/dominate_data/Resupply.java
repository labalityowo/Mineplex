package nautilus.game.arcade.game.games.common.dominate_data;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.GameTeam;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class Resupply 
{
	private Game Host;
	
	private Location _loc;
	
	private long _time;
	private Item _ent;
	
	public Resupply(Game host, Location loc) 
	{
		Host = host;
		
		_time = System.currentTimeMillis();
		
		_loc = loc;
		
		_loc.getBlock().getRelative(BlockFace.DOWN).setType(Material.IRON_BLOCK);
	}

	public void Update()
	{
		if (_ent != null)
		{
			if (!_ent.isValid())
			{
				_ent.remove();
				_ent = null;
			}
			
			return;
		}
		
		if (!UtilTime.elapsed(_time, 60000))
			return;
		
		//Spawn
		_ent = _loc.getWorld().dropItem(_loc.clone().add(0, 1, 0), new ItemStack(Material.CHEST));
		_ent.setVelocity(new Vector(0,1,0));
		
		//Block
		_loc.getBlock().getRelative(BlockFace.DOWN).setType(Material.GOLD_BLOCK);
		
		//Firework
		UtilFirework.playFirework(_loc.clone().add(0, 1, 0), FireworkEffect.builder().flicker(false).withColor(Color.YELLOW).with(Type.BURST).trail(true).build());
	}
	
	public void Pickup(Player player, Item item)
	{
		if (!Host.IsLive())
			return;
		
		if (_ent == null)
			return;
		
		if (!_ent.equals(item))
			return;
		
		if (_ent.getTicksLived() <= 40)
			return;
		
		if (!Host.IsAlive(player))
			return;
		
		if (Host.Manager.isSpectator(player))
			return;
		
		GameTeam team = Host.GetTeam(player);
		if (team == null)	return;
		
		//Remove
		_ent.remove();
		_ent = null;
		_time = System.currentTimeMillis();
		_loc.getBlock().getRelative(BlockFace.DOWN).setType(Material.IRON_BLOCK);
		
		Host.Manager.getClassManager().Get(player).ResetItems();
		
		//Inform
		UtilPlayer.message(player, C.cYellow + C.Bold + "Your inventory was restocked!");
		
		//Firework
		UtilFirework.playFirework(_loc.clone().add(0, 1, 0), FireworkEffect.builder().flicker(false).withColor(Color.YELLOW).with(Type.BALL_LARGE).trail(true).build());
	}
}
