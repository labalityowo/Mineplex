package nautilus.game.arcade.game.games.minecraftleague.data;

import java.util.HashSet;

import nautilus.game.arcade.game.GameTeam;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class TeamBeacon
{
	private GameTeam _team;
	private Block _block;
	//private Location _core;
	
	public TeamBeacon(GameTeam team, Block block/*, Location core*/)
	{
		_team = team;
		_block = block;
		//_core = core;
		
		_block.setType(Material.BEACON);
	}
	
	/*public boolean isBlock(Block match)
	{
		if (match.getX() == _block.getX())
			if (match.getZ() == _block.getZ())
				if (match.getY() == _block.getY())
					return true;
		
		return false;
	}*/
	
	/*public Block getBlock()
	{
		return _block;
	}*/
	
	public void setBlock(Block block)
	{
		_block = block;
		block.setType(Material.BEACON);
	}
	
	public void update()
	{
		if (_block.getType() != Material.BEACON)
			return;
		
		HashSet<Material> ignore = new HashSet<Material>();
		ignore.add(Material.GLASS);
		ignore.add(Material.THIN_GLASS);
		ignore.add(Material.STAINED_GLASS);
		ignore.add(Material.STAINED_GLASS_PANE);
		//Bukkit.broadcastMessage(UtilBlock.getHighest(_block.getWorld(), _block.getX(), _block.getZ(), ignore) + "");
		//if (!isBlock(UtilBlock.getHighest(_block.getWorld(), _block.getX(), _block.getZ(), ignore).getRelative(BlockFace.DOWN)))
			//return;
		
		for (Player player : _team.GetPlayers(true))
		{
			if (player.getLocation().distance(_block.getLocation()) < 15)
			{
				player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 5 * 20, 0));
				//player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 5 * 20, 0));
				player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5 * 20, 0));
				//player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 5 * 20, 0));
			}
		}
	}
}
