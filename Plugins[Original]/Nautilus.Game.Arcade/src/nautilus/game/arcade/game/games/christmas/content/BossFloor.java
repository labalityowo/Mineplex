package nautilus.game.arcade.game.games.christmas.content;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.itemstack.ItemStackFactory;
import nautilus.game.arcade.game.games.christmas.ChristmasAudio;
import nautilus.game.arcade.game.games.christmas.parts.Part5;

public class BossFloor 
{
	private Part5 Host;

	private boolean _active = false;
	private int _difficulty = 0;
	
	private HashMap<Location, Byte> _floor;
	
	private int _state = 0;
	private byte _color = 0;
	private long _last = 0;
	private Location _restoreLoc = null;
	
	public BossFloor(Part5 host, ArrayList<Location> floor)
	{
		Host = host;
		
		_floor = new HashMap<Location, Byte>();

		for (Location loc : floor)
		{
			loc.getBlock().setType(Material.AIR);
			
			loc.add(0, 1, 0);
			
			_floor.put(loc, loc.getBlock().getData());
		}
	}
	
	public void SetActive(boolean active, int difficulty)
	{
		_active = active;
		_difficulty = difficulty;
	}
	
	public void Ignite(byte ignore)
	{
		for (Location loc : _floor.keySet())
		{
			if (_floor.get(loc) == ignore)
				continue;
			
			if (loc.clone().add(0, 1, 0).getBlock().getType() != Material.FIRE)
				MapUtil.QuickChangeBlockAt(loc.clone().add(0, 1, 0), Material.FIRE);
		}
	}
	
	public void Restore()
	{
		for (Location loc : _floor.keySet())
		{
			if (loc.clone().add(0, 1, 0).getBlock().getType() == Material.FIRE)
				MapUtil.QuickChangeBlockAt(loc.clone().add(0, 1, 0), Material.AIR);
		}
	}

	public void Update() 
	{
		//Normal
		if (_active && _state == 0 && UtilTime.elapsed(_last, 6000 - (1000 * _difficulty)))
		{
			for (Player player : UtilServer.getPlayers())
				player.setExp(0f);
			
			_state = 1;
			
			_last = System.currentTimeMillis();
			
			_color = Host.GetBoss().GetEntity().getLocation().getBlock().getRelative(BlockFace.DOWN).getData();
			
			String color = "White";
			ChatColor textColor = ChatColor.WHITE;
			ChristmasAudio audio = ChristmasAudio.STAY_ON_WHITE;
			
			if (_color == 1)		{color = "Orange";	textColor = ChatColor.GOLD;}
			else if (_color == 2)	{color = "Purple";	textColor = ChatColor.LIGHT_PURPLE;}
			else if (_color == 3)	{color = "Blue";	textColor = ChatColor.BLUE;}
			else if (_color == 4)	{color = "Yellow";	textColor = ChatColor.YELLOW;			audio = ChristmasAudio.STAY_ON_YELLOW;}
			else if (_color == 5)	{color = "Green";	textColor = ChatColor.GREEN;}
			else if (_color == 6)	{color = "Pink";	textColor = ChatColor.RED;				audio = ChristmasAudio.STAY_ON_RED;}
			else if (_color == 7)	{color = "Gray";	textColor = ChatColor.GRAY;}
			else if (_color == 8)	{color = "Gray";	textColor = ChatColor.GRAY;}
			else if (_color == 9)	{color = "Blue";	textColor = ChatColor.BLUE;}
			else if (_color == 10)	{color = "Purple";	textColor = ChatColor.LIGHT_PURPLE;}
			else if (_color == 11)	{color = "Blue";	textColor = ChatColor.BLUE;}
			else if (_color == 12)	{color = "Brown";	textColor = ChatColor.DARK_GRAY;}
			else if (_color == 13)	{color = "Green";	textColor = ChatColor.GREEN;			audio = ChristmasAudio.STAY_ON_GREEN;}
			else if (_color == 14)	{color = "Red";		textColor = ChatColor.RED;				audio = ChristmasAudio.STAY_ON_RED;}
			else if (_color == 15)	{color = "Black";	textColor = ChatColor.BLACK;}
			
			Host.Host.SantaSay("Stay on " + textColor + C.Bold + color.toUpperCase(), audio);
			
			_restoreLoc = Host.GetBoss().GetEntity().getLocation().getBlock().getLocation().add(0.5, 0, 0.5);
			
			//Give Wool
			for (Player player : Host.Host.GetPlayers(true))
				for (int i=0 ; i<9 ; i++)
					if (player.getInventory().getItem(i) == null || player.getInventory().getItem(i).getType() == Material.AIR)
						player.getInventory().setItem(i, ItemStackFactory.Instance.CreateStack(Material.WOOL, _color));
		}
		//Countdown
		else if (_state == 1)
		{
			long req = 4000 - (2000 * _difficulty);
			
			for (Player player : UtilServer.getPlayers())
				player.setExp(Math.min(0.99f, Math.max(0.0f, (req - (System.currentTimeMillis() - _last)) / req)));
			
			if (UtilTime.elapsed(_last, req))
			{
				_last = System.currentTimeMillis();
				
				_state = 2;
				
				Ignite(_color);
			}
		}
		//Disappear
		else if (_state == 2)
		{
			for (Player player : UtilServer.getPlayers())
				player.setExp(0.99f);
			
			if (UtilTime.elapsed(_last, 3000 - (1000 * _difficulty)))
			{
				_last = System.currentTimeMillis();
				
				_state = 0;
				
				Restore();
				
				Host.GetBoss().GetEntity().teleport(_restoreLoc);
				
				for (Player player : UtilServer.getPlayers())
					player.getInventory().remove(Material.WOOL);
			}
			else
			{
				Ignite(_color);
			}
		}
	}

	public boolean ShouldBossMove() 
	{
		return _state == 0;
	}
}
