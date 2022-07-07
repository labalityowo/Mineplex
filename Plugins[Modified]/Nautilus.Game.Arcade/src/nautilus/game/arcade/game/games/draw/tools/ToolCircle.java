package nautilus.game.arcade.game.games.draw.tools;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import nautilus.game.arcade.game.games.draw.BlockInfo;
import nautilus.game.arcade.game.games.draw.Draw;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class ToolCircle extends Tool
{
	public ToolCircle(Draw host)
	{
		super(host, Material.DIAMOND_SWORD);
	}

	public void customDraw(Block end)
	{
		Location mid = UtilAlg.getMidpoint(_start.getLocation().add(0.5, 0.5, 0.5), end.getLocation().add(0.5, 0.5, 0.5));
		
		double height = Math.max(_start.getY(), end.getY()) - mid.getY();
		
		boolean planeIsZ = _start.getZ() == end.getZ();
		
		double width = Math.max(_start.getX(), end.getX()) - mid.getX();
		if (!planeIsZ)
			width = Math.max(_start.getZ(), end.getZ()) - mid.getZ();
		
		for (double i=0 ; i<Math.PI*2 ; i += 0.05)
		{
			double horizontal = Math.sin(i) * width;
			double vertical = Math.cos(i) * height + 0.4;
			
			color(mid.clone().add(planeIsZ ? horizontal : 0, vertical, planeIsZ ? 0 : horizontal));
		}
	}
	
	public void color(Location loc)
	{
		Block block = loc.getBlock();
		
		if (_new.containsKey(block))
			return;
		
		if (!Host.getCanvas().contains(block))
			return;
		
		byte color = block.getData();
		Material type = block.getType();
		if (_past.containsKey(block))
		{
			type = _past.get(block).getType();
			color = _past.get(block).getData();
		}
		
		_new.put(block, new BlockInfo(type, color));
		block.setType(Host.getBrushMaterial());
		block.setData(Host.getColor());
	}
}
