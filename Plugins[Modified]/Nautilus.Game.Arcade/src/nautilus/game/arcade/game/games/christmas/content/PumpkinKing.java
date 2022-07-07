package nautilus.game.arcade.game.games.christmas.content;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.utils.UtilVariant;

import nautilus.game.arcade.game.games.christmas.ChristmasAudio;
import nautilus.game.arcade.game.games.christmas.parts.Part5;

public class PumpkinKing 
{
	private Part5 Host;
	
	private Skeleton _ent;
	private Location _target;
	
	private ArrayList<Location> _grid;
	private ArrayList<TNTPrimed> _tnt = new ArrayList<TNTPrimed>();
	
	private long _lastTNT = 0;
	
	public PumpkinKing(Part5 host, Location loc, ArrayList<Location> grid)
	{
		Host = host;
		
		_grid = grid;
		
		Host.Host.CreatureAllowOverride = true;
		_ent = UtilVariant.spawnWitherSkeleton(loc);
		Host.Host.CreatureAllowOverride = false;
		UtilEnt.vegetate(_ent);
		UtilEnt.ghost(_ent, true, false);
		
		_ent.getEquipment().setHelmet(new ItemStack(Material.PUMPKIN));
		_ent.getEquipment().setItemInHand(new ItemStack(Material.TNT));
		
		_ent.setCustomName("The Pumpkin King");
		_ent.setCustomNameVisible(true);
		
		_ent.setRemoveWhenFarAway(false);
		
		_ent.getWorld().strikeLightningEffect(_ent.getLocation());
	}

	public boolean IsDead() 
	{
		return (_ent != null && !_ent.isValid());
	}
	
	public void SetTarget(Location loc)
	{
		_target = loc;
	}
	
	public Location GetTarget()
	{
		return _target;
	}

	public Entity GetEntity() 
	{
		return _ent;
	}
	
	public void MoveUpdate()
	{
		if (IsDead())
			return;
		
		if (_target == null || UtilMath.offset(_ent.getLocation(), _target) < 1)
			SetTarget(UtilAlg.Random(_grid).clone().add(0, 0.5, 0));
		
		else
			UtilEnt.CreatureMoveFast(_ent, _target, (float) (1.2 + (0.06 * Host.GetState())));
	}
	
	public void TNTUpdate()
	{
		if (IsDead())
			return;
		
		if (!UtilTime.elapsed(Host.GetStateTime(), 4000))
			return;
		
		if (!UtilTime.elapsed(_lastTNT, 6000 - (200 * Host.GetState())))
			return;
		
		_lastTNT = System.currentTimeMillis();
		
		Player player = UtilAlg.Random(Host.Host.GetPlayers(true));
		
		TNTPrimed tnt = _ent.getWorld().spawn(_ent.getEyeLocation(), TNTPrimed.class);
		
		UtilAction.velocity(tnt, UtilAlg.getTrajectory(tnt, player), 1, false, 0, 0.2, 10, false);
		
		double mult = 0.5 + (0.6 * (UtilMath.offset(tnt, player)/24d));
		
		//Velocity
		tnt.setVelocity(player.getLocation().toVector().subtract(tnt.getLocation().toVector()).normalize().add(new Vector(0, 0.4, 0)).multiply(mult));
		
		_tnt.add(tnt);
	}

	public void StayIdle() 
	{
		if (IsDead())
			return;
		
		UtilEnt.CreatureMoveFast(_ent, _ent.getLocation().getBlock().getLocation().add(0.5, 0, 0.5), 0.6f);
	}

	public void Die() 
	{
		UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, _ent.getLocation(), 0, 1, 0, 0, 1,
				ViewDist.LONGER, UtilServer.getPlayers());
		UtilParticle.PlayParticle(ParticleType.LAVA, _ent.getLocation(), 0.25f, 1, 0.25f, 0, 50,
				ViewDist.LONGER, UtilServer.getPlayers());
		
		_ent.getWorld().playSound(_ent.getLocation(), Sound.ENDERDRAGON_DEATH, 4f, 0.5f);
		
		Host.Host.BossSay("Pumpkin King", "NOOOOOOOOOOOOOO!!!!!!", ChristmasAudio.END_WIN);
		
		_ent.remove();
	}
}
