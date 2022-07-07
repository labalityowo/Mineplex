package mineplex.game.clans.clans.claimview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.server.v1_8_R3.EnumDirection;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import mineplex.core.MiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.F;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.claimview.commands.ClaimVisualizeCommand;
import mineplex.game.clans.clans.event.ClanDisbandedEvent;
import mineplex.game.clans.clans.event.ClanLeaveEvent;
import mineplex.game.clans.clans.event.PlayerUnClaimTerritoryEvent;
import mineplex.game.clans.core.ClaimLocation;

public class ClaimVisualizer extends MiniPlugin
{
	public enum Perm implements Permission
	{
		VISUALIZE_COMMAND,
	}

	private ClansManager _clansManager;
	
	private List<String> _visualizing;
	
	private NautHashMap<ClanInfo, NautHashMap<ClaimLocation, VisualizedChunkData>> _calculated;
	
	public ClaimVisualizer(JavaPlugin plugin, ClansManager clansManager)
	{
		super("Claim Visualizer", plugin);
		
		_clansManager =	clansManager;
		_visualizing = new ArrayList<>();
		_calculated = new NautHashMap<>();
		
		for (ClanInfo clan : _clansManager.getClanMap().values())
		{
			_calculated.put(clan, new NautHashMap<>());
		}
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.PLAYER.setPermission(Perm.VISUALIZE_COMMAND, true, true);
	}
	
	@Override
	public void addCommands()
	{
		addCommand(new ClaimVisualizeCommand(this));
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOWER)
		{
			return;
		}
		
		_calculated.clear();
		for (ClanInfo clan : _clansManager.getClanMap().values())
		{
			_calculated.put(clan, new NautHashMap<>());
			
			for (ClaimLocation chunk : clan.getClaimSet())
			{
				calculate(clan, chunk);
			}
		}
	}
	
	@EventHandler
	public void runVisualization(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}
		
		for (String name : _visualizing)
		{
			Player player = Bukkit.getPlayer(name);
			
			if (player != null && _clansManager.isInClan(player))
			{
				visualize(player);
			}
		}
	}
	
	private void visualize(Player player)
	{
		for (ClaimLocation claim : _clansManager.getClan(player).getClaimSet())
		{
			if (!_calculated.get(_clansManager.getClan(player)).containsKey(claim))
			{
				calculate(_clansManager.getClan(player), claim);
			}
		}
		
		draw(player, _calculated.get(_clansManager.getClan(player)).values());
	}

	private void draw(Player player, Collection<VisualizedChunkData> chunks)
	{
		for (VisualizedChunkData chunk : chunks)
		{
			if (!chunk.getChunk().getWorld().equals(player.getWorld()))
			{
				// return not break because a clan can't have claims in different worlds.
				return;
			}
			
			if (UtilMath.offset2d(chunk.getChunk().getBlock(0, 0, 0).getLocation(), player.getLocation()) > 36)
			{
				break;
			}
			
			for (int x = 0; x < 16; x++)
			{
				for (int z = 0; z < 16; z++)
				{
					if (chunk.shouldDisplayEdge(x, z) && (z == 0 || z == 15 || x == 0 || x == 15))
					{
						Block block = chunk.getChunk().getBlock(x, 0, z);
						
						UtilParticle.PlayParticle(ParticleType.RED_DUST,
								new Location(
										chunk.getChunk().getWorld(),
										block.getX() + .5,
										UtilBlock.getHighest(player.getWorld(), block.getX(), block.getZ()).getY() + .5,
										block.getZ() + .5),
								new Vector(0f, 0f, 0f), 0f, 1, ViewDist.NORMAL, player);
					}
				}
			}
		}
	}

	private void calculate(ClanInfo clan, ClaimLocation claim)
	{
		Chunk chunk = claim.toChunk();
		
		List<EnumDirection> dirs = new ArrayList<>();
		
		if (!clan.getClaimSet().contains(ClaimLocation.of(chunk.getWorld().getChunkAt(chunk.getX(), chunk.getZ() - 1))))
		{
			dirs.add(EnumDirection.NORTH);
		}
		
		if (!clan.getClaimSet().contains(ClaimLocation.of(chunk.getWorld().getChunkAt(chunk.getX() + 1, chunk.getZ()))))
		{
			dirs.add(EnumDirection.EAST);
		}
		
		if (!clan.getClaimSet().contains(ClaimLocation.of(chunk.getWorld().getChunkAt(chunk.getX(), chunk.getZ() + 1))))
		{
			dirs.add(EnumDirection.SOUTH);
		}
		
		if (!clan.getClaimSet().contains(ClaimLocation.of(chunk.getWorld().getChunkAt(chunk.getX() - 1, chunk.getZ()))))
		{
			dirs.add(EnumDirection.WEST);
		}
		
		VisualizedChunkData cached = new VisualizedChunkData(chunk, dirs);
		
		_calculated.get(clan).put(claim, cached);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		if (isVisualizing(event.getPlayer()))
		{
			disableVisualizer(event.getPlayer());
		}
	}
	
	@EventHandler
	public void onKick(PlayerKickEvent event)
	{
		if (isVisualizing(event.getPlayer()))
		{
			disableVisualizer(event.getPlayer());
		}
	}
	
	@EventHandler
	public void onLeave(ClanLeaveEvent event)
	{
		if (isVisualizing(event.getPlayer().getPlayerName()))
		{
			disableVisualizer(event.getPlayer().getPlayerName());
		}
	}
	
	@EventHandler
	public void onClanDisband(ClanDisbandedEvent event)
	{
		for (Player player : event.getClan().getOnlinePlayers())
		{
			if (isVisualizing(player))
			{
				disableVisualizer(player);
			}
		}
	}
	
	@EventHandler
	public void update(ClanInfo clan)
	{
		_calculated.clear();
		
		for (ClaimLocation claim : clan.getClaimSet())
		{
			calculate(clan, claim);
		}
	}
	
	@EventHandler
	public void onUnclaim(PlayerUnClaimTerritoryEvent event)
	{
		if (event.getClan().getClaimCount() == 1)
		{
			for (Player player : event.getClan().getOnlinePlayers())
			{
				if (isVisualizing(player))
				{
					disableVisualizer(player);
				}
			}
		}
	}
	
	public boolean isVisualizing(Player player)
	{
		return _visualizing.contains(player.getName());
	}
	
	public boolean isVisualizing(String name)
	{
		return _visualizing.contains(name);
	}
	
	public void enableVisualizer(String name)
	{
		enableVisualizer(UtilServer.getServer().getPlayer(name));
	}
	
	public void disableVisualizer(String name)
	{
		disableVisualizer(UtilServer.getServer().getPlayer(name));
	}

	public void toggleVisualizer(Player player)
	{
		if (_visualizing.contains(player.getName()))
		{
			disableVisualizer(player);
		}
		else
		{
			enableVisualizer(player);
		}
	}
	
	public void enableVisualizer(Player player)
	{
		if (player == null)
		{
			return;
		}
		
		if (!_clansManager.isInClan(player))
		{
			UtilPlayer.message(player, F.main("Clans", "You must be in a clan to visualize claims."));
			return;
		}
		
		ClanInfo clan = _clansManager.getClan(player);
		
		if (clan.getClaimCount() == 0)
		{
			UtilPlayer.message(player, F.main("Clans", "Your Clan does not have any claims!"));
			return;
		}
		
		_visualizing.add(player.getName());
		UtilPlayer.message(player, F.main("Clans", "You are now visualizing your claims."));
		
		for (VisualizedChunkData chunk : _calculated.get(clan).values())
		{
			if (!chunk.getChunk().getWorld().equals(player.getWorld()))
			{
				// return not break because a clan can't have claims in different worlds.
				return;
			}
			
			if (UtilMath.offset2d(chunk.getChunk().getBlock(0, 0, 0).getLocation(), player.getLocation()) > 36)
			{
				break;
			}
			
			for (int x = 0; x < 16; x++)
			{
				for (int z = 0; z < 16; z++)
				{
					if (chunk.shouldDisplayEdge(x, z) && (z == 0 || z == 15 || x == 0 || x == 15))
					{
						Block block = chunk.getChunk().getBlock(x, 0, z);
					}
				}
			}
		}
	}
	
	public void disableVisualizer(Player player)
	{
		if (player == null)
		{
			return;
		}
		
		if (!_visualizing.contains(player.getName()))
		{
			UtilPlayer.message(player, F.main("Clans", "You are anot visualizing your claims."));
			return;
		}
		
		_visualizing.remove(player.getName());
		UtilPlayer.message(player, F.main("Clans", "You are no longer visualizing your claims."));
	}
	
}
