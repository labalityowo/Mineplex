package mineplex.game.clans.fields;

import java.util.HashSet;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.Managers;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.game.clans.clans.pvptimer.PvPTimerManager;

public class FieldOreData 
{
	protected boolean _active = false;

	protected Location _loc = null;
	protected String _server;
	protected HashSet<FieldOreData> _neighbours = new HashSet<FieldOreData>();
	protected double _neighbourDist = 4;

	protected FieldOre Field;

	public FieldOreData(FieldOre field, String server, Location loc)
	{
		Field = field;
		_loc = loc;
		_server = server;
		
		Field.getLocationMap().put(_loc, server);

		for (FieldOreData other : Field.GetInactive())
		{
			if (other.equals(this))
				continue;

			if (UtilMath.offset(_loc, other.GetLocation()) > _neighbourDist)
				continue;

			AddNeighbour(other);
			other.AddNeighbour(this);
		}

		for (FieldOreData other : Field.GetActive())
		{
			if (other.equals(this))
				continue;

			if (UtilMath.offset(_loc, other.GetLocation()) > _neighbourDist)
				continue;

			AddNeighbour(other);
			other.AddNeighbour(this);
		}
	}

	public void AddNeighbour(FieldOreData other)
	{
		_neighbours.add(other);
	}

	public void RemoveNeighbour(FieldOreData other) 
	{
		_neighbours.remove(other);
	}

	public Location GetLocation()
	{
		return _loc;
	}

	public boolean IsActive()
	{
		return _active;
	}

	public void OreMined(Player player, Location source)
	{
		//Persist
		OreLoot(player, source);

		if (Math.random() > 0.9)
			return;

		Field.GetInactive().add(this);
		Field.GetActive().remove(this);
		
		_loc.getBlock().setType(Material.STONE);

		_active = false;
	}

	@SuppressWarnings("deprecation")
	public void OreLoot(Player player, Location source)
	{
		ItemStack stack = null;

		Block block = _loc.getBlock();

		block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getTypeId());

		if (block.getType() == Material.IRON_ORE)				stack = ItemStackFactory.Instance.CreateStack(Material.IRON_INGOT);
		else if (block.getType() == Material.GOLD_ORE)			stack = ItemStackFactory.Instance.CreateStack(Material.GOLD_INGOT);
		else if (block.getType() == Material.DIAMOND_ORE)		stack = ItemStackFactory.Instance.CreateStack(Material.DIAMOND);
		else if (block.getType() == Material.LAPIS_ORE)			stack = ItemStackFactory.Instance.CreateStack(Material.INK_SACK, (byte)4);
		else if (block.getType() == Material.COAL_ORE)			stack = ItemStackFactory.Instance.CreateStack(Material.COAL, 2);
		else if (block.getType() == Material.REDSTONE_ORE)		stack = ItemStackFactory.Instance.CreateStack(Material.REDSTONE, 3);

		if (stack == null)
		{
			return;
		}
		
		if (!Managers.get(PvPTimerManager.class).handleMining(player, block, false, stack, false))
		{
			Vector vec = UtilAlg.getTrajectory(_loc.getBlock().getLocation().add(0.5, 0.5, 0.5), source).normalize();
			
			Item item = _loc.getWorld().dropItem(_loc.getBlock().getLocation().add(0.5, 0.5, 0.5).add(vec), stack);
			item.setPickupDelay(40);
		}
	}

	public void StartVein(int veinSize) 
	{
		_loc.getBlock().setType(OreSelect());

		Field.GetInactive().remove(this);
		Field.GetActive().add(this);

		_active = true;

		//Spread
		OreSpread(veinSize);
	}

	public void OreSpread(int veinSize) 
	{
		if (veinSize <= 0)
			return;
		
		//Spread To...
		FieldOreData closest = null;
		double dist = 10;

		for (FieldOreData other : _neighbours)
		{	
			if (other.IsActive())
				continue;

			if (UtilMath.offset(_loc, other.GetLocation()) > dist)
				continue;

			closest = other;
		}

		if (closest == null)
			return;
		
		closest.StartVein(veinSize - 1);
	}

	public Material OreSelect()
	{
		//Similar
		FieldOreData closest = null;
		double dist = 10;

		for (FieldOreData other : _neighbours)
		{	
			if (!other.IsActive())
				continue;

			if (UtilMath.offset(_loc, other.GetLocation()) > dist)
				continue;

			closest = other;
		}

		if (closest != null)
			return closest.GetLocation().getBlock().getType();


		//Spread
		double rand = Math.random();

		if (rand < 0.32)		return Material.IRON_ORE;
		if (rand < 0.64)		return Material.GOLD_ORE;
		if (rand < 0.96)		return Material.DIAMOND_ORE;
		if (rand < 0.98)		return Material.COAL_ORE;
		//if (rand < 0.97)		return Material.REDSTONE_ORE;
								return Material.LAPIS_ORE;
	}

	public void Delete()
	{
		for (FieldOreData other : _neighbours)
			other.RemoveNeighbour(this);
		
		Field.getLocationMap().remove(_loc);

		_neighbours.clear();
		_neighbours = null;
		_loc = null;
		
		System.out.println("Deleted");
	}

	public void SetActive(boolean active) 
	{
		_active = active;
		_loc.getBlock().setType(Material.STONE);
	}
}
