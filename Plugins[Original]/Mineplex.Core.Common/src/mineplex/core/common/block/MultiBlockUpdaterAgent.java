package mineplex.core.common.block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;

import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutMapChunk;
import net.minecraft.server.v1_8_R3.PacketPlayOutMultiBlockChange;
import net.minecraft.server.v1_8_R3.PacketPlayOutMultiBlockChange.MultiBlockChangeInfo;
import net.minecraft.server.v1_8_R3.TileEntity;
import net.minecraft.server.v1_8_R3.WorldServer;

/**
 * An agent used to easily record and send multi-block update packets to players. The agent handles if the packet should be a 
 * MultiBlock packet or a chunk update. It also supports blocks across multiple chunks.
 */

public class MultiBlockUpdaterAgent
{
	
	private Map<Chunk, List<BlockVector>> _chunks = new HashMap<>();
	
	/**
	 * Add a block to the list of blocks to send to the player. The agent supports blocks across different chunks and 
	 * will not send duplicates.
	 * @param block The block to send. The block is stored using a BlockVector, meaning that when the send method is called, it will use
	 * the material and data found for the block at the moment you call the send method.
	 * @see #send(Collection)
	 */
	public void addBlock(Block block)
	{
		Chunk c = ((CraftChunk)block.getChunk()).getHandle();
		List<BlockVector> list = _chunks.computeIfAbsent(c,chunk -> new ArrayList<>());
		
		if(list.size() >= 64) return;
		
		BlockVector bv = block.getLocation().toVector().toBlockVector();
		if(list.contains(bv)) return;
		list.add(bv);
	}
	
	/**
	 * Sends all the record blocks to all online players. Players out of range will not receive packets.
	 * @see #send(Collection)
	 */
	public void send()
	{
		send(UtilServer.getPlayersCollection());
	}
	
	/**
	 * Clear all blocks for this agent.
	 */
	public void reset()
	{
		_chunks.clear();
	}
	
	/**
	 * Send all the recorded blocks to the provided players. This will only send packets to players in range. If the blocks span multiple
	 * chunks then players will only receive block updates for chunks close to them.
	 * @param players The players which will the packets will be sent to.
	 */
	public void send(Collection<? extends Player> players)
	{
		for(Player p : players)
		{
			for(Chunk c : _chunks.keySet())
			{
				if(!p.getWorld().equals(c.bukkitChunk.getWorld())) continue;
				
				int x = p.getLocation().getChunk().getX();
				int z = p.getLocation().getChunk().getZ();
				
				int chunkDist = Math.max(Math.abs(c.locX-x), Math.abs(c.locZ-z));
				
				if(chunkDist > Bukkit.getViewDistance()) continue;
								
				sendPacket(c, p);
			}
		}
	}
	
	private void sendPacket(Chunk c, Player...players)
	{
		List<BlockVector> list = _chunks.get(c);
		
		if(list == null) return;
		
		if(list.size() >= 64) 
		{
			for(Player p : players)
			{
				int protocol = UtilPlayer.getProtocol(p);
				UtilPlayer.sendPacket(p, new PacketPlayOutMapChunk(protocol, c, true, 65535));
			}
		}
		else
		{
			PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange();
			packet.a = new ChunkCoordIntPair(c.locX, c.locZ);
			packet.b = new MultiBlockChangeInfo[list.size()];
			for(int i = 0; i < list.size(); i++)
			{
				BlockVector bv = list.get(i);
				short xyz = (short)((bv.getBlockX() & 0xF) << 12 | (bv.getBlockZ() & 0xF) << 8 | bv.getBlockY());
				packet.b[i] = packet.new MultiBlockChangeInfo(xyz, c);
			}
			
			for(Player p : players)
			{
				UtilPlayer.sendPacket(p, packet);
			}
		}
		
		Packet<?>[] tileEntities = new Packet[c.tileEntities.size()];
		int i = 0;
		for(TileEntity te : c.tileEntities.values())
		{
			tileEntities[i++] = te.getUpdatePacket();
		}
		for(Player p : players)
		{
			UtilPlayer.sendPacket(p, tileEntities);
			((WorldServer)c.world).getTracker().untrackPlayer(((CraftPlayer)p).getHandle());
		}
		Bukkit.getScheduler().runTaskLater(UtilServer.getPlugin(), () ->
		{
			for(Player p : players)
			{
				((WorldServer)c.world).getTracker().a(((CraftPlayer)p).getHandle(), c);
			}
		}, 5);
	}

}
