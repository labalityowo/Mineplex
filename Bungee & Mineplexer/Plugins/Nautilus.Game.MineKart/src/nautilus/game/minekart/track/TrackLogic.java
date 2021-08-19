package nautilus.game.minekart.track;

import java.util.ArrayList;
import java.util.Iterator;

import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTime;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.track.Track.TrackState;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public class TrackLogic 
{
	public static void Positions(Track track)
	{
		if (track.GetWorld() == null)
			return;
		
		if (track.GetState() == TrackState.Loading || track.GetState() == TrackState.Ended)
			return;

		for (Kart kart : track.GetGP().GetKarts())
			SetKartProgress(track, kart);

		//Store Scores
		track.GetScores().clear();
		for (Kart kart : track.GetGP().GetKarts())
			track.GetScores().put(kart, kart.GetLapScore());

		
		ArrayList<Kart> pos = track.GetPositions();
		
		//Order Scores
		pos.clear();
		for (Kart kart : track.GetScores().keySet())
		{
			boolean added = false;

			for (int i=0 ; i<pos.size() ; i++)
			{
				//Finished vs Unfinished
				if (kart.HasFinishedTrack() && !pos.get(i).HasFinishedTrack())
				{
					pos.add(i, kart);
					added = true;
					break;
				}
				
				//Unfinished vs Finished
				if (!kart.HasFinishedTrack() && pos.get(i).HasFinishedTrack())
				{
					continue;
				}

				//Finished vs Finished
				if (kart.HasFinishedTrack() && pos.get(i).HasFinishedTrack())
				{
					if (kart.GetLapPlace() < pos.get(i).GetLapPlace())
					{
						pos.add(i, kart);
						added = true;
						break;
					}

					continue;						
				}
				
				//Unfinished vs Unfinished
				if (track.GetScores().get(kart) > track.GetScores().get(pos.get(i)))
				{
					pos.add(i, kart);
					added = true;
					break;
				}
			}

			if (!added)
				pos.add(kart);
		}
		
		//Set Lap Place
		for (int i=0 ; i<pos.size() ; i++)
			pos.get(i).SetLapPlace(i);
		
		if (track.GetState() == TrackState.Live)
		{
			for (Kart kart : track.GetScores().keySet())
			{
				if (kart.HasFinishedTrack())
					track.SetState(TrackState.Ending);
			}
		}
	}
	
	private static void SetKartProgress(Track track, Kart kart) 
	{
		if (kart.HasFinishedTrack())
			return;
		
		int node = -1;
		double bestDist = 9999;

		for (int i=0 ; i<track.GetProgress().size() ; i++)
		{
			Location cur = track.GetProgress().get(i);
			double dist = UtilMath.offset(kart.GetDriver().getLocation(), cur);

			if (node == -1)
			{
				node  = i;
				bestDist = dist;
			}

			else if (dist < bestDist)
			{
				node  = i;
				bestDist = dist;
			}
		}

		if (node == -1)
			return;

		//Score
		double score = 1000 * (node+1);

		Location next = track.GetProgress().get((node+1)%track.GetProgress().size());
		score -= UtilMath.offset(kart.GetDriver().getLocation(), next);

		//Save
		kart.SetLapNode(node);
		kart.SetLapScore(score);
	}

	public static void UpdateItems(Track track)
	{
		if (track.GetWorld() == null)
			return;

		for (TrackItem item : track.GetItems())
		{
			if (item.GetEntity() != null && !item.GetEntity().isValid())
				item.SetEntity(null);

			if (item.GetEntity() != null)
			{
				if (item.GetEntity().getLocation().getY() <= item.GetLocation().getY())
					item.GetEntity().setVelocity(new Vector(0, 0.2, 0));

				item.GetEntity().setTicksLived(1);
			}

			if (!UtilTime.elapsed(item.GetDelay(), 6000))
				continue;

			if (item.GetEntity() == null)
				item.SpawnEntity(track.GetWorld());
		}
	}

	public static void PickupItem(Track track)
	{
		if (track.GetWorld() == null)
			return;

		for (Kart kart : track.GetGP().GetKarts())
		{
			if (kart.HasFinishedTrack())
				continue;
			
			for (TrackItem item : track.GetItems())
			{
				if (item.GetEntity() == null)
					continue;

				if (UtilMath.offset(item.GetLocation(), kart.GetDriver().getLocation()) < 1)
					item.Pickup(kart);
			}
		}
	}
	
	public static void Jump(Track track) 
	{
		for (Kart kart : track.GetGP().GetKarts())
		{	
			Block block = kart.GetDriver().getLocation().getBlock().getRelative(BlockFace.DOWN);
			
			if (block.getType() != Material.EMERALD_BLOCK)
				continue;
			
			if (!track.GetJumps().containsKey(block.getLocation()))
				continue;
			
			if (!track.GetRecharge().use(kart.GetDriver(), "Track Jump", 2000, false))
				continue;
						
			//Current Velocity
			Vector vel = kart.GetVelocity();

			//XXX Special Boost Jumps? 
			//vel.setY(0);
			//vel.normalizerr();
			//vel.multiply(1.8);

			vel.add(new Vector(0,track.GetJumps().get(block.getLocation()),0));
		}
	}

	public static void CollideMob(Track track) 
	{
		for (Kart kart : track.GetGP().GetKarts())
		{
			for (TrackEntity mob : track.GetCreatures())
			{
				mob.CheckCollision(kart);
			}
		}
	}

	public static void UpdateEntities(Track track) 
	{
		Iterator<TrackEntity> iterator = track.GetCreatures().iterator();

		while (iterator.hasNext())
		{	
			TrackEntity ent = iterator.next();
			
			if (ent.Update())
			{
				if (ent.GetEntity() != null)
					ent.GetEntity().remove();
				
				iterator.remove();
			}
				
		}
	}
}
