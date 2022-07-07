package nautilus.game.arcade.game.games.monstermaze;

import java.util.ArrayList;
import java.util.Iterator;

import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilPlayer;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutBlockBreakAnimation;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class SafePad
{
	private MonsterMaze Host;
//	private Maze _maze;
	
	private Location  _loc;
	
	private int _decayCount = 11;
	
	private ArrayList<SafePadBlock> _blocks = new ArrayList<SafePadBlock>();
	
	public SafePad(MonsterMaze host, Maze maze, Location loc)
	{
		Host = host;
//		_maze = maze;
		
		_loc = loc;
		
		//beacon surface
		for (int x = -2 ; x < 3 ; x++)
		{
			for (int z = -2 ; z < 3 ; z++)
			{
				if (x == 0 && z == 0)
					continue;
				
				_blocks.add(new SafePadBlock(loc.clone().add(x, 0, z), Material.STAINED_CLAY, (byte) 5));
			}
		}
		
		// beacon
		_blocks.add(new SafePadBlock(loc.clone().add(0, 0, 0), Material.BEACON, (byte)0));
		
		// beacon base
		for (int x = -1 ; x < 2 ; x++)
		{
			for (int z = -1 ; z < 2 ; z++)
			{
				_blocks.add(new SafePadBlock(loc.clone().add(x, -1, z), Material.IRON_BLOCK, (byte)0));
			}
			
			//Stairs
			_blocks.add(new SafePadBlock(loc.clone().add(x, -1, 2), Material.QUARTZ_STAIRS, (byte) 7));
			_blocks.add(new SafePadBlock(loc.clone().add(x, -1, -2), Material.QUARTZ_STAIRS, (byte) 6));
			_blocks.add(new SafePadBlock(loc.clone().add(2, -1, x), Material.QUARTZ_STAIRS, (byte) 5));
			_blocks.add(new SafePadBlock(loc.clone().add(-2, -1, x), Material.QUARTZ_STAIRS, (byte) 4));
		}
		
		// corner blocks
		_blocks.add(new SafePadBlock(loc.clone().add(2, -1, 2), Material.QUARTZ_BLOCK, (byte) 1));
		_blocks.add(new SafePadBlock(loc.clone().add(-2, -1, 2), Material.QUARTZ_BLOCK, (byte) 1));
		_blocks.add(new SafePadBlock(loc.clone().add(2, -1, -2), Material.QUARTZ_BLOCK, (byte) 1));
		_blocks.add(new SafePadBlock(loc.clone().add(-2, -1, -2), Material.QUARTZ_BLOCK, (byte) 1));
				
		// air slabs
		for (int x = -2 ; x < 3 ; x++)
		{
			for (int z = -2 ; z < 3 ; z++)
			{
				_blocks.add(new SafePadBlock(loc.clone().add(x, -2, z), Material.AIR, (byte) 0));
			}
		}
	}
	
	public class SafePadBlock
	{
		private Material _origM;
		private byte _origD;
		
		private Material _tempMat;
		private byte _tempData;
		
		Location _loc;
		
		@SuppressWarnings("deprecation")
		public SafePadBlock(Location loc, Material newMat, byte newData)
		{
			_origM = loc.getBlock().getType();
			_origD = loc.getBlock().getData();
			
			_tempMat = newMat;
			_tempData = newData;
			
			_loc = loc;
		}
		
		@SuppressWarnings("deprecation")
		public void setMaterial(Material m)
		{
			_tempMat = m;
			MapUtil.QuickChangeBlockAt(_loc, _tempMat.getId(), _tempData);
		}
		
		@SuppressWarnings("deprecation")
		public void setData(byte b) // for glass changing
		{
			_tempData = b;
			MapUtil.QuickChangeBlockAt(_loc, _tempMat.getId(), _tempData);
		}
		
		public Material getBlockMaterial()
		{
			return _loc.getBlock().getType();
		}
		
		@SuppressWarnings("deprecation")
		public byte getBlockData()
		{
			return _loc.getBlock().getData();
		}
		
		public Location getLocation()
		{
			return _loc;
		}
		
		@SuppressWarnings("deprecation")
		public void build()
		{
			MapUtil.QuickChangeBlockAt(_loc, _tempMat.getId(), _tempData);
//			_loc.getWorld().playEffect(_loc, Effect.STEP_SOUND, _loc.getBlock().getTypeId());
		}
		
		@SuppressWarnings("deprecation")
		public void restore()
		{
			MapUtil.QuickChangeBlockAt(_loc, _origM.getId(), _origD);
//			_loc.getWorld().playEffect(_loc, Effect.STEP_SOUND, _loc.getBlock().getTypeId());
		}
	}
	
	public void build()
	{
		for(SafePadBlock spb : _blocks)
		{
			spb.build();
		}
	}
	
	private void setBreakData(byte newData)
	{
		for (SafePadBlock spb : _blocks)
		{
			if (spb.getBlockMaterial() != Material.STAINED_CLAY) 
				continue;
			
			spb.setData(newData);
		}
	}
	
	public void turnOffBeacon()
	{
		for (SafePadBlock bl : _blocks)
		{
			if (bl.getBlockMaterial() == Material.IRON_BLOCK)
				bl.setMaterial(Material.QUARTZ_BLOCK);
			
			if (bl.getBlockMaterial() == Material.BEACON)
			{
				bl.setMaterial(Material.STAINED_CLAY);
				bl.setData((byte) 5);
			}
		}
	}

	public boolean decay()
	{
		if (_decayCount == -1)
			return true;

		_decayCount--;

		if(_decayCount == 10)
		{
			setBreakData((byte)5); // green
			setCrackedProgress(1);
		}
		else if(_decayCount == 9)
		{
			setCrackedProgress(2);
		}
		else if(_decayCount == 8)
		{
			setBreakData((byte)4); // yellow
			setCrackedProgress(3);
		}
		else if(_decayCount == 7)
		{
			setCrackedProgress(4);
		}
		else if(_decayCount == 6)
		{
			setBreakData((byte)1); // orange
			setCrackedProgress(5);
		}
		else if(_decayCount == 5)
		{
			setCrackedProgress(6);
		}
		else if(_decayCount == 4)
		{
			setBreakData((byte)14); // red
			setCrackedProgress(7);
		}
		else if(_decayCount == 3)
		{
			setCrackedProgress(8);
		}
		else if(_decayCount == 2)
		{
			setCrackedProgress(9);
		}
		else if(_decayCount == 1)
		{
			_decayCount = -1;
			
			setCrackedProgress(-1);

			destroySurface();
			destroyBase();

			return true;
		}
		return false;
	}

	private void setCrackedProgress(int progress)
	{
		int i = 0;
		Iterator<SafePadBlock> iter = _blocks.iterator();
		ArrayList<Packet> packets = new ArrayList<Packet>();
		while(iter.hasNext())
		{
			SafePadBlock spb = iter.next();
			if (!spb.getLocation().getBlock().getType().equals(Material.STAINED_CLAY)) 
				continue;
						
			i++;
			
			Packet packet = new PacketPlayOutBlockBreakAnimation(i, new BlockPosition(spb.getLocation().getBlockX(), spb.getLocation().getBlockY(), spb.getLocation().getBlockZ()), progress);
			packets.add(packet);
		}
		for(Player p : Host.GetPlayers(false))
		{
			Packet[] pcks = new Packet[packets.size()];
			packets.toArray(pcks);
			UtilPlayer.sendPacket(p, pcks);
		}
		/*for(SafePadBlock spb : _blocks)
		{
			if(!spb.getLocation().getBlock().getType().equals(Material.STAINED_GLASS)) continue;
			
			UtilParticle.PlayParticle(ParticleType.BLOCK_DUST.getParticle(Material.STAINED_GLASS, spb.getBlockData()), spb.getLocation(), 0.5f, 0.5f, 0.5f, 0.05f, 8, ViewDist.NORMAL, Host.GetPlayers(false).toArray(new Player[Host.GetPlayers(false).size()]));
			Packet packet = new PacketPlayOutBlockBreakAnimation(1, spb.getLocation().getBlockX(), spb.getLocation().getBlockY(), spb.getLocation().getBlockZ(), progress);
			for(Player p : Host.GetPlayers(false))
			{
				UtilPlayer.sendPacket(p, packet);
			}
		}*/
	}

	public void destroyBase()
	{
		for (final SafePadBlock bl : _blocks)
		{			
			if (bl.getBlockMaterial() == Material.QUARTZ_BLOCK || bl.getBlockMaterial() == Material.QUARTZ_STAIRS || bl.getBlockMaterial() == Material.AIR)
			{
				bl.restore();
			}
		}
	}
	
	public void destroySurface()
	{
		for (final SafePadBlock bl : _blocks)
		{			
			if (bl.getBlockMaterial() == Material.STAINED_CLAY)
			{		
				bl.restore();
			}
		}
	}

	public Location getLocation()
	{
		return _loc;
	}
	
	public boolean isOn(Entity e)
	{
		return UtilAlg.inBoundingBox(e.getLocation(), getLocation().clone().add(2.999, 5, 2), getLocation().clone().add(-2, 0, -2.999));
	}
}
