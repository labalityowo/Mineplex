package nautilus.game.arcade.game.games.christmas.parts;

import java.util.ArrayList;
import java.util.Iterator;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.disguise.disguises.DisguiseSpider;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.games.christmas.Christmas;
import nautilus.game.arcade.game.games.christmas.ChristmasAudio;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Block;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Spider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class Part2 extends Part 
{
	private ArrayList<Location> _spiders;
	private ArrayList<Location> _switches;
	private ArrayList<Location> _switchLights;
	
	private ArrayList<Location> _switched = new ArrayList<Location>();
	
	private boolean _a = false; 
	private boolean _b = false; 
	
	public Part2(Christmas host, Location sleigh, Location[] presents, ArrayList<Location> spiders, ArrayList<Location> switches, ArrayList<Location> switchLights) 
	{
		super(host, sleigh, presents);

		_spiders = spiders;
		
		_switches = new ArrayList<Location>();
		for (Location loc : switches)
			_switches.add(loc.getBlock().getLocation());
		
		_switchLights = switchLights;

		for (Location loc : _switchLights)
			loc.getBlock().setTypeIdAndData(35, (byte)14, false);
	}

	@Override
	public void Activate()
	{
		for (Location loc : _switches)
			loc.getBlock().setTypeIdAndData(69, (byte)5, false);
	}
	
	@EventHandler
	public void Update(UpdateEvent event) 
	{
		if (event.getType() == UpdateType.FAST)
			UpdateIntroA();	
		
		if (event.getType() == UpdateType.FAST)
			UpdateIntroB();	
		
		if (event.getType() == UpdateType.FAST)
			UpdateSpiders();	
		
		if (event.getType() == UpdateType.FASTER)
			UpdateSpiderLeap();
		
		if (event.getType() == UpdateType.SEC)
		{
			if (_switched.size() == 4 && HasPresents())	
			{
				SetObjectiveText("Wait for the Magic Bridge", 1);
			}
		}
	}
	
	private void UpdateIntroA() 
	{
		if (_a)
			return;

		if (UtilMath.offset(Host.GetSleigh().GetLocation(), GetSleighWaypoint()) > 20)
			return;
		
		_a = true;
		
		Host.SantaSay("Oh no! My magic bridge has been turned off!", ChristmasAudio.P2_A);	
	}
	
	private void UpdateIntroB() 
	{
		if (_b)
			return;

		if (UtilMath.offset(Host.GetSleigh().GetLocation(), GetSleighWaypoint()) > 10)
			return;
		
		_b = true;
		
		Host.SantaSay("Turn on all four switches to rebuild it!", ChristmasAudio.P2_B);	
		SetObjectiveText("Turn on the 4 switches", 0);
	}
	
	private void UpdateSpiders() 
	{
		if (GetCreatures().size() > 40)
			return;
		
		if (!_a)
			return;
		
		//Create
		Location loc = UtilAlg.Random(_spiders);

		Host.CreatureAllowOverride = true;
		Skeleton ent = loc.getWorld().spawn(loc, Skeleton.class);
		Host.CreatureAllowOverride = false;
		DisguiseSpider disguise = new DisguiseSpider(ent);
		Host.Manager.GetDisguise().disguise(disguise);
		
		ent.setHealth(10);

		this.AddCreature(ent);
	}
	
	private void UpdateSpiderLeap() 
	{
		for (Creature ent : GetCreatures().keySet())
		{
			if (!UtilEnt.isGrounded(ent))
				continue;
			
			if (Math.random() > 0.05)
				continue;
			
			Player target = GetCreatures().get(ent);
			if (target == null || !target.isValid())
				continue;	
			
			double flatDist = UtilMath.offset(ent, target);
			
			double yDiff = target.getLocation().getY() - ent.getLocation().getY();
			
			UtilAction.velocity(ent, UtilAlg.getTrajectory(ent, target), Math.min(1.6, 0.2 + (0.1 * flatDist)), false, 0, 0.1 + (0.1 * yDiff), 1.4, true);
			
			ent.getWorld().playSound(ent.getLocation(), Sound.SPIDER_IDLE, 1.5f, 2f);
		}
	}
	
	

	@EventHandler
	public void ToggleSwitch(PlayerInteractEvent event)
	{
		if (event.getClickedBlock() == null)
			return;

		if (!_switches.contains(event.getClickedBlock().getLocation()))
			return;

		if (_switched.contains(event.getClickedBlock().getLocation()))
			return;
		
		event.setCancelled(true);

		if (!Host.IsLive())
			return;

		if (!Host.IsAlive(event.getPlayer()))
			return;

		//Set
		_switched.add(event.getClickedBlock().getLocation());
		
		//Remvoe Switch
		event.getClickedBlock().setType(Material.AIR);
		
		//Visual
		Location bestLoc = null;
		double bestDist = 0;
		
		for (Location loc : _switchLights)
		{
			double dist = UtilMath.offset(event.getClickedBlock().getLocation(), loc);
			
			if (bestLoc == null || bestDist > dist)
			{
				bestLoc = loc;
				bestDist = dist;
			}
		}
		
		bestLoc.getBlock().setData((byte)5);
		UtilFirework.playFirework(bestLoc, FireworkEffect.builder().flicker(true).withColor(Color.GREEN).with(Type.BALL).trail(true).build());
		
		//Announce
		if (_switched.size() == 1)		
		{
			Host.SantaSay("Great job, " + event.getPlayer().getName() + "! Only 3 switches to go!", ChristmasAudio.P2_C);
			SetObjectiveText("Turn on the 4 switches", 0.25);
		}
		else if (_switched.size() == 2)	
		{
			Host.SantaSay("Well done, " + event.getPlayer().getName() + "! Only 2 switches to go!", ChristmasAudio.P2_D);
			SetObjectiveText("Turn on the 4 switches", 0.5);
		}
		else if (_switched.size() == 3)	
		{
			Host.SantaSay("Wonderful, " + event.getPlayer().getName() + "! Only 1 switch to go!", ChristmasAudio.P2_E);
			SetObjectiveText("Turn on the 4 switches", 0.75);
		}
		else if (_switched.size() == 4)	
		{
			Host.SantaSay("Excellent work! The bridge is powering up!", ChristmasAudio.P2_F);

			if (!HasPresents())
				SetObjectivePresents();
		}
	}
	
	@Override
	public boolean CanFinish() 
	{
		return _switched.size() >= 4;
	}
}
