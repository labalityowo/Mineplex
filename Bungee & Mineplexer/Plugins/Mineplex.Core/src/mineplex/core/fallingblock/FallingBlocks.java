package mineplex.core.fallingblock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import mineplex.core.MiniPlugin;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;

public class FallingBlocks extends MiniPlugin
{
	public static FallingBlocks Instance;
	
	private static final String METADATA = "FALLING_BLOCK_SPECIAL";
	
	public FallingBlocks(JavaPlugin plugin)
	{
		super("Falling Blocks", plugin);
		
		Instance = this;
	}

	public void Spawn(Location location, Material type, byte data, Location center)
	{
		Vector vec =  UtilAlg.getTrajectory(center, location);
		
		if (vec.getY() < 0)
		{
			vec.setY(-vec.getY());
		}
		
		Spawn(location, type, data, vec);
	}
	
	public void Spawn(Location location, Material type, byte data, Vector velocity)
	{
		FallingBlock fall = location.getWorld().spawnFallingBlock(location.add(0.5, 0.5, 0.5), type, data);
		fall.setDropItem(false);

		UtilAction.velocity(fall, velocity, 0.5 + 0.25 * Math.random(), false, 0, 0.4 + 0.20 * Math.random(), 10, false);
		
		UtilEnt.SetMetadata(fall, METADATA, "x");
	}
	
	@EventHandler
	public void BlockFall(EntityChangeBlockEvent event)
	{
		if (event.getEntity().hasMetadata(METADATA))
		{
			event.getEntity().remove();
			event.setCancelled(true);
		}
	}
}