package nautilus.game.arcade.game.games.valentines.tutorial;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Item;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.valentines.Valentines;
import nautilus.game.arcade.gametutorial.GameTutorial;
import nautilus.game.arcade.gametutorial.TutorialPhase;

public class TutorialValentines extends GameTutorial
{
	private Valentines Host;

	private Cow _cowBoy;
	private Cow _cowGirl;
	private HashMap<Pig, Integer> _pigs = new HashMap<Pig, Integer>();

	private Location _pigSpawn;
	private Location _pigWaypointA;
	private Location _pigWaypointB;

	public TutorialValentines(Valentines valentines, ArcadeManager manager) 
	{
		super(manager, new TutorialPhase[]{new TutorialPhaseValentines()});

		Host = valentines;
		
		PlayTutorialSounds = true;
	}

	@Override
	public void onTick(int tick)
	{
		if (!hasStarted())
			return;
		
		if (getCurrentPhase() == null || getCurrentPhase().getCurrentText() == null)
			return;
		
		cowGirlUpdate(tick);
		cowBoyUpdate(tick);
		pigUpdate(tick);
		
		lookUpdate();

	}

	private void lookUpdate() 
	{
		if (getCurrentPhase().getCurrentText().ID() == 2)
			getPhase(1).setTarget(_cowBoy.getLocation().add(0, 2, 0));

		if (getCurrentPhase().getCurrentText().ID() == 3)
			getPhase(1).setTarget(_cowBoy.getLocation().add(0, 2, 0));
		
		if (getCurrentPhase().getCurrentText().ID() == 4)
			getPhase(1).setTarget(Host.WorldData.GetDataLocs("RED").get(0).clone().add(0, 2, 0));
		
		if (getCurrentPhase().getCurrentText().ID() == 5)
			getPhase(1).setTarget(_pigWaypointA.clone().add(0, 2, 0));
	}

	private void cowGirlUpdate(int tick) 
	{
		if (getCurrentPhase().getCurrentText().ID() >= 4)
		{
			Location loc = _cowGirl.getLocation().add(UtilAlg.getTrajectory(_pigWaypointA, _cowGirl.getLocation()).multiply(5));
			
			UtilEnt.CreatureMoveFast(_cowGirl, loc, 1.4f);
		}			
	}
	
	private void cowBoyUpdate(int tick) 
	{
		if (getCurrentPhase().getCurrentText().ID() == 5)
		{
			Location loc = _cowBoy.getLocation().add(UtilAlg.getTrajectory(_cowBoy.getLocation(), _pigSpawn).multiply(5));
			
			UtilEnt.CreatureMoveFast(_cowBoy, loc, 1f);
		}			
	}

	private void pigUpdate(int tick) 
	{
		//Pigs
		if (getCurrentPhase().getCurrentText().ID() > 5)
		{
			//Spawn
			if (_pigs.size() < 5 && tick % 15 == 0)
			{
				Host.CreatureAllowOverride = true;

				//Spawn
				Pig pig = _pigSpawn.getWorld().spawn(_pigSpawn, Pig.class);
				UtilEnt.vegetate(pig);


				//Item
				Item item = pig.getWorld().dropItem(pig.getLocation(), new ItemStack(Material.CHEST));
				pig.setPassenger(item);

				_pigs.put(pig, 0);;

				Host.CreatureAllowOverride = false;
			}

			//Move		
			for (Entry<Pig,Integer> data : _pigs.entrySet())
			{
				//Move
				if (data.getValue() == 0)
				{
					if (UtilMath.offset(data.getKey().getLocation(), _pigWaypointA) > 1)
					{
						UtilEnt.CreatureMoveFast(data.getKey(), _pigWaypointA, 1.4f);
					}
					else
					{
						data.setValue(1);
					}
				}
				if (data.getValue() == 1)
				{
					if (UtilMath.offset(data.getKey().getLocation(), _pigWaypointB) > 1)
					{
						UtilEnt.CreatureMoveFast(data.getKey(), _pigWaypointB, 1.2f);
					}
					else
					{
						data.setValue(2);
					}
				}
				
				//Oink
				if (Math.random() > 0.98)
				{
					data.getKey().getWorld().playSound(data.getKey().getLocation(), Sound.PIG_IDLE, 1.5f, 1f);
				}
			}
		}
	}

	@Override
	public void onStart() 
	{
		//Pig Spawn
		_pigSpawn = Host.WorldData.GetDataLocs("PINK").get(0);

		//Pig Waypoints
		if (UtilMath.offset(_pigSpawn, Host.WorldData.GetDataLocs("ORANGE").get(0)) <
				UtilMath.offset(_pigSpawn, Host.WorldData.GetDataLocs("ORANGE").get(1)))
		{
			_pigWaypointA = Host.WorldData.GetDataLocs("ORANGE").get(0);
			_pigWaypointB = Host.WorldData.GetDataLocs("ORANGE").get(1);
		}
		else
		{
			_pigWaypointA = Host.WorldData.GetDataLocs("ORANGE").get(1);
			_pigWaypointB = Host.WorldData.GetDataLocs("ORANGE").get(0);
		}

		//Spawn Cows
		Host.CreatureAllowOverride = true;

		_cowBoy = _pigSpawn.getWorld().spawn(Host.WorldData.GetDataLocs("BROWN").get(0), Cow.class);
		_cowBoy.setCustomName(C.cGreenB + "Calvin");
		_cowBoy.setCustomNameVisible(true);
		UtilEnt.vegetate(_cowBoy);

		_cowGirl = _pigSpawn.getWorld().spawn(Host.WorldData.GetDataLocs("RED").get(0), MushroomCow.class);
		_cowGirl.setCustomName(C.cRedB + "Moolanie");
		_cowGirl.setCustomNameVisible(true);
		UtilEnt.vegetate(_cowGirl);

		Host.CreatureAllowOverride = false;
		
		
		//Player Data
		getPhase(1).setLocation(Host.WorldData.GetDataLocs("WHITE").get(0));
		getPhase(1).setTarget(_cowGirl.getLocation().add(0, 2, 0));
	}

	@Override
	public void onEnd()
	{
		_cowBoy.remove();
		_cowGirl.remove();
		
		for (Pig pig : _pigs.keySet())
		{
			if (pig.getPassenger() != null)
				pig.getPassenger().remove();
			
			pig.remove();
		}
		_pigs.clear();
		
		for (Player player : getPlayers().keySet())
		{
			Host.Manager.GetCondition().Factory().Blind("Tutorial End", player, player, 4, 0, false, false, false);
		}
		
		UtilTextMiddle.display(C.cGreenB + "Calvin", "Punch the Pigs to get my items back!", 0, 100, 20, UtilServer.getPlayers());
	}
}
