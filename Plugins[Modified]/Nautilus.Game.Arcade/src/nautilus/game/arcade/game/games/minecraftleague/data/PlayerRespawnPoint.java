package nautilus.game.arcade.game.games.minecraftleague.data;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PlayerRespawnPoint
{
	private Player _owner;
	private Location _loc;
	
	public PlayerRespawnPoint(Player owner, Location loc)
	{
		_owner = owner;
		_loc = loc;
		UtilPlayer.message(owner, F.main("Game", "You have set your bed respawn point. The next time you die you will respawn here!"));
	}
	
	@SuppressWarnings("deprecation")
	private boolean isBedType(Block bedBlock, boolean foot)
	{
		boolean head = (bedBlock.getData() & 0x8) != 0;
		
		if (foot)
			return !head;
		else
			return head;
	}
	
	private Block getOtherBedBlock(Block b1)
	{
		if (b1.getType() != Material.BED_BLOCK)
			return null;
		
		boolean lookingForFoot = isBedType(b1, false);
		
		for (int x = -1; x <= 1; x++) 
		{
			for (int z = -1; z <= 1; z++) 
			{
				Block b2 = b1.getRelative(x, 0, z);
				if (!(b1.getLocation().equals(b2.getLocation()))) 
				{
					if (b2.getType().equals(Material.BED_BLOCK)) 
					{
						if (lookingForFoot && isBedType(b2, true))
							return b2;
						
						if (!lookingForFoot && isBedType(b2, false))
							return b2;
					}
				}
			}
		}
		return null;
	}
	
	public boolean respawnPlayer()
	{
		if (_loc.getBlock().getType() != Material.BED_BLOCK)
		{
			UtilPlayer.message(_owner, F.main("Game", "Your bed has been destroyed, and your respawn point has been reset!"));
			return false;
		}
		
		_owner.teleport(_loc.clone().add(0.5, 1.5, 0.5));
		UtilPlayer.message(_owner, F.main("Game", "You have been respawned at your bed location!"));
		
		return true;
	}
	
	public boolean breakBed(Block block)
	{
		boolean isOther = false;
		
		if (getOtherBedBlock(_loc.getBlock()) != null)
			if (getOtherBedBlock(_loc.getBlock()).equals(block))
				isOther = true;
		
		if (block.getLocation().equals(_loc) || isOther)
		{
			UtilPlayer.message(_owner, F.main("Game", "Your bed respawn point has been broken!"));
			return true;
		}
		return false;
	}
	
	public void overWrite(Location newLocation)
	{
		if (newLocation.distance(_loc) <= .2)
			return;
		
		if (getOtherBedBlock(_loc.getBlock()) != null)
			if (getOtherBedBlock(_loc.getBlock()).getLocation().distance(newLocation) <= .2)
				return;
			
		UtilPlayer.message(_owner, F.main("Game", "You have set your bed respawn point. The next time you die you will respawn here!"));
		if (_loc.getBlock().getType() == Material.BED_BLOCK)
			_loc.getBlock().breakNaturally();
		
		_loc = newLocation;
	}
}
