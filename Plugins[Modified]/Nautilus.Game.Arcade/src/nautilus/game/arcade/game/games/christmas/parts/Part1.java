package nautilus.game.arcade.game.games.christmas.parts;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.games.christmas.Christmas;
import nautilus.game.arcade.game.games.christmas.ChristmasAudio;

public class Part1 extends Part
{
	private ArrayList<Location> _skeletons;
	private ArrayList<Location> _tnt;
	private ArrayList<Location> _blocks;
	private ArrayList<Location> _clear;

	private long _presents = -1; 
	private long _ignited = -1; 
	private long _exploded = -1; 
	private long _cleared = -1; 

	public Part1(Christmas host, Location sleigh, Location[] presents, ArrayList<Location> skeletons, ArrayList<Location> tnt, ArrayList<Location> blocks, ArrayList<Location> clear) 
	{
		super(host, sleigh, presents);

		_skeletons = skeletons;
		_tnt = tnt;
		_blocks = blocks;
		_clear = clear;

		for (Location loc : _blocks)
			if (loc.getBlock().getRelative(BlockFace.UP).getType() == Material.AIR)
				loc.getBlock().setType(Material.SNOW);
			else
				loc.getBlock().setType(Material.STONE);

		//Clear
		Iterator<Location> locIterator = _clear.iterator();
		while (locIterator.hasNext())
		{
			Location loc = locIterator.next();

			if (UtilMath.offset(loc, GetSleighWaypoint()) > 50)
				locIterator.remove();

			loc.getBlock().setType(Material.AIR);
		}
	}

	@Override
	public void Activate()
	{
		Host.SantaSay("Follow me! Let's find those stolen presents!", ChristmasAudio.P1_A);	
	}

	@EventHandler
	public void Update(UpdateEvent event) 
	{
		if (event.getType() != UpdateType.FAST)
			return;
		
		UpdatePresents();	
		UpdateIgnite();	
		UpdateExplode();
		UpdateClear();
		UpdateSkeleton();
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void ExplodeCancel(EntityExplodeEvent event)
	{
		event.setCancelled(true);
		
		event.getEntity().getWorld().playSound(event.getLocation(), Sound.EXPLODE, 4f, 1f);
		UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION, event.getLocation(), 0, 0, 0, 0, 1,
				ViewDist.MAX, UtilServer.getPlayers());
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void ExplodeCancel(ExplosionPrimeEvent event)
	{
		event.setCancelled(true);
		
		event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.EXPLODE, 4f, 1f);
		UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION, event.getEntity().getLocation(), 0, 0, 0, 0, 1,
				ViewDist.MAX, UtilServer.getPlayers());
	}

	private void UpdateSkeleton() 
	{
		if (_ignited < 0)
			return;
		
		if (GetCreatures().size() > 40)
			return;

		//Create
		Location loc = UtilAlg.Random(_skeletons);

		Host.CreatureAllowOverride = true;
		Skeleton skel = loc.getWorld().spawn(loc, Skeleton.class);
		skel.getEquipment().setItemInHand(new ItemStack(Material.WOOD_HOE));
		skel.setHealth(4);
		Host.CreatureAllowOverride = false;

		this.AddCreature(skel);
	}

	private void UpdatePresents() 
	{
		if (_presents > 0)
			return;

		if (UtilMath.offset(Host.GetSleigh().GetLocation(), GetSleighWaypoint()) > 35)
			return;

		_presents = System.currentTimeMillis();

		Host.SantaSay("There are some of the presents up ahead!", ChristmasAudio.P1_B);	
		SetObjectivePresents();
	}

	private void UpdateIgnite() 
	{
		if (_ignited > 0)
			return;

		if (UtilMath.offset(Host.GetSleigh().GetLocation(), GetSleighWaypoint()) > 20)
			return;

		_ignited = System.currentTimeMillis();

		for (Location loc : _tnt)
			loc.getWorld().spawn(loc, TNTPrimed.class);

		Host.SantaSay("LOOK OUT! IT'S A TRAP!!!", ChristmasAudio.P1_C);	
	}

	private void UpdateExplode() 
	{
		if (_exploded > 0)
			return;

		if (_ignited < 0)
			return;

		if (!UtilTime.elapsed(_ignited, 4000))
			return;

		_exploded = System.currentTimeMillis();

		for (Location loc : _blocks)
		{
			Block block = loc.getBlock();

			block.setType(Material.AIR);

			if (Math.random() > 0.66)
				loc.getWorld().spawnFallingBlock(loc, Material.COBBLESTONE, (byte) 0);
		}

		Host.SantaSay("Clear the path! Watch out for the undead!", ChristmasAudio.P1_D);
		SetObjectiveText("Clear a path for Santa's Sleigh!", 1);
	}

	private void UpdateClear() 
	{
		if (_cleared > 0)
			return;

		//Not Exploded
		if (_exploded < 0 || !UtilTime.elapsed(_exploded, 5000))
			return;

		//Not Clear
		for (Location loc : _clear)
			if (loc.getBlock().getType() != Material.AIR)
				return;

		_cleared = System.currentTimeMillis();
	}

	public boolean CanFinish() 
	{
		if (_cleared < 0 || !UtilTime.elapsed(_cleared, 2000))
			return false;

		return true;
	}
	
	
}
