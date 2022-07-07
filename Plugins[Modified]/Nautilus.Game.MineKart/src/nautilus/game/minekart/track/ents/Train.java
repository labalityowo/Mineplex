package nautilus.game.minekart.track.ents;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftCreature;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilPlayer;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.kart.KartState;
import nautilus.game.minekart.kart.crash.Crash_Explode;
import nautilus.game.minekart.track.Track;
import nautilus.game.minekart.track.TrackEntity;
import net.minecraft.server.v1_7_R1.EntityCreature;

public class Train extends TrackEntity
{
	private Location _next = null;
	private Location _past = null;
	
	public Train(Track track, Location loc) 
	{
		super(track, EntityType.IRON_GOLEM, "Golem Train", 5, 1.6, 200, loc);
		
		_past = new Location(GetLocation().getWorld(), GetLocation().getX(), GetLocation().getY(), GetLocation().getZ());
		_next = _past;
	}

	@Override
	public void Collide(Kart kart) 
	{
		if (kart.GetKartState() == KartState.Crash)
			return;
		
		if (!kart.IsInvulnerable(false))
		{

			UtilPlayer.message(kart.GetDriver(), 		F.main("MK", "You hit " + F.item(GetName()) + "."));

			//Crash
			kart.CrashStop();
			new Crash_Explode(kart, 1.4f, false);
		}

		//Effect
		GetEntity().getWorld().playSound(GetEntity().getLocation(), Sound.EXPLODE, 2f, 0.2f);
	}
	
	@Override
	public void Movement()
	{
		/*
		if (_train >= 4)
			return;
		
		//Put on tracks
		Location loc = FindTarget();
		
		if (loc == null)
			return;

		_past = _next;
		_next = loc;
		
		GetEntity().teleport(_next);
		
		//Project Forward
		loc = FindTarget();
		
		if (loc == null)
			return;

		_past = _next;
		_next = loc;
		
		GetEntity().setVelocity(UtilAlg.getTrajectory(_past, _next).multiply(0.5));
		
		Minecart cart = (Minecart)GetEntity();
		cart.setSlowWhenEmpty(false);
		
		SetEntity(null);
		
		_past = new Location(GetLocation().getWorld(), GetLocation().getX(), GetLocation().getY(), GetLocation().getZ());
		_next = _past;

		_carriage++;
		
		if (_carriage >= 8)
		{
			_train++;
			_carriage = 0;
			SetSpawnTimer(System.currentTimeMillis() + 6000);
		}
		*/
		if (_next.equals(_past))
		{
			//Get First
			Location loc = FindTarget();
			
			if (loc == null)
				return;

			_next = loc;
			
			if (_next.getBlock().getRelative(BlockFace.NORTH).equals(_past.getBlock()))
			{
				_past = _next;
				_next = _next.getBlock().getRelative(BlockFace.WEST).getLocation().add(0.5, 0, 0.5);
			}
			else if (_next.getBlock().getRelative(BlockFace.SOUTH).equals(_past.getBlock()))
			{
				_past = _next;
				_next = _next.getBlock().getRelative(BlockFace.EAST).getLocation().add(0.5, 0, 0.5);
			}
			else if (_next.getBlock().getRelative(BlockFace.EAST).equals(_past.getBlock()))
			{
				_past = _next;
				_next = _next.getBlock().getRelative(BlockFace.NORTH).getLocation().add(0.5, 0, 0.5);
			}	
			else if (_next.getBlock().getRelative(BlockFace.WEST).equals(_past.getBlock()))
			{
				_past = _next;
				_next = _next.getBlock().getRelative(BlockFace.SOUTH).getLocation().add(0.5, 0, 0.5);
			}
		}
		else
		{
			Location loc = FindTarget();
			
			if (loc == null)
				return;

			_past = _next;
			_next = loc;
			
			GetEntity().teleport(_next);
			SetSpawnTimer(System.currentTimeMillis());
			
			if (GetEntity() instanceof Creature)
			{
				EntityCreature ec = ((CraftCreature)GetEntity()).getHandle();
				
				//Direction
				ec.setPositionRotation(GetEntity().getLocation().getX(), GetEntity().getLocation().getY(), GetEntity().getLocation().getZ(), 
						UtilAlg.GetYaw(UtilAlg.getTrajectory2d(_past, _next)), 0);
			}
		}
		
		/*
		if (UtilMath.offset(_next, GetEntity().getLocation()) < 0.5)
		{
			Location loc = FindTarget();
			
			if (loc == null)
				return;

			_past = _next;
			_next = loc;
		}	
		else
		{
			
			if (GetEntity() instanceof Creature)
			{
				EntityCreature ec = ((CraftCreature)GetEntity()).getHandle();
				Navigation nav = ec.getNavigation();
				nav.a(_next.getX(), _next.getY(), _next.getZ(), 0.4f);
			}
			
			if (UtilTime.elapsed(GetSpawnTimer(), 200))
			{
				GetEntity().teleport(_next);
				SetSpawnTimer(System.currentTimeMillis());
			}
		}
		*/
	}
	
	public Location FindTarget()
	{
		ArrayList<Block> tracks = new ArrayList<Block>();
		Block check;
		
		check = _next.getBlock().getRelative(0, 0,  1);
		if (check.getTypeId() == 66 || check.getTypeId() == 27)		tracks.add(check);
		
		check = _next.getBlock().getRelative( 1, 0, 0);
		if (check.getTypeId() == 66 || check.getTypeId() == 27)		tracks.add(check);
		
		check = _next.getBlock().getRelative(0, 0, -1);
		if (check.getTypeId() == 66 || check.getTypeId() == 27)		tracks.add(check);
		
		check = _next.getBlock().getRelative(-1, 0, 0);
		if (check.getTypeId() == 66 || check.getTypeId() == 27)		tracks.add(check);
		
		tracks.remove(_past.getBlock());
		
		if (tracks.isEmpty())
			return null;
		
		return tracks.get(0).getLocation().add(0.5, 0, 0.5);
	}
}
