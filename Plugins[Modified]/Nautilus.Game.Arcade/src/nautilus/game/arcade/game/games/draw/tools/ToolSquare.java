package nautilus.game.arcade.game.games.draw.tools;

import nautilus.game.arcade.game.games.draw.BlockInfo;
import nautilus.game.arcade.game.games.draw.Draw;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class ToolSquare extends Tool
{
	public ToolSquare(Draw host)
	{
		super(host, Material.GOLD_SWORD);
	}

	public void customDraw(Block end)
	{
		Location upper = new Location(end.getWorld(), Math.max(_start.getX(), end.getX()), Math.max(_start.getY(), end.getY()), Math.max(_start.getZ(), end.getZ()));
		upper.add(0.5, 0.5, 0.5);
		
		Location lower = new Location(end.getWorld(), Math.min(_start.getX(), end.getX()), Math.min(_start.getY(), end.getY()), Math.min(_start.getZ(), end.getZ()));
		lower.add(0.5, 0.5, 0.5);
		
		Location cur = upper.clone();
		
		//Decrease
		while (cur.getX() > lower.getX())
		{	
			cur.setX(cur.getX() - 1);
			color(cur);
		}
		
		while (cur.getY() > lower.getY())
		{	
			cur.setY(cur.getY() - 1);
			color(cur);
		}
		
		while (cur.getZ() > lower.getZ())
		{	
			cur.setZ(cur.getZ() - 1);
			color(cur);
		}
		
		//Increase
		while (cur.getX() < upper.getX())
		{	
			cur.setX(cur.getX() + 1);
			color(cur);
		}
		
		while (cur.getY() < upper.getY())
		{	
			cur.setY(cur.getY() + 1);
			color(cur);
		}
		
		while (cur.getZ() < upper.getZ())
		{	
			cur.setZ(cur.getZ() + 1);
			color(cur);
		}
	}
	
	public void color(Location loc)
	{
		Block block = loc.getBlock();
		
		if (_new.containsKey(block))
			return;
		
		if (!Host.getCanvas().contains(block))
			return;

		Material type = block.getType();
		byte color = block.getData();
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
