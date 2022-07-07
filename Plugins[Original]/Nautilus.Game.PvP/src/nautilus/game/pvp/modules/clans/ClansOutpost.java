package nautilus.game.pvp.modules.clans;

import java.util.ArrayList;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

public class ClansOutpost 
{
	private static int size = 3;
	
	private ClansClan _clan;
	private Location _block;
	private long _created;
	private int _state;
	private ArrayList<ClansOutpostBlock> _build = new ArrayList<ClansOutpostBlock>();

	public ClansOutpost(ClansClan clan, Location block, long created)
	{
		_clan = clan;
		_block = block;
		_created = created;
		_state = 0;

		Build();
		
		clan.Clans.GetOutpostMap().put(clan.GetName(), this);
	}

	public void Build()
	{
		_build = new ArrayList<ClansOutpostBlock>();

		for (int y=-1 ; y <= 6 ; y++)
			for (int x=-size ; x <= size ; x++)
				for (int z=-size ; z <= size ; z++)
				{
					Location loc = new Location(_block.getWorld(), _block.getX()+x, _block.getY()+y, _block.getZ()+z);

					if (_clan.Clans.CUtil().isClaimed(loc))
						continue;
					
					//Clear
					if (x != 0 || z != 0 || y != 0)
						if (loc.getBlock().getTypeId() != 0)
							_build.add(new ClansOutpostBlock(loc, 0, (byte)0));
					
					//Floor
					if (y == -1 && Math.abs(x) <= size-1 && Math.abs(z) <= size-1)
					{
						_build.add(new ClansOutpostBlock(loc, 98, (byte)0));
					}

					//Walls
					if (Math.abs(x) == size || Math.abs(z) == size)
					{
						_build.add(new ClansOutpostBlock(loc, 98, (byte)0));
					}

					//Roof
					if (y == 5 && Math.abs(x) <= size-1 && Math.abs(z) <= size-1)
					{
						_build.add(new ClansOutpostBlock(loc, 44, (byte)13));
					}
				}
		
		for (int y=-1 ; y <= 6 ; y++)
			for (int x=-size ; x <= size ; x++)
				for (int z=-size ; z <= size ; z++)
				{
					Location loc = new Location(_block.getWorld(), _block.getX()+x, _block.getY()+y, _block.getZ()+z);

					if (_clan.Clans.CUtil().isClaimed(loc))
						continue;
					
					//Doors
					if (y == 0 || y == 1)
					{
						if (x == 0 && z == size)
						{
							_build.add(new ClansOutpostBlock(loc, 71, (byte)(y * 8 + 2 + 4)));
						}
						if (x == 0 && z == -size)
						{
							_build.add(new ClansOutpostBlock(loc, 71, (byte)(y * 8 + 4)));
						}
						if (x == size && z == 0)
						{
							_build.add(new ClansOutpostBlock(loc, 71, (byte)(y * 8 + 3 + 4)));
						}
						if (x == -size && z == 0)
						{
							_build.add(new ClansOutpostBlock(loc, 71, (byte)(y * 8 + 1 + 4)));
						}
					}
					
					//Platform
					if (y == 2)
					{
						if (Math.abs(x) == size-1 && Math.abs(z) < size)
						{
							_build.add(new ClansOutpostBlock(loc, 44, (byte)13));
						}
						if (Math.abs(z) == size-1 && Math.abs(x) < size)
						{
							_build.add(new ClansOutpostBlock(loc, 44, (byte)13));
						}
					}
					
					//Windows
					if (y == 4)
					{
						if (Math.abs(x) == size && Math.abs(z) < size-1)
						{
							_build.add(new ClansOutpostBlock(loc, 0, (byte)0));
						}
						if (Math.abs(z) == size && Math.abs(x) < size-1)
						{
							_build.add(new ClansOutpostBlock(loc, 0, (byte)0));
						}
					}
					
					//Ladders
					if (y >= 0 && y < 3)
					{
						if (x == size-1 && z == size-1)
						{
							_build.add(new ClansOutpostBlock(loc, 65, (byte)2));
						}
						if (x == (-size)+1 && z == (-size)+1)
						{
							_build.add(new ClansOutpostBlock(loc, 65, (byte)3));
						}
					}
					
					//Chests
					if (y == 0)
					{
						if (x == size-1 && z == (-size)+1)
						{
							_build.add(new ClansOutpostBlock(loc, 54, (byte)0));
						}
						if (x == (-size)+1 && z == size-1)
						{
							_build.add(new ClansOutpostBlock(loc, 54, (byte)0));
						}
						
						if (x == size-2 && z == (-size)+1)
						{
							_build.add(new ClansOutpostBlock(loc, 54, (byte)0));
						}
						if (x == (-size)+2 && z == size-1)
						{
							_build.add(new ClansOutpostBlock(loc, 54, (byte)0));
						}
					}
					
					//Beacon Floor
					if (y == -1)
					{
						if (Math.abs(x) <= 1 && Math.abs(z) <= 1)
						{
							_build.add(new ClansOutpostBlock(loc, 42, (byte)0));
						}
					}
					
					//Beacon Roof
					if (y == 5)
					{
						if (Math.abs(x) == 1 && Math.abs(z) <= 1)
						{
							_build.add(new ClansOutpostBlock(loc, 98, (byte)0));
						}	
						if (Math.abs(z) == 1 && Math.abs(x) <= 1)
						{
							_build.add(new ClansOutpostBlock(loc, 98, (byte)0));
						}
					}
					
					//Beacon Glass
					if (y == 5 && x == 0 && z == 0)
					{
						_build.add(new ClansOutpostBlock(loc, 20, (byte)0));
					}
				}
	}

	public void BuildUpdate()
	{
		if (_build == null)
			return;

		if (_build.isEmpty())
			return;

		ClansOutpostBlock block = _build.remove(0);

		block.Build();
	}
	
	public void Clean()
	{
		_build = new ArrayList<ClansOutpostBlock>();
		
		for (int y=-1 ; y <= 6 ; y++)
			for (int x=-size ; x <= size ; x++)
				for (int z=-size ; z <= size ; z++)
				{
					Location loc = new Location(_block.getWorld(), _block.getX()+x, _block.getY()+y, _block.getZ()+z);

					if (_clan.Clans.CUtil().isClaimed(loc))
						continue;
					
					//Clear
					if (loc.getBlock().getTypeId() == 0)
						continue;
					
					if (loc.getBlock().getType() == Material.CHEST)
					{
						loc.getBlock().breakNaturally();
						continue;
					}
	
					if (loc.getBlock().getType() == Material.BEACON)
					{
						loc.getBlock().setTypeId(0);
						continue;
					}
					
					if (loc.getBlock().getType() == Material.IRON_BLOCK)
					{
						loc.getBlock().setTypeId(98);
					}
					
					_build.add(new ClansOutpostBlock(loc, loc.getBlock().getTypeId(), (byte)loc.getBlock().getData()));
					loc.getBlock().setTypeId(0);
				}
		
		for (ClansOutpostBlock block : _build)
		{
			//Block
			FallingBlock fall = block.loc.getWorld().spawnFallingBlock(block.loc.add(0.5, 0.5, 0.5), block.id, block.data);

			Vector vec =  UtilAlg.getTrajectory(_block, fall.getLocation());
			if (vec.getY() < 0)			vec.setY(vec.getY() * -1);

			UtilAction.velocity(fall, vec,	1, false, 0, 0.6, 10, false);

			_clan.Clans.Explosion().GetExplosionBlocks().add(fall);
		}
	}

	public Location GetLocation() 
	{
		return _block;
	}

	public ClansClan GetClan()
	{
		return _clan;
	}

	public long GetCreated()
	{
		return _created;
	}

	public int GetState()
	{
		return _state;
	}
	
	public boolean IsOn()
	{
		if (_block.getBlock().getType() != Material.BEACON)
			return false;
		
		if (_block.getBlock().getLightFromSky() != (byte)15)
			return false;
		
		for (int x=-1 ; x<=1 ; x++)
			for (int z=-1 ; z<=1 ; z++)
			{
				if (_block.getBlock().getRelative(x, -1, z).getType() != Material.IRON_BLOCK)
					return false;
			}
		
		return true;
	}

	public boolean Contains(Location loc) 
	{
		if (Math.abs(loc.getBlockX() - _block.getBlockX()) > size)
			return false;
		
		if (Math.abs(loc.getBlockZ() - _block.getBlockZ()) > size)
			return false;
		
		if (loc.getBlockX() == _block.getBlockX())
			if (loc.getBlockZ() == _block.getBlockZ())
				if (loc.getBlockY() >= _block.getBlockY())
					return true;

		if (loc.getBlockY() < _block.getBlockY() - 1 || loc.getBlockY() > _block.getBlockY() + 6)
			return false;

		return true;
	}
}
