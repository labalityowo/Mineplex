package nautilus.game.minekart.track;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import mineplex.core.MiniPlugin;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.fakeEntity.FakeEntityManager;
import mineplex.core.teleport.Teleport;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import nautilus.game.minekart.gp.GPBattle;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.kart.KartState;
import nautilus.game.minekart.track.Track.TrackState;
import net.minecraft.server.v1_7_R1.ChunkPreLoadEvent;
import net.minecraft.server.v1_7_R1.EntityPlayer;
import net.minecraft.server.v1_7_R1.Packet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class TrackManager extends MiniPlugin
{
	private Teleport _teleport;
	private HashSet<Track> _tracks = new HashSet<Track>();
	private HashSet<Track> _trackLoader = new HashSet<Track>();

	public TrackManager(JavaPlugin plugin, Teleport teleport) 
	{
		super("Track Manager", plugin);
		
		_teleport = teleport;
	}

	public Set<Track> GetTracks()
	{
		return _tracks;
	}

	public void RegisterTrack(Track track)
	{
		_tracks.add(track);
	}

	public void LoadTrack(Track track)
	{
		_trackLoader.add(track);
	}

	@EventHandler
	public void LoadTrackUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		Iterator<Track> trackIterator = _trackLoader.iterator();

		long endTime = System.currentTimeMillis() + 25;

		while (trackIterator.hasNext())
		{	
			long timeLeft = endTime - System.currentTimeMillis();
			if (timeLeft <= 0)	continue;

			final Track track = trackIterator.next();

			if (track.GetWorld() == null)
			{
				trackIterator.remove();
			}
			else if (track.LoadChunks(timeLeft))
			{
				trackIterator.remove();
				track.SetState(TrackState.Countdown);
				track.GetGP().Announce(F.main("MK", "Starting Track: " + F.elem(track.GetName())));
				
				track.SpawnTeleport();
				
				for (final Kart kart : track.GetGP().GetKarts())
				{
					kart.GetEntity().SetLocation(kart.GetDriver().getLocation());
					
					Packet spawnPacket = kart.GetEntity().Spawn();
					Packet attachPacket = kart.GetEntity().SetPassenger(kart.GetDriver().getEntityId());
					
					for (final Kart otherPlayer : track.GetGP().GetKarts())
					{
						if (kart == otherPlayer)
							continue;
						
						kart.GetDriver().hidePlayer(otherPlayer.GetDriver());
						
						final EntityPlayer entityPlayer = ((CraftPlayer)otherPlayer.GetDriver()).getHandle();
						
						entityPlayer.playerConnection.sendPacket(spawnPacket);
						FakeEntityManager.Instance.ForwardMovement(otherPlayer.GetDriver(), kart.GetDriver(), kart.GetEntity().GetEntityId());
						FakeEntityManager.Instance.FakeVehicle(otherPlayer.GetDriver(), kart.GetDriver().getEntityId(), attachPacket);
					}
				}
				
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(GetPlugin(), new Runnable()
				{
					public void run()
					{
						for (Kart kart : track.GetGP().GetKarts())
						{
							for (Kart player : track.GetGP().GetKarts())
							{
								if (kart.GetDriver() == player)
									continue;
			
								kart.GetDriver().showPlayer(player.GetDriver());
							}
							
							kart.Equip();
						}
					}
				}, 5L);
			}
		}
	}

	@EventHandler
	public void DeleteTrack(UpdateEvent event)
	{
		if (event.getType() == UpdateType.TICK)
			for (Track track : _tracks)
				if (track.GetWorld() != null)
					if (track.GetState() == TrackState.Ended)
						if (track.GetWorld().getPlayers().isEmpty())
							track.Uninitialize();
	}

	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() == UpdateType.TICK)
			for (Track track : _tracks)
				if (!(track.GetGP() instanceof GPBattle))
					if (track.GetWorld() != null)
						TrackLogic.Positions(track);

		if (event.getType() == UpdateType.TICK)
			for (Track track : _tracks)
				if (track.GetWorld() != null)
					TrackLogic.Jump(track);

		if (event.getType() == UpdateType.FAST)
			for (Track track : _tracks)
				if (track.GetWorld() != null)
					TrackLogic.UpdateItems(track);
		
		if (event.getType() == UpdateType.TICK)
			for (Track track : _tracks)
				if (track.GetWorld() != null)
					TrackLogic.UpdateEntities(track);

		if (event.getType() == UpdateType.TICK)
			for (Track track : _tracks)
				if (track.GetWorld() != null)
					TrackLogic.PickupItem(track);
		
		if (event.getType() == UpdateType.TICK)
			for (Track track : _tracks)
				if (track.GetWorld() != null)
					TrackLogic.CollideMob(track);
	}

	@EventHandler
	public void BlockIgnite(BlockIgniteEvent event)
	{
		if (event.getCause() == IgniteCause.ENDER_CRYSTAL)
			event.setCancelled(true);
	}

	@EventHandler
	public void PlayerIgnite(EntityCombustEvent event)
	{
		if (event.getDuration() < 100)
			event.setCancelled(true);
	}

	@EventHandler
	public void ChunkUnload(ChunkUnloadEvent event)
	{
		for (Track track : _tracks)
			track.ChunkUnload(event);
	}

	@EventHandler
	public void ChunkLoad(ChunkPreLoadEvent event)
	{
		for (Track track : _tracks)
		{
			track.ChunkLoad(event); 
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void Lakitu(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		for (Track track : _tracks)
		{
			if (track.GetWorld() == null)
				continue;

			if (track.GetState() != TrackState.Live)
				continue;

			for (Kart kart : track.GetGP().GetKarts())
			{
				kart.SetLakituTick(-1);

				if (kart.GetKartState() == KartState.Lakitu)
					continue;		

				//Get Return
				int locId = -1;
				double bestDist = 9999;		

				for (int i=0 ; i<kart.GetGP().GetTrack().GetReturn().size() ; i++)
				{
					Location loc = kart.GetGP().GetTrack().GetReturn().get(i);
					
					double dist = UtilMath.offset(loc, kart.GetDriver().getLocation());

					if (locId == -1)
					{
						locId = i;
						bestDist = dist;
					}

					else if (dist < bestDist)
					{
						locId = i;
						bestDist = dist;
					}
				}

				if (locId == -1)
					continue;

				//Distance
				if (bestDist > 120)
				{
					Lakitu(kart, locId);
					continue;
				}

				Block block;

				//Void
				if (kart.GetDriver().getLocation().getY() < 0)
				{
					Lakitu(kart, locId);
					continue;
				}

				//Drown
				block = kart.GetDriver().getLocation().getBlock().getRelative(BlockFace.UP);
				if (block.getTypeId() == 8 || block.getTypeId() == 9)
				{
					int neighbours = 0;

					if (block.getRelative( 1, 0, 0).getTypeId() == 8 || block.getRelative(1, 0, 0).getTypeId() == 9) neighbours++;
					if (block.getRelative(-1, 0, 0).getTypeId() == 8 || block.getRelative(1, 0, 0).getTypeId() == 9) neighbours++;
					if (block.getRelative(0, 0,  1).getTypeId() == 8 || block.getRelative(1, 0, 0).getTypeId() == 9) neighbours++;
					if (block.getRelative(0, 0, -1).getTypeId() == 8 || block.getRelative(1, 0, 0).getTypeId() == 9) neighbours++;

					if (neighbours >= 3)
					{
						kart.SetLakituTick(2);

						if (kart.GetLakituTick() > 6)
						{
							Lakitu(kart, locId);
							continue;
						}
					}		
				}

				//Negative Blocks
				block = kart.GetDriver().getLocation().getBlock().getRelative(BlockFace.DOWN);
				if (kart.GetGP().GetTrack().GetReturnBlocks().contains(block.getTypeId()))
				{
					kart.SetLakituTick(2);

					if (kart.GetLakituTick() > 6)
					{
						Lakitu(kart, locId);
						continue;
					}
				}
			}
		}
	}

	public void Lakitu(Kart kart, int locId)
	{
		kart.SetVelocity(new Vector(0,0,0));

		kart.SetKartState(KartState.Lakitu);

		kart.ExpireConditions();

		kart.SetLakituTick(-1000);

		kart.LoseLife();
		
		//Get Locations
		Location loc;
		if (locId > 0)	loc = kart.GetGP().GetTrack().GetReturn().get(locId-1);
		else			loc = kart.GetGP().GetTrack().GetReturn().get(kart.GetGP().GetTrack().GetReturn().size());
			
		Location next = kart.GetGP().GetTrack().GetReturn().get(locId);

		Vector dir = UtilAlg.getTrajectory(loc, next);
		loc.setYaw(UtilAlg.GetYaw(dir));
		loc.setPitch(0);
		
		_teleport.TP(kart.GetDriver(), loc);
		
		UtilPlayer.message(kart.GetDriver(), F.main("MK", "You are being returned to the track."));
		UtilPlayer.message(kart.GetDriver(), F.main("MK", "You cannot drive for 8 seconds."));

		kart.GetDriver().playSound(kart.GetDriver().getLocation(), Sound.NOTE_BASS_GUITAR, 2f, 1f);	
	}
}
