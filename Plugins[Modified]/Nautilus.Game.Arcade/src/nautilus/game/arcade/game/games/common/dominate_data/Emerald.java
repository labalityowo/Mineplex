package nautilus.game.arcade.game.games.common.dominate_data;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.mission.MissionTrackerType;

import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.common.Domination;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class Emerald 
{
	private Domination Host;
	
	private Location _loc;
	
	private long _time;
	private Item _ent;
	
	public Emerald(Domination host, Location loc) 
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
		_ent = _loc.getWorld().dropItem(_loc.clone().add(0, 1, 0), new ItemStack(Material.EMERALD));
		_ent.setVelocity(new Vector(0,1,0));
		_ent.setPickupDelay(30);

		//Block
		_loc.getBlock().getRelative(BlockFace.DOWN).setType(Material.EMERALD_BLOCK);
		
		//Firework
		UtilFirework.playFirework(_loc.clone().add(0, 1, 0), FireworkEffect.builder().flicker(false).withColor(Color.GREEN).with(Type.BURST).trail(true).build());
	}
	
	public void Pickup(Player player, Item item)
	{
		if (!Host.IsLive())
			return;
		
		if (_ent == null)
			return;
		
		if (!_ent.equals(item))
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
		
		//Give Points
		Host.AddScore(team, 300);
		
		//Inform
		UtilPlayer.message(player, C.cGreen + C.Bold + "You scored 300 Points for your team!");
		
		//Firework
		UtilFirework.playFirework(_loc.clone().add(0, 1, 0), FireworkEffect.builder().flicker(false).withColor(Color.GREEN).with(Type.BALL_LARGE).trail(true).build());
		
		//Gems
		Host.AddGems(player, 3, "Emerald Powerup", true, true);
		Host.getArcadeManager().getMissionsManager().incrementProgress(player, 1, MissionTrackerType.DOM_COLLECT_GEM, Host.GetType().getDisplay(), null);
	}
}
