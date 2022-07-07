package nautilus.game.arcade.game.games.buildmavericks;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mineplex.core.common.block.schematic.Schematic;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilItem;
import mineplex.core.recharge.Recharge;
import nautilus.game.arcade.game.games.build.BuildData;
import nautilus.game.arcade.game.games.build.GroundData;

/**
 * Represents the cylinder build area where the player can build. Holds particles and blocks.
 */
public class BuildDataCylinder extends BuildData
{
	
	private final double RADIUS = 11.5;
	private final double HEIGHT = 25;
	private final Location _blockSpawn;

	public BuildDataCylinder(Player player, Location spawn, Location center)
	{
		super(player, spawn);
		_blockSpawn = center.getBlock().getLocation().add(0.5, 0, 0.5);
	}
	
	@Override
	public boolean inBuildArea(Vector vec)
	{
		double yDiff = vec.getY()-_blockSpawn.getY();
		if(yDiff < 0 || yDiff > HEIGHT) return false;
		
		vec.setY(_blockSpawn.getY());
		
		return vec.distanceSquared(_blockSpawn.toVector()) < RADIUS*RADIUS;
	}
	
	@Override
	public void setGround(Player player, GroundData ground)
	{
		if (!Recharge.Instance.use(player, "Change Ground", 2000, true, false))
		{
			Player.playSound(Player.getLocation(), Sound.NOTE_BASS_GUITAR, 1f, 0.1f);
			return;
		}
		
		Material mat = ground.getMaterial();
		byte data = ground.getData();

		if (mat == Material.LAVA_BUCKET) mat = Material.LAVA;
		else if (mat == Material.WATER_BUCKET) mat = Material.WATER;
		
		//Set everything to air first to prevent the forming of obby.
		Set<Block> blocks = UtilBlock.getInRadius(_blockSpawn, RADIUS, 0).keySet();
		UtilBlock.startQuickRecording();
		for (Block b : blocks)
		{
			if (!UtilItem.isLiquid(b.getType())) continue;
			
			UtilBlock.setQuick(_blockSpawn.getWorld(), b.getX(), b.getY()-1, b.getZ(), 0, (byte) 0);
		}
		
		if (ground.hasSchematic())
		{
			UtilBlock.stopQuickRecording();
			
			//+2 to get the 2 block border
			ground.getSchematic().paste(_blockSpawn.clone().subtract(RADIUS+2, 1, RADIUS+2), true, false);
			
			return;
		}
		
		for (Block b : blocks)
		{
			UtilBlock.setQuick(_blockSpawn.getWorld(), b.getX(), b.getY()-1, b.getZ(), mat.getId(), data);
		}
		UtilBlock.stopQuickRecording();
	}
	
	@Override
	public Schematic convertToSchematic()
	{
		//Clear out other floor blocks before creating the schematic block
		// only keeping the build blocks and the floor set by the user
		Location min = getMin().getBlock().getLocation();
		Location max = getMax().getBlock().getLocation();
		
		int y = min.getBlockY();
		
		UtilBlock.startQuickRecording();
		for (int x = min.getBlockX(); x < max.getBlockX(); x++)
		{
			for (int z = min.getBlockZ(); z < max.getBlockZ(); z++)
			{
				if (!inBuildArea(new Vector(x + 0.5, min.getY()+1, z + 0.5)))
				{
					UtilBlock.setQuick(min.getWorld(), x, y, z, 0, (byte) 0);
				}
			}
		}
		UtilBlock.stopQuickRecording();
		return super.convertToSchematic();
	}
	
	@Override
	protected Location getMin()
	{
		return _blockSpawn.clone().subtract(RADIUS, 1, RADIUS);
	}
	
	@Override
	protected Location getMax()
	{
		return _blockSpawn.clone().add(RADIUS, HEIGHT+1, RADIUS);
	}
	
	@Override
	public double getMaxHeight()
	{
		return _blockSpawn.getY() + HEIGHT;
	}
}